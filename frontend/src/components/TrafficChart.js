import React from "react";
import { Bar } from "react-chartjs-2";
import { Chart as ChartJS } from "chart.js/auto";

function TrafficChart() {
  // const data = {
  //   labels: ["월", "화", "수", "목", "금"],
  //   datasets: [
  //     { label: "평균 차량 수", data: [20, 35, 50, 40, 60] }
  //   ]
  // };

  return (
    <div>
      <h3>혼잡도 그래프</h3>
      {/* <Bar data={data} /> */}
    </div>
  );
}

export default TrafficChart;
