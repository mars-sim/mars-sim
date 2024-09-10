/*
 * Mars Simulation Project
 * ProcessItem.java
 * @date 2024-02-25
 * @author Barry Evans
 */
package com.mars_sim.core.process;

import java.io.Serializable;

import com.mars_sim.core.resource.ItemType;

/**
 * A Process input or output item.
 * Note: Ideally this should be a simple 'record' but big impact with the method name change.
 */
public class ProcessItem implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	private String name;
	private ItemType type;
	private double amount;

    
	public ProcessItem(String name, ItemType type, double amount) {
        this.name = name;
        this.type = type;
        this.amount = amount;
    }

    public String getName() {
		return name;
	}

	public ItemType getType() {
		return type;
	}

	public double getAmount() {
		return amount;
	}

	@Override
	public String toString() {
		return name;
	}

    /**
     * Checks if another object is equal to this one.
     */
    public boolean equals(Object object) {
        boolean result = false;
        if (object instanceof ProcessItem) {
            ProcessItem item = (ProcessItem) object;
            result = (name.equals(item.getName())
                    && type.equals(item.getType())
                    && (amount == item.getAmount()));
        }

        return result;
    }

    /**
     * Gets the hash code for this object.
     * 
     * @return hash code.
     */
    public int hashCode() {
        return name.hashCode();
    }
}
