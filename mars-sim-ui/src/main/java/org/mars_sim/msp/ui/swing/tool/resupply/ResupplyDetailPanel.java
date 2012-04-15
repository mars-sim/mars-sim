/**
 * Mars Simulation Project
 * ResupplyDetailPanel.java
 * @version 3.02 2012-04-05
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import org.mars_sim.msp.ui.swing.MarsPanelBorder;

/**
 * A panel showing a selected resupply mission details.
 */
public class ResupplyDetailPanel extends JPanel {

    /**
     * Constructor
     */
    public ResupplyDetailPanel() {
     
        // Use JPanel constructor
        super();
        
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
        JPanel info2Pane = new JPanel(new GridLayout(4, 1, 5, 5));
        infoPane.add(info2Pane, BorderLayout.CENTER);
        
        // Create destination label.
        JLabel destinationLabel = new JLabel("Destination: ", JLabel.LEFT);
        info2Pane.add(destinationLabel);
        
        // Create arrivalDate label.
        JLabel arrivalDateLabel = new JLabel("Arrival Date: ", JLabel.LEFT);
        info2Pane.add(arrivalDateLabel);
        
        // Create timeArrival label.
        JLabel timeArrivalLabel = new JLabel("Time Until Arrival: ", JLabel.LEFT);
        info2Pane.add(timeArrivalLabel);
        
        // Create immigrants label.
        JLabel immigrantsLabel = new JLabel("Immigrants: ", JLabel.LEFT);
        info2Pane.add(immigrantsLabel);
        
        // Create the outer supply panel.
        JPanel outerSupplyPane = new JPanel();
        outerSupplyPane.setBorder(new TitledBorder("Supplies"));
        add(outerSupplyPane, BorderLayout.CENTER);
        
        // Create the inner supply panel.
        Box innerSupplyPane = Box.createVerticalBox();
        outerSupplyPane.add(new JScrollPane(innerSupplyPane), BorderLayout.CENTER);
    }
}