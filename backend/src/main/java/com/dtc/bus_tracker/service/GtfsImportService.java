package com.dtc.bus_tracker.service;

import com.dtc.bus_tracker.entity.Route;
import com.dtc.bus_tracker.entity.Stop;
import com.dtc.bus_tracker.repository.RouteRepository;
import com.dtc.bus_tracker.repository.StopRepository;
import com.opencsv.CSVReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Imports the bundled DTC/DIMTS GTFS feed.
 * We optimize the import by only storing the longest sequence of stops for each route
 * in memory, and attaching it to the Route entity, avoiding saving millions of StopTime rows.
 */
@Service
public class GtfsImportService {

    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;

    private static final String GTFS_ZIP_PATH = "static/GTFS.zip";

    @Value("${gtfs.import.center-lat:28.77}")
    private double centerLat;
    @Value("${gtfs.import.center-lng:77.09}")
    private double centerLng;
    @Value("${gtfs.import.radius-degrees:0.16}")
    private double radiusDegrees;

    public GtfsImportService(
            RouteRepository routeRepository,
            StopRepository stopRepository) {
        this.routeRepository = routeRepository;
        this.stopRepository = stopRepository;
    }

    private record StopRow(String stopId, String name, double lat, double lon) {}
    private record TripRow(String routeCode, String tripId) {}

    private static final String LFS_POINTER_MAGIC = "version https://git-lfs.github.com/spec/v1";

    public void importAll() throws Exception {
        if (routeRepository.count() > 0) {
            System.out.println("GTFS already seeded, skipping import.");
            return;
        }

        assertGtfsZipIsResolved();

        boolean filtered = radiusDegrees > 0;
        System.out.println(filtered
                ? "Importing GTFS subset within " + radiusDegrees + " deg of (" + centerLat + ", " + centerLng + ")"
                : "Importing full GTFS feed (no geographic filter)");

        // 1. stops.txt -> which stop_ids are in scope
        List<StopRow> stopRows = readStops();
        List<StopRow> keptStops = filtered ? filterStopsByBoundingBox(stopRows) : stopRows;
        Set<String> keptStopIds = new HashSet<>();
        for (StopRow s : keptStops) keptStopIds.add(s.stopId());

        // 2. trips.txt -> routeCode, tripId
        List<TripRow> tripRows = readTrips();
        
        // 3. routes.txt -> save all routes (or subset if you wanted, but we'll load all valid routeCodes)
        Set<String> allRouteCodesInTrips = new HashSet<>();
        for (TripRow t : tripRows) allRouteCodesInTrips.add(t.routeCode());
        Map<String, Route> routeByCode = importRoutes(allRouteCodesInTrips);

        // 4. save kept stops
        Map<String, Stop> stopByStopId = importStops(keptStops);

        // 5. Compute route stop sequences in-memory and link everything
        computeRouteSequences(tripRows, routeByCode, stopByStopId, keptStopIds, filtered);
    }

    private void assertGtfsZipIsResolved() throws Exception {
        ClassPathResource resource = new ClassPathResource(GTFS_ZIP_PATH);
        byte[] head = new byte[64];
        int read;
        try (var in = resource.getInputStream()) {
            read = in.readNBytes(head, 0, head.length);
        }
        String prefix = new String(head, 0, read, java.nio.charset.StandardCharsets.US_ASCII);
        if (prefix.startsWith(LFS_POINTER_MAGIC)) {
            throw new IllegalStateException(
                    "GTFS.zip is an unresolved Git LFS pointer file, not the real archive.");
        }
    }

    private boolean inBoundingBox(double lat, double lon) {
        return lat >= centerLat - radiusDegrees && lat <= centerLat + radiusDegrees
                && lon >= centerLng - radiusDegrees && lon <= centerLng + radiusDegrees;
    }

    private List<StopRow> filterStopsByBoundingBox(List<StopRow> rows) {
        List<StopRow> kept = new ArrayList<>();
        for (StopRow row : rows) {
            if (inBoundingBox(row.lat(), row.lon())) {
                kept.add(row);
            }
        }
        System.out.println("Bounding box kept " + kept.size() + " of " + rows.size() + " stops.");
        return kept;
    }

    private List<StopRow> readStops() throws Exception {
        List<StopRow> rows = new ArrayList<>();
        withZipEntry("stops.txt", zis -> {
            try (CSVReader reader = new CSVReader(new InputStreamReader(zis))) {
                reader.readNext();
                String[] row;
                while ((row = reader.readNext()) != null) {
                    rows.add(new StopRow(row[1], row[4], Double.parseDouble(row[2]), Double.parseDouble(row[3])));
                }
            }
        });
        return rows;
    }

    private List<TripRow> readTrips() throws Exception {
        List<TripRow> rows = new ArrayList<>();
        withZipEntry("trips.txt", zis -> {
            try (CSVReader reader = new CSVReader(new InputStreamReader(zis))) {
                reader.readNext();
                String[] row;
                while ((row = reader.readNext()) != null) {
                    rows.add(new TripRow(row[0], row[2]));
                }
            }
        });
        return rows;
    }

    private Map<String, Route> importRoutes(Set<String> validRouteCodes) throws Exception {
        Map<String, Route> byCode = new HashMap<>();
        withZipEntry("routes.txt", zis -> {
            try (CSVReader reader = new CSVReader(new InputStreamReader(zis))) {
                reader.readNext();
                String[] row;
                int count = 0;
                while ((row = reader.readNext()) != null) {
                    String routeCode = row[1];
                    if (!validRouteCodes.contains(routeCode)) continue;

                    Route route = Route.builder()
                            .routeCode(routeCode)
                            .name(row[2].isBlank() ? row[3] : row[2])
                            .stopSequence(new ArrayList<>())
                            .build();
                    routeRepository.save(route);
                    byCode.put(routeCode, route);
                    count++;
                }
                System.out.println("Imported " + count + " routes.");
            }
        });
        return byCode;
    }

    private Map<String, Stop> importStops(List<StopRow> keptStops) {
        Map<String, Stop> byStopId = new HashMap<>();
        for (StopRow row : keptStops) {
            Stop stop = Stop.builder()
                    .stopId(row.stopId())
                    .name(row.name())
                    .latitude(row.lat())
                    .longitude(row.lon())
                    .build();
            stopRepository.save(stop);
            byStopId.put(row.stopId(), stop);
        }
        System.out.println("Imported " + byStopId.size() + " stops.");
        return byStopId;
    }

    private void computeRouteSequences(List<TripRow> tripRows, Map<String, Route> routeByCode, 
                                       Map<String, Stop> stopByStopId, Set<String> keptStopIds, boolean filtered) throws Exception {
        Map<String, String> tripToRouteCode = new HashMap<>();
        for (TripRow t : tripRows) {
            if (routeByCode.containsKey(t.routeCode())) {
                tripToRouteCode.put(t.tripId(), t.routeCode());
            }
        }

        Map<String, List<String>> stopsForTrip = new HashMap<>();
        System.out.println("Processing stop_times...");

        withZipEntry("stop_times.txt", zis -> {
            try (CSVReader reader = new CSVReader(new InputStreamReader(zis))) {
                reader.readNext();
                String[] row;
                while ((row = reader.readNext()) != null) {
                    String tripId = row[0];
                    String stopId = row[3];
                    if (tripToRouteCode.containsKey(tripId) && stopByStopId.containsKey(stopId)) {
                        stopsForTrip.computeIfAbsent(tripId, k -> new ArrayList<>()).add(stopId);
                    }
                }
            }
        });

        // Find longest sequence for each route
        Map<String, List<String>> longestSeqForRoute = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : stopsForTrip.entrySet()) {
            String routeCode = tripToRouteCode.get(entry.getKey());
            List<String> seq = entry.getValue();
            List<String> existing = longestSeqForRoute.get(routeCode);
            if (existing == null || seq.size() > existing.size()) {
                longestSeqForRoute.put(routeCode, seq);
            }
        }

        // Save sequences to Routes and link Stops
        Map<Stop, Set<Route>> routesByStop = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : longestSeqForRoute.entrySet()) {
            Route route = routeByCode.get(entry.getKey());
            if (route != null) {
                route.setStopSequence(entry.getValue());
                routeRepository.save(route);
                
                for (String stopId : entry.getValue()) {
                    Stop stop = stopByStopId.get(stopId);
                    if (stop != null) {
                        routesByStop.computeIfAbsent(stop, k -> new HashSet<>()).add(route);
                    }
                }
            }
        }
        
        for (Map.Entry<Stop, Set<Route>> entry : routesByStop.entrySet()) {
            entry.getKey().setRoutes(new ArrayList<>(entry.getValue()));
        }
        stopRepository.saveAll(routesByStop.keySet());
        
        System.out.println("Optimized GTFS route sequences imported successfully.");
    }

    @FunctionalInterface
    private interface ZipEntryConsumer {
        void accept(ZipInputStream zis) throws Exception;
    }

    private void withZipEntry(String entryName, ZipEntryConsumer consumer) throws Exception {
        try (ZipInputStream zis = new ZipInputStream(new ClassPathResource(GTFS_ZIP_PATH).getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals(entryName)) {
                    consumer.accept(zis);
                    return;
                }
            }
        }
    }
}
