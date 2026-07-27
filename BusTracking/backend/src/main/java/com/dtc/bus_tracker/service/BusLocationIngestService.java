package com.dtc.bus_tracker.service;

import com.dtc.bus_tracker.dto.BusLocationEvent;
import com.dtc.bus_tracker.entity.Bus;
import com.dtc.bus_tracker.entity.BusLocation;
import com.dtc.bus_tracker.entity.BusStatus;
import com.dtc.bus_tracker.entity.Route;
import com.dtc.bus_tracker.repository.BusLocationRepository;
import com.dtc.bus_tracker.repository.BusRepository;
import com.dtc.bus_tracker.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Single entry point every ingestion path (Kafka consumer, direct-to-Redis
 * prod publisher, demo seeder) funnels a location update through. Keeping the
 * cache write, the durable history row, and the WebSocket broadcast together
 * here means none of those call sites need to know about the other two.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BusLocationIngestService {

    private static final String BUS_TOPIC = "/topic/buses";

    private final BusLocationStore busLocationStore;
    private final BusRepository busRepository;
    private final RouteRepository routeRepository;
    private final BusLocationRepository busLocationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public void ingest(BusLocationEvent event) {
        busLocationStore.save(event);
        Bus bus = upsertBus(event);
        recordHistory(bus, event);
        messagingTemplate.convertAndSend(BUS_TOPIC, event);
    }

    private Bus upsertBus(BusLocationEvent event) {
        Bus bus = busRepository.findByVehicleId(event.getVehicleId())
                .orElseGet(() -> Bus.builder().vehicleId(event.getVehicleId()).build());

        bus.setStatus(BusStatus.ACTIVE);
        if (event.getRouteId() != null) {
            Route route = routeRepository.findByRouteCode(event.getRouteId()).orElse(null);
            if (route != null) {
                bus.setRoute(route);
            }
        }
        return busRepository.save(bus);
    }

    private void recordHistory(Bus bus, BusLocationEvent event) {
        LocalDateTime recordedAt = event.getTimestamp() != null
                ? LocalDateTime.ofInstant(Instant.ofEpochSecond(event.getTimestamp()), ZoneId.systemDefault())
                : LocalDateTime.now();

        busLocationRepository.save(BusLocation.builder()
                .bus(bus)
                .latitude(event.getLatitude())
                .longitude(event.getLongitude())
                .speed(event.getSpeedKmh())
                .recordedAt(recordedAt)
                .build());
    }
}
