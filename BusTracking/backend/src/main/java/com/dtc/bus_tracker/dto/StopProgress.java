package com.dtc.bus_tracker.dto;

/** Where a stop sits relative to a tracked bus's live position along its route. */
public enum StopProgress {
    PASSED, CURRENT, NEXT, UPCOMING
}
