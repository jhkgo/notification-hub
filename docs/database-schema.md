# Database Schema

## 1. notifications

도메인 서비스가 "알림을 보내주세요"라고 요청한 내용을 저장하는 테이블


| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY | 알림 ID |
| type | VARCHAR(50) | NOT NULL | 알림 유형 (ORDER_COMPLETED, PAYMENT_FAILED, SYSTEM_ALERT) |
| title | VARCHAR(255) | NOT NULL | 알림 제목 |
| message | TEXT | NOT NULL | 알림 내용 |
| recipient_id | VARCHAR(100) | NOT NULL | 수신자 ID (고객 ID 또는 운영자 ID) |
| metadata | JSONB | | 추가 정보 (주문 번호, 결제 정보 등) |
| created_at | TIMESTAMP | NOT NULL | 생성 시간 |
| updated_at | TIMESTAMP | NOT NULL | 수정 시간 |

---

## 2. notification_deliveries

notifications를 참조해서 실제로 발송한 내역을 저장하는 테이블

| Column | Type | Constraints | Description                                  |
|--------|------|-------------|----------------------------------------------|
| id | BIGINT | PRIMARY KEY | 전송 ID                                        |
| notification_id | BIGINT | NOT NULL | 알림 ID                                        |
| channel | VARCHAR(20) | NOT NULL | 전송 채널 (EMAIL, SLACK)                         |
| status | VARCHAR(20) | NOT NULL | 전송 상태 (PENDING, PROCESSING, SUCCESS, FAILED) |
| recipient | VARCHAR(255) | NOT NULL | 수신처 (이메일 주소 또는 Slack 채널 URL)                 |
| sent_at | TIMESTAMP | | 실제 전송된 시간                                    |
| error_message | TEXT | | 전송 실패 시 에러 메시지                               |
| created_at | TIMESTAMP | NOT NULL | 생성 시간                                        |
| updated_at | TIMESTAMP | NOT NULL | 수정 시간                                        |

---

## 관계 (Relationships)

```
notifications (1) ----< (*) notification_deliveries
```

하나의 알림(notification)은 여러 전송(notification_deliveries)을 가질 수 있습니다.
예: 결제 실패 알림 → EMAIL 전송 + SLACK 전송

---

## DDL

```sql
-- notifications 테이블
CREATE TABLE notifications (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    recipient_id VARCHAR(100) NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);


-- deliveries 테이블
CREATE TABLE notification_deliveries (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    notification_id BIGINT NOT NULL,
    channel VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    sent_at TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- 인덱스
CREATE INDEX idx_notification_deliveries_status ON notification_deliveries(status);
CREATE INDEX idx_notification_deliveries_notification_id ON notification_deliveries(notification_id);

```
