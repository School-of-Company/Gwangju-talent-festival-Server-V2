#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 2 ]]; then
  echo "사용법: bash scripts/generate-performer-verification.sh <이름> <만료일시: YYYY-MM-DD HH:MM:SS>" >&2
  exit 1
fi

participant_name=$1
expires_at=$2

if [[ -z $participant_name || $participant_name == *"'"* ]]; then
  echo "이름은 비어 있거나 작은따옴표를 포함할 수 없습니다." >&2
  exit 1
fi

if [[ ! $expires_at =~ ^[0-9]{4}-[0-9]{2}-[0-9]{2}[[:space:]][0-9]{2}:[0-9]{2}:[0-9]{2}$ ]]; then
  echo "만료일시는 YYYY-MM-DD HH:MM:SS 형식이어야 합니다." >&2
  exit 1
fi

verification_code=$(openssl rand -hex 16)
code_hash=$(printf '%s' "$verification_code" | openssl dgst -sha256 -r | awk '{print $1}')

printf '참가자 전달용 인증코드: %s\n' "$verification_code"
printf "INSERT INTO performer_verification (participant_name, code_hash, expires_at) VALUES ('%s', '%s', '%s');\n" \
  "$participant_name" "$code_hash" "$expires_at"
