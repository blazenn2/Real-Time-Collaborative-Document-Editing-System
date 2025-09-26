package com.blazenn.realtime_document_editing.repository;

import com.blazenn.realtime_document_editing.model.UUIDHistory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UUIDHistoryRepository extends JpaRepository<UUIDHistory, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<UUIDHistory> findAllByDocumentIdOrderByCreatedDateAsc(Long documentId);

    Long countAllByDocumentId(Long documentId);
}
