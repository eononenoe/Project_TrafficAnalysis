###  백엔드 (Spring Boot)

#### 수정 파일: `CctvController.java`

- Python 실행 시 표준 출력 인코딩을 UTF-8로 지정하여 한글 깨짐 현상 해결

```java
BufferedReader reader = new BufferedReader(
    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
);
```

- `/cctv/update`, `/cctv/url` 엔드포인트 모두 UTF-8로 통일  
- Python 프로세스 실행 시 출력 스트림을 표준 UTF-8로 읽음  
- CCTV 목록 조회(`/cctv/list`) 기능은 기존 유지  
- 필요 시 `application.properties`에 다음 설정 추가 가능  

```properties
server.servlet.encoding.charset=UTF-8
server.servlet.encoding.enabled=true
server.servlet.encoding.force=true
```

---

###  Python (`get_cctv_url.py`)

- 출력 인코딩 문제 해결  

```python
import sys
sys.stdout.reconfigure(encoding='utf-8')
```

- CCTV 이름 정제 로직 개선  
  `[서울]`, `[부산]`, `[춘천]`, `[밀양]` 등 모든 괄호 구간 제거  
  특수 공백(`\u200b`, `\u3000`, `\xa0`) 제거  

```python
if name:
    clean_name = re.sub(r'[\u200b\u3000\xa0\s]+', ' ', name).strip()
    match = re.match(r'\[([^\]]+)\]', clean_name)
    if match:
        line_name = match.group(1).strip()
    location_name = re.sub(r'\[[^\]]*\]', '', clean_name).strip()
```

**예시 변환 결과:**

| 원본 이름 | line_name | location_name |
|------------|------------|----------------|
| `[경부선] [부산]옥천1터널(부산3)` | 경부선 | 옥천1터널(부산3) |
| `[호남선] [담양]문수산터널(담양외부)` | 호남선 | 문수산터널(담양외부) |

- 중복 CCTV URL 방지용 `seen = set()` 유지  
- ±0.01 → ±0.001 로 좌표 범위 축소 가능 (중복 CCTV 조회 감소)

```python
min_x, max_x = coordx - 0.001, coordx + 0.001
min_y, max_y = coordy - 0.001, coordy + 0.001
```

---

###  프론트엔드 (React)

#### 수정 파일: `CoordinateForm.js`

- `/cctv/list` 요청 시 DB가 비어 있으면 `/cctv/update` 자동 실행 후 CCTV 전체 데이터 로드  
- 노선(`lineName`) 중복 제거 및 가나다순 정렬  
- `locationName` 중복 제거 추가 (중복된 이름 하나만 표시)

```jsx
{cctvList
  .filter((c) => c.lineName?.trim() === selectedLine)
  .sort((a, b) => a.locationName.localeCompare(b.locationName, "ko"))
  // 중복된 위치명 제거
  .filter(
    (c, index, self) =>
      index === self.findIndex((t) => t.locationName === c.locationName)
  )
  .map((c) => (
    <option
      key={`${c.id}-${c.locationName}`}
      value={`${c.minX},${c.maxX},${c.minY},${c.maxY}`}
    >
      {c.locationName}
    </option>
  ))}
```

- CCTV 조회 버튼 클릭 시 선택한 좌표 범위로 `/cctv/url` 호출하여 CCTV URL 표시  

---

###  데이터베이스 (Oracle)

- 기존 깨진 데이터 제거  

```sql
DELETE FROM CCTV_INFO;
COMMIT;
```

---

###  개선 요약

| 구분 | 개선 내용 |
|------|------------|
| 인코딩 처리 | Spring ↔ Python 간 UTF-8 입출력 통일 |
| 이름 정제 | `[서울]`, `[부산]` 등 불필요한 괄호 구간 제거 |
| React 표시 | 동일한 `locationName` 중복 항목 제거 |
| DB 정리 | 기존 잘못된 데이터 삭제 후 갱신 |
| 검색 범위 | 필요 시 ±0.001 로 축소하여 중복 CCTV 최소화 |

---
