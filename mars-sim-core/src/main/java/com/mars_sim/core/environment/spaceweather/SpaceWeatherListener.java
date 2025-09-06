package com.mars_sim.core.environment.spaceweather;

import java.util.EventListener;

/**
 * Listener for {@link SpaceWeatherEvent} updates.
 *
 * <p>This interface has no external dependencies and is safe to use
 * throughout the core, headless, and UI modules.</p>
 */
@FunctionalInterface
public interface SpaceWeatherListener extends EventListener {

    /**
     * Called when the current {@link SpaceWeatherEvent} changes or is refreshed.
     *
     * @param event the new event state (never {@code null})
     */
    void onSpaceWeather(SpaceWeatherEvent event);
}
