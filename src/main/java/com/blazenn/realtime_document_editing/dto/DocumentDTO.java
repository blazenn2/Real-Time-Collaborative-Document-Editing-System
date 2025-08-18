package com.blazenn.realtime_document_editing.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@Data
@ToString
@RedisHash(value = "document", timeToLive = 60)
public class DocumentDTO implements Serializable {
    @NotNull
    @Positive
    private Long id;
    @NotNull
    @Size(min = 1, max = 255)
    @NotBlank
    private String title;
    private String content;

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
