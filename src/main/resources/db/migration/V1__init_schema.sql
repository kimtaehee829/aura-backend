SET NAMES utf8mb4;


CREATE TABLE stores (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    address     VARCHAR(255) NULL,
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '매장. Soul Tag의 Forged At 표기 출처';


CREATE TABLE products (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    product_type  VARCHAR(20)  NOT NULL COMMENT 'BAG | ACCESSORY',
    name          VARCHAR(100) NOT NULL,
    category      VARCHAR(50)  NULL,
    price         INT          NOT NULL,
    image_url     VARCHAR(500) NULL,
    model_url     VARCHAR(500) NULL COMMENT 'glTF/glb 경로 (로우폴리)',
    purchase_url  VARCHAR(500) NULL COMMENT 'MCM 공식몰 구매 링크',
    created_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_products_type (product_type)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '상품 마스터';


CREATE TABLE sessions (
    id                   BIGINT      NOT NULL AUTO_INCREMENT COMMENT '내부 전용. 외부 노출 금지',
    public_id            VARCHAR(24) NOT NULL COMMENT 'SecureRandom 128bit base64url 22자. QR/랜딩 URL용',

    store_id             BIGINT      NOT NULL,
    bag_product_id       BIGINT      NULL COMMENT '소환된 가방',

    status               VARCHAR(20) NOT NULL DEFAULT 'CONSENT'
                         COMMENT 'CONSENT|ANALYZING|SUMMONING|STYLING|FORGING|COMPLETE|ABANDONED',
    abandoned_at_status  VARCHAR(20) NULL COMMENT '이탈 시점의 단계. 퍼널 분석용',

    consent_agreed_at    DATETIME(6) NULL COMMENT '웹캠 분석 동의 시각',
    started_at           DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ended_at             DATETIME(6) NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uq_sessions_public_id (public_id),
    KEY idx_sessions_store_time (store_id, started_at),
    KEY idx_sessions_status (status),

    CONSTRAINT fk_sessions_store
        FOREIGN KEY (store_id) REFERENCES stores (id),
    CONSTRAINT fk_sessions_bag_product
        FOREIGN KEY (bag_product_id) REFERENCES products (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '체험 세션';


CREATE TABLE aura_analysis (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    session_id    BIGINT      NOT NULL,

    style         VARCHAR(30) NULL COMMENT 'STREET | ROCK_CHIC | CLASSIC | MINIMAL',
    mood          VARCHAR(30) NOT NULL COMMENT '비세토스 패턴 변형 분기 키',
    energy_level  VARCHAR(10) NULL COMMENT 'HIGH | LOW. 파티클 밀도 결정',

    palette_1     CHAR(7)     NOT NULL COMMENT '#RRGGBB 메인',
    palette_2     CHAR(7)     NULL COMMENT '#RRGGBB 패턴',
    palette_3     CHAR(7)     NULL COMMENT '#RRGGBB 그림자',

    latency_ms    INT         NULL COMMENT 'Vision API 응답 지연',
    is_fallback   BOOLEAN     NOT NULL DEFAULT FALSE COMMENT '타임아웃으로 기본 무드 적용 여부',

    raw_json      JSON        NULL COMMENT 'Vision 응답 원문. 보관 기간 제한 권장',
    created_at    DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    UNIQUE KEY uq_aura_analysis_session (session_id),

    CONSTRAINT fk_aura_analysis_session
        FOREIGN KEY (session_id) REFERENCES sessions (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'AI 아우라 분석 결과';


CREATE TABLE session_accessories (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    session_id   BIGINT      NOT NULL,
    product_id   BIGINT      NOT NULL,

    slot_order   INT         NOT NULL COMMENT '부유 등장 순서 (1~2)',
    is_attached  BOOLEAN     NOT NULL DEFAULT FALSE COMMENT '최대 1개 부착',
    attached_at  DATETIME(6) NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uq_session_accessory (session_id, product_id),

    CONSTRAINT fk_session_accessories_session
        FOREIGN KEY (session_id) REFERENCES sessions (id) ON DELETE CASCADE,
    CONSTRAINT fk_session_accessories_product
        FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '세션별 악세서리 추천/부착';


CREATE TABLE interaction_events (
    id                 BIGINT      NOT NULL AUTO_INCREMENT,
    session_id         BIGINT      NOT NULL,

    seq                INT         NOT NULL COMMENT '세션 내 발생 순서',
    phase              VARCHAR(20) NOT NULL COMMENT 'PHASE2_HAPTIC | PHASE2_FORGE | PHASE3_STYLING',

    target_type        VARCHAR(20) NOT NULL COMMENT 'BAG_PART | ACCESSORY | AURA_ORB',
    target_part        VARCHAR(30) NULL
                       COMMENT 'BISETOS_LEATHER|ZIPPER_LINE|LOGO_STUD|STRAP_BUCKLE',
    target_product_id  BIGINT      NULL COMMENT 'target_type=ACCESSORY 일 때',

    gesture            VARCHAR(10) NOT NULL COMMENT 'HOVER | PRESS | GRAB | GATHER',
    dwell_ms           INT         NOT NULL COMMENT '부위 체류 시간. 관심도 핵심 지표',
    is_completed       BOOLEAN     NOT NULL DEFAULT FALSE
                       COMMENT 'PRESS 2초 충족/GRAB 완료. false = 관심 있었으나 이탈',

    elapsed_ms         INT         NOT NULL COMMENT 'Phase 1 시작 기준 경과',
    occurred_at        DATETIME(6) NOT NULL,

    PRIMARY KEY (id),
    KEY idx_events_session_seq (session_id, seq),
    KEY idx_events_part_gesture (target_part, gesture),

    CONSTRAINT fk_interaction_events_session
        FOREIGN KEY (session_id) REFERENCES sessions (id) ON DELETE CASCADE,
    CONSTRAINT fk_interaction_events_product
        FOREIGN KEY (target_product_id) REFERENCES products (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '부위별 인터랙션 로그';


CREATE TABLE session_outputs (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    session_id         BIGINT       NOT NULL,

    video_status       VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                       COMMENT 'PENDING | UPLOADING | READY | FAILED',
    video_url          VARCHAR(500) NULL COMMENT 'GCS 오브젝트 경로. 응답 시 Signed URL 변환',
    video_format       VARCHAR(20)  NULL,
    video_duration_ms  INT          NULL,
    thumbnail_url      VARCHAR(500) NULL,

    soul_tag_url       VARCHAR(500) NULL,

    landing_url        VARCHAR(500) NULL COMMENT 'public_id 포함 대상 URL',
    qr_image_url       VARCHAR(500) NULL,
    expires_at         DATETIME(6)  NULL COMMENT '만료 후 410 Gone',

    created_at         DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at         DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                       ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    UNIQUE KEY uq_session_outputs_session (session_id),
    KEY idx_outputs_video_status (video_status),

    CONSTRAINT fk_session_outputs_session
        FOREIGN KEY (session_id) REFERENCES sessions (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '세션 산출물 (영상/Soul Tag/QR)';


CREATE TABLE landing_events (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    session_id   BIGINT      NOT NULL,

    event_type   VARCHAR(30) NOT NULL
                 COMMENT 'PAGE_VIEW|VIDEO_PLAY|VIDEO_DOWNLOAD|SOUL_TAG_DOWNLOAD|PURCHASE_CLICK',
    product_id   BIGINT      NULL COMMENT 'PURCHASE_CLICK 시 대상 상품',
    occurred_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    KEY idx_landing_session_time (session_id, occurred_at),
    KEY idx_landing_type (event_type),

    CONSTRAINT fk_landing_events_session
        FOREIGN KEY (session_id) REFERENCES sessions (id) ON DELETE CASCADE,
    CONSTRAINT fk_landing_events_product
        FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '모바일 랜딩 행동 로그';
