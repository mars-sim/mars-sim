package org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.nodes;

import org.mars_sim.msp.ui.ogl.sandbox.scene.Util;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.Simulation;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.ForceAbstract;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.ForceParameter;

/**
 * for simulating gravitational interaction of an
 * n-body system. very naively implemented,
 * complexity O(n²), only useful for n < 500.
 * @author stpa
 */
public class ForceNBodySimple
extends ForceAbstract {

	public static final String NAME = "nbody simple";

	public static final double DEFAULT_GRAVITATIONAL_CONSTANT = -1d;
	public static final double MIN_GRAVITATIONAL_CONSTANT = -10d;
	public static final double MAX_GRAVITATIONAL_CONSTANT = 10d;

	public static final double DEFAULT_DISTANCE = 20d;
	public static final double MIN_DISTANCE = 0d;
	public static final double MAX_DISTANCE = 500d;

	public static final int PARAMETER_INDEX_GRAVITATIONAL_CONSTANT = 0;
	public static final int PARAMETER_INDEX_DISTANCE = 1;

	/** constructor. */
	public ForceNBodySimple(double gravitationalConstant, double distance) {
		this.setActive(true);
		ForceParameter parameter = new ForceParameter(
			"gravitationalConstant",
			MIN_GRAVITATIONAL_CONSTANT,
			MAX_GRAVITATIONAL_CONSTANT,
			gravitationalConstant
		);
		this.add(parameter);
		parameter = new ForceParameter(
			"distance",
			MIN_DISTANCE,
			MAX_DISTANCE,
			distance
		);
		this.add(parameter);
	}

	/** constructor. */
	public ForceNBodySimple() {
		this(DEFAULT_GRAVITATIONAL_CONSTANT,DEFAULT_DISTANCE);
	}

	@Override
	public void calculate(Simulation sim, double delta_tempo) {
		if (this.isActive()) {
			double gravitationalConstant = this.get(PARAMETER_INDEX_GRAVITATIONAL_CONSTANT).getValue();
			double distance = this.get(PARAMETER_INDEX_DISTANCE).getValue();
			double mass1,mass2;
			double coef1,coef2;
			double[] force1,dif;
			double len;
			double[] position1,position2;
			for (NodeForceObject node1 : sim.getNodeForceObject()) {
				mass1 = node1.getMass();
				coef1 = delta_tempo * gravitationalConstant * mass1; // helpvaloro por rapidigi iteracion
				force1 = node1.getAcceleration();
				position1 = node1.getPosition();
				int dim = force1.length;
				for (NodeForceObject node2 : sim.getNodeForceObject()) {
					if (node1 != node2) {
						mass2 = node2.getMass();
						position2 = node2.getPosition();
						dif = new double[dim];
						for (int i = 0; i < dim; i++) {
							dif[i] = position1[i] - position2[i];
						}
						len = Math.sqrt(dif[0] * dif[0] + dif[1] * dif[1] + dif[2] * dif[2]);
						dif = Util.norm(dif);
						if (distance == 0.0f || len < distance) {
							coef2 = coef1 * mass2 / (len * len);
							for (int i = 0; i < dim; i++) {
								force1[i] -= coef2 * dif[i];
							}
						}
					}
				}
			}
		}
	}

	public String getName() {
		return NAME;
	}
}
