/*
 * Mars Simulation Project
 * ResupplyUtil.java
 * @date 2022-09-25
 * @author Scott Davis
 */
package org.mars_sim.msp.core.interplanetary.transport.resupply;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.interplanetary.transport.TransitState;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.SettlementConfig;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * Utility class for resupply missions.
 * Future: may reference the calculation of transit time at http://www.jpl.nasa.gov/edu/teach/activity/lets-go-to-mars-calculating-launch-windows/
 */
public final class ResupplyUtil {


    public static int MAX_NUM_SOLS_PLANNED = 2007; // 669 * 3 = 2007

    public static List<Resupply> resupplies;

    private static SimulationConfig simulationConfig = SimulationConfig.instance();
	private static SettlementConfig settlementConfig = simulationConfig.getSettlementConfiguration();
	private static ResupplyConfig resupplyConfig = simulationConfig.getResupplyConfiguration();
	
    // Average transit time for resupply missions from Earth to Mars [in sols]
    private static int averageTransitTime = simulationConfig.getAverageTransitTime();
    // TODO: implement calculation of transit time at http://www.jpl.nasa.gov/edu/teach/activity/lets-go-to-mars-calculating-launch-windows/

    
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
	 * Create the initial resupply missions from the configuration XML files.
	 */
	public static Resupply getResupply(String settlementName) {
        Iterator<Resupply> i = resupplies.iterator();
        while (i.hasNext()) {
        	Resupply r = i.next();
        	if (r.getSettlementName().equals(settlementName))
        		return r;
        }
        return null;
	}
	        
	/**
	 * Creates the initial resupply missions from the configuration XML files.
	 */
	public static List<Resupply> loadInitialResupplyMissions() {
		
		if (resupplies == null)  {
			resupplies = new CopyOnWriteArrayList<>();
			
	        Iterator<Settlement> i = unitManager.getSettlements().iterator();
	        while (i.hasNext()) {
	            Settlement settlement = i.next();
	            String templateName = settlement.getTemplate();
 
	            Iterator<ResupplyMissionTemplate> j =
	                settlementConfig.getItem(templateName).getResupplyMissionTemplates().iterator();
	            while (j.hasNext()) {
	                ResupplyMissionTemplate template = j.next();
	                // Determine arrival date.
	                MarsClock arrivalDate = new MarsClock(currentTime);
	                arrivalDate.addTime(template.getArrivalTime() * 1000D);
	               
	                Resupply resupply = new Resupply(template, arrivalDate, settlement);
	                
	                // Determine launch date.
	                MarsClock launchDate = new MarsClock(arrivalDate);
	                launchDate.addTime(-1D * averageTransitTime * 1000D);
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

	                String resupplyName = template.getName();
	                // Get new building types.
	                resupply.setBuildings(resupplyConfig.getResupplyBuildings(resupplyName));
	                // Get new vehicle types.
	                resupply.setVehicles(resupplyConfig.getResupplyVehicleTypes(resupplyName));
	                // Get new equipment types.
	                resupply.setEquipment(resupplyConfig.getResupplyEquipment(resupplyName));
	                // Get number of new immigrants.
	                resupply.setNewImmigrantNum(resupplyConfig.getNumberOfResupplyImmigrants(resupplyName));
	                // Get new resources map.
	                resupply.setResources(resupplyConfig.getResupplyResources(resupplyName));
	                // Get new parts map.
	                resupply.setParts(resupplyConfig.getResupplyParts(resupplyName));

	                resupplies.add(resupply);

	            }
	        }
		}

        return resupplies;
	}
}
