package com.dtc.bus_tracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "routes")
@Builder
@NoArgsConstructor  // ← ADD THIS
@AllArgsConstructor
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route_code")
    private String routeCode;

    @Column
    private String name;

    @Builder.Default
    @ManyToMany(mappedBy = "routes")
    private List<Stop> stops = new ArrayList<>();

    // Getters
    public Long getId() { return id; }
    public String getRouteCode() { return routeCode; }
    public String getName() { return name; }
    public List<Stop> getStops() { return stops; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setRouteCode(String routeCode) { this.routeCode = routeCode; }
    public void setName(String name) { this.name = name; }
    public void setStops(List<Stop> stops) { this.stops = stops; }
}