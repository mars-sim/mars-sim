/**
 * Mars Simulation Project
 * FacilityManager.java
 * @version 2.73 2001-12-07
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard; 
 
import org.mars_sim.msp.simulation.*; 
import org.mars_sim.msp.simulation.structure.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * The GreenhouseFacilityPanel class displays information about a settlement's greenhouse facility in the user interface.
 **/
public class GreenhouseFacilityPanel extends FacilityPanel {
	
    // Data members
    private GreenhouseFacility greenhouse;  // The greenhouse facility this panel displays.
    private JLabel maxHarvestLabel;         // A Label to display the full harvest amount.
    private JLabel growingCycleLabel;       // A label to display if growing cycle is active.
    private JProgressBar growthProgress;    // A progress bar for the growing cycle.
    private JProgressBar tendingProgress;   // A progress bar for the tending work completed.
	
    // Update data cache
    private double fullHarvest;             // The full harvest amount of food.
    private String phase;                   // The phase of the growing cycle that the greenhouse is in.
    private double growthPeriodCompleted;   // Number of days completed of current growth period.
    private double growingWork;             // Number of work-hours tending greenhouse completed for current growth period.
	
    /** Constructs a GreenhouseFacilityPanel object 
     *  @param greenhouse the greenhouse
     *  @param desktop the desktop pane
     */
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
        fullHarvest = greenhouse.getFullHarvestAmount();
	maxHarvestLabel = new JLabel("Full Harvest: " + fullHarvest + " kg Food", JLabel.CENTER);
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
	
    /** Updates the facility panel's information */
    public void updateInfo() { 
	
        // Update full harvest
        if (fullHarvest != greenhouse.getFullHarvestAmount()) {
            fullHarvest = greenhouse.getFullHarvestAmount();
            maxHarvestLabel.setText("Full Harvest: " + fullHarvest + " kg Food");
        }

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
