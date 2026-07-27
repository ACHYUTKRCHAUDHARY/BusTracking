package com.dtc.bus_tracker.mapper;

import com.dtc.bus_tracker.dto.StopDto;
import com.dtc.bus_tracker.entity.Stop;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StopMapper {
    StopDto toDto(Stop stop);
}
