### <select> 클릭 시 검은색 테두리(Outline) 표시 문제  

**문제**  
- 드롭다운(`<select>`)을 클릭하거나 Tab 키로 이동 시 검은색 외곽선(tab outline)이 생겨 UI가 어색하게 보임.  
- 브라우저의 기본 포커스 스타일로 인해 발생한 현상임.  

**원인**  
- 기본 `outline` 스타일이 제거되지 않아 브라우저가 포커스 시 기본 외곽선을 표시함.  

**해결**  
- CSS에서 `:focus` 상태일 때 `outline: none;` 적용.  
- 대신 `border-color`와 `box-shadow`를 사용해 부드러운 파란 포커스 효과로 대체.  

**결과**  
- 클릭 시 검은색 테두리가 사라지고, 파란색 그림자 효과로 시각적으로 깔끔해짐.  
- hover 및 focus 시 일관된 피드백 제공으로 UI 완성도 향상.  

---

### **README.md가 GitHub에서 한 줄로만 표시되는 문제**  

**문제**  
- GitHub에서 README.md가 이미지처럼 한 줄로만 표시되고 Markdown 서식이 전혀 적용되지 않음.  
- 내용은 Markdown 문법을 썼지만, GitHub이 일반 텍스트로 인식함.  

**원인**  
- 파일이 `UTF-8 with BOM` 인코딩으로 저장되어 있어 GitHub이 Markdown으로 렌더링하지 못함.  
- 또는 파일 이름(`README .md`)에 공백/대문자/특수문자 등의 오타가 있었을 가능성.  

**해결**  
- VS Code에서 인코딩을 **UTF-8 (without BOM)** 으로 변경 후 다시 저장.  
  - 하단 상태바 → “UTF-8 with BOM” 클릭 → “UTF-8” 선택 → 저장(Ctrl+S)  
- 첫 줄 맨 앞의 숨은 제어문자(보이지 않는 공백) 제거.  
- 파일 이름을 정확히 `README.md`로 유지.  

**결과**  
- GitHub에서 README.md가 정상적으로 Markdown 렌더링됨.  
- 헤더(`##`), 강조(`**`), 코드블록(```) 등 서식이 올바르게 표시됨.  

---
