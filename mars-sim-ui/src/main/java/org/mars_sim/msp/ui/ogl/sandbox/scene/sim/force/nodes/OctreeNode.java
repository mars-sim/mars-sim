package org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.nodes;

import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.Simulation;

/**
 * @author stpa
 */
public class OctreeNode {

	protected double mass;
	protected double[] center;
	protected boolean hasChildren;
	protected OctreeNode[] children;
	protected NodeForceObject node;

	/** constructor. */
	public OctreeNode() {
		center = new double[Simulation.DIMENSION];
		children = new OctreeNode[(int) Math.pow(2,Simulation.DIMENSION)];
		hasChildren = false;
		node = null;
	}
}
