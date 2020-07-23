/**
 * Mars Simulation Project
 * FoodType.java
 * @version 3.1.1 2020-07-22
 * @author Manny Kung
 */

package org.mars_sim.msp.core.foodProduction;

import org.mars_sim.msp.core.Msg;

public enum FoodType {

	AMOUNT_RESOURCE 	(Msg.getString("FoodType.amountResource")); //$NON-NLS-1$ 
//	ITEM_RESOURCE 		(Msg.getString("FoodType.itemResource")); //$NON-NLS-1$

	private String msgKey;

	private FoodType(String msgKey) {
		this.msgKey = msgKey;
	}

	public String getMsgKey() {
		return this.msgKey;
	}
}
