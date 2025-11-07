// ========== DTO 클래스들 ==========

// ApiResponse.java (공통 응답 포맷)
package com.example.traffic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    private boolean success;
    private String message;
    private Object data;
    private String error;
    private LocalDateTime timestamp;

    public static ApiResponse success(String message, Object data) {
        return new ApiResponse(true, message, data, null, LocalDateTime.now());
    }

    public static ApiResponse error(String message, String error) {
        return new ApiResponse(false, message, null, error, LocalDateTime.now());
    }
}