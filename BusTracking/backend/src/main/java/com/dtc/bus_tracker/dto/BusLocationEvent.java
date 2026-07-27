package com.dtc.bus_tracker.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusLocationEvent implements Serializable {
    private String vehicleId;
    private Double latitude;
    private Double longitude;
    private String routeId;
    private Long timestamp;
}