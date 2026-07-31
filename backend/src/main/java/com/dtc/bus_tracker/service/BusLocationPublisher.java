package com.dtc.bus_tracker.service;

import com.dtc.bus_tracker.dto.BusLocationEvent;

public interface BusLocationPublisher {
    void publish(BusLocationEvent event);
}
