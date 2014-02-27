package org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.edges;

import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.nodes.NodeForceObject;

/**
 * @author stpa
 * 2014-02-24
 */
public class EdgeForceObject {

	protected NodeForceObject node1;
	protected NodeForceObject node2;

	/** constructor. */
	public EdgeForceObject(
		NodeForceObject node1,
		NodeForceObject node2
	) {
		this.setNode1(node1);
		this.setNode2(node2);
	}

	public NodeForceObject getNode1() {
		return this.node1;
	}

	public NodeForceObject getNode2() {
		return this.node2;
	}

	public void setNode1(NodeForceObject node1) {
		this.node1 = node1;
	}

	public void setNode2(NodeForceObject node2) {
		this.node2 = node2;
	}
}
