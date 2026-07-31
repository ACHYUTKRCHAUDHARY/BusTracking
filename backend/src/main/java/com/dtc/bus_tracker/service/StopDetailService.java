package com.dtc.bus_tracker.service;

import com.dtc.bus_tracker.dto.BusLocationEvent;
import com.dtc.bus_tracker.dto.StopRouteInfo;
import com.dtc.bus_tracker.entity.Route;
import com.dtc.bus_tracker.entity.Stop;
import com.dtc.bus_tracker.exception.ResourceNotFoundException;
import com.dtc.bus_tracker.repository.StopRepository;
import com.dtc.bus_tracker.util.EtaCalculator;
import com.dtc.bus_tracker.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Answers "which routes serve this stop, and when's the next bus" - the gap
 * called out in BACKEND_NOTES.md (no "buses at this stop" endpoint existed).
 */
@Service
@RequiredArgsConstructor
public class StopDetailService {

    private final StopRepository stopRepository;
    private final BusLocationStore busLocationStore;

    public List<StopRouteInfo> routesServing(Long stopId) {
        Stop stop = stopRepository.findById(stopId)
                .orElseThrow(() -> new ResourceNotFoundException("Stop not found: " + stopId));

        Collection<BusLocationEvent> liveBuses = busLocationStore.findAll();
        List<StopRouteInfo> result = new ArrayList<>();

        for (Route route : stop.getRoutes()) {
            BusLocationEvent nearest = null;
            double nearestDistance = Double.MAX_VALUE;
            for (BusLocationEvent bus : liveBuses) {
                if (!route.getRouteCode().equals(bus.getRouteId())) continue;
                double distance = GeoUtils.haversine(
                        stop.getLatitude(), stop.getLongitude(), bus.getLatitude(), bus.getLongitude());
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = bus;
                }
            }

            result.add(StopRouteInfo.builder()
                    .routeId(route.getId())
                    .routeCode(route.getRouteCode())
                    .routeName(route.getName())
                    .nextVehicleId(nearest != null ? nearest.getVehicleId() : null)
                    .etaMinutes(nearest != null ? EtaCalculator.estimateMinutes(nearestDistance, nearest.getSpeedKmh()) : null)
                    .build());
        }
        return result;
    }
}
