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
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventCategory;
import org.mars_sim.msp.core.events.HistoricalEventListener;
import org.mars_sim.msp.core.events.SimpleEvent;
import org.mars_sim.msp.core.interplanetary.transport.settlement.ArrivingSettlement;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;


/**
 * A panel showing a selected arriving settlement details.
 */
@SuppressWarnings("serial")
public class ArrivingSettlementDetailPanel
extends JPanel
implements HistoricalEventListener {

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
	
	//private MarsClock currentTime;
	
	private int solsToArrival = -1;
	
	/**
	 * Constructor.
	 */
	public ArrivingSettlementDetailPanel(MainDesktopPane desktop) {

		// Use JPanel constructor.
		super();
		this.desktop = desktop;
	
		
		setLayout(new BorderLayout(0, 10));
		setBorder(new MarsPanelBorder());

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

		// Set as historical event listener.
		desktop.getMainWindow().getSimulation().getEventManager().addListener(this);
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
	}

	/** 
	 * Update displayed information.
	 */
	private void updateArrivingSettlementInfo() {
		nameValueLabel.setText(arrivingSettlement.getName());
		stateValueLabel.setText(arrivingSettlement.getTransitState().getName());
		arrivalDateValueLabel.setText(arrivingSettlement.getArrivalDate().getDateTimeStamp());

		MarsClock marsTime = desktop.getMainWindow().getSimulation().getMasterClock().getMarsClock();
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
	private void updateTimeToArrival(MarsClock currentTime) {
		String timeArrival = Msg.getString("ArrivingSettlementDetailPanel.noTime"); //$NON-NLS-1$
		solsToArrival = -1;
		double timeDiff = MarsClock.getTimeDiff(arrivingSettlement.getArrivalDate(), currentTime);
		if (timeDiff > 0D) {
			solsToArrival = (int) Math.abs(timeDiff / 1000D);
			timeArrival = Msg.getString(
				"ArrivingSettlementDetailPanel.sols", //$NON-NLS-1$
				Integer.toString(solsToArrival)
			);
		}
		timeArrivalValueLabel.setText(timeArrival);
	}


	@Override
	public void eventAdded(int index, SimpleEvent se, HistoricalEvent he) {
		if (HistoricalEventCategory.TRANSPORT == he.getCategory() && 
				EventType.TRANSPORT_ITEM_MODIFIED.equals(he.getType())) {
			if ((arrivingSettlement != null) && he.getSource().equals(arrivingSettlement)) {
				updateArrivingSettlementInfo();
			}
		}
	}

	@Override
	public void eventsRemoved(int startIndex, int endIndex) {
		// Do nothing.
	}

	private void updateArrival(MarsClock currentTime) {
		// Determine if change in time to arrival display value.
		if ((arrivingSettlement != null) && (solsToArrival >= 0)) {
			double timeDiff = MarsClock.getTimeDiff(arrivingSettlement.getArrivalDate(), currentTime);
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
	

	/**
	 * Prepares the panel for deletion.
	 */
	public void destroy() {
		desktop.getSimulation().getEventManager().removeListener(this);
		
		nameValueLabel = null;
		stateValueLabel = null;
		arrivalDateValueLabel = null;
		timeArrivalValueLabel = null;
		templateValueLabel = null;
		locationValueLabel = null;
		populationValueLabel = null;
		arrivingSettlement = null;		
		desktop = null;
	}

}
