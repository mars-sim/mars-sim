/*
 * Mars Simulation Project
 * ResupplyUtil.java
 * @date 2022-09-25
 * @author Scott Davis
 */
package com.mars_sim.core.interplanetary.transport.resupply;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.environment.MarsSurface;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.SettlementConfig;
import com.mars_sim.core.structure.SettlementTemplateConfig;
import com.mars_sim.core.time.MarsTime;

/**
 * Utility class for resupply missions.
 * Future: may reference the calculation of transit time at http://www.jpl.nasa.gov/edu/teach/activity/lets-go-to-mars-calculating-launch-windows/
 */
public final class ResupplyUtil {
	
    // Average transit time for resupply missions from Earth to Mars [in sols]
    private static int averageTransitTime = SimulationConfig.instance().getAverageTransitTime();

	/**
	 * Private constructor for utility class.
	 */
	private ResupplyUtil() {
		// nothing
	}

	public static int getAverageTransitTime() {
		return averageTransitTime;
	}
	

	/**
	 * Creates the initial resupply missions from the configuration XML files.
	 */
	public static List<Resupply> loadInitialResupplyMissions(Simulation sim) {
		MarsTime currentTime = sim.getMasterClock().getMarsTime();
		UnitManager unitManager = sim.getUnitManager();
		SettlementTemplateConfig settlementTemplateConfig = sim.getConfig().getSettlementTemplateConfiguration();

		List<Resupply> resupplies = new ArrayList<>();
			
		for(Settlement settlement : unitManager.getSettlements()) {
			String templateName = settlement.getTemplate();

			// For each Settlement get the resupply scheduled defined by the SettlementTemplate
			for(ResupplySchedule template : settlementTemplateConfig.getItem(templateName).getResupplyMissionTemplates()) {
				var schedule = template.getSchedule();
				// Get the local time at teh Settlemnt when this will arrive
				MarsTime arrivalDate = schedule.getFirstEvent(currentTime,
						MarsSurface.getTimeOffset(settlement.getCoordinates()));

				// If the frequency is less than transport time also add the next ones
				for(int cycle = 0; cycle < template.getActiveMissions(); cycle++) {
					Resupply resupply = new Resupply(template, cycle+1, arrivalDate, settlement);
					resupplies.add(resupply);

					arrivalDate = arrivalDate.addTime(schedule.getFrequency() * 1000D);
				}
			}
		}

        return resupplies;
	}
}
