package org.mars_sim.msp.ui.ogl.sandbox.scene.sphere;

import org.mars_sim.msp.ui.ogl.sandbox.scene.RotatingObjectAbstract;

/**
 * an abstract sphere.
 * @author stpa
 */
public abstract class SphereAbstract
extends RotatingObjectAbstract {

	public SphereAbstract(
		double[] center,
		double[] rotation,
		double[] deltaRotation,
		double radius
	) {
		super(center,rotation, deltaRotation);
		this.setRadius(radius);
	}

	public double getDiameter() {
		return 2.0f * this.getRadius();
	}
	
	public void setDiameter(double diameter) {
		this.setRadius(0.5f * diameter);
	}

	public double getEquator() {
		return Math.PI * 2.0f * this.getRadius();
	}

	public void setEquator(double equator) {
		this.setRadius(0.5f * equator / Math.PI);
	}
	
	public double getVolume() {
		return 4.0f * Math.PI * Math.pow(this.getRadius(),3) / 3.0f;
	}
	
	public void setVolume(double volume) {
		this.setRadius(Math.pow(3.0f * volume / (4.0f * Math.PI),1.0f / 3.0f)); 
	}
	
	public double[] getCenter() {
		return this.getTranslation();
	}

	public double getRadius() {
		return this.getParamDouble(PARAM_RADIUS);
	}

	public void setCenter(double[] center) {
		this.setTranslation(center);
	}

	public void setRadius(double radius) {
		this.setParam(PARAM_RADIUS,radius);
	}
}
