package com.dtc.bus_tracker.controller;


import com.dtc.bus_tracker.dto.RouteDto;
import com.dtc.bus_tracker.entity.Route;
import com.dtc.bus_tracker.entity.Stop;
import com.dtc.bus_tracker.repository.RouteRepository;
import com.dtc.bus_tracker.repository.StopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor

public class RouteController {

    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;

    // GET /api/routes - List all routes
    @GetMapping
    public ResponseEntity<List<RouteDto>> getAllRoutes() {
        List<Route> routes = routeRepository.findAll();
        List<RouteDto> dtos = routes.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // GET /api/routes/{id} - Get route with stops
    @GetMapping("/{id}")
    public ResponseEntity<RouteDto> getRouteById(@PathVariable Long id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Route not found: " + id));
        return ResponseEntity.ok(toDTO(route));
    }

    // GET /api/routes/search?query=bus&limit=10
    @GetMapping("/search")
    public ResponseEntity<List<RouteDto>> searchRoutes(@RequestParam String query,
                                                       @RequestParam(defaultValue = "10") int limit) {
        // Simple in-memory search; for production use @Query with LIKE
        List<Route> routes = routeRepository.findAll().stream()
                .filter(r -> r.getName() != null && r.getName().toLowerCase().contains(query.toLowerCase())
                        || r.getRouteCode() != null && r.getRouteCode().toLowerCase().contains(query.toLowerCase()))
                .limit(limit)
                .collect(Collectors.toList());
        return ResponseEntity.ok(routes.stream().map(this::toDTO).collect(Collectors.toList()));
    }

    // DTO conversion - Hides entity internals
    private RouteDto toDTO(Route route) {
        RouteDto dto = new RouteDto();
        dto.setId(route.getId());
        dto.setRouteCode(route.getRouteCode());
        dto.setName(route.getName());

        // Get stops for this route via join table
        List<Stop> stops = stopRepository.findByRoutes_Id(route.getId());
        dto.setStopIds(stops.stream().map(Stop::getId).collect(Collectors.toList()));
        dto.setStopNames(stops.stream().map(Stop::getName).collect(Collectors.toList()));

        return dto;
    }
}