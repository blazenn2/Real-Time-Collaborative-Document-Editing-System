package com.blazenn.realtime_document_editing.repository;


import com.blazenn.realtime_document_editing.model.DeadEvents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeadEventsRepository extends JpaRepository<DeadEvents, Long> {
}
