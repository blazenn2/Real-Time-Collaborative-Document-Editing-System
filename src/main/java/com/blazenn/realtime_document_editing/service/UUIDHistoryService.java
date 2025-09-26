package com.blazenn.realtime_document_editing.service;

import com.blazenn.realtime_document_editing.model.UUIDHistory;
import com.blazenn.realtime_document_editing.repository.UUIDHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UUIDHistoryService {
    Logger log = LoggerFactory.getLogger(UUIDHistoryService.class);
    private final UUIDHistoryRepository uuidHistoryRepository;

    public UUIDHistoryService(UUIDHistoryRepository uuidHistoryRepository) {
        this.uuidHistoryRepository = uuidHistoryRepository;
    }

    public void capUUIDHistoryForSpecificDocument(Long documentId, Integer capValue) {
        log.info("Checking whether to delete outbound values against the provided cap[value={}] for document[id={}]", capValue, documentId);
        Long uuidHistoryListSize = uuidHistoryRepository.countAllByDocumentId(documentId);
        if (uuidHistoryListSize > capValue) {
            List<UUIDHistory> uuidHistoryList = uuidHistoryRepository.findAllByDocumentIdOrderByCreatedDateAsc(documentId);
            int deleteCount = (int)(uuidHistoryListSize - capValue);
            log.info("The data count exceeds the cap for UUID History, deleting {} old data entries", deleteCount);
            uuidHistoryRepository.deleteAllInBatch(uuidHistoryList.subList(0, deleteCount));
        }
    }
}
