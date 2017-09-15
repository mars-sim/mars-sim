/**
 * Mars Simulation Project
 * Ingredient.java
 * @version 3.1.0 2017-09-14
 * @author Manny Kung
 */

package org.mars_sim.msp.core.structure.building.function.cooking;

import java.io.Serializable;

import org.mars_sim.msp.core.resource.AmountResource;

// 2014-11-29 Created Ingredient()
public class Ingredient implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private boolean isItAvailable;	

	private int id;

	private double proportion;
	private double ingredientDryMass;

	private AmountResource amountResource;

	Ingredient(int id, AmountResource amountResource, double proportion) {//String name, , boolean isItAvailable) {
		this.id = id;
		this.amountResource = amountResource;
		//this.name = name;
		this.proportion = proportion;
		//this.isItAvailable = isItAvailable;
	}
	    public String getName() {
    		return amountResource.getName();
    	}
	    
	    public AmountResource getAR() {
	    	return amountResource;
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
	    public void setDryMass(double ingredientDryWeight) {
	    	this.ingredientDryMass = ingredientDryWeight;
	    }
	    public double getDryMass() {

	    	return ingredientDryMass;
	    }
	    
	    public String toString() {
    		return amountResource.getName();
    	}
	    
	    public AmountResource toAR() {
	    	return amountResource;
	    }
}