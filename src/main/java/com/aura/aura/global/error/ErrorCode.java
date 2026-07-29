package com.aura.aura.global.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    INVALID_REQUEST_PARAMETER(400, "잘못된 요청 파라미터입니다."),
    MISSING_IMAGE_FILE(400, "분석할 이미지 파일이 누락되었습니다."),

    SOULTAG_NOT_FOUND(404, "요청하신 Soul Tag를 찾을 수 없습니다."),

    INTERNAL_SERVER_ERROR(500, "서버 내부 오류가 발생했습니다."),
    GCS_UPLOAD_FAILED(500, "GCS 영상 업로드에 실패했습니다."),
    AI_ANALYSIS_FAILED(500, "AI 무드 분석 중 오류가 발생했습니다.");

    private final int status;
    private final String message;
}