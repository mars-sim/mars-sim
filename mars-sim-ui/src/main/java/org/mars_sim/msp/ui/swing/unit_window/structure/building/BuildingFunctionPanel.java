/**
 * Mars Simulation Project
 * BuildingFunctionPanel.java
 * @date 2021-10-06
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.label.WebLabel;

/**
 * The BuildingFunctionPanel class is a panel representing a function for a
 * settlement building.
 */
@SuppressWarnings("serial")
public abstract class BuildingFunctionPanel extends TabPanel {

	/** The building this panel is for. */
	protected Building building;

	private boolean isUIDone = false;

	/**
	 * Constructor.
	 * 
	 * @param building The building this panel is for.
	 * @param desktop  The main desktop.
	 * @deprecated
	 */
	public BuildingFunctionPanel(Building building, MainDesktopPane desktop) {
		this("Unknown", building, desktop);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param building The building this panel is for.
	 * @param desktop  The main desktop.
	 */
	public BuildingFunctionPanel(String title, Building building, MainDesktopPane desktop) {
		// User JPanel constructor
		super(title, null, title, building, desktop);

		// Initialize data members
		this.building = building;
	}

	@Override
	public boolean isUIDone() {
		return isUIDone;
	}
	
	@Override
	public void initializeUI() {
		if (!isUIDone) {
			// Create label in top panel
//			WebLabel titleLabel = new WebLabel(getTabTitle(), WebLabel.CENTER);
//			titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
//			topContentPanel.add(titleLabel);

			buildUI(centerContentPanel, bottomContentPanel);
			
			isUIDone = true;
		}	
	}
	
	/**
	 * Build the UI element using the 3 components.
	 * @param centerContentPanel
	 * @param bottomContentPanel
	 */
	protected void buildUI(JPanel centerContentPanel, JPanel bottomContentPanel) {
		// TODO remove this once all building panels migrated
		throw new UnsupportedOperationException("No build UI logic defined");
	}

	/**
	 * Update this panel.
	 */
	@Override
	public void update() {
		// Nothing to update by default
	}
}
