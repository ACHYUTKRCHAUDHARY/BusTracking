package com.dtc.bus_tracker.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RouteDetailResponse {
    private Long id;
    private String routeCode;
    private String name;
    /** Ordered stops with a polyline implied by stop lat/lng in sequence. */
    private List<RouteStopInfo> stops;
    private String trackedVehicleId;
    private Double vehicleLatitude;
    private Double vehicleLongitude;
    private Integer etaToNextStopMinutes;
}
