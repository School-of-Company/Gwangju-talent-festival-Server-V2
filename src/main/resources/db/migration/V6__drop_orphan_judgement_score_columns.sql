-- 2026-07-14(966811c) 심사 항목 5개 -> 3개 재구성 시 엔티티 필드는 리네임됐지만,
-- 당시 ddl-auto=update 로 운영되던 DB는 컬럼 리네임/삭제를 반영하지 못해
-- 아래 옛 NOT NULL 컬럼이 orphan 으로 남아 INSERT 시 값 누락으로 저장이 실패하는 문제를 해소한다.
ALTER TABLE judgement
    DROP COLUMN IF EXISTS expression_communication_score,
    DROP COLUMN IF EXISTS technical_completeness_score,
    DROP COLUMN IF EXISTS stage_presence_performance_score,
    DROP COLUMN IF EXISTS teamwork_stage_harmony_score;
