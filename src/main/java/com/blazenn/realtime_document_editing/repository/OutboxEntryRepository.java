package com.blazenn.realtime_document_editing.repository;

import com.blazenn.realtime_document_editing.model.OutboxEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEntryRepository extends JpaRepository<OutboxEntry, Long> {
    OutboxEntry findOutboxEntryByDocumentId(Long documentId);
    List<OutboxEntry> findAllByIsProcessedFalse();
}
