package com.lms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket event wrapper for broadcasting request updates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketEvent {
    
    private String type;
    private Object payload;
    private Long timestamp;

    /**
     * Factory method to create a WebSocket event.
     */
    public static WebSocketEvent of(String type, Object payload) {
        return WebSocketEvent.builder()
                .type(type)
                .payload(payload)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}