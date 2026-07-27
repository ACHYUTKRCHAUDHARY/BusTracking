package com.dtc.bus_tracker.service;

import com.dtc.bus_tracker.dto.BusLocationEvent;
import com.dtc.bus_tracker.entity.Route;
import com.dtc.bus_tracker.entity.Stop;
import com.dtc.bus_tracker.repository.RouteRepository;
import com.dtc.bus_tracker.repository.StopRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Fakes the DTC ingestion pipeline (DtcIngestionService -> Kafka -> Redis) when
 * running the "demo" profile without Kafka/Redis available. Picks a handful of
 * real imported stops and walks synthetic buses around them so the frontend
 * has something to poll.
 */
@Service
@Profile("demo")
public class DemoBusSeeder {

    private static final int BUS_COUNT = 15;
    private static final double JITTER_DEGREES = 0.01; // ~1km

    private final BusLocationStore busLocationStore;
    private final StopRepository stopRepository;
    private final RouteRepository routeRepository;
    private final Random random = new Random();

    private List<Stop> anchorStops = List.of();
    private List<Route> routes = List.of();

    public DemoBusSeeder(BusLocationStore busLocationStore, StopRepository stopRepository, RouteRepository routeRepository) {
        this.busLocationStore = busLocationStore;
        this.stopRepository = stopRepository;
        this.routeRepository = routeRepository;
    }

    @Scheduled(fixedRateString = "${dtc.poll.interval:10000}", initialDelay = 3000)
    public void tick() {
        if (anchorStops.isEmpty() && !ensureAnchors()) {
            return; // GTFS import hasn't finished seeding stops yet
        }

        for (int i = 0; i < anchorStops.size(); i++) {
            Stop stop = anchorStops.get(i);
            double lat = stop.getLatitude() + (random.nextDouble() - 0.5) * JITTER_DEGREES;
            double lng = stop.getLongitude() + (random.nextDouble() - 0.5) * JITTER_DEGREES;
            String routeCode = routes.isEmpty() ? "DEMO" : routes.get(i % routes.size()).getRouteCode();

            busLocationStore.save(BusLocationEvent.builder()
                    .vehicleId("DEMO-" + i)
                    .latitude(lat)
                    .longitude(lng)
                    .routeId(routeCode)
                    .timestamp(System.currentTimeMillis() / 1000)
                    .build());
        }
    }

    private boolean ensureAnchors() {
        List<Stop> allStops = stopRepository.findAll();
        if (allStops.isEmpty()) {
            return false;
        }
        List<Stop> picked = new ArrayList<>();
        for (int i = 0; i < Math.min(BUS_COUNT, allStops.size()); i++) {
            picked.add(allStops.get(random.nextInt(allStops.size())));
        }
        anchorStops = picked;
        routes = routeRepository.findAll();
        return true;
    }
}
