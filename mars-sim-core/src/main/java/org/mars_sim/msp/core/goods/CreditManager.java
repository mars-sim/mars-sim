/*
 * Mars Simulation Project
 * CreditManager.java
 * @date 2022-06-11
 * @author Scott Davis
 */
package org.mars_sim.msp.core.goods;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * The CreditManager class keeps track of all credits/debts between settlements.
 */
public class CreditManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Domain members
	private int settlementID;
	
	/** Credit listeners. */
	private transient List<CreditListener> listeners;
	/** The person's opinion toward another person. */
	private Map<Integer, Double> creditMap = new HashMap<>();
	
	/** The Unit Manager instance. */
	private static UnitManager unitManager;
	
	/**
	 * Constructor.
	 * 
	 * @param settlement
	 */
	public CreditManager(Settlement settlement) {
		settlementID = settlement.getIdentifier();
		// Creates credit manager with all settlements in the simulation.
		Iterator<Settlement> i = unitManager.getSettlements().iterator();
		while (i.hasNext()) {
			int id = i.next().getIdentifier();
			if (!creditMap.containsKey(id) && settlementID != id)
				creditMap.put(id, 0.0);
		}
	}

	/**
	 * Sets the credit between two settlements.
	 * 
	 * @param settlement1 the first settlement.
	 * @param settlement2 the second settlement.
	 * @param amount      the credit amount (VP) that the first settlement has with
	 *                    the second settlement. (negative value if the first
	 *                    settlement owes the second settlement).
	 */
	public static void setCredit(Settlement settlement1, Settlement settlement2, double amount) {

		int id = settlement2.getIdentifier();
		
		if (settlement1.getCreditManager().getCreditMap().containsKey(id))
			settlement1.getCreditManager().getCreditMap().put(id, 0.0);
		
		// Update listeners.
		synchronized (settlement1.getCreditManager().getListeners()) {
			Iterator<CreditListener> i = settlement1.getCreditManager().getListeners().iterator();
			while (i.hasNext())
				i.next().creditUpdate(new CreditEvent(settlement1, settlement2, amount));
		}
	}

	/**
	 * Gets the credit between two settlements.
	 * 
	 * @param settlement1 the first settlement.
	 * @param settlement2 the second settlement.
	 * @return the credit amount (VP) that the first settlement has with the second
	 *         settlement. (negative value if the first settlement owes the second
	 *         settlement).
	 */
	public static double getCredit(Settlement settlement1, Settlement settlement2) {
		int id = settlement2.getIdentifier();
		if (settlement1.getCreditManager().getCreditMap().containsKey(id))
			return settlement1.getCreditManager().getCreditMap().get(id);
		return 0;
	}

	/**
	 * Add a new settlement to the credit graph.
	 * 
	 * @param newSettlement the new settlement.
	 */
	public void addSettlement(Settlement newSettlement) {
		if (newSettlement == null) {
			throw new IllegalArgumentException("Settlement is null");
		}
		int id = newSettlement.getIdentifier();
		if (!creditMap.containsKey(id) && settlementID != id)
			creditMap.put(id, 0.0);
	}

	/**
	 * Returns the credit map
	 * 
	 * @return
	 */
	public Map<Integer, Double> getCreditMap() {
		return creditMap;
	}
	
	/**
	 * Gets the list of credit listeners.
	 * 
	 * @return list of credit listeners.
	 */
	private List<CreditListener> getListeners() {
		if (listeners == null)
			listeners = Collections.synchronizedList(new CopyOnWriteArrayList<CreditListener>());
		return listeners;
	}

	/**
	 * Add a listener
	 * 
	 * @param newListener The listener to add.
	 */
	public void addListener(CreditListener newListener) {
		if (!getListeners().contains(newListener))
			getListeners().add(newListener);
	}

	/**
	 * Remove a listener
	 * 
	 * @param oldListener the listener to remove.
	 */
	public void removeListener(CreditListener oldListener) {
		if (getListeners().contains(oldListener))
			getListeners().remove(oldListener);
	}

	/**
	 * Initialize instances
	 * 
	 * @param um the unitManager instance
	 */
	public static void initializeInstances(UnitManager um) {
		unitManager = um;		
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		unitManager = null;
		if (listeners != null)
			listeners.clear();
		listeners = null;
	}
}
