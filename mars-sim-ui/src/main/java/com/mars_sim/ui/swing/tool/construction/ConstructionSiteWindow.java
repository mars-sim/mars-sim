/**
 * Mars Simulation Project
 * ConstructionSiteWindow.java
 * @date 2023-06-07
 * @author Manny Kung
 */

package com.mars_sim.ui.swing.tool.construction;

import com.mars_sim.core.structure.construction.ConstructionSite;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.unit_window.UnitWindow;


/**
 * The ConstructionSiteWindow is the window for viewing how a construction site proceeds
 */
public class ConstructionSiteWindow extends UnitWindow {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	private boolean done;

	private ConstructionSite constructionSite;
	
    /**
     * Constructor.
     *
     * @param desktop the main desktop panel.
     * @param equipment the constructionSite this window is for.
     */
    public ConstructionSiteWindow(MainDesktopPane desktop, ConstructionSite constructionSite) {
        // Use UnitWindow constructor
        super(desktop, constructionSite, constructionSite.getName() 
        		+ " - " + constructionSite.getName(), false);
        this.constructionSite = constructionSite;

        // Add tab panels
        addTabPanel(new TabPanelSiteGeneral(constructionSite, desktop));

        // Sort tab panels
    	sortTabPanels();

		// Add to tab panels.
		addTabIconPanels();
    }

    /**
     * Updates this window.
     */
	@Override
    public void update() {
        super.update();
        //
    }
}
