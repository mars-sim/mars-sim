/*
 * Mars Simulation Project
 * TransportDetailPanel.java
 * @date 2022-07-19
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.CardLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

import org.mars_sim.msp.core.interplanetary.transport.Transportable;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.interplanetary.transport.settlement.ArrivingSettlement;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

/**
 * A panel showing a selected transport details.
 * TODO externalize strings
 */
@SuppressWarnings("serial")
public class TransportDetailPanel
extends JPanel {

	// Panel key strings.
	private static final String RESUPPLY = "resupply";
	private static final String SETTLEMENT = "settlement";

	// Data members.
	private CardLayout cardLayout;
	private ResupplyDetailPanel resupplyPanel;
	private ArrivingSettlementDetailPanel arrivingSettlementPanel;

	/**
	 * Constructor.
	 */
	public TransportDetailPanel(MainDesktopPane desktop) {

		// Use JPanel constructor.
		super();

		cardLayout = new CardLayout();
		setLayout(cardLayout);
		setPreferredSize(new Dimension(300, 300));

		// Add resupply detail panel.
		resupplyPanel = new ResupplyDetailPanel(desktop);
		add(resupplyPanel, RESUPPLY);

		// Add arriving settlement detail panel.
		arrivingSettlementPanel = new ArrivingSettlementDetailPanel(desktop);
		add(arrivingSettlementPanel, SETTLEMENT);

		// Set resupply panel as initial panel displayed.
		cardLayout.show(this, RESUPPLY);
	}

	/**
	 * Change the Trans[otalbe displayed
	 * @param newTransportable
	 */
	void setTransportable(Transportable newTransportable) {
		if (newTransportable instanceof Resupply resupply) {
			resupplyPanel.setResupply(resupply);
			cardLayout.show(this, RESUPPLY);
		}
		else if (newTransportable instanceof ArrivingSettlement arriving) {
			arrivingSettlementPanel.setArrivingSettlement(arriving);
			cardLayout.show(this, SETTLEMENT);
		}
	}

	/**
	 * Clock has changed triggering a refresh
	 * @param pulse Clock change
	 */
	void update(ClockPulse pulse) {
		resupplyPanel.update(pulse);
		arrivingSettlementPanel.update(pulse);
	}
}
