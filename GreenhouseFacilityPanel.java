//************************** Greenhouse Facility Panel **************************
// Last Modified: 5/22/00

// The GreenhouseFacilityPanel class displays information about a settlement's greenhouse facility in the user interface.

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class GreenhouseFacilityPanel extends FacilityPanel {

	// Data members
	
	private GreenhouseFacility greenhouse;  // The greenhouse facility this panel displays.
	private JLabel growingCycleLabel;       // A label to display if growing cycle is active.
	private JProgressBar harvestProgress;   // A progress bar for the growing cycle.
	private JProgressBar tendingProgress;   // A progress bar for the tending work completed.
	
	// Update data cache
	
	private boolean harvestPeriodStarted;   // True if harvest cycle has been started.
	private float harvestPeriodCompleted;   // Number of days completed of current harvest period.
	private float workCompleted;            // Number of work-hours tending greenhouse completed for current harvest.
	
	// Constructor
	
	public GreenhouseFacilityPanel(GreenhouseFacility greenhouse, MainDesktopPane desktop) {
	
		// Use FacilityPanel's constructor
		
		super(desktop);
		
		// Initialize data members
		
		this.greenhouse = greenhouse;
		tabName = "Greenhouse";
		
		// Set up components
		
		setLayout(new BorderLayout());
		
		// Prepare main pane
		
		JPanel contentPane = new JPanel(new BorderLayout(0, 5));
		add(contentPane, "North");
		
		// Prepare greenhouse label
		
		JLabel nameLabel = new JLabel("Greenhouse", JLabel.CENTER);
		nameLabel.setForeground(Color.black);
		contentPane.add(nameLabel, "North");
		
		// Prepare info panel
		
		JPanel infoPane = new JPanel(new BorderLayout(0, 5));
		contentPane.add(infoPane, "Center");
	
		// Prepare label pane
		
		JPanel labelPane = new JPanel(new GridLayout(2, 1));
		labelPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
		infoPane.add(labelPane, "North");
		
		// Prepare max harvest label
		
		JLabel maxHarvestLabel = new JLabel("Full Harvest: " + greenhouse.getHarvestAmount() + " Food", JLabel.CENTER);
		maxHarvestLabel.setForeground(Color.black);
		labelPane.add(maxHarvestLabel);
		
		// Prepare growing cycle label
		
		harvestPeriodStarted = greenhouse.getHarvestPeriodStarted();
		growingCycleLabel = new JLabel("Growing Cycle Inactive", JLabel.CENTER);
		if (harvestPeriodStarted) growingCycleLabel.setText("Growing Cycle Active");
		growingCycleLabel.setForeground(Color.black);
		labelPane.add(growingCycleLabel);
		
		// Prepare lists pane
		
		JPanel listsPane = new JPanel(new GridLayout(2, 1, 0, 5));
		infoPane.add(listsPane, "Center");
		
		// Prepare harvest completion pane
		
		JPanel harvestCompletionPane = new JPanel(new BorderLayout());
		harvestCompletionPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
		listsPane.add(harvestCompletionPane);
		
		// Prepare harvest status label
		
		JLabel harvestStatusLabel = new JLabel("Greenhouse Harvest Status", JLabel.CENTER);
		harvestStatusLabel.setForeground(Color.black);
		harvestCompletionPane.add(harvestStatusLabel, "North");
		
		// Prepare harvest progress bar
		
		harvestPeriodCompleted = greenhouse.getTimeCompleted();
		int percentHarvestCompleted = (int) (100F * (harvestPeriodCompleted / greenhouse.getHarvestPeriod()));
		harvestProgress = new JProgressBar();
		harvestProgress.setValue(percentHarvestCompleted);
		harvestProgress.setStringPainted(true);
		harvestCompletionPane.add(harvestProgress, "Center");
		
		// Prepare tending pane
		
		JPanel tendingPane = new JPanel(new BorderLayout());
		tendingPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
		listsPane.add(tendingPane);
		
		// Prepare tending work label
		
		JLabel tendingWorkLabel = new JLabel("Greenhouse Tending Work Status", JLabel.CENTER);
		tendingWorkLabel.setForeground(Color.black);
		tendingPane.add(tendingWorkLabel, "North");
		
		// Prepare tending progress bar
		
		workCompleted = greenhouse.getWorkCompleted();
		int percentTendingCompleted = (int) (100F * (workCompleted / greenhouse.getWorkLoad()));
		tendingProgress = new JProgressBar();
		tendingProgress.setValue(percentTendingCompleted);
		tendingProgress.setStringPainted(true);
		tendingPane.add(tendingProgress, "Center");
	}
	
	// Updates the facility panel's information
	
	public void updateInfo() { 
	
		// Update growingCycleLabel
		
		if (harvestPeriodStarted != greenhouse.getHarvestPeriodStarted()) {
			harvestPeriodStarted = greenhouse.getHarvestPeriodStarted();
			if (harvestPeriodStarted) growingCycleLabel.setText("Growing Cycle Active");
			else growingCycleLabel.setText("Growing Cycle Inactive");
		}
		
		// Update harvestProgress
		
		if (harvestPeriodCompleted != greenhouse.getTimeCompleted()) {
			harvestPeriodCompleted = greenhouse.getTimeCompleted();
			int percentCompleted = (int) (100F * (harvestPeriodCompleted / greenhouse.getHarvestPeriod()));
			harvestProgress.setValue(percentCompleted);
		}
		
		// Update tendingProgress
		
		if (workCompleted != greenhouse.getWorkCompleted()) {
			workCompleted = greenhouse.getWorkCompleted();
			int percentCompleted = (int) (100F * (workCompleted / greenhouse.getWorkLoad()));
			tendingProgress.setValue(percentCompleted);
		}
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