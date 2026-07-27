package com.dtc.bus_tracker.service;

import com.dtc.bus_tracker.dto.BusLocationEvent;

import java.util.Collection;

public interface BusLocationStore {
    void save(BusLocationEvent event);
    Collection<BusLocationEvent> findAll();
}
