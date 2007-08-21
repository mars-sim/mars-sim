/**
 * Mars Simulation Project
 * CreditManager.java
 * @version 2.81 2007-08-20
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure.goods;

import java.io.Serializable;

import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.SettlementCollection;
import org.mars_sim.msp.simulation.structure.SettlementIterator;

import com.phoenixst.plexus.DefaultGraph;
import com.phoenixst.plexus.EdgePredicate;
import com.phoenixst.plexus.EdgePredicateFactory;
import com.phoenixst.plexus.Graph;
import com.phoenixst.plexus.GraphUtils;
import com.phoenixst.plexus.Graph.Edge;

/** 
 * The CreditManager class keeps track of all credits/debts between settlements.
 * The simulation instance has only one credit manager. 
 */
public class CreditManager implements Serializable {

	// Domain members
	private Graph creditGraph;
	
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
	public CreditManager(SettlementCollection settlements) {
		// Create new graph for credit.
		creditGraph = new DefaultGraph();
		
		// Add all settlements as nodes.
		SettlementIterator i = settlements.iterator();
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
	public void setCredit(Settlement settlement1, Settlement settlement2, double amount) throws Exception {
		
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
		if (amount >= 0D) creditGraph.addEdge(new Double(Math.abs(amount)), settlement1, settlement2, true);
		else creditGraph.addEdge(new Double(Math.abs(amount)), settlement2, settlement1, true);
	}
	
	/**
	 * Gets the credit between two settlements.
	 * @param settlement1 the first settlement.
	 * @param settlement2 the second settlement.
	 * @return the credit amount (VP) that the first settlement has with the second settlement. (negative
	 * value if the first settlement owes the second settlement).
	 * @throws Exception if error getting the credit between the settlements.
	 */
	public double getCredit(Settlement settlement1, Settlement settlement2) throws Exception {
		
		double result = 0D;
		
		// Gets an edge associated with these two settlements if any.
		EdgePredicate edgePredicate = EdgePredicateFactory.createEqualsNodes(settlement1, settlement2, GraphUtils.ANY_DIRECTION_MASK);
		Edge existingEdge = creditGraph.getEdge(edgePredicate);
		if (existingEdge != null) {
			result = ((Double) existingEdge.getUserObject()).doubleValue();
			if (existingEdge.getHead() == settlement1) result *= -1D;
		}
		
		return result;
	}
}