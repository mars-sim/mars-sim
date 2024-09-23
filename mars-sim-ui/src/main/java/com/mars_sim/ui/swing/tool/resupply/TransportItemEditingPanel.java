/*
 * Mars Simulation Project
 * TransportEditingPanel.java
 * @date 2022-07-19
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.resupply;

import javax.swing.JPanel;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.interplanetary.transport.Transportable;
import com.mars_sim.core.person.PersonConfig;
import com.mars_sim.core.structure.SettlementConfig;
import com.mars_sim.core.structure.SettlementTemplateConfig;

/**
 * An abstract panel for editing a transport item.
 */
@SuppressWarnings("serial")
public abstract class TransportItemEditingPanel
extends JPanel {

	protected static SimulationConfig simulationConfig = SimulationConfig.instance();
	protected static SettlementConfig settlementConfig = simulationConfig.getSettlementConfiguration();
	protected static SettlementTemplateConfig settlementTemplateConfig = simulationConfig.getSettlementTemplateConfiguration();
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
