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

    /** 영상 포맷 (video/mp4) */
    @Column(name = "video_format", length = 20)
    private String videoFormat;

    /** 영상 길이(ms) */
    @Column(name = "video_duration_ms")
    private Integer videoDurationMs;

    /** 영상 썸네일 이미지 경로 */
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    /** Soul Tag 이미지 경로 */
    @Column(name = "soul_tag_url", length = 500)
    private String soulTagUrl;

    /** QR이 연결되는 랜딩 페이지 URL */
    @Column(name = "landing_url", length = 500)
    private String landingUrl;

    /** QR 이미지 경로 */
    @Column(name = "qr_image_url", length = 500)
    private String qrImageUrl;

    /** 랜딩 페이지 만료 시간 (생성 후 48시간) */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /** 생성 시각 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 마지막 수정 시각 (DB에서 자동 관리) */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * SessionOutput 생성
     * 최초 생성 시 영상 상태는 항상 PENDING
     */
    @Builder
    public SessionOutput(
            Session session,
            String landingUrl,
            String qrImageUrl,
            String soulTagUrl,
            LocalDateTime expiresAt
    ) {
        this.session = session;
        this.videoStatus = VideoStatus.PENDING;
        this.landingUrl = landingUrl;
        this.qrImageUrl = qrImageUrl;
        this.soulTagUrl = soulTagUrl;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Signed URL 발급 후 영상 업로드 시작
     */
    public void startUploading(String videoFormat) {
        this.videoStatus = VideoStatus.UPLOADING;
        this.videoFormat = videoFormat;
    }

    /**
     * 영상 업로드 완료
     */
    public void completeVideo(
            String videoUrl,
            Integer videoDurationMs,
            String thumbnailUrl
    ) {
        this.videoStatus = VideoStatus.READY;
        this.videoUrl = videoUrl;
        this.videoDurationMs = videoDurationMs;
        this.thumbnailUrl = thumbnailUrl;
    }

    /**
     * 영상 업로드 실패
     */
    public void failVideo() {
        this.videoStatus = VideoStatus.FAILED;
    }
}