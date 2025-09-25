
## 1. CCTV API 연동 문제
- **문제**  
  - 공공데이터포털 CCTV API 응답이 XML 형식  
  - 지정한 좌표 범위와 맞지 않으면 URL을 못 가져옴  

- **해결**  
  - `ElementTree`로 XML 파싱 후 `cctvurl` 추출  
  - 경도·위도 범위를 조정하여 원하는 CCTV URL 획득  

- **포인트**  
  > 실제 공공 데이터 API를 사용해 **좌표 기반 CCTV 스트림을 가져온 경험**  

---

## 2. YOLO 차량 탐지 정확도 & 성능 이슈
- **문제1**  
  - 흰색 트럭, 어두운 장면에서 탐지율 낮음  

- **해결1**  
  - `cv2.convertScaleAbs`, `CLAHE`(Contrast Limited Adaptive Histogram Equalization)로 영상 보정  
  - 탐지율 개선  

- **문제2**  
  - 프레임 처리량 과부하로 렉 발생  

- **해결2**  
  - `frame_skip` 적용, `cv2.CAP_PROP_BUFFERSIZE` 활용  
  - 초당 1~2프레임만 추론하여 성능 안정화  

- **포인트**  
  > 실시간 스트리밍 영상에서 **영상 보정 + 프레임 스킵 최적화로 성능 개선**  

---

## 3. GPU 사용 불가 (CUDA 설치 문제)
- **문제**  
  - `torch.cuda.is_available() == False`  
  - CPU 전용 PyTorch 설치됨  

- **해결**  
  ```bash
  pip uninstall torch torchvision torchaudio
  pip install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cu118
  ```
  GPU 지원 버전 설치 후 CUDA 정상 동작  

- **포인트**  
  > GPU 환경 세팅 문제 해결을 통해 모델 성능 개선  

---

## 4. Spring Boot + Oracle 연동
- **문제1**  
  - application.yml에서 Oracle 연결 실패  

- **해결1**  
  ```yaml
  spring:
    datasource:
      url: jdbc:oracle:thin:@localhost:1521:xe
      username: myuser
      password: mypass
      driver-class-name: oracle.jdbc.OracleDriver
  ```
  HikariCP 풀링 적용 → 연결 성공  

- **문제2**  
  - DTO → Entity 변환 시 LocalDateTime 파싱 오류 (DateTimeParseException)  

- **해결2**  
  - DateTimeFormatter 패턴을 `yyyy-MM-dd'T'HH:mm:ss`로 수정  

- **포인트**  
  > 백엔드에서 JSON ↔ DB 타입 매핑 오류 해결 경험  

---

## 5. REST API 검증 (PowerShell + curl)
- **문제**  
  - PowerShell에서 curl 옵션(-H, -d) 인식 불가  

- **해결**  
  ```powershell
  curl -X POST https://api.example.com/test `
       -H "Content-Type: application/json" `
       -d "{ \"id\": 123, \"name\": \"test\" }"
  ```
  이스케이프 처리 후 요청 성공  

- **포인트**  
  > 로컬 환경에서 REST API 검증 자동화 (curl 활용)  

---
