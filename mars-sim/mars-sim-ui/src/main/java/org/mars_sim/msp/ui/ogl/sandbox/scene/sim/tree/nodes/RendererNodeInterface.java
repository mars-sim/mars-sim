package org.mars_sim.msp.ui.ogl.sandbox.scene.sim.tree.nodes;

import javax.media.opengl.GL2;

import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.Simulation;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.nodes.NodeForceObject;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.tree.TreeNode;

/**
 * @author stpa
 * 2014-02-24
 */
public interface RendererNodeInterface {

	public void setGl(GL2 gl);
	public void render(TreeNode treeNode, NodeForceObject nodeForceObject);
	public void init(Simulation sim);
	public void update(long deltaTime);
	public void prerender();
	public void postrender();
}
