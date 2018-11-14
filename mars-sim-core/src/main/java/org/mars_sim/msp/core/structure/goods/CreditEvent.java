/**
 * Mars Simulation Project
 * CreditEvent.java
 * @version 3.1.0 2014-12-06
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure.goods;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.structure.Settlement;

import java.io.Serializable;
import java.util.EventObject;

/**
 * A credit change event.
 */
public class CreditEvent extends EventObject implements Serializable {

    private static final long serialVersionUID = 2L;
    
	// Data members
	private Settlement settlement1;
	private Settlement settlement2;
	private double credit;
	
	/**
	 * Constructor
	 * @param settlement1 the first settlement.
	 * @param settlement2 the second settlement.
	 * @param credit the credit amount (VP).
	 */
	public CreditEvent(Settlement settlement1, Settlement settlement2, double credit) {
		// Use EventObject constructor
		super(Simulation.instance().getCreditManager());
		
		this.settlement1 = settlement1;
		this.settlement2 = settlement2;
		this.credit = credit;
	}

	/**
	 * Gets the first settlement.
	 * 
	 * @return settlement.
	 */
	public Settlement getSettlement1() {
		return settlement1;
	}
	
	/**
	 * Gets the second settlement.
	 * @return settlement.
	 */
	public Settlement getSettlement2() {
		return settlement2;
	}
	
	/**
	 * Gets the credit amount.
	 * @return credit amount (VP).
	 */
	public double getCredit() {
		return credit;
	}
}