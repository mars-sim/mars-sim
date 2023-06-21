/*
 * Mars Simulation Project
 * ArrivingSettlementDetailPanel.java
 * @date 2021-12-18
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.interplanetary.transport.settlement.ArrivingSettlement;
import org.mars_sim.msp.core.structure.SettlementConfig;
import org.mars_sim.msp.core.structure.SettlementSupplies;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsTime;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;


/**
 * A panel showing a selected arriving settlement details.
 */
@SuppressWarnings("serial")
public class ArrivingSettlementDetailPanel
extends JPanel
{

	// Data members
	private JLabel nameValueLabel;
	private JLabel stateValueLabel;
	private JLabel arrivalDateValueLabel;
	private JLabel timeArrivalValueLabel;
	private JLabel templateValueLabel;
	private JLabel locationValueLabel;
	private JLabel populationValueLabel;

	private ArrivingSettlement arrivingSettlement;
	
	private MainDesktopPane desktop;
	
	private int solsToArrival = -1;
	private SettlementSuppliesPanel suppliesPanel;
	
	/**
	 * Constructor.
	 */
	public ArrivingSettlementDetailPanel(MainDesktopPane desktop) {

		// Use JPanel constructor.
		super();
		this.desktop = desktop;
	
		
		setLayout(new BorderLayout(0, 10));

		// Create the title label.
		JLabel titleLabel = new JLabel(
			Msg.getString("ArrivingSettlementDetailPanel.arrivingSettlement"), //$NON-NLS-1$
			JLabel.CENTER
		);
		StyleManager.applyHeading(titleLabel);
		titleLabel.setPreferredSize(new Dimension(-1, 25));
		add(titleLabel, BorderLayout.NORTH);

		JPanel detailsPane = new JPanel(new BorderLayout());
		detailsPane.setBorder(BorderFactory.createEtchedBorder());
		add(detailsPane, BorderLayout.CENTER);

		// Create the info panel.
		AttributePanel infoPane = new AttributePanel(7);
		detailsPane.add(infoPane, BorderLayout.NORTH);

		// Create name title label.
		nameValueLabel = infoPane.addTextField(Msg.getString("ArrivingSettlementDetailPanel.name"), "", null);
		stateValueLabel = infoPane.addTextField(Msg.getString("ArrivingSettlementDetailPanel.state"), "", null);
		templateValueLabel = infoPane.addTextField(Msg.getString("ArrivingSettlementDetailPanel.layoutTemplate"), "", null);
		populationValueLabel = infoPane.addTextField(Msg.getString("ArrivingSettlementDetailPanel.immigrants"), "", null);
		arrivalDateValueLabel = infoPane.addTextField(Msg.getString("ArrivingSettlementDetailPanel.arrivalDate"), "", null);
		timeArrivalValueLabel = infoPane.addTextField(Msg.getString("ArrivingSettlementDetailPanel.timeUntilArrival"), "", null);
		locationValueLabel = infoPane.addTextField(Msg.getString("ArrivingSettlementDetailPanel.location"), "", null);

		suppliesPanel = new SettlementSuppliesPanel();
		detailsPane.add(suppliesPanel.getComponent(), BorderLayout.CENTER);
	}

	/**
	 * Set the arriving settlement.
	 * @param newArrivingSettlement the arriving settlement or null if none.
	 */
	public void setArrivingSettlement(ArrivingSettlement newArrivingSettlement) {
		if (arrivingSettlement != newArrivingSettlement) {
			arrivingSettlement = newArrivingSettlement;
			if (newArrivingSettlement == null) {
				clearInfo();
			}
			else {
				SettlementConfig sConfig = SimulationConfig.instance().getSettlementConfiguration();
				SettlementSupplies template = sConfig.getItem(arrivingSettlement.getTemplate());
				if (template != null) {
					suppliesPanel.show(template);
				}
				else {
					suppliesPanel.clear();
				}

				updateArrivingSettlementInfo();
			}
		}
	}

	/**
	 * Clear displayed information.
	 */
	private void clearInfo() {
		nameValueLabel.setText(""); //$NON-NLS-1$
		stateValueLabel.setText(""); //$NON-NLS-1$
		arrivalDateValueLabel.setText(""); //$NON-NLS-1$
		timeArrivalValueLabel.setText(""); //$NON-NLS-1$
		templateValueLabel.setText(""); //$NON-NLS-1$
		locationValueLabel.setText(""); //$NON-NLS-1$
		populationValueLabel.setText(""); //$NON-NLS-1$

		suppliesPanel.clear();
	}

	/** 
	 * Update displayed information.
	 */
	private void updateArrivingSettlementInfo() {
		nameValueLabel.setText(arrivingSettlement.getName());
		stateValueLabel.setText(arrivingSettlement.getTransitState().getName());
		arrivalDateValueLabel.setText(arrivingSettlement.getArrivalDate().getDateTimeStamp());

		MarsTime marsTime = desktop.getSimulation().getMasterClock().getMarsTime();
		updateTimeToArrival(marsTime);

		templateValueLabel.setText(arrivingSettlement.getTemplate());
		locationValueLabel.setText(arrivingSettlement.getLandingLocation().getFormattedString());
		populationValueLabel.setText(Integer.toString(arrivingSettlement.getPopulationNum()));

		validate();
	}

	/**
	 * Update the time to arrival label.
	 * @param currentTime
	 */
	private void updateTimeToArrival(MarsTime currentTime) {
		String timeArrival = Msg.getString("ArrivingSettlementDetailPanel.noTime"); //$NON-NLS-1$
		solsToArrival = -1;
		double timeDiff = arrivingSettlement.getArrivalDate().getTimeDiff(currentTime);
		if (timeDiff > 0D) {
			solsToArrival = (int) Math.abs(timeDiff / 1000D);
			timeArrival = Msg.getString(
				"ArrivingSettlementDetailPanel.sols", //$NON-NLS-1$
				Integer.toString(solsToArrival)
			);
		}
		timeArrivalValueLabel.setText(timeArrival);
	}

	private void updateArrival(MarsTime currentTime) {
		// Determine if change in time to arrival display value.
		if ((arrivingSettlement != null) && (solsToArrival >= 0)) {
			double timeDiff = arrivingSettlement.getArrivalDate().getTimeDiff(currentTime);
			double newSolsToArrival = (int) Math.abs(timeDiff / 1000D);
			if (newSolsToArrival != solsToArrival) {
				// Update time to arrival label.
					updateTimeToArrival(currentTime);
			}
		}
	}

	/**
	 * Time has moved forward
	 * @param pulse Amount of clock movement
	 */	
	void update(ClockPulse pulse) {
		updateArrival(pulse.getMarsTime());			
	}
}
