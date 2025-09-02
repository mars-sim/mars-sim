/*
 *  Mars Simulation Project – Clean Solar Panels Task
 *  GPL-3.0
 */
package org.mars_sim.msp.core.person.ai.task.maintenance;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.person.ai.task.utils.TaskCategory;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.power.SolarPanelDustComponent;
import org.mars_sim.msp.core.time.MarsTime;
import java.util.List;
import java.util.Optional;

/**
 * Work-hour task that sends a worker (with EVA if required) to clean dusty panels.
 * Preconditions:
 *  - Target building has SolarPanelDustComponent
 *  - Dust > threshold
 *  - Weather safe for EVA (wind <= limit, tau <= limit)
 */
public class CleanSolarPanels extends Task {

    public static final String NAME = "Clean Solar Panels";

    private final Building target;
    private final SolarPanelDustComponent dust;
    private double minutesRemaining;
    private boolean done;

    // Tuning knobs
    private static final double BRUSH_M2_PER_MIN = 1.0; // one square meter per min
    private static final double DUST_THRESHOLD = 0.20;  // start cleaning above 20% dust
    private static final double EVA_TAU_LIMIT = 1.5;    // don't EVA in extreme storms
    private static final double EVA_WIND_LIMIT = 20.0;  // m/s

    public CleanSolarPanels(Person worker, Building target, SolarPanelDustComponent dust) {
        super(NAME, worker, TaskCategory.WORK);
        this.target = target;
        this.dust = dust;

        // Work estimate: 1 min per m2, scaled by dust level
        double area = dust.getPanelAreaM2();
        double base = Math.max(15, area); // at least 15 minutes
        this.minutesRemaining = base * Math.max(0.3, dust.getDust());
        addPhase(TaskPhase.PREPARE);
        addPhase(TaskPhase.EXECUTE);
        addPhase(TaskPhase.CLEAN_UP);
    }

    @Override
    protected void performPhase(TaskPhase phase, double millisols) {
        if (done) return;

        // Convert millisols to minutes (1 sol ≈ 24h 39m -> ~24.65 h)
        double minutes = MarsTime.millisolsToMinutes(millisols);

        switch (phase) {
            case PREPARE -> {
                if (!isWeatherSafe() || dust.getDust() < DUST_THRESHOLD) {
                    endTask(); return;
                }
                // TODO: Request EVA suit if panels are external; navigate to target anchor/spot
                setPhase(TaskPhase.EXECUTE);
            }
            case EXECUTE -> {
                // Spend time brushing
                double brush = Math.min(minutesRemaining, minutes);
                dust.manualClean(brush, BRUSH_M2_PER_MIN);
                minutesRemaining -= brush;

                // Consume stamina/calories (framework methods)
                getPerson().addFatigue(0.001 * brush);
                getPerson().consumeCalories(2.0 * brush); // illustrative

                if (minutesRemaining <= 0 || dust.getDust() < 0.02) {
                    setPhase(TaskPhase.CLEAN_UP);
                }
            }
            case CLEAN_UP -> {
                // TODO: return tools, cycle airlock if needed
                done = true;
                endTask();
            }
        }
    }

    private boolean isWeatherSafe() {
        var w = getPerson().getSettlement().getWeather(); // adjust to your API
        if (w == null) return true;
        double tau = w.getOpticalDepth();     // provided by weather model
        double wind = w.getWindSpeed();       // m/s
        return tau <= EVA_TAU_LIMIT && wind <= EVA_WIND_LIMIT;
    }

    @Override
    public String getDescription() {
        return "Brushing dust off solar panels at " + target.getName();
    }

    /** Utility to find the dustiest solar target in a settlement. */
    public static Optional<Building> selectTarget(List<Building> buildings) {
        return buildings.stream()
            .filter(b -> b.getPowerFunction() != null && b.getPowerFunction().hasSolar())
            .sorted((a, b) -> Double.compare(
                b.getPowerFunction().getDust().map(SolarPanelDustComponent::getDust).orElse(0.0),
                a.getPowerFunction().getDust().map(SolarPanelDustComponent::getDust).orElse(0.0)))
            .findFirst();
    }
}
