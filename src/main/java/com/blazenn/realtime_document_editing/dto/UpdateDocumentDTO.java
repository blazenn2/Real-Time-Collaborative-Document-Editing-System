package com.blazenn.realtime_document_editing.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class UpdateDocumentDTO {
    @NotNull
    private Long id;
    @NotNull
    @NotEmpty
    private String title;
    @NotNull
    private List<DocumentTextDiffDTO> textDiff;
    @NotNull
    private Instant lastModifiedDate;
    @NotNull
    private UUID uuid;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<DocumentTextDiffDTO> getTextDiff() {
        return textDiff;
    }

    public void setTextDiff(List<DocumentTextDiffDTO> textDiff) {
        this.textDiff = textDiff;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
