package com.blazenn.realtime_document_editing.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.util.Map;

@Entity
@Data
public class OutboxEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "outbox_entry_sequence_generator")
    @SequenceGenerator(name = "outbox_entry_sequence_generator", sequenceName = "outbox_entry_seq", allocationSize = 1)
    private Long id;

    @NotNull
    private Long documentId;

    private String operation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> payload;

    private Boolean isProcessed;

    @CreatedDate
    private Instant createdDate=Instant.now();

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
