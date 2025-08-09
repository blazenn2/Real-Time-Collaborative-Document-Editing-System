package com.blazenn.realtime_document_editing.messaging.consumer;

import com.blazenn.realtime_document_editing.constants.RabbitMQConstants;
import com.blazenn.realtime_document_editing.dto.DocumentDTO;
import com.blazenn.realtime_document_editing.service.DeadEventsService;
import com.blazenn.realtime_document_editing.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DocumentUpdateEventListener {
    Logger log = LoggerFactory.getLogger(DocumentUpdateEventListener.class);
    private final DocumentService documentService;
    private final SimpMessagingTemplate messagingTemplate;
    private final DeadEventsService deadEventsService;

    public DocumentUpdateEventListener(DocumentService documentService, SimpMessagingTemplate messagingTemplate, DeadEventsService deadEventsService) {
        this.documentService = documentService;
        this.messagingTemplate = messagingTemplate;
        this.deadEventsService = deadEventsService;
    }

    @RabbitListener(queues = RabbitMQConstants.UPDATE_DOC_QUEUE)
    public void receiveUpdateDocument(DocumentDTO documentDTO) {
        log.info("Event for update document received on queue[{}]: {}",RabbitMQConstants.UPDATE_DOC_QUEUE, documentDTO);
        DocumentDTO updatedDocumentDTO = documentService.saveContentAndTitle(documentDTO.getId(), documentDTO);
        messagingTemplate.convertAndSend("/topic/documents/" + documentDTO.getId(), updatedDocumentDTO);
    }

    @RabbitListener(queues = RabbitMQConstants.DEAD_LETTER_QUEUE)
    public void receiveDeadLetterEvents(DocumentDTO documentDTO, Message message) {
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        log.info("Event received in dead letter queue with this payload:[{}]", documentDTO);
        log.info("The payload contains the following headers: {}", headers);
        log.info("The payload contains the following routing key: {}", routingKey);
        deadEventsService.save(documentDTO, routingKey, headers);
    }
}
