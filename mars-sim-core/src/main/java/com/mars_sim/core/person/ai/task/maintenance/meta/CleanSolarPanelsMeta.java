/*
 *  GPL-3.0
 */
package org.mars_sim.msp.core.person.ai.task.maintenance.meta;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.meta.MetaTask;
import org.mars_sim.msp.core.person.ai.task.maintenance.CleanSolarPanels;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

public class CleanSolarPanelsMeta extends MetaTask {

    public CleanSolarPanelsMeta() {
        super(CleanSolarPanels.NAME);
    }

    @Override
    public double getProbability(Person p) {
        Settlement s = p.getSettlement();
        if (s == null) return 0;

        // Find a target with dusty panels
        Building best = s.getBuildingManager().stream()
            .filter(b -> b.getPowerFunction() != null && b.getPowerFunction().hasSolar())
            .filter(b -> b.getPowerFunction().getDust().map(d -> d.getDust() > 0.20).orElse(false))
            .findFirst().orElse(null);

        if (best == null) return 0;

        // Weight by maintenance skill & current power deficit
        double skill = 0.5 + 0.1 * p.getSkillLevel("Maintenance"); // illustrative
        double deficit = Math.max(0, s.getPowerGrid().getDemandKW() - s.getPowerGrid().getSupplyKW());
        double pressure = 0.3 + Math.min(0.7, deficit / 10.0);

        // Avoid during unsafe weather
        var w = s.getWeather();
        if (w != null && (w.getOpticalDepth() > 1.5 || w.getWindSpeed() > 20)) return 0.01;

        return skill * pressure;
    }

    @Override
    public CleanSolarPanels instantiate(Person p) {
        var target = p.getSettlement().getBuildingManager().stream()
            .filter(b -> b.getPowerFunction() != null && b.getPowerFunction().hasSolar())
            .filter(b -> b.getPowerFunction().getDust().map(d -> d.getDust() > 0.20).orElse(false))
            .findFirst().orElse(null);
        if (target == null) return null;
        return new CleanSolarPanels(p, target, target.getPowerFunction().getDust().get());
    }
}
