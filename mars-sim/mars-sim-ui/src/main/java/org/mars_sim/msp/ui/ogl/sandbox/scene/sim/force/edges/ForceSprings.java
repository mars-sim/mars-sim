package org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.edges;

import org.mars_sim.msp.ui.ogl.sandbox.scene.Util;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.Simulation;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.ForceAbstract;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.ForceParameter;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.nodes.NodeForceObject;

/**
 * @author stpa
 * 2014-02-24
 */
public class ForceSprings
extends ForceAbstract {

	public static final String NAME = "springs";
	public static final int DIM = Simulation.DIMENSION;

	public static final double DEFAULT_LENGTH = 1d;
	public static final double MIN_LENGTH = 0d;
	public static final double MAX_LENGTH = 1000d;

	public static final double DEFAULT_COEFFICIENT = 0.00001d;
	public static final double MIN_COEFFICIENT     = 0.0000001d;
	public static final double MAX_COEFFICIENT     = 0.01d;

	public static final int PARAMETER_INDEX_LENGTH = 0;
	public static final int PARAMETER_INDEX_COEFFICIENT = 1;

	/** constructor. */
	public ForceSprings(double length, double coefficient) {
		this.setActive(true);
		ForceParameter parameter = new ForceParameter(
			"length",
			MIN_LENGTH,
			MAX_LENGTH,
			length
		);
		this.add(parameter);
		parameter = new ForceParameter(
			"koeficiento",
			MIN_COEFFICIENT,
			MAX_COEFFICIENT,
			coefficient
		);
		this.add(parameter);
	}

	/** constructor. */
	public ForceSprings() {
		this(DEFAULT_LENGTH,DEFAULT_COEFFICIENT);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void calculate(Simulation sim, double delta_tempo) {
		if (this.isActive()) {
			double length = this.get(PARAMETER_INDEX_LENGTH).getValue();
			double coefficient = this.get(PARAMETER_INDEX_COEFFICIENT).getValue();
			for (EdgeForceObject eĝo : sim.getEdgeForceObject()) {
				NodeForceObject nodo1 = eĝo.getNode1();
				NodeForceObject nodo2 = eĝo.getNode2();
				double[] position1 = nodo1.getPosition();
				double[] position2 = nodo2.getPosition();
				double[] dif = new double[DIM];
				for (int i = 0; i < DIM; i++) {
					dif[i] = position2[i] - position1[i];
				}
				double len = Math.sqrt(dif[0] * dif[0] + dif[1] * dif[1] + dif[2] * dif[2]);
				while (len == 0.0f) {
					dif = Util.rnd3(-0.001,0.001);
					len = Math.sqrt(dif[0] * dif[0] + dif[1] * dif[1] + dif[2] * dif[2]);
				}
				double d = len - length;
				double coeff = coefficient * d / len;
				double[] force1 = nodo1.getAcceleration();
				double[] force2 = nodo2.getAcceleration();
				for (int i = 0; i < DIM; i++) {
					double force = coeff * dif[i];
					force1[i] += force;
					force2[i] -= force;
				}
			}
		}
	}
}
