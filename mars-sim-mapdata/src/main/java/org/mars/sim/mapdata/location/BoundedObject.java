/**
 * Mars Simulation Project
 * BoundedObject.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars.sim.mapdata.location;

public class BoundedObject implements LocalBoundedObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	double w;
	double l;
	double f;
	private LocalPosition loc;

	public BoundedObject(double x, double y, double w, double l, double f) {
		this(new LocalPosition(x, y), w, l, f);
	}

	public BoundedObject(LocalPosition loc, double w, double l, double f) {
		this.loc = loc;
		this.w = w;
		this.l = l;
		this.f = f;
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

	/**
	 * Get the local position within the context of an existing Unit.
	 * @return
	 */
	public LocalPosition getPosition() {
		return loc;
	}
}
