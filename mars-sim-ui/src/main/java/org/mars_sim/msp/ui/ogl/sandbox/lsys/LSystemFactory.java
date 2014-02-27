package org.mars_sim.msp.ui.ogl.sandbox.lsys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.ui.ogl.sandbox.scene.SceneGroup;
import org.mars_sim.msp.ui.ogl.sandbox.scene.Util;
import org.mars_sim.msp.ui.ogl.sandbox.scene.line.Line;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.tree.Tree;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.tree.TreeEdge;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.tree.TreeNode;

/**
 * interpretes the output of l-systems as painting instructions.
 * @author stpa.
 */
public class LSystemFactory {

	public static final int MAX_STACK = 32;
	public static final String SYMBOL_PUSH = "[";
	public static final String SYMBOL_POP = "]";
	public static final String SYMBOL_JUMP = "f";
	public static final String SYMBOL_PAINT = "F";
	public static final String SYMBOL_ALPHA_PLUS = "+";
	public static final String SYMBOL_ALPHA_MINUS = "-";
	public static final String SYMBOL_BETA_PLUS = "/";
	public static final String SYMBOL_BETA_MINUS = "\\";
	public static final String SYMBOL_GAMMA_PLUS = ".";
	public static final String SYMBOL_GAMMA_MINUS = ",";
	public static final String SYMBOL_COLOR_PLUS = "*";
	public static final String SYMBOL_COLOR_MINUS = "#";
	public static final String[] SYMBOLJ = {
		SYMBOL_PUSH,
		SYMBOL_POP,
		SYMBOL_JUMP,
		SYMBOL_PAINT,
		SYMBOL_ALPHA_MINUS,
		SYMBOL_ALPHA_PLUS,
		SYMBOL_BETA_MINUS,
		SYMBOL_BETA_PLUS,
		SYMBOL_GAMMA_MINUS,
		SYMBOL_GAMMA_PLUS,
		SYMBOL_COLOR_PLUS,
		SYMBOL_COLOR_MINUS,
	};
	protected static double pipi = Math.PI / 180.0f;

	protected LSysState state;
	protected List<LSysState> stack;
	protected LSystem lsys;
	protected String rest;

	/**
	 * constructor.
	 */
	public LSystemFactory() {
		this(null);
	}

	/**
	 * constructor.
	 * @param lsys {@link LSistemo}
	 */
	public LSystemFactory(LSystem lsys) {
		this.setLSystem(lsys);
	}

	public void setLSystem(LSystem lsys) {
		this.lsys = lsys;
	}

	public LSystem getLSystem() {
		return this.lsys;
	}

	protected void start() {
		this.state = new LSysState();
		this.stack = new ArrayList<LSysState>();
		if (lsys != null) {
			lsys.doIt();
			this.rest = new String(lsys.getContent());
		}
	}

	protected boolean hasNext() {
		return (this.rest != null && this.rest.length() > 0);
	}

	protected boolean next(double[] delta) {
		boolean hit = false;
		boolean paint = false;
		int i = 0;
		while ((!hit) && (i < SYMBOLJ.length)) {
			if (rest.startsWith(SYMBOLJ[i])) {
				hit = true;
				String symbol = SYMBOLJ[i];
				rest = rest.substring(symbol.length());
				if (symbol.compareTo(SYMBOL_JUMP) == 0) {
					this.state = this.state.jump();
				} else
				if (symbol.compareTo(SYMBOL_PAINT) == 0) {
					this.state = this.state.jump();
					paint = true;
				} else
				if (symbol.compareTo(SYMBOL_PUSH) == 0) {
					if (this.stack.size() < MAX_STACK) {
						this.stack.add(0,this.state.copy());
					}
				} else
				if (symbol.compareTo(SYMBOL_POP) == 0) {
					if (this.stack.size() > 0) {
						this.state = this.stack.get(0);
						this.stack.remove(0);
					}
				} else
				if (symbol.compareTo(SYMBOL_ALPHA_PLUS) == 0) {
					this.state.rotation[0] += delta[0];
				} else
				if (symbol.compareTo(SYMBOL_ALPHA_MINUS) == 0) {
					this.state.rotation[0] -= delta[0];
				} else
				if (symbol.compareTo(SYMBOL_BETA_PLUS) == 0) {
					this.state.rotation[2] += delta[2];
				} else
				if (symbol.compareTo(SYMBOL_BETA_MINUS) == 0) {
					this.state.rotation[2] -= delta[2];
				} else
				if (symbol.compareTo(SYMBOL_GAMMA_PLUS) == 0) {
					this.state.rotation[1] += delta[1];
				} else
				if (symbol.compareTo(SYMBOL_GAMMA_MINUS) == 0) {
					this.state.rotation[1] -= delta[1];
				}
			}
			i++;
		}
		if (!hit) {
			rest = rest.substring(1);
		}
		return paint;
	}

	/**
	 * @param delta {@link Double}[]
	 * @param color1 {@link Double}[]
	 * @param color2 {@link Double}[]
	 * @return {@link SceneGroup}
	 */
	public SceneGroup sceneLSystem(
		double[] delta,
		double[] color1,
		double[] color2
	) {
		SceneGroup scene = new SceneGroup(
			Util.nul3(),
			Util.nul3(),
			new double[] {0.0f,0.0f,0.0f},
			Util.one3()
		);
		this.start();
		while (this.hasNext()) {
			double[] position = new double[] {
				this.state.position[0],
				this.state.position[1],
				this.state.position[2]
			};
			boolean pentri = next(delta);
			if (pentri) {
				Line nova = new Line(
					Util.nul3(),
					Util.nul3(),
					position,
					state.position,
					color2
				);
				scene.addSubobject(nova);
			}
		}
		return scene;
	}

	/**
	 * builds a tree for physics simulation.
	 * @param directed
	 * @param delta
	 * @return
	 */
	public Tree treeLSystem(boolean directed, double[] delta) {
		Tree tree = new Tree(directed);
		Map<String,TreeNode> vertices = new HashMap<String,TreeNode>();
		List<TreeEdge> edges = new ArrayList<TreeEdge>();
		this.start();
		while (this.hasNext()) {
			double[] position1 = new double[] {
				this.state.position[0],
				this.state.position[1],
				this.state.position[2]
			};
			boolean show = next(delta);
			if (show) {
				// first node
				String pos1 = Util.toString(position1);
				TreeNode node1 = vertices.get(pos1);
				if (node1 == null) {
					node1 = new TreeNode(vertices.size());
					vertices.put(pos1,node1);
				}
				// second node
				double[] position2 = new double[] {
					this.state.position[0],
					this.state.position[1],
					this.state.position[2]
				};
				String pos2 = Util.toString(position2);
				TreeNode node2 = vertices.get(pos2);
				if (node2 == null) {
					node2 = new TreeNode(vertices.size());
					vertices.put(pos2,node2);
				}
				// the edge
				TreeEdge edge = new TreeEdge(node1,node2);
				edges.add(edge);
			}
		}
		tree.addNodes(vertices.values());
		tree.addEdges(edges);
		return tree;
	}

	class LSysState {
		double[] position = Util.nul3();
		double[] rotation = Util.nul3();
		double[] color = Util.nul4();
		LSysState jump() {
			LSysState result = new LSysState();
			result.color = this.color.clone();
			result.rotation = this.rotation.clone();
			result.position = new double[3];
			double[] x = new double[] {
				Math.sin(this.rotation[0] * pipi) * Math.sin(2.0f * this.rotation[2] * pipi),
				Math.sin(this.rotation[0] * pipi) * Math.cos(2.0f * this.rotation[2] * pipi),
				Math.cos(this.rotation[0] * pipi),
			};
			result.position[0] = this.position[0] + x[0];
			result.position[1] = this.position[1] + x[1];
			result.position[2] = this.position[2] + x[2];
			return result;
		}
		LSysState copy() {
			LSysState result = new LSysState();
			result.color[0] = this.color[0];
			result.color[1] = this.color[1];
			result.color[2] = this.color[2];
			result.color[3] = this.color[3];
			result.position[0] = this.position[0];
			result.position[1] = this.position[1];
			result.position[2] = this.position[2];
			result.rotation[0] = this.rotation[0];
			result.rotation[1] = this.rotation[1];
			result.rotation[2] = this.rotation[2];
			return result;
		}
	}
}
