package com.dtc.bus_tracker.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JourneyLeg {
    private Long routeId;
    private String routeCode;
    private String routeName;
    private String boardStopName;
    private String alightStopName;
    private double inVehicleDistanceMeters;
    private int inVehicleMinutes;
}
