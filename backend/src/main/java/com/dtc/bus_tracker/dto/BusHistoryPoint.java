package com.dtc.bus_tracker.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BusHistoryPoint {
    private Double latitude;
    private Double longitude;
    private Double speedKmh;
    private String recordedAt;
}
