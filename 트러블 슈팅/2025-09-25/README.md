2025-09-25 트러블슈팅1. CCTV API 연동 문제

문제: 공공데이터포털 CCTV API가 XML로 응답, 좌표 범위 안 맞으면 URL 못 가져옴.

해결: ElementTree로 XML 파싱 후 cctvurl 추출, 경도·위도 범위 조정해서 원하는 CCTV URL 획득.

포인트: "실제 공공 데이터 API를 사용해 좌표 기반 CCTV 스트림을 가져왔다" → 실무적인 경험.

2. YOLO 차량 탐지 정확도 & 성능 이슈

문제1: 흰색 트럭/어두운 장면에서 탐지 잘 안 됨.

해결1: cv2.convertScaleAbs, CLAHE(Contrast Limited Adaptive Histogram Equalization)로 영상 보정 → 탐지율 향상.

문제2: 프레임 처리 때문에 렉 발생.

해결2: frame_skip, cv2.CAP_PROP_BUFFERSIZE 활용해 초당 1~2프레임만 추론 → 성능 안정화.

포인트: "실시간 스트리밍 영상의 탐지 성능을 개선하기 위해 영상 보정 + 프레임 스킵 최적화" → AI 실무 경험 강조 가능.

3. GPU 사용 불가 → CUDA 설치 문제

문제: torch.cuda.is_available() == False, CPU 전용 PyTorch 설치됨.

해결: 기존 torch 제거 → pip install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cu118 로 GPU 지원 버전 설치.

포인트: "GPU 환경 세팅 문제를 직접 해결해 모델 성능 개선" → 환경 세팅 경험 강조 가능.

4. Spring Boot + Oracle 연동

문제1: application.yml 설정에서 Oracle 연결 안 됨.

해결1: spring.datasource.url, username, password, driver-class-name 세팅 → HikariCP 풀링으로 연결 성공.

문제2: DTO → Entity 변환 시 LocalDateTime 파싱 오류 (DateTimeParseException).

해결2: DateTimeFormatter 패턴을 JSON timestamp 형식 (yyyy-MM-dd'T'HH:mm:ss)에 맞춰 수정.

포인트: "백엔드에서 JSON-DB 타입 매핑 오류 해결" → 데이터 핸들링 능력 강조.

5. REST API 검증

문제: PowerShell에서 curl 옵션(-H, -d) 인식 안 됨.

해결: 이스케이프 처리 → curl -X POST ... -H "Content-Type: application/json" -d "{...}" 형태로 요청 성공.

포인트: "로컬에서 REST API 검증 자동화 (curl 사용)" → 개발 프로세스 기록.