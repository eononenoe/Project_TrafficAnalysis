### 백엔드(Spring Boot)
- `CctvController` 개선  
  - `/cctv/list` API 추가 → DB에 저장된 CCTV 목록 조회  
  - `/cctv/update` API 추가 → Python 실행 후 전국 CCTV 데이터 Oracle DB 저장  
  - `/cctv/url` API 추가 → 좌표 입력 시 Python 실행 → CCTV URL 반환  
  - `ProcessBuilder`로 `get_cctv_url.py` 호출 및 stdout 읽어 JSON 응답 반환  
- `CctvInfo` 엔티티 수정  
  - `LINE_NAME`, `LOCATION_NAME` 컬럼 추가 → 노선명/위치명 저장용  
  - React에서 노선·위치 선택에 활용 가능하도록 매핑  
- `CctvRepository` 유지  
  - CCTV 전체 목록 조회 (`findAll`) 사용  
- `config/WebConfig.java`  
  - React(3000) ↔ Spring Boot(8080) 간 교차 요청(CORS) 허용 설정  

---

### Python
- `get_cctv_url.py` 수정  
  - CCTV 이름, 좌표, URL 파싱 (`cctvname`, `coordx`, `coordy`, `cctvurl`)  
  - `[도로명] 위치명` 구조에서 `line_name`, `location_name` 분리 저장  
    ```python
    if name.startswith("[") and "]" in name:
        line_name = name.split("]")[0][1:]
        location_name = name.split("]")[1].strip()
    ```
  - `oracledb` 사용해 Oracle DB 연결  
  - `INSERT INTO CCTV_INFO (NAME, COORDX, COORDY, MINX, MAXX, MINY, MAXY, STREAM_URL, LINE_NAME, LOCATION_NAME)` 형태로 저장  
  - `[DB] 4608개의 CCTV 정보 저장 완료` → DB 반영 성공  
  - `seen = set()` 적용으로 중복 CCTV URL 제거  
- `traffic_counter.py`  
  - YOLOv7 모델 로딩 → 차량 수 탐지 및 평균 계산  
  - `/traffic/save` API로 DB 저장 (백엔드와 연동 구조 유지)  

---

### 프론트(React)
- `api.js`  
  - `fetchCctvUrl()` 추가 → `/cctv/url` 호출  
  - CCTV URL 데이터 받아 `<video>` 태그로 전달  
- `CoordinateForm.js`  
  - `/cctv/list` 로드 → CCTV 목록 DB에서 불러오기  
  - 데이터 없을 시 `/cctv/update` 자동 호출 (전국 범위 업데이트)  
  - 노선(`lineName`) 기준 중복 제거 및 **가나다순 정렬**, 빈 문자열 제거  
    ```js
    const uniqueLines = [
      ...new Set(
        cctvList
          .map((c) => c.lineName?.trim())
          .filter((line) => line && line !== "")
      ),
    ].sort((a, b) => a.localeCompare(b, "ko"));
    ```
  - 위치(`locationName`)도 가나다순 정렬 후 출력  
  - 선택 시 좌표 자동 입력 → CCTV 조회 버튼 클릭 시 URL 요청  
- `VideoStream.js`  
  - CCTV URL을 `<video>` 태그로 스트리밍 표시  
  - `hls.js` 적용으로 `.m3u8` HLS 영상 지원  
- `Dashboard.js`  
  - CCTV URL 상태 관리 및 영상 컴포넌트 연결  
  - 향후 혼잡도 그래프 연동 예정  