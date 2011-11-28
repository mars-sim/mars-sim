/**
 * Mars Simulation Project
 * CreditManager.java
 * @version 3.02 2011-11-26
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure.goods;

import com.phoenixst.plexus.*;
import com.phoenixst.plexus.Graph.Edge;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.structure.Settlement;

import java.io.Serializable;
import java.util.*;

/** 
 * The CreditManager class keeps track of all credits/debts between settlements.
 * The simulation instance has only one credit manager. 
 */
public class CreditManager implements Serializable {

	// Domain members
	private Graph creditGraph;
	private transient List<CreditListener> listeners; // Credit listeners.
	
	/**
	 * Constructor
	 */
	public CreditManager() {
		// Creates credit manager with all settlements in the simulation.
		this(Simulation.instance().getUnitManager().getSettlements());
	}
	
	/**
	 * Constructor
	 * @param settlements collection of settlements to use.
	 */
	public CreditManager(Collection<Settlement> settlements) {
		// Create new graph for credit.
		creditGraph = new DefaultGraph();
		
		// Add all settlements as nodes.
		Iterator<Settlement> i = settlements.iterator();
		while (i.hasNext()) creditGraph.addNode(i.next());
	}
	
	/**
	 * Sets the credit between two settlements.
	 * @param settlement1 the first settlement.
	 * @param settlement2 the second settlement.
	 * @param amount the credit amount (VP) that the first settlement has with the second settlement. (negative
	 * value if the first settlement owes the second settlement).
	 * @throws Exception if error setting the credit between the settlements.
	 */
	public void setCredit(Settlement settlement1, Settlement settlement2, double amount) {
		
		// Check that settlements are in graph.
		if (!creditGraph.containsNode(settlement1)) 
			throw new IllegalArgumentException("settlement: " + settlement1 + " is invalid");
		if (!creditGraph.containsNode(settlement2)) 
			throw new IllegalArgumentException("settlement: " + settlement2 + " is invalid");
		
		// Remove existing edge between settlements if any.
		EdgePredicate edgePredicate = EdgePredicateFactory.createEqualsNodes(settlement1, settlement2, GraphUtils.ANY_DIRECTION_MASK);
		Edge existingEdge = creditGraph.getEdge(edgePredicate);
		if (existingEdge != null) creditGraph.removeEdge(existingEdge);
		
		// Add edge for credit.
		if (amount >= 0D) creditGraph.addEdge(Math.abs(amount), settlement1, settlement2, true);
		else creditGraph.addEdge(Math.abs(amount), settlement2, settlement1, true);
		
        // Update listeners.
        synchronized(getListeners()) {
        	Iterator<CreditListener> i = getListeners().iterator();
        	while (i.hasNext()) i.next().creditUpdate(new CreditEvent(settlement1, settlement2, amount));
        }
	}
	
	/**
	 * Gets the credit between two settlements.
	 * @param settlement1 the first settlement.
	 * @param settlement2 the second settlement.
	 * @return the credit amount (VP) that the first settlement has with the second settlement. (negative
	 * value if the first settlement owes the second settlement).
	 * @throws Exception if error getting the credit between the settlements.
	 */
	public double getCredit(Settlement settlement1, Settlement settlement2) {
		
		double result = 0D;
		
		// Gets an edge associated with these two settlements if any.
		EdgePredicate edgePredicate = EdgePredicateFactory.createEqualsNodes(settlement1, settlement2, GraphUtils.ANY_DIRECTION_MASK);
		Edge existingEdge = creditGraph.getEdge(edgePredicate);
		if (existingEdge != null) {
			result = (Double) existingEdge.getUserObject();
			if (existingEdge.getHead() == settlement1) result *= -1D;
		}
		
		return result;
	}
	
	/**
	 * Gets the list of credit listeners.
	 * @return list of credit listeners.
	 */
	private List<CreditListener> getListeners() {
		if (listeners == null) listeners = Collections.synchronizedList(new ArrayList<CreditListener>());
		return listeners;
	}
	
    /**
     * Add a listener
     * @param newListener The listener to add.
     */
    public void addListener(CreditListener newListener) {
        if (!getListeners().contains(newListener)) getListeners().add(newListener);
    }
    
    /**
     * Remove a listener
     * @param oldListener the listener to remove.
     */
    public void removeListener(CreditListener oldListener) {
    	if (getListeners().contains(oldListener)) getListeners().remove(oldListener);
    }
    
    /**
     * Prepare object for garbage collection.
     */
    public void destroy() {
       creditGraph = null;
       if (listeners != null) listeners.clear();
       listeners = null;
    }
}