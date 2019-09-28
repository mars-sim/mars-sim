/**
 * Mars Simulation Project
 * TabPanelConstruction.java
 * @version 3.1.0 2017-09-22
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.construction.ConstructionManager;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

@SuppressWarnings("serial")
public class TabPanelConstruction
extends TabPanel {

	// Data members
	/** Is UI constructed. */
	private boolean uiDone = false;
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
			null,
			Msg.getString("TabPanelConstruction.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

		settlement = (Settlement) unit;

	}
	
	public boolean isUIDone() {
		return uiDone;
	}
	
	public void initializeUI() {
		uiDone = true;
		
		ConstructionManager manager = settlement.getConstructionManager();

		JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(titlePanel);

		JLabel titleLabel = new JLabel(Msg.getString("TabPanelConstruction.label"), JLabel.CENTER); //$NON-NLS-1$
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		//titleLabel.setForeground(new Color(102, 51, 0)); // dark brown
		titlePanel.add(titleLabel);

		JPanel mainContentPanel = new JPanel(new GridLayout(2, 1));
		centerContentPanel.add(mainContentPanel, BorderLayout.CENTER);

		sitesPanel = new ConstructionSitesPanel(manager);
		mainContentPanel.add(sitesPanel);

		buildingsPanel = new ConstructedBuildingsPanel(manager);
		mainContentPanel.add(buildingsPanel);

		// Create bottom panel.
		JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
//		bottomPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(bottomPanel, BorderLayout.SOUTH);

		// Create override check box.
		overrideCheckbox = new JCheckBox(Msg.getString("TabPanelConstruction.checkbox.overrideConstructionAndSalvage")); //$NON-NLS-1$
		overrideCheckbox.setToolTipText(Msg.getString("TabPanelConstruction.tooltip.overrideConstructionAndSalvage")); //$NON-NLS-1$
		overrideCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setConstructionOverride(overrideCheckbox.isSelected());
			}
		});
		overrideCheckbox.setSelected(settlement.getConstructionOverride());
		bottomPanel.add(overrideCheckbox);
	}

	/**
	 * Sets the settlement construction override.
	 * @param constructionOverride true if construction/salvage building missions are overridden.
	 */
	private void setConstructionOverride(boolean constructionOverride) {
		settlement.setConstructionOverride(constructionOverride);
	}

	@Override
	public void update() {
		if (!uiDone)
			this.initializeUI();
		
		sitesPanel.update();
		buildingsPanel.update();

		// Update construction override check box if necessary.
		if (settlement.getConstructionOverride() != overrideCheckbox.isSelected()) 
			overrideCheckbox.setSelected(settlement.getConstructionOverride());
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		settlement = null;
		sitesPanel = null;
		buildingsPanel = null;
		overrideCheckbox = null;
	}
}