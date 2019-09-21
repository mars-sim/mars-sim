/**
 * Mars Simulation Project
 * FoodProductionProcessItem.java
 * @version 3.1.0 2019-09-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.foodProduction;

import java.io.Serializable;

import org.mars_sim.msp.core.resource.ItemType;

/**
 * A Food Production process input or output item.
 */
public class FoodProductionProcessItem implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	private String name;
	private ItemType type;
	private double amount;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ItemType getType() {
		return type;
	}

	public void setType(ItemType type) {
		this.type = type;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
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
        if (object instanceof FoodProductionProcessItem) {
            FoodProductionProcessItem item = (FoodProductionProcessItem) object;
            result = true;
            if (!name.equals(item.getName())) {
                result = false;
            }
            else if (!type.equals(item.getType())) {
                result = false;
            }
            else if (amount != item.getAmount()) {
                result = false;
            }
        }

        return result;
    }

    /**
     * Gets the hash code for this object.
     * @return hash code.
     */
    public int hashCode() {
		// 2017-05-09 Upgrade from StringBuffer to StringBuilder
        StringBuilder sb = new StringBuilder("");
        sb.append(name);
        sb.append(type);
        sb.append(amount);
        return sb.hashCode();
    }
}