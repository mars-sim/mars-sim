/*
 * Mars Simulation Project
 * BuildingDisplayInfoBean.java
 * @date 2023-04-28
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.unit_display_info;

import javax.swing.Icon;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.ui.swing.ImageLoader;

public class BuildingDisplayInfoBean extends AbstractUnitDisplayInfo {

	private Icon buttonIcon;
	
	public BuildingDisplayInfoBean() {
		// Needs changing
        buttonIcon = ImageLoader.getIconByName("building");
	}

	@Override
	public Icon getButtonIcon(Unit unit) {
		return buttonIcon;
	}
}
