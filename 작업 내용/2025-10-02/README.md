### 백엔드(Spring Boot)
- `CctvController` 추가  
  - `/cctv/url` API 구현 (좌표 입력 → Python 실행 → CCTV URL 반환)  
  - `ProcessBuilder`로 `get_cctv_url.py` 실행, stdout 읽어서 JSON 반환  
- `config/WebConfig.java` 추가  
  - React(3000) → Spring Boot(8080) 교차 호출(CORS) 허용 설정

### Python
- `get_cctv_url.py` 분리  
  - 좌표(minX, maxX, minY, maxY) 받아 ITS API 호출  
  - `cctvname`, `cctvurl`, `coordx`, `coordy` 파싱  
  - `seen = set()` 적용 → CCTV URL 중복 제거  
  - `print(url)`로 CCTV URL을 Spring Boot에서 잡아 JSON으로 전달  
- `traffic_counter.py`  
  - YOLOv7 모델 로딩 후 차량 탐지 + 1분 단위 평균값 계산  
  - `/traffic/save` API로 DB 저장  
  - **프론트와 분리 → 서버에서 독립 실행 구조로 정리**

### 프론트(React)
- Axios `api.js` 수정  
  - `fetchCctvUrl` 추가 → `/cctv/url` 호출  
  - `fetchTrafficStats`로 혼잡도 데이터 조회 유지  
- `CoordinateForm.js`  
  - 좌표 입력 후 `fetchCctvUrl` 호출 → CCTV URL 상태 관리  
- `VideoStream.js`  
  - CCTV URL을 `<video>` 태그로 출력  
  - HLS(.m3u8) 지원 위해 `hls.js` 적용 (`<video ref>` 방식)  
- `Dashboard.js`  
  - CCTV URL 상태 관리 + 영상 출력 + 추후 혼잡도 그래프 연동 준비  
