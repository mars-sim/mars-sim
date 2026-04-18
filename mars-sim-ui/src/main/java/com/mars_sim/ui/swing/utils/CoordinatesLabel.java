/*
 * Mars Simulation Project
 * CoordinatesLabel.java
 * @date 2025-06-22
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.EntityLabel;
import com.mars_sim.ui.swing.tool.MapSelector;

/**
 * A label that displays a Coordinate and provides buttons to show location on the map.
 */
@SuppressWarnings("serial")
public class CoordinatesLabel extends JPanel {
	
    private JLabel label;
    private Coordinates displayed;
    private JButton mapButton;

    /**
     * Constructor for creating
     * 
     * @param location the Coordinate to display.
     * @param uiContext the UI context for launcher windows
     */
    public CoordinatesLabel(Coordinates location, UIContext uiContext) {
        this(uiContext);
        setCoordinates(location);
    }

    /**
     * Constructor for creating an empty CoordinatesLabel with just the map button.
     * Useful for cases where the coordinates may not be known at the time of creation.
     * @param uiContext the UI context for launcher windows
     */
    public CoordinatesLabel(UIContext uiContext) {

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        label = new JLabel("", SwingConstants.LEFT);
        add(label);
        add(Box.createRigidArea(new Dimension(5, 0)));

        // Some entities have a physical location
        mapButton = new JButton(EntityLabel.LOCATE); 
        mapButton.setToolTipText(Msg.getString("EntityLabel.locate"));
        mapButton.addActionListener(e -> MapSelector.displayCoords(uiContext, displayed));

        add(mapButton);
    }

    /**
     * Sets the coordinates to display and updates the label text and map button state accordingly.
     * @param location Location to display
     */
    public void setCoordinates(Coordinates location) {
        this.displayed = location;
        if (location == null) {
            label.setText("...");
        }
        else {
            label.setText(location.getFormattedString());
        }
        mapButton.setEnabled(location != null);
    }
}
