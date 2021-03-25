package org.mars_sim.msp.core.person.ai.task.utils;

import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.SkillManager;

public interface Worker {

	/**
	 * Returns a reference to the Worker natural attribute manager
	 *
	 * @return the person's natural attribute manager
	 */
	public NaturalAttributeManager getNaturalAttributeManager();
	

	/**
	 * Returns a reference to the Person's skill manager
	 * 
	 * @return the person's skill manager
	 */
	public SkillManager getSkillManager();

	/**
	 * Get the workers name
	 * @return
	 */
	public String getName();
}
