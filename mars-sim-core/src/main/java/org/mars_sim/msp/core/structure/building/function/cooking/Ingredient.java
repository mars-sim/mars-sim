/**
 * Mars Simulation Project
 * Ingredient.java
 * @version 3.07 2014-12-02
 * @author Manny Kung
 */

package org.mars_sim.msp.core.structure.building.function.cooking;

import java.io.Serializable;

// 2014-11-29 Created Ingredient()
public class Ingredient implements Serializable {
	String name;
	double amount;
	

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	Ingredient(String name, double amount) {
			this.name = name;
			this.amount = amount;
	}
	    public String getName() {
	    		return name;
	    	}
	    public double getAmount() {
	    		return amount;
	    	}
}