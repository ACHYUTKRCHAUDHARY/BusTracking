package com.dtc.bus_tracker.service;

import com.dtc.bus_tracker.dto.BusLocationEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stands in for Redis when running the "demo" profile on a machine without
 * Docker/Redis available. Same contract as {@link RedisBusLocationStore}.
 */
@Service
@Profile("demo")
public class InMemoryBusLocationStore implements BusLocationStore {

    private final ConcurrentHashMap<String, BusLocationEvent> buses = new ConcurrentHashMap<>();

    @Override
    public void save(BusLocationEvent event) {
        buses.put(event.getVehicleId(), event);
    }

    @Override
    public Collection<BusLocationEvent> findAll() {
        return buses.values();
    }
}
