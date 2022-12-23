/*
 * Mars Simulation Project
 * Architect.java
 * @date 2022-09-01
 * @author Manny Kung
 */
package org.mars_sim.msp.core.robot.ai.job;

import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.ObjectiveType;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * The Architect class represents an architect job focusing on construction of buildings, settlement
 * and other structures.
 */
public class Constructionbot extends RobotJob { 

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	private static final double POP_PER_BOT = 25;

	//private static final Logger logger = Logger.getLogger(Architect.class.getName());

	/** Constructor. */
	public Constructionbot() {
		// Use Job constructor.
		super();
	}

	/**
	 * Assessment is based on the Objective of the Settlement.
	 */
	@Override
	public double getOptimalCount(Settlement settlement) {
		double result = 0D;

		if (settlement.getObjective() == ObjectiveType.BUILDERS_HAVEN) {
			result = 1D + (settlement.getAllAssociatedPeople().size() / POP_PER_BOT);
		}
		return result;
	}

	@Override
	public double getCapability(Robot robot) {
		return 0;
	}
}
