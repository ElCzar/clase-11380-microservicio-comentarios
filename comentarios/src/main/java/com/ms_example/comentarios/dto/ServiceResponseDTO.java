package com.ms_example.comentarios.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO para recibir respuestas de servicios vía Kafka desde el microservicio
 * marketplace
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ServiceResponseDTO {

    // Campos para correlación de mensajes
    @JsonProperty("requestId")
    private String requestId;

    @JsonProperty("errorMessage")
    private String errorMessage;

    @JsonProperty("serviceId")
    private String serviceId;

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("averageRating")
    private Double averageRating;

    @JsonProperty("eventType")
    private String eventType;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("categoryId")
    private UUID categoryId;

    @JsonProperty("categoryName")
    private String categoryName;

    @JsonProperty("statusId")
    private UUID statusId;

    @JsonProperty("statusName")
    private String statusName;

    @JsonProperty("countryId")
    private UUID countryId;

    @JsonProperty("countryName")
    private String countryName;

    @JsonProperty("countryCode")
    private String countryCode;

    @JsonProperty("primaryImageUrl")
    private String primaryImageUrl;

    @JsonProperty("isActive")
    private Boolean isActive;

    public Boolean isAvailable() {
        return isActive != null && isActive;
    }

    public UUID getServiceId() {
        if (serviceId != null) {
            try {
                return UUID.fromString(serviceId);
            } catch (IllegalArgumentException e) {
                return id;
            }
        }
        return id;
    }

    public String getServiceIdAsString() {
        if (serviceId != null) {
            return serviceId;
        }
        return id != null ? id.toString() : null;
    }

    public String getName() {
        return title;
    }

    public Boolean getAvailable() {
        return isAvailable();
    }

    public String getPrimaryImageUrl() {
        return primaryImageUrl;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public boolean isValidForCart() {
        boolean hasValidId = (serviceId != null && !serviceId.trim().isEmpty()) || id != null;
        return hasValidId &&
                title != null && !title.trim().isEmpty() &&
                price != null &&
                price.compareTo(BigDecimal.ZERO) >= 0;
    }

    public String getSafeCategoryName() {
        return categoryName != null && !categoryName.trim().isEmpty()
                ? categoryName
                : "General";
    }

    public String getSafeDescription() {
        return description != null ? description : "Sin descripción disponible";
    }

    public Double getSafeAverageRating() {
        return averageRating != null ? averageRating : 0.0;
    }

    public String getSafePrimaryImageUrl() {
        return primaryImageUrl != null ? primaryImageUrl : "https://via.placeholder.com/300x200?text=Sin+Imagen";
    }

    public String getSafeUserId() {
        return userId != null ? userId : "unknown";
    }

    public String getSafeEventType() {
        return eventType != null ? eventType : "UNKNOWN";
    }

    public String getSafeTimestamp() {
        return timestamp != null ? timestamp : "";
    }

    public boolean isCreatedEvent() {
        return "CREATED".equalsIgnoreCase(eventType);
    }

    public boolean isUpdatedEvent() {
        return "UPDATED".equalsIgnoreCase(eventType);
    }

    public boolean isDeletedEvent() {
        return "DELETED".equalsIgnoreCase(eventType);
    }
}
