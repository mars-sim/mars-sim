//************************** Greenhouse Facility Panel **************************
// Last Modified: 5/24/00

// The GreenhouseFacilityPanel class displays information about a settlement's greenhouse facility in the user interface.

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class GreenhouseFacilityPanel extends FacilityPanel {

	// Data members
	
	private GreenhouseFacility greenhouse;  // The greenhouse facility this panel displays.
	private JLabel growingCycleLabel;       // A label to display if growing cycle is active.
	private JProgressBar growthProgress;    // A progress bar for the growing cycle.
	private JProgressBar tendingProgress;   // A progress bar for the tending work completed.
	
	// Update data cache
	
	private String phase;                   // The phase of the growing cycle that the greenhouse is in.
	private float growthPeriodCompleted;    // Number of days completed of current growth period.
	private float growingWork;              // Number of work-hours tending greenhouse completed for current growth period.
	
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
		
		JLabel maxHarvestLabel = new JLabel("Full Harvest: " + greenhouse.getFullHarvestAmount() + " Food", JLabel.CENTER);
		maxHarvestLabel.setForeground(Color.black);
		labelPane.add(maxHarvestLabel);
		
		// Prepare growing cycle label
		
		phase = greenhouse.getPhase();
		growingCycleLabel = new JLabel("Growing Cycle Phase: " + phase, JLabel.CENTER);
		growingCycleLabel.setForeground(Color.black);
		labelPane.add(growingCycleLabel);
		
		// Prepare lists pane
		
		JPanel listsPane = new JPanel(new GridLayout(2, 1, 0, 5));
		infoPane.add(listsPane, "Center");
		
		// Prepare growth completion pane
		
		JPanel growthCompletionPane = new JPanel(new BorderLayout());
		growthCompletionPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
		listsPane.add(growthCompletionPane);
		
		// Prepare growth status label
		
		JLabel growthStatusLabel = new JLabel("Greenhouse Growth Cycle Status", JLabel.CENTER);
		growthStatusLabel.setForeground(Color.black);
		growthCompletionPane.add(growthStatusLabel, "North");
		
		// Prepare growth progress bar
		
		growthPeriodCompleted = greenhouse.getTimeCompleted();
		int percentGrowthCompleted = (int) (100F * (growthPeriodCompleted / greenhouse.getGrowthPeriod()));
		growthProgress = new JProgressBar();
		growthProgress.setValue(percentGrowthCompleted);
		growthProgress.setStringPainted(true);
		growthCompletionPane.add(growthProgress, "Center");
		
		// Prepare tending pane
		
		JPanel tendingPane = new JPanel(new BorderLayout());
		tendingPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
		listsPane.add(tendingPane);
		
		// Prepare tending work label
		
		JLabel tendingWorkLabel = new JLabel("Greenhouse Tending Status", JLabel.CENTER);
		tendingWorkLabel.setForeground(Color.black);
		tendingPane.add(tendingWorkLabel, "North");
		
		// Prepare tending progress bar
		
		growingWork = greenhouse.getGrowingWork();
		int percentTendingCompleted = (int) (100F * (growingWork / greenhouse.getWorkLoad()));
		tendingProgress = new JProgressBar();
		tendingProgress.setValue(percentTendingCompleted);
		tendingProgress.setStringPainted(true);
		tendingPane.add(tendingProgress, "Center");
	}
	
	// Updates the facility panel's information
	
	public void updateInfo() { 
	
		// Update growingCycleLabel
		
		if (!phase.equals(greenhouse.getPhase())) {
			phase = greenhouse.getPhase();
			growingCycleLabel.setText("Growing Cycle Phase: " + phase);
		}
		
		// Update growthProgress
		
		if (growthPeriodCompleted != greenhouse.getTimeCompleted()) {
			growthPeriodCompleted = greenhouse.getTimeCompleted();
			int percentCompleted = (int) (100F * (growthPeriodCompleted / greenhouse.getGrowthPeriod()));
			growthProgress.setValue(percentCompleted);
		}
		
		// Update tendingProgress
		
		if (growingWork != greenhouse.getGrowingWork()) {
			growingWork = greenhouse.getGrowingWork();
			int percentCompleted = (int) (100F * (growingWork / greenhouse.getWorkLoad()));
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