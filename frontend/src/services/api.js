import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080"  // Spring Boot 서버
});

// CCTV URL 요청
export const fetchCctvUrl = async ({ minX, maxX, minY, maxY }) => {
  const res = await api.get("/cctv/url", {
    params: { minX, maxX, minY, maxY }
  });
  return res.data.cctvUrl; // { "cctvUrl": "http://..." } 형태로 반환한다고 가정
};

// 혼잡도 통계 요청
export const fetchTrafficStats = async (cctvId) => {
  const res = await api.get("/traffic/stats", { params: { cctvId } });
  return res.data;
};
