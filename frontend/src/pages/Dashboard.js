import React, { useState, useEffect } from "react";
import CoordinateForm from "../components/CoordinateForm";
import VideoStream from "../components/VideoStream";
import TrafficChart from "../components/TrafficChart";
import { fetchTrafficStats } from "../services/api";

function Dashboard() {
  const [cctvUrl, setCctvUrl] = useState("");
  const [stats, setStats] = useState([]);
  const [selectedCctvId, setSelectedCctvId] = useState(null);
  const [coords, setCoords] = useState({ minX: "", maxX: "", minY: "", maxY: "" }); // ✅ 좌표 전달용

  // Python 분석 실행 요청 함수
  const handleStartAnalysis = async () => {
    if (!selectedCctvId || !coords.minX) {
      alert("CCTV 위치를 먼저 선택해주세요!");
      return;
    }

    try {
      const res = await fetch(
        `http://localhost:8080/traffic/start?cctvId=${selectedCctvId}&minX=${coords.minX}&maxX=${coords.maxX}&minY=${coords.minY}&maxY=${coords.maxY}`,
        { method: "POST" }
      );

      const text = await res.text();
      console.log("분석 요청 결과:", text);
      alert("차량 분석이 시작되었습니다!");
    } catch (err) {
      console.error("분석 요청 실패:", err);
      alert("Python 실행 중 오류가 발생했습니다.");
    }
  };

  // CCTV 선택 시마다 해당 ID의 통계 새로 요청
  useEffect(() => {
    if (!selectedCctvId) return;
    fetchTrafficStats(selectedCctvId).then((data) => setStats(data));
  }, [selectedCctvId]);

  return (
    <div className="main">
      <div>
        {/* 좌표 입력 → CCTV URL 가져오기 */}
        <CoordinateForm
          setCctvUrl={setCctvUrl}
          setSelectedCctvId={setSelectedCctvId}
          setCoords={setCoords} //  좌표도 부모로 전달
        />

        {/* Python 분석 실행 버튼 */}
        <button onClick={handleStartAnalysis} style={{ marginTop: "10px" }}>
          분석 시작
        </button>

        {/* 통계 그래프 */}
        <TrafficChart stats={stats} />
      </div>

      {/* CCTV 영상 */}
      <VideoStream cctvUrl={cctvUrl} />
    </div>
  );
}

export default Dashboard;
