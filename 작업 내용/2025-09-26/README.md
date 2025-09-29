## 작업 내용

- React 프론트엔드 기본 구조 설계  
  - `index.js`, `App.js`, `pages/Dashboard.js`, `components/VideoStream.js`, `components/TrafficChart.js`, `services/api.js`  
  - 최소(MVP): CCTV 영상 + 혼잡도 그래프  
  - 확장: Heatmap(Leaflet.js), CCTV 선택 기능  

- Python 모듈 수정 방향  
  - `get_cctv_url(minX, maxX, minY, maxY)` 형태로 파라미터화  
  - React에서 입력값 받아 CCTV API 동적 호출 가능  
  - 전달 방식: (1) React → Spring Boot → Python, (2) CLI 인자(`argparse`)  

- 백엔드 API 확인  
  - `POST /traffic/save` : Python → Spring Boot → DB 저장  
  - `GET /traffic/stats?cctvId=1` : React에서 혼잡도 데이터 조회  

- 로드맵 정리 (오늘 확정한 부분)  
  1. CCTV 영상 → 차량 수 카운팅 (Python + YOLOv7)  
  2. 차량 수 → DB 기록 (Spring Boot + Oracle)  
  3. 요일/시간대별 혼잡도 API 제공 (`/traffic/stats`)  
  4. React 대시보드에서 CCTV 영상 + 그래프 + 지도 표시  