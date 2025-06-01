/*
 * Mars Simulation Project
 * JCoordinateEditor.java
 * @date 2025-06-01
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.tool.Msg;

/**
 * This is a Swing component for editing a Coordinate
 */
public class JCoordinateEditor extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final String NORTH = Msg.getString("direction.northShort");
    private static final String SOUTH = Msg.getString("direction.southShort");
    private static final String EAST = Msg.getString("direction.eastShort");
    private static final String WEST = Msg.getString("direction.westShort");

    private JComboBox<String> latCBDir;
    private JComboBox<String> lonCBDir;
    private SpinnerNumberModel latModel;
    private SpinnerNumberModel lonModel;

    public JCoordinateEditor() {
        super();

        initComponents();
    }

    /**
     * Set the coordinate to display
     * @param locn
     */
    public void setCoordinates(Coordinates locn) {
        var latValue = locn.getLatitudeDouble(); 
        latCBDir.setSelectedItem(latValue < 0 ? SOUTH : NORTH);
        latModel.setValue(Math.abs((int)latValue));

        var lonValue = locn.getLongitudeDouble();
        lonCBDir.setSelectedItem(latValue < 0 ? WEST : EAST);
        lonModel.setValue(Math.abs((int)lonValue));
    }

    /**
     * Get the coordinate current displayed
     * @return
     */
    public Coordinates getCoordinates() {
        var latStr = Integer.toString((Integer)latModel.getNumber()) + ' ' + (String)latCBDir.getSelectedItem();
        var lonStr = Integer.toString((Integer)lonModel.getNumber()) + ' ' + (String)lonCBDir.getSelectedItem();

        return new Coordinates(latStr, lonStr);
    }

    private void initComponents() {

        JPanel latPanel = new JPanel(new FlowLayout(SwingConstants.CENTER, 1, 1));
        JLabel latLabel = new JLabel("Lat :", SwingConstants.RIGHT);
		latPanel.add(latLabel);

        latModel = new SpinnerNumberModel(0, 0, 90, 1); 
		var latCB = new JSpinner(latModel);
		latPanel.add(latCB);

        var dim = new Dimension(60, 25);

		String[] latStrings = {NORTH,SOUTH};
		latCBDir = new JComboBox<>(latStrings);
        latCBDir.setPreferredSize(dim);
		latCBDir.setEditable(false);
		latPanel.add(latCBDir);

        add(latPanel);

        var lonPanel = new JPanel();
		JLabel longLabel = new JLabel("Lon :", SwingConstants.RIGHT);
		lonPanel.add(longLabel);

		// Switch to using ComboBoxMW for longitude
        lonModel = new SpinnerNumberModel(0, 0, 180, 1); 
		var lonCB = new JSpinner(lonModel);
		lonPanel.add(lonCB);

		String[] longStrings = {EAST, WEST};
		lonCBDir = new JComboBox<>(longStrings);
        lonCBDir.setPreferredSize(dim);
		lonCBDir.setEditable(false);
		lonPanel.add(lonCBDir);

        add(lonPanel);
    }
}