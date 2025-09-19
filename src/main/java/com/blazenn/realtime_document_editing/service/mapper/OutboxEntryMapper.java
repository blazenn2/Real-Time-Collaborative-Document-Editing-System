package com.blazenn.realtime_document_editing.service.mapper;

import com.blazenn.realtime_document_editing.constants.OutboxEntryOperationConstants;
import com.blazenn.realtime_document_editing.dto.DocumentDTO;
import com.blazenn.realtime_document_editing.dto.OutboxEntryDTO;
import com.blazenn.realtime_document_editing.model.OutboxEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

@Mapper(componentModel = "spring", uses = DocumentMapper.class)
public interface OutboxEntryMapper {

    OutboxEntryDTO outboxEntryToOutboxEntryDTO(OutboxEntry outboxEntry);

    OutboxEntry outboxEntryDTOToOutboxEntry(OutboxEntryDTO outboxEntryDTO);

    @Mapping(target = "payload", source = ".", qualifiedByName = "documentDTOToMap")
    @Mapping(target = "documentId", source = "id")
    @Mapping(target = "id", ignore = true)
    OutboxEntryDTO documentDTOToOutboxEntryDTO(DocumentDTO documentDTO);

    default OutboxEntryDTO convertDocumentToOutboxEntryDTO(DocumentDTO documentDTO, Boolean isProcessed, String operation) {
        OutboxEntryDTO outboxEntryDTO = documentDTOToOutboxEntryDTO(documentDTO);
        outboxEntryDTO.setCreatedDate(Instant.now());
        outboxEntryDTO.setOperation(operation == null ? OutboxEntryOperationConstants.UPDATE_CACHE_OPERATION : operation);
        outboxEntryDTO.setProcessed(isProcessed);
        return outboxEntryDTO;
    }
}
