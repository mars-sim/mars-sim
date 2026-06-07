/**
 * Mars Simulation Project
 * BuildingPanelEVA.java
 * @date 2024-06-24
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.entitywindow.building;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.ClassicAirlock;
import com.mars_sim.core.building.function.EVA;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AirlockPanel;

/**
 * The BuildingPanelEVA class presents the EVA activities
 * of a building.
 */
@SuppressWarnings("serial")
class BuildingPanelEVA extends EntityTabPanel<Building> implements TemporalComponent {
	
	private static final String SUIT_ICON = "eva";

	private ClassicAirlock buildingAirlock;

	private AirlockPanel airlockPanel;

	/**
	 * Constructor.
	 * 
	 * @param eva the eva function of a building this panel is for.
	 * @param context the UI context
	 */
	public BuildingPanelEVA(EVA eva, UIContext context) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelEVA.title"), 
			ImageLoader.getIconByName(SUIT_ICON), null,
			context, eva.getBuilding()
		);

		// Initialize data members
		this.buildingAirlock = (ClassicAirlock)eva.getAirlock();
	}
	
	/**
	 * Builds the UI.
	 * 
	 * @param content
	 */
	@Override
	protected void buildUI(JPanel content) {
		
		airlockPanel = new AirlockPanel(buildingAirlock, getContext());
        content.add(airlockPanel, BorderLayout.CENTER);
	}

	@Override
	public void clockUpdate(ClockPulse pulse) {
		airlockPanel.clockUpdate(pulse);
	}

	@Override
    public void destroy() {
		if (airlockPanel != null) {
    		airlockPanel.release();
        }
        super.destroy();
    }
}