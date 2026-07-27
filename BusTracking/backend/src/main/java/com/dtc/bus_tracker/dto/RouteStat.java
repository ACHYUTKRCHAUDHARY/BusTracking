package com.dtc.bus_tracker.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RouteStat {
    private String routeCode;
    private long activeBusCount;
}
