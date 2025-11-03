import React, { useState, useEffect } from "react";
import { fetchCctvUrl } from "../services/api";

function CoordinateForm({ setCctvUrl, setSelectedCctvId, setCoords }) {
  const [coords, setLocalCoords] = useState({
    minX: "",
    maxX: "",
    minY: "",
    maxY: ""
  });

  const [cctvList, setCctvList] = useState([]);
  const [selectedLine, setSelectedLine] = useState("");
  const [selectedLocation, setSelectedLocation] = useState("");

  // 노선 선택
  const handleLineSelect = (e) => {
    const line = e.target.value;
    setSelectedLine(line);
    setSelectedLocation(""); // 노선 바꾸면 위치 초기화
  };

  // 위치 선택 시 CCTV ID + 좌표 전달
  const handleLocationSelect = (e) => {
    const selectedId = e.target.value;
    const selected = cctvList.find((c) => String(c.id) === selectedId);
    if (selected) {
      // ✅ 로컬 좌표 저장
      const newCoords = {
        minX: selected.minX,
        maxX: selected.maxX,
        minY: selected.minY,
        maxY: selected.maxY
      };
      setLocalCoords(newCoords);

      // ✅ 부모로 CCTV ID + 좌표 전달
      setSelectedCctvId(selected.id);
      setCoords(newCoords);
    }
    setSelectedLocation(selected?.locationName || "");
  };

  // CCTV URL 요청
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!coords.minX) {
      alert("위치를 선택해주세요!");
      return;
    }
    try {
      const url = await fetchCctvUrl(coords);
      console.log("받은 CCTV URL:", url);
      setCctvUrl(url);
    } catch (err) {
      console.error("CCTV 요청 실패", err);
    }
  };

  // DB에서 CCTV 목록 불러오기
  useEffect(() => {
    const fetchCctvs = async () => {
      try {
        const res = await fetch("http://localhost:8080/cctv/list");
        const data = await res.json();
        if (data.length === 0) {
          console.log("CCTV 목록이 비어있음 → 자동 업데이트 실행");
          await fetch("http://localhost:8080/cctv/update?minX=124.0&maxX=132.0&minY=33.0&maxY=39.0");
          await new Promise((resolve) => setTimeout(resolve, 10000));
          const updated = await fetch("http://localhost:8080/cctv/list");
          const updatedData = await updated.json();
          setCctvList(updatedData);
          console.log("전국 CCTV 데이터 로드 완료:", updatedData.length);
        } else {
          setCctvList(data);
          console.log("CCTV 목록 불러오기 성공:", data.length);
        }
      } catch (err) {
        console.error("CCTV 목록 불러오기 실패:", err);
      }
    };

    fetchCctvs();
  }, []);

  // 중복 제거된 노선 목록
  const uniqueLines = [
    ...new Set(
      cctvList
        .map((c) => c.lineName?.trim())
        .filter((line) => line && line !== "")
    ),
  ].sort((a, b) => a.localeCompare(b, "ko"));

  return (
    <form onSubmit={handleSubmit} className="coordinate-form">
      <h2>실시간 CCTV</h2>

      {/* 노선 선택 */}
      <select onChange={handleLineSelect} value={selectedLine}>
        <option value="">노선 선택</option>
        {uniqueLines.map((line, idx) => (
          <option key={`${line}-${idx}`} value={line}>
            {line}
          </option>
        ))}
      </select>

      {/* 위치 선택 (value를 CCTV ID로 변경) */}
      <select
        onChange={handleLocationSelect}
        value={selectedLocation}
        disabled={!selectedLine}
      >
        <option value="">위치 선택</option>
        {cctvList
          .filter((c) => c.lineName?.trim() === selectedLine)
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

      <button type="submit">CCTV 조회</button>
    </form>
  );
}

export default CoordinateForm;
