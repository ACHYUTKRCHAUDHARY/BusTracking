package com.dtc.bus_tracker.mapper;

import com.dtc.bus_tracker.dto.RouteDto;
import com.dtc.bus_tracker.entity.Route;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps the flat Route fields only. stopIds/stopNames are populated
 * separately by the controller/service since they come from a join-table
 * lookup (stops for a route), not a direct property of Route.
 */
@Mapper(componentModel = "spring")
public interface RouteMapper {

    @Mapping(target = "stopIds", ignore = true)
    @Mapping(target = "stopNames", ignore = true)
    RouteDto toDto(Route route);
}
