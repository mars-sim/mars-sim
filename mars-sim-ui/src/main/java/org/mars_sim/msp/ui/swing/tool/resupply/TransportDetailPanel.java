/**
 * Mars Simulation Project
 * TransportDetailPanel.java
 * @version 3.1.0 2017-11-21
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.CardLayout;
import java.awt.Dimension;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mars_sim.msp.core.interplanetary.transport.Transportable;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.interplanetary.transport.settlement.ArrivingSettlement;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

/**
 * A panel showing a selected transport details.
 * TODO externalize strings
 */
@SuppressWarnings("serial")
public class TransportDetailPanel
extends JPanel
implements ListSelectionListener {

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

	@Override
	public void valueChanged(ListSelectionEvent evt) {

		JList<?> transportList = (JList<?>) evt.getSource();
		if (!transportList.getValueIsAdjusting()) {
			Transportable newTransportable = (Transportable) transportList.getSelectedValue();
			if (newTransportable instanceof Resupply) {
				resupplyPanel.setResupply((Resupply) newTransportable);
				cardLayout.show(this, RESUPPLY);
			}
			else if (newTransportable instanceof ArrivingSettlement) {
				arrivingSettlementPanel.setArrivingSettlement((ArrivingSettlement) newTransportable);
				cardLayout.show(this, SETTLEMENT);
			}
		}
	}

	/**
	 * Prepares the panel for deletion.
	 */
	public void destroy() {
		resupplyPanel.destroy();
		arrivingSettlementPanel.destroy();
	}
}