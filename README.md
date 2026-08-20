#  AURA - Backend

<div align="center">
  <img src="https://img.shields.io/badge/Java_21-007396?style=for-the-badge&logo=java&logoColor=white">
  <img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white">
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white">
  <br>
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white">
  <img src="https://img.shields.io/badge/Google_Cloud_Run-4285F4?style=for-the-badge&logo=google-cloud&logoColor=white">
  <img src="https://img.shields.io/badge/Google_Cloud_Storage-4285F4?style=for-the-badge&logo=google-cloud&logoColor=white">
  <img src="https://img.shields.io/badge/OpenAI-412991?style=for-the-badge&logo=openai&logoColor=white">
  <img src="https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=github-actions&logoColor=white">
</div>

## 📖 프로젝트 소개
AURA는 매장 방문객의 착장을 AI로 분석해 개인의 컬러 팔레트와 무드를 도출하고,
이를 3D로 렌더링된 MCM 백에 실시간으로 입혀주는 인터랙티브 미러 서비스입니다.
매장 아이패드에서의 체험이 QR을 통해 모바일로 이어지며, 영상과 디지털 보증서(Soul Tag)로 남습니다.

---

## ⚙️ 핵심 아키텍처 및 기능

### 1. ☁️ 인프라 및 배포 (DevOps)
- **Spring Boot & Cloud Run:** Spring Boot 기반으로 서버를 패키징하여 Google Cloud Run에 배포, 트래픽에 따른 유연한 Auto-scaling 환경 구축.
- **CI/CD 파이프라인:** GitHub Actions를 활용하여 `main` 브랜치 PR 머지 시 자동 빌드/배포 수행.

### 2. 🧠 AI 기반 아우라 분석 (OpenAI Vision)
- **실시간 분석 및 프롬프트 최적화:** 사용자의 이미지를 OpenAI gpt-4o-mini 모델로 분석하여 개인의 Mood와 핵심이 되는 2가지 메인 컬러 팔레트를 신속하게 추출합니다.
- **백엔드 컬러 연산 (Color Derivation):** AI의 연산 부담(토큰)과 환각 오류율을 줄이기 위해, 세 번째 포인트 컬러(Accent Color)는 AI에 의존하지 않고 백엔드 자체 알고리즘(`AuraColorDeriver`)을 통해 메인 컬러와 무드(Mood)를 기반으로 수학적으로 계산하여 완성도 높은 3색 팔레트를 구성합니다.
- **동적 소울태그(SoulTag) 생성:** 분석된 결과를 바탕으로 커스텀 소울태그 이미지를 서버에서 동적으로 렌더링 및 생성.

### 3. 📱 세션 및 상호작용 관리 (Kiosk ↔ Mobile)
- **익명 세션(Public ID) 발급:** 번거로운 회원가입이나 로그인 절차 없이,체험이 시작될 때마다 암호화된 난수(Public ID)를 즉시 발급하여 유저의 상태와 산출물을 익명으로 안전하게 식별 및 관리.
- **오프라인-모바일 연동:** 체험 기기(iPad)의 오프라인 상호작용 이벤트를 추적하고 세션 상태를 실시간으로 제어.
- **QR 코드 연동:** 사용자가 세션 체험을 마치면 모바일 랜딩 페이지로 이어지는 고유 QR 코드를 즉시 생성.

### 4. 🗄️ 대용량 미디어 처리 (Google Cloud Storage)
- **안전한 업로드:** 프론트엔드에서 직접 GCS로 무거운 숏폼 영상을 올릴 수 있도록 Signed URL 발급.
- **만료 데이터 스케줄링 청소:** 48시간이 지나 만료된 무거운 비디오 및 이미지(소울태그, QR) 파일들을 GCS에서 자동으로 삭제하여 클라우드 과금 방지.

### 5. 📊 어드민 리포트 (Admin)
- **다중 시트 엑셀 추출:** 팝업스토어 내 유저 상호작용 통계 및 랜딩 페이지 유입 데이터를 Apache POI를 통해 멀티시트 엑셀(Excel)로 자동 생성하여 제공.

---

## 💡 트러블 슈팅 및 기술적 도전 (Troubleshooting & Tech Challenges)

### 1. 무거운 미디어 파일로 인한 클라우드 과금 방지 (GCS Cleanup Scheduler)
- **문제:** 사용자가 세션을 체험할 때마다 수십 MB의 고화질 비디오, 썸네일, 소울태그 이미지, QR 코드가 구글 클라우드 스토리지(GCS)에 쌓이면서 스토리지 유지 비용이 급증할 위험이 있었습니다.
- **해결:** Spring Boot의 `@Scheduled`를 활용하여 매일 새벽 유휴 시간에 동작하는 `OutputCleanupService` 배치 스케줄러를 구현했습니다. DB에서 생성 후 48시간이 경과한 `SessionOutput` 엔티티를 조회하여 상태를 `EXPIRED`로 변경함과 동시에, GCS Bucket에 저장된 실제 미디어 객체(Object)들만 정확하게 타겟팅하여 물리적으로 삭제함으로써 불필요한 클라우드 과금을 원천 차단했습니다.

### 2. 협업 환경의 스키마 충돌 해결을 위한 Flyway 도입 (DB Migration)
- **배경:** 백엔드 개발자 2명이 하나의 Google Cloud SQL 인스턴스를 공유하며 개발하는 구조였습니다.
- **문제 (`ddl-auto: update`의 한계):**
  1. **컬럼 변경 미반영:** 컬럼명이나 타입을 변경해도 기존 컬럼이 삭제되지 않고 남아, 한 명의 변경사항이 다른 팀원의 로컬 INSERT 쿼리를 모두 실패(NOT NULL 제약조건 위반 등)하게 만드는 병목이 발생했습니다.
  2. **숨겨진 스키마 변경:** 스키마 변경이 코드 리뷰(PR)에 드러나지 않고 엔티티 Diff만 남으며, 실제 DDL은 '누가 먼저 앱을 부팅하느냐'에 의존하는 매우 불안정한 상태였습니다.
  3. **초기 시드 데이터 부재:** 프론트엔드 API 연동을 위해 매장/상품 데이터가 선행되어야 했으나, 한 번만 깔끔하게 주입할 위치가 없었습니다.
- **해결:** **Flyway**를 전격 도입하여 모든 스키마 변경과 초기 시드 데이터 주입을 `.sql` 마이그레이션 파일로 관리했습니다.
  - 변경 내역이 PR Diff에 명시적으로 노출되어 팀원 간의 코드 리뷰가 가능해졌습니다.
  - 실제로 '회전 제스처' 기능 개발 시 `rotation_degrees` 컬럼이 추가되었을 때, 마이그레이션 파일 단 한 장으로 팀원 환경에 충돌 없이 스키마가 동기화되는 효과를 보았습니다.
  - `char(7)`, `datetime(6)` 등 정밀한 데이터 타입을 정확히 DDL에 반영할 수 있었으며, 운영(Prod) 환경에서는 `ddl-auto: validate`를 사용하여 엔티티와 실제 스키마의 일치 여부만을 안전하게 검증하고 있습니다.

### 3. 동적 커스텀 이미지(SoulTag)의 서버 사이드 렌더링 및 I/O 최적화
- **문제:** AI 분석 결과에 따라 유저마다 각기 다른 '무드(Mood)' 텍스트와 '아우라 컬러(Aura Color HEX)' 팔레트가 조합된 고유의 **소울태그(SoulTag) 이미지**를 생성해야 했습니다. 외부 이미지 생성 API를 도입하거나 프론트엔드(Canvas)에서 렌더링한 후 백엔드로 다시 전송하는 방식은 불필요한 네트워크 지연과 추가 비용을 야기할 수 있었습니다.
- **해결:** Java 내장 라이브러리인 `Graphics2D`를 활용하여 백엔드 서버 메모리 단에서 템플릿 이미지 위에 동적 데이터를 직접 렌더링하는 `SoulTagImageGenerator`를 구현했습니다.
  - 커스텀 폰트(`.ttf`) 로드 및 안티앨리어싱(`KEY_ANTIALIASING`) 처리를 통해 텍스트 품질을 높이고, 유저의 컬러 HEX 코드에 맞춰 도형(`fillOval`)을 동적 좌표에 정확히 배치했습니다.
  - 특히 상태를 보존하지 않는(Stateless) Cloud Run 환경의 특성과 디스크 I/O 병목을 고려하여, 생성된 이미지를 로컬(디스크)에 임시 저장하지 않고 즉시 `ByteArrayOutputStream`을 거쳐 `byte[]` 스트림으로 변환한 뒤 GCS로 다이렉트 업로드하도록 최적화했습니다.

### 4. 에셋 매니페스트 API의 HTTP 캐싱 최적화 (Cache-Control)
- **문제:** 팝업스토어 현장의 키오스크와 모바일 웹은 화면을 그리기 위해 필수적인 3D/AR 기본 에셋(가방 패턴, 제스처 이미지 등)의 최신 URL 목록(Manifest)을 백엔드로부터 받아와야 했습니다. 에셋의 URL은 매우 드물게 변경됨에도 불구하고, 매 세션마다 프론트엔드가 지속적으로 API를 호출하면 서버의 트래픽과 리소스 낭비가 발생했습니다.
- **해결:** `AssetController`의 `/api/assets/manifest` 응답에 Spring의 `ResponseEntity`를 활용하여 `CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic()` 설정을 명시적으로 추가했습니다. 
  - 이를 통해 HTTP 응답 헤더에 `Cache-Control: max-age=300, public`이 포함되어 내려가며, 클라이언트(브라우저)는 5분 동안 동일한 API 요청을 서버로 보내지 않고 자체 로컬 캐시를 사용하게 됩니다.
  - 결과적으로 불필요한 서버 API 호출을 획기적으로 줄이고 네트워크 지연을 없애어, 오프라인 현장 키오스크의 체감 렌더링 속도를 한층 더 끌어올렸습니다.

---

## 👥 백엔드 역할 분담 (Backend Roles)


| 이름      | 담당 업무 및 기여                                                                                                                                                                                                                                                                                                                                         |
|---------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **김태희** | - 인프라 구축 및 CI/CD 파이프라인(Cloud Run, GitHub Actions) 자동화<br>- 키오스크 ↔ 모바일 간 세션 제어 로직 및 익명 세션(Public ID) 구현<br>- 미디어 만료 데이터 자동 삭제 스케줄러(Cleanup Scheduler) 구현<br>- OpenAI Vision API 연동 및 아우라 분석 프롬프트 고도화<br>- 백엔드 컬러 연산 로직(`AuraColorDeriver`) 구현<br>- Java `Graphics2D` 기반 동적 커스텀 이미지(SoulTag) 생성기 개발<br>- Apache POI를 활용한 어드민 통계 엑셀 다중 시트 다운로드 API 구현 |
| **김용빈** | - Google Cloud Storage(GCS) 직접 업로드를 위한 Signed URL 발급 로직 개발<br>- 모바일 랜딩 페이지 연결용 QR 코드 자동 생성 API 구현<br>- 모바일 랜딩 페이지 데이터 조회 및 상태 폴링 로직 구현                                                                                                                                                                                                             |
