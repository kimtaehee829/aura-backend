package com.aura.aura.domain.output.service;

import com.aura.aura.domain.output.dto.FinalizeOutputResponse;
import com.aura.aura.domain.output.dto.request.*;
import com.aura.aura.domain.output.dto.response.*;
import com.aura.aura.domain.output.entity.SessionOutput;
import com.aura.aura.domain.output.enums.VideoStatus;
import com.aura.aura.domain.output.repository.SessionOutputRepository;
import com.aura.aura.domain.session.entity.Session;
import com.aura.aura.domain.session.repository.SessionRepository;
import com.aura.aura.global.exception.BusinessException;
import com.aura.aura.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class OutputService {
    //더미
    @Value("${app.base-url}")
    private String baseurl;

    @Value("${gcp.bucket-name}")
    private String bucketName;

    private final SessionRepository sessionRepository;
    private final SessionOutputRepository sessionOutputRepository;

    public FinalizeOutputResponse finalizeOutput(String publicId, FinalizeOutputRequest request) {

        Session session = getSession(publicId);
        SessionOutput output = getOrCreateOutput(session);

        output.validateReady();

        output.applyFinalizeData(
                request.getAuraCode(),
                request.getAttachedAccessoryId()
        );

        String soulTagUrl = buildSoulTagUrl(publicId);
        String landingUrl = buildLandingUrl(publicId);
        String qrUrl = buildQrUrl(publicId);

        output.updateAfterFinalize(
                soulTagUrl,
                landingUrl,
                qrUrl,
                LocalDateTime.now().plusHours(48)
        );

        return FinalizeOutputResponse.builder()
                .landingUrl(landingUrl)
                .qrImageUrl(qrUrl)
                .soulTagUrl(soulTagUrl)
                .videoStatus(output.getVideoStatus().name())
                .expiresAt(output.getExpiresAt())
                .build();
    }

    public VideoUploadUrlResponse generateVideoUploadUrl(String publicId, VideoUploadUrlRequest request) {

        Session session = getSession(publicId);
        SessionOutput output = getOrCreateOutput(session);

        if (output.getVideoStatus() != VideoStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_STATUS);
        }

        String objectPath = buildObjectPath(publicId);
        String uploadUrl = baseurl + "/upload/" + publicId;

        output.startUploading(objectPath);

        return VideoUploadUrlResponse.builder()
                .uploadUrl(uploadUrl)
                .objectPath(objectPath)
                .expiresInSeconds(600)
                .build();
    }

    public VideoCompleteResponse completeVideo(String publicId, VideoCompleteRequest request) {

        Session session = getSession(publicId);
        SessionOutput output = getOutput(session);

        if (output.getVideoStatus() != VideoStatus.UPLOADING) {
            throw new BusinessException(ErrorCode.INVALID_STATUS);
        }

        if (!request.getObjectPath().equals(output.getObjectPath())) {
            throw new BusinessException(ErrorCode.INVALID_OBJECT_PATH);
        }

        String videoUrl = buildVideoUrl(request.getObjectPath());

        output.completeVideo(
                videoUrl,
                request.getDurationMs(),
                null // TODO: thumbnail
        );

        return VideoCompleteResponse.builder()
                .videoStatus(output.getVideoStatus())
                .build();
    }

    public OutputResponse getOutput(String publicId) {
        Session session = getSession(publicId);
        SessionOutput output = getOutput(session);
        return OutputResponse.from(output);
    }

    public void failVideo(String publicId) {
        Session session = getSession(publicId);
        SessionOutput output = getOutput(session);
        output.failVideo();
    }

    // ===== private helpers =====

    private Session getSession(String publicId) {
        return sessionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));
    }

    private SessionOutput getOrCreateOutput(Session session) {
        return sessionOutputRepository.findBySession(session)
                .orElseGet(() -> sessionOutputRepository.save(SessionOutput.create(session)));
    }

    private SessionOutput getOutput(Session session) {
        return sessionOutputRepository.findBySession(session)
                .orElseThrow(() -> new BusinessException(ErrorCode.OUTPUT_NOT_FOUND));
    }

    private String buildObjectPath(String publicId) {
        return "videos/" + publicId + "_" + System.currentTimeMillis() + ".mp4";
    }

    private String buildVideoUrl(String objectPath) {
        return "https://storage.googleapis.com/" + bucketName + "/" + objectPath;
    }

    private String buildSoulTagUrl(String publicId) {
        return baseurl + "/soul-tag/" + publicId;
    }

    private String buildLandingUrl(String publicId) {
        return baseurl + "/landing/" + publicId;
    }

    private String buildQrUrl(String publicId) {
        return baseurl + "/qr/" + publicId;
    }
}