package org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.nodes;

import org.mars_sim.msp.ui.ogl.sandbox.scene.Util;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.Simulation;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.ForceParameter;

/**
 * @author stpa
 * 2014-02-25
 */
public class ForceNBodyOctreeSlow
extends ForceNBodySimple {

	public static final String NAME = "nbody octree";
	public static final double SAME_LOCATION_DISTANCE = 0.01f;

	/** under the strong assumption that this will always be 3. */
	public static final int DIM = Simulation.DIMENSION;

	public static final double DEFAULT_THETA = 1d;
	public static final double MIN_THETA = 0d;
	public static final double MAX_THETA = 1d;

	public static final int PARAMETER_INDEX_BARNESHUTTHETA = 2;

	protected OctreeNode octree = new OctreeNode();

	protected double barneshuttheta;
	protected double distance;
	protected double gravitationalConstant;
	
	public ForceNBodyOctreeSlow() {
		this(DEFAULT_GRAVITATIONAL_CONSTANT,DEFAULT_DISTANCE,DEFAULT_THETA);
	}
	
	public ForceNBodyOctreeSlow(double gravitationalConstant, double distance, double theta) {
		super(gravitationalConstant,distance);
		ForceParameter parameter = new ForceParameter(
			"BarnesHutTheta",
			MIN_THETA,
			MAX_THETA,
			DEFAULT_THETA
		);
		this.add(parameter);
	}

	@Override
	public String getName() {
		return NAME;
	}

	protected void cleanUp(OctreeNode octree) {
		for (OctreeNode node : octree.children) {
			if (node != null) {
				cleanUp(node);
			}
		}
	}

	@Override
	public void calculate(Simulation sim, double deltaTime) {
		if (this.isActive()) {
			cleanUp(this.octree);
			this.octree = new OctreeNode();
			this.barneshuttheta = this.get(PARAMETER_INDEX_BARNESHUTTHETA).getValue();
			this.distance = this.get(PARAMETER_INDEX_DISTANCE).getValue();
			this.gravitationalConstant = this.get(PARAMETER_INDEX_GRAVITATIONAL_CONSTANT).getValue();

			// find min and max values for all dimensions
			double[] min = new double[Simulation.DIMENSION];
			double[] max = new double[Simulation.DIMENSION];
			for (int i = 0; i < DIM; i++) {
				min[i] = Double.MAX_VALUE;
				max[i] = Double.MIN_VALUE;
			}
			for (NodeForceObject node : sim.getNodeForceObject()) {
				double[] position = node.getPosition();
				for (int i = 0; i < DIM; i++) {
					if (position[i] < min[i]) {
						min[i] = position[i];
					} else if (position[i] > max[i]) {
						max[i] = position[i];
					}
				}
			}

			// insert force object into the octree
			for (NodeForceObject nodo : sim.getNodeForceObject()) {
				insert(nodo,this.octree,min,max);
			}

			// calculate magnitude and center of mass
			calculateMass(octree);

			for (NodeForceObject node : sim.getNodeForceObject()) {
				forces(node,octree,min,max);
			}
		}
	}

	private boolean sameLocation(NodeForceObject n1, NodeForceObject n2) {
		double[] p1 = n1.getPosition();
		double[] p2 = n2.getPosition();
		return Util.dif3(p1,p2) < SAME_LOCATION_DISTANCE;
	}

	private void insert(NodeForceObject node, OctreeNode octree, double[] min, double[] max) {
		if (octree.hasChildren) {
			insertNode(node,octree,min,max);
		} else if (octree.node != null) {
			if (sameLocation(node,octree.node)) {
				insertNode(node,octree,min,max);
			} else {
				NodeForceObject n = octree.node;
				octree.node = null;
				insertNode(n,octree,min,max);
				insertNode(node,octree,min,max);
			}
		} else {
			octree.node = node;
		}
	}

	private void insertNode(NodeForceObject node, OctreeNode octree, double[] min, double[] max) {
		double[] position = node.getPosition();
		double[] split = new double[DIM];
		double[] nmin = new double[DIM];
		double[] nmax = new double[DIM];
		int index = 0;
		int power = 1;
		for (int i = 0; i < DIM; i++) {
			split[i] = (max[i] + min[i]) * 0.5f;
			if (position[i] >= split[i]) {
				index += power;
				nmin[i] = split[i];
				nmax[i] = max[i];
			} else {
				nmin[i] = min[i];
				nmax[i] = split[i];
			}
			power = (power << 1);
		}
		if (octree.children[index] == null) {
			octree.children[index] = new OctreeNode();
			octree.hasChildren = true;
		}
		// insert recursively
		insert(node,octree.children[index],nmin,nmax);
	}

	private void calculateMass(OctreeNode octree) {
		octree.mass = 0d;
		double[] centerOfMass = new double[DIM];
		for (int i = 0; i < DIM; i++) {
			centerOfMass[i] = 0d;
		}
		if (octree.hasChildren) {
			for (OctreeNode child : octree.children) {
				if (child != null) {
					calculateMass(child);
					octree.mass += child.mass;
					for (int i = 0; i < DIM; i++) {
						centerOfMass[i] += child.mass * child.center[i];
					}
				}
			}
		}
		if (octree.node != null) {
			octree.mass += octree.node.getMass();
			double[] position = octree.node.getPosition();
			for (int i = 0; i < DIM; i++) {
				centerOfMass[i] += octree.node.mass * position[i];
			}
		}
		for (int i = 0; i < DIM; i++) {
			octree.center[i] = centerOfMass[i] / octree.mass;
		}
	}

	private void forces(NodeForceObject node, OctreeNode octree, double[] min, double[] max) {
		double[] position = node.getPosition();
		double[] center = octree.center;
		double[] dif = new double[DIM];
		for (int i = 0; i < DIM; i++) {
			dif[i] = center[i] - position[i];
		}
		double len = Math.sqrt(dif[0] * dif[0] + dif[1] * dif[1] + dif[2] * dif[2]);
		boolean same = false;
		while (len == 0.0f) {
			// if same location, add random jitter
			for (int i = 0; i < DIM; i++) {
				dif[i] = Math.random() * 0.01d;
			}
			len = Math.sqrt(dif[0] * dif[0] + dif[1] * dif[1] + dif[2] * dif[2]);
			same = true;
		}
		boolean minDist = (this.distance > 0.0f) && (len > this.distance);
		// the Barnes-Hut approximation criteria is if the ratio of the
		// size of the quadtree box to the distance between the point and
		// the box's center of mass is beneath some threshold theta.
		if (
			(!octree.hasChildren && octree.node != node) || 
			(!same && (max[0] - min[0]) / len < this.barneshuttheta)
		) {
			if (minDist) return;
			// either only 1 particle or we meet criteria
			// for Barnes-Hut approximation, so calc force
			double v = this.gravitationalConstant * node.getMass() * octree.mass * Math.pow(len,-3);
			double[] forto = node.getAcceleration();
			for (int i = 0; i < DIM; i++) {
				forto[i] += v * dif[i];
			}
		} else if (octree.hasChildren) {
			// recurse for more accurate calculation
			double[] split = new double[DIM];
			for (int i = 0; i < DIM; i++) {
				split[i] = (max[i] + min[i]) * 0.5f;
			}
			for (int i = 0; i < octree.children.length; i++) {
				if (octree.children[i] != null) {
					double[] nmin = new double[DIM];
					double[] nmax = new double[DIM];
					int power = 1;
					for (int j = 0; j < DIM; j++) {
						if ((i & power) == power) {
							nmin[j] = split[j];
							nmax[j] = max[j];
						} else {
							nmin[j] = min[j];
							nmax[j] = split[j];
						}
						power = (power << 1);
					}
					forces(node, octree.children[i], nmin, nmax);
				}
			}
			if (minDist) return;
			if (octree.node != null && octree.node != node) {
				double v = this.gravitationalConstant * node.getMass() * octree.node.getMass() * Math.pow(len,-3);
				double[] force = node.getAcceleration();
				for (int i = 0; i < DIM; i++) {
					force[i] += v * dif[i];
				}
			}
		}
	}
}
