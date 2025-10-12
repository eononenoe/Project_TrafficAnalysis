## CCTV 데이터 갱신 및 URL 조회 문제
- **문제**  
  - `/cctv/update` 실행 시 Python 로그는 정상 출력되지만, DB 반영이 안 됨  
  - React에서 `/cctv/list` 호출 시 빈 배열이 반환되어 자동 `/cctv/update` 반복 호출  
  - `/cctv/url` 호출 시 좌표값에 해당하는 CCTV URL이 `null` 또는 잘못된 값으로 반환됨  

- **해결**  
  - Python `get_cctv_url.py` 내부 `oracledb` 연결에 `conn.commit()` 추가  
  - CCTV 이름 파싱 시 `[도로명] 위치명` 구조가 아닌 데이터도 처리하도록 조건문 보강  
  - React에서 `/cctv/list` 응답이 비어 있을 때 10초 대기 후 다시 조회하도록 수정  
  - URL 매칭 시 좌표 비교 범위(`minX~maxX`, `minY~maxY`) 재검토 및 오차 보정  

- **포인트**  
  > Python ↔ Spring Boot ↔ Oracle 간 연동 구조를 점검하며  
  > **데이터 저장·조회 전체 흐름을 안정화한 경험**

---

## CCTV 데이터 구조 및 엔티티 확장
- **문제**  
  - 노선명·위치명을 구분할 수 없어 React UI에서 정렬/필터 불가  

- **해결**  
  - `CctvInfo` 엔티티에 `LINE_NAME`, `LOCATION_NAME` 컬럼 추가  
  - Python에서 CCTV 이름 파싱 시 자동 분리 저장  
    ```python
    if name.startswith("[") and "]" in name:
        line_name = name.split("]")[0][1:]
        location_name = name.split("]")[1].strip()
    else:
        line_name = ""
        location_name = name.strip()
    ```
  - React에서 `lineName` 기준 중복 제거, `locationName` 가나다순 정렬 기능 구현  

- **포인트**  
  > CCTV 명칭을 구조적으로 분리하여  
  > **UI 필터링 및 데이터 시각화 확장 기반 확보**

---

## 백엔드 API 구조 개선 (Spring Boot)
- **문제**  
  - Python 호출 결과를 단순 콘솔 로그로만 확인 가능  
  - React에서 `/cctv/url` 요청 시 응답이 비어 있거나 JSON 변환 오류 발생  

- **해결**  
  - `ProcessBuilder`로 Python 실행 후 표준출력(`stdout`)을 읽어 JSON 형태로 반환  
  - `/cctv/list`, `/cctv/update`, `/cctv/url` API 명확히 분리  
  - CORS 설정(`config/WebConfig.java`)으로 React(3000) ↔ Spring Boot(8080) 통신 허용  

- **포인트**  
  > 백엔드에서 **Python 연동 + DB 입출력 + JSON 변환 구조 설계** 경험  

---

## 프론트엔드(React) 데이터 표시 로직 개선
- **문제**  
  - DB 비어 있을 때 `/cctv/list` 요청 반복으로 렉 발생  
  - 노선 목록에 공백·중복 값 존재  

- **해결**  
  - DB가 비었을 경우 `/cctv/update` 자동 호출 후 일정 시간 대기  
  - `lineName` 기준 중복 제거 및 가나다순 정렬  
    ```js
    const uniqueLines = [
      ...new Set(
        cctvList
          .map((c) => c.lineName?.trim())
          .filter((line) => line && line !== "")
      ),
    ].sort((a, b) => a.localeCompare(b, "ko"));
    ```
  - CCTV 선택 시 좌표 자동 입력, CCTV 조회 버튼 클릭 시 `/cctv/url` 호출  
  - `hls.js` 적용으로 `.m3u8` 실시간 영상 재생 지원  

- **포인트**  
  > React에서 **실시간 CCTV 조회 UI를 완성**하며  
  > 데이터 비동기 처리와 사용자 피드백 구조 개선 경험  

---
