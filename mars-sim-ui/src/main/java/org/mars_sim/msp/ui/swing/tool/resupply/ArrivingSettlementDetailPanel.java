/*
 * Mars Simulation Project
 * ArrivingSettlementDetailPanel.java
 * @date 2021-12-18
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventCategory;
import org.mars_sim.msp.core.events.HistoricalEventListener;
import org.mars_sim.msp.core.events.SimpleEvent;
import org.mars_sim.msp.core.interplanetary.transport.settlement.ArrivingSettlement;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.tool.mission.MissionWindow;


/**
 * A panel showing a selected arriving settlement details.
 */
@SuppressWarnings("serial")
public class ArrivingSettlementDetailPanel
extends JPanel
implements ClockListener, HistoricalEventListener {

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
	
	private MarsClock currentTime;
	
	private int solsToArrival = -1;
	
	/**
	 * Constructor.
	 */
	public ArrivingSettlementDetailPanel(MainDesktopPane desktop) {

		// Use JPanel constructor.
		super();
		this.desktop = desktop;
	
		MasterClock masterClock = desktop.getSimulation().getMasterClock();
		currentTime = masterClock.getMarsClock();
		
		setLayout(new BorderLayout(0, 10));
		setBorder(new MarsPanelBorder());

		// Create the title label.
		JLabel titleLabel = new JLabel(
			Msg.getString("ArrivingSettlementDetailPanel.arrivingSettlement"), //$NON-NLS-1$
			JLabel.CENTER
		);
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
		titleLabel.setPreferredSize(new Dimension(-1, 25));
		add(titleLabel, BorderLayout.NORTH);

		// Create the info panel.
		JPanel infoPane = new JPanel(new SpringLayout());
		add(infoPane, BorderLayout.CENTER);

		// Create name panel.
//		JPanel namePane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
//		infoPane.add(namePane);

		// Create name title label.
		JLabel nameTitleLabel = new JLabel(Msg.getString("ArrivingSettlementDetailPanel.name"), JLabel.RIGHT); //$NON-NLS-1$
		infoPane.add(nameTitleLabel);

		// Create name value label.
		nameValueLabel = new JLabel("", JLabel.LEFT); //$NON-NLS-1$
		infoPane.add(nameValueLabel);

		// Create state panel.
//		JPanel statePane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
//		infoPane.add(statePane);

		// Create state title label.
		JLabel stateTitleLabel = new JLabel(Msg.getString("ArrivingSettlementDetailPanel.state"), JLabel.RIGHT); //$NON-NLS-1$
		infoPane.add(stateTitleLabel);

		// Create state value label.
		stateValueLabel = new JLabel("", JLabel.LEFT); //$NON-NLS-1$
		infoPane.add(stateValueLabel);

		// Create template panel.
//		JPanel templatePane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
//		infoPane.add(templatePane);

		// Create template title label.
		JLabel templateTitleLabel = new JLabel(Msg.getString("ArrivingSettlementDetailPanel.layoutTemplate"), JLabel.RIGHT); //$NON-NLS-1$
		infoPane.add(templateTitleLabel);

		// Create template value label.
		templateValueLabel = new JLabel("", JLabel.LEFT); //$NON-NLS-1$
		infoPane.add(templateValueLabel);

		// Create population panel.
//		JPanel populationPane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
//		infoPane.add(populationPane);

		// Create population title label.
		JLabel populationTitleLabel = new JLabel(Msg.getString("ArrivingSettlementDetailPanel.immigrants"), JLabel.RIGHT); //$NON-NLS-1$
		infoPane.add(populationTitleLabel);

		// Create population value label.
		populationValueLabel = new JLabel("", JLabel.LEFT); //$NON-NLS-1$
		infoPane.add(populationValueLabel);

		// Create arrival date panel.
//		JPanel arrivalDatePane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
//		infoPane.add(arrivalDatePane);

		// Create arrival date title label.
		JLabel arrivalDateTitleLabel = new JLabel(Msg.getString("ArrivingSettlementDetailPanel.arrivalDate"), JLabel.RIGHT); //$NON-NLS-1$
		infoPane.add(arrivalDateTitleLabel);

		// Create arrival date value label.
		arrivalDateValueLabel = new JLabel("", JLabel.LEFT); //$NON-NLS-1$
		infoPane.add(arrivalDateValueLabel);

		// Create time arrival panel.
//		JPanel timeArrivalPane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
//		infoPane.add(timeArrivalPane);

		// Create time arrival title label.
		JLabel timeArrivalTitleLabel = new JLabel(Msg.getString("ArrivingSettlementDetailPanel.timeUntilArrival"), JLabel.RIGHT); //$NON-NLS-1$
		infoPane.add(timeArrivalTitleLabel);

		// Create time arrival value label.
		timeArrivalValueLabel = new JLabel("", JLabel.LEFT); //$NON-NLS-1$
		infoPane.add(timeArrivalValueLabel);

		// Create location panel.
//		JPanel locationPane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
//		infoPane.add(locationPane);

		// Create location title label.
		JLabel locationTitleLabel = new JLabel(Msg.getString("ArrivingSettlementDetailPanel.location"), JLabel.RIGHT); //$NON-NLS-1$
		infoPane.add(locationTitleLabel);

		// Create location value label.
		locationValueLabel = new JLabel("", JLabel.LEFT); //$NON-NLS-1$
		infoPane.add(locationValueLabel);

		// Lay out the spring panel.
		SpringUtilities.makeCompactGrid(infoPane, 
						7, 2, // rows, cols
						30, 10, // initX, initY
						7, 3); // xPad, yPad

		// Set as clock listener for a rate of 1 pulse per 2 seconds
		masterClock.addClockListener(this, 2000L);

		// Set as historical event listener.
		Simulation.instance().getEventManager().addListener(this);
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

		updateTimeToArrival();

		templateValueLabel.setText(arrivingSettlement.getTemplate());
		locationValueLabel.setText(arrivingSettlement.getLandingLocation().getFormattedString());
		populationValueLabel.setText(Integer.toString(arrivingSettlement.getPopulationNum()));

		validate();
	}

	/**
	 * Update the time to arrival label.
	 */
	private void updateTimeToArrival() {
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
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						// Update arriving settlement info.
						if (arrivingSettlement != null) {
							updateArrivingSettlementInfo();
						}
					}
				});
			}
		}
	}

	@Override
	public void eventsRemoved(int startIndex, int endIndex) {
		// Do nothing.
	}

	public void updateArrival() {
		// Determine if change in time to arrival display value.
		if ((arrivingSettlement != null) && (solsToArrival >= 0)) {
			double timeDiff = MarsClock.getTimeDiff(arrivingSettlement.getArrivalDate(), currentTime);
			double newSolsToArrival = (int) Math.abs(timeDiff / 1000D);
			if (newSolsToArrival != solsToArrival) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						// Update time to arrival label.
						if (arrivingSettlement != null) {
							updateTimeToArrival();
						}
					}
				});
			}
		}
	}

	
	@Override
	public void clockPulse(ClockPulse pulse) {
		if (desktop.isToolWindowOpen(MissionWindow.NAME)) {
			updateArrival();
		}				
	}
	
	@Override
	public void pauseChange(boolean isPaused, boolean showPane) {
		// placeholder
	}
	
	/**
	 * Prepares the panel for deletion.
	 */
	public void destroy() {
		desktop.getSimulation().getEventManager().removeListener(this);
		desktop.getSimulation().getMasterClock().removeClockListener(this);
		
		nameValueLabel = null;
		stateValueLabel = null;
		arrivalDateValueLabel = null;
		timeArrivalValueLabel = null;
		templateValueLabel = null;
		locationValueLabel = null;
		populationValueLabel = null;
		arrivingSettlement = null;		
		desktop = null;

		currentTime = null;
	}

}
