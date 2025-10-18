### Python ↔ Spring Boot 인코딩 깨짐 문제
- **문제**  
  - Spring Boot에서 Python 실행 시 `BufferedReader`로 읽은 한글이 깨짐 (`�ߺμ�`, `����` 등 출력)
- **원인**  
  - `InputStreamReader` 기본 인코딩이 `MS949`로 되어 있어 UTF-8 출력과 불일치
- **해결**  
  - Python 측 표준 출력 인코딩 지정  
    ```python
    sys.stdout.reconfigure(encoding='utf-8')
    ```
  - Java 측에서 명시적으로 UTF-8로 읽도록 변경  
    ```java
    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
    ```
- **결과**  
  - Python → Java 콘솔 출력 한글 완전 정상화  

---

### CCTV 이름에 `[서울]`, `[부산]`, `[춘천]` 등 중첩 표시 문제
- **문제**  
  - CCTV 이름이 `[경부선] [부산]옥천1터널(부산3)` 처럼 여러 대괄호 포함  
  - React UI와 DB에 불필요한 지역명이 반복 저장됨
- **원인**  
  - 기존 정규식이 첫 번째 대괄호만 처리하여 두 번째 이후가 남음
- **해결**  
  - 모든 `[ ... ]` 패턴을 전부 제거하는 정규식 적용  
    ```python
    if name:
        clean_name = re.sub(r'[\u200b\u3000\xa0\s]+', ' ', name).strip()
        location_name = re.sub(r'\[[^\]]*\]', '', clean_name).strip()
    ```
- **결과**  
  - `[서울]`, `[부산]`, `[밀양]` 등 모든 괄호 포함 텍스트 제거  
  - DB 및 UI에는 `옥천1터널(부산3)` 같은 순수 이름만 표시됨  

---

### 동일한 `locationName` 중복 표시 문제 (React)
- **문제**  
  - 동일한 `locationName`이 여러 CCTV 데이터에 존재할 경우  
    드롭다운에서 같은 이름이 여러 번 반복 표시됨
- **해결**  
  - `findIndex()` 기반 중복 제거 필터 추가  
    ```jsx
    .filter(
      (c, index, self) =>
        index === self.findIndex((t) => t.locationName === c.locationName)
    )
    ```
- **결과**  
  - 동일 이름은 한 번만 표시  
  - 노선 필터 및 가나다순 정렬 동작 유지  

---

### CCTV 다중 조회 (한 번 선택 시 여러 개 저장)
- **문제**  
  - 하나의 CCTV 선택 시 같은 지역 인근 CCTV 여러 개가 동시에 저장됨  
- **원인**  
  - Python의 좌표 범위 설정이 ±0.01로 너무 넓어  
    근접한 CCTV 여러 개가 검색됨
- **해결**  
  - 범위 축소로 검색 대상 제한  
    ```python
    min_x, max_x = coordx - 0.001, coordx + 0.001
    min_y, max_y = coordy - 0.001, coordy + 0.001
    ```
- **결과**  
  - 선택한 위치 기준 단일 CCTV만 검색 및 저장됨  

---

### DB 기존 데이터 유지로 정제 미반영
- **문제**  
  - 정규식 로직 수정 후에도 기존 잘못된 데이터(`"[서울]"`, `"[부산]"`)가 그대로 표시됨
- **원인**  
  - Python 스크립트는 새 데이터만 추가, 기존 레코드 갱신 없음
- **해결**  
  - 수동으로 테이블 데이터 초기화 후 재갱신
    ```sql
    DELETE FROM CCTV_INFO;
    COMMIT;
    ```
- **결과**  
  - 새로 수집된 CCTV 데이터만 표시되어 중복 및 잘못된 이름 완전 제거  

---

### 요약

| 구분 | 원인 | 조치 | 결과 |
|------|------|------|------|
| 인코딩 깨짐 | UTF-8 불일치 | Java `StandardCharsets.UTF_8` 지정 | 한글 정상 출력 |
| 이름 중복 괄호 | 정규식 미흡 | `re.sub(r'\[[^\]]*\]', '', name)` | 불필요한 지역명 제거 |
| 드롭다운 중복 | 중복 locationName 존재 | `.findIndex()` 중복 필터 | 한 번만 표시 |
| CCTV 다중 조회 | 좌표 범위 과대 | ±0.001로 축소 | 단일 CCTV만 검색 |
| DB 데이터 불일치 | 이전 데이터 유지 | `DELETE FROM CCTV_INFO` 후 갱신 | 데이터 정제 완료 |

---
