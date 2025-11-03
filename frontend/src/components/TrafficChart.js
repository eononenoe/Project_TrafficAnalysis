import React, { useEffect, useState } from "react";

function TrafficChart({ selectedCctvId }) {
  const [status, setStatus] = useState(null);

  useEffect(() => {
    if (!selectedCctvId) return;

    const fetchStats = async () => {
      try {
        const id = typeof selectedCctvId === "object" ? selectedCctvId.id : selectedCctvId;
        const res = await fetch(`http://localhost:8080/traffic/stats?cctvId=${id}`);
        const data = await res.json();

        if (data && data.length > 0) {
          const latest = data[data.length - 1];
          setStatus({
            vehicleCount: latest.avgCount,
            congestionLevel: latest.congestion,
            updatedAt: new Date().toISOString(),
          });
        } else {
          setStatus(null);
        }
      } catch (err) {
        console.error("혼잡도 정보 불러오기 실패:", err);
      }
    };

    fetchStats();
    const interval = setInterval(fetchStats, 5000);
    return () => clearInterval(interval);
  }, [selectedCctvId]);

  if (!status) return <div className="traffic-status">데이터 불러오는 중...</div>;

  const getColor = (level) => {
    switch (level) {
      case "HIGH": return "red";
      case "MID":
      case "MEDIUM": return "orange";
      case "LOW": return "green";
      default: return "gray";
    }
  };

  return (
    <div className="traffic-status" style={{ borderLeft: `5px solid ${getColor(status.congestionLevel)}` }}>
      <h3>실시간 혼잡도</h3>
      <p>차량 수: <strong>{status.vehicleCount}</strong>대</p>
      <p>혼잡도: <strong style={{ color: getColor(status.congestionLevel) }}>
        {status.congestionLevel === "HIGH" ? "혼잡" :
         status.congestionLevel === "MID" || status.congestionLevel === "MEDIUM" ? "보통" : "여유"}
      </strong></p>
      <p>갱신 시각: {new Date(status.updatedAt).toLocaleTimeString()}</p>
    </div>
  );
}

export default TrafficChart;
