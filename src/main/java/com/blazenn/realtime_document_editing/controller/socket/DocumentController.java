package com.blazenn.realtime_document_editing.controller.socket;

import com.blazenn.realtime_document_editing.dto.DocumentDTO;
import com.blazenn.realtime_document_editing.service.DocumentService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller("DocumentSocketController")
public class DocumentController {
    Logger log = LogManager.getLogger(DocumentController.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final DocumentService documentService;

    public DocumentController(SimpMessagingTemplate messagingTemplate, DocumentService documentService) {
        this.messagingTemplate = messagingTemplate;
        this.documentService = documentService;
    }

    @MessageMapping("/documents/{id}")
    public void getDocumentById(@DestinationVariable String id, @Payload DocumentDTO documentDTO) {
        log.info("Socket request to update document of id '{}' with this payload: {}", id, documentDTO);
        DocumentDTO document = documentService.saveContentAndTitle(Long.parseLong(id), documentDTO);
        messagingTemplate.convertAndSend("/topic/documents/" + id, document);
    }
}
