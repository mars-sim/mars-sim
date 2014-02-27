package org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force;

import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.Simulation;

/**
 * @author stpa
 * 2014-02-24
 */
public interface ForceInterface
extends ParametrizedInterface{

	/**
	 * returns the name of the force.
	 * @return {@link String}
	 */
	public String getName();
	
	/**
	 * initializes the force before starting the simulation.
	 * @param sim {@link Simulation}
	 */
	public void init(Simulation sim);
	
	/**
	 * calculates changes to the simulation caused by this force.
	 * @param sim {@link Simulation}
	 * @param deltaTime {@link Double}
	 */
	public void calculate(Simulation sim, double deltaTime);

	public boolean isActive();
	public void setActive(boolean active);
}
