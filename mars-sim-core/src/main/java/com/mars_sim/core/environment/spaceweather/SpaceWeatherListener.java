package com.mars_sim.core.environment.spaceweather;

/** Subscribe to receive new events or updates. */
@FunctionalInterface
public interface SpaceWeatherListener {
    void onSpaceWeather(SpaceWeatherEvent event);
}
