package org.mars_sim.msp.ui.ogl.sandbox.scene.sim;

import java.util.Map.Entry;

import javax.media.opengl.GL2;

import org.mars_sim.msp.ui.ogl.sandbox.scene.RotatingObjectAbstract;
import org.mars_sim.msp.ui.ogl.sandbox.scene.Util;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.edges.EdgeForceObject;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.nodes.NodeForceObject;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.tree.TreeEdge;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.tree.TreeNode;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.tree.edges.RendererEdgeInterface;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.tree.nodes.RendererNodeInterface;

/**
 * @author stpa
 * 2014-02-24
 */
public class SimulationContainer
extends RotatingObjectAbstract {

	protected Simulation simulation;
	protected RendererNodeInterface rendererNodes;
	protected RendererEdgeInterface rendererEdges;
	protected double[] color = Util.one4();

	public SimulationContainer(
		Simulation simulation,
		double[] translation,
		double[] rotation,
		double[] deltaRotation,
		RendererNodeInterface rn,
		RendererEdgeInterface re
	) {
		super(translation, rotation, deltaRotation);
		this.setRendererNode(rn);
		this.setRendererEdge(re);
		this.setSimulation(simulation);
	}

	@Override
	public void preinit(GL2 gl) {
		super.preinit(gl);
		if (this.rendererNodes != null) {
			this.rendererNodes.setGl(gl);
			this.rendererNodes.init(this.simulation);
		}
	}

	@Override
	public void preupdate(long delta_tempo) {
		this.simulation.step();
		if (this.rendererNodes != null) {
			rendererNodes.update(delta_tempo);
		}
	}

	@Override
	public void prerender(GL2 gl) {
		super.prerender(gl);
		gl.glColor4d(
			this.color[0],
			this.color[1],
			this.color[2],
			this.color[3]
		);
		if (this.rendererNodes != null) {
			this.rendererNodes.prerender();
			for (Entry<TreeNode,NodeForceObject> ero : this.getSimulation().getMapNodes().entrySet()) {
				TreeNode arbonodo = ero.getKey();
				NodeForceObject nodofortoobjekto = ero.getValue();
				this.rendererNodes.render(arbonodo,nodofortoobjekto);
			}
			this.rendererNodes.postrender();
		}
		if (this.rendererEdges != null) {
			this.rendererEdges.setGl(gl);
			for (Entry<TreeEdge,EdgeForceObject> ero : this.getSimulation().getMapEdges().entrySet()) {
				TreeEdge arboeĝo = ero.getKey();
				EdgeForceObject eĝofortoobjekto = ero.getValue();
				this.rendererEdges.render(arboeĝo,eĝofortoobjekto);
			}
		}
	}

	public void setRendererNode(RendererNodeInterface rn) {
		this.setParam(PARAM_NODE_RENDERER,rn);
		this.rendererNodes = rn;
	}

	public void setRendererEdge(RendererEdgeInterface re) {
		this.setParam(PARAM_EDGE_RENDERER,re);
		this.rendererEdges = re;
	}

	public void setSimulation(Simulation simulation) {
		this.simulation = simulation;
	}

	public Simulation getSimulation() {
		return this.simulation;
	}

	@SuppressWarnings("unchecked")
	public Class<? extends RendererEdgeInterface> getRendererEdge() {
		return (Class<? extends RendererEdgeInterface>) this.getParam(PARAM_EDGE_RENDERER);
	}

	@SuppressWarnings("unchecked")
	public Class<? extends RendererNodeInterface> getRendererNodo() {
		return (Class<? extends RendererNodeInterface>) this.getParam(PARAM_NODE_RENDERER);
	}
}
