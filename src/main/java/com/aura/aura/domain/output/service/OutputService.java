package com.aura.aura.domain.output.service;

import com.aura.aura.domain.output.dto.FinalizeOutputResponse;
import com.aura.aura.domain.output.dto.request.*;
import com.aura.aura.domain.output.dto.response.*;
import com.aura.aura.domain.output.entity.SessionOutput;
import com.aura.aura.domain.output.enums.VideoStatus;
import com.aura.aura.domain.output.repository.SessionOutputRepository;
import com.aura.aura.domain.session.entity.Session;
import com.aura.aura.domain.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class OutputService {

    private final SessionRepository sessionRepository;
    private final SessionOutputRepository sessionOutputRepository;

    /**
     * 최종 산출물 생성 (SoulTag, QR 등)
     */
    public FinalizeOutputResponse finalizeOutput(
            String publicId,
            FinalizeOutputRequest request
    ) {

        // 1. Session 조회
        Session session = sessionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("세션 없음"));

        // 2. 이미 있으면 가져오고 없으면 생성
        SessionOutput output = sessionOutputRepository.findBySession(session)
                .orElseGet(() -> {
                    SessionOutput newOutput = SessionOutput.create(session);
                    return sessionOutputRepository.save(newOutput);
                });

        output.validateReady();

        // 3. 더미 URL 생성 (나중에 교체)
        String soulTagUrl = "https://dummy/soul-tag/" + publicId;
        String landingUrl = "https://dummy/landing/" + publicId;
        String qrUrl = "https://dummy/qr/" + publicId;

        // 4. 값 세팅
        output.updateAfterFinalize(
                soulTagUrl,
                landingUrl,
                qrUrl,
                LocalDateTime.now().plusHours(48)
        );

        // 5. 응답
        return FinalizeOutputResponse.builder()
                .landingUrl(landingUrl)
                .qrImageUrl(qrUrl)
                .soulTagUrl(soulTagUrl)
                .videoStatus(output.getVideoStatus().name())
                .expiresAt(output.getExpiresAt())
                .build();
    }

    /**
     * 영상 업로드 URL 발급
     */
    public VideoUploadUrlResponse generateVideoUploadUrl(
            String publicId,
            VideoUploadUrlRequest request
    ) {

        Session session = sessionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("세션 없음"));

        SessionOutput output = sessionOutputRepository.findBySession(session)
                .orElseGet(() -> {
                    SessionOutput newOutput = SessionOutput.create(session);
                    return sessionOutputRepository.save(newOutput);
                });

        // 더미 signed url
        String uploadUrl = "https://dummy/upload/" + publicId;

        String objectPath = "videos/" + publicId + "_" + System.currentTimeMillis() + ".mp4";

// 👉 Entity에게 맡김
        output.startUploading(objectPath);

        return VideoUploadUrlResponse.builder()
                .uploadUrl(uploadUrl)
                .objectPath(objectPath)
                .expiresInSeconds(600) // 10분
                .build();
    }

    /**
     * 영상 업로드 완료 처리
     */
    public VideoCompleteResponse completeVideo(
            String publicId,
            VideoCompleteRequest request
    ) {

        Session session = sessionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("세션 없음"));

        SessionOutput output = sessionOutputRepository.findBySession(session)
                .orElseThrow(() -> new IllegalArgumentException("output 없음"));

        // objectPath → 실제 URL 변환 (임시)
        String videoUrl = "https://storage.googleapis.com/your-bucket/"
                + request.getObjectPath();

        // 썸네일은 아직 없음
        String thumbnailUrl = null;

        //경로검증
        String reqPath = request.getObjectPath();

        if (reqPath == null || reqPath.isBlank()) {
            throw new IllegalArgumentException("objectPath 필수");
        }

        if (!reqPath.equals(output.getObjectPath())) {
            throw new IllegalArgumentException("잘못된 objectPath");
        }

        // 엔티티 업데이트
        output.completeVideo(
                videoUrl,
                request.getDurationMs(),
                thumbnailUrl
        );

        // response는 실제 값 기준으로
        return VideoCompleteResponse.builder()
                .videoStatus(output.getVideoStatus().name())
                .build();
    }

    /**
     * 결과 조회
     */
    public OutputResponse getOutput(String publicId) {

        Session session = sessionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("세션 없음"));

        SessionOutput output = sessionOutputRepository.findBySession(session)
                .orElseThrow(() -> new IllegalArgumentException("output 없음"));

        return OutputResponse.from(output);
    }

    public void failVideo(String publicId) {

        Session session = sessionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("세션 없음"));

        SessionOutput output = sessionOutputRepository.findBySession(session)
                .orElseThrow(() -> new IllegalArgumentException("output 없음"));

        output.failVideo();
    }
}