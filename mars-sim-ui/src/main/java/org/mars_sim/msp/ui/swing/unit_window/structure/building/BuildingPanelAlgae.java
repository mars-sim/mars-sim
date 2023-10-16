/*
 * Mars Simulation Project
 * BuildingPanelAlgae.java
 * @date 2023-09-19
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars_sim.msp.core.structure.building.function.farming.AlgaeFarming;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;
import org.mars_sim.tools.Msg;

/**
 * The BuildingPanelAlgae class is a building function panel for
 * the algae pond building.
 */
@SuppressWarnings("serial")
public class BuildingPanelAlgae extends BuildingFunctionPanel {

	private static final String FISH_ICON = "fish";

	// Caches
	private double algaeMass;
	private double idealAlgaeMass; 
	private double maxAlgaeMass;

	private double foodMass;
	private double foodDemand;
	
	private double powerReq;
	
	private JLabel algaeMassLabel;
	private JLabel idealAlgaeMassLabel;
	private JLabel maxAlgaeMassLabel;
	
	private JLabel foodMassLabel;
	private JLabel foodDemandLabel;

	private JLabel powerReqLabel;

	private AlgaeFarming pond;

	
	/**
	 * Constructor.
	 * 
	 * @param The panel for AlgaeFarming
	 * @param The main desktop
	 */
	public BuildingPanelAlgae(AlgaeFarming pond, MainDesktopPane desktop) {
		super(
			Msg.getString("BuildingPanelAlgae.title"), 
			ImageLoader.getIconByName(FISH_ICON), 
			pond.getBuilding(), 
			desktop
		);
		
		this.pond = pond;
	}
	
	/**
	 * Build the UI
	 */
	@Override
	protected void buildUI(JPanel center) {
		AttributePanel labelPanel = new AttributePanel(7);
		center.add(labelPanel, BorderLayout.NORTH);
		
		labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.tankSize"), Integer.toString(pond.getTankSize()), null);
		
		algaeMass = pond.getCurrentAlgae();
		algaeMassLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.algaeMass"),
				StyleManager.DECIMAL_KG2.format(algaeMass), null);
				
		idealAlgaeMass = pond.getIdealAlgae();
		idealAlgaeMassLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.idealAlgaeMass"),
				StyleManager.DECIMAL_KG2.format(idealAlgaeMass), null);
		
		maxAlgaeMass = pond.getMaxAlgae();
		maxAlgaeMassLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.maxAlgaeMass"),
				StyleManager.DECIMAL_KG2.format(maxAlgaeMass), null);

		foodMass = pond.getFoodMass();	
		foodMassLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.foodMass"),
								 StyleManager.DECIMAL_KG2.format(foodMass), null);
		
		foodDemand = pond.getFoodDemand();	
		foodDemandLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.foodDemand"),
								 StyleManager.DECIMAL_PLACES2.format(foodDemand), null);
		
		powerReq = pond.getFullPowerRequired();	
		powerReqLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAlgae.powerReq"),
								 StyleManager.DECIMAL_KW.format(powerReq), null);
	}

	/**
	 * Updates this panel with latest values.
	 */
	@Override
	public void update() {	

		double newAlgae = pond.getCurrentAlgae();
		if (algaeMass != newAlgae) {
			algaeMass = newAlgae;
			algaeMassLabel.setText(StyleManager.DECIMAL_KG2.format(newAlgae));
		}
		
		double newIdealAlgae = pond.getIdealAlgae();
		if (idealAlgaeMass != newIdealAlgae) {
			idealAlgaeMass = newIdealAlgae;
			idealAlgaeMassLabel.setText(StyleManager.DECIMAL_KG2.format(newIdealAlgae));
		}
		
		double newMaxAlgae = pond.getMaxAlgae();
		if (maxAlgaeMass != newMaxAlgae) {
			maxAlgaeMass = newMaxAlgae;
			maxAlgaeMassLabel.setText(StyleManager.DECIMAL_KG2.format(newMaxAlgae));
		}

		double newFoodMass = pond.getFoodMass();
		if (foodMass != newFoodMass) {
			foodMass = newFoodMass;
			foodMassLabel.setText(StyleManager.DECIMAL_KG2.format(newFoodMass));
		}
		
		double newFoodDemand = pond.getFoodDemand();
		if (foodDemand != newFoodDemand) {
			foodDemand = newFoodDemand;
			foodDemandLabel.setText(StyleManager.DECIMAL_PLACES1.format(newFoodDemand));
		}
		
		double newPowerReq = pond.getFullPowerRequired();	
		if (powerReq != newPowerReq) {
			powerReq = newPowerReq;
			powerReqLabel.setText(StyleManager.DECIMAL_KW.format(newPowerReq));
		}
	}
}