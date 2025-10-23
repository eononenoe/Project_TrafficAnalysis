###  프론트엔드 (React)

#### 수정 파일: `CoordinateForm.js`

- 노선(`lineName`)과 위치(`locationName`) 선택 구조 유지.  
- 좌표값(`minX`, `maxX`, `minY`, `maxY`)은 프론트에 표시하지 않고 hidden 처리.  
- “노선 선택 → 위치 선택 → CCTV 조회” 순서로 UX 단순화.  
- 선택된 좌표값을 기반으로 `fetchCctvUrl()` 호출하여 CCTV URL 요청.  

#### 수정 파일: `index.css`
- select와 button 요소에 hover 효과 추가 (테두리 색 변화, 그림자 효과, 커서 변경).  
- focus 시 outline 제거 및 파란색 포커스 효과 추가로 시각적 통일성 확보.  

###  개선 요약
- 노선 및 위치 선택 로직 정상 작동.  
- 불필요한 좌표 입력창 제거로 UI 정돈.  
- select 클릭 시 발생하던 검은 outline 문제 해결 완료.  
- hover 및 focus 상태에서 자연스러운 인터랙션 제공으로 UX 향상.