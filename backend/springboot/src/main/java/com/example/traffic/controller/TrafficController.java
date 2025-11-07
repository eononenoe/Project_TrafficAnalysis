package com.example.traffic.controller;

import com.example.traffic.dto.ApiResponse;
import com.example.traffic.dto.AnalysisStatusDto;
import com.example.traffic.entity.TrafficEntity;
import com.example.traffic.repository.TrafficRepository;
import com.example.traffic.service.TrafficService;
import com.example.traffic.dto.TrafficStatsDto;
import com.example.traffic.dto.TrafficData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/traffic")
@CrossOrigin(origins = "http://localhost:3000")
public class TrafficController {

    private final TrafficService trafficService;
    private final TrafficRepository trafficRepository;

    // 분석 상태 추적 (메모리 기반, 실제 운영에서는 Redis 권장)
    private final Map<String, AnalysisStatusDto> analysisStatusMap = new ConcurrentHashMap<>();

    // Python 경로 설정
    private static final String PYTHON_PATH = "C:\\Users\\User\\AppData\\Local\\Programs\\Python\\Python310\\python.exe";
    private static final String SCRIPT_PATH = "C:\\Users\\User\\Desktop\\Project_TrafficAnalysis\\backend\\yolov7\\main_pipeline.py";

    /**
     * Python으로부터 차량 데이터 수신 및 저장
     */
    @PostMapping("/save")
    public ResponseEntity<ApiResponse> saveTraffic(@RequestBody TrafficData data) {
        try {
            log.info("차량 데이터 수신 - CCTV: {}, 시간: {}, 차량 수: {}",
                    data.getCctvId(), data.getTimestamp(), data.getVehicleCount());

            // DTO -> Entity 변환
            TrafficEntity entity = new TrafficEntity();
            entity.setCctvId(data.getCctvId());
            entity.setVehicleCount(data.getVehicleCount());
            entity.setTimestamp(data.getTimestamp() != null ? data.getTimestamp() : LocalDateTime.now());

            trafficRepository.save(entity);

            // 분석 상태 업데이트
            updateAnalysisStatus(data.getCctvId(), "running", "데이터 수신 중", data.getVehicleCount());

            return ResponseEntity.ok(ApiResponse.success("데이터 저장 완료", entity));
        } catch (Exception e) {
            log.error("차량 데이터 저장 실패", e);
            return ResponseEntity.status(500).body(
                    ApiResponse.error("데이터 저장 실패", e.getMessage())
            );
        }
    }

    /**
     * 특정 CCTV의 통계 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<List<TrafficStatsDto>> getTrafficStats(@RequestParam String cctvId) {
        try {
            log.info("통계 조회 요청: CCTV ID = {}", cctvId);
            List<TrafficStatsDto> stats = trafficService.getStats(cctvId);

            if (stats.isEmpty()) {
                log.warn("통계 데이터 없음: CCTV ID = {}", cctvId);
            }

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("통계 조회 실패: CCTV ID = {}", cctvId, e);
            return ResponseEntity.status(500).body(List.of());
        }
    }

    /**
     * 분석 시작 (Python 프로세스 실행)
     */
    @PostMapping("/start")
    public ResponseEntity<ApiResponse> startAnalysis(
            @RequestParam String cctvId,
            @RequestParam double minX,
            @RequestParam double maxX,
            @RequestParam double minY,
            @RequestParam double maxY
    ) {
        log.info("분석 시작 요청: CCTV ID = {}, 좌표 = [{}, {}] x [{}, {}]",
                cctvId, minX, maxX, minY, maxY);

        // 이미 실행 중인지 확인
        if (analysisStatusMap.containsKey(cctvId) &&
                "running".equals(analysisStatusMap.get(cctvId).getStatus())) {
            return ResponseEntity.status(409).body(
                    ApiResponse.error("이미 분석이 진행 중입니다", cctvId)
            );
        }

        try {
            // 초기 상태 설정
            updateAnalysisStatus(cctvId, "starting", "분석 시작 중", 0);

            // 실행 명령 구성
            String command = String.format(
                    "\"%s\" \"%s\" --cctvId %s --minX %f --maxX %f --minY %f --maxY %f",
                    PYTHON_PATH, SCRIPT_PATH, cctvId, minX, maxX, minY, maxY
            );

            log.info("Python 실행: {}", command);

            // 비동기로 Python 프로세스 실행
            new Thread(() -> executePythonAnalysis(cctvId, command)).start();

            return ResponseEntity.ok(ApiResponse.success(
                    "분석이 시작되었습니다",
                    Map.of("cctvId", cctvId, "status", "starting")
            ));

        } catch (Exception e) {
            log.error("분석 시작 실패: CCTV ID = {}", cctvId, e);
            updateAnalysisStatus(cctvId, "error", e.getMessage(), 0);
            return ResponseEntity.status(500).body(
                    ApiResponse.error("분석 시작 실패", e.getMessage())
            );
        }
    }

    /**
     * 분석 중지
     */
    @PostMapping("/stop")
    public ResponseEntity<ApiResponse> stopAnalysis(@RequestParam String cctvId) {
        log.info("분석 중지 요청: CCTV ID = {}", cctvId);

        AnalysisStatusDto status = analysisStatusMap.get(cctvId);
        if (status != null && "running".equals(status.getStatus())) {
            updateAnalysisStatus(cctvId, "stopped", "사용자가 중지함", 0);
            // 실제로는 프로세스를 종료해야 하지만, 여기서는 상태만 업데이트
            return ResponseEntity.ok(ApiResponse.success("분석이 중지되었습니다", cctvId));
        } else {
            return ResponseEntity.status(404).body(
                    ApiResponse.error("실행 중인 분석을 찾을 수 없습니다", cctvId)
            );
        }
    }

    /**
     * 분석 상태 조회
     */
    @GetMapping("/status")
    public ResponseEntity<AnalysisStatusDto> getAnalysisStatus(@RequestParam String cctvId) {
        AnalysisStatusDto status = analysisStatusMap.get(cctvId);

        if (status == null) {
            status = new AnalysisStatusDto(cctvId, "idle", "분석 대기 중", 0, LocalDateTime.now());
        }

        return ResponseEntity.ok(status);
    }

    /**
     * 실시간 혼잡도 조회 (최근 데이터)
     */
    @GetMapping("/current")
    public ResponseEntity<TrafficData> getCurrentTraffic(@RequestParam String cctvId) {
        try {
            TrafficEntity latest = trafficRepository.findTopByCctvIdOrderByTimestampDesc(cctvId);

            if (latest == null) {
                return ResponseEntity.status(404).body(null);
            }

            TrafficData data = new TrafficData();
            data.setCctvId(latest.getCctvId());
            data.setTimestamp(latest.getTimestamp());
            data.setVehicleCount(latest.getVehicleCount());

            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("현재 교통 상황 조회 실패: CCTV ID = {}", cctvId, e);
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * 시간대별 평균 차량 수
     */
    @GetMapping("/hourly-average")
    public ResponseEntity<List<Map<String, Object>>> getHourlyAverage(@RequestParam String cctvId) {
        try {
            List<Map<String, Object>> result = trafficService.getHourlyAverage(cctvId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("시간대별 평균 조회 실패: CCTV ID = {}", cctvId, e);
            return ResponseEntity.status(500).body(List.of());
        }
    }

    /**
     * Python 분석 실행 (비동기)
     */
    private void executePythonAnalysis(String cctvId, String command) {
        Process process = null;
        try {
            updateAnalysisStatus(cctvId, "running", "분석 실행 중", 0);

            process = Runtime.getRuntime().exec(command);
            final Process finalProcess = process;

            // 표준 출력 로그
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(finalProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.info("[Python STDOUT] {}", line);
                    }
                } catch (Exception e) {
                    log.error("Python 출력 읽기 실패", e);
                }
            }).start();

            // 에러 출력 로그
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(finalProcess.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.error("[Python STDERR] {}", line);
                    }
                } catch (Exception e) {
                    log.error("Python 에러 읽기 실패", e);
                }
            }).start();

            // 프로세스 완료 대기
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                updateAnalysisStatus(cctvId, "completed", "분석 완료", 0);
                log.info("분석 완료: CCTV ID = {}", cctvId);
            } else {
                updateAnalysisStatus(cctvId, "error", "Python 프로세스 오류 (exit code: " + exitCode + ")", 0);
                log.error("Python 프로세스 오류: exit code = {}", exitCode);
            }

        } catch (Exception e) {
            log.error("Python 실행 중 오류 발생: CCTV ID = {}", cctvId, e);
            updateAnalysisStatus(cctvId, "error", e.getMessage(), 0);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * 분석 상태 업데이트
     */
    private void updateAnalysisStatus(String cctvId, String status, String message, int vehicleCount) {
        AnalysisStatusDto dto = new AnalysisStatusDto(
                cctvId,
                status,
                message,
                vehicleCount,
                LocalDateTime.now()
        );
        analysisStatusMap.put(cctvId, dto);
    }
}