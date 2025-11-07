import React, { useState, useEffect, useCallback } from "react";
import CoordinateForm from "../components/CoordinateForm";
import VideoStream from "../components/VideoStream";
import TrafficChart from "../components/TrafficChart";
import TrafficGraph from "../components/TrafficGraph.js";
import Heatmap from "../components/Heatmap";
import { fetchTrafficStats } from "../services/api";
import '../css/Dashboard.css';

function Dashboard() {
  const [cctvUrl, setCctvUrl] = useState("");
  const [stats, setStats] = useState([]);
  const [selectedCctvId, setSelectedCctvId] = useState(null);
  const [coords, setCoords] = useState({ minX: "", maxX: "", minY: "", maxY: "" });
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [autoRefresh, setAutoRefresh] = useState(true);
  const [viewMode, setViewMode] = useState("list"); // "list" | "map"
  const [analysisStatus, setAnalysisStatus] = useState(null); // "ready" | "running" | "completed" | "error"

  //  통계 자동 업데이트 (5초마다)
  useEffect(() => {
    if (!selectedCctvId || !autoRefresh) return;

    const fetchStats = async () => {
      try {
        const data = await fetchTrafficStats(selectedCctvId);
        setStats(data);
      } catch (err) {
        console.error("통계 업데이트 실패:", err);
      }
    };

    fetchStats(); // 즉시 실행
    const interval = setInterval(fetchStats, 5000);
    return () => clearInterval(interval);
  }, [selectedCctvId, autoRefresh]);

  //  분석 상태 확인 (폴링)
  useEffect(() => {
    if (!isAnalyzing || !selectedCctvId) return;

    const checkStatus = async () => {
      try {
        const res = await fetch(`http://localhost:8080/traffic/status?cctvId=${selectedCctvId}`);
        const data = await res.json();
        
        if (data.status === "completed") {
          setIsAnalyzing(false);
          setAnalysisStatus("completed");
          // 통계 새로고침
          const newStats = await fetchTrafficStats(selectedCctvId);
          setStats(newStats);
        } else if (data.status === "error") {
          setIsAnalyzing(false);
          setAnalysisStatus("error");
        }
      } catch (err) {
        console.error("상태 확인 실패:", err);
      }
    };

    const interval = setInterval(checkStatus, 3000);
    return () => clearInterval(interval);
  }, [isAnalyzing, selectedCctvId]);

  //  원클릭 분석 시작 (CCTV 선택 → URL 가져오기 → 분석 시작)
  const handleStartAnalysis = useCallback(async () => {
    if (!selectedCctvId || !coords.minX) {
      alert("CCTV 위치를 먼저 선택해주세요!");
      return;
    }

    setIsAnalyzing(true);
    setAnalysisStatus("running");

    try {
      const res = await fetch(
        `http://localhost:8080/traffic/start?cctvId=${selectedCctvId}&minX=${coords.minX}&maxX=${coords.maxX}&minY=${coords.minY}&maxY=${coords.maxY}`,
        { method: "POST" }
      );

      if (res.ok) {
        console.log("분석 시작 성공");
      } else {
        throw new Error("분석 시작 실패");
      }
    } catch (err) {
      console.error("분석 요청 실패:", err);
      setIsAnalyzing(false);
      setAnalysisStatus("error");
      alert("분석 시작 중 오류가 발생했습니다.");
    }
  }, [selectedCctvId, coords]);

  //  CCTV 선택 시 자동으로 URL 가져오고 분석 시작
  const handleCctvSelect = useCallback(async (cctvId, cctvCoords, url) => {
    setSelectedCctvId(cctvId);
    setCoords(cctvCoords);
    setCctvUrl(url);
    setAnalysisStatus("ready");
  }, []);

  // 분석 상태에 따른 메시지
  const getStatusMessage = () => {
    switch (analysisStatus) {
      case "ready":
        return { text: "분석 준비 완료", color: "blue" };
      case "running":
        return { text: "차량 분석 중...", color: "orange" };
      case "completed":
        return { text: "분석 완료", color: "green" };
      case "error":
        return { text: "분석 실패", color: "red" };
      default:
        return { text: "CCTV를 선택하세요", color: "gray" };
    }
  };

  const statusMsg = getStatusMessage();

  return (
    <div className="dashboard-container">
      {/* 헤더 */}
      <header className="dashboard-header">
        <h1>🚗 실시간 교통 혼잡도 분석</h1>
        <div className="header-controls">
          <button
            className={`view-toggle ${viewMode === "list" ? "active" : ""}`}
            onClick={() => setViewMode("list")}
          >
            📋 목록
          </button>
          <button
            className={`view-toggle ${viewMode === "map" ? "active" : ""}`}
            onClick={() => setViewMode("map")}
          >
            🗺️ 지도
          </button>
          <label className="auto-refresh-toggle">
            <input
              type="checkbox"
              checked={autoRefresh}
              onChange={(e) => setAutoRefresh(e.target.checked)}
            />
            자동 새로고침
          </label>
        </div>
      </header>

      {/* 상태 표시 바 */}
      {selectedCctvId && (
        <div className="status-bar" style={{ borderLeft: `5px solid ${statusMsg.color}` }}>
          <span className="status-icon">
            {isAnalyzing ? "⏳" : analysisStatus === "completed" ? "✅" : "📍"}
          </span>
          <span className="status-text">{statusMsg.text}</span>
          {isAnalyzing && <div className="loading-spinner" />}
        </div>
      )}

      <div className="dashboard-content">
        {/* 좌측: CCTV 선택 + 통계 */}
        <aside className="sidebar">
          {viewMode === "list" ? (
            <CoordinateForm
              setCctvUrl={setCctvUrl}
              setSelectedCctvId={setSelectedCctvId}
              setCoords={setCoords}
              onCctvSelect={handleCctvSelect}
            />
          ) : (
            <Heatmap
              onCctvSelect={handleCctvSelect}
              selectedCctvId={selectedCctvId}
            />
          )}

          {/* 분석 제어 버튼 */}
          {selectedCctvId && (
            <div className="analysis-controls">
              <button
                className="btn-primary"
                onClick={handleStartAnalysis}
                disabled={isAnalyzing}
              >
                {isAnalyzing ? "분석 중..." : "🚀 분석 시작"}
              </button>
              {isAnalyzing && (
                <button
                  className="btn-secondary"
                  onClick={() => {
                    setIsAnalyzing(false);
                    setAnalysisStatus(null);
                  }}
                >
                  ⏸️ 중지
                </button>
              )}
            </div>
          )}

          {/* 실시간 혼잡도 */}
          <TrafficChart selectedCctvId={selectedCctvId} />
        </aside>

        {/* 중앙: CCTV 영상 */}
        <main className="main-content">
          <VideoStream cctvUrl={cctvUrl} isAnalyzing={isAnalyzing} />
          
          {/* 통계 그래프 */}
          {stats.length > 0 && (
            <TrafficGraph stats={stats} selectedCctvId={selectedCctvId} />
          )}
        </main>

        {/* 우측: 추천 정보 (선택) */}
        {stats.length > 0 && (
          <aside className="recommendations">
            <h3>📊 추천 정보</h3>
            <RecommendationPanel stats={stats} />
          </aside>
        )}
      </div>
    </div>
  );
}

// 🎯 추천 정보 패널
function RecommendationPanel({ stats }) {
  if (!stats || stats.length === 0) return null;

  // 시간대별 평균 계산
  const hourlyAvg = stats.reduce((acc, stat) => {
    const hour = new Date(stat.timestamp).getHours();
    if (!acc[hour]) acc[hour] = { total: 0, count: 0 };
    acc[hour].total += stat.avgCount;
    acc[hour].count += 1;
    return acc;
  }, {});

  // 가장 여유로운 시간대 찾기
  const bestHours = Object.entries(hourlyAvg)
    .map(([hour, data]) => ({
      hour: parseInt(hour),
      avg: data.total / data.count,
    }))
    .sort((a, b) => a.avg - b.avg)
    .slice(0, 3);

  return (
    <div className="recommendation-card">
      <h4>🕐 추천 시간대</h4>
      <ul>
        {bestHours.map((item) => (
          <li key={item.hour}>
            <strong>{item.hour}시</strong>: 평균 {Math.round(item.avg)}대
            <span className="badge-low">여유</span>
          </li>
        ))}
      </ul>

      <h4>⚠️ 혼잡 시간대</h4>
      <ul>
        {Object.entries(hourlyAvg)
          .map(([hour, data]) => ({
            hour: parseInt(hour),
            avg: data.total / data.count,
          }))
          .sort((a, b) => b.avg - a.avg)
          .slice(0, 3)
          .map((item) => (
            <li key={item.hour}>
              <strong>{item.hour}시</strong>: 평균 {Math.round(item.avg)}대
              <span className="badge-high">혼잡</span>
            </li>
          ))}
      </ul>
    </div>
  );
}

export default Dashboard;