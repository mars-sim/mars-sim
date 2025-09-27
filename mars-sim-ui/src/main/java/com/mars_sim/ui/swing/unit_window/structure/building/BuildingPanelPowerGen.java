/*
 * Mars Simulation Project
 * BuildingPanelPowerGen.java
 * @date 2025-09-26
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.utility.power.AdjustablePowerSource;
import com.mars_sim.core.building.utility.power.FuelPowerSource;
import com.mars_sim.core.building.utility.power.PowerGeneration;
import com.mars_sim.core.building.utility.power.PowerMode;
import com.mars_sim.core.building.utility.power.PowerSource;
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
	private static final String POWER_TYPE = Msg.getString("BuildingPanelPowerGen.powersource.powerType"); //$NON-NLS-1$
	private static final String MAX_POWER = Msg.getString("BuildingPanelPowerGen.powersource.maxPower"); //$NON-NLS-1$
	private static final String LOAD_CAP = Msg.getString("BuildingPanelPowerGen.powersource.loadCapacity"); //$NON-NLS-1$
	private static final String POWER_GEN = Msg.getString("BuildingPanelPowerGen.powersource.powerGen"); //$NON-NLS-1$
			
	/** The power production cache. */
	private double totalProducedCache;
	/** The total power used cache. */
	private double totalUsedCache;
	
	private JLabel powerModeLabel;
	private JLabel totalUsedLabel;
	private JLabel totalProducedLabel;
	
	/** The power status cache. */
	private PowerMode powerModeCache;

	private PowerGeneration generator;
	
	private List<PowerSource> sources = null;

	protected JLabel[] maxPowerLabels;

	private JLabel[] loadCapLabels;
	
	private JLabel[] powerGenLabels;
	
	/**
	 * Constructor.
	 * 
	 * @param building the building the panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelPowerGen(Building building, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelPowerGen.title"),  //$NON-NLS-1$
			ImageLoader.getIconByName(POWER_ICON),
			building, 
			desktop
		);

		// Check if the building is a power producer.
		generator = building.getFunction(FunctionType.POWER_GENERATION);
		if (generator != null) {
			sources = generator.getPowerSources();
		}
	}

	/**
	 * Builds the UI elements.
	 */
	@Override
	protected void buildUI(JPanel center) {
		
		AttributePanel totalsPanel = new AttributePanel();
		totalsPanel.setBorder(StyleManager.createLabelBorder("Total"));
		center.add(totalsPanel, BorderLayout.NORTH);
		
		// Prepare power status label.
		powerModeCache = building.getPowerMode();
		
		powerModeLabel = totalsPanel.addRow(Msg.getString("BuildingPanelPowerGen.powerStatus"), //$NON-NLS-1$
					powerModeCache.getName());
		
		totalUsedLabel = totalsPanel.addRow(Msg.getString("BuildingPanelPowerGen.powerTotalUsed"), //$NON-NLS-1$
										StyleManager.DECIMAL_KW.format(totalUsedCache));

		// If power producer, prepare power producer label.
		if (generator != null) {
			totalProducedCache = generator.getGeneratedPower();
			
			totalProducedLabel = totalsPanel.addRow(Msg.getString("BuildingPanelPowerGen.totalProduced"), //$NON-NLS-1$
									  StyleManager.DECIMAL_KW.format(totalProducedCache));
			
			int num = sources.size();
			AttributePanel sPanel = new AttributePanel(num * 4);
			sPanel.setBorder(StyleManager.createLabelBorder("Sources"));

			var centerPanel = new JPanel(new BorderLayout());
			center.add(centerPanel, BorderLayout.CENTER);
			centerPanel.add(sPanel, BorderLayout.NORTH);

			maxPowerLabels = new JLabel[num];
			loadCapLabels = new JLabel[num];
			powerGenLabels = new JLabel[num];
			
			int count = 0;
			for(var powerSource : sources) {

				sPanel.addRow(POWER_TYPE + " " + (count + 1), powerSource.getType().getName());

				maxPowerLabels[count] = sPanel.addRow(MAX_POWER, "");
				loadCapLabels[count] = sPanel.addRow(LOAD_CAP, "100 %");
				powerGenLabels[count] = sPanel.addRow(POWER_GEN, "");
				
				count++;
			}
		}

		update();
	}

	/**
	 * Updates this panel.
	 */
	@Override
	public void update() {	

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
		if (generator != null) {
			double totalProduced = generator.getGeneratedPower();
			if (totalProducedCache != totalProduced) {
				totalProducedCache = totalProduced;
				totalProducedLabel.setText(StyleManager.DECIMAL_KW.format(totalProduced));
			}
			
			int count = 0;
			for (var powerSource : sources) {

				double maxPower = powerSource.getMaxPower();
				maxPowerLabels[count].setText(StyleManager.DECIMAL_KW.format(maxPower));
				
				double powerGen = 0;
				if (powerSource instanceof FuelPowerSource fuel) {
					if (fuel.isToggleON())
						powerGen = fuel.measurePower(100);
				}
				else {
					powerGen = powerSource.measurePower(100);
				}
				
				powerGenLabels[count].setText(StyleManager.DECIMAL_KW.format(powerGen));
				
				if (powerSource instanceof AdjustablePowerSource adj) {					
					double loadCapacity = adj.getCurrentLoadCapacity();
					loadCapLabels[count].setText(StyleManager.DECIMAL1_PERC.format(loadCapacity));
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
	}
}
