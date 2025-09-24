작업 내용

ITS 공공데이터포털 CCTV API 연동 → 특정 좌표 CCTV URL 추출 성공.

YOLOv7-tiny 적용 → 차량 카운팅 기능 구현.

CLAHE / 밝기·대비 보정으로 흰색 트럭·야간 차량 탐지율 향상.

frame_skip 적용으로 프레임 드랍 개선.

Torch GPU 지원 버전 설치 성공 (RTX 3060 Ti + CUDA 11.8).

Spring Boot + Oracle DB 연동 완료.

application.yml 세팅 → DB 연결 성공.

TrafficEntity + TrafficController → 데이터 insert 검증 OK.

JSON ↔ LocalDateTime 변환 오류 해결 (yyyy-MM-dd'T'HH:mm:ss 패턴).

curl로 REST API 호출 및 테스트 성공 (/traffic/save).