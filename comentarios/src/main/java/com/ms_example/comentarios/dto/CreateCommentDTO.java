package com.ms_example.comentarios.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO para crear comentarios sobre servicios recibidos desde Kafka
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentDTO {

    @NotNull(message = "El ID del servicio es requerido")
    private UUID serviceId;

    @NotNull(message = "El ID del perfil es requerido")
    private Long profileId;

    @NotNull(message = "El rating es requerido")
    @DecimalMin(value = "0.0", message = "El rating debe ser mayor o igual a 0")
    @DecimalMax(value = "5.0", message = "El rating debe ser menor o igual a 5")
    private BigDecimal rating;

    @NotBlank(message = "El contenido del comentario es requerido")
    @Size(min = 10, max = 1000, message = "El comentario debe tener entre 10 y 1000 caracteres")
    private String content;
}
