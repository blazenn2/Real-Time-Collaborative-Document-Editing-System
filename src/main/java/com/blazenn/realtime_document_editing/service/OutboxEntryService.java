package com.blazenn.realtime_document_editing.service;

import com.blazenn.realtime_document_editing.dto.DocumentDTO;
import com.blazenn.realtime_document_editing.dto.OutboxEntryDTO;
import com.blazenn.realtime_document_editing.model.OutboxEntry;
import com.blazenn.realtime_document_editing.repository.OutboxEntryRepository;
import com.blazenn.realtime_document_editing.service.mapper.OutboxEntryMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OutboxEntryService {
    private final OutboxEntryRepository outboxEntryRepository;
    private final OutboxEntryMapper outboxEntryMapper;

    public OutboxEntryService(OutboxEntryRepository outboxEntryRepository, OutboxEntryMapper outboxEntryMapper) {
        this.outboxEntryRepository = outboxEntryRepository;
        this.outboxEntryMapper = outboxEntryMapper;
    }

    public void save(DocumentDTO documentDTO, Boolean isProcessed) {
        this.save(outboxEntryMapper.convertDocumentToOutboxEntryDTO(documentDTO, isProcessed, null));
    }

    public void save(OutboxEntryDTO outboxEntrydDTO) {
        OutboxEntry outboxEntry = outboxEntryMapper.outboxEntryDTOToOutboxEntry(outboxEntrydDTO);
        if (outboxEntry.getId() == null) {
            OutboxEntry existingOutboxEntry = outboxEntryRepository.findOutboxEntryByDocumentId(outboxEntry.getDocumentId());
            if (existingOutboxEntry != null) outboxEntry.setId(existingOutboxEntry.getId());
        }
        outboxEntry = outboxEntryRepository.save(outboxEntry);
        outboxEntryMapper.outboxEntryToOutboxEntryDTO(outboxEntry);
    }

    public List<OutboxEntryDTO> findAllOutboxEntriesThatNeedsToBeAddedToCache() {
        return outboxEntryRepository.findAllByIsProcessedFalse().stream().map(outboxEntryMapper::outboxEntryToOutboxEntryDTO).collect(Collectors.toList());
    }

    public void updateIsProcessedField(List<OutboxEntryDTO> outboxEntryDTOList) {
        List<OutboxEntry> outboxEntryList = outboxEntryDTOList.stream().map(outboxEntryMapper::outboxEntryDTOToOutboxEntry).peek(o -> o.setProcessed(true)).toList();
        outboxEntryRepository.saveAll(outboxEntryList);
    }
}
