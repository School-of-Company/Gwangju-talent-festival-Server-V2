# S3 → Cloudflare R2 마이그레이션 구현 명세 (Spec)

> 작성: plan-deep-dive 인터뷰 기반 / 대상 브랜치: develop

## 1. Context (배경 · 목적)

영상 업로드는 현재 AWS S3(`ap-northeast-2`)를 사용하며, `AwsS3Adapter`에 멀티파트 업로드 + presigned URL(PUT 파트 업로드 / GET 다운로드)이 구현되어 있다. **S3 egress(전송 OUT) 요금 절감**을 위해 egress 무료인 **Cloudflare R2**로 전환한다. R2는 S3 호환 API를 제공하므로 **`AwsS3Adapter`의 비즈니스 로직은 변경하지 않고**, 엔드포인트·자격증명·리전 설정만 R2용으로 전환한다.

### 결정 사항 요약 (인터뷰 결과)
| 항목 | 결정 |
|------|------|
| 설정 방식 | 기존 `aws.s3` prefix에 `endpoint` 필드 추가 (조건부 override, 하위호환) |
| 기존 데이터 | **전수 마이그레이션** (S3→R2 전체 복사), 재현 가능한 스크립트로 자동화 |
| 다운로드 URL | presigned GET 유지 (코드 변경 최소) |
| CORS | Terraform으로 R2 버킷 CORS 선언적 관리 |
| 전환 절차 | 검증 환경에서 E2E 확인 후 운영 전환 |
| Terraform state | R2 자체를 원격 backend(S3 호환)로 |
| 버킷 location | APAC |
| 미완료 멀티파트 정리(lifecycle) | **이번 범위 제외** (추후) |
| 기존 S3 버킷 정리 | **이번 범위 제외** (별도 작업) |
| Cloudflare 자격증명 | **분리**: TF용 계정 토큰 / 앱용 버킷 범위 R2 Access Key·Secret |
| CI Secrets 갱신 | 체크리스트로 명시 (수동 적용) |

### 사전 검증된 중요 사실
- **AWS SDK 버전 `2.28.0`** (build.gradle:95). R2 presigned 업로드를 깨뜨리는 기본 체크섬(CRC32) 강제는 SDK **2.30.0부터** 발생하므로 현재 버전은 **안전**. ⚠️ SDK를 2.30+로 올릴 경우 `requestChecksumCalculation(WHEN_REQUIRED)` 미설정 시 파트 업로드가 깨짐 → **이번 작업에서 SDK 버전 고정 유지**.
- 설정 prefix는 `aws.s3` (코드/yaml 일치), 단일 `application.yaml` + 환경변수 주입 구조.
- 기존 Terraform 코드 **없음** → 신규 작성.
- 배포는 `cd.yml`에서 `secrets.APPLICATION_YML` → `application.yml`, `secrets.ENV_FILE` → `.env`로 주입. **레포 파일 변경만으로는 운영 미반영** → Secret 갱신 필수.

---

## 2. 코드 변경

### 2.1 `AwsS3Properties` — `endpoint` 필드 추가
`src/main/java/team/startup/gwangjutalentfestival/global/s3/properties/AwsS3Properties.java`

`@AllArgsConstructor` 생성자 바인딩이므로 필드 추가만으로 충분.

```java
@Getter
@AllArgsConstructor
@ConfigurationProperties(prefix = "aws.s3")
public class AwsS3Properties {
    private String accessKey;
    private String secretKey;
    private String region;
    private String bucket;
    private String endpoint;   // R2 엔드포인트. 비어있으면 실제 AWS S3 사용
}
```

### 2.2 `AwsS3Config` — endpoint 조건부 적용
`src/main/java/team/startup/gwangjutalentfestival/global/s3/config/AwsS3Config.java`

`endpoint`가 설정된 경우에만 `S3Client`/`S3Presigner` 양쪽에 `endpointOverride()` 적용. 빈 값이면 기존 AWS S3 동작 유지(하위호환 + env 롤백 가능).

```java
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import org.springframework.util.StringUtils;
import java.net.URI;

@Bean
public S3Client s3Client(AwsCredentialsProvider provider) {
    S3ClientBuilder builder = S3Client.builder()
            .region(Region.of(awsS3Properties.getRegion()))
            .credentialsProvider(provider);
    if (StringUtils.hasText(awsS3Properties.getEndpoint())) {
        builder.endpointOverride(URI.create(awsS3Properties.getEndpoint()))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build());
    }
    return builder.build();
}

@Bean
public S3Presigner s3Presigner(AwsCredentialsProvider provider) {
    S3Presigner.Builder builder = S3Presigner.builder()
            .region(Region.of(awsS3Properties.getRegion()))
            .credentialsProvider(provider);
    if (StringUtils.hasText(awsS3Properties.getEndpoint())) {
        builder.endpointOverride(URI.create(awsS3Properties.getEndpoint()))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build());
    }
    return builder.build();
}
```

> **path-style 필수**: R2의 기본 엔드포인트(`<account>.r2.cloudflarestorage.com`)는 **path-style만 지원**한다. AWS SDK 기본값인 가상 호스트 스타일은 `<bucket>.<account>.r2.cloudflarestorage.com`(2단계 서브도메인)로 요청하는데, 와일드카드 인증서(`*.r2.cloudflarestorage.com`)가 이를 커버하지 못해 SSL 핸드셰이크가 실패한다. 따라서 endpoint가 설정될 때 `S3Client`/`S3Presigner` 양쪽에 `pathStyleAccessEnabled(true)`를 **반드시** 적용한다. (가상 호스트 스타일은 R2에 커스텀 도메인을 연결한 경우에만 가능)

### 2.3 `application.yaml`
`src/main/resources/application.yaml` (`aws.s3` 블록)

```yaml
aws:
  s3:
    access-key: ${AWS_ACCESS_KEY}      # R2 Access Key ID로 교체
    secret-key: ${AWS_SECRET_KEY}      # R2 Secret Access Key로 교체
    region: ${AWS_REGION:ap-northeast-2}   # 기본값은 S3 롤백 안전을 위해 유지, R2는 AWS_REGION=auto 주입
    bucket: ${AWS_S3_BUCKET}
    endpoint: ${AWS_S3_ENDPOINT:}      # https://<ACCOUNT_ID>.r2.cloudflarestorage.com
```

### 2.4 변경하지 않는 것
- **`AwsS3Adapter`** 전체 (멀티파트/presigned 로직) — S3 호환 API 그대로.
- 호출 서비스 구현체들(`InitiateApplyUploadServiceImpl`, `GetApplyPartUrlsServiceImpl`, `ApplyServiceImpl`, `AbortApplyUploadServiceImpl`, `GetApplyVideoUrlServiceImpl`).
- AWS SDK 의존성 버전 `2.28.0` (의도적 고정 — 체크섬 이슈 회피).

---

## 3. Terraform (Cloudflare R2)

신규 디렉토리 `terraform/`:
```
terraform/
  backend.tf      # R2를 S3 호환 원격 state backend로
  main.tf         # cloudflare provider + r2 bucket + CORS
  variables.tf    # api_token, account_id, bucket_name, allowed_origins
  outputs.tf      # endpoint, bucket name
  terraform.tfvars.example
```

### 3.1 자격증명 분리 (중요)
- **Terraform용**: Cloudflare 대시보드에서 **계정 레벨 R2 관리 권한** API 토큰 발급 → `var.cloudflare_api_token`.
- **앱 런타임용**: R2 → "Manage R2 API Tokens"에서 **버킷 범위(Object Read & Write)** 토큰 발급 → Access Key ID / Secret을 앱 `AWS_ACCESS_KEY`/`AWS_SECRET_KEY`로 사용. (Terraform 토큰과 별개)

### 3.2 `main.tf` 골자
```hcl
terraform {
  required_providers {
    cloudflare = { source = "cloudflare/cloudflare", version = "~> 4" }
  }
}

provider "cloudflare" {
  api_token = var.cloudflare_api_token
}

resource "cloudflare_r2_bucket" "video" {
  account_id = var.account_id
  name       = var.bucket_name
  location   = "APAC"
}

# 브라우저 직접 PUT 업로드를 위한 CORS (프론트 출처와 동기화)
resource "cloudflare_r2_bucket_cors_configuration" "video" {
  account_id  = var.account_id
  bucket_name = cloudflare_r2_bucket.video.name
  rules {
    allowed { methods = ["GET", "PUT"], origins = var.allowed_origins, headers = ["*"] }
    expose_headers  = ["ETag"]   # completeMultipartUpload는 파트 ETag가 필요
    max_age_seconds = 3600
  }
}
```
> CORS 리소스명/스키마는 사용 중인 cloudflare provider 4.x 실제 버전에 맞춰 확정한다(provider 메이저에 따라 R2 CORS 지원 여부·형식이 다를 수 있어, 미지원 시 R2 대시보드 수동 설정으로 대체하고 그 사실을 문서화).
> `allowed_origins`는 application.yaml의 `cors.allowed-origins`(현재 localhost 및 운영 프론트 도메인)와 일치시킨다. **ETag expose 필수** — 미설정 시 브라우저가 파트 ETag를 못 읽어 `completeMultipartUpload`가 실패.

### 3.3 `backend.tf` — R2를 원격 state backend로
R2는 S3 호환이므로 terraform `s3` backend로 사용 가능. R2 특성상 다음 skip 옵션과 **`skip_s3_checksum = true`(terraform 1.6.3+ 필요)**가 필수.

```hcl
terraform {
  backend "s3" {
    bucket = "<state-bucket>"     # 상태 전용 R2 버킷
    key    = "r2-migration/terraform.tfstate"
    region = "auto"
    endpoints = { s3 = "https://<ACCOUNT_ID>.r2.cloudflarestorage.com" }
    skip_credentials_validation = true
    skip_region_validation      = true
    skip_metadata_api_check     = true
    skip_requesting_account_id  = true
    skip_s3_checksum            = true
    use_path_style              = true
  }
}
```
> **부트스트랩 주의(chicken-and-egg)**: state용 R2 버킷은 `terraform init` 전에 **먼저 존재**해야 한다 → state 버킷은 R2 대시보드에서 수동 생성(또는 별도 부트스트랩). backend 자격증명은 `AWS_ACCESS_KEY_ID`/`AWS_SECRET_ACCESS_KEY` 환경변수로 R2 토큰 주입.

### 3.4 `.gitignore`
`*.tfvars`(단 `*.example` 제외), `.terraform/`, `*.tfstate*` 추가.

---

## 4. 데이터 마이그레이션 (전수 복사, 자동화)

S3→R2 전체 객체 복사를 **재현 가능한 스크립트**로 작성 (예: `terraform/`와 분리된 `scripts/migrate-s3-to-r2.sh`). rclone 사용.

```bash
# rclone.conf: [s3] 와 [r2](provider=Cloudflare, endpoint=https://<ACCOUNT_ID>.r2.cloudflarestorage.com) 정의
rclone copy s3:<SOURCE_BUCKET> r2:<DEST_BUCKET> \
  --transfers 16 --checkers 16 --progress
# 검증: 개수/크기 대조
rclone check s3:<SOURCE_BUCKET> r2:<DEST_BUCKET> --one-way
```
> 재실행 안전(idempotent): `rclone copy`는 동일 객체를 건너뜀. 운영 전환 직전 **증분 재동기화**(delta) 1회 더 수행 권장.

---

## 5. 전환 절차 (검증 환경 우선)

1. Terraform으로 R2 버킷 + CORS 생성 (`terraform apply`).
2. 데이터 전수 마이그레이션 스크립트 실행 + `rclone check` 통과 확인.
3. **검증 환경**에 R2 자격증명 / `AWS_S3_ENDPOINT` / `AWS_REGION=auto` / `AWS_S3_BUCKET` 주입 → 아래 §6 E2E 수행.
4. E2E 통과 시 **운영 전환**: GitHub Secrets 갱신(§7) 후 배포.
5. 문제 발생 시 **즉시 롤백**: `AWS_S3_ENDPOINT`를 비우고(또는 S3 자격증명 복원) 재배포 → 코드가 조건부라 S3로 자동 복귀.

---

## 6. 검증 (E2E)

1. **빌드/단위 테스트**: `./gradlew build` — Config/Properties 변경 컴파일·기존 테스트 통과.
2. **하위호환(S3 모드)**: `AWS_S3_ENDPOINT` 빈 값 + 기존 S3 자격증명으로 업로드/다운로드 정상 → 롤백 경로 보증.
3. **R2 모드 업로드 플로우**:
   - `createMultipartUpload` → `generatePartUploadUrl` presigned PUT으로 브라우저(또는 curl)에서 파트 업로드 → 응답 `ETag` 수신 확인 (CORS expose-headers 검증 포함) → `completeMultipartUpload`.
   - 잘못된 uploadId/ETag 시 4xx → `InvalidVideoFileException` 매핑 정상 확인.
4. **R2 모드 다운로드**: `generateVideoDownloadUrl` presigned GET으로 다운로드 + `Content-Disposition` 한글 파일명(UTF-8) 정상 확인. ← **R2의 `responseContentDisposition` 쿼리 파라미터 지원 여부 핵심 검증 포인트**.
5. **Range 검증**: `readObjectHead`의 `bytes=0-N` Range 요청으로 파일 시그니처 검증 정상 동작.
6. **Terraform**: `terraform init && terraform plan`으로 R2 버킷·CORS 리소스 계획 확인 (apply는 운영자 판단).
7. **CORS**: 브라우저 실제 출처에서 preflight(OPTIONS) 통과 확인.

---

## 7. 운영 작업 체크리스트 (코드 외 · 필수)

배포 반영을 위해 **레포 파일 변경과 별개로** 아래를 수동 적용:

- [ ] GitHub Secret **`APPLICATION_YML`** 갱신: `aws.s3.endpoint` 추가된 yaml 내용 반영 (워크플로가 `application.yml`로 생성 → 이 secret이 우선 적용됨).
- [ ] GitHub Secret **`ENV_FILE`** 갱신:
  - [ ] `AWS_ACCESS_KEY` / `AWS_SECRET_KEY` → R2 **버킷 범위** Access Key·Secret
  - [ ] `AWS_REGION=auto`
  - [ ] `AWS_S3_ENDPOINT=https://<ACCOUNT_ID>.r2.cloudflarestorage.com`
  - [ ] `AWS_S3_BUCKET` → R2 버킷명
- [ ] Cloudflare API 토큰 2종 발급·보관 (TF용 계정 토큰 / 앱용 버킷 토큰).
- [ ] Terraform backend용 state 전용 R2 버킷 사전 생성.
- [ ] (검증 환경) 동일 env 세트 별도 구성.

---

## 8. 명시적 범위 제외 (추후 별도 작업)

- 미완료 멀티파트 업로드 자동 정리(R2 lifecycle 규칙).
- 기존 AWS S3 버킷 비우기/삭제(decommission).
- R2 Custom Domain/CDN 기반 공개 다운로드(현재는 presigned GET 유지).
- AWS SDK 2.30+ 업그레이드(체크섬 대응 필요).
