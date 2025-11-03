###  **Python 모듈 누락 오류 (`ModuleNotFoundError`)**

**문제**

* Spring Boot에서 Python 파이프라인 실행 시
  `ModuleNotFoundError: No module named 'requests'`, `'pandas'`, `'scipy'`, `'seaborn'` 등의 오류 발생.
* YOLOv7 내부 코드에서 사용하는 외부 패키지가 Python 환경에 설치되어 있지 않음.

**원인**

* Spring Boot가 실행 중인 Python 버전이 GPU 지원되지 않는 3.13 버전이었음.
* 이후 GPU 지원 가능한 **Python 3.10**으로 변경했지만,
  해당 환경에 YOLOv7 의존 패키지가 새로 설치되지 않아 오류가 반복됨.

**해결**

* Spring Boot의 Python 실행 경로를 3.10 버전으로 지정.

  ```java
  String pythonPath = "C:\\Users\\User\\AppData\\Local\\Programs\\Python\\Python310\\python.exe";
  ```
* Python 3.10 환경에 필요한 모든 패키지 설치:

  ```bash
  pip install torch torchvision torchaudio opencv-python oracledb numpy h5py pandas scipy tqdm matplotlib seaborn pyyaml requests
  ```

**결과**

* YOLOv7 파이프라인이 정상적으로 로드됨.
* CCTV 조회 → 차량 감지 → DB 저장이 한 번에 실행되는 정상 흐름 복구.

---

###  **CUDA 비활성화 오류 (`torch.cuda.is_available() is False`)**

**문제**

* YOLOv7 모델 로드 시

  ```
  RuntimeError: Attempting to deserialize object on a CUDA device but torch.cuda.is_available() is False
  ```

  라는 오류 발생.
* GPU가 존재함에도 PyTorch가 CUDA를 인식하지 못함.

**원인**

* Spring Boot가 Python 3.13을 실행하고 있었고,
  해당 버전의 PyTorch는 CUDA 빌드가 공식 지원되지 않음.
* 따라서 GPU 드라이버가 있어도 `torch.cuda.is_available()`이 `False`로 표시됨.

**해결**

* Python 3.10 환경에서 CUDA 지원 PyTorch 설치:

  ```bash
  pip install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cu121
  ```
* YOLOv7 코드에서 GPU/CPU 자동 선택 로직 추가:

  ```python
  device = 'cuda' if torch.cuda.is_available() else 'cpu'
  model = attempt_load(weights, map_location=torch.device(device))
  ```

**결과**

* GPU(`cuda`)가 정상 인식됨.
* YOLOv7 모델이 GPU에서 추론 실행 → 성능 약 5~6배 향상.

---

###  **YOLOv7 모델 파일 로드 실패 (`attempt_download KeyError`)**

**문제**

* `yolov7-tiny.pt` 모델을 로드할 때

  ```
  KeyError: 'assets'
  IndexError: list index out of range
  ```

  등의 오류 발생.

**원인**

* YOLOv7이 `google_utils.py`에서 GitHub Release API 호출을 시도했는데,
  로컬에 모델 파일이 이미 존재하지 않거나 경로가 잘못 설정되어 있었음.

**해결**

* YOLOv7 모델 파일(`yolov7-tiny.pt`)을
  `backend/yolov7/` 디렉토리 내부에 직접 배치.
* 코드에서 상대 경로 → 절대 경로로 명시:

  ```python
  weights = "C:\\Users\\User\\Desktop\\Project_TrafficAnalysis\\backend\\yolov7\\yolov7-tiny.pt"
  ```

**결과**

* 모델 파일 로드 정상화, GitHub 호출 불필요.
* YOLOv7 모델 로드 시 “Fusing layers...” 로그 출력 확인.

---

###  **Python 모듈 간 import 경로 문제**

**문제**

* `main_pipeline.py` 실행 시 `get_cctv_url` 또는 `traffic_counter` 모듈을 인식하지 못하는 오류 발생.

**원인**

* `main_pipeline.py`와 동일한 경로에 모듈이 있음에도
  Python의 실행 경로(`sys.path`)가 `yolov7` 상위 폴더가 아닌 다른 위치로 잡혀 있었음.

**해결**

* `main_pipeline.py` 상단에 실행 경로를 강제로 등록:

  ```python
  import sys, os
  sys.path.append(os.path.dirname(os.path.abspath(__file__)))
  ```
* 모듈 import 경로를 절대 경로 기반으로 수정.

**결과**

* `get_cctv_url.py`, `traffic_counter.py`가 정상 인식.
* CCTV 조회 → YOLO 카운팅 전체 실행 흐름 정상화.

---

###  **torch.meshgrid 경고 출력**

**문제**

* YOLOv7 실행 시 콘솔에 다음과 같은 경고가 반복 출력됨:

  ```
  UserWarning: torch.meshgrid: in an upcoming release, it will be required to pass the indexing argument.
  ```

**원인**

* PyTorch 2.9.0에서 `torch.meshgrid()`의 향후 변경사항을 알리는 내부 경고임.
* YOLOv7 코드(`utils/layers.py`)에서 `indexing` 인자를 명시하지 않아 발생.

**해결 (선택사항)**

* 기능상 문제는 없으므로 무시 가능.
* 경고 숨기기용 코드 추가 가능:

  ```python
  import warnings
  warnings.filterwarnings("ignore", category=UserWarning, message="torch.meshgrid")
  ```

**결과**

* 프로그램 실행에는 전혀 영향 없음.
* 필요 시 콘솔 로그 깔끔하게 유지 가능.

---

###  **최종 결과**

* 모든 의존성 모듈 설치 및 경로 수정 완료.
* GPU(CUDA) 환경에서 YOLOv7 모델 정상 추론.
* CCTV 영상 분석 → 차량 수 계산 → Spring Boot 연동 → Oracle DB 저장까지 전 과정 성공.
