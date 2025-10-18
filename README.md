# 🚗 출퇴근 교통 혼잡 분석 시스템 (Project_TrafficAnalysis)

> **CCTV 교통 데이터를 활용한 AI 기반 혼잡도 분석 및 출퇴근 시간 추천 서비스**

[![React](https://img.shields.io/badge/Frontend-React%20%7C%20Chart.js-lightblue)]()
[![Java](https://img.shields.io/badge/Backend-Java%20%7C%20Spring%20Boot-orange)]()
[![Python](https://img.shields.io/badge/AI-Python%20%7C%20YOLOv7-blue)]()
[![Oracle](https://img.shields.io/badge/DB-Oracle-red)]()

---

## 📌 프로젝트 개요

도시의 출퇴근 시간에는 특정 구간에서 교통 정체가 심각하게 발생합니다.  
기존 교통 서비스들은 단순히 “혼잡/원활” 정도의 정보만 제공하기 때문에,  
**실제 언제 출발하면 가장 덜 막히는지 알기 어렵다는 한계**가 있습니다.

이에 따라 **공공 교통 CCTV 데이터를 활용해 차량을 실시간으로 분석하고**,  
**요일/시간대별 교통 혼잡 패턴을 시각화**하여 사용자가 **최적의 출발 시간을 선택할 수 있도록 돕는 시스템**을 개발했습니다.

---

## 🧠 문제 정의

- 출퇴근 시간대 교통 혼잡이 심각하지만, 데이터 기반의 예측 서비스가 부족함  
- 기존 교통 포털은 단순한 “혼잡/원활” 정보만 제공  
- **실제 출발 시점별 혼잡도 예측**이 가능한 시스템 필요  

---

## 🏗 아키텍처 구성
```
CCTV 영상 → [Python(OpenCV + YOLOv7)] 차량 탐지
↓
[Spring Boot API] 데이터 수집 / DB 저장
↓
[Oracle DB] 시간대별 차량 수 통계
↓
[React Dashboard] 혼잡도 시각화 / 추천 기능

```
> 💡 선택 사항: `Docker`로 전체 환경을 컨테이너 기반 로컬 환경에서 통합 실행 가능

---

## ✨ 주요 기능

| 구분 | 설명 |
|------|------|
| 🚘 **차량 탐지 및 카운팅 (AI/영상 분석)** | OpenCV + YOLOv7 모델로 차량(자동차, 버스, 트럭, 오토바이 등) 탐지 및 카운트 |
| 🗄️ **데이터 저장 및 API 제공 (백엔드)** | Spring Boot REST API 서버에서 차량 수/시간 저장 및 통계 API 제공 |
| 📊 **대시보드 시각화 (프론트엔드)** | React + Chart.js를 이용해 CCTV별 혼잡도 시각화 및 통계 그래프 제공 |
| 🕐 **출퇴근 추천 시간 (추가기능)** | 혼잡도 데이터를 기반으로 가장 덜 막히는 출발 시각 추천 |
| 🔥 **Heatmap 확장** | 지도 기반으로 CCTV별 실시간 혼잡도 Heatmap 표시 (React-Leaflet) |

---

## 🛠 기술 스택

| 분야 | 기술 |
|------|------|
| **AI/영상분석** | Python, OpenCV, YOLOv7 |
| **백엔드** | Java, Spring Boot, Spring Data JPA |
| **데이터베이스** | **Oracle** |
| **프론트엔드** | React, Chart.js |
| **기타** | REST API, GitHub, Docker |

---

## 👩‍💻 역할 및 기여

| 역할 | 상세 내용 |
|------|------------|
| **AI 모듈 개발** | YOLOv7 + OpenCV로 차량 탐지 및 카운팅 구현 |
| **백엔드 서버 설계** | Spring Boot 기반 REST API 설계 및 DB(Oracle) 저장 로직 개발 |
| **데이터 연동** | Python 모듈 → API → Oracle DB로 차량 데이터 자동 누적 |
| **프론트엔드 연동** | React 대시보드에서 API 호출 및 통계 시각화 |
| **성능 개선** | CCTV 스트림 오류/지연 문제 해결, DB 인덱싱 최적화 |

---

### 🧩 트러블슈팅
[트러블슈팅](https://github.com/eononenoe/Project_TrafficAnalysis/tree/main/%ED%8A%B8%EB%9F%AC%EB%B8%94%20%EC%8A%88%ED%8C%85)
### 🛠️ 작업 내용
[작업 내용](https://github.com/eononenoe/Project_TrafficAnalysis/tree/main/%EC%9E%91%EC%97%85%20%EB%82%B4%EC%9A%A9)
### 🗺️ 프로젝트 로드맵
[로드맵](https://github.com/eononenoe/Project_TrafficAnalysis/blob/main/%EB%A1%9C%EB%93%9C%EB%A7%B5.md)

---

## 📊 주요 결과물

- **요일/시간대별 차량 수 통계**
- **혼잡도 Heatmap**
- **출퇴근 추천 시간 자동 계산**
- **실제 CCTV 데이터 기반 통계 시각화**
- **API 응답 예시**

```json
{
  "cctvId": 1,
  "day": "Monday",
  "hour": "08",
  "vehicleCount": 124,
  "congestionLevel": "HIGH"
}
```

---

## 📁 프로젝트 구조
```
traffic-analysis/
├── ai_module/
│   ├── detect.py              # YOLOv7 차량 탐지
│   └── send_to_api.py         # 백엔드 API 연동
├── backend/
│   ├── src/main/java/com/traffic/controller/
│   ├── src/main/java/com/traffic/entity/
│   └── src/main/java/com/traffic/repository/
├── frontend/
│   ├── src/components/
│   ├── src/pages/
│   └── src/App.js
└── README.md
```

---

## 🧩 배운 점 & 성장 포인트
```
Python과 Java 간 데이터 연동 및 통신 구조 이해
CCTV 영상 처리와 객체 탐지 모델의 실시간성 확보
REST API 설계 및 Oracle 인덱스 기반 성능 최적화 경험
AI와 백엔드, 프론트엔드를 통합한 시스템 아키텍처 구현 역량 강화
```
