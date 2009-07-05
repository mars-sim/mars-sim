package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.logging.Logger;

import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.ai.Skill;

public class ObserveAstronomicalObjects extends ResearchScience implements Serializable {

	private static final long serialVersionUID = 1L;
	
	
    private static String CLASS_NAME = 
    "org.mars_sim.msp.simulation.person.ai.task.ObserveAstronomicalObjects";

    private static Logger s_log = Logger.getLogger(CLASS_NAME);
	
	public ObserveAstronomicalObjects(Person person)
			throws Exception {
		super(Skill.ASTRONOMY, person);

	}
	
	public static double getProbability(Person person) {
		double result = 0D;
	
		return result;
	}

	

}
