package com.dtc.bus_tracker.service;

import com.dtc.bus_tracker.Kafka.producer.BusLocationEventProducer;
import com.dtc.bus_tracker.dto.BusLocationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Used when a Kafka broker is actually available (local/dev via docker-compose).
 * Production has no managed Kafka, so it uses {@link DirectBusLocationPublisher} instead.
 */
@Service
@Profile({"local", "dev"})
@RequiredArgsConstructor
public class KafkaBusLocationPublisher implements BusLocationPublisher {

    private final BusLocationEventProducer producer;

    @Override
    public void publish(BusLocationEvent event) {
        producer.publish(event);
    }
}
