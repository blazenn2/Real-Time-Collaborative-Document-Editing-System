package com.blazenn.realtime_document_editing.service.mapper;

import com.blazenn.realtime_document_editing.dto.DeadEventsDTO;
import com.blazenn.realtime_document_editing.dto.DocumentDTO;
import com.blazenn.realtime_document_editing.model.DeadEvents;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.util.Map;

@Mapper(componentModel = "spring", uses = DocumentMapper.class)
public interface DeadEventsMapper {
    DeadEventsDTO deadEventsToDeadEventsDTO(DeadEvents deadEvents);
    DeadEvents deadEventsDTOToDeadEvents(DeadEventsDTO deadEventsDTO);

    @Mapping(target = "payload", source = ".", qualifiedByName = "documentDTOToMap")
    @Mapping(target = "documentId", source = "id")
    @Mapping(target = "contentBody", source = "content")
    @Mapping(target = "id", ignore = true)
    DeadEventsDTO documentDTOToDeadEventsDTO(DocumentDTO documentDTO);

    default DeadEventsDTO createPayloadFromDeadLetterQueue(DocumentDTO documentDTO, Map<String, Object> headers, String routingKey) {
        DeadEventsDTO deadEventsDTO = documentDTOToDeadEventsDTO(documentDTO);
        deadEventsDTO.setEventType("DEAD_LETTER_QUEUE_EVENT");
        deadEventsDTO.setHeaders(headers);
        deadEventsDTO.setRoutingKey(routingKey);
        deadEventsDTO.setCreatedDate(Instant.now());
        return deadEventsDTO;
    }
}
