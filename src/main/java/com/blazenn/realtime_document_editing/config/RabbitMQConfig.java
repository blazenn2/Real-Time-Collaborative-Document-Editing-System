package com.blazenn.realtime_document_editing.config;

import com.blazenn.realtime_document_editing.constants.RabbitMQConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@Configuration
public class RabbitMQConfig {
    // Main Queue
    @Bean
    public Queue documentQueue() {
        return QueueBuilder.durable(RabbitMQConstants.UPDATE_DOC_QUEUE).withArgument("x-dead-letter-exchange", RabbitMQConstants.DEAD_LETTER_EXCHANGE).withArgument("x-dead-letter-routing-key", RabbitMQConstants.DEAD_LETTER_ROUTING_KEY).build();
    }

    @Bean
    public DirectExchange documentDirectExchange() {
        return new DirectExchange(RabbitMQConstants.UPDATE_DOC_EXCHANGE, true, false);
    }

    @Bean
    public Binding documentBinding() {
        return BindingBuilder.bind(documentQueue()).to(documentDirectExchange()).with(RabbitMQConstants.UPDATE_DOC_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);

        // Below code sets the max amount of attempts if consumer fails
        RetryOperationsInterceptor retryInterceptor = RetryInterceptorBuilder.stateless().maxAttempts(3).backOffOptions(2000, 2, 10000).recoverer(new RejectAndDontRequeueRecoverer()).build();
        factory.setAdviceChain(retryInterceptor);
        return factory;
    }

    // Dead Letter Queue
    @Bean
    public Queue deadLetterQueue() {
        return new Queue(RabbitMQConstants.DEAD_LETTER_QUEUE, true, false, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(RabbitMQConstants.DEAD_LETTER_EXCHANGE,true, false);
    }

    @Bean
    public Binding documentBindingDeadLetter() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(RabbitMQConstants.DEAD_LETTER_ROUTING_KEY);
    }
}
