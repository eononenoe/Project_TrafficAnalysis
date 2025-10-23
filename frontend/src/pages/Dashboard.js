import React, { useState, useEffect } from "react";
import CoordinateForm from "../components/CoordinateForm";
import VideoStream from "../components/VideoStream";
import TrafficChart from "../components/TrafficChart";
import { fetchTrafficStats } from "../services/api";

function Dashboard() {
  const [cctvUrl, setCctvUrl] = useState("");
  const [stats, setStats] = useState([]);

  useEffect(() => {
    // 기본 CCTV ID=1 기준 통계 데이터
    fetchTrafficStats(1).then((data) => setStats(data));
  }, []);

  return (
    <div>
      <h2>교통 혼잡도 대시보드</h2>
      {/* 좌표 입력 → CCTV URL 가져오기 */}
      <CoordinateForm setCctvUrl={setCctvUrl} />

      {/* CCTV 영상 */}
      <VideoStream cctvUrl={cctvUrl} />

      {/* 통계 그래프 */}
      {/* <TrafficChart stats={stats} /> */}
    </div>
  );
}

export default Dashboard;
