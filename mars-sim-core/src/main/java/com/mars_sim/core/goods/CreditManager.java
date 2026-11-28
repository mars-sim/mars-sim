/*
 * Mars Simulation Project
 * CreditManager.java
 * @date 2022-06-11
 * @author Scott Davis
 */
package com.mars_sim.core.goods;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.mars_sim.core.authority.Authority;
import com.mars_sim.core.logging.SimLogger;

/**
 * The CreditManager class keeps track of all credits/debts between authorities.
 */
public class CreditManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private static SimLogger logger = SimLogger.getLogger(CreditManager.class.getName());

	private String ownerName;
	
	/** Credit listeners. */
	private transient List<CreditListener> listeners;
	/** The map of this authority's credit/debit with another authority. */
	private Map<Authority, Double> creditMap = new ConcurrentHashMap<>();

	/**
	 * Constructor.
	 * 
	 * @param authority Owning authority
	 */
	public CreditManager(Authority authority) {
		if (authority == null) {
			throw new IllegalArgumentException("Authority is null");
		}
		ownerName = authority.getName();
	}

	/**
	 * Sets the credit between this authority and another authority.
	 * 
	 * @param otherAuthority Other authority
	 * @param amount Credit amount (VP)
	 */
	public void setCredit(Authority otherAuthority, double amount) {
		if (otherAuthority == null) {
			return;
		}
		if (ownerName.equals(otherAuthority.getName())) {
			return;
		}

		creditMap.put(otherAuthority, amount);

		logger.info("Account ledger updated for " + ownerName + " with " + otherAuthority.getName()
				+ "  Credit: " + Math.round(amount * 10.0)/10.0);

		// Update listeners.
		synchronized (getListeners()) {
			Iterator<CreditListener> i = getListeners().iterator();
			while (i.hasNext())
				i.next().creditUpdate(new CreditEvent(ownerName, otherAuthority.getName(), amount));
		}
	}

	/**
	 * Gets the credit between this authority and another authority.
	 * 
	 * @param otherAuthority Other authority
	 * @return the credit amount (VP).
	 */
	public double getCredit(Authority otherAuthority) {
		if (otherAuthority == null) {
			return 0D;
		}
		return creditMap.getOrDefault(otherAuthority, 0D);
	}

	/**
	 * Returns the credit map.
	 * 
	 * @return
	 */
	public Map<Authority, Double> getCreditMap() {
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
	 * Adds a listener.
	 * 
	 * @param newListener The listener to add.
	 */
	public void addListener(CreditListener newListener) {
		if (!getListeners().contains(newListener))
			getListeners().add(newListener);
	}

	/**
	 * Removes a listener.
	 * 
	 * @param oldListener the listener to remove.
	 */
	public void removeListener(CreditListener oldListener) {
		if (getListeners().contains(oldListener))
			getListeners().remove(oldListener);
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		if (listeners != null)
			listeners.clear();
		listeners = null;
		if (creditMap != null)
			creditMap.clear();
		creditMap = null;
	}
}
