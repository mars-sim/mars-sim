/*
 * Mars Simulation Project
 * BuildingPanelPowerGen.java
 * @date 2025-09-26
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.entitywindow.building;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.utility.power.AdjustablePowerSource;
import com.mars_sim.core.building.utility.power.FuelPowerSource;
import com.mars_sim.core.building.utility.power.PowerGeneration;
import com.mars_sim.core.building.utility.power.PowerMode;
import com.mars_sim.core.building.utility.power.PowerSource;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.JDoubleLabel;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * The BuildingPanelPowerGen class is a building function panel representing 
 * the power production and use of a settlement building.
 */
@SuppressWarnings("serial")
class BuildingPanelPowerGen extends EntityTabPanel<Building>
	implements TemporalComponent {

	private static final String POWER_ICON = "power";
	private static final String POWER_TYPE = Msg.getString("BuildingPanelPowerGen.powersource.powerType"); //$NON-NLS-1$
	private static final String MAX_POWER = Msg.getString("BuildingPanelPowerGen.powersource.maxPower"); //$NON-NLS-1$
	private static final String LOAD_CAP = Msg.getString("BuildingPanelPowerGen.powersource.loadCapacity"); //$NON-NLS-1$
	private static final String POWER_GEN = Msg.getString("BuildingPanelPowerGen.powersource.powerGen"); //$NON-NLS-1$
			
	private JLabel powerModeLabel;
	private JDoubleLabel totalUsedLabel;
	private JDoubleLabel totalProducedLabel;
	
	/** The power status cache. */
	private PowerMode powerModeCache;

	private PowerGeneration generator;
	
	private record PowerSourceRecord(
		PowerSource source,
		JLabel maxPowerLabel,
		JLabel loadCapLabel,
		JLabel powerGenLabel
	) {
		void refresh(){
			double maxPower = source.getMaxPower();
			maxPowerLabel.setText(StyleManager.DECIMAL_KW.format(maxPower));
				
			double powerGen = 0;
			if (source instanceof FuelPowerSource fuel) {
				if (fuel.isToggleON())
					powerGen = fuel.measurePower(100);
			}
			else {
				powerGen = source.measurePower(100);
			}
				
			powerGenLabel.setText(StyleManager.DECIMAL_KW.format(powerGen));	
			if (source instanceof AdjustablePowerSource adj) {					
				double loadCapacity = adj.getCurrentLoadCapacity();
				loadCapLabel.setText(StyleManager.DECIMAL1_PERC.format(loadCapacity));
			}
		}
	}	
	private List<PowerSourceRecord> sources = new ArrayList<>();

	
	/**
	 * Constructor.
	 * 
	 * @param building the building the panel is for.
	 * @param context the UI context
	 */
	public BuildingPanelPowerGen(Building building, UIContext context) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelPowerGen.title"),
			ImageLoader.getIconByName(POWER_ICON), null,
			context, building
		);

		// Check if the building is a power producer.
		generator = building.getFunction(FunctionType.POWER_GENERATION);
	}

	/**
	 * Builds the UI elements.
	 */
	@Override
	protected void buildUI(JPanel center) {
		
		AttributePanel totalsPanel = new AttributePanel();
		center.add(totalsPanel, BorderLayout.NORTH);
		
		// Prepare power status label.
		var building = getEntity();
		powerModeCache = building.getPowerMode();
		
		powerModeLabel = totalsPanel.addRow(Msg.getString("BuildingPanelPowerGen.powerStatus"), //$NON-NLS-1$
					powerModeCache.getName());
		
		totalUsedLabel = new JDoubleLabel(StyleManager.DECIMAL_KW);
		totalsPanel.addLabelledItem(Msg.getString("BuildingPanelPowerGen.powerTotalUsed"),
										totalUsedLabel);

		// If power producer, prepare power producer label.
		if (generator != null) {
			var totalProducedCache = generator.getGeneratedPower();
			
			totalProducedLabel = new JDoubleLabel(StyleManager.DECIMAL_KW, totalProducedCache);
			totalsPanel.addLabelledItem(Msg.getString("BuildingPanelPowerGen.totalProduced"),
										totalProducedLabel);

			// Create a vertical box for power sources.
			var sourcesPanel = Box.createVerticalBox();
			for(var s : generator.getPowerSources()) {
				sourcesPanel.add(createSourcePanel(s));
			}

			// Add some space.
			var spacePanel = new JPanel(new BorderLayout());
			spacePanel.add(sourcesPanel, BorderLayout.NORTH);
			center.add(spacePanel,  BorderLayout.CENTER);
		}
	}

	/**
	 * Creates a power source panel.
	 * 
	 * @param source The power source.
	 * @return The power source panel.
	 */
	private JPanel createSourcePanel(PowerSource source) {
		int sourceId = sources.size() + 1;
		var sPanel = new AttributePanel();
		sPanel.setBorder(SwingHelper.createLabelBorder("Power Source " + sourceId));

		sPanel.addRow(POWER_TYPE, source.getType().getName());

		var maxPowerLabel = sPanel.addRow(MAX_POWER, "");
		var loadCapLabel = sPanel.addRow(LOAD_CAP, "100 %");
		var powerGenLabel = sPanel.addRow(POWER_GEN, "");
		
		sources.add(new PowerSourceRecord(source, maxPowerLabel, loadCapLabel, powerGenLabel));
		return sPanel;
	}

	/**
	 * Updates this panel on clock pulse. Power production and use values are updated
	 * only if they have changed since the last update.
	 */
	@Override
	public void clockUpdate(ClockPulse pulse) {
		var building = getEntity();

		// Update power status if necessary.
		PowerMode mode = building.getPowerMode();
		if (powerModeCache != mode) {
			powerModeCache = mode;
			powerModeLabel.setText(mode.getName());
		}

		// Update power used if necessary.
		double totalUsed = 0D;
		if (powerModeCache == PowerMode.FULL_POWER) 
			totalUsed = building.getFullPowerRequired();
		else if (powerModeCache == PowerMode.LOW_POWER) 
			totalUsed = building.getLowPowerRequired();
		totalUsedLabel.setValue(totalUsed);

		if (generator == null)
			return;
		double totalProduced = generator.getGeneratedPower();
		totalProducedLabel.setValue(totalProduced);
			
		// Update power production if necessary.
		sources.forEach(PowerSourceRecord::refresh);
	}
}