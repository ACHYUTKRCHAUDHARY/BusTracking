package com.dtc.bus_tracker.kafka.consumer;

import com.dtc.bus_tracker.dto.BusLocationEvent;
import com.dtc.bus_tracker.service.BusLocationStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Profile({"local", "dev"})
@RequiredArgsConstructor
@Slf4j
public class BusLocationEventConsumer {

    private final BusLocationStore busLocationStore;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "bus-location-events", groupId = "bus-tracker-group")
    public void consume(String message) {
        try {
            BusLocationEvent event = objectMapper.readValue(message, BusLocationEvent.class);
            busLocationStore.save(event);
            log.debug("Stored bus {}", event.getVehicleId());
        } catch (Exception e) {
            log.error("Failed to process bus location event: {}", e.getMessage());
        }
    }
}