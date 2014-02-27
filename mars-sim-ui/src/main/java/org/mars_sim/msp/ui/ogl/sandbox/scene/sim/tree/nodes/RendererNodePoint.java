package org.mars_sim.msp.ui.ogl.sandbox.scene.sim.tree.nodes;

import javax.media.opengl.GL;

import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.nodes.NodeForceObject;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.tree.TreeNode;

/**
 * @author stpa
 * 2014-02-24
 */
public class RendererNodePoint
extends RendererNodeAbstract {

	/** constructor. */
	public RendererNodePoint() {
		// do nothing.
	}

	public void render(TreeNode treeNode, NodeForceObject nodeForceObject) {
		double[] position = nodeForceObject.getPosition();
		gl.glVertex3d(position[0],position[1],position[2]);
	}

	public void prerender() {
		gl.glPointSize(3.0f);
		gl.glBegin(GL.GL_POINTS);
	}

	public void postrender() {
		gl.glEnd();
		gl.glPointSize(1.0f);
	}
}
