package org.mars_sim.msp.ui.ogl.sandbox.scene.sim.tree.edges;

import javax.media.opengl.GL2;

import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.Simulation;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.edges.EdgeForceObject;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.tree.TreeEdge;

/**
 * @author stpa
 * 2014-02-24
 */
public interface RendererEdgeInterface {

	public void setGl(GL2 gl);
	public void render(TreeEdge treeEdge, EdgeForceObject edgeForceObject);
	public void init(Simulation sim);
	public void update(long deltaTime);
	public void prerender();
	public void postrender();
}
