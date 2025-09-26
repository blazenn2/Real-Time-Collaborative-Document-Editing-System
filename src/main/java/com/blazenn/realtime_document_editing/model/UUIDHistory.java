package com.blazenn.realtime_document_editing.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Table(name="uuid_history")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class UUIDHistory {

    @Id
    @NotNull
    private UUID uuid;

    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @CreatedDate
    private Instant createdDate;

    public UUIDHistory(UUID uuid, Document document, Instant createdDate) {
        this.uuid = uuid;
        this.document = document;
        this.createdDate = createdDate;
    }

    public UUIDHistory() {

    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }
}
