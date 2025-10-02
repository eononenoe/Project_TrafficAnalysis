import requests
import xml.etree.ElementTree as ET
import argparse
from config import API_KEY

def get_cctv(minX, maxX, minY, maxY):
    url = (
        "https://openapi.its.go.kr:9443/cctvInfo"
        f"?apiKey={API_KEY}"
        "&type=all"
        "&cctvType=1"
        f"&minX={minX}&maxX={maxX}"
        f"&minY={minY}&maxY={maxY}"
        "&getType=xml"
    )
    res = requests.get(url)
    if res.status_code == 200:
        root = ET.fromstring(res.content)
        seen = set()   # 이미 본 URL 저장
        for cctv in root.iter("data"):
            name = cctv.findtext("cctvname")
            url = cctv.findtext("cctvurl")
            coordx = cctv.findtext("coordx")
            coordy = cctv.findtext("coordy")

            if url and url not in seen:   # 중복 필터링
                seen.add(url)
                print(f"[CCTV] {name} ({coordx}, {coordy}) → {url}")
                print(url)   # Spring Boot가 잡아가는 stdout

        if not seen:
            print("CCTV URL을 찾지 못했습니다.")
            return None
        return list(seen)
    else:
        print("CCTV API 요청 실패:", res.status_code)
        return None


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--minX", type=float, required=True)
    parser.add_argument("--maxX", type=float, required=True)
    parser.add_argument("--minY", type=float, required=True)
    parser.add_argument("--maxY", type=float, required=True)
    args = parser.parse_args()

    get_cctv(args.minX, args.maxX, args.minY, args.maxY)
