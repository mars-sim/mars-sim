//*********************** Navigator Tool Window ***********************
// Last Modified: 4/6/00

// The NavigatorWindow is a tool window that displays virtual Mars 
// and allows the user to navigate around.

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

public class NavigatorWindow extends ToolWindow implements ActionListener, ItemListener {

	// Data members

	private MainDesktopPane desktop;             // Desktop pane
	private MapDisplay surfaceMap;               // Surface map navigation
	private GlobeDisplay globeNav;               // Globe navigation
	private NavButtonDisplay navButtons;         // Compass navigation buttons
	private LegendDisplay legend;                // Topographical and distance legend
	private JCheckBox topoCheck;                 // Topographical view checkbox
	private JTextField latText;                  // Latitude entry
	private JTextField longText;                 // Longitude entry
	private JComboBox latDir;	             // Latitude direction choice
	private JComboBox longDir;                   // Longitude direction choice
	private JButton goThere;                     // Location entry submit button     
	private JCheckBox unitLabelCheckbox;         // Show unit labels checkbox

	// Constructor

	public NavigatorWindow(MainDesktopPane desktop) {
		
		// Use ToolWindow constructor
		
		super("Mars Navigator");
		
		// Set internal frame listener
		
		addInternalFrameListener(new ViewFrameListener());

		// Initialize data members

		this.desktop = desktop;

		// Prepare content pane

		JPanel mainPane = new JPanel();
		mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
		mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(mainPane);
		
		// Prepare top layout panes
		
		JPanel topMainPane = new JPanel();
		topMainPane.setLayout(new BoxLayout(topMainPane, BoxLayout.X_AXIS));
		mainPane.add(topMainPane);
		
		JPanel leftTopPane = new JPanel();
		leftTopPane.setLayout(new BoxLayout(leftTopPane, BoxLayout.Y_AXIS));
		topMainPane.add(leftTopPane);
		
		// Prepare globe display
		
		globeNav = new GlobeDisplay(this);
		JPanel globePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		globePane.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new LineBorder(Color.green)));
		globePane.add(globeNav);
		leftTopPane.add(globePane);
		
		// Prepare navigation buttons display
		
		navButtons = new NavButtonDisplay(this);
		JPanel navPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		navPane.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new LineBorder(Color.green)));
		navPane.add(navButtons);
		leftTopPane.add(navPane);
		
		// Put strut spacer in
		
		topMainPane.add(Box.createHorizontalStrut(5));
		
		// Prepare surface map display
		
		surfaceMap = new MapDisplay(this);
		JPanel mapPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		mapPane.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new LineBorder(Color.green)));
		mapPane.setMaximumSize(new Dimension(306, 306));
		mapPane.add(surfaceMap);
		topMainPane.add(mapPane);

		// Prepare topographical panel

		JPanel topoPane = new JPanel(new BorderLayout());
		topoPane.setBorder(new EmptyBorder(0, 3, 0, 0));
		mainPane.add(topoPane);
		
		// Prepare checkbox panel
		
		JPanel checkBoxPane = new JPanel(new GridLayout(2, 1));
		topoPane.add(checkBoxPane, "West");
		
		// Prepare show topographical map checkbox
		
		topoCheck = new JCheckBox("Topographical Mode");
		topoCheck.setFont(new Font("Helvetica", Font.BOLD, 12));
		topoCheck.addItemListener(this);
		checkBoxPane.add(topoCheck);
		
		// Prepare unit label mode checkbox

		unitLabelCheckbox = new JCheckBox("Show Unit Labels");
		unitLabelCheckbox.setSelected(true);
		unitLabelCheckbox.addItemListener(this);
		checkBoxPane.add(unitLabelCheckbox);
		
		// Prepare legend icon
		
		legend = new LegendDisplay();
		legend.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new LineBorder(Color.green)));
		topoPane.add(legend, "East");
		
		// Prepare position entry panel
		
		JPanel positionPane = new JPanel();
		positionPane.setLayout(new BoxLayout(positionPane, BoxLayout.X_AXIS));
		positionPane.setBorder(new EmptyBorder(6, 6, 3, 3));
		mainPane.add(positionPane);
		
		// Prepare latitude entry components
		
		JLabel latLabel = new JLabel("Latitude: ");
		latLabel.setFont(new Font("Helvetica", Font.BOLD, 12));
		latLabel.setForeground(Color.black);
		latLabel.setAlignmentY(.5F);
		positionPane.add(latLabel);

		latText = new JTextField(5);
		positionPane.add(latText);
		
		String[] latStrings = { "N", "S" };
		latDir = new JComboBox(latStrings);
		latDir.setEditable(false);
		positionPane.add(latDir);
		
		// Put glue and strut spacers in
		
		positionPane.add(Box.createHorizontalGlue());
		positionPane.add(Box.createHorizontalStrut(5));
		
		// Prepare longitude entry components
		
		JLabel longLabel = new JLabel("Longitude: ");
		longLabel.setFont(new Font("Helvetica", Font.BOLD, 12));
		longLabel.setForeground(Color.black);
		longLabel.setAlignmentY(.5F);
		positionPane.add(longLabel);
		
		longText = new JTextField(5);
		positionPane.add(longText);
		
		String[] longStrings = { "E", "W" };
		longDir = new JComboBox(longStrings);
		longDir.setEditable(false);
		positionPane.add(longDir);

		// Put glue and strut spacers in
		
		positionPane.add(Box.createHorizontalGlue());
		positionPane.add(Box.createHorizontalStrut(5));

		// Prepare location entry submit button

		goThere = new JButton("Go There");
		goThere.addActionListener(this);
		goThere.setAlignmentY(.5F);
		positionPane.add(goThere);
		
		// Pack window
		
		pack();
	}
	
	// Update coordinates in map, buttons, and globe
	// Redraw map and globe if necessary
	
	public void updateCoords(Coordinates newCoords) {
		navButtons.updateCoords(newCoords);
		surfaceMap.showMap(newCoords);
		globeNav.showGlobe(newCoords);
	}
	
	// Update coordinates on globe only
	// Redraw globe if necessary
	
	public void updateGlobeOnly(Coordinates newCoords) { globeNav.showGlobe(newCoords); }
	
	// Change topographical mode
	// Redraw legend
	// Redraw map and globe if necessary
	
	public void updateTopo(boolean topoMode) {
		if (topoMode) {
			legend.showColor();
			globeNav.showTopo();
			surfaceMap.showTopo();
		}
		else {
			legend.showMap();
			globeNav.showReal();
			surfaceMap.showReal();
		}
	}

	// ActionListener method overridden

	public void actionPerformed(ActionEvent event) {

		// Read longitude and latitude from user input, translate to radians, 
		// and recenter globe and surface map on that location.

		try {
			double latitude = ((Float) new Float(latText.getText())).doubleValue();
			double longitude = ((Float) new Float(longText.getText())).doubleValue();
			String latDirStr = (String) latDir.getSelectedItem();
			String longDirStr = (String) longDir.getSelectedItem();

			if ((latitude >= 0D) && (latitude <= 90D)) { 
				if ((longitude >= 0D) && (longitude <= 180)) {
					if (latDirStr.equals("N")) latitude = 90D - latitude;
					else latitude += 90D;
					if (longitude > 0D) if (longDirStr.equals("W")) longitude = 360D - longitude;
					double phi = Math.PI * (latitude / 180D);
					double theta = (2 * Math.PI) * (longitude / 360D);
					updateCoords(new Coordinates(phi, theta));
				}
			}
		}
		catch (NumberFormatException e) {}
	}

	// ItemListener method overridden

	public void itemStateChanged(ItemEvent event) {
		
		Object object = event.getSource();
		
		if (object == topoCheck) updateTopo(event.getStateChange() == ItemEvent.SELECTED);
		else if (object == unitLabelCheckbox) surfaceMap.setLabels(unitLabelCheckbox.isSelected());  // Change surface map's label settings
	}
	
	// Returns an array of unit info for all moving vehicles
	
	public UnitInfo[] getMovingVehicleInfo() { return desktop.getMovingVehicleInfo(); }
	
	// Returns an array of unit info for all settlements
	
	public UnitInfo[] getSettlementInfo() { return desktop.getSettlementInfo(); }
	
	// Opens a unit window on the desktop
	
	public void openUnitWindow(int unitID) { desktop.openUnitWindow(unitID); }
}

// Mars Simulation Project
// Copyright (C) 1999 Scott Davis
//
// For questions or comments on this project, contact:
//
// Scott Davis
// 1725 W. Timber Ridge Ln. #6206
// Oak Creek, WI  53154
// scud1@execpc.com
// http://www.execpc.com/~scud1/
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA