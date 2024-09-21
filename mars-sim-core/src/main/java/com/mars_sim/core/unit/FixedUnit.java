/*
 * Mars Simulation Project
 * FixedUnit.java
 * @date 2024-09-15
 * @author Barry Evans
 */
package com.mars_sim.core.unit;

import com.mars_sim.core.Unit;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.LocalBoundedObject;
import com.mars_sim.core.structure.Settlement;

/**
 * Represent a Unit that is at a Fixed location in a Settlement
 */
public abstract class FixedUnit extends Unit 
    implements LocalBoundedObject {
    
    private Settlement owner;

    /**
	 * Constructor.
	 * 
	 * @param name the name of the unit
	 * @param owner the unit's location
	 */
	protected FixedUnit(String name, Settlement owner) {
		super(name, owner.getCoordinates());

        this.owner = owner;

        // TODO Place holder; once completed this can be removed
		setContainerID(owner.getIdentifier());
	}

    /**
     * Get the coordinates of this fixed unit on the surface.
     * @return Coordinates of the owning Settlement
     */
    @Override
    public Coordinates getCoordinates() {
        return owner.getCoordinates();
    }

    /**
     * Settlement that is associated with this FixedUnit.
     */
    @Override
    public Settlement getAssociatedSettlement() {
        return owner;
    }   
    
    /**
     * Get the settlement of this fixed unit which is same as associated.
     * This should be deprecated and removed
     */
    public Settlement getSettlement() {
        return getAssociatedSettlement();
    }

    /**
	 * Is this unit inside a settlement
     * TODO This will be removed once completed
	 *
	 * @return true if the unit is inside a settlement
	 */
	@Override
	public boolean isInSettlement() {
		return true;
	}

    /**
	 * This method return the context of this FixedUnit which is always the parent 
	 */
	@Override
	public String getContext() {
		return owner.getName();
	}
}
