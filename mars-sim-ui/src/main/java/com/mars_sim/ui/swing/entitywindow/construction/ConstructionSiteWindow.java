/**
 * Mars Simulation Project
 * ConstructionSiteWindow.java
 * @date 2023-06-07
 * @author Manny Kung
 */

package com.mars_sim.ui.swing.entitywindow.construction;

import java.util.Properties;

import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityContentPanel;
import com.mars_sim.ui.swing.unit_window.LocationTabPanel;


/**
 * The ConstructionSiteWindow is the window for viewing how a construction site proceeds
 */
public class ConstructionSiteWindow extends EntityContentPanel<ConstructionSite> {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
    /**
     * Constructor.
     *
     * @param constructionSite the construction site to display.
     * @param context the UI context.
     * @param props any initial properties for the panel.
     */
    public ConstructionSiteWindow(ConstructionSite constructionSite, UIContext context, Properties props) {
        // Use UnitWindow constructor
        super(constructionSite, context);

        // Add tab panels
        addTabPanel(new TabPanelSiteGeneral(constructionSite, context));
        addTabPanel(new LocationTabPanel(constructionSite, context));

        applyProps(getUIProps());
    }
}
