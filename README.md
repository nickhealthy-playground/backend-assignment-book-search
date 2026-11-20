# 프로젝트
- Library Search API

이 프로젝트는 사용자가 도서 제목으로 도서를 검색하고, 검색 결과를 페이징 형태로 제공하는 도서 검색 시스템입니다.  또한, 사용자들이 검색한 도서에 대한 통계를 제공하여, 인기 검색어와 일별 검색 횟수를 분석할 수 있습니다.

## api docs
http://localhost:8080/swagger-ui/index.html

## 외부 연동 API 출처
- https://openapi.naver.com
  - 네이버 '검색 - 책' API가 2026-07-31 종료되어, 같은 요청·응답 스펙을 재현한 `mock-naver-api` 모듈로 대체합니다.
- https://developers.kakao.com

## 기능 요구사항
- 도서 검색 기능
  - 도서 제목으로 도서를 검색하여 도서 정보를 제공해야 한다.
  - 검색 결과는 페이징 형태로 제공되어야 한다.
- 도서 검색 통계 기능
  - 사용자들이 검색한 도서에 대한 검색어 횟수를 일별로 제공해야한다.
  - 사용자들이 검색한 도서 순위를 상위 5개를 제공해야한다.

## 실행 방법
JDK 21 (Gradle toolchain), 포트 8080·9090을 사용합니다.

```bash
# 1. Mock 네이버 서버 (9090)
./gradlew :mock-naver-api:bootRun

# 2. 검색 API 서버 (8080, local 프로파일)
#    카카오 키가 없으면 기동되지 않습니다. 키 없이 둘러볼 때는 아무 값이나 넣으면 되고,
#    이 경우 네이버 장애 시 카카오 fallback 호출은 401로 실패합니다.
KAKAO_REST_API_KEY={카카오 REST API 키} ./gradlew :search-api:bootRun

# 3. 확인
curl "http://localhost:8080/v1/books?query=HTTP&page=1&size=10"
curl "http://localhost:8080/v1/books/stats?query=HTTP&date=$(date +%F)"
curl "http://localhost:8080/v1/books/stats/ranking"
```

- local 프로파일에서는 통계 API 확인용 샘플 데이터가 적재되고, 부하 확인용 `/v1/test` 엔드포인트가 열립니다.
- 테스트: `./gradlew test` (실제 외부 API를 호출하는 통합 테스트 2개는 `@Ignore`)

### 장애 재현 (서킷브레이커 · fallback 확인)
```bash
curl -X POST "http://localhost:9090/mock-admin/failure?mode=error"                 # 네이버 500 응답
curl -X POST "http://localhost:9090/mock-admin/failure?mode=delay&delayMs=5000"    # 네이버 5초 지연
curl -X POST "http://localhost:9090/mock-admin/failure?mode=none"                  # 정상 복구
```

### 검색 요청 제약
| 파라미터 | 범위 | 이유 |
|---|---|---|
| `page` | 1 ~ 50 | 카카오 page 상한 50 |
| `size` | 1 ~ 50 | 카카오 size 상한 50 |
| `(page-1) * size` | 1000 미만 | 네이버 start 상한 1000 |

네이버 장애로 카카오에 fallback 되어도 같은 요청이 유효하도록 두 API의 공통 범위로 제한합니다.

### 장애 대응 설정 (`search-api/src/main/resources/application.yml`)
| 항목 | 값 | 이유 |
|---|---|---|
| Feign connect / read timeout | 1s / 3s | 기본값(10s / 60s)이면 지연 장애 동안 요청 스레드가 묶이고 서킷이 열리지 않음 |
| slow call 기준 | 2s, 50% | 타임아웃 이전 단계의 지연도 장애 신호로 집계 |
| 실패 집계 제외 | 400 응답 | 잘못된 요청은 외부 API 장애가 아니므로 fallback·실패율에서 제외 (401·429·5xx는 집계) |
| 비동기 큐 포화 | CallerRunsPolicy | 통계 저장 실패가 검색 실패(500)로 번지지 않도록, 유실 없이 호출 스레드에서 저장 |

## 작업 목록
EPIC: LS-0
- LS-1: 규모추정 & 시스템디자인
- LS-2: 요구사항 작성
- LS-3: 멀티모듈 구성
- LS-4: 외부 api 연동
- LS-5: api 서버 구현
- LS-6: 데이터베이스 연결
- LS-7: 검색 통계 기능 구현
- LS-8: 문서화
- LS-9: 고가용성을 위한 설계
- LS-10: 실행 환경 정리 (local 전용 컴포넌트 분리, 패키지 정리, 실행 방법 문서화)
- LS-11: 외부 연동 정합성 · 장애 격리 (네이버 페이징 변환, 통계 시각, 타임아웃, 서킷 집계 기준, 비동기 큐 포화 처리)

## 시스템디자인 (AS-IS)
<img width="953" height="586" alt="Image" src="https://github.com/user-attachments/assets/c2087243-3c8c-4975-945a-f4370809828f" />

## 시스템디자인 (TO-BE)
<img width="1342" height="651" alt="Image" src="https://github.com/user-attachments/assets/9f60d870-bafa-4f55-9d1a-1062e30e549a" />

## 개선포인트
- External Server 별도 구성
- 외부 api 일 호출량 제한을 대비한 다수 키 매니징 기능 구현
- Event발행 별도 메세지 서버 구성
- RDS에 정해진 처리량만큼 write하는 worker 서버 구성
- 데이터베이스 일간 파티셔닝