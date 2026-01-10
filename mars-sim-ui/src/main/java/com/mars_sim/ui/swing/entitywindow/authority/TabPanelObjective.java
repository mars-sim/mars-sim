/*
 * Mars Simulation Project
 * TabPanelObjective.java
 * @date 2025-12-02
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.entitywindow.authority;

import java.awt.BorderLayout;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import com.mars_sim.core.authority.Authority;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * This tab panel shows the Objective of an Authority.
 */
@SuppressWarnings("serial")
class TabPanelObjective extends EntityTabPanel<Authority> {
	
	private static final String TAB_ICON = "sponsor";
	
	/**
	 * Constructor.
	 * 
	 * @param ra the Authority to display.
	 * @param context the UI context.
	 */
	public TabPanelObjective(Authority ra, UIContext context) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelObjective.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(TAB_ICON),
			null,
			context, ra
		);
	}

	@Override
	protected void buildUI(JPanel content) {
	
		JPanel panelNorth = new JPanel();
		panelNorth.setLayout(new BoxLayout(panelNorth, BoxLayout.Y_AXIS));
		content.add(panelNorth, BorderLayout.NORTH);
		
		var ra = getEntity().getMissionAgenda();
		panelNorth.add(SwingHelper.createTextBlock(Msg.getString("TabPanelObjective.agenda"),
						ra.getName()));

		var phase = SwingHelper.createTextBlock(Msg.getString("TabPanelObjective.objective"),
						ra.getObjectiveName());
		panelNorth.add(phase);
		
		// For each phase, add to the text area.
		panelNorth.add(SwingHelper.createTextBlock(Msg.getString("TabPanelObjective.report"), ra.getReports()));
		
		// For each phase, add to the text area.
		panelNorth.add(SwingHelper.createTextBlock(Msg.getString("TabPanelObjective.data"), ra.getData()));
		
		// For each phase, add to the text area.
		var capText = ra.getCapabilities()
				.stream()
				.map(mc -> "- " + mc.getDescription())
				.collect(Collectors.joining("\n"));
		var caps = SwingHelper.createTextBlock(Msg.getString("TabPanelObjective.capability"), capText);
		content.add(caps, BorderLayout.CENTER);
	}
}
