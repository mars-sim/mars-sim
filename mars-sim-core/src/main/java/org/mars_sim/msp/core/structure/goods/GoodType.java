/**
 * Mars Simulation Project
 * GoodType.java
 * @version 3.1.0 2018-12-29
 * @author stpa
 */
package org.mars_sim.msp.core.structure.goods;

public enum GoodType {

	AMOUNT_RESOURCE ("GoodType.amountResource"), //$NON-NLS-1$
	ITEM_RESOURCE ("GoodType.itemResource"), //$NON-NLS-1$
	EQUIPMENT ("GoodType.equipment"), //$NON-NLS-1$
	VEHICLE ("GoodType.vehicle"); //$NON-NLS-1$
//  ROBOT ("GoodType.robot"); //$NON-NLS-1$
	
	private String msgKey;

	private GoodType(String msgKey) {
		this.msgKey = msgKey;
	}

	public String getMsgKey() {
		return this.msgKey;
	}
}
