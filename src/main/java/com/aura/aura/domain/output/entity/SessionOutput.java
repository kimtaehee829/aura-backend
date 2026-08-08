package com.aura.aura.domain.output.entity;

import com.aura.aura.domain.output.enums.VideoStatus;
import com.aura.aura.domain.session.entity.Session;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "session_outputs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SessionOutput {

    /** 내부 PK */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 세션당 결과물은 하나만 생성 */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private Session session;

    /** 영상 생성 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "video_status", nullable = false, length = 20)
    private VideoStatus videoStatus;

    /** GCS에 저장된 영상 경로 (응답 시 Signed URL로 변환) */
    @Column(name = "video_url", length = 500)
    private String videoUrl;

    /** 영상 길이(ms) */
    @Column(name = "video_duration_ms")
    private Integer videoDurationMs;

    /** 영상 썸네일 이미지 경로 */
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    /** Soul Tag 이미지 경로 */
    @Column(name = "soul_tag_url", length = 500)
    private String soulTagUrl;

    //QR이 연결되는 랜딩 페이지 URL
    @Column(name = "landing_url", length = 500)
    private String landingUrl;

    //QR 이미지 경로
    @Column(name = "qr_image_url", length = 500)
    private String qrImageUrl;

    //랜딩 페이지 만료 시간 (생성 후 48시간)
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    //생성 시각
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    //마지막 수정 시각 (DB에서 자동 관리)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    //경로 일관성
    @Column(name = "object_path", length = 500)
    private String objectPath;

    //Signed URL 발급 후 영상 업로드 시작
    public void startUploading(String objectPath) {

        //null
        if (objectPath == null || objectPath.isBlank()) {
            throw new IllegalArgumentException("objectPath는 필수입니다.");
        }

        if (this.videoStatus != VideoStatus.PENDING) {
            throw new IllegalStateException("영상 업로드는 PENDING 상태에서만 가능합니다.");
        }

        this.videoStatus = VideoStatus.UPLOADING;
        this.objectPath = objectPath;
    }

    //영상 업로드 완료
    public void completeVideo(
            String videoUrl,
            Integer videoDurationMs,
            String thumbnailUrl
    ) {
        if (this.videoStatus != VideoStatus.UPLOADING) {
            throw new IllegalStateException("업로드 중 상태에서만 완료 가능");
        }

        //null
        if (videoUrl == null || videoUrl.isBlank()) {
            throw new IllegalArgumentException("videoUrl은 필수입니다.");
        }
        if (videoDurationMs == null || videoDurationMs <= 0) {
            throw new IllegalArgumentException("videoDurationMs는 0보다 커야 합니다.");
        }

        this.videoStatus = VideoStatus.READY;
        this.videoUrl = videoUrl;
        this.videoDurationMs = videoDurationMs;
        this.thumbnailUrl = thumbnailUrl;
    }

    //영상 업로드 실패
    public void failVideo() {
        if (this.videoStatus == VideoStatus.FAILED) {
            return; // idempotent
        }
        this.videoStatus = VideoStatus.FAILED;
    }

    // 생성 메서드
    public static SessionOutput create(Session session) {

        //null
        if (session == null) {
            throw new IllegalArgumentException("session은 필수입니다.");
        }

        SessionOutput output = new SessionOutput();
        output.session = session;
        output.videoStatus = VideoStatus.PENDING;
        return output;
    }

    // finalize 후 값 세팅
    public void updateAfterFinalize(
            String soulTagUrl,
            String landingUrl,
            String qrUrl,
            LocalDateTime expiresAt
    ) {
        //null
        if (soulTagUrl == null || landingUrl == null || qrUrl == null) {
            throw new IllegalArgumentException("URL 값은 필수입니다.");
        }

        this.soulTagUrl = soulTagUrl;
        this.landingUrl = landingUrl;
        this.qrImageUrl = qrUrl;
        this.expiresAt = expiresAt;
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

    public void validateReady() {
        if (this.videoStatus != VideoStatus.READY) {
            throw new IllegalStateException("영상 준비 완료 상태가 아님");
        }
    }
}