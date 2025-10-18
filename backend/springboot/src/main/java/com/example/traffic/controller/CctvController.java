package com.example.traffic.controller;

import com.example.traffic.entity.CctvInfo;
import com.example.traffic.repository.CctvRepository;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;


@RestController
@RequestMapping("/cctv")
@CrossOrigin(origins = "http://localhost:3000")
public class CctvController {

    private final CctvRepository cctvRepository;

    public CctvController(CctvRepository cctvRepository) {
        this.cctvRepository = cctvRepository;
    }

    // Python 실행 → DB에 CCTV 저장
    @GetMapping("/update")
    public Map<String, String> updateCctvData(
            @RequestParam double minX,
            @RequestParam double maxX,
            @RequestParam double minY,
            @RequestParam double maxY
    ) {
        try {
            String pythonExe = "C:/Users/User/Desktop/Project_TrafficAnalysis/backend/yolov7/.venv/Scripts/python.exe";
            String scriptPath = "C:/Users/User/Desktop/Project_TrafficAnalysis/backend/yolov7/get_cctv_url.py";

            ProcessBuilder pb = new ProcessBuilder(
                    pythonExe, scriptPath,
                    "--minX", String.valueOf(minX),
                    "--maxX", String.valueOf(maxX),
                    "--minY", String.valueOf(minY),
                    "--maxY", String.valueOf(maxY)
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(),StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[Python] " + line);
            }
            process.waitFor();

            return Collections.singletonMap("message", "CCTV 데이터 갱신 완료 (DB 저장)");
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.singletonMap("error", e.getMessage());
        }
    }

    // DB에 저장된 CCTV 목록 전체 조회
    @GetMapping("/list")
    public List<CctvInfo> getCctvList() {
        return cctvRepository.findAll();
    }

    // 특정 좌표 범위로 CCTV URL 직접 조회 (React → Python → CCTV URL)
    @GetMapping("/url")
    public Map<String, String> getCctvUrl(
            @RequestParam double minX,
            @RequestParam double maxX,
            @RequestParam double minY,
            @RequestParam double maxY
    ) {
        try {
            String pythonExe = "C:/Users/User/Desktop/Project_TrafficAnalysis/backend/yolov7/.venv/Scripts/python.exe";
            String scriptPath = "C:/Users/User/Desktop/Project_TrafficAnalysis/backend/yolov7/get_cctv_url.py";

            ProcessBuilder pb = new ProcessBuilder(
                    pythonExe, scriptPath,
                    "--minX", String.valueOf(minX),
                    "--maxX", String.valueOf(maxX),
                    "--minY", String.valueOf(minY),
                    "--maxY", String.valueOf(maxY)
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
            );
            String line;
            String cctvUrl = null;

            // Python에서 URL 추출
            while ((line = reader.readLine()) != null) {
                System.out.println("[Python] " + line);
                if (line.startsWith("http")) {
                    cctvUrl = line.trim();
                }
            }
            process.waitFor();

            if (cctvUrl != null) {
                return Collections.singletonMap("cctvUrl", cctvUrl);
            } else {
                return Collections.singletonMap("error", "CCTV URL not found");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.singletonMap("error", e.getMessage());
        }
    }
}
