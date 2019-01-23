/**
 * Mars Simulation Project
 * CreditEvent.java
 * @version 3.1.0 2014-12-06
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure.goods;

import java.io.Serializable;
import java.util.EventObject;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * A credit change event.
 */
public class CreditEvent extends EventObject implements Serializable {

    private static final long serialVersionUID = 2L;
    
	// Data members
	private int settlement1;
	private int settlement2;
	private double credit;
	
	private static Simulation sim = Simulation.instance();
	private static UnitManager unitManager = sim.getUnitManager();
	private static CreditManager creditManager = sim.getCreditManager();
	
	/**
	 * Constructor
	 * @param settlement1 the first settlement.
	 * @param settlement2 the second settlement.
	 * @param credit the credit amount (VP).
	 */
	public CreditEvent(Settlement settlement1, Settlement settlement2, double credit) {
		// Use EventObject constructor
		super(creditManager);
		
		this.settlement1 = settlement1.getIdentifier();
		this.settlement2 = settlement2.getIdentifier();
		this.credit = credit;
	}

	/**
	 * Gets the first settlement.
	 * 
	 * @return settlement.
	 */
	public Settlement getSettlement1() {
		return unitManager.getSettlementByID(settlement1);
	}
	
	/**
	 * Gets the second settlement.
	 * @return settlement.
	 */
	public Settlement getSettlement2() {
		return unitManager.getSettlementByID(settlement2);
	}
	
	/**
	 * Gets the credit amount.
	 * @return credit amount (VP).
	 */
	public double getCredit() {
		return credit;
	}
	
//	/**
//	 * Reloads instances after loading from a saved sim
//	 * 
//	 * @param u {@link UnitManager}
//	 * @param c {@link CreditManager}
//	 */
//	public static void setInstances(UnitManager u, CreditManager c) {
//		unitManager = u;
//		creditManager = c;
//	}

}