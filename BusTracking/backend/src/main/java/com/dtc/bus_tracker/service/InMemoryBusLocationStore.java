package com.dtc.bus_tracker.service;

import com.dtc.bus_tracker.dto.BusLocationEvent;

import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stands in for Redis when running the "demo" profile on a machine without
 * Redis available. Same contract as {@link RedisBusLocationStore}.
 */
@Service
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

    @Override
    public Optional<BusLocationEvent> findByVehicleId(String vehicleId) {
        return Optional.ofNullable(buses.get(vehicleId));
    }
}
