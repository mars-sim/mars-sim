package org.mars_sim.msp.core;

import java.io.Serializable;

/**
 * This represent a position within the local frame of reference of a Unit, e.g. Vehicle, Building
 */
public class LocalPosition implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double x;
	private double y;
	
	public LocalPosition(double x, double y) {
		super();
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}
 
    /**
     * Returns the distance to another point
     *
     * @param other Other location
     * @return the distance between the two sets of specified
     * coordinates.
     * @since 1.2
     */
    public double getDistanceTo(LocalPosition other)
    {
        double x1 = x - other.x;
        double y1 = y - other.y;
        return Math.sqrt(x1 * x1 + y1 * y1);
    }
    
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LocalPosition other = (LocalPosition) obj;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[x=" + x + ", y=" + y + "]";
	}
}
