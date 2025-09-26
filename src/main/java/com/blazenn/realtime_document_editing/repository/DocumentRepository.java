package com.blazenn.realtime_document_editing.repository;

import com.blazenn.realtime_document_editing.model.Document;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Document d LEFT JOIN FETCH d.uuidHistory WHERE d.id= :id")
    Document findByIdWithUUIDHistory(@Param("id") Long id);
}
