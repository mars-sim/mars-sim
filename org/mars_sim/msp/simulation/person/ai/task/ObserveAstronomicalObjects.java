package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;

import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.ai.Skill;

public class ObserveAstronomicalObjects extends ResearchScience implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public ObserveAstronomicalObjects(Person person)
			throws Exception {
		super(Skill.ASTRONOMY, person);

	}

	

}
