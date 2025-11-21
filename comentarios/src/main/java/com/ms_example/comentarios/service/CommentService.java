package com.ms_example.comentarios.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ms_example.comentarios.dto.CreateCommentDTO;
import com.ms_example.comentarios.dto.ServiceResponseDTO;
import com.ms_example.comentarios.model.Comment;
import com.ms_example.comentarios.repository.CommentRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    public Comment getCommentsById(Long id) {
        return commentRepository.findById(id).orElse(null);
    }

    public List<Comment> getCommentByServiceId(Long serviceId) {
        return commentRepository.findByServiceId(serviceId);
    }

    public List<Comment> getCommentByProfileId(Long profileId) {
        return commentRepository.findByProfileId(profileId);
    }

    public Comment createComment(Comment comment) {
        return commentRepository.save(comment);
    }

    public Comment updateComment(Long id, Comment updatedComment) {
        if (commentRepository.existsById(id)) {
            updatedComment.setId(id);
            commentRepository.save(updatedComment);
            return updatedComment;
        }
        return null;
    }

    public boolean deleteComment(Long id) {
        if (commentRepository.existsById(id)) {
            commentRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Crea un comentario para un servicio recibido desde Kafka
     * Valida que el servicio exista en la cola antes de crear el comentario
     * 
     * @param commentDTO DTO con los datos del comentario
     * @return El comentario creado
     * @throws IllegalArgumentException si el servicio no existe en la cola
     */
    public Comment createCommentForKafkaService(CreateCommentDTO commentDTO) {
        UUID serviceUUID = commentDTO.getServiceId();

        // Validar que el servicio existe en la cola de Kafka
        ServiceResponseDTO service = ServiceKafkaConsumer.getServiceById(serviceUUID);
        if (service == null) {
            log.error("Intento de crear comentario para servicio inexistente: {}", serviceUUID);
            throw new IllegalArgumentException(
                    "El servicio con ID " + serviceUUID + " no existe en la cola de servicios");
        }

        // Log para debugging del estado del servicio
        log.info("Servicio encontrado - ID: {}, Name: {}, isActive: {}, isAvailable: {}",
                service.getServiceId(), service.getName(), service.getIsActive(), service.isAvailable());

        // Validar que el servicio esté activo (solo si isActive es explícitamente
        // false)
        if (service.getIsActive() != null && !service.getIsActive()) {
            log.error("Intento de crear comentario para servicio inactivo: {}", serviceUUID);
            throw new IllegalArgumentException("El servicio con ID " + serviceUUID + " no está activo");
        }

        log.info("Creando comentario para servicio: {} - {}", service.getServiceId(), service.getName());

        // Convertir UUID a Long (usando hashCode como estrategia simple)
        // Nota: En producción considera una mejor estrategia de mapeo
        Long serviceLongId = Math.abs((long) serviceUUID.hashCode());

        // Crear el comentario
        Comment comment = new Comment();
        comment.setServiceId(serviceLongId);
        comment.setProfileId(commentDTO.getProfileId());
        comment.setRating(commentDTO.getRating());
        comment.setContent(commentDTO.getContent());

        Comment savedComment = commentRepository.save(comment);
        log.info("Comentario creado exitosamente con ID: {} para servicio: {}",
                savedComment.getId(), service.getName());

        return savedComment;
    }

}
