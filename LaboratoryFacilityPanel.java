//************************** Laboratory Facility Panel **************************
// Last Modified: 5/22/00

// The LaboratoryFacilityPanel class displays information about a settlement's laboratory facility in the user interface.

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class LaboratoryFacilityPanel extends FacilityPanel {

	// Data members
	
	private LaboratoryFacility laboratory;  // The laboratory facility this panel displays.
	
	// Constructor
	
	public LaboratoryFacilityPanel(LaboratoryFacility laboratory, MainDesktopPane desktop) {
	
		// Use FacilityPanel's constructor
		
		super(desktop);
		
		// Initialize data members
		
		this.laboratory = laboratory;
		tabName = "Lab";
		
		// Set up components
		
		setLayout(new BorderLayout());
		
		// Prepare content pane
		
		JPanel contentPane = new JPanel(new BorderLayout(0, 5));
		add(contentPane, "North");
		
		// Prepare name label
		
		JLabel nameLabel = new JLabel("Laboratory", JLabel.CENTER);
		nameLabel.setForeground(Color.black);
		contentPane.add(nameLabel, "North");
		
		// Prepare info pane
		
		JPanel infoPane = new JPanel(new BorderLayout(0, 5));
		contentPane.add(infoPane, "Center");
		
		// Prepare label panel
		
		JPanel labelPane = new JPanel(new GridLayout(2, 1, 0, 5));
		labelPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
		infoPane.add(labelPane, "North");
		
		// Prepare lab size label
		
		JLabel labSizeLabel = new JLabel("Laboratory Size: " + laboratory.getLaboratorySize(), JLabel.CENTER);
		labSizeLabel.setForeground(Color.black);
		labelPane.add(labSizeLabel);
		
		// Prepare lab tech label
		
		JLabel labTechLabel = new JLabel("Technology Level: " + laboratory.getTechnologyLevel(), JLabel.CENTER);
		labTechLabel.setForeground(Color.black);
		labelPane.add(labTechLabel);
		
		// Prepare tech pane
		
		JPanel techPane = new JPanel();
		techPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
		infoPane.add(techPane, "Center");
		
		// Prepare inner tech pane
		
		JPanel innerTechPane = new JPanel(new BorderLayout());
		techPane.add(innerTechPane);
		
		// Prepare tech label
		
		JLabel techLabel = new JLabel("Research Specialities:", JLabel.CENTER);
		techLabel.setBorder(new EmptyBorder(0, 0, 5, 0));
		techLabel.setForeground(Color.black);
		innerTechPane.add(techLabel, "North");
		
		// Get specialities info
		
		String[] specialities = laboratory.getTechSpecialities();
		
		// Prepare speciality pane
		
		JPanel specialityPane = new JPanel(new GridLayout(specialities.length, 1));
		innerTechPane.add(specialityPane, "Center");
		
		// Prepare speciality labels
		
		JLabel[] specialityLabels = new JLabel[specialities.length];
		for (int x=0; x < specialities.length; x++) {
			specialityLabels[x] = new JLabel(specialities[x], JLabel.CENTER);
			specialityLabels[x].setForeground(Color.black);
			specialityPane.add(specialityLabels[x]);
		}
	}
	
	// Updates the facility panel's information
	
	public void updateInfo() { 
		// Implement later	
	}
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