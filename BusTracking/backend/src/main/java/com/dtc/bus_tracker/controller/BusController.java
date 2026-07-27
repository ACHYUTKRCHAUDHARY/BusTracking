package com.dtc.bus_tracker.controller;

import com.dtc.bus_tracker.dto.NearbyBusResponse;
import com.dtc.bus_tracker.service.NearbyBusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buses")
@RequiredArgsConstructor
public class BusController {

    private final NearbyBusService nearbyBusService;

    @GetMapping("/nearby")
    public ResponseEntity<List<NearbyBusResponse>> getNearbyBuses(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "1000") double radiusMeters,
            @RequestParam(defaultValue = "10") int limit) {

        List<NearbyBusResponse> response = nearbyBusService.findNearbyBuses(lat, lng, radiusMeters, limit);
        return ResponseEntity.ok(response);
    }
}