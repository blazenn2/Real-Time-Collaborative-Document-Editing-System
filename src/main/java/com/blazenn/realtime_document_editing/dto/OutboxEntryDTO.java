package com.blazenn.realtime_document_editing.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;

public class OutboxEntryDTO {
    private Long id;
    @NotNull
    private Long documentId;
    private String operation;
    private Map<String, Object> payload;
    private Boolean isProcessed;
    private Instant createdDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public Boolean getProcessed() {
        return isProcessed;
    }

    public void setProcessed(Boolean processed) {
        isProcessed = processed;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }
}
