/*
 * Mars Simulation Project
 * BuildingPanelAir
 * @date 2026-11-20
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.entitywindow.building;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import com.mars_sim.core.air.AirComposition;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.AttributePanel;
import com.mars_sim.ui.swing.components.JDoubleLabel;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;

/**
 * The BuildingPanelAir class is a building function panel for
 * tracking the indoor air.
 */
class BuildingPanelAir extends EntityTabPanel<Building> 
	implements TemporalComponent {

	private static final String AIR_ICON = "air";

	private JDoubleLabel totalAirMassLabel;
	private JDoubleLabel airMassFlowRateLabel;
	private JDoubleLabel cumulativeAirLeakageMassLabel;
	
	private AirComposition air;
	
	
	/**
	 * Constructor.
	 * 
	 * @param air
	 * @param context the UI context
	 */
	public BuildingPanelAir(AirComposition air, UIContext context) {
		super(
			Msg.getString("BuildingPanelAir.title"), 
			ImageLoader.getIconByName(AIR_ICON), null,
			context, air.getBuilding()
		);
		
		this.air = air;
	}
	
	/**
	 * Builds the UI.
	 */
	@Override
	protected void buildUI(JPanel center) {
		AttributePanel labelPanel = new AttributePanel();
		center.add(labelPanel, BorderLayout.NORTH);

		totalAirMassLabel = new JDoubleLabel(StyleManager.DECIMAL2_KG, air.getTotalMass());
		labelPanel.addLabelledItem(Msg.getString("BuildingPanelAir.totalAirMass"), totalAirMassLabel);
				
		airMassFlowRateLabel = new JDoubleLabel(StyleManager.DECIMAL2_KG_SOL, air.getMassLeakageFlowRate() * 1000);
		labelPanel.addLabelledItem(Msg.getString("BuildingPanelAir.airMassLeakage"), airMassFlowRateLabel);
		
		cumulativeAirLeakageMassLabel = new JDoubleLabel(StyleManager.DECIMAL2_KG, air.getCumulativeAirLeakageMass());
		labelPanel.addLabelledItem(Msg.getString("BuildingPanelAir.cumulativeAirLeakageMass"), cumulativeAirLeakageMassLabel);
	}

	@Override
	public void clockUpdate(ClockPulse pulse) {

		totalAirMassLabel.setValue(air.getTotalMass());
		
		airMassFlowRateLabel.setValue(air.getMassLeakageFlowRate() * 1000);
		
		cumulativeAirLeakageMassLabel.setValue(air.getCumulativeAirLeakageMass());
	}
}