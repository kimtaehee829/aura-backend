UPDATE aura_analysis SET energy_level = '100' WHERE energy_level = 'HIGH';
UPDATE aura_analysis SET energy_level = '0' WHERE energy_level = 'LOW';

ALTER TABLE aura_analysis MODIFY COLUMN energy_level INT COMMENT '0~100. 파티클 밀도 결정';
