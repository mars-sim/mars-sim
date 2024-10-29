/*
 * Mars Simulation Project
 * Finance.java
 * @date 2024-10-25
 * @author Manny Kung
 */

package com.mars_sim.core.moon;

import java.io.Serializable;
import java.util.Map;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;

public class Finance implements Serializable, Temporal {

	private static final long serialVersionUID = 1L;

	private static final SimLogger logger = SimLogger.getLogger(Finance.class.getName());

	private int currentMonthInt;
	
	private Map<String, Balance> balanceHistory;
	
	private Balance currentBalance;
	
	private Colony colony;
	
	public Finance(Colony colony) {
		this.colony = colony;
		
		currentBalance = new Balance(colony.getTotalArea());
	}
	
	@Override
	public boolean timePassing(ClockPulse pulse) {
		
		int newMonthInt = pulse.getMasterClock().getEarthTime().getMonthValue();
		
		if (newMonthInt != currentMonthInt) {
			int newYearInt = pulse.getMasterClock().getEarthTime().getYear();
			// At the end of each earth month, record the balance sheet
			balanceHistory.put(newYearInt + "-" + newMonthInt, currentBalance);
		}
		
		return true;
	}
	
	public double getCurrentBalance() {
		return currentBalance.getRunningBalance();
	}
}




