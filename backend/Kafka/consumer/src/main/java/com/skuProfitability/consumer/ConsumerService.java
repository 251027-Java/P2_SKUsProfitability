package com.skuProfitability.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class ConsumerService {

    private final List<String> receivedMessages = new ArrayList<>();

    @KafkaListener(topics = "messages", groupId = "sku-group")
    public void listen(String message) {
        System.out.println("Received message: " + message);
        receivedMessages.add(message);
    }

    public List<String> getMessages() {
        return receivedMessages;
    }

    public void clearMessages() {
        receivedMessages.clear();
    }
}
