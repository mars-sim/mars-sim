/**
 * Mars Simulation Project
 * ResupplyUtil.java
 * @version 3.04 2013-04-04
 * @author Scott Davis
 */
package org.mars_sim.msp.core.interplanetary.transport.resupply;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.interplanetary.transport.Transportable;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.SettlementConfig;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * Utility class for resupply missions.
 */
public final class ResupplyUtil {

    // Average transit time for resupply missions from Earth to Mars (sols).
    public static int AVG_TRANSIT_TIME = 200;
	
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
                String state = Transportable.PLANNED;
                if (MarsClock.getTimeDiff(currentTime, launchDate) >= 0D) {
                    state = Transportable.IN_TRANSIT;
                    if (MarsClock.getTimeDiff(currentTime, arrivalDate) >= 0D) {
                        state = Transportable.ARRIVED;
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