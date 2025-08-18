package com.blazenn.realtime_document_editing.service;

import com.blazenn.realtime_document_editing.dto.DeadEventsDTO;
import com.blazenn.realtime_document_editing.dto.DocumentDTO;
import com.blazenn.realtime_document_editing.model.DeadEvents;
import com.blazenn.realtime_document_editing.repository.DeadEventsRepository;
import com.blazenn.realtime_document_editing.service.mapper.DeadEventsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DeadEventsService {
    private final Logger log = LoggerFactory.getLogger(DeadEventsService.class);
    private final DeadEventsRepository deadEventsRepository;
    private final DeadEventsMapper deadEventsMapper;

    public DeadEventsService(DeadEventsRepository deadEventsRepository, DeadEventsMapper deadEventsMapper) {
        this.deadEventsRepository = deadEventsRepository;
        this.deadEventsMapper = deadEventsMapper;
    }

    public List<DeadEventsDTO> findAll(Pageable pageable) {
        log.info("Finding all dead events");
        return deadEventsRepository.findAll(pageable).stream().map(deadEventsMapper::deadEventsToDeadEventsDTO).collect(Collectors.toList());
    }

    public void save(DocumentDTO documentDTO, String routingKey, Map<String, Object> headers, String errorType) {
        this.save(deadEventsMapper.createPayloadFromDeadLetterQueue(documentDTO, headers, routingKey, errorType));
    };

    public void save(DeadEventsDTO deadEventsDTO) {
        log.info("Request to save dead event {}", deadEventsDTO);
        DeadEvents deadEvents = deadEventsRepository.save(deadEventsMapper.deadEventsDTOToDeadEvents(deadEventsDTO));
        deadEventsMapper.deadEventsToDeadEventsDTO(deadEvents);
    };
}
