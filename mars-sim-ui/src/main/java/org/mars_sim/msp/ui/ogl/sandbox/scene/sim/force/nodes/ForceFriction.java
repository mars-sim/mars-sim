package org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.nodes;

import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.Simulation;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.ForceAbstract;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.ForceParameter;

/**
 * @author stpa
 * 2014-02-24
 */
public class ForceFriction
extends ForceAbstract {

	public static final String NAME = "friction";
	
	public static final double DEFAULT_FRICTION_COEFFICIENT = 0.001d;
	public static final double DEFAULT_MIN = 0.00d;
	public static final double DEFAULT_MAX = 0.10d;
	
	public static final int PARAMETER_INDEX_FRICTION_COEFFICIENT = 0;
	
	public ForceFriction(double frictionCoefficient) {
		this.setActive(true);
		ForceParameter parameter = new ForceParameter(
			"friction coefficient",
			DEFAULT_MIN,
			DEFAULT_MAX,
			frictionCoefficient
		);
		this.add(parameter);
	}
	
	public ForceFriction() {
		this(DEFAULT_FRICTION_COEFFICIENT);
	}

	@Override
	public void calculate(Simulation sim, double deltaTime) {
		if (this.isActive()) {
			double coeff = this.get(PARAMETER_INDEX_FRICTION_COEFFICIENT).getValue();
			for (NodeForceObject forceObject : sim.getNodeForceObject()) {
				double[] acceleration = forceObject.getAcceleration();
				double[] velocity = forceObject.getVelocity();
				for (int i = 0; i < acceleration.length; i++) {
					acceleration[i] -= coeff * velocity[i];
				}
			}
		}
	}
	
	@Override
	public String getName() {
		return NAME;
	}
}
