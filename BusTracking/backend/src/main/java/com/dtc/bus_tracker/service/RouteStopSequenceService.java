package com.dtc.bus_tracker.service;

import com.dtc.bus_tracker.entity.Route;
import com.dtc.bus_tracker.entity.Stop;
import com.dtc.bus_tracker.entity.StopTime;
import com.dtc.bus_tracker.entity.Trip;
import com.dtc.bus_tracker.repository.StopTimeRepository;
import com.dtc.bus_tracker.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * GTFS route-stop membership (the {@code route_stop} join table) has no
 * ordering, so "current / next / remaining stop" features need the real
 * sequence from a representative trip's stop_times instead. Any trip on the
 * route works equally well here since we only need stop order, not schedule
 * adherence.
 */
@Service
@RequiredArgsConstructor
public class RouteStopSequenceService {

    private final TripRepository tripRepository;
    private final StopTimeRepository stopTimeRepository;

    /**
     * Ordered stops for a route. Falls back to the (unordered) route-stop
     * join table if no trip/stop_times data is available for this route.
     */
    public List<Stop> orderedStops(Route route) {
        return tripRepository.findFirstByRoute_Id(route.getId())
                .map(this::orderedStopsForTrip)
                .filter(stops -> !stops.isEmpty())
                .orElseGet(route::getStops);
    }

    private List<Stop> orderedStopsForTrip(Trip trip) {
        return stopTimeRepository.findByTrip_IdOrderByStopSequenceAsc(trip.getId()).stream()
                .map(StopTime::getStop)
                .toList();
    }
}
