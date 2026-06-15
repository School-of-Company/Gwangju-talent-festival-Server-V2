terraform {
  required_version = ">= 1.6.3"

  required_providers {
    cloudflare = {
      source  = "cloudflare/cloudflare"
      version = "~> 4"
    }
  }
}

provider "cloudflare" {
  api_token = var.cloudflare_api_token
}

# 영상 저장용 R2 버킷
resource "cloudflare_r2_bucket" "video" {
  account_id = var.account_id
  name       = var.bucket_name
  location   = var.location
}

# 브라우저에서 presigned URL로 직접 파트를 PUT 업로드하기 위한 CORS 설정.
# completeMultipartUpload가 파트 ETag를 필요로 하므로 ETag expose가 필수.
resource "cloudflare_r2_bucket_cors_configuration" "video" {
  account_id  = var.account_id
  bucket_name = cloudflare_r2_bucket.video.name

  rules {
    allowed {
      methods = ["GET", "PUT"]
      origins = var.allowed_origins
      headers = ["*"]
    }
    expose_headers  = ["ETag"]
    max_age_seconds = 3600
  }
}
