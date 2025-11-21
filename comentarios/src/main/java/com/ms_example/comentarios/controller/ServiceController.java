package com.ms_example.comentarios.controller;

import com.ms_example.comentarios.dto.CreateCommentDTO;
import com.ms_example.comentarios.dto.ServiceResponseDTO;
import com.ms_example.comentarios.model.Comment;
import com.ms_example.comentarios.service.CommentService;
import com.ms_example.comentarios.service.ServiceKafkaConsumer;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para acceder a los servicios recibidos desde Kafka
 */
@RestController
@RequestMapping("/api/services")
@Slf4j
public class ServiceController {

    private final CommentService commentService;

    public ServiceController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Obtiene todos los servicios disponibles recibidos desde Kafka
     * 
     * @return Lista de todos los servicios
     */
    @GetMapping
    public ResponseEntity<List<ServiceResponseDTO>> getAllServices() {
        log.info("GET /api/services - Obteniendo todos los servicios");
        List<ServiceResponseDTO> services = ServiceKafkaConsumer.getAllAvailableServices();
        log.info("Se encontraron {} servicios", services.size());
        return ResponseEntity.ok(services);
    }

    /**
     * Obtiene un servicio específico por su ID
     * 
     * @param serviceId ID del servicio a buscar
     * @return El servicio si existe, 404 si no se encuentra
     */
    @GetMapping("/{serviceId}")
    public ResponseEntity<ServiceResponseDTO> getServiceById(@PathVariable UUID serviceId) {
        log.info("GET /api/services/{} - Buscando servicio", serviceId);
        ServiceResponseDTO service = ServiceKafkaConsumer.getServiceById(serviceId);

        if (service != null) {
            log.info("Servicio {} encontrado", serviceId);
            return ResponseEntity.ok(service);
        } else {
            log.warn("Servicio {} no encontrado", serviceId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Verifica si un servicio está disponible
     * 
     * @param serviceId ID del servicio a verificar
     * @return true si el servicio existe, false en caso contrario
     */
    @GetMapping("/{serviceId}/available")
    public ResponseEntity<Boolean> isServiceAvailable(@PathVariable UUID serviceId) {
        log.info("GET /api/services/{}/available - Verificando disponibilidad", serviceId);
        boolean available = ServiceKafkaConsumer.isServiceAvailable(serviceId);
        return ResponseEntity.ok(available);
    }

    /**
     * Obtiene el conteo total de servicios disponibles
     * 
     * @return Número de servicios en la lista
     */
    @GetMapping("/count")
    public ResponseEntity<Integer> getServicesCount() {
        log.info("GET /api/services/count - Obteniendo conteo de servicios");
        int count = ServiceKafkaConsumer.getAvailableServicesCount();
        log.info("Total de servicios: {}", count);
        return ResponseEntity.ok(count);
    }

    /**
     * Limpia la lista de servicios (útil para testing)
     * 
     * @return Confirmación de limpieza
     */
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearServices() {
        log.warn("DELETE /api/services/clear - Limpiando lista de servicios");
        ServiceKafkaConsumer.clearServicesList();
        return ResponseEntity.ok("Lista de servicios limpiada exitosamente");
    }

    /**
     * Crea un comentario para un servicio de la cola de Kafka
     * Valida que el servicio exista antes de crear el comentario
     * 
     * @param commentDTO Datos del comentario a crear
     * @return El comentario creado con status 201
     */
    @PostMapping("/{serviceId}/comments")
    public ResponseEntity<?> createCommentForService(
            @PathVariable UUID serviceId,
            @Valid @RequestBody CreateCommentDTO commentDTO) {

        log.info("POST /api/services/{}/comments - Creando comentario para servicio", serviceId);

        // Validar que el serviceId del path coincida con el del body
        if (!serviceId.equals(commentDTO.getServiceId())) {
            log.warn("ServiceId del path ({}) no coincide con el del body ({})",
                    serviceId, commentDTO.getServiceId());
            return ResponseEntity.badRequest()
                    .body("El ID del servicio en la URL no coincide con el del cuerpo de la petición");
        }

        try {
            Comment createdComment = commentService.createCommentForKafkaService(commentDTO);
            log.info("Comentario creado exitosamente: {}", createdComment.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);

        } catch (IllegalArgumentException e) {
            log.error("Error validando servicio: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            log.error("Error creando comentario: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear el comentario: " + e.getMessage());
        }
    }
}
