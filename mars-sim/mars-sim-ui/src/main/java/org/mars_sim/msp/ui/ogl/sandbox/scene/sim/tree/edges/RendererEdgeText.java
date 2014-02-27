package org.mars_sim.msp.ui.ogl.sandbox.scene.sim.tree.edges;

import javax.media.opengl.GL;

import org.mars_sim.msp.ui.ogl.sandbox.scene.Util;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.edges.EdgeForceObject;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.nodes.NodeForceObject;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.tree.TreeEdge;
import org.mars_sim.msp.ui.ogl.sandbox.scene.text.EnumTextOrientation;
import org.mars_sim.msp.ui.ogl.sandbox.scene.text.TextOutline;

/**
 * @author stpa
 * 2014-02-24
 */
public class RendererEdgeText
extends RendererEdgeAbstract {

	protected String parameterName;

	protected TextOutline text = new TextOutline(
		 Util.nul3(),
		 Util.nul3(),
		 new double[] {0f,66f,0f},
		 Util.one3(),
		 "",
		 EnumTextOrientation.CENTER,
		 Util.one4()
	);

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

		double[] position = Util.avg3(position1,position2);
		gl.glPushMatrix();
		gl.glTranslated(position[0],position[1],position[2]);
		text.setCaption(treeEdge.getParamString(parameterName));
		text.render(this.gl);
		gl.glPopMatrix();
	}

	public RendererEdgeText(String parametronomo) {
		this.setParametronomo(parametronomo);
	}

	public void setParametronomo(String parametronomo) {
		this.parameterName = parametronomo;
	}

	public String getParametronomo() {
		return this.parameterName;
	}

	@Override
	public void update(long deltaTime) {
		text.update(deltaTime);
	}
}
