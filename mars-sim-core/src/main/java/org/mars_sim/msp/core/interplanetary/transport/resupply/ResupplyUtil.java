/*
 * Mars Simulation Project
 * ResupplyUtil.java
 * @date 2022-09-25
 * @author Scott Davis
 */
package org.mars_sim.msp.core.interplanetary.transport.resupply;

import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.SettlementConfig;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * Utility class for resupply missions.
 * Future: may reference the calculation of transit time at http://www.jpl.nasa.gov/edu/teach/activity/lets-go-to-mars-calculating-launch-windows/
 */
public final class ResupplyUtil {


    public static int MAX_NUM_SOLS_PLANNED = 2007; // 669 * 3 = 2007

    private static SimulationConfig simulationConfig = SimulationConfig.instance();
	private static SettlementConfig settlementConfig = simulationConfig.getSettlementConfiguration();
	
    // Average transit time for resupply missions from Earth to Mars [in sols]
    private static int averageTransitTime = simulationConfig.getAverageTransitTime();

    
    private static Simulation sim = Simulation.instance();
	private static MarsClock currentTime = sim.getMasterClock().getMarsClock();
	private static UnitManager unitManager = sim.getUnitManager();
    
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
	public static List<Resupply> loadInitialResupplyMissions() {
		List<Resupply> resupplies = new ArrayList<>();
			
		for(Settlement settlement : unitManager.getSettlements()) {
			String templateName = settlement.getTemplate();

			// For each Settlement get the resupply scheduled defined by the SettlementTemplate
			for(ResupplySchedule template : settlementConfig.getItem(templateName).getResupplyMissionTemplates()) {
				MarsClock arrivalDate = new MarsClock(currentTime);
				arrivalDate.addTime(template.getFirstArrival() * 1000D);

				// If the frequency is less than transport time also add the next ones
				for(int cycle = 0; cycle < template.getActiveMissions(); cycle++) {
					Resupply resupply = new Resupply(template, cycle+1, arrivalDate, settlement);
					resupplies.add(resupply);

					arrivalDate = new MarsClock(arrivalDate);
					arrivalDate.addTime(template.getFrequency() * 1000D);
				}
			}
		}

        return resupplies;
	}
}
