package com.blazenn.realtime_document_editing.service;

import com.blazenn.realtime_document_editing.dto.DocumentDTO;
import com.blazenn.realtime_document_editing.model.Document;
import com.blazenn.realtime_document_editing.repository.DocumentRepository;
import com.blazenn.realtime_document_editing.service.mapper.DocumentMapper;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DocumentService {
    Logger log = LoggerFactory.getLogger(DocumentService.class);
    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;
    private final CacheManager cacheManager;
    private final OutboxEntryService outboxEntryService;
    private final RedisService redisService;

    public DocumentService(DocumentRepository documentRepository, DocumentMapper documentMapper, CacheManager cacheManager, OutboxEntryService outboxEntryService, RedisService redisService) {
        this.documentRepository = documentRepository;
        this.documentMapper = documentMapper;
        this.cacheManager = cacheManager;
        this.outboxEntryService = outboxEntryService;
        this.redisService = redisService;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public DocumentDTO save(DocumentDTO documentDTO) {
        log.info("Request to save document {}", documentDTO);
        boolean isProcessed = true;
        Document document = documentMapper.documentDTOToDocument(documentDTO);
        document = documentRepository.save(document);
        DocumentDTO mappedDocument = documentMapper.documentToDocumentDTO(document);
        try {
            Cache documentCache = cacheManager.getCache("document");
            // Update or insert into cache (put will replace if already exists)
            if (documentCache != null) documentCache.put(mappedDocument.getId(), mappedDocument);
            else redisService.addToCache("document::" + mappedDocument.getId(), mappedDocument, 10);
            log.debug("Document {} cached successfully", document.getId());
        } catch (Exception e) {
            log.error("Exception caught when trying to save document in cache", e);
            isProcessed = false;
        }
        outboxEntryService.save(mappedDocument, isProcessed);
        return mappedDocument;
    }

    @Cacheable(value = "document", key = "#id")
    public DocumentDTO findOneById(Long id) {
        log.info("Request to find document by id {}", id);
        Document document = documentRepository.findById(id).orElse(null);
        return documentMapper.documentToDocumentDTO(document);
    }

    public List<DocumentDTO> findAll(Pageable pageable) {
        log.info("Request to find all documents");
        return documentRepository.findAll(pageable).stream().map(documentMapper::documentToDocumentDTO).collect(Collectors.toList());
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public DocumentDTO partialUpdate(DocumentDTO documentDTO) {
        log.info("Request to partial update document {}", documentDTO);
        Cache documentCache = cacheManager.getCache("document");
        if (documentCache != null) documentCache.evictIfPresent(documentDTO.getId());
//        outboxEntryService.save(documentDTO);
        return documentRepository.findById(documentDTO.getId()).map(document -> documentMapper.updateDocumentFromDTO(documentDTO, document)).map(documentMapper::documentToDocumentDTO).orElse(null);
    }

    @CacheEvict(cacheNames = "document", key = "#id", beforeInvocation = true)
    public void deleteById(Long id) {
        log.info("Request to delete document {}", id);
        documentRepository.deleteById(id);
    }

    public Boolean checkIfDocumentExists(Long id) {
        log.info("Request to find document by id to find its existence: {}", id);
        return documentRepository.findById(id).isPresent();
    }

    public void validateDocumentForUpdate(Long id, DocumentDTO documentDTO) throws BadRequestException {
        if (documentDTO.getId() == null) throw new BadRequestException("Payload doesn't contain id to update");
        if (!Objects.equals(documentDTO.getId(), id)) throw new BadRequestException("Document id mismatch");
        if (Boolean.FALSE.equals(checkIfDocumentExists(id))) throw new BadRequestException("No document was found for id " + documentDTO.getId());
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public DocumentDTO saveContentAndTitle(Long id, DocumentDTO documentDTO) {
        if (documentDTO.getTitle() == null || documentDTO.getContent() == null) {
            DocumentDTO documentFromRepository = this.findOneById(id);
            documentDTO.setContent(documentDTO.getContent() == null ? documentFromRepository.getContent() : documentDTO.getContent());
            documentDTO.setTitle(documentDTO.getTitle() == null ? documentFromRepository.getTitle() : documentDTO.getTitle());
        }
        return this.save(documentDTO);
    }

    public DocumentDTO convertMapJsonToDocumentDTO(Map<String, Object> map) {
        return documentMapper.mapDocumentToDTO(map);
    }
}
