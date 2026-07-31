package com.dtc.bus_tracker.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JourneyPlanRequest {
    @NotNull
    private Double sourceLat;
    @NotNull
    private Double sourceLng;
    @NotNull
    private Double destinationLat;
    @NotNull
    private Double destinationLng;
}
