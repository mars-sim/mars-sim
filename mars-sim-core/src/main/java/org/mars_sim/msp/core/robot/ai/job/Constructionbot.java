/*
 * Mars Simulation Project
 * Architect.java
 * @date 2022-09-01
 * @author Manny Kung
 */
package org.mars_sim.msp.core.robot.ai.job;

import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * The Architect class represents an architect job focusing on construction of buildings, settlement
 * and other structures.
 */
public class Constructionbot
extends RobotJob { 

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	//private static final Logger logger = Logger.getLogger(Architect.class.getName());

	/** Constructor. */
	public Constructionbot() {
		// Use Job constructor.
		super();

		// Add architect-related missions.
		//jobMissionStarts.add(BuildingConstructionMission.class);
		//jobMissionJoins.add(BuildingConstructionMission.class);
		//jobMissionStarts.add(BuildingSalvageMission.class);
		//jobMissionJoins.add(BuildingSalvageMission.class);

	}

	@Override
	public double getSettlementNeed(Settlement settlement) {
		double result = 0D;
		// Add number of buildings currently at settlement.
		result += settlement.getBuildingManager().getNumBuildings() / 10D;
		return result;
	}

	@Override
	public double getCapability(Robot robot) {
		// TODO Auto-generated method stub
		return 0;
	}
}
