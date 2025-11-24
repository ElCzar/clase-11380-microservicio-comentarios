package com.ms_example.comentarios.integration_test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms_example.comentarios.dto.ServiceResponseDTO;
import com.ms_example.comentarios.model.Comment;
import com.ms_example.comentarios.repository.CommentRepository;
import com.ms_example.comentarios.service.ServiceKafkaConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


class ServiceControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommentRepository commentRepository;

    private static final String SERVICE_RESPONSE_TOPIC = "service-response-topic-test";

    @BeforeEach
    @Override
    void init() {
        super.init();
        ServiceKafkaConsumer.clearServicesList();
    }

    @AfterEach
    void end() {
        ServiceKafkaConsumer.clearServicesList();
        commentRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "testuser", roles = { "USER" })
    void testGetAllServices() throws Exception {
        ServiceResponseDTO service1 = createTestService(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "Servicio de Prueba 1",
                new BigDecimal("100.00"));
        ServiceResponseDTO service2 = createTestService(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "Servicio de Prueba 2",
                new BigDecimal("200.00"));

        publishServiceToKafka(service1);
        publishServiceToKafka(service2);
        waitForKafkaProcessing();

        List<ServiceResponseDTO> result = webTestClient.get()
                .uri("/api/services")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ServiceResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(result);
        assertTrue(result.size() >= 2);
        assertEquals(2, ServiceKafkaConsumer.getAvailableServicesCount());
    }

    @Test
    @WithMockUser(username = "testuser", roles = { "USER" })
    void testGetServiceById() throws Exception {
        // Obtener un servicio específico por UUID
        UUID serviceId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        ServiceResponseDTO service = createTestService(
                serviceId,
                "Servicio Específico",
                new BigDecimal("150.50"));

        publishServiceToKafka(service);
        waitForKafkaProcessing();

        ServiceResponseDTO result = webTestClient.get()
                .uri("/api/services/" + serviceId.toString())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ServiceResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(result);
        assertEquals("Servicio Específico", result.getTitle());
        assertEquals(new BigDecimal("150.50"), result.getPrice());
    }

    @Test
    @WithMockUser(username = "testuser", roles = { "USER" })
    void testGetServiceById_NotFound() {
        UUID nonExistentId = UUID.fromString("99999999-9999-9999-9999-999999999999");

        webTestClient.get()
                .uri("/api/services/" + nonExistentId.toString())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @WithMockUser(username = "testuser", roles = { "USER" })
    void testIsServiceAvailable() throws Exception {
        UUID serviceId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        ServiceResponseDTO service = createTestService(serviceId, "Servicio Disponible", new BigDecimal("99.99"));
        service.setIsActive(true);

        publishServiceToKafka(service);
        waitForKafkaProcessing();

        Boolean result = webTestClient.get()
                .uri("/api/services/" + serviceId.toString() + "/available")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(result);
        assertTrue(result);
        assertTrue(ServiceKafkaConsumer.isServiceAvailable(serviceId));
    }

    @Test
    @WithMockUser(username = "testuser", roles = { "USER" })
    void testGetServicesCount() throws Exception {
        for (int i = 1; i <= 5; i++) {
            ServiceResponseDTO service = createTestService(
                    UUID.randomUUID(),
                    "Servicio " + i,
                    new BigDecimal("50.00"));
            publishServiceToKafka(service);
        }

        waitForKafkaProcessing();

        Integer result = webTestClient.get()
                .uri("/api/services/count")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Integer.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(result);
        assertEquals(5, result);
        assertEquals(5, ServiceKafkaConsumer.getAvailableServicesCount());
    }

    @Test
    @WithMockUser(username = "testuser", roles = { "ADMIN" })
    void testClearServices() throws Exception {
        ServiceResponseDTO service = createTestService(
                UUID.randomUUID(),
                "Servicio a Limpiar",
                new BigDecimal("75.00"));
        publishServiceToKafka(service);
        waitForKafkaProcessing();

        assertTrue(ServiceKafkaConsumer.getAvailableServicesCount() > 0);

        String result = webTestClient.delete()
                .uri("/api/services/clear")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(result);
        assertEquals("Lista de servicios limpiada exitosamente", result);
        assertEquals(0, ServiceKafkaConsumer.getAvailableServicesCount());
    }

    @Test
    @WithMockUser(username = "testuser", roles = { "USER" })
    void testCreateCommentForService() throws Exception {
        UUID serviceId = UUID.fromString("55555555-5555-5555-5555-555555555555");
        ServiceResponseDTO service = createTestService(
                serviceId,
                "Servicio Para Comentar",
                new BigDecimal("120.00"));
        service.setIsActive(true);

        publishServiceToKafka(service);
        waitForKafkaProcessing();

        assertTrue(ServiceKafkaConsumer.isServiceAvailable(serviceId));

        Map<String, Object> commentData = new HashMap<>();
        commentData.put("serviceId", serviceId.toString());
        commentData.put("profileId", 1L);
        commentData.put("rating", 4.8);
        commentData.put("content", "Excelente servicio recibido desde Kafka, muy recomendado para todos los usuarios");

        Comment result = webTestClient.post()
                .uri("/api/services/" + serviceId.toString() + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(commentData)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Comment.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(result);
        assertNotNull(result.getId());
        assertTrue(result.getContent().contains("Excelente servicio"));
        assertEquals(new BigDecimal("4.8"), result.getRating());
        assertEquals(1L, result.getProfileId());
        assertEquals(1, commentRepository.count());
    }

    private ServiceResponseDTO createTestService(UUID id, String title, BigDecimal price) {
        ServiceResponseDTO service = new ServiceResponseDTO();
        service.setId(id);
        service.setServiceId(id.toString());
        service.setTitle(title);
        service.setDescription("Descripción de " + title);
        service.setPrice(price);
        service.setAverageRating(4.5);
        service.setIsActive(true);
        service.setCategoryName("Test Category");
        service.setUserId("test-user-id");
        service.setEventType("CREATED");
        return service;
    }

    private void publishServiceToKafka(ServiceResponseDTO service) throws Exception {
        String jsonPayload = objectMapper.writeValueAsString(service);
        String encodedPayload = Base64.getEncoder().encodeToString(jsonPayload.getBytes());
        kafkaTemplate.send(SERVICE_RESPONSE_TOPIC, encodedPayload);
        System.out.println("Servicio publicado en Kafka: " + service.getTitle());
    }
}
