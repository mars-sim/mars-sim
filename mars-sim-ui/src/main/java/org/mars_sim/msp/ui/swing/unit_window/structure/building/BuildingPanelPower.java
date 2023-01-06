/*
 * Mars Simulation Project
 * BuildingPanelPower.java
 * @date 2022-07-10
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.PowerGeneration;
import org.mars_sim.msp.core.structure.building.function.PowerMode;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;

/**
 * The BuildingPanelPower class is a building function panel representing 
 * the power production and use of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelPower
extends BuildingFunctionPanel {

	private static final String FUSE_ICON = Msg.getString("icon.fuse"); //$NON-NLS-1$
	
	private static final String kW = " kW";
	
	/** Is the building a power producer? */
	private boolean isProducer;
	
	/** The power production cache. */
	private double powerCache;
	/** The power used cache. */
	private double usedCache;
	
	private JTextField statusTF;
	private JTextField producedTF;
	private JTextField usedTF;

	/** The power status cache. */
	private PowerMode powerStatusCache;

	private PowerGeneration generator;

	/**
	 * Constructor.
	 * 
	 * @param building the building the panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelPower(Building building, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelPower.title"), 
			ImageLoader.getNewIcon(FUSE_ICON),
			building, 
			desktop
		);

		// Check if the building is a power producer.
		isProducer = building.hasFunction(FunctionType.POWER_GENERATION);
		generator = building.getPowerGeneration();
	}

	/**
	 * Build the UI elements
	 */
	@Override
	protected void buildUI(JPanel center) {
		
		JPanel springPanel = new JPanel(new SpringLayout());
		center.add(springPanel, BorderLayout.NORTH);
		
		// Prepare power status label.
		powerStatusCache = building.getPowerMode();
		statusTF = addTextField(springPanel, Msg.getString("BuildingPanelPower.powerStatus"),
				                powerStatusCache.getName(), null);

		// If power producer, prepare power producer label.
		if (isProducer) {
			powerCache = generator.getGeneratedPower();
			producedTF = addTextField(springPanel, Msg.getString("BuildingPanelPower.powerProduced"),
									  DECIMAL_PLACES1.format(powerCache) + kW, null);
		}

		// Prepare power used label.
		if (powerStatusCache == PowerMode.FULL_POWER) 
			usedCache = building.getFullPowerRequired();
		else if (powerStatusCache == PowerMode.POWER_DOWN) 
			usedCache = building.getPoweredDownPowerRequired();
		else usedCache = 0D;
		usedTF = addTextField(springPanel, Msg.getString("BuildingPanelPower.powerUsed"),
													DECIMAL_PLACES1.format(usedCache) + kW, null);
		
		//Lay out the spring panel.
		if (isProducer) {
			SpringUtilities.makeCompactGrid(springPanel,
		                                3, 2, //rows, cols
		                                INITX_DEFAULT, INITY_DEFAULT,        //initX, initY
		                                XPAD_DEFAULT, YPAD_DEFAULT);       //xPad, yPad
		}
		else {
			SpringUtilities.makeCompactGrid(springPanel,
                    2, 2, //rows, cols
                    INITX_DEFAULT, INITY_DEFAULT,        //initX, initY
                    XPAD_DEFAULT, YPAD_DEFAULT);       //xPad, yPad
		}
	}

	/**
	 * Update this panel
	 */
	@Override
	public void update() {

		// Update power status if necessary.
		PowerMode mode = building.getPowerMode();
		if (powerStatusCache != mode) {
			powerStatusCache = mode;
			statusTF.setText(powerStatusCache.getName()); //$NON-NLS-1$
		}

		// Update power production if necessary.
		if (isProducer) {
			//PowerGeneration generator = building.getPowerGeneration();//(PowerGeneration) building.getFunction(BuildingFunction.POWER_GENERATION);
			double power = generator.getGeneratedPower();
			if (powerCache != power) {
				powerCache = power;
				producedTF.setText(DECIMAL_PLACES1.format(powerCache) + kW); //$NON-NLS-1$
			}
		}

		// Update power used if necessary.
		double usedPower = 0D;
		if (powerStatusCache == PowerMode.FULL_POWER) 
			usedPower = building.getFullPowerRequired();
		else if (powerStatusCache == PowerMode.POWER_DOWN) 
			usedPower = building.getPoweredDownPowerRequired();
		
		if (usedCache != usedPower) {
			usedCache = usedPower;
			usedTF.setText(DECIMAL_PLACES1.format(usedCache) + kW); //$NON-NLS-1$
		}
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	@Override
	public void destroy() {
		super.destroy();
		
		// take care to avoid null exceptions
		statusTF = null;
		producedTF = null;
		usedTF = null;
		powerStatusCache = null;
		generator = null;
	}
}
