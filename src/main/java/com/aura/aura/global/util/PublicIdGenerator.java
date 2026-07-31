package com.aura.aura.global.util;

import java.security.SecureRandom;
import java.util.Base64;

public final class PublicIdGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    private static final int BYTE_LENGTH = 16;

    private PublicIdGenerator() {
        throw new AssertionError("유틸리티 클래스는 인스턴스화할 수 없습니다.");
    }

    public static String generate() {
        byte[] bytes = new byte[BYTE_LENGTH];
        RANDOM.nextBytes(bytes);
        return ENCODER.encodeToString(bytes);
    }
}
