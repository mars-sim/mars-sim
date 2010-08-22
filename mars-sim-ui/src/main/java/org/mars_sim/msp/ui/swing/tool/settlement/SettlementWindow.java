/**
 * Mars Simulation Project
 * SettlementWindow.java
 * @version 3.00 2010-08-22
 * @author Lars Naesbye Christensen
 */

package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.ToolWindow;


/**
 * The SettlementWindow is a tool window that displays the Settlement Map Tool.
 */
public class SettlementWindow extends ToolWindow {
	
    // Tool name
    public static final String NAME = "Settlement Map Tool";

    private JComboBox settlementListBox; // Lists all settlements
    private JComboBox zoomBox; // Lists zoom levels
    private JLabel zoomLabel; // Label for Zoom box
    /**
     * Constructor
     * @param desktop the main desktop panel.
     */
	public SettlementWindow(MainDesktopPane desktop) {

		// Use ToolWindow constructor
		super(NAME, desktop);

		setMaximizable(true);

		// Create content pane
		JPanel mainPane = new JPanel(new BorderLayout());
		mainPane.setBorder(new MarsPanelBorder());
		setContentPane(mainPane);

		// Create top widget pane
		JPanel widgetPane = new JPanel();
		widgetPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		mainPane.add(widgetPane, BorderLayout.WEST);

		// Create bottom (map) pane
		JPanel mapPane = new JPanel(new BorderLayout());
		mainPane.add(mapPane, BorderLayout.SOUTH);

		Object settlements[] = Simulation.instance().getUnitManager()
				.getSettlements().toArray();

		settlementListBox = new JComboBox(settlements);
		settlementListBox.setSelectedIndex(0);
		widgetPane.add(settlementListBox, BorderLayout.WEST);

		// Create zoom label and combobox
		JPanel zoomPane = new JPanel(new BorderLayout());
		zoomLabel = new JLabel("Zoom: ");
		zoomPane.add(zoomLabel, BorderLayout.CENTER);

		String[] zoomLevels = { "50%", "100%", "200%", "400%" };
		zoomBox = new JComboBox(zoomLevels);
		zoomPane.add(zoomBox, BorderLayout.EAST);

		widgetPane.add(zoomPane);

		JPanel buttonsPane = new JPanel();
		JButton rotateButton = new JButton("Rotate");
		JButton recenterButton = new JButton("Recenter");
		JButton labelsButton = new JButton("Labels");
		buttonsPane.add(rotateButton);
		buttonsPane.add(recenterButton);
		buttonsPane.add(labelsButton);
		widgetPane.add(buttonsPane);

		// Add placeholder for upcoming map

		// Pack window.
		pack();
	}

}