/**
 * Mars Simulation Project
 * TransportEditingStage.java
 * @version 3.1.0 2017-11-21
 * @author Manny Kung
 */

package org.mars_sim.javafx.settlement;

import javafx.stage.Stage;

import org.mars_sim.msp.core.interplanetary.transport.Transportable;

/**
 * An abstract panel for editing a transport item.
 */
@SuppressWarnings("restriction")
public abstract class TransportItemEditingStage
extends Stage {

	// Data members
	private Transportable transportItem;

	/**
	 * Constructor.
	 * @param transportItem the transport item to edit.
	 */
	public TransportItemEditingStage(Transportable transportItem) {
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