package com.dtc.bus_tracker.service;

import com.dtc.bus_tracker.entity.Route;
import com.dtc.bus_tracker.entity.Stop;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Maps the optimal stop sequence stored on the Route entity back into Stop objects.
 */
@Service
@RequiredArgsConstructor
public class RouteStopSequenceService {

    public List<Stop> orderedStops(Route route) {
        if (route.getStopSequence() == null || route.getStopSequence().isEmpty()) {
            return route.getStops();
        }
        
        Map<String, Stop> stopById = route.getStops().stream()
                .collect(Collectors.toMap(Stop::getStopId, s -> s, (s1, s2) -> s1));
                
        return route.getStopSequence().stream()
                .map(stopById::get)
                .filter(java.util.Objects::nonNull)
                .toList();
    }
}
