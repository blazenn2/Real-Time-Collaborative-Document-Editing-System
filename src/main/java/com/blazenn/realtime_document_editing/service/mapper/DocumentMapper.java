package com.blazenn.realtime_document_editing.service.mapper;

import com.blazenn.realtime_document_editing.dto.DocumentDTO;
import com.blazenn.realtime_document_editing.model.Document;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.*;

@Mapper(componentModel = "spring")
public interface DocumentMapper {
    DocumentMapper INSTANCE = Mappers.getMapper(DocumentMapper.class);

    Document documentDTOToDocument(DocumentDTO documentDTO);

    DocumentDTO documentToDocumentDTO(Document document);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Document updateDocumentFromDTO(DocumentDTO documentDTO, @MappingTarget Document document);

    @Named("documentDTOToMap")
    default HashMap<String, Object> documentDTOToMap(DocumentDTO documentDTO) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(documentDTO, HashMap.class);
    };

    @Named("MapToDocumentDTO")
    default DocumentDTO mapDocumentToDTO(Map<String, Object> documentPayload) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(documentPayload, DocumentDTO.class);
    }
}
