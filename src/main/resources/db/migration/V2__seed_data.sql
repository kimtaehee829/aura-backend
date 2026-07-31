-- ==========================================================
-- AURA Backend : 시연용 시드 데이터
-- 위치: src/main/resources/db/migration/V2__seed_data.sql
--
-- BE가 확보하는 3D 에셋은 가방 1종 + 악세서리 2종.
-- model_url / image_url / purchase_url 은 실제 값 확보 후 UPDATE 하거나
-- 이 파일을 수정하지 말고 V3 마이그레이션으로 갱신할 것.
-- (Flyway는 이미 적용된 파일이 변경되면 체크섬 오류로 실행을 거부함)
-- ==========================================================

-- 매장 : 시연 기준 1개
INSERT INTO stores (name, address) VALUES
    ('MCM Cheongdam House', '서울 강남구 청담동');

-- 가방 1종
INSERT INTO products (product_type, name, category, price, image_url, model_url, purchase_url) VALUES
    ('BAG', 'Monumental Visetos Tote', 'TOTE', 1290000,
     'https://storage.googleapis.com/aura-assets/products/bag_01.png',
     'https://storage.googleapis.com/aura-assets/models/bag_01.glb',
     'https://kr.mcmworldwide.com/');

-- 악세서리 2종
INSERT INTO products (product_type, name, category, price, image_url, model_url, purchase_url) VALUES
    ('ACCESSORY', 'Aura Logo Stud Charm', 'CHARM', 190000,
     'https://storage.googleapis.com/aura-assets/products/acc_stud.png',
     'https://storage.googleapis.com/aura-assets/models/acc_stud.glb',
     'https://kr.mcmworldwide.com/'),
    ('ACCESSORY', 'Visetos Chain Strap', 'STRAP', 320000,
     'https://storage.googleapis.com/aura-assets/products/acc_chain.png',
     'https://storage.googleapis.com/aura-assets/models/acc_chain.glb',
     'https://kr.mcmworldwide.com/');
