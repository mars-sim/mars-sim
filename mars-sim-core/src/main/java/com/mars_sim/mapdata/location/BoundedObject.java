/*
 * Mars Simulation Project
 * BoundedObject.java
 * @date 2021-06-20
 * @author Manny Kung
 */

package com.mars_sim.mapdata.location;

import java.awt.geom.Rectangle2D;

public class BoundedObject implements LocalBoundedObject {

	private static final long serialVersionUID = 1L;
	
	double w;
	double l;
	double f;
	private LocalPosition loc;

	/**
	 * Constructor 1: For panels, Resupply class, and maven testing only.
	 * 
	 * @param x
	 * @param y
	 * @param w
	 * @param l
	 * @param f
	 */
	public BoundedObject(double x, double y, double w, double l, double f) {
		this(new LocalPosition(x, y), w, l, f);
	}

	/**
	 * Constructor 2 : For start of sim.
	 * 
	 * @param loc
	 * @param w
	 * @param l
	 * @param f
	 */
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
	 * Gets the local position within the context of an existing Unit.
	 * 
	 * @return
	 */
	public LocalPosition getPosition() {
		return loc;
	}
	
	/**
	 * Are two rectangles collided ? 
	 * Note: assume no rotation
	 * 
	 * @param o0
	 * @param o1
	 * @return
	 */
	public static boolean isCollided(BoundedObject o0, BoundedObject o1) {
		Rectangle2D rectA = new Rectangle2D.Double(o0.getXLocation() - (o0.getWidth() / 2D),
				o0.getYLocation() - (o0.getLength() / 2D), o0.getWidth(), o0.getLength());
		Rectangle2D rectB = new Rectangle2D.Double(o1.getXLocation() - (o1.getWidth() / 2D),
				o0.getYLocation() - (o1.getLength() / 2D), o1.getWidth(), o1.getLength());
	
		if(rectA.intersects(rectB)) {
			return true;
		}
			
		return false;
	}
	
}
