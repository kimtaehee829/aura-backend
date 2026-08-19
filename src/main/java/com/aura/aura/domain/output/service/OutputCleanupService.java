package com.aura.aura.domain.output.service;

import com.aura.aura.domain.output.entity.SessionOutput;
import com.aura.aura.domain.output.enums.VideoStatus;
import com.aura.aura.domain.output.repository.SessionOutputRepository;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutputCleanupService {

    private final SessionOutputRepository sessionOutputRepository;
    private final Storage storage;

    @Value("${gcp.bucket-name}")
    private String bucketName;

    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void cleanupExpiredOutputs() {
        log.info("Starting expired outputs cleanup process...");
        LocalDateTime now = LocalDateTime.now();

        List<SessionOutput> expiredOutputs = sessionOutputRepository.findByExpiresAtBeforeAndVideoStatusNot(now, VideoStatus.EXPIRED);

        int count = 0;
        for (SessionOutput output : expiredOutputs) {
            try {
                deleteFromGcs(output.getVideoUrl());
                deleteFromGcs(output.getThumbnailUrl());
                deleteFromGcs(output.getSoulTagUrl());
                deleteFromGcs(output.getQrImageUrl());

                output.markAsExpired();
                count++;
            } catch (Exception e) {
                log.error("Failed to cleanup output for session: {}", output.getSession().getPublicId(), e);
            }
        }

        log.info("Successfully cleaned up {} expired outputs.", count);
    }

    private void deleteFromGcs(String url) {
        if (url == null) return;
        
        String prefix = "https://storage.googleapis.com/" + bucketName + "/";
        if (url.startsWith(prefix)) {
            String objectPath = url.substring(prefix.length());
            try {
                boolean deleted = storage.delete(BlobId.of(bucketName, objectPath));
                if (!deleted) {
                    log.warn("Blob not found or couldn't be deleted: {}", objectPath);
                }
            } catch (Exception e) {
                log.error("Error deleting blob: {}", objectPath, e);
            }
        }
    }
}
