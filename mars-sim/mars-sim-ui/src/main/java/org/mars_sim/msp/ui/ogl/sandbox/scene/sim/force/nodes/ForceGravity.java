package org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.nodes;

import org.mars_sim.msp.ui.ogl.sandbox.scene.Util;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.Simulation;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.ForceAbstract;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.ForceParameter;

/**
 * @author stpa
 * 2014-02-24
 */
public class ForceGravity
extends ForceAbstract {

	public static final String NAME = "constant gravity";

	public static final double DEFAULT_GRAVITATIONAL_CONSTANT = 0.0001;
	public static final double DEFAULT_MIN = 0.00f;
	public static final double DEFAULT_MAX = 0.10f;

	public static final double[] DEFAULT_DIRECTION_OF_GRAVITY = new double[] {0d,0d,-1d};
	public static final double[] MIN_DIRECTION = new double[] {-1d,-1d,-1d};
	public static final double[] MAX_DIRECTION = new double[] {1d,1d,1d};

	public static final int PAMETER_INDEX_GRAVITATIONAL_CONSTANT = 0;
	public static final int PAMETER_INDEX_DIRECTION_OF_GRAVITY = 1;

	/** constructor. */
	public ForceGravity(double gravitationalConstant, double[] directionOfGravity) {
		this.setActive(true);
		ForceParameter parameter = new ForceParameter(
			"gravitational constant",
			DEFAULT_MIN,
			DEFAULT_MAX,
			gravitationalConstant
		);
		this.add(parameter);
		double[] direction = Util.norm(directionOfGravity);
		for (int i = 0; i < direction.length; i++) {
			parameter = new ForceParameter(
				"direction of gravity [" + Integer.toString(i) + "]",
				MIN_DIRECTION[i],
				MAX_DIRECTION[i],
				direction[i]
			);
			this.add(parameter);
		}
	}

	/** constructor. */
	public ForceGravity() {
		this(DEFAULT_GRAVITATIONAL_CONSTANT,DEFAULT_DIRECTION_OF_GRAVITY);
	}

	@Override
	public void calculate(Simulation sim, double deltaTime) {
		if (this.isActive()) {
			double gravitationalConstant = this.get(PAMETER_INDEX_GRAVITATIONAL_CONSTANT).getValue();
			double[] direction = new double[] {
				this.get(PAMETER_INDEX_DIRECTION_OF_GRAVITY).getValue(),
				this.get(PAMETER_INDEX_DIRECTION_OF_GRAVITY + 1).getValue(),
				this.get(PAMETER_INDEX_DIRECTION_OF_GRAVITY + 2).getValue()
			};
			for (NodeForceObject forceObject : sim.getNodeForceObject()) {
				double[] force = forceObject.getAcceleration();
				double mass = forceObject.getMass();
				double helpValue = mass * gravitationalConstant;
				for (int i = 0; i < force.length; i++) {
					force[i] += direction[i] * helpValue;
				}
			}
		}
	}

	@Override
	public String getName() {
		return NAME;
	}
}
