/**
 * Mars Simulation Project
 * MarsSurface.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.mars;

import java.io.Serializable;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;

/**
 * MarsSurface is the object unit that represents the surface of Mars
 */
public class MarsSurface extends Unit implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 123L;
	
	
	private static final String NAME = "Mars Surface";
	
	public MarsSurface() {
		super(NAME, null);
		
		setContainerUnit(null);
		setContainerID(Unit.OUTER_SPACE_UNIT_ID);
				
		getInventory().addGeneralCapacity(Double.MAX_VALUE);
		
		// This is hack playing on how the identifiers are created
		if (getIdentifier() != Unit.MARS_SURFACE_UNIT_ID) {
			throw new IllegalStateException("MarsSurface has wrong ID: " + getIdentifier());
		}
	}
	
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		Unit u = (Unit) obj;
		return this.getName().equals(u.getName())
				&& this.getIdentifier() == ((Unit) obj).getIdentifier() ;
	}
	
	@Override
	protected UnitType getUnitType() {
		return UnitType.PLANET;
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
}
