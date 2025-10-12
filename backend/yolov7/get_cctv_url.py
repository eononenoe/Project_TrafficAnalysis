import requests
import xml.etree.ElementTree as ET
import argparse
import oracledb as cx_Oracle
from config import API_KEY

# Oracle DB 연결 정보
DB_USER = "system"           # DB 사용자명
DB_PASSWORD = "1234"   # DB 비밀번호
DB_DSN = "localhost:1521/xe"  # Oracle 접속 주소


def save_to_db(cctv_list):
    # CCTV 데이터를 DB에 저장
    try:
        # Oracle DB 연결
        conn = cx_Oracle.connect(user=DB_USER, password=DB_PASSWORD, dsn=DB_DSN)
        cur = conn.cursor()

        # CCTV_INFO 테이블에 데이터 삽입 SQL
        sql = """
        INSERT INTO CCTV_INFO 
        (NAME, COORDX, COORDY, MINX, MAXX, MINY, MAXY, STREAM_URL, LINE_NAME, LOCATION_NAME)
        VALUES (:1, :2, :3, :4, :5, :6, :7, :8, :9, :10)
        """

        # 리스트 형태로 여러 CCTV 정보를 한 번에 삽입
        cur.executemany(sql, cctv_list)
        conn.commit()   # DB에 반영
        print(f"[DB] {len(cctv_list)}개의 CCTV 정보 저장 완료")

    except Exception as e:
        # DB 오류 발생 시 콘솔 출력
        print("[DB] 저장 중 오류 발생:", e)

    finally:
        # DB 연결 종료
        if 'cur' in locals():
            cur.close()
        if 'conn' in locals():
            conn.close()


def get_cctv(minX, maxX, minY, maxY):
    # ITS Open API 호출 → CCTV 목록 수집 → DB 저장

    # ITS 국가교통정보센터 CCTV API URL 구성
    url = (
        "https://openapi.its.go.kr:9443/cctvInfo"
        f"?apiKey={API_KEY}"         # 인증키
        "&type=all"                  # 도로 유형
        "&cctvType=1"                # CCTV 종류 (고정형)
        f"&minX={minX}&maxX={maxX}"  # 경도 범위
        f"&minY={minY}&maxY={maxY}"  # 위도 범위
        "&getType=xml"               # XML 응답 형식
    )

    # API 요청 전송
    res = requests.get(url)

    # 응답 실패 시 오류 메시지 출력
    if res.status_code != 200:
        print("[Python] CCTV API 요청 실패:", res.status_code)
        return None

    # XML 파싱
    root = ET.fromstring(res.content)
    seen = set()       # 중복된 CCTV URL 방지용
    cctv_data = []     # DB 저장용 CCTV 데이터 목록

    # XML의 <data> 태그 반복 순회 (CCTV 1개당 1개 태그)
    for cctv in root.iter("data"):
        name = cctv.findtext("cctvname")   # CCTV 이름
        cctv_url = cctv.findtext("cctvurl")# CCTV 실시간 스트리밍 URL
        coordx = cctv.findtext("coordx")   # CCTV 경도 (X좌표)
        coordy = cctv.findtext("coordy")   # CCTV 위도 (Y좌표)

        line_name = "기타"
        location_name = name

        if name.startswith("[") and "]" in name:
            line_name = name.split("]")[0][1:]  # 대괄호 안의 노선명 추출
            location_name = name.split("]")[1].strip()  # 나머지 이름

        # 필수 데이터가 하나라도 없으면 무시
        if not (name and cctv_url and coordx and coordy):
            continue

        # 중복 URL이 아닐 때만 처리
        if cctv_url not in seen:
            seen.add(cctv_url)  # 이미 본 URL 저장

            # 문자열을 숫자로 변환
            coordx, coordy = float(coordx), float(coordy)

            # 주변 범위 계산 (지도 검색용)
            min_x, max_x = coordx - 0.01, coordx + 0.01
            min_y, max_y = coordy - 0.01, coordy + 0.01

            # 콘솔 로그 출력 (Spring Boot에서 stdout으로 읽을 수 있음)
            print(f"[CCTV] {name} ({coordx}, {coordy}) → {cctv_url}")
            print(cctv_url)

            # DB 저장용 데이터 튜플 추가
            cctv_data.append((
                name, coordx, coordy, min_x, max_x, min_y, max_y, cctv_url,
                line_name, location_name
            ))

    # CCTV 데이터가 있으면 DB에 저장
    if cctv_data:
        save_to_db(cctv_data)
    else:
        print("[Python] CCTV 데이터 없음")

    return cctv_data


if __name__ == "__main__":
    # 명령줄 인자 설정 (Spring Boot으로 전달)
    parser = argparse.ArgumentParser()
    parser.add_argument("--minX", type=float, required=True)
    parser.add_argument("--maxX", type=float, required=True)
    parser.add_argument("--minY", type=float, required=True)
    parser.add_argument("--maxY", type=float, required=True)
    args = parser.parse_args()

    # CCTV 정보 수집 및 저장 실행
    get_cctv(args.minX, args.maxX, args.minY, args.maxY)
