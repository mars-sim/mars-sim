package org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.nodes;

import org.mars_sim.msp.ui.ogl.sandbox.scene.Util;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.Simulation;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.ForceAbstract;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.ForceParameter;

/**
 * @author stpa
 * 2014-02-24
 */
public class ForceBrownianMotion
extends ForceAbstract {

	public static final String NAME = "Brownian Motion";

	public static final double[] DEFAULT_BROWNIAN_MOTION = new double[] {0.0001d,0.0001d,0.0001d};
	public static final double[] MIN_MOTION = new double[] {-1d,-1d,-1d};
	public static final double[] MAX_MOTION = new double[] {1d,1d,1d};
	
	public static final int PARAMETER_INDEX_BROWNIAN_MOTION = 0;

	/** constructor. */
	public ForceBrownianMotion(double[] brownianMotion) {
		this.setActive(true);
		for (int i = 0; i < brownianMotion.length; i++) {
			ForceParameter parameter = new ForceParameter(
				"brownianMotion[" + Integer.toString(i) + "]",
				MIN_MOTION[i],
				MAX_MOTION[i],
				brownianMotion[i]
			);
			this.add(parameter);
		}
	}

	/** constructor. */
	public ForceBrownianMotion() {
		this(DEFAULT_BROWNIAN_MOTION);
	}

	@Override
	public void calculate(Simulation sim, double deltaTime) {
		if (this.isActive()) {
			double[] brownianMotion = new double[] {
				this.get(PARAMETER_INDEX_BROWNIAN_MOTION).getValue(),
				this.get(PARAMETER_INDEX_BROWNIAN_MOTION + 1).getValue(),
				this.get(PARAMETER_INDEX_BROWNIAN_MOTION + 2).getValue()
			};
			for (NodeForceObject nodofortoobjekto : sim.getNodeForceObject()) {
				double[] forto = nodofortoobjekto.getAcceleration();
				for (int i = 0; i < forto.length; i++) {
					forto[i] += deltaTime * (Util.rnd() - 0.5f) * brownianMotion[i];
				}
			}
		}
	}

	@Override
	public String getName() {
		return NAME;
	}
}
