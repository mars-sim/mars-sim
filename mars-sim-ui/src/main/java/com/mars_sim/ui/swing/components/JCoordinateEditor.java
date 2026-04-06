/*
 * Mars Simulation Project
 * JCoordinateEditor.java
 * @date 2025-06-01
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.components;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
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

    public JCoordinateEditor(boolean vertical) {
        super();
        if (vertical) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        }
        initComponents();
    }

    /**
     * Set the coordinate to display
     * @param locn
     */
    public void setCoordinates(Coordinates locn) {
        var latValue = locn.getLatitudeDouble(); 
        latCBDir.setSelectedItem(latValue < 0 ? SOUTH : NORTH);
        latModel.setValue(Math.abs(latValue));

        var lonValue = locn.getLongitudeDouble();
        lonCBDir.setSelectedItem(lonValue < 0 ? WEST : EAST);
        lonModel.setValue(Math.abs(lonValue));
    }

    /**
     * Get the coordinate current displayed
     * @return
     */
    public Coordinates getCoordinates() {
        var latStr = Double.toString((Double)latModel.getNumber()) + ' ' + (String)latCBDir.getSelectedItem();
        var lonStr = Double.toString((Double)lonModel.getNumber()) + ' ' + (String)lonCBDir.getSelectedItem();

        return new Coordinates(latStr, lonStr);
    }

    private void initComponents() {

        JPanel latPanel = new JPanel(new FlowLayout(SwingConstants.CENTER, 1, 1));
        JLabel latLabel = new JLabel("Lat :", SwingConstants.RIGHT);
		latPanel.add(latLabel);

        latModel = new SpinnerNumberModel(0D, 0D, 90D, 0.001); 
		var latCB = new JSpinner(latModel);
        latCB.setEditor(new JSpinner.NumberEditor(latCB, "#0.000"));
		latPanel.add(latCB);

        var dim = new Dimension(60, 25);

		String[] latStrings = {NORTH,SOUTH};
		latCBDir = new JComboBox<>(latStrings);
        latCBDir.setPreferredSize(dim);
		latCBDir.setEditable(false);
		latPanel.add(latCBDir);

        add(latPanel);

        var lonPanel = new JPanel(new FlowLayout(SwingConstants.CENTER, 1, 1));
		JLabel longLabel = new JLabel("Lon :", SwingConstants.RIGHT);
		lonPanel.add(longLabel);

		// Switch to using ComboBoxMW for longitude
        lonModel = new SpinnerNumberModel(0D, 0D, 180D, 0.001); 
		var lonCB = new JSpinner(lonModel);
        lonCB.setEditor(new JSpinner.NumberEditor(lonCB, "#0.000"));
		lonPanel.add(lonCB);

		String[] longStrings = {EAST, WEST};
		lonCBDir = new JComboBox<>(longStrings);
        lonCBDir.setPreferredSize(dim);
		lonCBDir.setEditable(false);
		lonPanel.add(lonCBDir);

        add(lonPanel);
    }
}