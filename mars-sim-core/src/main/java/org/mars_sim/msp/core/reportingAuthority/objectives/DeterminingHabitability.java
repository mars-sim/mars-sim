/**
 * Mars Simulation Project
 * DeterminingHabitability.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.reportingAuthority.objectives;

import java.io.Serializable;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.reportingAuthority.MissionAgenda;

public class DeterminingHabitability implements MissionAgenda, Serializable  {
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private static SimLogger logger = SimLogger.getLogger(DeterminingHabitability.class.getName());

	private final String name = "Determining Human Habitability";

	private final String[] agendas = new String[] {
								"Predict meteorological changes",
								"Study underground water reserve", 
								"Characterize radiation countermeasures" 
//								"Analyze Medical Data"
								};

	// Note : index for missionModifiers : 
	//	0 : AreologyFieldStudy
	//	1 : BiologyFieldStudy
	//	2 : CollectIce
	//	3 : CollectRegolith	
	//	4 : Exploration
	//	5 : MeteorologyFieldStudy
	//	6 : Mining
	//  7 : Trade
	//  8 : TravelToSettlement
	
	private final int[][] missionModifiers = new int[][] {
			{3, 0, 0, 0, 0, 9, 0, 0, 0},
			{0, 0, 9, 0, 0, 0, 0, 0, 0},
			{0, 3, 0, 0, 0, 3, 0, 0, 0}
	};
	
	private Unit unit;
	
	public DeterminingHabitability(Unit unit) {
		this.unit = unit;
	}	
	
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
	public void reportFindings() {
		logger.info(unit, 20_000L, "Updating the report of the habitability of this local region for human beings.");
	}

	@Override
	public void gatherSamples() {
		logger.info(unit, 20_000L, "Analyzing soil samples, atmospheric condition dataset and geographical factors of how suitable human beings are to live in this local region.");
	}





}
