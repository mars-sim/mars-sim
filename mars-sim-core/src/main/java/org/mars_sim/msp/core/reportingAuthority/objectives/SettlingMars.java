/**
 * Mars Simulation Project
 * SettlingMars.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.reportingAuthority.objectives;

import java.io.Serializable;

import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.reportingAuthority.MissionAgenda;

public class SettlingMars implements MissionAgenda, Serializable {
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private static SimLogger logger = SimLogger.getLogger(SettlingMars.class.getName());
	// Mars Society's goal
	private final String name = "Settling Mars";
	
	private final String[] agendas = new String[] {
			"Engineer soil capable of hosting and sustaining organic microbial life",
			"Improve building structural integrity", 
			"Minimize physiological effects of long term exposure to martian environment" 
//			"Gather Hydro-Meteorological Data"
			};

	// Note : index for missionModifiers : 
	//	0 : AreologyFieldStudy
	//	1 : BiologyFieldStudy
	//	2 : CollectIce
	//	3 : CollectRegolith	
	//	4 : Delivery
	//	5 : Emergency
	//	6 : Exploration
	//	7 : MeteorologyFieldStudy
	//	8 : Mining
    //	9 : RescueSalvageVehicle
	//  10 : Trade
	//  11 : TravelToSettlement
	
	private final int[][] missionModifiers = new int[][] {
			{0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0},
			{0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 2, 0},
			{1, 1, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1}
	};

	@Override	
	public int[][] getMissionModifiers() {
		return missionModifiers;
	}
		
	@Override
	public String[] getAgendas() {
		return agendas;
	}


	@Override
	public String getObjectiveName() {
		return name;
	}

	@Override
	public void reportFindings(Worker unit) {
		logger.info(unit, 20_000L, "Updating the report of the local in-situ resources "
				+ "that one can collect and process for immediate uses.");
	}

	@Override
	public void gatherSamples(Worker unit) {
		logger.info(unit, 20_000L, "Analyzing structural integrity of geological features for building settlements in this region.");
	}
	
	public void setMissionModifiers() {
	}
}
