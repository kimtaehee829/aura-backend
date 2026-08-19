package com.aura.aura.domain.output.enums;

public enum VideoStatus {

    PENDING,     // finalize 직후 (아직 업로드 안됨)
    UPLOADING,   // GCS 업로드 진행 중
    READY,       // 업로드 완료 (재생 가능)
    FAILED,      // 업로드 실패
    EXPIRED      // 48시간 지나서 자동 삭제됨
}