//************************** Living Quarters Facility Panel **************************
// Last Modified: 5/24/00

// The LivingQuartersFacilityPanel class displays information about a settlement's living quarters facility in the user interface.

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class LivingQuartersFacilityPanel extends FacilityPanel implements MouseListener {

	// Data members
	
	private LivingQuartersFacility livingQuarters;  // The living quarters facility this panel displays.
	private JLabel currentPopulationLabel;          // A label that displays the current population.
	private JList inhabitantList;                   // A list to display the names of the current inhabitants.
	
	// Update data cache
	
	private UnitInfo[] inhabitantInfo;              // An array of information on the current inhabitants.
	private int currentPopulation;                  // The current population.
	
	// Constructor
	
	public LivingQuartersFacilityPanel(LivingQuartersFacility livingQuarters, MainDesktopPane desktop) {
	
		// Use FacilityPanel's constructor
		
		super(desktop);
		
		// Initialize data members
		
		this.livingQuarters = livingQuarters;
		tabName = "Quarters";
		currentPopulation = livingQuarters.getCurrentPopulation();
		
		// Set up components
		
		setLayout(new BorderLayout());
		
		// Prepare content pane
		
		JPanel contentPane = new JPanel(new BorderLayout(0, 5));
		add(contentPane, "North");
		
		// Prepare name label
		
		JLabel nameLabel = new JLabel("Living Quarters", JLabel.CENTER);
		nameLabel.setForeground(Color.black);
		contentPane.add(nameLabel, "North");
		
		// Prepare info pane
		
		JPanel infoPane = new JPanel(new BorderLayout(0, 5));
		contentPane.add(infoPane, "Center");
		
		// Prepare label pane
		
		JPanel labelPane = new JPanel(new GridLayout(3, 1));
		labelPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
		infoPane.add(labelPane, "North");
		
		// Prepare normal capacity label
		
		JLabel normalCapacityLabel = new JLabel("Normal Capacity: " + livingQuarters.getNormalCapacity(), JLabel.CENTER);
		normalCapacityLabel.setForeground(Color.black);
		labelPane.add(normalCapacityLabel);
		
		// Prepare maximum capacity label
		
		JLabel maximumCapacityLabel = new JLabel("Maximum Capacity: " + livingQuarters.getMaximumCapacity(), JLabel.CENTER);
		maximumCapacityLabel.setForeground(Color.black);
		labelPane.add(maximumCapacityLabel);
		
		// Prepare current population label
		
		currentPopulationLabel = new JLabel("Current Population: " + currentPopulation, JLabel.CENTER);
		currentPopulationLabel.setForeground(Color.black);
		labelPane.add(currentPopulationLabel);
		
		// Prepare inhabitant panel
		
		JPanel inhabitantPane = new JPanel();
		inhabitantPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
		infoPane.add(inhabitantPane, "Center");
		
		// Prepare inner inhabitant panel
		
		JPanel innerInhabitantPane = new JPanel(new BorderLayout());
		innerInhabitantPane.setPreferredSize(new Dimension(150, 100));
		inhabitantPane.add(innerInhabitantPane);
		
		// Prepare inhabitant list
		
		inhabitantInfo = livingQuarters.getPopulationInfo();
		DefaultListModel inhabitantListModel = new DefaultListModel();
		for (int x=0; x < livingQuarters.getCurrentPopulation(); x++) inhabitantListModel.addElement(inhabitantInfo[x].getName());
		inhabitantList = new JList(inhabitantListModel);
		inhabitantList.setVisibleRowCount(6);
		inhabitantList.addMouseListener(this);
		innerInhabitantPane.add(new JScrollPane(inhabitantList), "Center");	
	}
	
	// Updates the facility panel's information
	
	public void updateInfo() { 
		
		// Update current population
		
		int population = livingQuarters.getCurrentPopulation();
		if (currentPopulation != population) {
			currentPopulation = population;
			currentPopulationLabel.setText("Current Population: " + currentPopulation);
		}
		
		// Update inhabitant list
		
		DefaultListModel model = (DefaultListModel) inhabitantList.getModel();
		inhabitantInfo = livingQuarters.getPopulationInfo();
		
		boolean match = false;
		
		// Check if inhabitant list matches settlement's population
		
		if (model.getSize() == population) {
			match = true;
			for (int x=0; x < population; x++) 
				if (!((String) model.getElementAt(x)).equals(inhabitantInfo[x].getName())) match = false;
		}
		
		// If no match, update inhabitant list
		
		if (!match) {
			model.removeAllElements();
			for (int x=0; x < population; x++) model.addElement(inhabitantInfo[x].getName());
			validate();
		}	
	}
	
	// Implement MouseListener Methods
	
	public void mouseClicked(MouseEvent event) {

		if (event.getClickCount() >= 2) {
  	     		int index = inhabitantList.locationToIndex(event.getPoint());
  	     		if (index > -1) {
  	     			try { desktop.openUnitWindow(inhabitantInfo[index].getID()); }
  	     			catch(NullPointerException e) {}
  	     		}
	 	}
	}
	
	public void mousePressed(MouseEvent event) {}
	public void mouseReleased(MouseEvent event) {}
	public void mouseEntered(MouseEvent event) {}
	public void mouseExited(MouseEvent event) {}
}	

// Mars Simulation Project
// Copyright (C) 2000 Scott Davis
//
// For questions or comments on this project, email:
// mars-sim-users@lists.sourceforge.net
//
// or visit the project's Web site at:
// http://mars-sim@sourceforge.net
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