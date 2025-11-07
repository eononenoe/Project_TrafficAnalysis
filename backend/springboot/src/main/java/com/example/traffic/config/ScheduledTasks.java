
// ========== 스케줄러 설정 (선택) ==========
package com.example.traffic.config;

import com.example.traffic.service.TrafficService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final TrafficService trafficService;

    /**
     * 매일 새벽 3시에 30일 이상 된 데이터 정리
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupOldTrafficData() {
        log.info("오래된 교통 데이터 정리 작업 시작");
        try {
            trafficService.cleanupOldData(30); // 30일 이상 데이터 삭제
            log.info("데이터 정리 작업 완료");
        } catch (Exception e) {
            log.error("데이터 정리 작업 실패", e);
        }
    }

    /**
     * 매시간 정각에 CCTV 상태 확인 (선택)
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void checkCctvStatus() {
        log.info("CCTV 상태 확인 작업 시작");
        // CCTV 연결 상태 확인 로직
    }
}

// ========== application.properties 설정 예시 ==========
/*
# Server Port
server.port=8080

# Database Configuration (Oracle)
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:xe
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.Oracle12cDialect

# Logging
logging.level.com.example.traffic=INFO
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# Python Path Configuration
python.exe.path=C:/Users/User/Desktop/Project_TrafficAnalysis/backend/yolov7/.venv/Scripts/python.exe
python.script.path=C:/Users/User/Desktop/Project_TrafficAnalysis/backend/yolov7

# Analysis Configuration
analysis.high.threshold=50
analysis.mid.threshold=20
analysis.data.retention.days=30

# CORS Configuration
cors.allowed.origins=http://localhost:3000,http://localhost:3001
*/