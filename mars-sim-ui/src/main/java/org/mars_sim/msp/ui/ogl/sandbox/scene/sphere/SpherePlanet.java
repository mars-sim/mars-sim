package org.mars_sim.msp.ui.ogl.sandbox.scene.sphere;

import javax.media.opengl.GL2;

import org.mars_sim.msp.ui.ogl.sandbox.scene.Util;
import org.mars_sim.msp.ui.ogl.sandbox.scene.text.EnumTextOrientation;
import org.mars_sim.msp.ui.ogl.sandbox.scene.text.TextOutline;

public class SpherePlanet
extends SphereTextured {

	protected TextOutline caption;

	public SpherePlanet(
		double[] center,
		double[] rotation,
		double[] deltaRotation,
		double radius,
		String texture,
		String caption,
		double[] color
	) {
		super(center, rotation, deltaRotation, radius, texture, color);
		this.caption = new TextOutline(
			new double[] {0d,0d,1.1d}, // move it just above the sphere
			new double[] {90d,0d,0d},
			new double[] {0d,90d,0d}, // set it spinning
			Util.one3(), // keep scale of parent
			caption,
			EnumTextOrientation.CENTER,
			color // keep color of parent
		);
		this.setCaption(caption);
	}

	public void prerender(GL2 gl) {
		super.prerender(gl);
		this.caption.render(gl);
	}

	public void update(long deltaTime) {
		super.update(deltaTime);
		this.caption.update(deltaTime);
	}

	public void close(GL2 gl) {
		super.close(gl);
		this.caption.close(gl);
	}

	public final void setCaption(String caption) {
		this.setParam(PARAM_CAPTION,caption);
		this.caption.setCaption(caption);
	}

	public final String getCaption() {
		return this.getParamString(PARAM_CAPTION);
	}
}
