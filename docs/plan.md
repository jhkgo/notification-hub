- [x] 1.0 Notification 생성 트랜잭션 처리
  - [x] 1.1 POST /notifications 요청 시 Notification과 채널별 Delivery를 함께 저장한다
  - [x] 1.2 모든 Delivery의 초기 상태를 PENDING으로 설정한다
  - [x] 1.3 API가 202 Accepted를 반환하도록 보장한다

- [x] 2.0 비동기 워커 전송 파이프라인
  - [x] 2.1 PENDING Delivery를 조회하면서 즉시 PROCESSING으로 잠근다
  - [x] 2.2 채널별 실제 전송을 외부 트랜잭션에서 수행한다
  - [x] 2.3 전송 결과에 따라 SUCCESS/FAILED로 상태를 업데이트한다

- [x] 3.0 스케줄 기반 워커 실행
  - [x] 3.1 @Scheduled 워커가 주기적으로 PENDING Delivery를 조회한다
  - [x] 3.2 워커 max-batch-size와 실행 주기를 설정 파일로 관리한다

- [x] 4.0 Slack Webhook 전송
  - [x] 4.1 Webhook URL을 설정에서 주입받는다
  - [x] 4.2 Slack 메시지를 규격에 맞게 구성해 전송한다

- [x] 5.0 Email Mock 전송
  - [x] 5.1 Email 전송 시도를 로그 또는 콘솔로 출력한다
  - [x] 5.2 전송 성공/실패 상태를 Delivery에 반영한다

- [ ] 6.0 조회 API 제공
  - [ ] 6.1 단건 Notification 상세 조회 API를 제공한다
  - [ ] 6.2 목록 조회 API를 제공하고 Delivery 상태까지 포함한다

- [ ] 7.0 Notification 종합 상태 계산
  - [ ] 7.1 Delivery 상태 집계를 통해 Notification 진행 상태를 계산한다
  - [ ] 7.2 계산 로직을 조회 API 응답에 포함한다
