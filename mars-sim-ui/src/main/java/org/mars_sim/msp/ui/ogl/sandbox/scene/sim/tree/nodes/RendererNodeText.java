package org.mars_sim.msp.ui.ogl.sandbox.scene.sim.tree.nodes;

import org.mars_sim.msp.ui.ogl.sandbox.scene.Util;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.force.nodes.NodeForceObject;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sim.tree.TreeNode;
import org.mars_sim.msp.ui.ogl.sandbox.scene.text.EnumTextOrientation;
import org.mars_sim.msp.ui.ogl.sandbox.scene.text.TextOutline;

/**
 * @author stpa
 * 2014-02-24
 */
public class RendererNodeText
extends RendererNodeAbstract {

	protected String parameterName;

	public RendererNodeText(String parameterName) {
		this.setParameterName(parameterName);
	}

	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	public String getParametronomo() {
		return this.parameterName;
	}

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
	public void update(long deltaTime) {
		text.update(deltaTime);
	}

	@Override
	public void render(TreeNode treeNode, NodeForceObject nodofortoobjekto) {
		double[] pozicio = nodofortoobjekto.getPosition();
		gl.glPushMatrix();
		gl.glTranslated(pozicio[0],pozicio[1],pozicio[2]);
		text.setCaption(treeNode.getParamString(parameterName));
		text.render(this.gl);
		gl.glPopMatrix();
	}
}
