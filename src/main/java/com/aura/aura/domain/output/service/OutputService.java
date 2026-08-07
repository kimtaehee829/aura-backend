package com.aura.aura.domain.output.service;

import com.aura.aura.domain.output.dto.*;
import com.aura.aura.domain.output.dto.FinalizeOutputResponse;
import com.aura.aura.domain.output.dto.request.FinalizeOutputRequest;
import com.aura.aura.domain.output.dto.request.VideoCompleteRequest;
import com.aura.aura.domain.output.dto.request.VideoUploadUrlRequest;
import com.aura.aura.domain.output.dto.response.OutputResponse;
import com.aura.aura.domain.output.dto.response.VideoCompleteResponse;
import com.aura.aura.domain.output.dto.response.VideoUploadUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OutputService {

    /**
     * Soul Tag, QR, Landing URL 생성
     */
    public FinalizeOutputResponse finalizeOutput(
            String publicId,
            FinalizeOutputRequest request
    ) {

        // TODO
        // 1. Session 조회
        // 2. AuraAnalysis 조회
        // 3. Accessory(Product) 조회
        // 4. Soul Tag 생성
        // 5. QR 생성
        // 6. GCS 업로드
        // 7. SessionOutput 저장

        return null;
    }

    /**
     * 영상 업로드용 Signed URL 발급
     */
    public VideoUploadUrlResponse generateVideoUploadUrl(
            String publicId,
            VideoUploadUrlRequest request
    ) {

        // TODO
        // 1. SessionOutput 조회
        // 2. Signed URL 생성
        // 3. videoStatus -> UPLOADING
        // 4. 저장

        return null;
    }

    /**
     * 영상 업로드 완료
     */
    public VideoCompleteResponse completeVideo(
            String publicId,
            VideoCompleteRequest request
    ) {

        // TODO
        // 1. SessionOutput 조회
        // 2. videoUrl 저장
        // 3. duration 저장
        // 4. thumbnail 저장
        // 5. status -> READY

        return null;
    }

    /**
     * 산출물 조회
     */
    public OutputResponse getOutput(String publicId) {

        // TODO
        // 1. SessionOutput 조회
        // 2. Response 생성

        return null;
    }

}