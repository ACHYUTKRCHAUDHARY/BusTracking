package com.dtc.bus_tracker.service;

import com.dtc.bus_tracker.dto.AdminStatsResponse;
import com.dtc.bus_tracker.dto.BusLocationEvent;
import com.dtc.bus_tracker.dto.RouteStat;
import com.dtc.bus_tracker.repository.BusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Live/offline/delayed are all derived from the same in-memory location feed
 * rather than a stored schedule, since this project has no per-trip
 * schedule-adherence tracking. "Delayed" here means "reporting a live
 * position but moving well below normal traffic speed" - a proxy for being
 * stuck, not a comparison against a timetable.
 */
@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private static final long STALE_AFTER_SECONDS = 120;
    private static final double DELAYED_SPEED_KMH_THRESHOLD = 5.0;

    private final BusRepository busRepository;
    private final BusLocationStore busLocationStore;

    public AdminStatsResponse getStats() {
        long nowEpochSeconds = System.currentTimeMillis() / 1000;
        Collection<BusLocationEvent> allLive = busLocationStore.findAll();

        List<BusLocationEvent> reporting = allLive.stream()
                .filter(e -> e.getTimestamp() != null && (nowEpochSeconds - e.getTimestamp()) <= STALE_AFTER_SECONDS)
                .toList();

        long totalRegisteredBuses = busRepository.count();
        long liveCount = reporting.size();
        long offlineCount = Math.max(0, totalRegisteredBuses - liveCount);

        long delayedCount = reporting.stream()
                .filter(e -> e.getSpeedKmh() != null && e.getSpeedKmh() < DELAYED_SPEED_KMH_THRESHOLD)
                .count();

        double averageSpeed = reporting.stream()
                .filter(e -> e.getSpeedKmh() != null)
                .mapToDouble(e -> e.getSpeedKmh())
                .average()
                .orElse(0.0);

        Map<String, Long> busesPerRoute = reporting.stream()
                .filter(e -> e.getRouteId() != null)
                .collect(Collectors.groupingBy(e -> e.getRouteId(), Collectors.counting()));

        List<RouteStat> routeStats = busesPerRoute.entrySet().stream()
                .map(e -> RouteStat.builder().routeCode(e.getKey()).activeBusCount(e.getValue()).build())
                .sorted(Comparator.comparingLong((RouteStat r) -> r.getActiveBusCount()).reversed())
                .toList();

        return AdminStatsResponse.builder()
                .liveBusCount(liveCount)
                .offlineBusCount(offlineCount)
                .delayedBusCount(delayedCount)
                .averageSpeedKmh(Math.round(averageSpeed * 10) / 10.0)
                .routeStats(routeStats)
                .build();
    }
}
