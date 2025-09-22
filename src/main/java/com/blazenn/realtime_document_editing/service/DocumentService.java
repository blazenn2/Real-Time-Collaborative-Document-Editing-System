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

    public void handleDocumentCache(DocumentDTO documentDTO) {
        Cache documentCache = cacheManager.getCache("document");
        // Update or insert into cache (put will replace if already exists)
        if (documentCache != null) documentCache.put(documentDTO.getId(), documentDTO);
        else {
            Map<String, DocumentDTO> map = new HashMap<>();
            map.put("document::" + documentDTO.getId(), documentDTO);
            redisService.addMultipleToCache(map, 10);
        }
        log.debug("Document {} cached successfully", documentDTO.getId());
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public DocumentDTO save(DocumentDTO documentDTO) {
        log.info("Request to save document {}", documentDTO);
        boolean isProcessed = true;
        Document document = documentMapper.documentDTOToDocument(documentDTO);
        document = documentRepository.save(document);
        DocumentDTO mappedDocument = documentMapper.documentToDocumentDTO(document);
        try {
            this.handleDocumentCache(mappedDocument);
        } catch (Exception e) {
            log.error("Exception caught when trying to save document in cache", e);
            isProcessed = false;
        }
        outboxEntryService.save(mappedDocument, isProcessed);
        return mappedDocument;
    }

    public DocumentDTO findOneById(Long id) {
        log.info("Request to find document by id {}", id);
        DocumentDTO cachedDocumentDTO = redisService.getFromCache("document::" + id);
        if (cachedDocumentDTO != null) return cachedDocumentDTO;
        else {
            Document document = documentRepository.findById(id).orElse(null);
            DocumentDTO documentDTO =  documentMapper.documentToDocumentDTO(document);
            Map<String, DocumentDTO> map = new HashMap<>();
            map.put("document::" + id, documentDTO);
            redisService.addMultipleToCache(map, 10);
            return documentDTO;
        }
    }

    public List<DocumentDTO> findAll(Pageable pageable) {
        log.info("Request to find all documents");
        return documentRepository.findAll(pageable).stream().map(documentMapper::documentToDocumentDTO).collect(Collectors.toList());
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public DocumentDTO partialUpdate(DocumentDTO documentDTO) {
        log.info("Request to partial update document {}", documentDTO);
        boolean isProcessed = true;
        DocumentDTO updatedDocument = documentRepository.findById(documentDTO.getId()).map(document -> documentMapper.updateDocumentFromDTO(documentDTO, document)).map(documentMapper::documentToDocumentDTO).orElse(null);
        try {
            this.handleDocumentCache(updatedDocument);
        } catch (Exception e) {
            log.error("Exception caught when trying to save document in cache", e);
            isProcessed = false;
        }
        outboxEntryService.save(documentDTO, isProcessed);
        return updatedDocument;
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
