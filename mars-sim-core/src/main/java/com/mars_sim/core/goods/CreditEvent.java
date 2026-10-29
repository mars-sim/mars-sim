/*
 * Mars Simulation Project
 * CreditEvent.java
 * @date 2024-08-10
 * @author Scott Davis
 */

package com.mars_sim.core.goods;

import java.util.EventObject;

import com.mars_sim.core.structure.Settlement;

/**
 * A credit change event.
 */
public class CreditEvent extends EventObject {

    private static final long serialVersionUID = 2L;
    
	// Data members
	private int settlement1;
	private int settlement2;
	private double credit;
	
	/**
	 * Constructor.
	 * 
	 * @param settlement1 the first settlement.
	 * @param settlement2 the second settlement.
	 * @param credit the credit amount (VP).
	 */
	public CreditEvent(Settlement settlement1, Settlement settlement2, double credit) {
		// Use EventObject constructor
		super(settlement1.getCreditManager());
		
		this.settlement1 = settlement1.getIdentifier();
		this.settlement2 = settlement2.getIdentifier();
		this.credit = credit;
	}

	/**
	 * Gets the first settlement.
	 * 
	 * @return settlement.
	 */
	public int getSettlement1() {
		return settlement1;
	}
	
	/**
	 * Gets the second settlement.
	 * 
	 * @return settlement.
	 */
	public int getSettlement2() {
		return settlement2;
	}
	
	/**
	 * Gets the credit amount.
	 * 
	 * @return credit amount (VP).
	 */
	public double getCredit() {
		return credit;
	}	
}
