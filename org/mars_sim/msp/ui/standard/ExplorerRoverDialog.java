/**
 * Mars Simulation Project
 * ExplorerRoverDialog.java
 * @version 2.74 2002-03-17
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard; 
 
import org.mars_sim.msp.simulation.vehicle.*;

/**
 * The ExplorerRoverDialog class is the detail window for an explorer rover.
 */
public class ExplorerRoverDialog extends RoverDialog {
    
    // Data members
    protected ExplorerRover rover;  // Explorer rover related to this detail window
    protected LaboratoryPanel labPane; // The rover's laboratory panel
	
    /** Constructs an ExplorerRoverDialog object 
     *  @param parentDesktop desktop pane
     *  @param ExplorerRoverUIProxy the explorer rover's UI proxy
     */
    public ExplorerRoverDialog(MainDesktopPane parentDesktop, ExplorerRoverUIProxy explorerRoverUIProxy) {
	// Use RoverDialog constructor	
	super(parentDesktop, explorerRoverUIProxy);
    }

    /** Override setupComponents */
    protected void setupComponents() {
		
	// Initialize explorer rover 
	rover = (ExplorerRover) parentUnit;
	
	super.setupComponents();

	labPane = new LaboratoryPanel(rover.getLab(), parentDesktop);
	tabPane.addTab("Lab", labPane);
    }

    /** Override generalUpdate */
    protected void generalUpdate() {
	super.generalUpdate();
        labPane.updateInfo();	
    }
}
