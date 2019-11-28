/**
 * Mars Simulation Project
 * ArrivingSettlementDetailPanel.java
 * @version 3.1.0 2017-10-16
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

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
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.mission.MissionWindow;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;

/**
 * A panel showing a selected arriving settlement details.
 */
public class ArrivingSettlementDetailPanel
extends WebPanel
implements ClockListener, HistoricalEventListener {

//	private static final int PERIOD_IN_MILLISOLS = 10D * 500D / MarsClock.SECONDS_PER_MILLISOL;//3;

	// Data members
	private WebLabel nameValueLabel;
	private WebLabel stateValueLabel;
	private WebLabel arrivalDateValueLabel;
	private WebLabel timeArrivalValueLabel;
	private WebLabel templateValueLabel;
	private WebLabel locationValueLabel;
	private WebLabel populationValueLabel;

	private ArrivingSettlement arrivingSettlement;
	
	private MainDesktopPane desktop;
//	private MainScene mainScene;
	
	private static MarsClock currentTime;
	private static MasterClock masterClock;
	
//	private double timeCache = 0;
	private int solsToArrival = -1;
	
	/**
	 * Constructor.
	 */
	public ArrivingSettlementDetailPanel(MainDesktopPane desktop) {

		// Use WebPanel constructor.
		super();
		this.desktop = desktop;
//		this.mainScene = desktop.getMainScene();
		
		masterClock = Simulation.instance().getMasterClock();
		currentTime = masterClock.getMarsClock();
		
		setLayout(new BorderLayout(0, 10));
		setBorder(new MarsPanelBorder());

		// Create the info panel.
		WebPanel infoPane = new WebPanel(new BorderLayout());
		add(infoPane, BorderLayout.NORTH);

		// Create the title label.
		WebLabel titleLabel = new WebLabel(
			Msg.getString("ArrivingSettlementDetailPanel.arrivingSettlement"), //$NON-NLS-1$
			WebLabel.CENTER
		);
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
		titleLabel.setPreferredSize(new Dimension(-1, 25));
		infoPane.add(titleLabel, BorderLayout.NORTH);

		// Create the info2 panel.
		WebPanel info2Pane = new WebPanel(new GridLayout(7, 1, 5, 5));
		infoPane.add(info2Pane, BorderLayout.CENTER);

		// Create name panel.
		WebPanel namePane = new WebPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		info2Pane.add(namePane);

		// Create name title label.
		WebLabel nameTitleLabel = new WebLabel(Msg.getString("ArrivingSettlementDetailPanel.name"), WebLabel.LEFT); //$NON-NLS-1$
		namePane.add(nameTitleLabel);

		// Create name value label.
		nameValueLabel = new WebLabel("", WebLabel.LEFT); //$NON-NLS-1$
		namePane.add(nameValueLabel);

		// Create state panel.
		WebPanel statePane = new WebPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		info2Pane.add(statePane);

		// Create state title label.
		WebLabel stateTitleLabel = new WebLabel(Msg.getString("ArrivingSettlementDetailPanel.state"), WebLabel.LEFT); //$NON-NLS-1$
		statePane.add(stateTitleLabel);

		// Create state value label.
		stateValueLabel = new WebLabel("", WebLabel.LEFT); //$NON-NLS-1$
		statePane.add(stateValueLabel);

		// Create template panel.
		WebPanel templatePane = new WebPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		info2Pane.add(templatePane);

		// Create template title label.
		WebLabel templateTitleLabel = new WebLabel(Msg.getString("ArrivingSettlementDetailPanel.layoutTemplate"), WebLabel.LEFT); //$NON-NLS-1$
		templatePane.add(templateTitleLabel);

		// Create template value label.
		templateValueLabel = new WebLabel("", WebLabel.LEFT); //$NON-NLS-1$
		templatePane.add(templateValueLabel);

		// Create population panel.
		WebPanel populationPane = new WebPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		info2Pane.add(populationPane);

		// Create population title label.
		WebLabel populationTitleLabel = new WebLabel(Msg.getString("ArrivingSettlementDetailPanel.immigrants"), WebLabel.LEFT); //$NON-NLS-1$
		populationPane.add(populationTitleLabel);

		// Create population value label.
		populationValueLabel = new WebLabel("", WebLabel.LEFT); //$NON-NLS-1$
		populationPane.add(populationValueLabel);

		// Create arrival date panel.
		WebPanel arrivalDatePane = new WebPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		info2Pane.add(arrivalDatePane);

		// Create arrival date title label.
		WebLabel arrivalDateTitleLabel = new WebLabel(Msg.getString("ArrivingSettlementDetailPanel.arrivalDate"), WebLabel.LEFT); //$NON-NLS-1$
		arrivalDatePane.add(arrivalDateTitleLabel);

		// Create arrival date value label.
		arrivalDateValueLabel = new WebLabel("", WebLabel.LEFT); //$NON-NLS-1$
		arrivalDatePane.add(arrivalDateValueLabel);

		// Create time arrival panel.
		WebPanel timeArrivalPane = new WebPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		info2Pane.add(timeArrivalPane);

		// Create time arrival title label.
		WebLabel timeArrivalTitleLabel = new WebLabel(Msg.getString("ArrivingSettlementDetailPanel.timeUntilArrival"), WebLabel.LEFT); //$NON-NLS-1$
		timeArrivalPane.add(timeArrivalTitleLabel);

		// Create time arrival value label.
		timeArrivalValueLabel = new WebLabel("", WebLabel.LEFT); //$NON-NLS-1$
		timeArrivalPane.add(timeArrivalValueLabel);

		// Create location panel.
		WebPanel locationPane = new WebPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		info2Pane.add(locationPane);

		// Create location title label.
		WebLabel locationTitleLabel = new WebLabel(Msg.getString("ArrivingSettlementDetailPanel.location"), WebLabel.LEFT); //$NON-NLS-1$
		locationPane.add(locationTitleLabel);

		// Create location value label.
		locationValueLabel = new WebLabel("", WebLabel.LEFT); //$NON-NLS-1$
		locationPane.add(locationValueLabel);

		// Set as clock listener.
		masterClock.addClockListener(this);

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
		arrivalDateValueLabel.setText(arrivingSettlement.getArrivalDate().getDateString());

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
		//MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
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
			//MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
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
	public void clockPulse(double time) {
		// TODO Auto-generated method stub	
	}
	
	@Override
	public void uiPulse(double time) {
		if (desktop.isToolWindowOpen(MissionWindow.NAME)) {
			updateArrival();
		}				
	}
	
	@Override
	public void pauseChange(boolean isPaused, boolean showPane) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Prepares the panel for deletion.
	 */
	public void destroy() {
		if (Simulation.instance().getEventManager() != null)
			Simulation.instance().getEventManager().removeListener(this);
		if (masterClock != null)
			masterClock.removeClockListener(this);
		
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
		masterClock = null;
	}

}