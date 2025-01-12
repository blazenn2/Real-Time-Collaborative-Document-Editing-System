package com.blazenn.realtime_document_editing.repository;


import com.blazenn.realtime_document_editing.model.AppUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepository extends CrudRepository<AppUser, Long> {
}
