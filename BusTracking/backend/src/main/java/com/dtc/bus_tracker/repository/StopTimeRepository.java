package com.dtc.bus_tracker.repository;

import com.dtc.bus_tracker.entity.StopTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StopTimeRepository extends JpaRepository<StopTime, Long> {
}