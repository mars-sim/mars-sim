/**
 * Mars Simulation Project
 * ResupplyUtil.java
 * @version 3.1.0 2017-02-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.interplanetary.transport.resupply;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.interplanetary.transport.TransitState;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.SettlementConfig;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * Utility class for resupply missions.
 */
public final class ResupplyUtil {

    // Average transit time for resupply missions from Earth to Mars [in sols]
    public static int AVG_TRANSIT_TIME = SimulationConfig.instance().getAverageTransitTime();
    // TODO: implement calculation of transit time at http://www.jpl.nasa.gov/edu/teach/activity/lets-go-to-mars-calculating-launch-windows/

    public static int MAX_NUM_SOLS_PLANNED = 2007; // 669 * 3 = 2007



	/**
	 * Private constructor for utility class.
	 */
	private ResupplyUtil() {
		// Do nothing
	}

	/**
	 * Create the initial resupply missions from the configuration XML files.
	 */
	public static List<Resupply> createInitialResupplyMissions() {

		List<Resupply> resupplyMissions = new ArrayList<Resupply>();

	    SettlementConfig settlementConfig = SimulationConfig.instance().getSettlementConfiguration();
        Iterator<Settlement> i = Simulation.instance().getUnitManager().getSettlements().iterator();
        while (i.hasNext()) {
            Settlement settlement = i.next();
            String templateName = settlement.getTemplate();
            MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();

            Iterator<ResupplyMissionTemplate> j =
                settlementConfig.getSettlementTemplate(templateName).getResupplyMissionTemplates().iterator();
            while (j.hasNext()) {
                ResupplyMissionTemplate template = j.next();
                MarsClock arrivalDate = (MarsClock) currentTime.clone();
                arrivalDate.addTime(template.getArrivalTime() * 1000D);
                Resupply resupply = new Resupply(arrivalDate, settlement);

                // Determine launch date.
                MarsClock launchDate = (MarsClock) arrivalDate.clone();
                launchDate.addTime(-1D * AVG_TRANSIT_TIME * 1000D);
                resupply.setLaunchDate(launchDate);

                // Set resupply state based on launch and arrival time.
                TransitState state = TransitState.PLANNED;
                if (MarsClock.getTimeDiff(currentTime, launchDate) >= 0D) {
                    state = TransitState.IN_TRANSIT;
                    if (MarsClock.getTimeDiff(currentTime, arrivalDate) >= 0D) {
                        state = TransitState.ARRIVED;
                    }
                }
                resupply.setTransitState(state);

                // Get resupply info from the config file.
                ResupplyConfig resupplyConfig = SimulationConfig.instance().getResupplyConfiguration();
                String resupplyName = template.getName();

                // Get new building types.
                resupply.setNewBuildings(resupplyConfig.getResupplyBuildings(resupplyName));

                // Get new vehicle types.
                resupply.setNewVehicles(resupplyConfig.getResupplyVehicleTypes(resupplyName));

                // Get new equipment types.
                resupply.setNewEquipment(resupplyConfig.getResupplyEquipment(resupplyName));

                // Get number of new immigrants.
                resupply.setNewImmigrantNum(resupplyConfig.getNumberOfResupplyImmigrants(resupplyName));

                // Get new resources map.
                resupply.setNewResources(resupplyConfig.getResupplyResources(resupplyName));

                // Get new parts map.
                resupply.setNewParts(resupplyConfig.getResupplyParts(resupplyName));

                resupplyMissions.add(resupply);
            }
        }

        return resupplyMissions;
	}
}