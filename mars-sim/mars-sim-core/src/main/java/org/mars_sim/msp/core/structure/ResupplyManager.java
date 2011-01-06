/**
 * Mars Simulation Project
 * ResupplyManager.java
 * @version 3.00 2010-08-25
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.time.MarsClock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manages resupply missions from Earth for a settlement.
 */
public class ResupplyManager implements Serializable {
	
	// Data members
	private List<Resupply> resupplies;
	
	/**
	 * Constructor
	 * @param settlement the settlement the manager is for.
	 * @throws Exception if problem creating resupply missions.
	 */
	ResupplyManager(Settlement settlement) {
		
		//Initialize data
		resupplies = new ArrayList<Resupply>();
		
		// Create resupply missions.
		SettlementConfig config = SimulationConfig.instance().getSettlementConfiguration();
		String templateName = settlement.getTemplate();
		MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
		
		Iterator<ResupplyMissionTemplate> i = 
		    config.getSettlementTemplate(templateName).getResupplyMissionTemplates().iterator();
		while (i.hasNext()) {
		    ResupplyMissionTemplate template = i.next();
		    MarsClock arrivalDate = (MarsClock) currentTime.clone();
		    arrivalDate.addTime(template.getArrivalTime() * 1000D);
		    resupplies.add(new Resupply(arrivalDate, template.getName(), settlement));
		}
	}
	
	/**
	 * Gets the settlement resupply missions.
	 * @return list of resupply missions.
	 */
	public List<Resupply> getResupplies() {
		return resupplies;
	}
	
	/**
	 * Time passing at settlement.
	 *
	 * @param time amount of time passing (in millisols)
	 * @throws Exception if error.
	 */
	public void timePassing(double time) {
//		try {
			Iterator<Resupply> i = resupplies.iterator();
			while (i.hasNext()) {
				Resupply resupply = i.next();
				if (!resupply.isDelivered()) {
					MarsClock currentDate = Simulation.instance().getMasterClock().getMarsClock();
					if (MarsClock.getTimeDiff(resupply.getArrivalDate(), currentDate) <= 0D) {
						// Deliver supplies
						resupply.deliverSupplies();
					}
				}
			} 
//		}
//		catch (Exception e) {
//			throw new IllegalStateException("ResupplyManager.timePassing(): " + e.getMessage());
//		}
	}   	
}