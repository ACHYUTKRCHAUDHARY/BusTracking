package com.dtc.bus_tracker.controller;

import com.dtc.bus_tracker.dto.JourneyPlanRequest;
import com.dtc.bus_tracker.dto.JourneyPlanResponse;
import com.dtc.bus_tracker.service.JourneyPlannerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/journey")
@RequiredArgsConstructor
public class JourneyController {

    private final JourneyPlannerService journeyPlannerService;

    // POST /api/journey/plan - Smart Journey Planner: direct or single-transfer
    // bus options between two points, with walking distance and total time.
    @PostMapping("/plan")
    public ResponseEntity<JourneyPlanResponse> plan(@Valid @RequestBody JourneyPlanRequest request) {
        return ResponseEntity.ok(journeyPlannerService.plan(
                request.getSourceLat(), request.getSourceLng(),
                request.getDestinationLat(), request.getDestinationLng()));
    }
}
