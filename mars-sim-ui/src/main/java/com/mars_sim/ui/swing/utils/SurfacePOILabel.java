/*
 * Mars Simulation Project
 * SurfacePOILabel.java
 * @date 2026-07-19
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

import com.mars_sim.core.map.location.SurfacePOI;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.tool.MapSelector;

/**
 * A label that displays the name of a SurfacePOI and provides buttons to show location on the map.
 */
@SuppressWarnings("serial")
public class SurfacePOILabel extends JPanel {

    /**
     * Constructor for creating an label with a specific POI.
     * 
     * @param subject the POI to display.
     * @param uiContext the UI context for launcher windows
     */
    public SurfacePOILabel(SurfacePOI subject, UIContext uiContext) {

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        var label = new JLabel(subject.getName(), SwingConstants.LEFT);
        add(label);
        add(Box.createRigidArea(new Dimension(5, 0)));

        // Some entities have a physical location
        var mapButton = new JButton(EntityLabel.LOCATE); 
        mapButton.setToolTipText(Msg.getString("EntityLabel.locate"));
        mapButton.addActionListener(e -> MapSelector.displayCoords(uiContext, subject.getCoordinates()));

        add(mapButton);
    }
}
