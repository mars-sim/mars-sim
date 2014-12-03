/**
 * Mars Simulation Project
 * Ingredient.java
 * @version 3.07 2014-12-02
 * @author Manny Kung
 */

package org.mars_sim.msp.core.structure.building.function.cooking;

// 2014-11-29 Created Ingredient()
public class Ingredient {
	String name;
	double amount;
	
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