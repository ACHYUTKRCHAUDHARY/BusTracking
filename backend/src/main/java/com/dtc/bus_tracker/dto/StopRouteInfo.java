package com.dtc.bus_tracker.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StopRouteInfo {
    private Long routeId;
    private String routeCode;
    private String routeName;
    /** Null when no live bus is currently reporting a position on this route. */
    private String nextVehicleId;
    private Integer etaMinutes;
}
