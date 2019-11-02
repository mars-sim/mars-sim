/**
 * Mars Simulation Project
 * CollectionSite.java
 * @version 3.1.0 2019-10-29
 * @author Manny Kung
 */

package org.mars_sim.msp.core.mars;

import java.io.Serializable;

import org.mars_sim.msp.core.Coordinates;

public class CollectionSite extends Site implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private double iceCollectionRate = -1;
	
	private double regolithCollectionRate = -1;
	
	private double estimatedIceVolume;
	
	public CollectionSite(Coordinates location) {
		super(location);
	}

	public double getIceCollectionRate() {
		return iceCollectionRate;
	}

	public void setIceCollectionRate(double iceCollectionRate) {
		this.iceCollectionRate = iceCollectionRate;
	}

	public double getRegolithCollectionRate() {
		return regolithCollectionRate;
	}

	public void setRegolithCollectionRate(double regolithCollectionRate) {
		this.regolithCollectionRate = regolithCollectionRate;
	}

	public double getEstimatedIceVolume() {
		return estimatedIceVolume;
	}

	public void setEstimatedIceVolume(double estimatedVolume) {
		this.estimatedIceVolume = estimatedVolume;
	}

	/**
	 * Returns true if the collection site have the same coordinates
	 * 
	 * @param o
	 * @return true if matched, false otherwise
	 */
	public boolean equals(Object o) {
		if (this == o) return true;
		if ((o != null) && (o instanceof CollectionSite)) {
			CollectionSite s = (CollectionSite) o;
			if (this.location.equals(s.getLocation())
					&& this.iceCollectionRate == s.getIceCollectionRate()
					&& this.regolithCollectionRate == s.getRegolithCollectionRate())
				return true;
		}

		return false;
	}
	
	/**
	 * Gets the hash code for this object.
	 * 
	 * @return hash code.
	 */
	public int hashCode() {
		return (int)(Math.abs(iceCollectionRate) + Math.abs(regolithCollectionRate)) + location.hashCode();
	}

}
