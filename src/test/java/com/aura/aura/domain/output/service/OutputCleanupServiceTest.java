package com.aura.aura.domain.output.service;

import com.aura.aura.domain.output.entity.SessionOutput;
import com.aura.aura.domain.output.enums.VideoStatus;
import com.aura.aura.domain.output.repository.SessionOutputRepository;
import com.aura.aura.domain.session.entity.Session;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutputCleanupServiceTest {

    @Mock
    private SessionOutputRepository sessionOutputRepository;

    @Mock
    private Storage storage;

    @InjectMocks
    private OutputCleanupService outputCleanupService;

    private final String bucketName = "test-bucket";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(outputCleanupService, "bucketName", bucketName);
    }

    @Test
    void cleanupExpiredOutputs_ShouldDeleteFilesAndMarkAsExpired() {
        // given
        Session session = mock(Session.class);

        SessionOutput output = SessionOutput.create(session);
        output.startUploading("videos/test.mp4");
        output.completeVideo(
                "https://storage.googleapis.com/test-bucket/videos/test.mp4",
                "https://storage.googleapis.com/test-bucket/thumbnails/test.png",
                15000
        );
        output.updateAfterFinalize(
                "https://storage.googleapis.com/test-bucket/soul-tags/test.png",
                "https://test.com/landing/123",
                "https://storage.googleapis.com/test-bucket/qr/test.png",
                LocalDateTime.now().minusDays(1)
        );

        when(sessionOutputRepository.findByExpiresAtBeforeAndVideoStatusNot(any(LocalDateTime.class), eq(VideoStatus.EXPIRED)))
                .thenReturn(List.of(output));

        when(storage.delete(any(BlobId.class))).thenReturn(true);

        // when
        outputCleanupService.cleanupExpiredOutputs();

        // then
        // Verify storage deletion calls for all 4 media files
        verify(storage, times(1)).delete(BlobId.of(bucketName, "videos/test.mp4"));
        verify(storage, times(1)).delete(BlobId.of(bucketName, "thumbnails/test.png"));
        verify(storage, times(1)).delete(BlobId.of(bucketName, "soul-tags/test.png"));
        verify(storage, times(1)).delete(BlobId.of(bucketName, "qr/test.png"));

        // Verify status is changed to EXPIRED and URLs are nullified
        assertThat(output.getVideoStatus()).isEqualTo(VideoStatus.EXPIRED);
        assertThat(output.getVideoUrl()).isNull();
        assertThat(output.getThumbnailUrl()).isNull();
        assertThat(output.getSoulTagUrl()).isNull();
        assertThat(output.getQrImageUrl()).isNull();
        assertThat(output.getObjectPath()).isNull();
    }
}
