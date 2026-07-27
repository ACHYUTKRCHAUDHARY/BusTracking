package com.dtc.bus_tracker.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdminStatsResponse {
    private long liveBusCount;
    private long offlineBusCount;
    private long delayedBusCount;
    private double averageSpeedKmh;
    private List<RouteStat> routeStats;
}
