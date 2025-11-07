package com.example.traffic.service;

import com.example.traffic.dto.TrafficStatsDto;
import com.example.traffic.entity.TrafficEntity;
import com.example.traffic.repository.TrafficRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrafficService {

    private final TrafficRepository trafficRepository;

    // 혼잡도 임계값 설정
    private static final int HIGH_THRESHOLD = 50;
    private static final int MID_THRESHOLD = 20;

    /**
     * 요일/시간대별 통계 조회
     */
    @Transactional(readOnly = true)
    public List<TrafficStatsDto> getStats(String cctvId) {
        log.info("통계 조회 시작: CCTV ID = {}", cctvId);

        List<Object[]> results = trafficRepository.findAvgCountByCctv(cctvId);
        List<TrafficStatsDto> dtoList = new ArrayList<>();

        for (Object[] row : results) {
            String weekday = (String) row[0];
            String hour = (String) row[1];
            Double avgCount = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;

            String congestion = calculateCongestion(avgCount);
            dtoList.add(new TrafficStatsDto(weekday, hour, avgCount, congestion));
        }

        log.info("통계 조회 완료: CCTV ID = {}, 결과 {}건", cctvId, dtoList.size());
        return dtoList;
    }

    /**
     * 시간대별 평균 조회 (최근 24시간)
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getHourlyAverage(String cctvId) {
        log.info("시간대별 평균 조회: CCTV ID = {}", cctvId);

        List<Object[]> results = trafficRepository.findHourlyAverageLast24Hours(cctvId);

        return results.stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("hour", row[0]);
                    map.put("avgCount", row[1] != null ? ((Number) row[1]).doubleValue() : 0.0);
                    map.put("latestTime", row[2]);
                    map.put("congestion", calculateCongestion(
                            row[1] != null ? ((Number) row[1]).doubleValue() : 0.0
                    ));
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * 실시간 혼잡도 조회
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCurrentCongestion(String cctvId) {
        TrafficEntity latest = trafficRepository.findTopByCctvIdOrderByTimestampDesc(cctvId);

        if (latest == null) {
            log.warn("데이터 없음: CCTV ID = {}", cctvId);
            return Map.of(
                    "status", "NO_DATA",
                    "message", "데이터가 없습니다"
            );
        }

        String congestion = calculateCongestion((double) latest.getVehicleCount());

        return Map.of(
                "cctvId", cctvId,
                "vehicleCount", latest.getVehicleCount(),
                "congestion", congestion,
                "timestamp", latest.getTimestamp(),
                "status", "OK"
        );
    }

    /**
     * 오늘의 통계 요약
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getTodaySummary(String cctvId) {
        Object[] stats = trafficRepository.findTodayStats(cctvId);

        if (stats == null || stats[0] == null) {
            return Map.of("status", "NO_DATA");
        }

        int maxCount = stats[0] != null ? ((Number) stats[0]).intValue() : 0;
        int minCount = stats[1] != null ? ((Number) stats[1]).intValue() : 0;
        double avgCount = stats[2] != null ? ((Number) stats[2]).doubleValue() : 0.0;

        return Map.of(
                "maxCount", maxCount,
                "minCount", minCount,
                "avgCount", avgCount,
                "congestion", calculateCongestion(avgCount),
                "date", LocalDateTime.now().toLocalDate()
        );
    }

    /**
     * 추천 시간대 조회 (혼잡도 낮은 시간)
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRecommendedHours(String cctvId) {
        List<Map<String, Object>> hourlyData = getHourlyAverage(cctvId);

        return hourlyData.stream()
                .filter(data -> "LOW".equals(data.get("congestion")))
                .sorted(Comparator.comparingDouble(
                        data -> ((Number) data.get("avgCount")).doubleValue()
                ))
                .limit(3)
                .collect(Collectors.toList());
    }

    /**
     * 혼잡 시간대 조회
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCongestionHours(String cctvId) {
        List<Map<String, Object>> hourlyData = getHourlyAverage(cctvId);

        return hourlyData.stream()
                .filter(data -> "HIGH".equals(data.get("congestion")))
                .sorted(Comparator.comparingDouble(
                        data -> -((Number) data.get("avgCount")).doubleValue()
                ))
                .limit(3)
                .collect(Collectors.toList());
    }

    /**
     * 혼잡도 비율 통계
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCongestionDistribution(String cctvId) {
        List<Object[]> results = trafficRepository.countByCongestionLevel(cctvId);

        Map<String, Long> distribution = new HashMap<>();
        long total = 0;

        for (Object[] row : results) {
            String level = (String) row[0];
            long count = ((Number) row[1]).longValue();
            distribution.put(level, count);
            total += count;
        }

        // 비율 계산
        Map<String, Object> response = new HashMap<>();
        response.put("counts", distribution);
        response.put("total", total);

        if (total > 0) {
            Map<String, Double> percentages = new HashMap<>();
            for (Map.Entry<String, Long> entry : distribution.entrySet()) {
                percentages.put(entry.getKey(),
                        (entry.getValue() * 100.0) / total);
            }
            response.put("percentages", percentages);
        }

        return response;
    }

    /**
     * 특정 기간의 데이터 조회
     */
    @Transactional(readOnly = true)
    public List<TrafficEntity> getDataByTimeRange(
            String cctvId,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        return trafficRepository.findByCctvIdAndTimeRange(cctvId, startTime, endTime);
    }

    /**
     * 오래된 데이터 정리 (배치 작업용)
     */
    @Transactional
    public void cleanupOldData(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        log.info("오래된 데이터 정리 시작: {} 이전 데이터", cutoffDate);

        trafficRepository.deleteOldData(cutoffDate);

        log.info("데이터 정리 완료");
    }

    /**
     * 혼잡도 계산 헬퍼 메서드
     */
    private String calculateCongestion(Double avgCount) {
        if (avgCount == null || avgCount < 0) {
            return "UNKNOWN";
        }

        if (avgCount > HIGH_THRESHOLD) {
            return "HIGH";
        } else if (avgCount > MID_THRESHOLD) {
            return "MID";
        } else {
            return "LOW";
        }
    }

    /**
     * 혼잡도 예측 (간단한 이동 평균 기반)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> predictCongestion(String cctvId, int hoursAhead) {
        // 현재 시간 + 예측 시간
        LocalDateTime now = LocalDateTime.now();
        int targetHour = (now.getHour() + hoursAhead) % 24;

        // 과거 데이터 기반 평균
        Double avgCount = trafficRepository.findAvgCountByHour(cctvId, targetHour);

        if (avgCount == null) {
            return Map.of(
                    "status", "NO_DATA",
                    "message", "예측에 필요한 데이터가 부족합니다"
            );
        }

        String predictedCongestion = calculateCongestion(avgCount);

        return Map.of(
                "targetHour", targetHour,
                "predictedCount", avgCount,
                "predictedCongestion", predictedCongestion,
                "confidence", calculateConfidence(avgCount),
                "timestamp", LocalDateTime.now()
        );
    }

    /**
     * 예측 신뢰도 계산 (간단한 버전)
     */
    private double calculateConfidence(Double avgCount) {
        // 데이터가 많을수록 신뢰도 높음 (실제로는 분산 등을 고려해야 함)
        if (avgCount > 100) return 0.9;
        if (avgCount > 50) return 0.8;
        if (avgCount > 20) return 0.7;
        return 0.6;
    }
}