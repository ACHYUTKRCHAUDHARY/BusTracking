package com.dtc.bus_tracker.dto;

import java.util.List;

public class RouteDto {
    private Long id;
    private String routeCode;
    private String name;
    private List<Long> stopIds;
    private List<String> stopNames;

    // Manual getters
    public Long getId() { return id; }
    public String getRouteCode() { return routeCode; }
    public String getName() { return name; }
    public List<Long> getStopIds() { return stopIds; }
    public List<String> getStopNames() { return stopNames; }

    // Manual setters
    public void setId(Long id) { this.id = id; }
    public void setRouteCode(String routeCode) { this.routeCode = routeCode; }
    public void setName(String name) { this.name = name; }
    public void setStopIds(List<Long> stopIds) { this.stopIds = stopIds; }
    public void setStopNames(List<String> stopNames) { this.stopNames = stopNames; }
}