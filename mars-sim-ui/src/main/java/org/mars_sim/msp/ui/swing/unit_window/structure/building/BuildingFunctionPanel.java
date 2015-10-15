/**
 * Mars Simulation Project
 * BuildingFunctionPanel.java
 * @version 3.07 2015-01-01

 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.Color;

import javax.swing.JPanel;

import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

/**
 * The BuildingFunctionPanel class is a panel representing a function for
 * a settlement building.
 */
public abstract class BuildingFunctionPanel
extends JPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** The building this panel is for. */
	protected Building building;
	/** The main desktop. */
	protected MainDesktopPane desktop;

	/**
	 * Constructor.
	 * @param building The building this panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingFunctionPanel(Building building, MainDesktopPane desktop) {
		// User JPanel constructor
		super();

		// Initialize data members
		this.building = building;
		this.desktop = desktop;

        //this.setOpaque(false);
        //this.setBackground(new Color(0,0,0,128));

		setBorder(new MarsPanelBorder());
	}

	/**
	 * Update this panel.
	 */
	public abstract void update();
}
