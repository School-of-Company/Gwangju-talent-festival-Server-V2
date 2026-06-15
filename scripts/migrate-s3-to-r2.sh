#!/usr/bin/env bash
#
# AWS S3 -> Cloudflare R2 영상 데이터 전수 마이그레이션 (rclone)
#
# 사전 준비:
#   1) rclone 설치 (https://rclone.org/install/)
#   2) rclone 리모트 2개 구성 (~/.config/rclone/rclone.conf):
#
#      [s3]
#      type = s3
#      provider = AWS
#      access_key_id = <AWS_ACCESS_KEY>
#      secret_access_key = <AWS_SECRET_KEY>
#      region = ap-northeast-2
#
#      [r2]
#      type = s3
#      provider = Cloudflare
#      access_key_id = <R2_ACCESS_KEY>
#      secret_access_key = <R2_SECRET_KEY>
#      endpoint = https://<ACCOUNT_ID>.r2.cloudflarestorage.com
#      region = auto
#
# 사용:
#   ./scripts/migrate-s3-to-r2.sh <SOURCE_S3_BUCKET> <DEST_R2_BUCKET>
#
# 멱등(idempotent): rclone copy 는 동일 객체를 건너뛴다.
# 운영 전환 직전 동일 명령을 한 번 더 실행해 증분(delta) 재동기화를 권장한다.

set -euo pipefail

SRC_BUCKET="${1:?사용법: $0 <SOURCE_S3_BUCKET> <DEST_R2_BUCKET>}"
DST_BUCKET="${2:?사용법: $0 <SOURCE_S3_BUCKET> <DEST_R2_BUCKET>}"

echo ">> S3(${SRC_BUCKET}) -> R2(${DST_BUCKET}) 복사 시작"
rclone copy "s3:${SRC_BUCKET}" "r2:${DST_BUCKET}" \
  --transfers 16 --checkers 16 --progress

echo ">> 검증: 개수/크기 대조 (one-way)"
rclone check "s3:${SRC_BUCKET}" "r2:${DST_BUCKET}" --one-way

echo ">> 완료. 운영 전환 직전 본 스크립트를 한 번 더 실행해 증분 재동기화하세요."
