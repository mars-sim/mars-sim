/**
 * Mars Simulation Project
 * Ingredient.java
 * @version 3.07 2014-12-07
 * @author Manny Kung
 */

package org.mars_sim.msp.core.structure.building.function.cooking;

import java.io.Serializable;

// 2014-11-29 Created Ingredient()
public class Ingredient implements Serializable {
	int id;
	String name;
	double proportion;
	

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	Ingredient(int id, String name, double proportion) {
			this.name = name;
			this.proportion = proportion;
	}
	    public String getName() {
	    		return name;
	    	}
	    public double getProportion() {
	    		return proportion;
	    	}
	    public int getID() {
    		return id;
    	}
}