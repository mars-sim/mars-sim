/*
 * Mars Simulation Project
 * BuildingPanelThermal.java
 * @date 2022-07-10
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.HeatMode;
import org.mars_sim.msp.core.structure.building.function.ThermalGeneration;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;

/**
 * The BuildingPanelThermal class is a building function panel representing 
 * the heat production of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelThermal
extends BuildingFunctionPanel {

	private static final String HEAT_ICON = Msg.getString("icon.heat"); //$NON-NLS-1$

	/** The heat status textfield. */
	private JTextField statusTF;
	/** The heat production textfield. */
	private JTextField producedTF;

	// Caches
	/** The heat status cache. */
	private HeatMode heatStatusCache;
	/** The heat production cache. */
	private double productionCache;
	/** The ThermalGeneration instance. */
	private ThermalGeneration furnace;
	
	/**
	 * Constructor.
	 * @param The panel for the Heating System
	 * @param The main desktop
	 */
	public BuildingPanelThermal(ThermalGeneration furnace, MainDesktopPane desktop) {
		super(
			Msg.getString("BuildingPanelThermal.title"),
			ImageLoader.getNewIcon(HEAT_ICON), 
			furnace.getBuilding(), 
			desktop
		);

		this.furnace = furnace;
		this.building = furnace.getBuilding();
	}
	
	/**
	 * Build the UI
	 */
	@Override
	protected void buildUI(JPanel center) {
	
		// Prepare spring layout info panel.
		JPanel infoPanel = new JPanel(new SpringLayout());
		center.add(infoPanel, BorderLayout.NORTH);
		
		// Prepare heat status label.
		heatStatusCache = building.getHeatMode();
		statusTF = addTextField(infoPanel, Msg.getString("BuildingPanelThermal.heatStatus"),
								heatStatusCache.getName(), "The status of the heating system");
		
		productionCache = furnace.getGeneratedHeat();		
		producedTF = addTextField(infoPanel, Msg.getString("BuildingPanelThermal.heatProduced"),
								  StyleManager.DECIMAL_KW.format(productionCache), "The heat production of this building");

		// Prepare SpringLayout
		SpringUtilities.makeCompactGrid(infoPanel, 2, 2, // rows, cols
				INITX_DEFAULT, INITY_DEFAULT, // initX, initY
				XPAD_DEFAULT, YPAD_DEFAULT); // xPad, yPad
	}

	/**
	 * Update this panel with latest Heat Mode status and amount of heat produced
	 */
	@Override
	public void update() {	
		// Update heat status if necessary.
		if (!heatStatusCache.equals(building.getHeatMode())) {
			heatStatusCache = building.getHeatMode();			
			statusTF.setText(heatStatusCache.getName());
		}

		double newProductionCache = furnace.getGeneratedHeat();
		if (productionCache != newProductionCache) {
			productionCache = newProductionCache;
			producedTF.setText(StyleManager.DECIMAL_KW.format(productionCache));
		}
	}
}
