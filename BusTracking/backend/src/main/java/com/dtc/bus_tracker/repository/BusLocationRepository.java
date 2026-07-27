package com.dtc.bus_tracker.repository;

import com.dtc.bus_tracker.entity.BusLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusLocationRepository extends JpaRepository<BusLocation, Long> {

    List<BusLocation> findByBus_VehicleIdOrderByRecordedAtAsc(String vehicleId);

    List<BusLocation> findTop500ByBus_VehicleIdOrderByRecordedAtDesc(String vehicleId);
}
