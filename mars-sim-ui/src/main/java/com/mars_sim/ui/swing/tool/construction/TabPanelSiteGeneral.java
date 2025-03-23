/**
 * Mars Simulation Project
 * TabPanelSiteGeneral.java
 * @date 2023-06-07
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.construction;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.building.construction.ConstructionStageInfo;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.unit_window.TabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;

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
		String stageInfoType = stageInfo.getType().name().toLowerCase();
		
		infoPanel.addTextField("Site Name", name, null);
		infoPanel.addTextField("Stage Info Name", stageInfoName, null);
		infoPanel.addTextField("Stage Info Type", stageInfoType, null);
	}
}
