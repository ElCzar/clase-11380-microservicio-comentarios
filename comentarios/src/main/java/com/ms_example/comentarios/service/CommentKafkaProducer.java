package com.ms_example.comentarios.service;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import com.ms_example.comentarios.dto.CommentResponseDTO;
import com.ms_example.comentarios.model.Comment;

import lombok.extern.slf4j.Slf4j;

/**
 * Servicio productor de Kafka para enviar comentarios al microservicio de
 * servicios
 * Utiliza el tópico comments-response para publicar los comentarios
 * creados/actualizados
 */
@Service
@Slf4j
public class CommentKafkaProducer {

    private final StreamBridge streamBridge;

    public CommentKafkaProducer(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    /**
     * Publica un comentario al tópico de Kafka usando StreamBridge
     * 
     * @param comment El comentario a publicar
     */
    public void publishComment(Comment comment) {
        try {
            CommentResponseDTO dto = CommentResponseDTO.builder()
                    .commentId(comment.getId())
                    .serviceUuid(comment.getServiceUuid())
                    .serviceIdHash(comment.getServiceIdHash())
                    .profileId(comment.getProfileId())
                    .rating(comment.getRating())
                    .content(comment.getContent())
                    .createdAt(comment.getCreatedAt())
                    .build();

            log.info("Enviando comentario a Kafka - ID: {}, ServiceUUID: {}, ServiceIdHash: {}",
                    comment.getId(), comment.getServiceUuid(), comment.getServiceIdHash());

            // Enviar al binding commentResponse-out-0 configurado en application.yml
            boolean sent = streamBridge.send("commentResponse-out-0", dto);

            if (sent) {
                log.info("Comentario publicado exitosamente al tópico comments-response");
            } else {
                log.error("Fallo al publicar comentario al tópico comments-response");
            }

        } catch (Exception e) {
            log.error("Error inesperado al publicar comentario", e);
        }
    }

}
