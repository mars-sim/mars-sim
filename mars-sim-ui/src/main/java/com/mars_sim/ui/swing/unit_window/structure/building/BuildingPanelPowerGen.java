/*
 * Mars Simulation Project
 * BuildingPanelPowerGen.java
 * @date 2024-06-12
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.util.Iterator;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.utility.power.FissionPowerSource;
import com.mars_sim.core.structure.building.utility.power.PowerGeneration;
import com.mars_sim.core.structure.building.utility.power.PowerMode;
import com.mars_sim.core.structure.building.utility.power.PowerSource;
import com.mars_sim.core.structure.building.utility.power.PowerSourceType;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * The BuildingPanelPowerGen class is a building function panel representing 
 * the power production and use of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelPowerGen
extends BuildingFunctionPanel {

	private static final String POWER_ICON = "power";
	
	/** Is UI constructed. */
	private boolean uiDone = false;
	
	/** Is the building a power producer? */
	private boolean isProducer;
	
	/** The power production cache. */
	private double totalProducedCache;
	/** The total power used cache. */
	private double totalUsedCache;
	/** The max power cache. */
	private double maxPowerCache0;
	/** The max power cache. */
	private double maxPowerCache1;
	/** The load capacity cache. */
	private double loadCapacity0;
	/** The load capacity cache. */
	private double loadCapacity1;
	
	private JLabel powerModeLabel;
	private JLabel totalUsedLabel;
	private JLabel totalProducedLabel;

	private JLabel maxPowerLabel0;
	private JLabel loadCapacityLabel0;
	
	private JLabel maxPowerLabel1;
	private JLabel loadCapacityLabel1;
	
	/** The power status cache. */
	private PowerMode powerModeCache;

	private PowerGeneration generator;
	
	private List<PowerSource> sources;
	
	/**
	 * Constructor.
	 * 
	 * @param building the building the panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelPowerGen(Building building, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelPowerGen.title"), 
			ImageLoader.getIconByName(POWER_ICON),
			building, 
			desktop
		);

		// Check if the building is a power producer.
		isProducer = building.hasFunction(FunctionType.POWER_GENERATION);
		generator = building.getPowerGeneration();
		if (generator != null)
			sources = generator.getPowerSources();
	}

	/**
	 * Builds the UI elements.
	 */
	@Override
	protected void buildUI(JPanel center) {
		
		JPanel panel = new JPanel(new BorderLayout(5, 10));
		AttributePanel springPanel = new AttributePanel(3);
		center.add(panel, BorderLayout.NORTH);
		panel.add(springPanel, BorderLayout.NORTH);
		
		// Prepare power status label.
		powerModeCache = building.getPowerMode();
		
		powerModeLabel = springPanel.addRow(Msg.getString("BuildingPanelPowerGen.powerStatus"),
					powerModeCache.getName());
		
		// Prepare power used label.
		if (powerModeCache == PowerMode.FULL_POWER) 
			totalUsedCache = building.getFullPowerRequired();
		else if (powerModeCache == PowerMode.LOW_POWER) 
			totalUsedCache = building.getLowPowerRequired();
		else 
			totalUsedCache = 0D;
		
		totalUsedLabel = springPanel.addRow(Msg.getString("BuildingPanelPowerGen.powerTotalUsed"),
										StyleManager.DECIMAL_KW.format(totalUsedCache));

		// If power producer, prepare power producer label.
		if (isProducer) {
			totalProducedCache = generator.getGeneratedPower();
			
			totalProducedLabel = springPanel.addRow(Msg.getString("BuildingPanelPowerGen.totalProduced"),
									  StyleManager.DECIMAL_KW.format(totalProducedCache));
			
			int num = sources.size();
			
			AttributePanel sPanel = new AttributePanel(num * 3);

			panel.add(sPanel, BorderLayout.CENTER);
			
			int count = 0;
			Iterator<PowerSource> iP = sources.iterator();
			while (iP.hasNext()) {

				PowerSource powerSource = iP.next();

				sPanel.addRow(Msg.getString("BuildingPanelPowerGen.powerType") 
						+ " " + count,
						powerSource.getType().getName());	

				double maxPowerCache = powerSource.getMaxPower();
				JLabel maxPowerLabel = sPanel.addRow(Msg.getString("BuildingPanelPowerGen.maxPower"),
						StyleManager.DECIMAL_KW.format(maxPowerCache));

				double loadCapacity = 0;
				JLabel loadLabel = null;
				
				if (powerSource.getType() == PowerSourceType.FISSION_POWER
						|| powerSource.getType() == PowerSourceType.THERMIONIC_NUCLEAR_POWER) {
							
					loadCapacity = ((FissionPowerSource)powerSource).getCurrentLoadCapacity();
					loadLabel = sPanel.addRow(Msg.getString("BuildingPanelPowerGen.loadCapacity"),
							Math.round(loadCapacity *10.0)/10.0 + " %");
					break;
				}
				else {
					loadLabel = sPanel.addRow(Msg.getString("BuildingPanelPowerGen.loadCapacity"),
							"None");
				}
				
				if (count == 0) {
					maxPowerCache0 = maxPowerCache;
					maxPowerLabel0 = maxPowerLabel;	
					loadCapacity0 = loadCapacity;
					loadCapacityLabel0 = loadLabel;
				}
				else if (count == 1) {
					maxPowerCache1 = maxPowerCache;
					maxPowerLabel1 = maxPowerLabel;	
					loadCapacity1 = loadCapacity;
					loadCapacityLabel1 = loadLabel;
				}	
				
				count++;
			}
		}
	}

	/**
	 * Updates this panel.
	 */
	@Override
	public void update() {	
		if (!uiDone)
			initializeUI();
		

		// Update power status if necessary.
		PowerMode mode = building.getPowerMode();
		if (powerModeCache != mode) {
			powerModeCache = mode;
			powerModeLabel.setText(mode.getName()); //$NON-NLS-1$
		}


		// Update power used if necessary.
		double totalUsed = 0D;
		if (powerModeCache == PowerMode.FULL_POWER) 
			totalUsed = building.getFullPowerRequired();
		else if (powerModeCache == PowerMode.LOW_POWER) 
			totalUsed = building.getLowPowerRequired();
		
		if (totalUsedCache != totalUsed) {
			totalUsedCache = totalUsed;
			totalUsedLabel.setText(StyleManager.DECIMAL_KW.format(totalUsed)); //$NON-NLS-1$
		}
		
		// Update power production if necessary.
		if (isProducer) {
			double totalProduced = generator.getGeneratedPower();
			if (totalProducedCache != totalProduced) {
				totalProducedCache = totalProduced;
				totalProducedLabel.setText(StyleManager.DECIMAL_KW.format(totalProduced));
			}
			
			int count = 0;
			Iterator<PowerSource> iP = sources.iterator();
			while (iP.hasNext()) {
				PowerSource powerSource = iP.next();

				double maxPower = powerSource.getMaxPower();
				
				if (count == 0) {
					if (maxPowerCache0 != maxPower) {
						maxPowerCache0 = maxPower;
						maxPowerLabel0.setText(StyleManager.DECIMAL_KW.format(maxPower));
					}
				}
				else {
					if (maxPowerCache1 != maxPower) {
						maxPowerCache1 = maxPower;
						maxPowerLabel1.setText(StyleManager.DECIMAL_KW.format(maxPower));
					}
				}
				
				if (powerSource.getType() == PowerSourceType.FISSION_POWER
						|| powerSource.getType() == PowerSourceType.THERMIONIC_NUCLEAR_POWER) {
					
					double loadCapacity = ((FissionPowerSource)powerSource).getCurrentLoadCapacity();
					
					if (count == 0) {
						if (loadCapacity0 != loadCapacity) {
							loadCapacity0 = loadCapacity;
							loadCapacityLabel0.setText(Math.round(loadCapacity *10.0)/10.0 + " %");
						}
					}
					else {
						if (loadCapacity1 != loadCapacity) {
							loadCapacity1 = loadCapacity;
							loadCapacityLabel1.setText(Math.round(loadCapacity *10.0)/10.0 + " %");
						}
					}
				}
				
				count++;
			}
		}
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	@Override
	public void destroy() {
		super.destroy();
		powerModeLabel = null;
		totalProducedLabel = null;

		totalUsedLabel = null;
		powerModeCache = null;
		generator = null;
		
		maxPowerLabel0 = null;
		loadCapacityLabel0 = null;
		
		maxPowerLabel1 = null;	
		loadCapacityLabel1 = null;
	}
}
