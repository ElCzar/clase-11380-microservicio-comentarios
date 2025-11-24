package com.ms_example.comentarios.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para enviar información de comentarios al microservicio de servicios a
 * través de Kafka
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long commentId;
    private String serviceUuid;
    private Long serviceIdHash;
    private Long profileId;
    private BigDecimal rating;
    private String content;
    private LocalDateTime createdAt;

}
