package com.blazenn.realtime_document_editing.messaging.producer;

import com.blazenn.realtime_document_editing.constants.RabbitMQConstants;
import com.blazenn.realtime_document_editing.dto.UpdateDocumentDTO;
import com.blazenn.realtime_document_editing.security.Jwt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class DocumentUpdateEventProducer {
    Logger log = LoggerFactory.getLogger(DocumentUpdateEventProducer.class);
    private final RabbitTemplate rabbitTemplate;
    private final Jwt jwt;

    public DocumentUpdateEventProducer(RabbitTemplate rabbitTemplate, Jwt jwt) {
        this.rabbitTemplate = rabbitTemplate;
        this.jwt = jwt;
    }

    public void sendDocumentUpdate(UpdateDocumentDTO updateDocumentDTO, String token) {
        log.info("Adding document to the queue[{}] with this payload: [{}]", RabbitMQConstants.UPDATE_DOC_QUEUE, updateDocumentDTO);
        rabbitTemplate.convertAndSend(RabbitMQConstants.UPDATE_DOC_EXCHANGE, RabbitMQConstants.UPDATE_DOC_ROUTING_KEY, updateDocumentDTO, message -> {
            message.getMessageProperties().setExpiration("15000");
            message.getMessageProperties().setHeader("Authorization", token);
            return message;
        });
    }

    public void sendDocumentErrorToDLQ(UpdateDocumentDTO updateDocumentDTO, String errorType) {
        log.info("Sending document[id={}] to DLQ with this payload: {}", updateDocumentDTO.getId(), updateDocumentDTO);
        String token = jwt.createToken(null, true);
        rabbitTemplate.convertAndSend(RabbitMQConstants.DEAD_LETTER_EXCHANGE, RabbitMQConstants.DEAD_LETTER_ROUTING_KEY, updateDocumentDTO, message -> {
            message.getMessageProperties().setHeader("errorType", errorType);
            message.getMessageProperties().setHeader("Authorization", token);
            return message;
        });
    }
}
