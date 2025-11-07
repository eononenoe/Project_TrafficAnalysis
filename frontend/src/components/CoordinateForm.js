import React, { useState, useEffect, useCallback } from "react";
import { fetchCctvUrl } from "../services/api";

function CoordinateForm({ onCctvSelect }) {
  const [coords, setLocalCoords] = useState({
    minX: "",
    maxX: "",
    minY: "",
    maxY: ""
  });

  const [cctvList, setCctvList] = useState([]);
  const [filteredCctvs, setFilteredCctvs] = useState([]);
  const [selectedLine, setSelectedLine] = useState("");
  const [selectedLocation, setSelectedLocation] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // 🔥 CCTV 목록 불러오기
  useEffect(() => {
    const fetchCctvs = async () => {
      try {
        setLoading(true);
        const res = await fetch("http://localhost:8080/cctv/list");
        const data = await res.json();

        if (data.length === 0) {
          console.log("CCTV 목록 없음 → 자동 업데이트");
          await fetch("http://localhost:8080/cctv/update?minX=124.0&maxX=132.0&minY=33.0&maxY=39.0");
          await new Promise((resolve) => setTimeout(resolve, 10000));

          const updated = await fetch("http://localhost:8080/cctv/list");
          const updatedData = await updated.json();
          setCctvList(updatedData);
        } else {
          setCctvList(data);
        }
      } catch (err) {
        console.error("CCTV 목록 로드 실패:", err);
        setError("CCTV 목록을 불러오는데 실패했습니다.");
      } finally {
        setLoading(false);
      }
    };

    fetchCctvs();
  }, []);

  // 🔥 노선 또는 검색어 변경 시 필터링
  useEffect(() => {
    let filtered = cctvList;

    // 노선 필터
    if (selectedLine) {
      filtered = filtered.filter((c) => c.lineName?.trim() === selectedLine);
    }

    // 검색어 필터
    if (searchQuery) {
      filtered = filtered.filter((c) =>
        c.locationName?.toLowerCase().includes(searchQuery.toLowerCase())
      );
    }

    setFilteredCctvs(filtered);
  }, [cctvList, selectedLine, searchQuery]);

  // 🔥 노선 선택
  const handleLineSelect = useCallback((e) => {
    const line = e.target.value;
    setSelectedLine(line);
    setSelectedLocation("");
    setSearchQuery("");
  }, []);

  // 🔥 위치 선택 + 자동으로 CCTV URL 가져오기
  const handleLocationSelect = useCallback(async (e) => {
    const selectedId = e.target.value;
    const selected = cctvList.find((c) => String(c.id) === selectedId);

    if (selected) {
      const newCoords = {
        minX: selected.minX,
        maxX: selected.maxX,
        minY: selected.minY,
        maxY: selected.maxY
      };

      console.log("✅ 선택된 CCTV:", selected);
      console.log("📍 newCoords:", newCoords);
      console.log("📊 실제 전달될 값:", {
        minX: typeof newCoords.minX,
        maxX: typeof newCoords.maxX,
        minY: typeof newCoords.minY,
        maxY: typeof newCoords.maxY,
        values: newCoords
      });

      setLocalCoords(newCoords);
      setSelectedLocation(selected.locationName);

      try {
        // CCTV URL 자동으로 가져오기
        const url = await fetchCctvUrl(newCoords);

        // 부모 컴포넌트로 전달
        onCctvSelect(selected.id, newCoords, url);
      } catch (err) {
        console.error("CCTV URL 가져오기 실패:", err);
        alert("CCTV 연결에 실패했습니다.");
      }
    }
  }, [cctvList, onCctvSelect]);

  // 🔥 검색 필터
  const handleSearch = useCallback((e) => {
    setSearchQuery(e.target.value);
    setSelectedLine(""); // 검색 시 노선 필터 해제
  }, []);

  // 중복 제거된 노선 목록
  const uniqueLines = [
    ...new Set(
      cctvList
        .map((c) => c.lineName?.trim())
        .filter((line) => line && line !== "")
    ),
  ].sort((a, b) => a.localeCompare(b, "ko"));

  if (loading) {
    return (
      <div className="coordinate-form loading">
        <div className="spinner" />
        <p>CCTV 목록 로딩 중...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="coordinate-form error">
        <p>❌ {error}</p>
        <button onClick={() => window.location.reload()}>다시 시도</button>
      </div>
    );
  }

  return (
    <div className="coordinate-form">
      <h2>🎥 CCTV 선택</h2>

      {/* 검색창 */}
      <div className="search-box">
        <input
          type="text"
          placeholder="🔍 위치명으로 검색..."
          value={searchQuery}
          onChange={handleSearch}
          className="search-input"
        />
      </div>

      {/* 노선 선택 */}
      <div className="form-group">
        <label>노선</label>
        <select
          onChange={handleLineSelect}
          value={selectedLine}
          className="form-select"
        >
          <option value="">전체 노선</option>
          {uniqueLines.map((line, idx) => (
            <option key={`${line}-${idx}`} value={line}>
              {line}
            </option>
          ))}
        </select>
      </div>

      {/* 위치 선택 */}
      <div className="form-group">
        <label>위치</label>
        <select
          onChange={handleLocationSelect}
          value={selectedLocation}
          className="form-select"
        >
          <option value="">위치 선택</option>
          {filteredCctvs
            .sort((a, b) => a.locationName.localeCompare(b.locationName, "ko"))
            .filter(
              (c, index, self) =>
                index === self.findIndex((t) => t.locationName === c.locationName)
            )
            .map((c) => (
              <option key={c.id} value={c.id}>
                {c.locationName}
              </option>
            ))}
        </select>
      </div>

      {/* CCTV 개수 표시 */}
      <div className="cctv-count">
        총 {filteredCctvs.length}개 CCTV
      </div>

      {/* 빠른 선택 버튼 (인기 구간) */}
      <div className="quick-select">
        <h4>⚡ 빠른 선택</h4>
        <div className="quick-buttons">
          <button
            className="quick-btn"
            onClick={() => {
              const gangnam = cctvList.find(c => c.locationName?.includes("강남"));
              if (gangnam) handleLocationSelect({ target: { value: String(gangnam.id) } });
            }}
          >
            강남역
          </button>
          <button
            className="quick-btn"
            onClick={() => {
              const seoul = cctvList.find(c => c.locationName?.includes("서울역"));
              if (seoul) handleLocationSelect({ target: { value: String(seoul.id) } });
            }}
          >
            서울역
          </button>
          <button
            className="quick-btn"
            onClick={() => {
              const jamsil = cctvList.find(c => c.locationName?.includes("잠실"));
              if (jamsil) handleLocationSelect({ target: { value: String(jamsil.id) } });
            }}
          >
            잠실
          </button>
        </div>
      </div>
    </div>
  );
}

export default CoordinateForm;