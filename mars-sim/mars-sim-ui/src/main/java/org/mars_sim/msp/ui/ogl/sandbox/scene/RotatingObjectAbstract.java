package org.mars_sim.msp.ui.ogl.sandbox.scene;

public abstract class RotatingObjectAbstract
extends SceneObjectAbstract {

	public RotatingObjectAbstract(
		double[] translation, double[] rotation, double[] deltaRotation
	) {
		super(translation, rotation);
		this.setDeltaRotation(deltaRotation);
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
}
