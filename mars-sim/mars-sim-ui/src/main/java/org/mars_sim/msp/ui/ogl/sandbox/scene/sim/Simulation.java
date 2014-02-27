package org.mars_sim.msp.ui.ogl.sandbox.scene.sim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.ui.ogl.sandbox.scene.Util;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.ForceInterface;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.ForceParameter;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.ParametrizedAbstract;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.edges.EdgeForceObject;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.nodes.NodeForceObject;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.tree.Tree;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.tree.TreeEdge;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.tree.TreeNode;

/**
 * @author stpa
 * 2014-02-24
 */
public class Simulation
extends ParametrizedAbstract {

	protected static final double DELTA_TIME = 0.01d;
	protected static final double MIN_DELTA_TIME = 0.00000000001d;
	protected static final double MAX_DELTA_TIME = 2d;

	protected static final double DEFAULT_SPEED_LIMIT = 1.0d;
	protected static final double MIN_SPEED_LIMIT = 0.0000001d;
	protected static final double MAX_SPEED_LIMIT = 100000000d;

	protected static final int PARAMETERINDEX_DELTA_TIME = 0;
	protected static final int PARAMETERINDEX_SPEED_LIMIT = 1;

	public static final int DIMENSION = 3;

	protected boolean active;
	protected List<ForceInterface> forces = new ArrayList<ForceInterface>();
	protected Tree tree;
	protected Map<TreeNode,NodeForceObject> mapNodes;
	protected Map<TreeEdge,EdgeForceObject> mapEdges;
	protected double speedLimit;

	/**
	 * constructor.
	 * @param tree
	 * @param speedLimit
	 */
	public Simulation(Tree tree, double speedLimit, boolean fixFirst) {
		this.setActive(true);
		ForceParameter param = new ForceParameter(
			"delta time",
			MIN_DELTA_TIME,
			MAX_DELTA_TIME,
			DELTA_TIME
		);
		this.add(param);
		param = new ForceParameter(
			"speed limit",
			MIN_SPEED_LIMIT,
			MAX_SPEED_LIMIT,
			DEFAULT_SPEED_LIMIT
		);
		this.add(param);
		this.setTree(tree,fixFirst);
		this.setSpeedLimit(speedLimit);
	}

	/**
	 * constructor.
	 * @param tree
	 */
	public Simulation(Tree tree) {
		this(tree,DEFAULT_SPEED_LIMIT,false);
	}
	
	public Simulation(Tree tree, boolean fixFirst) {
		this(tree,DEFAULT_SPEED_LIMIT,fixFirst);
	}

	public void addForce(ForceInterface force) {
		this.forces.add(force);
	}

	public List<ForceInterface> getForces() {
		return this.forces;
	}

	public void setTree(Tree tree,boolean fixFirst) {
		this.tree = tree;
		if (mapNodes == null) {
			mapNodes = new HashMap<TreeNode,NodeForceObject>();
		} else {
			mapNodes.clear();
		}
		if (mapEdges == null) {
			mapEdges = new HashMap<TreeEdge,EdgeForceObject>();
		}
		boolean first = true;
		for (TreeNode node : tree.getNodes().values()) {
			boolean fixed;
			if (first) {
				fixed = fixFirst;
				first = false;
			} else {
				fixed = false;
			}
			mapNodes.put(node,new NodeForceObject(Util.rnd3(-100.0f,100.0f),fixed));
		}
		for (TreeEdge edge : tree.getEdges()) {
			NodeForceObject node1 = mapNodes.get(edge.getSource());
			NodeForceObject node2 = mapNodes.get(edge.getTarget());
			mapEdges.put(edge,new EdgeForceObject(node1,node2));
		}
	}

	public void step() {
		if (this.isActive()) {
			double deltaTime = this.getDeltaTime();
			for (NodeForceObject forceObject : this.getNodeForceObject()) {
				forceObject.nullForce();
			}
			for (ForceInterface force : forces) {
				force.calculate(this, deltaTime);
			}
			for (NodeForceObject nodeForceObject : this.getNodeForceObject()) {
				if (!nodeForceObject.isFixed()) {
					double mass = nodeForceObject.getMass();
					double[] force = nodeForceObject.getAcceleration();
					double[] position = nodeForceObject.getPosition();
					double[] velocity = nodeForceObject.getVelocity();
					double coef = deltaTime / mass;
					int dim = force.length;
					for (int i = 0; i < dim; i++) {
						position[i] += deltaTime * velocity[i];
						velocity[i] += coef * force[i];
					}
					double speed = Math.sqrt(velocity[0] * velocity[0] + velocity[1] * velocity[1] + velocity[2] * velocity[2]);
					if (speed > this.getSpeedLimit()) {
						for (int i = 0; i < dim; i++) {
							velocity[i] = speedLimit * velocity[i] / speed;
						}
					}
				}
			}
		}
	}

	public Collection<EdgeForceObject> getEdgeForceObject() {
		return this.mapEdges.values();
	}

	public Collection<NodeForceObject> getNodeForceObject() {
		return this.mapNodes.values();
	}

	public void setSpeedLimit(double speedLimit) {
		this.speedLimit = Math.abs(speedLimit);
	}

	public double getSpeedLimit() {
		return this.speedLimit;
	}

	public Map<TreeNode,NodeForceObject> getMapNodes() {
		return this.mapNodes;
	}

	public Map<TreeEdge,EdgeForceObject> getMapEdges() {
		return this.mapEdges;
	}

	public void setDeltaTime(double deltaTime) {
		this.set(PARAMETERINDEX_DELTA_TIME,deltaTime);
	}

	public double getDeltaTime() {
		return this.parameters.get(PARAMETERINDEX_DELTA_TIME).getValue();
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return this.active;
	}
}
