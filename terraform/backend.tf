# R2(S3 호환)를 Terraform 원격 state backend로 사용.
#
# 사전 준비(부트스트랩, chicken-and-egg):
#   - state 전용 R2 버킷을 Cloudflare 대시보드에서 먼저 생성해야 한다.
#   - backend 자격증명은 환경변수로 주입한다:
#       export AWS_ACCESS_KEY_ID=<R2 Access Key>
#       export AWS_SECRET_ACCESS_KEY=<R2 Secret>
#   - 아래 <...> 값을 실제 값으로 치환한 뒤 `terraform init` 실행.
#
# R2 호환을 위해 skip_* 옵션과 skip_s3_checksum(terraform 1.6.3+)이 필수.
terraform {
  backend "s3" {
    bucket = "<STATE_BUCKET>"
    key    = "r2-migration/terraform.tfstate"
    region = "auto"

    endpoints = {
      s3 = "https://<ACCOUNT_ID>.r2.cloudflarestorage.com"
    }

    skip_credentials_validation = true
    skip_region_validation      = true
    skip_metadata_api_check     = true
    skip_requesting_account_id  = true
    skip_s3_checksum            = true
    use_path_style              = true
  }
}
