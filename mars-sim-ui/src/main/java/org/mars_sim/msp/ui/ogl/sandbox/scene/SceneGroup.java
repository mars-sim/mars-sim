package org.mars_sim.msp.ui.ogl.sandbox.scene;

import javax.media.opengl.GL2;

public class SceneGroup
extends SceneObjectAbstract {

	public SceneGroup(
		double[] translation,
		double[] rotation,
		double[] deltaRotation,
		double[] scale
	) {
		super(translation,rotation);
		this.setDeltaRotation(deltaRotation);
		this.setScale(scale);
	}

	@Override
	public void preinit(GL2 gl) {
		System.out.println("beginning opengl scene initialization.");
		super.preinit(gl);
	}

	@Override
	public void postinit(GL2 gl) {
		super.postinit(gl);
		System.out.println("finished opengl scene initialization.");
	}

	@Override
	public void prerender(GL2 gl) {
		super.prerender(gl);
		double[] skalo = this.getParamDoubleArray(PARAM_SCALE);
		gl.glScaled(skalo[0],skalo[1],skalo[2]);
	}

	@Override
	protected void preupdate(long deltaTime) {
		double[] rotation = this.getRotation();
		double[] delta = this.getDeltaRotation();
		rotation[0] += delta[0] * ((double) deltaTime * PLANCK_TIME) * PI180;
		rotation[1] += delta[1] * ((double) deltaTime * PLANCK_TIME) * PI180;
		rotation[2] += delta[2] * ((double) deltaTime * PLANCK_TIME) * PI180;
	}
	
	public void setDeltaRotation(double[] deltaRotation) {
		this.setParam(PARAM_DELTA_ROTATION,deltaRotation);
	}
	
	public double[] getDeltaRotation() {
		return this.getParamDoubleArray(PARAM_DELTA_ROTATION);
	}
	
	public void setScale(double[] scale) {
		this.setParam(PARAM_SCALE,scale);
	}
}
