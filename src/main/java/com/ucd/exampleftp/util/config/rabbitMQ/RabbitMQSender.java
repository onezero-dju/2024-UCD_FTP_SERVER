package com.ucd.exampleftp.util.config.rabbitMQ;

import com.ucd.exampleftp.STT.db.STTResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RabbitMQSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendSTTResponse(STTResponse sttResponse) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY,
                    sttResponse
            );
            log.info("Sent STTResponse to RabbitMQ: {}", sttResponse);
        } catch (Exception e) {
            log.error("Failed to send STTResponse to RabbitMQ", e);
        }
    }
}
