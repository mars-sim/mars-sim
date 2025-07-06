/*
 * Mars Simulation Project
 * EntityLabel.java
 * @date 2025-06-22
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.components;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.mars_sim.core.Entity;
import com.mars_sim.core.tool.Conversion;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.unit.FixedUnit;
import com.mars_sim.core.unit.MobileUnit;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.tool_window.MapSelector;

/**
 * A label that displays the name of an Entity and provides buttons to show details
 * or location on the map.
 */
@SuppressWarnings("serial")
public class EntityLabel extends JPanel {
	
    private static final Icon DETAILS = ImageLoader.getIconByName("action/details_small");
    private static final Icon LOCATE = ImageLoader.getIconByName("action/locate_small");

    private JLabel label;
    private JButton detailButton;
    private JButton mapButton;
    private Entity subject;

    /**
     * Constructor for creating an EntityLabel with a specific entity.
     * 
     * @param subject the entity to display.
     * @param uiContext the UI context for launcher windows
     */
    public EntityLabel(Entity subject, MainDesktopPane uiContext) {
        this(uiContext);
        setEntity(subject);
    }

    /**
     * Constructor for creating an empty EntityLabel.
     * @param uiContext COntext for launcher windows
     */
    public EntityLabel(MainDesktopPane uiContext) {

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        label = new JLabel("", SwingConstants.LEFT);
        add(label);
        add(Box.createRigidArea(new Dimension(5, 0)));

        detailButton = new JButton(DETAILS); 
        detailButton.setToolTipText(Msg.getString("EntityLabel.details"));
        detailButton.addActionListener(e -> uiContext.showDetails(subject));
        detailButton.setVisible(false);
        add(detailButton);

        // Some entities have a physical location
        mapButton = new JButton(LOCATE); 
        mapButton.setToolTipText(Msg.getString("EntityLabel.locate"));
        mapButton.addActionListener(e -> MapSelector.displayOnMap(uiContext, subject));

        mapButton.setVisible(false);
        add(mapButton);
    }

    /**
     * Sets the entity to be displayed by this label.
     * 
     * @param subject the entity to display.
     */
    public void setEntity(Entity subject) {
        this.subject = subject;
        if (subject == null) {
            label.setText("");
            label.setToolTipText(null);
            detailButton.setVisible(false);
            mapButton.setVisible(false);
        }
        else {
            label.setText(subject.getName());
            var entityType = Conversion.split(subject.getClass().getSimpleName());
            label.setToolTipText(entityType);
            detailButton.setVisible(true);
            mapButton.setVisible((subject instanceof MobileUnit) || (subject instanceof FixedUnit));
        }
    }

}
