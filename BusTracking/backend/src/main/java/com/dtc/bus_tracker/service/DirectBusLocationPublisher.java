package com.dtc.bus_tracker.service;

import com.dtc.bus_tracker.dto.BusLocationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Production has no managed Kafka broker, so ingested events are written
 * straight to the Redis-backed {@link BusLocationStore} instead of going
 * through the Kafka producer/consumer pipeline.
 */
@Service
@Profile("prod")
@RequiredArgsConstructor
public class DirectBusLocationPublisher implements BusLocationPublisher {

    private final BusLocationStore busLocationStore;

    @Override
    public void publish(BusLocationEvent event) {
        busLocationStore.save(event);
    }
}
