# Notification Hub (MVP) --- Product Requirements Document

## 1. 소개 / 개요

여러 도메인 서비스(주문/결제/모니터링)에서 알림 요청을 받아,
고객/운영자를 대상으로 Email·Slack 등으로 비동기 전송하고
전송 로그를 관리하는 내부용 Notification Hub.

## 2. 목표 (Goals)

-   도메인 서비스가 Notification Hub에 알림 요청을 보내면,
    Slack 또는 Email(Mock)로 실제 전송될 것.

-   알림 전송 로직은 API 요청과 분리되어,
    별도의 비동기 워커가 처리될 것.

-   모든 알림 생성 및 채널별 전송 결과는
    DB에 저장되고 조회 가능할 것.

## 3. 사용자 스토리 (User Stories)

### 3.1 주문 서비스

-   주문 서비스는 주문이 완료되면 Notification Hub에 알림을 전송하고,
    고객이 Email로 주문 완료 정보를 받을 수 있기를 원한다.

### 3.2 결제 서비스

-   결제 서비스는 결제 실패가 발생하면 Hub에 알림을 전송하고,
    고객에게 Email, 운영팀에게 Slack으로 각각 알림이 전달되기를 원한다.

### 3.3 운영자(내부 직원)

-   운영자는 결제 실패나 시스템 장애 알림을 Slack에서 실시간으로
    확인하고,
    필요할 경우 Hub의 조회 API로 상세 알림 내역을 확인할 수 있기를
    원한다.

## 4. 기능 요구사항 (Functional Requirements)

1.  `POST /notifications` 요청 시, 시스템은 단일 트랜잭션 내에서 다음을 모두 처리해야 한다.
    - 알림의 메타 정보(Notification)를 DB에 저장한다.
    - 요청된 각 채널(EMAIL, SLACK 등)에 대한 개별 전송 작업(Delivery)을 생성하고, 초기 상태를 **PENDING**으로 하여 DB에 함께 저장한다.

2.  시스템은 별도의 비동기 워커를 통해 다음 순서로 전송 작업을 처리해야 한다.
    - 작업 선점(Locking): DB에서 PENDING 상태의 Delivery들을 일정 개수 조회한 후, 즉시 상태를 **PROCESSING**으로 변경하여 다른 워커가 중복으로 작업을 가져가지 못하도록 한다.
    - 전송 실행: PROCESSING 상태로 변경된 Delivery 목록에 대해 실제 전송(Slack 웹훅 호출, Email 로깅 등)을 시도한다. 이 과정은 외부 시스템 호출을 포함하므로 DB 트랜잭션 외부에서 수행한다.
    - 결과 업데이트: 전송 시도 후, 결과에 따라 각 Delivery의 상태를 SUCCESS 또는 FAILED로 업데이트한다.

3.  시스템은 별도의 비동기 워커를 통해
    PENDING 상태의 Delivery를 일정 주기로 조회하고 전송을 시도해야 한다.

4.  시스템은 Slack Webhook을 사용하여
    운영팀 Slack 채널로 메시지를 전송해야 한다.

5.  시스템은 Email 전송을 Mock 방식(콘솔 또는 로그 출력)으로 처리하고
    성공/실패 상태를 기록해야 한다.

6.  시스템은 알림 및 채널별 전송 결과를
    조회할 수 있는 API(상세 조회/목록 조회)를 제공해야 한다.

7. 단일 Notification의 종합 상태(예: 처리 중, 완료, 실패)는 별도 컬럼으로 저장하지 않고, 조회 시점에 연관된 모든 Delivery들의 상태를 조합하여 동적으로 결정한다.

## 5. 비범위 (Non-Goals)

이번 MVP에서는 아래 기능들을 포함하지 않는다.
이 기능들은 추후 확장 가능하지만, 현재 범위에서는 제외한다.

-   사용자 인증 및 권한 관리
-   이메일 템플릿(HTML/Markdown)
-   실제 Email 서버(SMTP) 연동
-   예약 알림(스케줄 기반 알림)
-   전송 재시도, 백오프, Dead Letter Queue(DLQ)
-   관리자 Web UI
-   추가 알림 채널(SMS, WebPush 등)

## 6. 기술 고려사항 (Technology Considerations)

-   백엔드 서버는 Spring Boot 기반의 REST API로 구성한다.
-   데이터베이스는 PostgreSQL을 사용하며, 로컬 개발 환경에서는
    docker-compose로 실행한다.
-   데이터 저장은 JPA/Hibernate 기반으로 구현한다.
-   비동기 처리 워커는 Spring의 `@Scheduled` 기능을 사용하며,
    PENDING 상태의 delivery를 주기적으로 조회하여 전송을 시도한다.
-   Slack 알림 전송은 Slack Webhook을 사용하며,
    Webhook URL은 환경변수 또는 설정 파일에서 관리한다.
-   Email 전송은 실제 SMTP 연동 없이 콘솔 출력 또는 로깅 방식의 Mock
    구현으로 처리한다.
-   서버 설정, DB 접속 정보 등 환경 의존적인 값은 application.yml 또는
    환경변수로 분리 관리한다.
-   Notification 엔티티는 그 자체의 진행 상태(예: PENDING, COMPLETED)를 갖지 않는다. 
-   전체 알림의 성공 여부는 연관된 Delivery 엔티티들의 상태를 통해 논리적으로 판단한다. 
-   Delivery 엔티티는 PENDING, PROCESSING, SUCCESS, FAILED의 명확한 상태 값을 가지며, 비동기 워커의 작업 흐름을 제어하는 데 사용된다.-
-   PROCESSING 상태는 작업 중복 실행을 방지하기 위한 Lock 역할을 수행한다.

## 7. 성공 지표 (Success Metrics)

-   도메인 서비스가 Notification Hub에 알림을 요청하면,
    Slack 또는 Email(Mock)로 실제로 전송되어야 한다.

-   알림 생성 API의 평균 응답 속도는 200ms 이하를 유지해야 한다.

-   각 알림과 채널별 전송 결과(SUCCESS/FAILED)는 DB에 정확하게
    저장되고,
    조회 API를 통해 확인할 수 있어야 한다.

-   주요 시나리오(주문 완료, 결제 실패, 시스템 장애) 3가지가
    모두 정상적으로 처리되어야 한다.

## 8. 오픈된 질문 (Open Questions)

-   비동기 워커의 실행 주기는 몇 초로 설정할 것인가?  
    (예: 3초, 5초, 10초 --- 트래픽 증가 시 조정 필요)

-   Slack 메시지는 단순 텍스트로 유지할 것인가,  
    아니면 향후 Block Kit 기반 포맷을 사용할 것인가?

-   Email(Mock)은 현재 콘솔 출력 방식인데,  
    실제 SMTP 연동을 할 경우 어느 메일 서비스를 사용할 것인가?

-   알림 요청에 대한 인증/토큰 처리(API Key or JWT 등)는  
    어떤 방식으로 적용할 것인가?
-   비동기 워커가 PROCESSING 상태의 작업을 처리하던 중 장애로 인해 서버가 다운되면,  
    해당 작업은 영원히 PROCESSING 상태로 남게 된다. 이런 '유령(Stuck) 작업'에 대한 처리 정책은 어떻게 할 것인가?
