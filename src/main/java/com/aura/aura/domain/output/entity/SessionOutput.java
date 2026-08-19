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
    @Column(name = "video_status", nullable = false)
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

    @Column(name = "object_path", length = 500)
    private String objectPath;

    public void startUploading(String objectPath) {
        if (this.videoStatus != VideoStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_STATUS);
        }

        this.videoStatus = VideoStatus.UPLOADING;
        this.objectPath = objectPath;
    }

    public void completeVideo(String videoUrl, String thumbnailUrl, Integer durationMs) {
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.videoDurationMs = durationMs;
        this.videoStatus = VideoStatus.READY;
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

    public void markAsExpired() {
        this.videoStatus = VideoStatus.EXPIRED;
        this.videoUrl = null;
        this.thumbnailUrl = null;
        this.soulTagUrl = null;
        this.qrImageUrl = null;
        this.objectPath = null;
    }

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