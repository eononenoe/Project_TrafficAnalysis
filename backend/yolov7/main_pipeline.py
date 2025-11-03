import argparse
from get_cctv_url import get_cctv        # CCTV 조회 및 DB 저장
from traffic_counter import run_vehicle_counter  # YOLO 차량 카운터

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--minX", type=float, required=True)
    parser.add_argument("--maxX", type=float, required=True)
    parser.add_argument("--minY", type=float, required=True)
    parser.add_argument("--maxY", type=float, required=True)
    args = parser.parse_args()

    print("\n===== [1단계] CCTV 조회 및 DB 저장 =====")
    cctvs = get_cctv(args.minX, args.maxX, args.minY, args.maxY)

    if not cctvs or len(cctvs) == 0:
        print("[Python] CCTV 데이터가 없습니다. 종료합니다.")
        exit(0)

    print("\n===== [2단계] 차량 카운팅 시작 =====")
    # 첫 번째 CCTV URL 선택 (원하면 여러 개 반복문 가능)
    first_cctv = cctvs[0]
    name, coordx, coordy, minx, maxx, miny, maxy, url, line_name, location_name = first_cctv

    # 실제 DB CCTV ID를 사용할 수도 있음 (지금은 예시로 1번)
    cctv_id = 1

    print(f"[Python] 차량 카운팅 실행 중 → {name} ({url})")
    run_vehicle_counter(url, cctv_id)

    print("\n===== [완료] 차량 카운팅 및 DB 저장 종료 =====")
