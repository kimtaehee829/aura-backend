package com.aura.aura.domain.output.entity;

import com.aura.aura.domain.output.enums.VideoStatus;
import com.aura.aura.domain.session.entity.Session;
import com.aura.aura.global.exception.BusinessException;
import com.aura.aura.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "session_outputs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SessionOutput {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private Session session;

    @Enumerated(EnumType.STRING)
    @Column(name = "video_status", nullable = false, length = 20)
    private VideoStatus videoStatus;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "video_duration_ms")
    private Integer videoDurationMs;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "soul_tag_url", length = 500)
    private String soulTagUrl;

    @Column(name = "landing_url", length = 500)
    private String landingUrl;

    @Column(name = "qr_image_url", length = 500)
    private String qrImageUrl;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "aura_code", length = 100)
    private String auraCode;

    @Column(name = "accessory_id")
    private Long accessoryId;

    @Column(name = "object_path", length = 500)
    private String objectPath;

    // =========================
    // 상태 기반 로직 (핵심)
    // =========================

    public void startUploading(String objectPath) {
        if (this.videoStatus != VideoStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_STATUS);
        }

        this.videoStatus = VideoStatus.UPLOADING;
        this.objectPath = objectPath;
    }

    public void completeVideo(String videoUrl, Integer videoDurationMs) {
        if (this.videoStatus != VideoStatus.UPLOADING) {
            throw new BusinessException(ErrorCode.INVALID_STATUS);
        }

        this.videoStatus = VideoStatus.READY;
        this.videoUrl = videoUrl;
        this.videoDurationMs = videoDurationMs;

        // 👉 썸네일 더미
        this.thumbnailUrl = "https://dummy-thumbnail.com/default.png";
    }

    public void failVideo() {
        if (this.videoStatus == VideoStatus.FAILED) return;
        this.videoStatus = VideoStatus.FAILED;
    }

    public void validateReady() {
        if (this.videoStatus != VideoStatus.READY) {
            throw new BusinessException(ErrorCode.OUTPUT_NOT_READY);
        }
    }

    // =========================
    // 생성 & finalize
    // =========================

    public static SessionOutput create(Session session) {
        SessionOutput output = new SessionOutput();
        output.session = session;
        output.videoStatus = VideoStatus.PENDING;
        return output;
    }

    public void updateAfterFinalize(
            String soulTagUrl,
            String landingUrl,
            String qrUrl,
            LocalDateTime expiresAt
    ) {
        this.soulTagUrl = soulTagUrl;
        this.landingUrl = landingUrl;
        this.qrImageUrl = qrUrl;
        this.expiresAt = expiresAt;
    }

    public void applyFinalizeData(List<String> auraCode, Long accessoryId) {
        this.auraCode = String.join(",", auraCode);
        this.accessoryId = accessoryId;
    }

    // =========================
    // JPA lifecycle
    // =========================

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}