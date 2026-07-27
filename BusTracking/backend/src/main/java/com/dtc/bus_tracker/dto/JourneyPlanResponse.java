package com.dtc.bus_tracker.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class JourneyPlanResponse {
    /** Best options first. Empty (not an error) when nothing connects the two points. */
    private List<JourneyOption> options;
}
