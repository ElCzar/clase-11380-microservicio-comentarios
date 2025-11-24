package com.ms_example.comentarios.integration_test;

import com.ms_example.comentarios.model.Comment;
import com.ms_example.comentarios.repository.CommentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para CommentController
 */
class CommentControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CommentRepository commentRepository;

    @AfterEach
    void end() {
        commentRepository.deleteAll();
    }

    // Obtiene todos los comentarios
    @Test
    @WithMockUser(username = "testuser", roles = { "USER" })
    void testGetAllComments() {
        Comment comment1 = createTestComment(1L, 100L, new BigDecimal("4.5"), "Excelente servicio");
        Comment comment2 = createTestComment(1L, 101L, new BigDecimal("3.0"), "Servicio regular");
        commentRepository.save(comment1);
        commentRepository.save(comment2);

        List<Comment> result = webTestClient.get()
                .uri("/api/comments")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Comment.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2, commentRepository.count());
    }

    
    // Obtiene un comentario por ID
    @Test
    @WithMockUser(username = "testuser", roles = { "USER" })
    void testGetCommentById() {
        Comment comment = createTestComment(1L, 100L, new BigDecimal("5.0"), "Increíble servicio");
        Comment saved = commentRepository.save(comment);

        Comment result = webTestClient.get()
                .uri("/api/comments/" + saved.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Comment.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(result);
        assertEquals(saved.getId(), result.getId());
        assertEquals("Increíble servicio", result.getContent());
        assertEquals(new BigDecimal("5.0"), result.getRating());
    }

    // Obtiene comentarios por serviceId 
    @Test
    @WithMockUser(username = "testuser", roles = { "USER" })
    void testGetCommentByServiceId() {
        Long serviceId = 12345L;
        Comment comment1 = createTestComment(1L, serviceId, new BigDecimal("4.0"), "Buen servicio");
        Comment comment2 = createTestComment(2L, serviceId, new BigDecimal("4.5"), "Muy buen servicio");
        Comment comment3 = createTestComment(3L, 99999L, new BigDecimal("3.0"), "Otro servicio");

        commentRepository.save(comment1);
        commentRepository.save(comment2);
        commentRepository.save(comment3);

        List<Comment> result = webTestClient.get()
                .uri("/api/comments/service-id/" + serviceId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Comment.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(c -> c.getServiceId().equals(serviceId)));
    }

    
    // Obtiene comentarios por UUID de servicio
    @Test
    @WithMockUser(username = "testuser", roles = { "USER" })
    void testGetCommentByServiceUuid() {
        UUID serviceUuid = UUID.fromString("9eb1b573-1fa1-4a1e-afbb-ccbffe0ed114");
        Long serviceLongId = Math.abs((long) serviceUuid.hashCode());

        Comment comment1 = createTestComment(1L, serviceLongId, new BigDecimal("5.0"), "Excelente con UUID");
        Comment comment2 = createTestComment(2L, serviceLongId, new BigDecimal("4.8"), "Casi perfecto");

        commentRepository.save(comment1);
        commentRepository.save(comment2);

        List<Comment> result = webTestClient.get()
                .uri("/api/comments/service-uuid/" + serviceUuid.toString())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Comment.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    
    // Obtiene comentarios por profileId
    @Test
    @WithMockUser(username = "testuser", roles = { "USER" })
    void testGetCommentByProfileId() {
        Long profileId = 42L;
        Comment comment1 = createTestComment(profileId, 100L, new BigDecimal("4.0"), "Mi primer comentario");
        Comment comment2 = createTestComment(profileId, 101L, new BigDecimal("4.5"), "Mi segundo comentario");
        Comment comment3 = createTestComment(99L, 102L, new BigDecimal("3.0"), "Comentario de otro usuario");

        commentRepository.save(comment1);
        commentRepository.save(comment2);
        commentRepository.save(comment3);

        List<Comment> result = webTestClient.get()
                .uri("/api/comments/profile-id/" + profileId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Comment.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(c -> c.getProfileId().equals(profileId)));
    }

    
    // Crea un nuevo comentario
    @Test
    @WithMockUser(username = "testuser", roles = { "USER" })
    void testCreateComment() {
        Map<String, Object> commentData = new HashMap<>();
        commentData.put("serviceId", 12345L);
        commentData.put("profileId", 1L);
        commentData.put("rating", 4.5);
        commentData.put("content", "Este es un comentario de prueba creado vía POST");

        Comment result = webTestClient.post()
                .uri("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(commentData)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Comment.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Este es un comentario de prueba creado vía POST", result.getContent());
        assertEquals(new BigDecimal("4.5"), result.getRating());
        assertEquals(12345L, result.getServiceId());
        assertEquals(1L, result.getProfileId());
        assertEquals(1, commentRepository.count());
    }

    
    // Actualiza un comentario existente
    @Test
    @WithMockUser(username = "testuser", roles = { "USER" })
    void testUpdateComment() {
        Comment original = createTestComment(1L, 100L, new BigDecimal("3.0"), "Comentario original");
        Comment saved = commentRepository.save(original);

        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("serviceId", 100L);
        updatedData.put("profileId", 1L);
        updatedData.put("rating", 5.0);
        updatedData.put("content", "Comentario actualizado - ahora es excelente");

        Comment result = webTestClient.put()
                .uri("/api/comments/update/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedData)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Comment.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(result);
        assertEquals(saved.getId(), result.getId());
        assertEquals("Comentario actualizado - ahora es excelente", result.getContent());
        assertEquals(new BigDecimal("5.0"), result.getRating());

        Comment updated = commentRepository.findById(saved.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals("Comentario actualizado - ahora es excelente", updated.getContent());
    }

    
    // Elimina un comentario existente
    @Test
    @WithMockUser(username = "testuser", roles = { "USER" })
    void testDeleteComment() {
    
        Comment comment = createTestComment(1L, 100L, new BigDecimal("4.0"), "Comentario a eliminar");
        Comment saved = commentRepository.save(comment);
        Long commentId = saved.getId();

        assertTrue(commentRepository.existsById(commentId));

        Boolean result = webTestClient.delete()
                .uri("/api/comments/delete/" + commentId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(result);
        assertTrue(result);
        assertFalse(commentRepository.existsById(commentId));
        assertEquals(0, commentRepository.count());
    }

    private Comment createTestComment(Long profileId, Long serviceId, BigDecimal rating, String content) {
        Comment comment = new Comment();
        comment.setProfileId(profileId);
        comment.setServiceId(serviceId);
        comment.setRating(rating);
        comment.setContent(content);
        return comment;
    }
}
