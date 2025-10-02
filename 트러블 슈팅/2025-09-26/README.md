## 1.  프론트 구조 고민
  - 모든 기능을 Dashboard.js에 넣을지, 모듈화(Component 분리)할지 논의  
  - 결론: `VideoStream`, `TrafficChart`, `Heatmap` 컴포넌트로 분리  

## 2.  Python CCTV API URL 수정 문제
  - 기존: 좌표 하드코딩  
  - 문제: CCTV 범위를 유연하게 못 바꿈  
  - 해결: 함수 파라미터화 + React 입력값 전달로 수정 가능  

## 3.  백엔드 API 확인 이슈
  - `/traffic/stats` 호출 시 어떤 값이 넘어오는지 불명확  
  - 해결: Spring Boot Controller 코드 확인 → `cctvId` 필요, DTO 응답 구조 확인 완료  
