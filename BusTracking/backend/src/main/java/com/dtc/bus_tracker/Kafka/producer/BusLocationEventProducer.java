package com.dtc.bus_tracker.Kafka.producer;

import com.dtc.bus_tracker.dto.BusLocationEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class BusLocationEventProducer {

    private static final String TOPIC = "bus-location-events";

    private final KafkaTemplate<String, BusLocationEvent> kafkaTemplate;

    public BusLocationEventProducer(KafkaTemplate<String, BusLocationEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(BusLocationEvent event) {
        kafkaTemplate.send(TOPIC, event.getVehicleId(), event);
    }
}