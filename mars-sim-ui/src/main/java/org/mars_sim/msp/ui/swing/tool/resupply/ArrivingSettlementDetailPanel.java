/**
 * Mars Simulation Project
 * ArrivingSettlementDetailPanel.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventListener;
import org.mars_sim.msp.core.events.HistoricalEventManager;
import org.mars_sim.msp.core.interplanetary.transport.TransportEvent;
import org.mars_sim.msp.core.interplanetary.transport.settlement.ArrivingSettlement;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

/**
 * A panel showing a selected arriving settlement details.
 */
public class ArrivingSettlementDetailPanel extends JPanel implements
        ClockListener, HistoricalEventListener {

    // Data members
    private ArrivingSettlement arrivingSettlement;
    private JLabel nameValueLabel;
    private JLabel stateValueLabel;
    private JLabel arrivalDateValueLabel;
    private JLabel timeArrivalValueLabel;
    private int solsToArrival = -1;
    private JLabel templateValueLabel;
    private JLabel locationValueLabel;
    private JLabel populationValueLabel;

    /**
     * Constructor
     */
    public ArrivingSettlementDetailPanel() {

        // Use JPanel constructor.
        super();

        setLayout(new BorderLayout(0, 10));
        setBorder(new MarsPanelBorder());

        // Create the info panel.
        JPanel infoPane = new JPanel(new BorderLayout());
        add(infoPane, BorderLayout.NORTH);

        // Create the title label.
        JLabel titleLabel = new JLabel("Arriving Settlement", JLabel.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        titleLabel.setPreferredSize(new Dimension(-1, 25));
        infoPane.add(titleLabel, BorderLayout.NORTH);

        // Create the info2 panel.
        JPanel info2Pane = new JPanel(new GridLayout(7, 1, 5, 5));
        infoPane.add(info2Pane, BorderLayout.CENTER);

        // Create name panel.
        JPanel namePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        info2Pane.add(namePane);

        // Create name title label.
        JLabel nameTitleLabel = new JLabel("Name: ", JLabel.LEFT);
        namePane.add(nameTitleLabel);

        // Create name value label.
        nameValueLabel = new JLabel("", JLabel.LEFT);
        namePane.add(nameValueLabel);

        // Create state panel.
        JPanel statePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        info2Pane.add(statePane);

        // Create state title label.
        JLabel stateTitleLabel = new JLabel("State: ", JLabel.LEFT);
        statePane.add(stateTitleLabel);

        // Create state value label.
        stateValueLabel = new JLabel("", JLabel.LEFT);
        statePane.add(stateValueLabel);
        
        // Create template panel.
        JPanel templatePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        info2Pane.add(templatePane);

        // Create template title label.
        JLabel templateTitleLabel = new JLabel("Layout Template: ", JLabel.LEFT);
        templatePane.add(templateTitleLabel);

        // Create template value label.
        templateValueLabel = new JLabel("", JLabel.LEFT);
        templatePane.add(templateValueLabel);
        
        // Create population panel.
        JPanel populationPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        info2Pane.add(populationPane);

        // Create population title label.
        JLabel populationTitleLabel = new JLabel("Immigrants: ", JLabel.LEFT);
        populationPane.add(populationTitleLabel);

        // Create population value label.
        populationValueLabel = new JLabel("", JLabel.LEFT);
        populationPane.add(populationValueLabel);

        // Create arrival date panel.
        JPanel arrivalDatePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        info2Pane.add(arrivalDatePane);

        // Create arrival date title label.
        JLabel arrivalDateTitleLabel = new JLabel("Arrival Date: ", JLabel.LEFT);
        arrivalDatePane.add(arrivalDateTitleLabel);

        // Create arrival date value label.
        arrivalDateValueLabel = new JLabel("", JLabel.LEFT);
        arrivalDatePane.add(arrivalDateValueLabel);

        // Create time arrival panel.
        JPanel timeArrivalPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        info2Pane.add(timeArrivalPane);

        // Create time arrival title label.
        JLabel timeArrivalTitleLabel = new JLabel("Time Until Arrival: ", JLabel.LEFT);
        timeArrivalPane.add(timeArrivalTitleLabel);

        // Create time arrival value label.
        timeArrivalValueLabel = new JLabel("", JLabel.LEFT);
        timeArrivalPane.add(timeArrivalValueLabel);

        // Create location panel.
        JPanel locationPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        info2Pane.add(locationPane);

        // Create location title label.
        JLabel locationTitleLabel = new JLabel("Location: ", JLabel.LEFT);
        locationPane.add(locationTitleLabel);

        // Create location value label.
        locationValueLabel = new JLabel("", JLabel.LEFT);
        locationPane.add(locationValueLabel);

        // Set as clock listener.
        Simulation.instance().getMasterClock().addClockListener(this);

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
        nameValueLabel.setText("");
        stateValueLabel.setText("");
        arrivalDateValueLabel.setText("");
        timeArrivalValueLabel.setText("");
        templateValueLabel.setText("");
        locationValueLabel.setText("");
        populationValueLabel.setText("");
    }

    /** 
     * Update displayed information.
     */
    private void updateArrivingSettlementInfo() {

        nameValueLabel.setText(arrivingSettlement.getName());
        stateValueLabel.setText(arrivingSettlement.getTransitState());
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
        String timeArrival = "---";
        solsToArrival = -1;
        MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
        double timeDiff = MarsClock.getTimeDiff(arrivingSettlement.getArrivalDate(), currentTime);
        if (timeDiff > 0D) {
            solsToArrival = (int) Math.abs(timeDiff / 1000D);
            timeArrival = Integer.toString(solsToArrival) + " Sols";
        }
        timeArrivalValueLabel.setText(timeArrival);
    }

    /**
     * Prepares the panel for deletion.
     */
    public void destroy() {
        arrivingSettlement = null;
        Simulation.instance().getEventManager().removeListener(this);
        Simulation.instance().getMasterClock().removeClockListener(this);
    }

    @Override
    public void eventAdded(int index, HistoricalEvent event) {
        if (HistoricalEventManager.TRANSPORT.equals(event.getCategory()) && 
                TransportEvent.TRANSPORT_ITEM_MODIFIED.equals(event.getType())) {
            if ((arrivingSettlement != null) && event.getSource().equals(arrivingSettlement)) {
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

    @Override
    public void clockPulse(double time) {
        // Determine if change in time to arrival display value.
        if ((arrivingSettlement != null) && (solsToArrival >= 0)) {
            MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
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
    public void pauseChange(boolean isPaused) {
        // Do nothing.
    }
}