/**
 * Mars Simulation Project
 * BoundedObject.java
 * @version 3.1.0 2017-11-06
 * @author Manny Kung
 */

package org.mars_sim.msp.core;

public class BoundedObject implements LocalBoundedObject{

	double w;
	double l;
	double x;
	double y;
	double f;

	public BoundedObject(double x, double y, double w, double l, double f) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.l = l;
		this.f = f;
	}

	@Override
	public double getXLocation() {
		return x;
	}

	@Override
	public double getYLocation() {
		return y;
	}

	@Override
	public double getWidth() {
		return w;
	}

	@Override
	public double getLength() {
		return l;
	}

	@Override
	public double getFacing() {
		return f;
	}

}
