package com.example.traffic.controller;

import com.example.traffic.dto.ApiResponse;
import com.example.traffic.entity.CctvInfo;
import com.example.traffic.repository.CctvRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/cctv")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class CctvController {

    private final CctvRepository cctvRepository;

    // Python 경로 설정 (환경변수나 application.properties로 관리 권장)
    private static final String PYTHON_EXE = "C:/Users/User/Desktop/Project_TrafficAnalysis/backend/yolov7/.venv/Scripts/python.exe";
    private static final String SCRIPT_PATH = "C:/Users/User/Desktop/Project_TrafficAnalysis/backend/yolov7/get_cctv_url.py";

    /**
     * Python 실행 → DB에 CCTV 저장
     */
    @GetMapping("/update")
    public ResponseEntity<ApiResponse> updateCctvData(
            @RequestParam double minX,
            @RequestParam double maxX,
            @RequestParam double minY,
            @RequestParam double maxY
    ) {
        log.info("CCTV 데이터 업데이트 요청: minX={}, maxX={}, minY={}, maxY={}", minX, maxX, minY, maxY);

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    PYTHON_EXE, SCRIPT_PATH,
                    "--minX", String.valueOf(minX),
                    "--maxX", String.valueOf(maxX),
                    "--minY", String.valueOf(minY),
                    "--maxY", String.valueOf(maxY)
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("[Python] {}", line);
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                long cctvCount = cctvRepository.count();
                log.info("CCTV 데이터 업데이트 완료. 총 {}개", cctvCount);
                return ResponseEntity.ok(ApiResponse.success(
                        "CCTV 데이터 갱신 완료",
                        Map.of("count", cctvCount, "output", output.toString())
                ));
            } else {
                log.error("Python 스크립트 실행 실패. Exit code: {}", exitCode);
                return ResponseEntity.status(500).body(
                        ApiResponse.error("CCTV 데이터 업데이트 실패", "Python 스크립트 오류")
                );
            }

        } catch (Exception e) {
            log.error("CCTV 데이터 업데이트 중 오류 발생", e);
            return ResponseEntity.status(500).body(
                    ApiResponse.error("서버 오류", e.getMessage())
            );
        }
    }

    /**
     * DB에 저장된 CCTV 목록 전체 조회
     */
    @GetMapping("/list")
    public ResponseEntity<List<CctvInfo>> getCctvList() {
        try {
            List<CctvInfo> cctvList = cctvRepository.findAll();
            log.info("CCTV 목록 조회 완료: {}개", cctvList.size());
            return ResponseEntity.ok(cctvList);
        } catch (Exception e) {
            log.error("CCTV 목록 조회 실패", e);
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }

    /**
     * 특정 ID로 CCTV 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCctvById(@PathVariable Long id) {
        try {
            Optional<CctvInfo> cctv = cctvRepository.findById(id);
            if (cctv.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("CCTV 조회 성공", cctv.get()));
            } else {
                return ResponseEntity.status(404).body(
                        ApiResponse.error("CCTV를 찾을 수 없습니다", "ID: " + id)
                );
            }
        } catch (Exception e) {
            log.error("CCTV 조회 실패. ID: {}", id, e);
            return ResponseEntity.status(500).body(
                    ApiResponse.error("서버 오류", e.getMessage())
            );
        }
    }

    /**
     * 특정 좌표 범위로 CCTV URL 직접 조회 (React → Python → CCTV URL)
     */
    @GetMapping("/url")
    public ResponseEntity<Map<String, String>> getCctvUrl(
            @RequestParam double minX,
            @RequestParam double maxX,
            @RequestParam double minY,
            @RequestParam double maxY
    ) {
        log.info("CCTV URL 요청: minX={}, maxX={}, minY={}, maxY={}", minX, maxX, minY, maxY);

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    PYTHON_EXE, SCRIPT_PATH,
                    "--minX", String.valueOf(minX),
                    "--maxX", String.valueOf(maxX),
                    "--minY", String.valueOf(minY),
                    "--maxY", String.valueOf(maxY)
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String cctvUrl = null;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("[Python] {}", line);
                    if (line.startsWith("http")) {
                        cctvUrl = line.trim();
                        break; // 첫 번째 URL만 사용
                    }
                }
            }

            int exitCode = process.waitFor();

            if (cctvUrl != null && exitCode == 0) {
                log.info("CCTV URL 조회 성공: {}", cctvUrl);
                return ResponseEntity.ok(Collections.singletonMap("cctvUrl", cctvUrl));
            } else {
                log.warn("CCTV URL을 찾을 수 없음. Exit code: {}", exitCode);
                return ResponseEntity.status(404).body(
                        Collections.singletonMap("error", "CCTV URL not found")
                );
            }

        } catch (Exception e) {
            log.error("CCTV URL 조회 중 오류 발생", e);
            return ResponseEntity.status(500).body(
                    Collections.singletonMap("error", e.getMessage())
            );
        }
    }

    /**
     * 노선별 CCTV 목록 조회
     */
    @GetMapping("/by-line")
    public ResponseEntity<List<CctvInfo>> getCctvsByLine(@RequestParam String lineName) {
        try {
            List<CctvInfo> cctvList = cctvRepository.findByLineName(lineName);
            log.info("노선별 CCTV 조회 완료: {} - {}개", lineName, cctvList.size());
            return ResponseEntity.ok(cctvList);
        } catch (Exception e) {
            log.error("노선별 CCTV 조회 실패. lineName: {}", lineName, e);
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }

    /**
     * 위치명으로 CCTV 검색
     */
    @GetMapping("/search")
    public ResponseEntity<List<CctvInfo>> searchCctvs(@RequestParam String keyword) {
        try {
            List<CctvInfo> cctvList = cctvRepository.findByLocationNameContaining(keyword);
            log.info("CCTV 검색 완료: '{}' - {}개", keyword, cctvList.size());
            return ResponseEntity.ok(cctvList);
        } catch (Exception e) {
            log.error("CCTV 검색 실패. keyword: {}", keyword, e);
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }

    /**
     * 통계: 노선별 CCTV 개수
     */
    @GetMapping("/stats/by-line")
    public ResponseEntity<Map<String, Long>> getCctvStatsByLine() {
        try {
            List<Object[]> stats = cctvRepository.countByLineName();
            Map<String, Long> result = new HashMap<>();
            for (Object[] row : stats) {
                String lineName = (String) row[0];
                Long count = ((Number) row[1]).longValue();
                result.put(lineName, count);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("노선별 통계 조회 실패", e);
            return ResponseEntity.status(500).body(Collections.emptyMap());
        }
    }
}