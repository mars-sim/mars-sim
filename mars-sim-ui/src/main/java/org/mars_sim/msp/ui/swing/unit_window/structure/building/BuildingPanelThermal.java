/*
 * Mars Simulation Project
 * BuildingPanelThermal.java
 * @date 2021-10-08
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.HeatMode;
import org.mars_sim.msp.core.structure.building.function.ThermalGeneration;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;

/**
 * The BuildingPanelThermal class is a building function panel representing 
 * the heat production of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelThermal
extends BuildingFunctionPanel {

	// default logger.
	//private static final Logger logger = Logger.getLogger(BuildingPanelThermal.class.getName());

	/** Is the building a heat producer? */
	private boolean hasFurnace;
	/** The heat status textfield. */
	private JTextField statusTF;
	/** The heat production textfield. */
	private JTextField producedTF;
	
	/** Decimal formatter. */
	private DecimalFormat formatter = new DecimalFormat(Msg.getString("BuildingPanelThermal.decimalFormat")); //$NON-NLS-1$

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
		super(furnace.getBuilding(), desktop);

		this.furnace = furnace;
		this.building = furnace.getBuilding();
		
		furnace = building.getThermalGeneration();
			
		setLayout(new BorderLayout());
		
		WebLabel titleLabel = new WebLabel(
				Msg.getString("BuildingPanelThermal.title"), //$NON-NLS-1$
				WebLabel.CENTER);		
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		add(titleLabel, BorderLayout.NORTH);
		
		// Check if the building is a heat producer.
		hasFurnace = building.hasFunction(FunctionType.THERMAL_GENERATION);

		// If heat producer, prepare heat producer label.
		if (hasFurnace) {		
			// Prepare spring layout info panel.
			JPanel infoPanel = new JPanel(new SpringLayout());
//			infoPanel.setBorder(new MarsPanelBorder());
			add(infoPanel, BorderLayout.CENTER);
			
			// Prepare heat status label.
			heatStatusCache = building.getHeatMode();
			WebLabel heatStatusLabel = new WebLabel(
				Msg.getString("BuildingPanelThermal.heatStatus"), JLabel.RIGHT); //$NON-NLS-1$
			infoPanel.add(heatStatusLabel);	

			// Prepare status TF
			WebPanel wrapper0 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
			statusTF = new JTextField();
			statusTF.setText(heatStatusCache.getName()); 
			statusTF.setEditable(false);
			wrapper0.setPreferredSize(new Dimension(150, 24));
			TooltipManager.setTooltip (statusTF, 
					"The status of the heating system",
					TooltipWay.down);
			wrapper0.add(statusTF);
			infoPanel.add(wrapper0);
			
			productionCache = furnace.getGeneratedHeat();		
			WebLabel productionLabel = new WebLabel(	
				Msg.getString("BuildingPanelThermal.heatProduced"), JLabel.RIGHT); //$NON-NLS-1$
			infoPanel.add(productionLabel);
			
			// Prepare heat production TF
			WebPanel wrapper1 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
			producedTF = new JTextField();
			producedTF.setText(formatter.format(productionCache) + " kW"); 
			producedTF.setEditable(false);
			wrapper1.setPreferredSize(new Dimension(150, 24));
			TooltipManager.setTooltip (producedTF, 
					"The heat production of this building",
					TooltipWay.down);
			wrapper1.add(producedTF);
			infoPanel.add(wrapper1);
			
			// Prepare SpringLayout
			SpringUtilities.makeCompactGrid(infoPanel, 2, 2, // rows, cols
					80, 5, // initX, initY
					5, 1); // xPad, yPad
		}
	}

	/**
	 * Update this panel with latest Heat Mode status and amount of heat produced
	 */
	public void update() {	

		// Update heat production if necessary.
		if (hasFurnace) {
			
			// Update heat status if necessary.
			if (!heatStatusCache.equals(building.getHeatMode())) {
				heatStatusCache = building.getHeatMode();			
				statusTF.setText(heatStatusCache.getName());
			}

			double newProductionCache = furnace.getGeneratedHeat();
			if (productionCache != newProductionCache) {
				productionCache = newProductionCache;
				producedTF.setText(formatter.format(productionCache) + " kW");
			}
		}
	}
}
