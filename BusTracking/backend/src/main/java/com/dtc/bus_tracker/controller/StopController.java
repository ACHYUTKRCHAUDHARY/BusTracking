package com.dtc.bus_tracker.controller;

import com.dtc.bus_tracker.dto.StopDto;
import com.dtc.bus_tracker.entity.Stop;
import com.dtc.bus_tracker.repository.StopRepository;
import com.dtc.bus_tracker.util.GeoUtils;  // ← This import now works
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stops")
@RequiredArgsConstructor
public class StopController {

    private final StopRepository stopRepository;

    @GetMapping
    public ResponseEntity<Page<StopDto>> getAllStops(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Stop> stops = stopRepository.findAll(pageable);
        return ResponseEntity.ok(stops.map(this::toDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StopDto> getStopById(@PathVariable Long id) {
        Stop stop = stopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stop not found: " + id));
        return ResponseEntity.ok(toDto(stop));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<StopDto>> getNearbyStops(@RequestParam double lat,
                                                        @RequestParam double lng,
                                                        @RequestParam(defaultValue = "1000") double radiusMeters,
                                                        @RequestParam(defaultValue = "10") int limit) {
        List<Stop> allStops = stopRepository.findAll();
        List<Stop> nearby = allStops.stream()
                .filter(s -> GeoUtils.haversine(lat, lng, s.getLatitude(), s.getLongitude()) <= radiusMeters)
                .sorted((s1, s2) -> Double.compare(
                        GeoUtils.haversine(lat, lng, s1.getLatitude(), s1.getLongitude()),
                        GeoUtils.haversine(lat, lng, s2.getLatitude(), s2.getLongitude())))
                .limit(limit)
                .collect(Collectors.toList());
        return ResponseEntity.ok(nearby.stream().map(this::toDto).collect(Collectors.toList()));
    }

    private StopDto toDto(Stop stop) {
        StopDto dto = new StopDto();
        dto.setId(stop.getId());
        dto.setStopId(stop.getStopId());
        dto.setName(stop.getName());
        dto.setLatitude(stop.getLatitude());
        dto.setLongitude(stop.getLongitude());
        dto.setSequenceNumber(stop.getSequenceNumber());
        return dto;
    }
}