variable "cloudflare_api_token" {
  description = "Cloudflare API 토큰 (계정 레벨 R2 관리 권한). Terraform 전용."
  type        = string
  sensitive   = true
}

variable "account_id" {
  description = "Cloudflare 계정 ID. R2 엔드포인트(https://<account_id>.r2.cloudflarestorage.com)에 사용."
  type        = string
}

variable "bucket_name" {
  description = "영상 저장용 R2 버킷명."
  type        = string
}

variable "location" {
  description = "R2 버킷 location 힌트. provider v5는 소문자만 허용."
  type        = string
  default     = "apac"

  validation {
    condition     = contains(["apac", "eeur", "enam", "weur", "wnam", "oc"], var.location)
    error_message = "location은 apac, eeur, enam, weur, wnam, oc 중 하나여야 합니다."
  }
}

variable "allowed_origins" {
  description = "브라우저 직접 업로드(presigned PUT)를 허용할 출처 목록. application.yaml의 cors.allowed-origins와 동기화."
  type        = list(string)
}
