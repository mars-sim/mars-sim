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
	private int id;
	private String name;
	private double proportion;
	//2014-12-11 Added isItAvailable
	private boolean isItAvailable;	
	private double ingredientDryWeight;

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	Ingredient(int id, String name, double proportion) {//, boolean isItAvailable) {
		this.id = id;
		this.name = name;
		this.proportion = proportion;
		//this.isItAvailable = isItAvailable;
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
		//2014-12-11 Added isItAvailable, setDryWeight
	    public boolean getIsItAvailable() {
	    	return isItAvailable;
	    }
	    public void setIsItAvailable(boolean value) {
	    	isItAvailable = value;
	    }
	    public void setDryWeight(double ingredientDryWeight) {
	    	this.ingredientDryWeight = ingredientDryWeight;
	    }
	    public double getDryWeight() {

	    	return ingredientDryWeight;
	    }
	    public String toString() {
    		return name;
    	}
}