package com.dtc.bus_tracker.util;

/**
 * Centralizes ETA math so every feature (nearby buses, search-by-number,
 * route detail, passing-near-me) predicts arrival the same way: real
 * live speed when the feed reports one, a Delhi-traffic-realistic average
 * otherwise.
 */
public final class EtaCalculator {

    private static final double DEFAULT_SPEED_KMH = 20.0;
    private static final double MIN_MOVING_SPEED_KMH = 3.0;

    private EtaCalculator() {
    }

    public static int estimateMinutes(double distanceMeters, Double speedKmh) {
        double effectiveSpeed = (speedKmh != null && speedKmh > MIN_MOVING_SPEED_KMH) ? speedKmh : DEFAULT_SPEED_KMH;
        double metersPerMinute = effectiveSpeed * 1000.0 / 60.0;
        return (int) Math.ceil(distanceMeters / metersPerMinute);
    }
}
