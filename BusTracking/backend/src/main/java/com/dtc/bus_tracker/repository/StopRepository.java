package com.dtc.bus_tracker.repository;

import com.dtc.bus_tracker.entity.Stop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StopRepository extends JpaRepository<Stop, Long> {

    Optional<Stop> findByStopId(String stopId);

    // ADD THIS METHOD
    List<Stop> findByRoutes_Id(Long routeId);

    @Query("SELECT s FROM Stop s WHERE s.latitude BETWEEN :minLat AND :maxLat AND s.longitude BETWEEN :minLng AND :maxLng")
    List<Stop> findStopsWithinBoundingBox(@Param("minLat") double minLat, @Param("maxLat") double maxLat, @Param("minLng") double minLng, @Param("maxLng") double maxLng);
}