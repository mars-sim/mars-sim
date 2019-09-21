/**
 * Mars Simulation Project
 * TransportEditingPanel.java
 * @version 3.1.0 2017-04-13
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import javax.swing.JPanel;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.interplanetary.transport.Transportable;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.structure.SettlementConfig;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * An abstract panel for editing a transport item.
 */
@SuppressWarnings("serial")
public abstract class TransportItemEditingPanel
extends JPanel {

	protected static Simulation sim = Simulation.instance();
	protected static UnitManager unitManager = sim.getUnitManager();
	protected static MarsClock marsClock = sim.getMasterClock().getMarsClock();
	protected static SimulationConfig simulationConfig = SimulationConfig.instance();
	protected static SettlementConfig settlementConfig = simulationConfig.getSettlementConfiguration();
	protected static PersonConfig personConfig = simulationConfig.getPersonConfig();
	
	// Data members
	private Transportable transportItem;

	/**
	 * Constructor.
	 * @param transportItem the transport item to edit.
	 */
	public TransportItemEditingPanel(Transportable transportItem) {
		// Use JPanel constructor
		super();

		// Initialize data members.
		this.transportItem = transportItem;
	}

	/**
	 * Gets the transport item.
	 * @return transport item.
	 */
	public Transportable getTransportItem() {
		return transportItem;
	}

	/**
	 * Modifies the transport item with the editing panel information.
	 */
	public abstract boolean modifyTransportItem();

	/**
	 * Creates the transport item with the editing panel information.
	 */
	public abstract boolean createTransportItem();

}