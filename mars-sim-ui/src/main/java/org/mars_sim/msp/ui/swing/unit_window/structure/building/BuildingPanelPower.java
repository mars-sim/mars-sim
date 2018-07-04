/**
 * Mars Simulation Project
 * BuildingPanelPower.java
 * @version 3.1.0 2017-09-15
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.DecimalFormat;

import javax.swing.SpringLayout;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.PowerGeneration;
import org.mars_sim.msp.core.structure.building.function.PowerMode;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.text.WebTextField;

/**
 * The BuildingPanelPower class is a building function panel representing 
 * the power production and use of a settlement building.
 */
public class BuildingPanelPower
extends BuildingFunctionPanel {

	private static final String kW = " kW";
	
	/** Is the building a power producer? */
	private boolean isProducer;
	
	/** The power production cache. */
	private double powerCache;
	/** The power used cache. */
	private double usedCache;
	
	private WebTextField statusTF;
	private WebTextField producedTF;
	private WebTextField usedTF;
	
	/** The power status label. */
	private WebLabel powerStatusLabel;
	/** The power production label. */
	private WebLabel powerLabel;
	/** The power used label. */
	private WebLabel usedLabel;
	/** Decimal formatter. */
	private DecimalFormat formatter = new DecimalFormat(Msg.getString("BuildingPanelPower.decimalFormat")); //$NON-NLS-1$

	/** The power status cache. */
	private PowerMode powerStatusCache;

	private PowerGeneration generator;

	/**
	 * Constructor.
	 * @param building the building the panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelPower(Building building, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(building, desktop);

		// Check if the building is a power producer.
		isProducer = building.hasFunction(FunctionType.POWER_GENERATION);
		generator = building.getPowerGeneration();
		
		// Set the layout
		setLayout(new BorderLayout());

		// 2014-11-21 Changed font type, size and color and label text
		WebLabel titleLabel = new WebLabel(
				Msg.getString("BuildingPanelPower.title"), //$NON-NLS-1$
				WebLabel.CENTER);		
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		//titleLabel.setForeground(new Color(102, 51, 0)); // dark brown
		add(titleLabel, BorderLayout.NORTH);
		
		WebPanel springPanel = new WebPanel(new SpringLayout());
		add(springPanel, BorderLayout.CENTER);
		
		// Prepare power status label.
		powerStatusCache = building.getPowerMode();
		powerStatusLabel = new WebLabel(
				Msg.getString("BuildingPanelPower.powerStatus"), //$NON-NLS-1$
				WebLabel.RIGHT);
		springPanel.add(powerStatusLabel);
		
		WebPanel wrapper1 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		statusTF = new WebTextField(powerStatusCache.getName());
		statusTF.setEditable(false);
		statusTF.setColumns(7);
		statusTF.setPreferredSize(new Dimension(120, 25));
		wrapper1.add(statusTF);
		springPanel.add(wrapper1);

		// If power producer, prepare power producer label.
		if (isProducer) {
			//PowerGeneration generator = building.getPowerGeneration();//(PowerGeneration) building.getFunction(FunctionType.POWER_GENERATION);
			powerCache = generator.getGeneratedPower();
			powerLabel = new WebLabel(
				Msg.getString("BuildingPanelPower.powerProduced"), //$NON-NLS-1$
				WebLabel.RIGHT);
			
			springPanel.add(powerLabel);
			
			WebPanel wrapper2 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
			producedTF = new WebTextField(formatter.format(powerCache) + kW);
			producedTF.setEditable(false);
			producedTF.setColumns(7);
			producedTF.setPreferredSize(new Dimension(120, 25));
			wrapper2.add(producedTF);
			springPanel.add(wrapper2);
		}

		// Prepare power used label.
		if (powerStatusCache == PowerMode.FULL_POWER) 
			usedCache = building.getFullPowerRequired();
		else if (powerStatusCache == PowerMode.POWER_DOWN) 
			usedCache = building.getPoweredDownPowerRequired();
		else usedCache = 0D;
		
		usedLabel = new WebLabel(
			Msg.getString("BuildingPanelPower.powerUsed"), //$NON-NLS-1$
			WebLabel.RIGHT
		);
		
		springPanel.add(usedLabel);
		
		WebPanel wrapper3 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		usedTF = new WebTextField(formatter.format(usedCache) + kW);
		usedTF.setEditable(false);
		usedTF.setColumns(7);
		usedTF.setPreferredSize(new Dimension(120, 25));
		wrapper3.add(usedTF);
		springPanel.add(wrapper3);
		
		//Lay out the spring panel.
		if (isProducer) {
			SpringUtilities.makeCompactGrid(springPanel,
		                                3, 2, //rows, cols
		                                75, 10,        //initX, initY
		                                3, 1);       //xPad, yPad
		}
		else
			SpringUtilities.makeCompactGrid(springPanel,
                    2, 2, //rows, cols
                    75, 10,        //initX, initY
                    3, 1);       //xPad, yPad
		}

	/**
	 * Update this panel
	 */
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
				producedTF.setText(formatter.format(powerCache) + kW); //$NON-NLS-1$
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
			usedTF.setText(formatter.format(usedCache) + kW); //$NON-NLS-1$
		}
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		// take care to avoid null exceptions
		formatter = null;
		statusTF = null;
		producedTF = null;
		usedTF = null;
		powerStatusLabel = null;
		powerLabel = null;
		usedLabel = null;
		powerStatusCache = null;
		generator = null;
	}
}