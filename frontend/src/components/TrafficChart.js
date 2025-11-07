import React, { useEffect, useRef, useState } from "react";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
  Filler,
} from "chart.js";
import { Line, Bar } from "react-chartjs-2";

// Chart.js 등록
ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

function TrafficGraph({ stats, selectedCctvId }) {
  const [chartType, setChartType] = useState("line"); // "line" | "bar"
  const [timeRange, setTimeRange] = useState("24h"); // "1h" | "24h" | "7d"

  if (!stats || stats.length === 0) {
    return (
      <div className="traffic-graph empty">
        <p>데이터가 없습니다. 분석을 시작해주세요.</p>
      </div>
    );
  }

  // 시간 범위에 따라 데이터 필터링
  const getFilteredData = () => {
    const now = new Date();
    let cutoffTime;

    switch (timeRange) {
      case "1h":
        cutoffTime = new Date(now - 60 * 60 * 1000);
        break;
      case "24h":
        cutoffTime = new Date(now - 24 * 60 * 60 * 1000);
        break;
      case "7d":
        cutoffTime = new Date(now - 7 * 24 * 60 * 60 * 1000);
        break;
      default:
        cutoffTime = new Date(0);
    }

    return stats.filter((stat) => new Date(stat.timestamp) >= cutoffTime);
  };

  const filteredStats = getFilteredData();

  // 차트 데이터 준비
  const labels = filteredStats.map((stat) => {
    const date = new Date(stat.timestamp);
    if (timeRange === "1h") {
      return date.toLocaleTimeString("ko-KR", { hour: "2-digit", minute: "2-digit" });
    } else if (timeRange === "24h") {
      return date.toLocaleTimeString("ko-KR", { hour: "2-digit" });
    } else {
      return date.toLocaleDateString("ko-KR", { month: "short", day: "numeric" });
    }
  });

  const vehicleCounts = filteredStats.map((stat) => stat.avgCount);

  // 혼잡도에 따른 색상
  const backgroundColors = filteredStats.map((stat) => {
    if (stat.congestion === "HIGH") return "rgba(255, 99, 132, 0.2)";
    if (stat.congestion === "MID" || stat.congestion === "MEDIUM") return "rgba(255, 206, 86, 0.2)";
    return "rgba(75, 192, 192, 0.2)";
  });

  const borderColors = filteredStats.map((stat) => {
    if (stat.congestion === "HIGH") return "rgba(255, 99, 132, 1)";
    if (stat.congestion === "MID" || stat.congestion === "MEDIUM") return "rgba(255, 206, 86, 1)";
    return "rgba(75, 192, 192, 1)";
  });

  const chartData = {
    labels,
    datasets: [
      {
        label: "차량 수",
        data: vehicleCounts,
        fill: chartType === "line",
        backgroundColor: chartType === "line" ? "rgba(54, 162, 235, 0.2)" : backgroundColors,
        borderColor: chartType === "line" ? "rgba(54, 162, 235, 1)" : borderColors,
        borderWidth: 2,
        tension: 0.4,
        pointRadius: chartType === "line" ? 3 : 0,
        pointHoverRadius: 5,
      },
    ],
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true,
        position: "top",
      },
      title: {
        display: true,
        text: `차량 수 변화 (${timeRange === "1h" ? "최근 1시간" : timeRange === "24h" ? "최근 24시간" : "최근 7일"})`,
        font: { size: 16 },
      },
      tooltip: {
        callbacks: {
          label: (context) => {
            const stat = filteredStats[context.dataIndex];
            return [
              `차량 수: ${context.parsed.y}대`,
              `혼잡도: ${stat.congestion === "HIGH" ? "혼잡" : stat.congestion === "MID" || stat.congestion === "MEDIUM" ? "보통" : "여유"}`,
            ];
          },
        },
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        title: {
          display: true,
          text: "차량 수 (대)",
        },
      },
      x: {
        title: {
          display: true,
          text: "시간",
        },
      },
    },
  };

  // 통계 요약
  const avgCount = Math.round(vehicleCounts.reduce((a, b) => a + b, 0) / vehicleCounts.length);
  const maxCount = Math.max(...vehicleCounts);
  const minCount = Math.min(...vehicleCounts);

  return (
    <div className="traffic-graph">
      <div className="graph-controls">
        <div className="control-group">
          <label>차트 타입</label>
          <div className="btn-group">
            <button
              className={chartType === "line" ? "active" : ""}
              onClick={() => setChartType("line")}
            >
              📈 선 그래프
            </button>
            <button
              className={chartType === "bar" ? "active" : ""}
              onClick={() => setChartType("bar")}
            >
              📊 막대 그래프
            </button>
          </div>
        </div>

        <div className="control-group">
          <label>기간</label>
          <div className="btn-group">
            <button
              className={timeRange === "1h" ? "active" : ""}
              onClick={() => setTimeRange("1h")}
            >
              1시간
            </button>
            <button
              className={timeRange === "24h" ? "active" : ""}
              onClick={() => setTimeRange("24h")}
            >
              24시간
            </button>
            <button
              className={timeRange === "7d" ? "active" : ""}
              onClick={() => setTimeRange("7d")}
            >
              7일
            </button>
          </div>
        </div>
      </div>

      {/* 통계 요약 */}
      <div className="stats-summary">
        <div className="stat-card">
          <span className="stat-label">평균</span>
          <span className="stat-value">{avgCount}대</span>
        </div>
        <div className="stat-card">
          <span className="stat-label">최대</span>
          <span className="stat-value high">{maxCount}대</span>
        </div>
        <div className="stat-card">
          <span className="stat-label">최소</span>
          <span className="stat-value low">{minCount}대</span>
        </div>
      </div>

      {/* 차트 */}
      <div className="chart-container" style={{ height: "400px" }}>
        {chartType === "line" ? (
          <Line data={chartData} options={options} />
        ) : (
          <Bar data={chartData} options={options} />
        )}
      </div>
    </div>
  );
}

export default TrafficGraph;