package com.blazenn.realtime_document_editing.dto;

import com.blazenn.realtime_document_editing.constants.DocumentTextEventTypeConstants;
import jakarta.validation.constraints.*;

public class DocumentTextDiffDTO {
    @NotNull
    @NotEmpty
    private DocumentTextEventTypeConstants type;
    @NotNull
    @NotEmpty
    private String text;
    @NotNull
    @NotEmpty
    private long index;
    private long length;

    public DocumentTextEventTypeConstants getType() {
        return type;
    }

    public void setType(DocumentTextEventTypeConstants type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }
}
