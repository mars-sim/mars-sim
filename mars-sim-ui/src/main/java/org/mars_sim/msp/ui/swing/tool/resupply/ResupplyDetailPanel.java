/*
 * Mars Simulation Project
 * ResupplyDetailPanel.java
 * @date 2022-07-19
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventCategory;
import org.mars_sim.msp.core.events.HistoricalEventListener;
import org.mars_sim.msp.core.events.SimpleEvent;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;


/**
 * A panel showing a selected resupply mission details.
 */
@SuppressWarnings("serial")
public class ResupplyDetailPanel
extends JPanel
implements HistoricalEventListener {

	// Data members
	private int solsToArrival = -1;
	
	private JLabel templateLabel;
	private JLabel destinationValueLabel;
	private JLabel stateValueLabel;
	private JLabel arrivalDateValueLabel;
	private JLabel launchDateValueLabel;
	private JLabel timeArrivalValueLabel;
	private JLabel immigrantsValueLabel;
	
	private MainDesktopPane desktop;

	private Resupply resupply;

	private SettlementSuppliesPanel suppliesPanel;

	
	/**
	 * Constructor.
	 */
	public ResupplyDetailPanel(MainDesktopPane desktop) {

		// Use JPanel constructor
		super();

		this.desktop = desktop;
		
		Simulation sim = desktop.getSimulation();
	
		// Initialize data members.
		resupply = null;

		setLayout(new BorderLayout(0, 10));

		// Create the title label.
		JLabel titleLabel = new JLabel("Resupply Mission", SwingConstants.CENTER);
		StyleManager.applyHeading(titleLabel);
		titleLabel.setPreferredSize(new Dimension(-1, 25));
		add(titleLabel, BorderLayout.NORTH);

		JPanel infoPane = new JPanel(new BorderLayout());
		infoPane.setBorder(BorderFactory.createEtchedBorder());
		add(infoPane, BorderLayout.CENTER);

		// Create the details panel.
		AttributePanel detailsPane = new AttributePanel(7);
		infoPane.add(detailsPane, BorderLayout.NORTH);

		templateLabel = detailsPane.addTextField("Template", "", null);
		destinationValueLabel = detailsPane.addTextField("Destination", "", null);
		stateValueLabel = detailsPane.addTextField("State", "", null);
		launchDateValueLabel = detailsPane.addTextField("Launch Date", "", null);
		arrivalDateValueLabel = detailsPane.addTextField("Arrival Date", "", null);
		timeArrivalValueLabel = detailsPane.addTextField("Time Until Arrival", "", null);
		immigrantsValueLabel = detailsPane.addTextField("Immigrants", "", null);

		suppliesPanel = new SettlementSuppliesPanel();
		infoPane.add(suppliesPanel.getComponent(), BorderLayout.CENTER);

		add(infoPane, BorderLayout.CENTER);

		// Set as historical event listener.
		sim.getEventManager().addListener(this);
	}

	/**
	 * Set the resupply mission to show.
	 * If resupply is null, clear displayed info.
	 * 
	 * @param resupply the resupply mission.
	 */
	public void setResupply(Resupply resupply) {
		if (this.resupply != resupply) {
			this.resupply = resupply;
			if (resupply == null) {
				clearInfo();
			}
			else {
				updateResupplyInfo();
			}
		}
	}

	/**
	 * Clear the resupply info.
	 */
	private void clearInfo() {
		templateLabel.setText("");
		destinationValueLabel.setText("");
		stateValueLabel.setText("");
		launchDateValueLabel.setText("");
		arrivalDateValueLabel.setText("");
		timeArrivalValueLabel.setText("");
		immigrantsValueLabel.setText("");
		suppliesPanel.clear();
	}

	/**
	 * Updates the resupply info with the current resupply mission.
	 */
	private void updateResupplyInfo() {

		templateLabel.setText(resupply.getTemplate().getName());
		destinationValueLabel.setText(resupply.getSettlement().getName());
		stateValueLabel.setText(resupply.getTransitState().getName());
		launchDateValueLabel.setText(resupply.getLaunchDate().getDateString());
		arrivalDateValueLabel.setText(resupply.getArrivalDate().getDateTimeStamp());
		immigrantsValueLabel.setText(Integer.toString(resupply.getNewImmigrantNum()));
		
		updateTimeToArrival(desktop.getSimulation().getMasterClock().getMarsClock());
		suppliesPanel.show(resupply);

		validate();
	}

	/**
	 * Updates the time to arrival label.
	 * @param currentTime 
	 */
	private void updateTimeToArrival(MarsClock currentTime) {
		String timeArrival = "---";
		solsToArrival = -1;
		double timeDiff = MarsClock.getTimeDiff(resupply.getArrivalDate(), currentTime);
		if (timeDiff > 0D) {
			solsToArrival = (int) Math.abs(timeDiff / 1000D);
			timeArrival = Integer.toString(solsToArrival) + " Sols";
		}
		timeArrivalValueLabel.setText(timeArrival);
	}

	@Override
	public void eventAdded(int index, SimpleEvent se, HistoricalEvent he) {
		if (HistoricalEventCategory.TRANSPORT == he.getCategory() &&
				EventType.TRANSPORT_ITEM_MODIFIED.equals(he.getType())) {
			if ((resupply != null) && he.getSource().equals(resupply)) {
				if (resupply != null) {
					updateResupplyInfo();
				}
			}
		}
	}

	@Override
	public void eventsRemoved(int startIndex, int endIndex) {
		// Do nothing.
	}

	private void updateArrival(MarsClock currentTime) {
		// Determine if change in time to arrival display value.
		if ((resupply != null) && (solsToArrival >= 0)) {
			double timeDiff = MarsClock.getTimeDiff(resupply.getArrivalDate(), currentTime);
			double newSolsToArrival = (int) Math.abs(timeDiff / 1000D);
			if (newSolsToArrival != solsToArrival) {
				if (resupply != null) {
					updateTimeToArrival(currentTime);
				}
			}
		}
	}

	void update(ClockPulse pulse) {
		updateArrival(pulse.getMarsTime());
	}
	

	/**
	 * Prepares the panel for deletion.
	 */
	public void destroy() {
		Simulation sim = desktop.getSimulation();
		sim.getEventManager().removeListener(this);
		
		resupply = null;
		templateLabel = null;
		destinationValueLabel = null;
		stateValueLabel = null;
		arrivalDateValueLabel = null;
		launchDateValueLabel = null;
		timeArrivalValueLabel = null;
		immigrantsValueLabel = null;
		desktop = null;
	}
}
