package com.blazenn.realtime_document_editing.service.mapper;

import com.blazenn.realtime_document_editing.dto.DocumentDTO;
import com.blazenn.realtime_document_editing.dto.UpdateDocumentDTO;
import com.blazenn.realtime_document_editing.model.Document;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Mapper(componentModel = "spring")
public abstract class DocumentMapper {

    @Autowired
    protected ObjectMapper mapper;

    public abstract Document documentDTOToDocument(DocumentDTO documentDTO);

    public abstract DocumentDTO documentToDocumentDTO(Document document);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract Document updateDocumentFromDTO(DocumentDTO documentDTO, @MappingTarget Document document);

    @Named("documentDTOToMap")
    public HashMap<String, Object> documentDTOToMap(DocumentDTO documentDTO) {
        return mapper.convertValue(documentDTO, HashMap.class);
    };

    @Named("MapToDocumentDTO")
    public DocumentDTO mapDocumentToDTO(Map<String, Object> documentPayload) {
        return mapper.convertValue(documentPayload, DocumentDTO.class);
    }

    public DocumentDTO mapForDocumentDTO(String content, UpdateDocumentDTO updateDocumentDTO, Document document) {
        DocumentDTO documentDTO = this.documentToDocumentDTO(document);
        documentDTO.setContent(content);
        documentDTO.setUuid(updateDocumentDTO.getUuid());
        documentDTO.setLastModifiedDate(updateDocumentDTO.getLastModifiedDate());
        return documentDTO;
    }
}
