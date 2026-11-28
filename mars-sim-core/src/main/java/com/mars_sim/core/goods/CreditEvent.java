/*
 * Mars Simulation Project
 * CreditEvent.java
 * @date 2024-08-10
 * @author Scott Davis
 */

package com.mars_sim.core.goods;

import java.util.EventObject;

import com.mars_sim.core.authority.Authority;

/**
 * A credit change event.
 */
public class CreditEvent extends EventObject {

    private static final long serialVersionUID = 2L;
    
	// Data members
    private Authority authority1;
    private Authority authority2;
	private double credit;
	
	/**
	 * Constructor.
	 * 
     * @param authority1 the first authority.
     * @param authority2 the second authority.
	 * @param credit the credit amount (VP).
	 */
    public CreditEvent(Authority authority1, Authority authority2, double credit) {
		// Use EventObject constructor
    	super(authority1);
		
    	this.authority1 = authority1;
    	this.authority2 = authority2;
		this.credit = credit;
	}

	/**
     * Gets the first authority.
	 * 
     * @return first authority.
	 */
    public Authority getAuthority1() {
    	return authority1;
	}
	
	/**
     * Gets the second authority.
	 * 
     * @return second authority.
	 */
    public Authority getAuthority2() {
    	return authority2;
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
