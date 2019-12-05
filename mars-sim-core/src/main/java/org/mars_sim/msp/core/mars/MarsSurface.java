/**
 * Mars Simulation Project
 * MarsSurface.java
 * @version 3.1.0 2018-11-17
 * @author Manny Kung
 */

package org.mars_sim.msp.core.mars;

import java.io.Serializable;

import org.mars_sim.msp.core.Unit;

/**
 * MarsSurface is the object unit that represents the surface of Mars
 */
public class MarsSurface extends Unit implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 123L;
	
	/** Unique identifier. */
	/** The unit count for this robot. */
	private static int uniqueCount = Unit.MARS_SURFACE_UNIT_ID;
	
	private static final String NAME = "Mars Surface";
	
	private int identifier;

	/**
	 * Must be synchronised to prevent duplicate ids being assigned via different
	 * threads.
	 * 
	 * @return
	 */
	private static synchronized int getNextIdentifier() {
		return uniqueCount;
	}
	
	
	/**
	 * Get the unique identifier for this settlement
	 * 
	 * @return Identifier
	 */
	public int getIdentifier() {
		return identifier;
	}
	
	public void incrementID() {
		// Gets the identifier
		this.identifier = getNextIdentifier();
	}
	
	public MarsSurface() {
		super(NAME, null);
//		this.identifier = getNextIdentifier();
		
		setContainerUnit(null);
		setContainerID(Unit.OUTER_SPACE_UNIT_ID);
				
//		System.out.println("MarsSurface Container ID : " + getContainerID());
		getInventory().addGeneralCapacity(Double.MAX_VALUE);
	}
	
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		Unit u = (Unit) obj;
		return this.getName().equals(u.getName())
				&& this.getIdentifier() == ((Unit) obj).getIdentifier() ;
	}
	
	/**
	 * Gets the hash code for this object.
	 * 
	 * @return hash code.
	 */
	public int hashCode() {
		int hashCode = (int) ( (1.0 + getName().hashCode()) * (1.0 + getIdentifier()));
		return hashCode;
	}
	
//	@Override
//	public String toString() {
//		return NAME;
//	}
}
