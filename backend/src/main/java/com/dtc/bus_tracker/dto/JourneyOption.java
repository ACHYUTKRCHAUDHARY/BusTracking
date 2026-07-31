package com.dtc.bus_tracker.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class JourneyOption {
    private boolean direct;
    private List<JourneyLeg> legs;
    private double walkToBoardMeters;
    private double walkFromAlightMeters;
    private int totalMinutes;
}
