/**
 * Mars Simulation Project
 * FoodType.java
 * @version 3.07 2014-11-25
 * @author Manny Kung
 */

package org.mars_sim.msp.core.foodProduction;

public enum FoodType {

	AMOUNT_RESOURCE ("FoodType.amountResource"); //$NON-NLS-1$
	//ITEM_RESOURCE ("FoodType.itemResource"), //$NON-NLS-1$
	//EQUIPMENT ("FoodType.equipment"); //$NON-NLS-1$
	//VEHICLE ("FoodType.vehicle"); //$NON-NLS-1$

	private String msgKey;

	private FoodType(String msgKey) {
		this.msgKey = msgKey;
	}

	public String getMsgKey() {
		return this.msgKey;
	}
}
