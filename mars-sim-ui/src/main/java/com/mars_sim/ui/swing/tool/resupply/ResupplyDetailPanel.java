/*
 * Mars Simulation Project
 * ResupplyDetailPanel.java
 * @date 2022-07-19
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.mars_sim.core.interplanetary.transport.resupply.Resupply;
import com.mars_sim.core.interplanetary.transport.resupply.ResupplySchedule;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.utils.AttributePanel;


/**
 * A panel showing a selected resupply mission details.
 */
@SuppressWarnings("serial")
public class ResupplyDetailPanel extends JPanel {

	// Data members
	private int solsToArrival = -1;
	
	private JLabel templateLabel;
	private JLabel destinationValueLabel;
	private JLabel stateValueLabel;
	private JLabel arrivalDateValueLabel;
	private JLabel launchDateValueLabel;
	private JLabel timeArrivalValueLabel;
	private JLabel immigrantsValueLabel;
	
	private UIContext context;

	private Resupply resupply;

	private SettlementSuppliesPanel suppliesPanel;

	
	/**
	 * Constructor.
	 */
	public ResupplyDetailPanel(UIContext context) {

		// Use JPanel constructor
		super();

		this.context = context;
	
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

		templateLabel = detailsPane.addTextField("Schedule", "", null);
		destinationValueLabel = detailsPane.addTextField("Destination", "", null);
		stateValueLabel = detailsPane.addTextField("State", "", null);
		launchDateValueLabel = detailsPane.addTextField("Launch Date", "", null);
		arrivalDateValueLabel = detailsPane.addTextField("Arrival Date", "", null);
		timeArrivalValueLabel = detailsPane.addTextField("Time Until Arrival", "", null);
		immigrantsValueLabel = detailsPane.addTextField("Immigrants", "", null);

		suppliesPanel = new SettlementSuppliesPanel();
		infoPane.add(suppliesPanel.getComponent(), BorderLayout.CENTER);

		add(infoPane, BorderLayout.CENTER);
	}

	/**
	 * Set the resupply mission to show.
	 * If resupply is null, clear displayed info.
	 * 
	 * @param resupply the resupply mission.
	 */
	public void setResupply(Resupply resupply) {
		this.resupply = resupply;
		if (resupply == null) {
			clearInfo();
		}
		else {
			updateResupplyInfo();
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
		ResupplySchedule schedule = resupply.getTemplate();
		templateLabel.setText((schedule != null ? schedule.getName()
											+ " (every " + schedule.getSchedule().getFrequency() + " sols)" : ""));
		destinationValueLabel.setText(resupply.getSettlement().getName());
		stateValueLabel.setText(resupply.getTransitState().getName());
		launchDateValueLabel.setText(resupply.getLaunchDate().getTruncatedDateTimeStamp());
		arrivalDateValueLabel.setText(resupply.getArrivalDate().getTruncatedDateTimeStamp());
		immigrantsValueLabel.setText(Integer.toString(resupply.getNewImmigrantNum()));
		
		updateTimeToArrival(context.getSimulation().getMasterClock().getMarsTime());
		suppliesPanel.show(resupply);

		validate();
	}

	/**
	 * Updates the time to arrival label.
	 * @param currentTime 
	 */
	private void updateTimeToArrival(MarsTime currentTime) {
		String timeArrival = "---";
		solsToArrival = -1;
		double timeDiff = resupply.getArrivalDate().getTimeDiff(currentTime);
		if (timeDiff > 0D) {
			solsToArrival = (int) Math.abs(timeDiff / 1000D);
			timeArrival = Integer.toString(solsToArrival) + " Sols";
		}
		timeArrivalValueLabel.setText(timeArrival);
	}

	private void updateArrival(MarsTime currentTime) {
		// Determine if change in time to arrival display value.
		if ((resupply != null) && (solsToArrival >= 0)) {
			double timeDiff = resupply.getArrivalDate().getTimeDiff(currentTime);
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
}
