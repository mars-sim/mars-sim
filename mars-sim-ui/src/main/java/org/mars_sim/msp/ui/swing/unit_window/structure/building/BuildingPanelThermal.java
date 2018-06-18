/**
 * Mars Simulation Project
 * BuildingPanelThermal.java
 * @version 3.1.0 2017-09-15
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.Font;
import java.awt.GridLayout;
import java.text.DecimalFormat;

import javax.swing.JLabel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.ThermalGeneration;
import org.mars_sim.msp.core.structure.building.function.HeatMode;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

/**
 * The BuildingPanelThermal class is a building function panel representing 
 * the heat production of a settlement building.
 */
//TODO: list individual power source in building tab and the power generated from that source
public class BuildingPanelThermal
extends BuildingFunctionPanel {

	// default logger.
	//private static Logger logger = Logger.getLogger(BuildingPanelThermal.class.getName());

	/** Is the building a heat producer? */
	private boolean hasFurnace;
	/** The heat status label. */
	private JLabel heatStatusLabel;
	/** The heat production label. */
	private JLabel productionLabel;
	/** The heat used label. */
	//private JLabel usedLabel;
	/** Decimal formatter. */
	private DecimalFormat formatter = new DecimalFormat(Msg.getString("BuildingPanelThermal.decimalFormat")); //$NON-NLS-1$

	// Caches
	/** The heat status cache. */
	private HeatMode heatStatusCache;
	/** The heat production cache. */
	private double productionCache;
	/** The heat used cache. */
	
	// 2014-10-25 Added heat sources to the building tab
	private ThermalGeneration furnace;

	
	/**
	 * Constructor.
	 * @param The panel for the Heating System
	 * @param The main desktop
	 */
	//2014-10-28 mkung: Modified the structure of the constructor 
	public BuildingPanelThermal(ThermalGeneration furnace, MainDesktopPane desktop) {
		super(furnace.getBuilding(), desktop);
		// Use BuildingFunctionPanel constructor
		//super(building, desktop);
		
		this.furnace = furnace;
		this.building = furnace.getBuilding();
		
		furnace = building.getThermalGeneration();//(ThermalGeneration) building.getFunction(BuildingFunction.THERMAL_GENERATION);	
			
		// Check if the building is a heat producer.
		hasFurnace = building.hasFunction(FunctionType.THERMAL_GENERATION);

		// If heat producer, prepare heat producer label.
		if (hasFurnace) {
			 setLayout(new GridLayout(3, 1, 0, 0));
			
			// 2014-11-21 Changed font type, size and color and label text
			JLabel titleLabel = new JLabel(
					Msg.getString("BuildingPanelThermal.title"), //$NON-NLS-1$
					JLabel.CENTER);		
			titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
			//titleLabel.setForeground(new Color(102, 51, 0)); // dark brown
			add(titleLabel);
			
			// Prepare heat status label.
			heatStatusCache = building.getHeatMode();
			heatStatusLabel = new JLabel(
				Msg.getString("BuildingPanelThermal.heatStatus", heatStatusCache.getName()), //$NON-NLS-1$
				JLabel.CENTER
			);
			add(heatStatusLabel);	
				
			productionCache = furnace.getGeneratedHeat();		
			productionLabel = new JLabel(	
				Msg.getString("BuildingPanelThermal.heatProduced", formatter.format(productionCache)), //$NON-NLS-1$
				JLabel.CENTER
			);
			add(productionLabel);
		}
	}

	/**
	 * Update this panel with latest Heat Mode status and amount of heat produced
	 */
	//TODO: still need to extract power that generate heat
	//2014-10-28 mkung: Modified the structure of update() 
	public void update() {	

		// Update heat production if necessary.
		if (hasFurnace) {
			
			// Update heat status if necessary.
			if (!heatStatusCache.equals(building.getHeatMode())) {
				heatStatusCache = building.getHeatMode();			
				heatStatusLabel.setText(Msg.getString("BuildingPanelThermal.heatStatus", heatStatusCache.getName())); //$NON-NLS-1$
			}

			double newProductionCache = furnace.getGeneratedHeat();
			if (productionCache != newProductionCache) {
				productionCache = newProductionCache;
				productionLabel.setText(Msg.getString("BuildingPanelThermal.heatProduced", formatter.format(productionCache))); //$NON-NLS-1$
			}
		}
	}
}