package com.aura.aura.domain.output.service;

import com.aura.aura.domain.output.dto.FinalizeOutputResponse;
import com.aura.aura.domain.output.dto.request.*;
import com.aura.aura.domain.output.dto.response.*;
import com.aura.aura.domain.output.entity.SessionOutput;
import com.aura.aura.domain.output.enums.VideoStatus;
import com.aura.aura.domain.output.repository.SessionOutputRepository;
import com.aura.aura.domain.product.dto.AccessoryResponse;
import com.aura.aura.domain.product.dto.ProductResponse;
import com.aura.aura.domain.product.service.AccessoryService;
import com.aura.aura.domain.product.service.ProductService;
import com.aura.aura.domain.session.entity.Session;
import com.aura.aura.domain.session.repository.SessionRepository;
import com.aura.aura.global.exception.BusinessException;
import com.aura.aura.global.exception.ErrorCode;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
@Transactional
public class OutputService {

    private final ProductService productService;
    private final AccessoryService accessoryService;

    //더미
    @Value("${app.base-url}")
    private String baseUrl;

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
        String qrUrl = generateQrImage(publicId, landingUrl);

        output.updateAfterFinalize(
                soulTagUrl,
                landingUrl,
                qrUrl,
                LocalDateTime.now().plusHours(48)
        );

        sessionOutputRepository.save(output);

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

        String videoObjectPath = buildObjectPath(publicId);

        Storage storage = StorageOptions.getDefaultInstance().getService();

        URL videoUrl = storage.signUrl(
                BlobInfo.newBuilder(bucketName, videoObjectPath).build(),
                10, TimeUnit.MINUTES,
                Storage.SignUrlOption.httpMethod(HttpMethod.PUT)
        );

        output.startUploading(videoObjectPath);

        return VideoUploadUrlResponse.builder()
                .uploadUrl(videoUrl.toString())
                .objectPath(videoObjectPath)
                .expiresInSeconds(600)
                .build();

//        String dummyUrl = "https://dummy-upload-url.com";
//
//        output.startUploading(videoObjectPath);
//
//        return VideoUploadUrlResponse.builder()
//                .uploadUrl(dummyUrl)
//                .objectPath(videoObjectPath)
//                .expiresInSeconds(600)
//                .build();
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
                request.getDurationMs()
        );

        return VideoCompleteResponse.builder()
                .videoStatus(output.getVideoStatus().name())
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

    private String generateQrImage(String publicId, String url) {
        try {
            int size = 300;

            BitMatrix matrix = new QRCodeWriter().encode(
                    url,
                    BarcodeFormat.QR_CODE,
                    size,
                    size
            );

            String fileName = "qr_" + publicId + ".png";

// ✅ static 폴더로 저장
            String dir = System.getProperty("user.dir") + "/qr/";

            Path path = Paths.get(dir + fileName);

// 디렉터리 생성
            Files.createDirectories(path.getParent());

            MatrixToImageWriter.writeToPath(matrix, "PNG", path);

// URL 반환
            return baseUrl + "/qr/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException("QR 생성 실패", e);
        }
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
        //return "https://storage.googleapis.com/" + bucketName + "/" + objectPath;
        return "https://dummy-video.com/" + objectPath;
    }

    private String buildSoulTagUrl(String publicId) {
        return baseUrl + "/soul-tag/" + publicId;
    }

    private String buildLandingUrl(String publicId) {
        return baseUrl + "/landing/" + publicId;
    }

    @Transactional(readOnly = true)
    public LandingResponse getLanding(String publicId) {

        Session session = getSession(publicId);
        SessionOutput output = getOutput(session);

        List<ProductResponse> products = productService.getProducts(null);

        // ❗ accessory 가져오기
        List<AccessoryResponse> accessories = accessoryService.getSessionAccessories(publicId);

        AccessoryResponse attachedAccessory = accessories.stream()
                .filter(AccessoryResponse::getIsAttached)
                .findFirst()
                .orElse(null);

        if (output.getVideoStatus() != VideoStatus.READY) {
            return LandingResponse.builder()
                    .videoStatus(output.getVideoStatus().name())
                    .build();
        }

        // ✅ 여기서 소울태그 생성
        SoulTagResponse soulTag = SoulTagResponse.builder()
                .imageUrl(output.getSoulTagUrl())
                .bagName(attachedAccessory != null ? attachedAccessory.getName() : null)
                .auraCode(
                        output.getAuraCode() != null
                                ? List.of(output.getAuraCode().split(","))
                                : List.of()
                )
                .mood("STREET")
                .styling(attachedAccessory != null ? attachedAccessory.getName() : null)
                .forgedAt("MCM Cheongdam House")
                .date("2026-08-20")
                .build();

        return LandingResponse.builder()
                .videoStatus(output.getVideoStatus().name())
                .videoUrl(output.getVideoUrl())
                .thumbnailUrl(output.getThumbnailUrl())
                .soulTag(soulTag)
                .products(products)
                .build();
    }
}