package org.mars_sim.msp.ui.ogl.sandbox.scene.text;

import javax.media.opengl.GL2;

import org.mars_sim.msp.ui.ogl.sandbox.scene.RotatingObjectAbstract;

public abstract class TextAbstract
extends RotatingObjectAbstract {

	/**
	 * constructor.
	 * @param translation {@link Double}[3] anchor point for this text
	 * @param rotation {@link Double}[3]
	 * @param deltaRotation {@link Double}[3]
	 * @param scale {@link Double}[3]
	 * @param caption {@link String}
	 * @param textOrientation {@link EnumTextOrientation}
	 */
	public TextAbstract(
		double[] translation,
		double[] rotation,
		double[] deltaRotation,
		double[] scale,
		String caption,
		EnumTextOrientation textOrientation
	) {
		super(translation, rotation, deltaRotation);
		this.setCaption(caption);
		this.setTextOrientation(textOrientation);
		this.setScale(scale);
	}

	@Override
	protected void preupdate(long deltaTime) {
		double[] rotation = this.getRotation();
		double[] delta = this.getDeltaRotation();
		rotation[0] += delta[0] * ((double) deltaTime * PLANCK_TIME) * PI180;
		rotation[1] += delta[1] * ((double) deltaTime * PLANCK_TIME) * PI180;
		rotation[2] += delta[2] * ((double) deltaTime * PLANCK_TIME) * PI180;
	}

	@Override
	protected void prerender(GL2 gl) {
		super.prerender(gl);
		double scale[] = this.getScale();
		gl.glScaled(
			scale[0],
			scale[1],
			scale[2]
		);
	}

	public void setCaption(String caption) {
		this.setParam(PARAM_CAPTION,caption);
	}

	public String getCaption() {
		return this.getParamString(PARAM_CAPTION);
	}

	public void setTextOrientation(EnumTextOrientation textOrientation) {
		this.setParam(PARAM_TEXT_ORIENTATION, textOrientation);
	}

	public EnumTextOrientation getTextOrientation() {
		return (EnumTextOrientation) this.getParam(PARAM_TEXT_ORIENTATION);
	}

	public void setScale(double[] scale) {
		this.setParam(PARAM_SCALE,scale);
	}

	public double[] getScale() {
		return this.getParamDoubleArray(PARAM_SCALE);
	}
}
