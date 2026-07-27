package com.dtc.bus_tracker.service;

import com.dtc.bus_tracker.dto.BusLocationEvent;
import com.dtc.bus_tracker.dto.NearbyBusResponse;
import com.dtc.bus_tracker.entity.Stop;
import com.dtc.bus_tracker.repository.StopRepository;
import com.dtc.bus_tracker.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NearbyBusService {

    private final StopRepository stopRepository;
    private final BusLocationStore busLocationStore;

    public List<NearbyBusResponse> findNearbyBuses(double lat, double lng, double radiusMeters, int limit) {
        List<NearbyBusResponse> responses = new ArrayList<>();

        // 1. Find stops within radius
        List<Stop> nearbyStops = stopRepository.findAll().stream()
                .filter(stop -> GeoUtils.haversine(lat, lng, stop.getLatitude(), stop.getLongitude()) <= radiusMeters)
                .limit(limit * 3) // Get more, we'll filter later
                .toList();

        // 2. Get all known bus locations
        Collection<BusLocationEvent> buses = busLocationStore.findAll();
        if (buses.isEmpty()) {
            // Return stops only if no buses are being tracked yet
            return nearbyStops.stream()
                    .limit(limit)
                    .map(stop -> NearbyBusResponse.builder()
                            .stopName(stop.getName())
                            .distanceToStop(GeoUtils.haversine(lat, lng, stop.getLatitude(), stop.getLongitude()))
                            .build())
                    .toList();
        }

        // 3. For each stop, find nearby buses
        for (Stop stop : nearbyStops) {
            for (BusLocationEvent bus : buses) {
                double busDistance = GeoUtils.haversine(
                        lat, lng,
                        bus.getLatitude(), bus.getLongitude()
                );

                // Only include buses within radius
                if (busDistance <= radiusMeters) {
                    // Calculate ETA: distance / average bus speed (20 km/h ≈ 333 m/min)
                    int etaMinutes = (int) Math.ceil(busDistance / 333.0);

                    NearbyBusResponse response = NearbyBusResponse.builder()
                            .vehicleId(bus.getVehicleId())
                            .routeCode(bus.getRouteId())
                            .stopName(stop.getName())
                            .busLatitude(bus.getLatitude())
                            .busLongitude(bus.getLongitude())
                            .distanceToStop(GeoUtils.haversine(
                                    lat, lng,
                                    stop.getLatitude(), stop.getLongitude()
                            ))
                            .distanceToUser(busDistance)
                            .etaMinutes(etaMinutes)
                            .build();

                    responses.add(response);
                }
            }
        }

        // 4. Sort by distance and limit
        responses.sort((r1, r2) -> Double.compare(r1.getDistanceToUser(), r2.getDistanceToUser()));
        return responses.stream().limit(limit).toList();
    }
}