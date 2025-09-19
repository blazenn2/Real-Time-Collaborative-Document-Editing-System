package com.blazenn.realtime_document_editing.messaging.consumer;

import com.blazenn.realtime_document_editing.constants.ErrorTypeConstants;
import com.blazenn.realtime_document_editing.constants.RabbitMQConstants;
import com.blazenn.realtime_document_editing.controller.advice.errors.JwtUnathorizedException;
import com.blazenn.realtime_document_editing.dto.DocumentDTO;
import com.blazenn.realtime_document_editing.messaging.producer.DocumentUpdateEventProducer;
import com.blazenn.realtime_document_editing.security.Jwt;
import com.blazenn.realtime_document_editing.service.DeadEventsService;
import com.blazenn.realtime_document_editing.service.DocumentService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class DocumentUpdateEventListener {
    Logger log = LoggerFactory.getLogger(DocumentUpdateEventListener.class);
    private final DocumentService documentService;
    private final SimpMessagingTemplate messagingTemplate;
    private final DeadEventsService deadEventsService;
    private final DocumentUpdateEventProducer documentUpdateEventProducer;
    private final Validator validator;
    private final Jwt jwt;

    public DocumentUpdateEventListener(DocumentService documentService, SimpMessagingTemplate messagingTemplate, DeadEventsService deadEventsService, DocumentUpdateEventProducer documentUpdateEventProducer, Validator validator, Jwt jwt) {
        this.documentService = documentService;
        this.messagingTemplate = messagingTemplate;
        this.deadEventsService = deadEventsService;
        this.documentUpdateEventProducer = documentUpdateEventProducer;
        this.validator = validator;
        this.jwt = jwt;
    }

    @RabbitListener(queues = RabbitMQConstants.UPDATE_DOC_QUEUE)
    public void receiveUpdateDocument(DocumentDTO documentDTO, @Header(value = "Authorization", defaultValue = "") String token) {
        if (!jwt.validateToken(token)) {
            documentUpdateEventProducer.sendDocumentErrorToDLQ(documentDTO, ErrorTypeConstants.UNAUTHORIZED_ERROR);
            return;
        }
        Set<ConstraintViolation<DocumentDTO>> constraintViolationSet = validator.validate(documentDTO);
        if (!constraintViolationSet.isEmpty()) {
            constraintViolationSet.forEach(constraintViolation -> log.error("Caught validation error document[id={}] message : {}", documentDTO.getId(), constraintViolation.getMessage()));
            documentUpdateEventProducer.sendDocumentErrorToDLQ(documentDTO, ErrorTypeConstants.VALIDATION_ERROR);
            return;
        }
        log.info("Event for update document received on queue[{}]: {}",RabbitMQConstants.UPDATE_DOC_QUEUE, documentDTO);
        DocumentDTO updatedDocumentDTO = documentService.saveContentAndTitle(documentDTO.getId(), documentDTO);
        messagingTemplate.convertAndSend("/topic/documents/" + documentDTO.getId(), updatedDocumentDTO);
    }

    @RabbitListener(queues = RabbitMQConstants.DEAD_LETTER_QUEUE)
    public void receiveDeadLetterEvents(DocumentDTO documentDTO, Message message,@Header(value = "errorType", defaultValue = ErrorTypeConstants.PROCESS_ERROR) String errorType, @Header(value = "Authorization", defaultValue = "") String token) {
        try {
            if (!jwt.validateToken(token, true) || !jwt.validateToken(token, false)) throw new JwtUnathorizedException("Invalid JWT token caught in DLQ");
            String routingKey = message.getMessageProperties().getReceivedRoutingKey();
            Map<String, Object> headers = message.getMessageProperties().getHeaders();
            log.info("Event received in dead letter queue with this payload:[{}]", documentDTO);
            log.info("The payload contains the following headers: {}", headers);
            log.info("The payload contains the following routing key: {}", routingKey);
            deadEventsService.save(documentDTO, routingKey, headers, errorType);
        } catch (JwtUnathorizedException e) {
            log.error("Invalid JWT caught in the dead letter queue");
            e.printStackTrace();
        }
    }
}
