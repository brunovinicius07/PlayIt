package com.music.infra.kafka;

import com.music.model.event.EmailRequestEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendEmailEvent(EmailRequestEvent event) {
        kafkaTemplate.send("email-request-topic", event);
    }
}
