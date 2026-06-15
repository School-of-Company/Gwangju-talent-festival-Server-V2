output "bucket_name" {
  description = "생성된 R2 버킷명. 앱의 AWS_S3_BUCKET에 사용."
  value       = cloudflare_r2_bucket.video.name
}

output "endpoint" {
  description = "R2 S3 호환 엔드포인트. 앱의 AWS_S3_ENDPOINT에 사용."
  value       = "https://${var.account_id}.r2.cloudflarestorage.com"
}
