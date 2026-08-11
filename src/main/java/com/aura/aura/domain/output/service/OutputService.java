package com.aura.aura.domain.output.service;

import com.aura.aura.domain.analysis.entity.AuraAnalysis;
import com.aura.aura.domain.analysis.repository.AuraAnalysisRepository;
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

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
@Transactional
public class OutputService {

    private final ProductService productService;
    private final AccessoryService accessoryService;
    private final AuraAnalysisRepository auraAnalysisRepository;

    //더미
    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${gcp.bucket-name}")
    private String bucketName;

    private final SessionRepository sessionRepository;
    private final SessionOutputRepository sessionOutputRepository;
    private final SoulTagImageGenerator soulTagImageGenerator;

    public FinalizeOutputResponse finalizeOutput(String publicId) {

        Session session = getSession(publicId);
        SessionOutput output = getOrCreateOutput(session);

        output.validateReady();

        String landingUrl = buildLandingUrl(publicId);
        String qrUrl = generateQrImage(publicId, landingUrl);
        
        String soulTagUrl;
        try {
            AuraAnalysis aura = auraAnalysisRepository.findBySessionId(session.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ANALYSIS_NOT_FOUND));

            AccessoryResponse attachedAccessory = accessoryService.getSessionAccessories(publicId)
                    .stream()
                    .filter(AccessoryResponse::getIsAttached)
                    .findFirst()
                    .orElse(null);

            String bagName = session.getBagProduct() != null ? session.getBagProduct().getName() : null;
            List<String> auraCodes = List.of(aura.getPalette1(), aura.getPalette2(), aura.getPalette3());
            String styling = attachedAccessory != null ? attachedAccessory.getName() : null;
            String storeName = session.getStore() != null ? session.getStore().getName() : null;
            String date = session.getStartedAt() != null ? session.getStartedAt().toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy. MM. dd")) : null;

            byte[] soulTagBytes = soulTagImageGenerator.generate(bagName, auraCodes, aura.getMood(), styling, storeName, date);
            
            Storage storage = StorageOptions.getDefaultInstance().getService();
            String objectPath = "soul-tags/" + publicId + "_" + System.currentTimeMillis() + ".png";
            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectPath)
                    .setContentType("image/png")
                    .build();
            storage.create(blobInfo, soulTagBytes);
            
            soulTagUrl = "https://storage.googleapis.com/" + bucketName + "/" + objectPath;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SOUL_TAG_FAILED);
        }

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

    public VideoUploadUrlResponse generateVideoUploadUrl(String publicId) {

        Session session = getSession(publicId);
        SessionOutput output = getOrCreateOutput(session);

        if (output.getVideoStatus() != VideoStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_STATUS);
        }

        String videoObjectPath = buildObjectPath(publicId);

        Storage storage = StorageOptions.getDefaultInstance().getService();

        URL videoUrl = storage.signUrl(
                BlobInfo.newBuilder(bucketName, videoObjectPath)
                        .setContentType("video/mp4")
                        .build(),
                10, TimeUnit.MINUTES,
                Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
                Storage.SignUrlOption.withContentType()
        );

        output.startUploading(videoObjectPath);

        return VideoUploadUrlResponse.builder()
                .uploadUrl(videoUrl.toString())
                .objectPath(videoObjectPath)
                .expiresInSeconds(600)
                .build();
    }

    private String buildGcsUrl(String objectPath) {
        return "https://storage.googleapis.com/" + bucketName + "/" + objectPath;
    }

    public VideoCompleteResponse completeVideo(String publicId, VideoCompleteRequest request) {

        Session session = getSession(publicId);
        SessionOutput output = getOutput(session);

        if (output.getVideoStatus() != VideoStatus.UPLOADING) {
            throw new BusinessException(ErrorCode.INVALID_STATUS);
        }

        if (!Objects.equals(request.getObjectPath(), output.getObjectPath())) {
            throw new BusinessException(ErrorCode.INVALID_OBJECT_PATH);
        }

        String videoUrl = buildVideoUrl(request.getObjectPath());

        String thumbnailUrl = null;

        if (request.getThumbnailObjectPath() != null) {
            thumbnailUrl = buildVideoUrl(request.getThumbnailObjectPath());
        }

        output.completeVideo(
                videoUrl,
                thumbnailUrl,
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

            // 1. byte[] 생성
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream);
            byte[] qrBytes = outputStream.toByteArray();

            // 2. GCS 업로드
            Storage storage = StorageOptions.getDefaultInstance().getService();

            String objectPath = "qr/" + publicId + "_" + System.currentTimeMillis() + ".png";

            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectPath)
                    .setContentType("image/png")
                    .build();

            storage.create(blobInfo, qrBytes);

            // 3. URL 반환
            return "https://storage.googleapis.com/" + bucketName + "/" + objectPath;

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
        return "https://storage.googleapis.com/" + bucketName + "/" + objectPath;
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

        if (output.getVideoStatus() != VideoStatus.READY) {
            return LandingResponse.builder()
                    .videoStatus(output.getVideoStatus().name())
                    .products(products)
                    .build();
        }

        SoulTagResponse soulTag = getSoulTag(publicId);

        return LandingResponse.builder()
                .videoStatus(output.getVideoStatus().name())
                .videoUrl(output.getVideoUrl())
                .thumbnailUrl(output.getThumbnailUrl())
                .soulTag(soulTag)
                .products(products)
                .build();
    }

    @Transactional(readOnly = true)
    public SoulTagResponse getSoulTag(String publicId) {

        Session session = getSession(publicId);
        SessionOutput output = getOutput(session);

        if (output.getVideoStatus() != VideoStatus.READY) {
            throw new BusinessException(ErrorCode.INVALID_STATUS);
        }

        // ✅ aura_analysis 조회
        AuraAnalysis aura = auraAnalysisRepository.findBySessionId(session.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ANALYSIS_NOT_FOUND));

        // ✅ accessory
        AccessoryResponse attachedAccessory = accessoryService.getSessionAccessories(publicId)
                .stream()
                .filter(AccessoryResponse::getIsAttached)
                .findFirst()
                .orElse(null);

        return SoulTagResponse.builder()
                .imageUrl(output.getSoulTagUrl())

                .bagName(
                        session.getBagProduct() != null
                                ? session.getBagProduct().getName()
                                : null
                )

                .auraCode(List.of(
                        aura.getPalette1(),
                        aura.getPalette2(),
                        aura.getPalette3()
                ))

                .mood(aura.getMood())

                .forgedAt(session.getStore().getName())

                .date(session.getStartedAt().toLocalDate().toString())

                .styling(attachedAccessory != null ? attachedAccessory.getName() : null)

                .build();
    }

    public ThumbnailUploadUrlResponse getThumbnailUploadUrl(String publicId) {

        Session session = getSession(publicId);

        SessionOutput output = getOutput(session);

        if (output.getVideoStatus() != VideoStatus.UPLOADING) {
            throw new BusinessException(ErrorCode.INVALID_STATUS);
        }

        // 파일 경로 생성 (중요)
        String objectPath = "thumbnails/" + publicId + "_" + System.currentTimeMillis() + ".png";

        // signed url 생성
        String uploadUrl = generateUploadUrl(objectPath, "image/png");

        return ThumbnailUploadUrlResponse.builder()
                .uploadUrl(uploadUrl)
                .objectPath(objectPath)
                .expiresInSeconds(600)
                .build();
    }

    public String generateUploadUrl(String objectPath, String contentType) {

        Storage storage = StorageOptions.getDefaultInstance().getService();

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectPath)
                .setContentType(contentType)
                .build();

        URL url = storage.signUrl(
                blobInfo,
                10, TimeUnit.MINUTES,
                Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
                Storage.SignUrlOption.withContentType()
        );

        return url.toString();
    }

}