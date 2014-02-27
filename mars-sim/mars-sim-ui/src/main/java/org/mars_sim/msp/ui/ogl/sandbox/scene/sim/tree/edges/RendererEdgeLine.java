package org.mars_sim.msp.ui.ogl.sandbox.scene.sim.tree.edges;

import javax.media.opengl.GL;

import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.edges.EdgeForceObject;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.nodes.NodeForceObject;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.tree.TreeEdge;

/**
 * @author stpa
 * 2014-02-24
 */
public class RendererEdgeLine
extends RendererEdgeAbstract {

	@Override
	public void render(TreeEdge treeEdge, EdgeForceObject edgeForceObject) {
		NodeForceObject node1 = edgeForceObject.getNode1();
		NodeForceObject node2 = edgeForceObject.getNode2();
		double[] position1 = node1.getPosition();
		double[] position2 = node2.getPosition();
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3d(position1[0],position1[1],position1[2]);
		gl.glVertex3d(position2[0],position2[1],position2[2]);
		gl.glEnd();
	}
}
