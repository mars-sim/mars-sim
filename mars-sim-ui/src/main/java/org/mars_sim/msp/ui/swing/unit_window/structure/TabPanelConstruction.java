/*
 * Mars Simulation Project
 * TabPanelConstruction.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.structure.OverrideType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.construction.ConstructionManager;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

@SuppressWarnings("serial")
public class TabPanelConstruction
extends TabPanel {

	private static final String CONST_ICON = "construction";
	
	// Data members
	/** The Settlement instance. */
	private Settlement settlement;
	private ConstructionSitesPanel sitesPanel;
	private ConstructedBuildingsPanel buildingsPanel;
	private JCheckBox overrideCheckbox;

	/**
	 * Constructor.
	 * @param unit the unit the tab panel is for.
	 * @param desktop the desktop.
	 */
	public TabPanelConstruction(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelConstruction.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(CONST_ICON),
			Msg.getString("TabPanelConstruction.title"), //$NON-NLS-1$
			desktop
		);

		settlement = (Settlement) unit;

	}
	
	@Override
	protected void buildUI(JPanel content) {
		
		ConstructionManager manager = settlement.getConstructionManager();

		// Create override panel.
		JPanel overridePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		content.add(overridePanel, BorderLayout.NORTH);

		// Create override check box.
		overrideCheckbox = new JCheckBox(Msg.getString("TabPanelConstruction.checkbox.overrideConstructionAndSalvage")); //$NON-NLS-1$
		overrideCheckbox.setToolTipText(Msg.getString("TabPanelConstruction.tooltip.overrideConstructionAndSalvage")); //$NON-NLS-1$
		overrideCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setConstructionOverride(overrideCheckbox.isSelected());
			}
		});
		overrideCheckbox.setSelected(settlement.getProcessOverride(OverrideType.CONSTRUCTION));
		overridePanel.add(overrideCheckbox);
		
		JPanel mainContentPanel = new JPanel(new GridLayout(2, 1));
		content.add(mainContentPanel, BorderLayout.CENTER);

		sitesPanel = new ConstructionSitesPanel(manager);
		mainContentPanel.add(sitesPanel);

		buildingsPanel = new ConstructedBuildingsPanel(manager);
		mainContentPanel.add(buildingsPanel);
	}

	/**
	 * Sets the settlement construction override.
	 * 
	 * @param constructionOverride true if construction/salvage building missions are overridden.
	 */
	private void setConstructionOverride(boolean constructionOverride) {
		settlement.setProcessOverride(OverrideType.CONSTRUCTION, constructionOverride);
	}

	@Override
	public void update() {
		sitesPanel.update();
		buildingsPanel.update();

		// Update construction override check box if necessary.
		if (settlement.getProcessOverride(OverrideType.CONSTRUCTION) != overrideCheckbox.isSelected()) 
			overrideCheckbox.setSelected(settlement.getProcessOverride(OverrideType.CONSTRUCTION));
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	@Override
	public void destroy() {
		super.destroy();
		
		settlement = null;
		sitesPanel = null;
		buildingsPanel = null;
		overrideCheckbox = null;
	}
}
