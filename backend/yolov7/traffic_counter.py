import cv2
import datetime
import time
import requests
import xml.etree.ElementTree as ET
import torch
from models.experimental import attempt_load         # YOLOv7 모델 불러오기
from utils.general import non_max_suppression, scale_coords
from utils.plots import plot_one_box                  # 바운딩 박스 그리기


# =============================
# 1. 공공데이터포털 CCTV API 호출
# =============================
API_KEY = "96debc4e17cc4895beb0d96b58d5e449"   # 발급받은 인증키

def get_cctv_url():
    url = (
        "https://openapi.its.go.kr:9443/cctvInfo"
        f"?apiKey={API_KEY}"
        "&type=all"
        "&cctvType=1"   # 1 = 실시간 스트리밍
        "&minX=127.12350&maxX=127.12365"   # 경도 범위
        "&minY=37.42880&maxY=37.42890"     # 위도 범위 
        "&getType=xml"  # XML 형식 응답
    )

    res = requests.get(url)
    if res.status_code == 200:
        root = ET.fromstring(res.content)
        for cctv in root.iter("data"):
            name = cctv.find("cctvname").text     # CCTV 이름
            cctv_url = cctv.find("cctvurl").text # 스트리밍 URL
            print(f"[CCTV] {name} → {cctv_url}")
            return cctv_url
        print("CCTV URL을 찾지 못했습니다.")
    else:
        print("CCTV API 요청 실패:", res.status_code)
        return None


# =============================
# 2. YOLOv7 모델 불러오기
# =============================
weights = "yolov7-tiny.pt"                     # YOLOv7 가중치 파일 # 더 가벼운 모델 yolov7-tiny.pt
device = 'cuda'                                # CPU : cpu / GPU : cuda
model = attempt_load(weights, map_location=device)  # GPU/CPU에 모델 로드
model.eval()                                   # 추론 모드로 전환


# =============================
# 3. 차량 탐지 & 카운팅 함수
# =============================

def run_vehicle_counter(cctv_url):
    cap = cv2.VideoCapture(cctv_url)   # OpenCV로 CCTV 스트리밍 열기
    cap.set(cv2.CAP_PROP_BUFFERSIZE, 1) # 버퍼 초소화
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
        #밤(18시~23시) + 새벽(0시~07시59분)만 보정 적용
        if hour >= 18 or hour < 8:
            # 1차 조정
            frame = cv2.convertScaleAbs(frame, alpha=1.0, beta=15)
            # alpha: 대비 (1.0 = 원본, >1 = 강조)
            # beta: 밝기 (양수 = 더 밝게)
            # 2차 조정
            gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY) # 흑백 변환
            clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8)) # 어두운 구역 밝게, 밝은 구역은 제한해서 대비 균일화
            frame = cv2.cvtColor(clahe.apply(gray), cv2.COLOR_GRAY2BGR) # CLAHE 적용 후 다시 BGR 3채널로 변환 (YOLO 입력 맞춤)



        # YOLO 입력 크기에 맞게 전처리
        img = cv2.resize(frame, (960, 960))             # YOLO 입력 크기로 resize
        img_in = torch.from_numpy(img).to(device)       # numpy → tensor 변환
        img_in = img_in.float() / 255.0                 # 0~255 → 0~1 정규화
        img_in = img_in.permute(2, 0, 1).unsqueeze(0)   # (HWC) → (1, C, H, W)

        # 추론 (YOLO 모델 forward)
        pred = model(img_in, augment=False)[0]
        # 후처리 (비최대 억제, NMS)
        pred = non_max_suppression(pred, 0.10, 0.45, classes=[2, 5, 7], agnostic=False)
        # 2=car, 3=motorcycle, 5=bus, 7=truck

        vehicle_count = 0  # 차량 수 초기화
        for det in pred:   # 탐지된 객체들
            im0 = frame.copy()
            if len(det):
                # YOLO 내부 좌표 → 원본 이미지 좌표로 변환
                det[:, :4] = scale_coords(img_in.shape[2:], det[:, :4], im0.shape).round()
                # 탐지된 객체별 처리
                for *xyxy, conf, cls in det:
                    cls_name = model.names[int(cls)]   # 클래스 이름 (예: car, bus)
                    if cls_name in ["car", "bus", "truck"]:
                        vehicle_count += 1             # 차량 계수
                    label = f"{cls_name} {conf:.2f}"  # 라벨 (클래스명 + 신뢰도)
                    plot_one_box(
                        xyxy,
                        im0,
                        color=(0, 255, 0),
                        label=f"{cls_name} {conf:.2f}",
                        line_thickness=2
                    )

        # =============================
        # 5초 간격으로 기록
        # =============================
        if time.time() - last_log_time >= 5:
            now = datetime.datetime.now()
            counts.append((now, vehicle_count))
            print(f"[{now}] 차량 수: {vehicle_count}")
            last_log_time = time.time()

         # =============================
         # 1분(=12번 기록)되면 집계 후 종료
         # =============================
        if len(counts) >= 12:
            print("===== 1분 기록 완료 =====")
            values = [cnt for _, cnt in counts]
            avg = sum(values) / len(values)
            max_cnt = max(values)
            total = sum(values)

            print(f"평균 차량 수: {avg:.2f}")
            print(f"최댓값: {max_cnt}")
            print(f"총합(1분간 차량 수): {total}")

            # DB 저장 or JSON 변환 자리 (예시)
            results = [{"time": str(t), "count": c} for t, c in counts]
            print("JSON 변환 예시:", results)

            break

        #cv2.imshow("Traffic CCTV", im0) # 결과 영상 표시 (개발용)
        if cv2.waitKey(1) & 0xFF == 27:  # ESC 키 누르면 종료
            break

    cap.release()
    cv2.destroyAllWindows()


# =============================
# 실행 시작
# =============================
if __name__ == "__main__":
    cctv_url = get_cctv_url()   # API로 CCTV URL 가져오기
    if cctv_url:                # URL이 있으면 YOLO 탐지 실행
        run_vehicle_counter(cctv_url)
