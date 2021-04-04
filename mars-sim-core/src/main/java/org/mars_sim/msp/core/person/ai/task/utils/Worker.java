package org.mars_sim.msp.core.person.ai.task.utils;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.logging.Loggable;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.structure.Settlement;

public interface Worker extends Loggable {

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


	/**
	 * What is the Worker doing
	 * @return
	 */
	public String getTaskDescription();

	/**
	 * Where the the Worker positioned?
	 * @return
	 */
	public Coordinates getCoordinates();


	/**
	 * How efficient is this Worker
	 * @return
	 */
	public double getPerformanceRating();


	/**
	 * Get the current Settlement of the worker; may be different from the assoicated Settlement.
	 * @return
	 */
	public Settlement getSettlement();

	/**
	 * What is the top level container of this worker; this will be a Unit that is on the MarsSurface,
	 * e.g. Vehicle or Settlement.
	 * @return
	 */
	public Unit getTopContainerUnit();
}
