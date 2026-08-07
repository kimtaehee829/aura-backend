-- 가방 1종 업데이트 (bag_01.png / bag_01.glb)
UPDATE products 
SET name = 'Stark 사이드 스터드 비세토스 백팩',
    image_url = 'https://storage.googleapis.com/aura-assets-2026/products/bag_01.png',
    model_url = 'https://storage.googleapis.com/aura-assets-2026/models/bag_01.glb'
WHERE name = 'Monumental Visetos Tote';

-- 악세서리 1종 업데이트 (acc_01.png / acc_01.glb)
UPDATE products 
SET name = '비세토스 오리지널 키링',
    image_url = 'https://storage.googleapis.com/aura-assets-2026/products/acc_01.png',
    model_url = 'https://storage.googleapis.com/aura-assets-2026/models/acc_01.glb'
WHERE name = 'Aura Logo Stud Charm';

-- 악세서리 2종 업데이트 (acc_02.png / acc_02.glb)
UPDATE products 
SET name = 'MCM 비세토스 파크베어 참',
    image_url = 'https://storage.googleapis.com/aura-assets-2026/products/acc_02.png',
    model_url = 'https://storage.googleapis.com/aura-assets-2026/models/acc_02.glb'
WHERE name = 'Visetos Chain Strap';
