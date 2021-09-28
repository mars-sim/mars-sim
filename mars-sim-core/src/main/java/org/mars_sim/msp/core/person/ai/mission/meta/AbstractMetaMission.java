/*
 * Mars Simulation Project
 * Delivery.java
 * @date 2021-09-28
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.robot.Robot;

/**
 * Default implementation of the MetaMission interface. Provides 
 * default implementations.
 */
public class AbstractMetaMission implements MetaMission {
	private String name;
	private MissionType type;
	
	/**
	 * Creates a new Mission meta instance
	 * @param type 
	 * @param nameKey This is used as a lookup in the Msg bundle to find the name.
	 */
	protected AbstractMetaMission(MissionType type, String nameKey) {
		super();
		this.type = type;
		this.name = Msg.getString("Mission.description." + nameKey);
	}

	public MissionType getType() {
		return type;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public Mission constructInstance(Person person) {
		throw new UnsupportedOperationException("Mission Meta "+ name + " does not support mission for Person.");
	}

	@Override
	public Mission constructInstance(Robot robot) {
		throw new UnsupportedOperationException("Mission Meta "+ name + " does not support mission for Robots.");
	}

	@Override
	public double getProbability(Person person) {
		return 0;
	}

	@Override
	public double getProbability(Robot robot) {
		return 0;
	}
}
