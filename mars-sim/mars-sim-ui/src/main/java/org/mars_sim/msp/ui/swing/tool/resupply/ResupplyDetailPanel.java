/**
 * Mars Simulation Project
 * ResupplyDetailPanel.java
 * @version 3.02 2012-04-19
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventListener;
import org.mars_sim.msp.core.events.HistoricalEventManager;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.interplanetary.transport.resupply.ResupplyEvent;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

/**
 * A panel showing a selected resupply mission details.
 */
public class ResupplyDetailPanel extends JPanel implements ClockListener, 
        HistoricalEventListener, ListSelectionListener {

    // Data members
    private Resupply resupply;
    private JLabel destinationValueLabel;
    private JLabel stateValueLabel;
    private JLabel arrivalDateValueLabel;
    private JLabel timeArrivalValueLabel;
    private JLabel immigrantsValueLabel;
    private int solsToArrival = -1;
    
    /**
     * Constructor
     */
    public ResupplyDetailPanel() {
     
        // Use JPanel constructor
        super();
        
        // Initialize data members.
        resupply = null;
        
        setLayout(new BorderLayout(0, 10));
        setBorder(new MarsPanelBorder());
        setPreferredSize(new Dimension(300, 300));
        
        // Create the info panel.
        JPanel infoPane = new JPanel(new BorderLayout());
        add(infoPane, BorderLayout.NORTH);
        
        // Create the title label.
        JLabel titleLabel = new JLabel("Resupply Mission", JLabel.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        titleLabel.setPreferredSize(new Dimension(-1, 25));
        infoPane.add(titleLabel, BorderLayout.NORTH);
        
        // Create the info2 panel.
        JPanel info2Pane = new JPanel(new GridLayout(5, 1, 5, 5));
        infoPane.add(info2Pane, BorderLayout.CENTER);
        
        // Create destination panel.
        JPanel destinationPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        info2Pane.add(destinationPane);
        
        // Create destination title label.
        JLabel destinationTitleLabel = new JLabel("Destination: ", JLabel.LEFT);
        destinationPane.add(destinationTitleLabel);
        
        // Create destination value label.
        destinationValueLabel = new JLabel("", JLabel.LEFT);
        destinationPane.add(destinationValueLabel);
        
        // Create state panel.
        JPanel statePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        info2Pane.add(statePane);
        
        // Create state title label.
        JLabel stateTitleLabel = new JLabel("State: ", JLabel.LEFT);
        statePane.add(stateTitleLabel);
        
        // Create state value label.
        stateValueLabel = new JLabel("", JLabel.LEFT);
        statePane.add(stateValueLabel);
        
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
        
        // Create immigrants panel.
        JPanel immigrantsPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        info2Pane.add(immigrantsPane);
        
        // Create immigrants title label.
        JLabel immigrantsTitleLabel = new JLabel("Immigrants: ", JLabel.LEFT);
        immigrantsPane.add(immigrantsTitleLabel);
        
        // Create immigrants value label.
        immigrantsValueLabel = new JLabel("", JLabel.LEFT);
        immigrantsPane.add(immigrantsValueLabel);
        
        // Create the outer supply panel.
        JPanel outerSupplyPane = new JPanel();
        outerSupplyPane.setBorder(new TitledBorder("Supplies"));
        add(outerSupplyPane, BorderLayout.CENTER);
        
        // Create the inner supply panel.
        Box innerSupplyPane = Box.createVerticalBox();
        outerSupplyPane.add(new JScrollPane(innerSupplyPane), BorderLayout.CENTER);
        
        // Set as clock listener.
        Simulation.instance().getMasterClock().addClockListener(this);
        
        // Set as historical event listener.
        Simulation.instance().getEventManager().addListener(this);
    }
    
    /**
     * Prepares the panel for deletion.
     */
    public void destroy() {
        resupply = null;
        Simulation.instance().getEventManager().removeListener(this);
        Simulation.instance().getMasterClock().removeClockListener(this);
    }
    
    /**
     * Set the resupply mission to show.
     * If resupply is null, clear displayed info.
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
        destinationValueLabel.setText("");
        stateValueLabel.setText("");
        arrivalDateValueLabel.setText("");
        timeArrivalValueLabel.setText("");
        immigrantsValueLabel.setText("");
        
        // TODO
    }

    /**
     * Update the resupply info with the current resupply mission.
     */
    private void updateResupplyInfo() {
        
        destinationValueLabel.setText(resupply.getSettlement().getName());
        
        stateValueLabel.setText(resupply.getState());
        
        arrivalDateValueLabel.setText(resupply.getArrivalDate().getDateString());
        
        updateTimeToArrival();
        
        immigrantsValueLabel.setText(Integer.toString(resupply.getNewImmigrantNum()));
        
        // TODO
    }
    
    /**
     * Update the time to arrival label.
     */
    private void updateTimeToArrival() {
        String timeArrival = "---";
        solsToArrival = -1;
        MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
        double timeDiff = MarsClock.getTimeDiff(resupply.getArrivalDate(), currentTime);
        if (timeDiff > 0D) {
            solsToArrival = (int) Math.abs(timeDiff / 1000D);
            timeArrival = Integer.toString(solsToArrival) + " Sols";
        }
        timeArrivalValueLabel.setText(timeArrival);
    }

    @Override
    public void valueChanged(ListSelectionEvent evt) {
        JList resupplyList = (JList) evt.getSource();
        if (!resupplyList.getValueIsAdjusting()) {
            Resupply newResupply = (Resupply) ((JList) evt.getSource()).getSelectedValue();
            System.out.println("Setting new resupply mission: " + newResupply);
            setResupply(newResupply);
        }
    }

    @Override
    public void eventAdded(int index, HistoricalEvent event) {
        if (HistoricalEventManager.SUPPLY.equals(event.getCategory()) && 
                ResupplyEvent.RESUPPLY_MODIFIED.equals(event.getType())) {
            if ((resupply != null) && event.getSource().equals(resupply)) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        // Update resupply info.
                        if (resupply != null) {
                            updateResupplyInfo();
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
        if ((resupply != null) && (solsToArrival >= 0)) {
            MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
            double timeDiff = MarsClock.getTimeDiff(resupply.getArrivalDate(), currentTime);
            double newSolsToArrival = (int) Math.abs(timeDiff / 1000D);
            if (newSolsToArrival != solsToArrival) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        // Update time to arrival label.
                        if (resupply != null) {
                            updateTimeToArrival();
                        }
                    }
                });
            }
        }
    }
}