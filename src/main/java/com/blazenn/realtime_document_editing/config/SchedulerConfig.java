package com.blazenn.realtime_document_editing.config;

import com.blazenn.realtime_document_editing.dto.DocumentDTO;
import com.blazenn.realtime_document_editing.dto.OutboxEntryDTO;
import com.blazenn.realtime_document_editing.service.DocumentService;
import com.blazenn.realtime_document_editing.service.OutboxEntryService;
import com.blazenn.realtime_document_editing.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SchedulerConfig {
    Logger log = LoggerFactory.getLogger(SchedulerConfig.class);
    private final OutboxEntryService outboxEntryService;
    private final DocumentService documentService;
    private final RedisService redisService;

    public SchedulerConfig(OutboxEntryService outboxEntryService, DocumentService documentService, RedisService redisService) {
        this.outboxEntryService = outboxEntryService;
        this.documentService = documentService;
        this.redisService = redisService;
    }

    @Scheduled(fixedDelay = 1000)
    public void pollOnOutboxEntryToUpdateCache() {
        List<OutboxEntryDTO> outboxEntries = outboxEntryService.findAllOutboxEntriesThatNeedsToBeAddedToCache();
        if (!outboxEntries.isEmpty()) {
            log.info("There are {} outbox entries to be updated", outboxEntries.size());
            Map<String, DocumentDTO> map = new HashMap<>();
            for (OutboxEntryDTO outboxEntry : outboxEntries) map.put("document::" + outboxEntry.getDocumentId(), documentService.convertMapJsonToDocumentDTO(outboxEntry.getPayload()));
            try {
                redisService.addMultipleToCache(map, 10);
                outboxEntryService.updateIsProcessedField(outboxEntries);
            } catch (Exception e) {
                log.error("Exception caught in scheduler while updating the cache, below is the stack trace");
                e.printStackTrace();
            }
        }
    }
}
