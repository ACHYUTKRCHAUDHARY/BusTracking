package com.dtc.bus_tracker.service;

import com.dtc.bus_tracker.dto.BusLocationEvent;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DtcIngestionService {

    private final DtcApiClient dtcApiClient;
    private final BusLocationPublisher publisher;

    public DtcIngestionService(DtcApiClient dtcApiClient, BusLocationPublisher publisher) {
        this.dtcApiClient = dtcApiClient;
        this.publisher = publisher;
    }

    @Scheduled(fixedRateString = "${dtc.poll.interval:10000}")
    public void pollAndPublish() {
        try {
            List<BusLocationEvent> events = dtcApiClient.fetchVehiclePositions();
            events.forEach(publisher::publish);
            System.out.println("Published " + events.size() + " bus location events");
        } catch (Exception e) {
            System.err.println("DTC ingestion failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}