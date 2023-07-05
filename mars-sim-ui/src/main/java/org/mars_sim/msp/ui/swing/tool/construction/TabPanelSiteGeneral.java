/**
 * Mars Simulation Project
 * TabPanelSiteGeneral.java
 * @date 2023-06-07
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.construction;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.structure.construction.ConstructionStageInfo;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;

/**
 * The TabPanelSiteGeneral is a tab panel for general information about a construction site.
 */
@SuppressWarnings("serial")
public class TabPanelSiteGeneral extends TabPanel {

	private static final String ID_ICON = "info"; //$NON-NLS-1$
	
	/** The ConstructionSite instance. */
	private ConstructionSite constructionSite;
	
	/**
	 * Constructor.
	 * 
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelSiteGeneral(ConstructionSite unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelGeneral.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(ID_ICON),		
			Msg.getString("TabPanelGeneral.title"), //$NON-NLS-1$
			desktop
		);

		constructionSite = unit;
	}
	
	@Override
	protected void buildUI(JPanel content) {

		// Prepare spring layout info panel.
		AttributePanel infoPanel = new AttributePanel(3);
		
		content.add(infoPanel, BorderLayout.NORTH);

		
		String name = constructionSite.getName();
		ConstructionStageInfo stageInfo = constructionSite.getStageInfo();
		String stageInfoName = stageInfo.getName();
		String stageInfoType = stageInfo.getType();
		
		infoPanel.addTextField("Site Name", name, null);
		infoPanel.addTextField("Stage Info Name", stageInfoName, null);
		infoPanel.addTextField("Stage Info Type", stageInfoType, null);
	}
	
	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
	}
}
