###  백엔드 (Spring Boot)

#### 수정 파일: `TrafficController.java`

* `/traffic/start` 엔드포인트 추가 → Python 파이프라인 실행 명령 수행
* YOLOv7 파이프라인(`main_pipeline.py`) 자동 실행 기능 구현
* Python 경로를 GPU 지원되는 **Python 3.10**으로 변경

  ```java
  String pythonPath = "C:\\Users\\User\\AppData\\Local\\Programs\\Python\\Python310\\python.exe";
  ```
* CCTV 좌표(`minX`, `maxX`, `minY`, `maxY`)를 파라미터로 전달해 Python과 연동

#### 동작 결과

* CCTV 좌표 입력 → Spring Boot → Python 파이프라인 실행 성공
* YOLOv7이 차량 감지 후 `/traffic/save` API로 차량 수 전송
* Oracle DB에 차량 수, 시간, CCTV ID 정상 저장 확인

---

###  Python (YOLOv7 + Oracle 연동)

#### 수정/추가 파일

* **`main_pipeline.py`**

  * `get_cctv_url.py` + `traffic_counter.py` 통합 실행 스크립트
  * CCTV 조회(1단계) → 차량 카운팅(2단계) 순으로 자동 실행
  * Spring Boot stdout으로 진행 로그 출력 추가

* **`traffic_counter.py`**

  * YOLOv7 모델 로드 시 GPU/CPU 자동 선택 코드 적용

    ```python
    device = 'cuda' if torch.cuda.is_available() else 'cpu'
    model = attempt_load(weights, map_location=torch.device(device))
    ```
  * 1분 동안 차량 수 카운팅 후 평균/최댓값 계산
  * 결과를 Spring Boot `/traffic/save`로 JSON 전송

* **패키지 설치 (Python 3.10 환경)**

  ```
  pip install torch torchvision torchaudio opencv-python oracledb numpy h5py pandas scipy tqdm matplotlib seaborn pyyaml requests
  ```

  → YOLOv7 정상 실행 및 GPU 인식(`torch.cuda.is_available() == True`)

---

###  프론트엔드 (React)

#### 수정 파일: `Dashboard.js`

* “분석 시작” 버튼 → `/traffic/start` API 호출 추가
* 선택된 CCTV 좌표 및 ID를 전달하여 YOLO 분석 시작
* 실시간 통계(`TrafficChart`)와 연동 구조 개선

#### 수정 파일: `CoordinateForm.js`

* CCTV 선택 시 `setSelectedCctvId()`로 ID 전달
* 조회 시 `fetchCctvUrl()` 호출하여 영상 URL 로드
* CCTV 선택 UX 유지 (노선 → 위치 → 조회)

---

### 🔹 실행 결과

| 구분                       | 결과                             |
| ------------------------ | ------------------------------ |
| CCTV 조회                  |  정상 작동                        |
| YOLOv7 차량 감지             |  GPU 기반 추론 성공                 |
| API 통신 (`/traffic/save`) |  Spring Boot에서 DB 저장 확인       |
| Oracle DB 저장             |  차량 수, 시간, CCTV ID 정상 기록      |
| 프론트 혼잡도 표시               |  `/traffic/stats` 통해 통계 조회 가능 |

---

###  **개선 요약**

* Python–Spring–React 전 구간 데이터 파이프라인 완성
* GPU 환경에서 YOLOv7 실시간 차량 감지 성공
* Oracle DB와 완벽 연동 (Hibernate Insert 로그 확인)
* 프론트에서 “조회 → 분석 시작 → 혼잡도 표시” 전체 흐름 자동화

