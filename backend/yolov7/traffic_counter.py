import cv2
import datetime
import time
import requests
import xml.etree.ElementTree as ET
import torch
import argparse
from models.experimental import attempt_load         # YOLOv7 모델 불러오기
from utils.general import non_max_suppression, scale_coords
from utils.plots import plot_one_box                  # 바운딩 박스 그리기
from config import API_KEY


# =============================
# API 전송 함수 (Spring Boot)
# =============================
def send_vehicle_count(vehicle_count, cctv_id):
    API_URL = "http://localhost:8080/traffic/save"
    payload = {
        "cctvId": cctv_id,
        "timestamp": datetime.datetime.now().strftime("%Y-%m-%dT%H:%M:%S"),
        "vehicleCount": vehicle_count
    }
    headers = {"Content-Type": "application/json"}

    try:
        response = requests.post(API_URL, headers=headers, json=payload)
        if response.status_code == 200:
            print(f"[SUCCESS] 전송 성공 → {payload}")
        else:
            print(f"[FAIL] 전송 실패 ({response.status_code}): {response.text}")
    except Exception as e:
        print(f"[ERROR] API 호출 에러: {e}")


# =============================
# 1. 공공데이터포털 CCTV API 호출
# =============================
# def get_cctv(minX, maxX, minY, maxY):
#     url = (
#         "https://openapi.its.go.kr:9443/cctvInfo"
#         f"?apiKey={API_KEY}"
#         "&type=all"
#         "&cctvType=1"
#         f"&minX={minX}&maxX={maxX}"
#         f"&minY={minY}&maxY={maxY}"
#         "&getType=xml"
#     )
#
#     res = requests.get(url)
#     if res.status_code == 200:
#         root = ET.fromstring(res.content)
#         for cctv in root.iter("data"):
#             cctv_id = cctv.find("cctvid").text     # CCTV ID
#             name = cctv.find("cctvname").text      # CCTV 이름
#             cctv_url = cctv.find("cctvurl").text   # 스트리밍 URL
#             print(f"[CCTV] ID={cctv_id}, {name} → {cctv_url}")
#             return cctv_id, cctv_url
#         print("CCTV URL을 찾지 못했습니다.")
#         return None, None
#     else:
#         print("CCTV API 요청 실패:", res.status_code)
#         return None, None



# =============================
# 2. YOLOv7 모델 불러오기
# =============================
weights = "yolov7-tiny.pt"                     # YOLOv7 가중치 파일
device = 'cuda'                                # CPU : cpu / GPU : cuda
model = attempt_load(weights, map_location=device)  # GPU/CPU에 모델 로드
model.eval()                                   # 추론 모드로 전환


# =============================
# 3. 차량 탐지 & 카운팅 함수
# =============================
def run_vehicle_counter(cctv_url, cctv_id):
    cap = cv2.VideoCapture(cctv_url)   # OpenCV로 CCTV 스트리밍 열기
    cap.set(cv2.CAP_PROP_BUFFERSIZE, 1) # 버퍼 최소화
    if not cap.isOpened():
        print("CCTV 스트림 연결 실패")
        return

    frame_skip = 30  # 30프레임마다 1장만 처리 (대략 1초)
    frame_count = 0

    last_log_time = time.time()
    counts = [] # (timestamp, vehicle_count) 저장 리스트

    while True:
        ret, frame = cap.read()
        if not ret:
            print("프레임 수신 실패")
            break

        frame_count += 1
        if frame_count % frame_skip != 0:
            continue

        # 현재 시각 (0~23)
        hour = datetime.datetime.now().hour
        # 밤(18~23시) + 새벽(0~7시59분) 보정 적용
        if hour >= 18 or hour < 8:
            frame = cv2.convertScaleAbs(frame, alpha=1.0, beta=15)
            gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
            clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
            frame = cv2.cvtColor(clahe.apply(gray), cv2.COLOR_GRAY2BGR)

        # YOLO 입력 크기에 맞게 전처리
        img = cv2.resize(frame, (960, 960))
        img_in = torch.from_numpy(img).to(device)
        img_in = img_in.float() / 255.0
        img_in = img_in.permute(2, 0, 1).unsqueeze(0)

        # 추론
        pred = model(img_in, augment=False)[0]
        pred = non_max_suppression(pred, 0.10, 0.45, classes=[2, 5, 7], agnostic=False)

        vehicle_count = 0
        for det in pred:
            im0 = frame.copy()
            if len(det):
                det[:, :4] = scale_coords(img_in.shape[2:], det[:, :4], im0.shape).round()
                for *xyxy, conf, cls in det:
                    cls_name = model.names[int(cls)]
                    if cls_name in ["car", "bus", "truck"]:
                        vehicle_count += 1
                    plot_one_box(
                        xyxy,
                        im0,
                        color=(0, 255, 0),
                        label=f"{cls_name} {conf:.2f}",
                        line_thickness=2
                    )

        # 5초 간격으로 기록
        if time.time() - last_log_time >= 5:
            now = datetime.datetime.now()
            counts.append((now, vehicle_count))
            print(f"[{now}] 차량 수: {vehicle_count}")
            last_log_time = time.time()

        # 1분(=12번 기록)되면 집계 후 종료
        if len(counts) >= 12:
            print("===== 1분 기록 완료 =====")
            values = [cnt for _, cnt in counts]
            avg = sum(values) / len(values)
            max_cnt = max(values)
            total = sum(values)

            print(f"평균 차량 수: {avg:.2f}")
            print(f"최댓값: {max_cnt}")
            print(f"총합(1분간 차량 수): {total}")

            # DB 저장 전송
            send_vehicle_count(int(avg), cctv_id)

            break

       # cv2.imshow("Traffic CCTV", im0) # 결과 영상 표시 (개발용)
        if cv2.waitKey(1) & 0xFF == 27:  # ESC 키 누르면 종료
            break

    cap.release()
    cv2.destroyAllWindows()


# =============================
# 실행 시작
# =============================
# if __name__ == "__main__":
#     parser = argparse.ArgumentParser()
#     parser.add_argument("--minX", type=float, required=True)
#     parser.add_argument("--maxX", type=float, required=True)
#     parser.add_argument("--minY", type=float, required=True)
#     parser.add_argument("--maxY", type=float, required=True)
#     args = parser.parse_args()
#
#     cctv_id, cctv_url = get_cctv(args.minX, args.maxX, args.minY, args.maxY)
#     if cctv_url:
#         run_vehicle_counter(cctv_url, cctv_id)