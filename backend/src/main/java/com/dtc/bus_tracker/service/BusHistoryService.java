package com.dtc.bus_tracker.service;

import com.dtc.bus_tracker.dto.BusHistoryPoint;
import com.dtc.bus_tracker.entity.BusLocation;
import com.dtc.bus_tracker.repository.BusLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Backs "Journey Replay": every ingested location is durably recorded to the
 * bus_locations table (see BusLocationIngestService), so a route can be
 * replayed by simply reading it back in order.
 */
@Service
@RequiredArgsConstructor
public class BusHistoryService {

    private static final int MAX_POINTS = 500;

    private final BusLocationRepository busLocationRepository;

    public List<BusHistoryPoint> getHistory(String vehicleId) {
        List<BusLocation> recent = busLocationRepository.findTop500ByBus_VehicleIdOrderByRecordedAtDesc(vehicleId);
        Collections.reverse(recent); // oldest first, so the frontend can replay chronologically
        return recent.stream()
                .map(loc -> BusHistoryPoint.builder()
                        .latitude(loc.getLatitude())
                        .longitude(loc.getLongitude())
                        .speedKmh(loc.getSpeed())
                        .recordedAt(loc.getRecordedAt().toString())
                        .build())
                .limit(MAX_POINTS)
                .toList();
    }
}
