/**
 * Mars Simulation Project
 * BuildingPanelEVA.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.EVA;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;


/**
 * The BuildingPanelEVA class presents the EVA activities 
 * of a building.
 */
@SuppressWarnings("serial")
public class BuildingPanelEVA extends BuildingFunctionPanel {
	
	private int capCache;
	private int innerDoorCache;
	private int outerDoorCache;
	private int occupiedCache;
	private int emptyCache;
	private String airlockStateCache;
	
	private WebLabel capLabel;
	private WebLabel innerDoorLabel;
	private WebLabel outerDoorLabel;
	private WebLabel occupiedLabel;
	private WebLabel emptyLabel;
	private WebLabel airlockStateLabel;
	
	private EVA eva; 

	/**
	 * Constructor.
	 * @param medical the medical care building this panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelEVA(EVA eva, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(eva.getBuilding(), desktop);

		// Initialize data members
		this.eva = eva;

		// Set panel layout
		setLayout(new BorderLayout());

		// Create label panel
		WebPanel labelPanel = new WebPanel(new GridLayout(7, 1, 0, 0));
		add(labelPanel, BorderLayout.NORTH);
		labelPanel.setOpaque(false);
		labelPanel.setBackground(new Color(0,0,0,128));

		// Create medical care label
		WebLabel titleLabel = new WebLabel(Msg.getString("BuildingPanelEVA.title"), WebLabel.CENTER);
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		//medicalCareLabel.setForeground(new Color(102, 51, 0)); // dark brown
		labelPanel.add(titleLabel);

		// Create capacity label
		capLabel = new WebLabel(Msg.getString("BuildingPanelEVA.capacity",
				eva.getAirlockCapacity()), WebLabel.CENTER);
		labelPanel.add(capLabel);

		// Create outerDoorLabel
		outerDoorLabel = new WebLabel(Msg.getString("BuildingPanelEVA.outerDoor",
				eva.getNumAwaitingOuterDoor()), WebLabel.CENTER);
		labelPanel.add(outerDoorLabel);

		// Create innerDoorLabel
		innerDoorLabel = new WebLabel(Msg.getString("BuildingPanelEVA.innerDoor",
				eva.getNumAwaitingInnerDoor()), WebLabel.CENTER);
		labelPanel.add(innerDoorLabel);
		
		// Create occupiedLabel
		occupiedLabel = new WebLabel(Msg.getString("BuildingPanelEVA.occupied",
				eva.getNumOccupied()), WebLabel.CENTER);
		labelPanel.add(occupiedLabel);
		
		// Create emptyLabel
		emptyLabel = new WebLabel(Msg.getString("BuildingPanelEVA.empty",
				eva.getNumEmptied()), WebLabel.CENTER);
		labelPanel.add(emptyLabel);
		
		// Create airlockStateLabel
		airlockStateLabel = new WebLabel(Msg.getString("BuildingPanelEVA.airlockState",
				eva.getAirlock().getState().toString()), WebLabel.CENTER);
		labelPanel.add(airlockStateLabel);
	}

	@Override
	public void update() {
		// Update bedCapLabel
		if (capCache != eva.getAirlockCapacity()) {
			capCache = eva.getAirlockCapacity();
			capLabel.setText(Msg.getString("BuildingPanelEVA.capacity", capCache));
		}

		// Update innerDoorLabel
		if (innerDoorCache != eva.getNumAwaitingInnerDoor()) {
			innerDoorCache = eva.getNumAwaitingInnerDoor();
			innerDoorLabel.setText(Msg.getString("BuildingPanelEVA.innerDoor", innerDoorCache));
		}
		
		// Update outerDoorLabel
		if (outerDoorCache != eva.getNumAwaitingOuterDoor()) {
			outerDoorCache = eva.getNumAwaitingOuterDoor();
			outerDoorLabel.setText(Msg.getString("BuildingPanelEVA.outerDoor", outerDoorCache));
		}
		
		// Update occupiedLabel
		if (occupiedCache != eva.getNumOccupied()) {
			occupiedCache = eva.getNumOccupied();
			occupiedLabel.setText(Msg.getString("BuildingPanelEVA.occupied", occupiedCache));
		}	
		
		// Update emptyLabel
		if (emptyCache != eva.getNumEmptied()) {
			emptyCache = eva.getNumEmptied();
			emptyLabel.setText(Msg.getString("BuildingPanelEVA.empty", emptyCache));
		}	
		
		// Update airlockStateLabel
		if (airlockStateCache.equalsIgnoreCase(eva.getAirlock().getState().toString())) {
			airlockStateCache = eva.getAirlock().getState().toString();
			airlockStateLabel.setText(Msg.getString("BuildingPanelEVA.airlockState", airlockStateCache));
		}
	}
}
	
