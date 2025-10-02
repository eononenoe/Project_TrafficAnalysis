- **CCTV URL 조회 오류 (`AttributeError: 'NoneType' object has no attribute 'text'`)**  
  - 원인: ITS API 응답에 `<cctvid>` 태그가 존재하지 않음  
  - 해결: `cctv.findtext("cctvid")` 대신 `cctvname`, `coordx`, `coordy`, `cctvurl` 파싱으로 수정  

- **CCTV URL 중복 출력 문제**  
  - 원인: 동일 좌표 CCTV 여러 대가 응답으로 내려옴  
  - 해결: Python 코드에서 `seen = set()` 적용하여 URL 중복 제거  

- **프론트 Axios `Network Error`**  
  - 원인: React(3000) → Spring Boot(8080) 호출 시 CORS 미설정  
  - 해결: `config/WebConfig.java` 추가하여 `http://localhost:3000` 허용  

- **Spring Boot → Python 실행 경로 오류**  
  - 원인: `get_cctv_url.py` 파일이 존재하지 않아 ProcessBuilder 실행 실패  
  - 해결: CCTV URL 조회 전용 스크립트를 분리하여 `backend/yolov7/get_cctv_url.py` 생성 후 경로 맞춤  

- **프론트 CCTV 영상 출력 문제**  
  - 원인: ITS API CCTV URL이 HLS(.m3u8) 스트리밍 → `<video>` 태그만으론 재생 불가  
  - 해결: `hls.js` 라이브러리 적용, `<video ref>` 방식으로 HLS 지원  

- **구조 정리**  
  - 백엔드: `controller / service / repository / entity / dto / config` 패키지 구조로 재정리  
  - 프론트: `components / pages / services / styles` 기준 모듈화 유지  
