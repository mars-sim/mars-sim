//*********************** Search Tool Window ***********************
// Last Modified: 4/5/00

// The SearchWindow is a tool window that allows the user to search for 
// individual units by name and category.

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

public class SearchWindow extends ToolWindow implements ActionListener, ItemListener, MouseListener, DocumentListener {

	// Data Members
	
	private MainDesktopPane desktop;             // Desktop pane
	private JComboBox searchForSelect;           // Category selecter
	private JList unitList;                      // List of selectable units
	private DefaultListModel unitListModel;      // Model for unit select list
	private JTextField selectTextField;          // Selection text field
	private JLabel statusLabel;                  // Status label for displaying warnings.
	private JCheckBox openWindowCheck;           // Checkbox to indicate if unit window is to be opened.
	private JCheckBox centerMapCheck;            // Checkbox to indicate if map is to be centered on unit.
	private boolean lockUnitList;                // True if unitList selection events should be ignored.
	private boolean lockSearchText;              // True if selectTextField events should be ignored.
	private String[] unitCategoryNames;          // Array of category names.
	
	// Constructor
	
	public SearchWindow(MainDesktopPane desktop) {
	
		// Use ToolWindow constructor
		
		super("Search Tool");
		
		// Initialize locks
		
		lockUnitList = false;
		lockSearchText = false;
		
		// Initialize unitCategoryNames
		
		unitCategoryNames = new String[3];
		unitCategoryNames[0] = "Person";
		unitCategoryNames[1] = "Settlement";
		unitCategoryNames[2] = "Vehicle";
		
		// Set internal frame listener
		
		addInternalFrameListener(new ViewFrameListener());

		// Initialize data members

		this.desktop = desktop;
	
		// Set default font

		setFont(new Font("Helvetica", Font.BOLD, 12));
	
		// Get content pane
		
		JPanel mainPane = new JPanel(new BorderLayout());
		mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(mainPane);
		
		// Create search for panel
		
		JPanel searchForPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		mainPane.add(searchForPane, "North");
		
		// Create search for label
		
		JLabel searchForLabel = new JLabel("Search for: ");
		searchForLabel.setForeground(Color.black);
		searchForPane.add(searchForLabel);
		
		// Create search for select
		
		String[] categoryStrings = { "People", "Settlements", "Vehicles" };
		searchForSelect = new JComboBox(categoryStrings);
		searchForSelect.setSelectedIndex(0);
		searchForSelect.addItemListener(this);
		searchForPane.add(searchForSelect);
		
		// Create select unit panel
		
		JPanel selectUnitPane = new JPanel(new BorderLayout());
		mainPane.add(selectUnitPane, "Center");
		
		// Create select text field
		
		selectTextField = new JTextField();
		selectTextField.getDocument().addDocumentListener(this);
		selectUnitPane.add(selectTextField, "North");
		
		// Create unit list
		
		UnitInfo[] peopleInfo = desktop.getPeopleInfo();
		unitListModel = new DefaultListModel();
		for (int x=0; x < peopleInfo.length; x++) unitListModel.addElement(peopleInfo[x].getName());
		
		unitList = new JList(unitListModel);
		unitList.setSelectedIndex(0);
		unitList.addMouseListener(this);
		selectUnitPane.add(new JScrollPane(unitList), "Center");	
		
		// Create bottom panel
		
		JPanel bottomPane = new JPanel(new BorderLayout());
		mainPane.add(bottomPane, "South");
		
		// Create select options panel
		
		JPanel selectOptionsPane = new JPanel(new GridLayout(2, 1));
		bottomPane.add(selectOptionsPane, "North");
		
		// Create open window option check box
		
		openWindowCheck = new JCheckBox("Open Detail Window");
		openWindowCheck.setSelected(true);
		selectOptionsPane.add(openWindowCheck);
		
		// Create center map option
		
		centerMapCheck = new JCheckBox("Center Mars Navigator On Unit");
		selectOptionsPane.add(centerMapCheck);

		// Create status label
		
		statusLabel = new JLabel(" ", JLabel.CENTER);
		statusLabel.setBorder(new EtchedBorder());
		statusLabel.setForeground(Color.red);
		bottomPane.add(statusLabel, "Center");
		
		// Create search button panel
		
		JPanel searchButtonPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		bottomPane.add(searchButtonPane, "South");
		
		// Create search button
		
		JButton searchButton = new JButton("Search");
		searchButton.addActionListener(this);
		searchButtonPane.add(searchButton);
		
		// Pack window
		
		pack();
	}
	
	// ActionListener method overridden

	public void actionPerformed(ActionEvent event) {
		
		// Search for named unit when button is pushed.
		
		// Retrieve info on all units of selected category.
		
		UnitInfo[] unitInfo = new UnitInfo[0];
		String category = (String) searchForSelect.getSelectedItem();
		if (category.equals("People")) unitInfo = desktop.getPeopleInfo();
		if (category.equals("Settlements")) unitInfo = desktop.getSettlementInfo();
		if (category.equals("Vehicles")) unitInfo = desktop.getVehicleInfo();
		
		// If entered text equals the name of a unit in this category, take appropriate action.
		
		boolean foundUnit = false;
		for (int x=0; x < unitInfo.length; x++) {
			if (selectTextField.getText().equalsIgnoreCase(unitInfo[x].getName())) {
				foundUnit = true;
				if (openWindowCheck.isSelected()) desktop.openUnitWindow(unitInfo[x].getID());
				if (centerMapCheck.isSelected()) desktop.centerMapGlobe(unitInfo[x].getCoords());
			}
		}
		
		String tempName = unitCategoryNames[searchForSelect.getSelectedIndex()];
		
		// If not found, display "Unit Not Found" in statusLabel.
		
		if (!foundUnit) statusLabel.setText(tempName + " Not Found");
		
		// If there is no text entered, display "Enter The Name Of A Unit" in statusLabel.
		
		if (selectTextField.getText().equals("")) statusLabel.setText("Enter The Name Of A " + tempName);
	}
	
	// ItemListener method overridden

	public void itemStateChanged(ItemEvent event) {
		
		// Change unitList to the appropriate category list
		
		unitListModel.clear();
		UnitInfo[] unitInfo = new UnitInfo[0];
			
		String category = (String) searchForSelect.getSelectedItem();
		if (category.equals("People")) unitInfo = desktop.getPeopleInfo();
		if (category.equals("Settlements")) unitInfo = desktop.getSettlementInfo();
		if (category.equals("Vehicles")) unitInfo = desktop.getVehicleInfo();
			
		lockUnitList = true;
		for (int x=0; x < unitInfo.length; x++) unitListModel.addElement(unitInfo[x].getName());
		unitList.setSelectedIndex(0);
		unitList.ensureIndexIsVisible(0);
		lockUnitList = false;
		
		// Clear statusLabel.
		
		statusLabel.setText(" ");
	}	
	
	// MouseListener methods overridden
	
	public void mouseClicked(MouseEvent event) {}
	public void mousePressed(MouseEvent event) {}
	public void mouseReleased(MouseEvent event) {
		
		if (!lockUnitList) {
			
			// Change search text to selected name.
			
			String selectedUnitName = (String) unitList.getSelectedValue();
			lockSearchText = true;
			if (!selectTextField.getText().equals(selectedUnitName)) selectTextField.setText(selectedUnitName);
			lockSearchText = false;
		}
	}
	public void mouseEntered(MouseEvent event) {}
	public void mouseExited(MouseEvent event) {}
	
	// DocumentListener methods overridden
	
	public void changedUpdate(DocumentEvent event) {}
	public void insertUpdate(DocumentEvent event) { searchTextChange(); }
	public void removeUpdate(DocumentEvent event) { searchTextChange(); }
	
	// Make selection in list depending on what unit names begin with the changed text.
	
	private void searchTextChange() {
		
		if (!lockSearchText) { 
			String searchText = selectTextField.getText().toLowerCase();
			int fitIndex = 0;
			boolean goodFit = false;
			for (int x = unitListModel.size() - 1; x > -1; x--) {
				String unitString = ((String) unitListModel.elementAt(x)).toLowerCase();
				if (unitString.startsWith(searchText)) {
					fitIndex = x;
					goodFit = true;
				}
			}
			if (goodFit) {
				lockUnitList = true;
				unitList.setSelectedIndex(fitIndex);
				unitList.ensureIndexIsVisible(fitIndex);
				lockUnitList = false;
			}
		}
		
		// Clear statusLabel
		
		statusLabel.setText(" ");
	}
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