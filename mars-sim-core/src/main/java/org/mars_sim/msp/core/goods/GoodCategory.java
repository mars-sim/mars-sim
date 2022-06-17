/**
 * Mars Simulation Project
 * GoodCategory.java
 * @version 3.2.0 2021-06-20
 * @author stpa
 */
package org.mars_sim.msp.core.goods;

import org.mars_sim.msp.core.Msg;

public enum GoodCategory {

	AMOUNT_RESOURCE 	(Msg.getString("GoodCategory.amountResource")), //$NON-NLS-1$
	ITEM_RESOURCE 		(Msg.getString("GoodCategory.itemResource")), //$NON-NLS-1$
	EQUIPMENT 			(Msg.getString("GoodCategory.equipment")), //$NON-NLS-1$
	CONTAINER 			(Msg.getString("GoodCategory.container")), //$NON-NLS-1$
	VEHICLE 			(Msg.getString("GoodCategory.vehicle")), //$NON-NLS-1$
	ROBOT 				(Msg.getString("GoodCategory.robot")); //$NON-NLS-1$
	
	private String msgKey;

	private GoodCategory(String msgKey) {
		this.msgKey = msgKey;
	}

	public String getMsgKey() {
		return this.msgKey;
	}
}
