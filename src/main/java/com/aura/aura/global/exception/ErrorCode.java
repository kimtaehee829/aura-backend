package com.aura.aura.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ---------- 공통 ----------
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    // ---------- 매장 / 상품 ----------
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "매장을 찾을 수 없습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),

    // ---------- 세션 ----------
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "세션을 찾을 수 없습니다."),
    SESSION_EXPIRED(HttpStatus.GONE, "만료된 세션입니다."),
    SESSION_ALREADY_COMPLETED(HttpStatus.CONFLICT, "이미 종료된 세션입니다."),
    CONSENT_REQUIRED(HttpStatus.FORBIDDEN, "웹캠 분석 동의가 필요합니다."),
    ACCESSORY_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "악세서리는 최대 1개까지 부착할 수 있습니다."),

    // ---------- AI 분석 ----------
    ANALYSIS_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 아우라 분석이 완료된 세션입니다."),
    ANALYSIS_FAILED(HttpStatus.BAD_GATEWAY, "아우라 분석에 실패했습니다."),
    ANALYSIS_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "아우라 분석이 지연되고 있습니다."),

    // ---------- 산출물 / 미디어 ----------
    OUTPUT_NOT_FOUND(HttpStatus.NOT_FOUND, "결과물을 찾을 수 없습니다."),
    OUTPUT_NOT_READY(HttpStatus.ACCEPTED, "결과물을 준비하고 있습니다."),
    VIDEO_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "영상 업로드에 실패했습니다."),
    SIGNED_URL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "업로드 URL 발급에 실패했습니다."),
    SOUL_TAG_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Soul Tag 생성에 실패했습니다."),
    QR_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "QR 코드 생성에 실패했습니다.");

    private final HttpStatus status;
    private final String message;
}
