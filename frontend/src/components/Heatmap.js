import React, { useState, useEffect } from "react";
import { MapContainer, TileLayer, Marker, Popup, useMap } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import "leaflet.heat";

// 마커 아이콘 설정 (Leaflet 기본 아이콘 문제 해결)
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: require("leaflet/dist/images/marker-icon-2x.png"),
  iconUrl: require("leaflet/dist/images/marker-icon.png"),
  shadowUrl: require("leaflet/dist/images/marker-shadow.png"),
});

// 혼잡도에 따른 색상 마커 생성
const getMarkerIcon = (congestion) => {
  const color = congestion === "HIGH" ? "red" : congestion === "MID" || congestion === "MEDIUM" ? "orange" : "green";
  return L.divIcon({
    className: "custom-marker",
    html: `<div style="background-color: ${color}; width: 20px; height: 20px; border-radius: 50%; border: 2px solid white;"></div>`,
    iconSize: [20, 20],
  });
};

// Heatmap 레이어 컴포넌트
function HeatmapLayer({ cctvData }) {
  const map = useMap();

  useEffect(() => {
    if (!cctvData || cctvData.length === 0) return;

    // Heatmap 데이터 준비 (위도, 경도, 혼잡도 강도)
    const heatData = cctvData.map((cctv) => {
      const intensity = cctv.congestion === "HIGH" ? 1 : cctv.congestion === "MID" || cctv.congestion === "MEDIUM" ? 0.5 : 0.2;
      return [cctv.lat, cctv.lng, intensity];
    });

    // Heatmap 레이어 생성
    const heat = L.heatLayer(heatData, {
      radius: 25,
      blur: 15,
      maxZoom: 17,
      gradient: {
        0.2: "green",
        0.5: "yellow",
        0.8: "orange",
        1.0: "red",
      },
    }).addTo(map);

    return () => {
      map.removeLayer(heat);
    };
  }, [map, cctvData]);

  return null;
}

function Heatmap({ onCctvSelect, selectedCctvId }) {
  const [cctvData, setCctvData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [viewMode, setViewMode] = useState("markers"); // "markers" | "heatmap"

  useEffect(() => {
    const fetchCctvs = async () => {
      try {
        setLoading(true);
        const res = await fetch("http://localhost:8080/cctv/list");
        const data = await res.json();

        // 위치 정보가 있는 CCTV만 필터링
        const validCctvs = data
          .filter((c) => c.lat && c.lng)
          .map((c) => ({
            ...c,
            lat: parseFloat(c.lat),
            lng: parseFloat(c.lng),
          }));

        // 혼잡도 정보 가져오기 (각 CCTV별)
        const cctvWithStats = await Promise.all(
          validCctvs.map(async (cctv) => {
            try {
              const statsRes = await fetch(`http://localhost:8080/traffic/stats?cctvId=${cctv.id}`);
              const stats = await statsRes.json();
              const latestStat = stats.length > 0 ? stats[stats.length - 1] : null;
              return {
                ...cctv,
                congestion: latestStat ? latestStat.congestion : "LOW",
                vehicleCount: latestStat ? latestStat.avgCount : 0,
              };
            } catch {
              return { ...cctv, congestion: "LOW", vehicleCount: 0 };
            }
          })
        );

        setCctvData(cctvWithStats);
      } catch (err) {
        console.error("CCTV 데이터 로드 실패:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchCctvs();
    const interval = setInterval(fetchCctvs, 30000); // 30초마다 갱신
    return () => clearInterval(interval);
  }, []);

  const handleMarkerClick = async (cctv) => {
    try {
      const coords = {
        minX: cctv.minX,
        maxX: cctv.maxX,
        minY: cctv.minY,
        maxY: cctv.maxY,
      };

      const urlRes = await fetch(
        `http://localhost:8080/cctv/url?minX=${coords.minX}&maxX=${coords.maxX}&minY=${coords.minY}&maxY=${coords.maxY}`
      );
      const url = await urlRes.text();

      onCctvSelect(cctv.id, coords, url);
    } catch (err) {
      console.error("CCTV 선택 실패:", err);
    }
  };

  if (loading) {
    return <div className="heatmap-container loading">지도 로딩 중...</div>;
  }

  if (cctvData.length === 0) {
    return <div className="heatmap-container empty">위치 정보가 있는 CCTV가 없습니다.</div>;
  }

  // 지도 중심 (한국)
  const center = [37.5665, 126.9780]; // 서울
  const zoom = 11;

  return (
    <div className="heatmap-container">
      <div className="heatmap-controls">
        <h2>🗺️ 지도에서 선택</h2>
        <div className="view-toggle">
          <button
            className={viewMode === "markers" ? "active" : ""}
            onClick={() => setViewMode("markers")}
          >
            📍 마커
          </button>
          <button
            className={viewMode === "heatmap" ? "active" : ""}
            onClick={() => setViewMode("heatmap")}
          >
            🔥 히트맵
          </button>
        </div>
      </div>

      <div className="legend">
        <h4>혼잡도</h4>
        <div className="legend-item">
          <span className="legend-color" style={{ backgroundColor: "green" }}></span>
          여유
        </div>
        <div className="legend-item">
          <span className="legend-color" style={{ backgroundColor: "orange" }}></span>
          보통
        </div>
        <div className="legend-item">
          <span className="legend-color" style={{ backgroundColor: "red" }}></span>
          혼잡
        </div>
      </div>

      <MapContainer
        center={center}
        zoom={zoom}
        style={{ height: "500px", width: "100%" }}
        scrollWheelZoom={true}
      >
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        />

        {viewMode === "heatmap" && <HeatmapLayer cctvData={cctvData} />}

        {viewMode === "markers" &&
          cctvData.map((cctv) => (
            <Marker
              key={cctv.id}
              position={[cctv.lat, cctv.lng]}
              icon={getMarkerIcon(cctv.congestion)}
              eventHandlers={{
                click: () => handleMarkerClick(cctv),
              }}
            >
              <Popup>
                <div className="marker-popup">
                  <h4>{cctv.locationName}</h4>
                  <p>
                    <strong>노선:</strong> {cctv.lineName}
                  </p>
                  <p>
                    <strong>차량 수:</strong> {cctv.vehicleCount}대
                  </p>
                  <p>
                    <strong>혼잡도:</strong>{" "}
                    <span
                      style={{
                        color:
                          cctv.congestion === "HIGH"
                            ? "red"
                            : cctv.congestion === "MID" || cctv.congestion === "MEDIUM"
                            ? "orange"
                            : "green",
                      }}
                    >
                      {cctv.congestion === "HIGH" ? "혼잡" : cctv.congestion === "MID" || cctv.congestion === "MEDIUM" ? "보통" : "여유"}
                    </span>
                  </p>
                  <button onClick={() => handleMarkerClick(cctv)}>선택</button>
                </div>
              </Popup>
            </Marker>
          ))}
      </MapContainer>
    </div>
  );
}

export default Heatmap;