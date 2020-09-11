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
	
	private static final String UNLOCKED = "UNLOCKED";
	private static final String LOCKED = "LOCKED";
	
	private int capCache;
	private int innerDoorCache;
	private int outerDoorCache;
	private int occupiedCache;
	private int emptyCache;
	private double cycleTimeCache;
	
	private String airlockStateCache;
	private String innerDoorStateCache;
	private String outerDoorStateCache;
	
	private WebLabel capLabel;
	private WebLabel innerDoorLabel;
	private WebLabel outerDoorLabel;
	private WebLabel occupiedLabel;
	private WebLabel emptyLabel;
	private WebLabel airlockStateLabel;
	private WebLabel cycleTimeLabel;
	private WebLabel innerDoorStateLabel;
	private WebLabel outerDoorStateLabel;
	
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
		WebPanel labelPanel = new WebPanel(new GridLayout(10, 1, 0, 0));
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
		outerDoorLabel = new WebLabel(Msg.getString("BuildingPanelEVA.outerDoor.number",
				eva.getNumAwaitingOuterDoor()), WebLabel.CENTER);
		labelPanel.add(outerDoorLabel);

		// Create innerDoorLabel
		innerDoorLabel = new WebLabel(Msg.getString("BuildingPanelEVA.innerDoor.number",
				eva.getNumAwaitingInnerDoor()), WebLabel.CENTER);
		labelPanel.add(innerDoorLabel);
		
		
		if (eva.getAirlock().isInnerDoorLocked())
			innerDoorStateCache = LOCKED;
		else {
			innerDoorStateCache = UNLOCKED;
		}
		// Create innerDoorStateLabel
		innerDoorStateLabel = new WebLabel(Msg.getString("BuildingPanelEVA.innerDoor.state",
				innerDoorStateCache), WebLabel.CENTER);
		labelPanel.add(innerDoorStateLabel);
		
		
		if (eva.getAirlock().isOuterDoorLocked())
			outerDoorStateCache = LOCKED;
		else {
			outerDoorStateCache = UNLOCKED;
		}
		// Create outerDoorStateLabel
		outerDoorStateLabel = new WebLabel(Msg.getString("BuildingPanelEVA.outerDoor.state",
				outerDoorStateCache), WebLabel.CENTER);
		labelPanel.add(outerDoorStateLabel);
		
		
		// Create occupiedLabel
		occupiedLabel = new WebLabel(Msg.getString("BuildingPanelEVA.occupied",
				eva.getNumOccupied()), WebLabel.CENTER);
		labelPanel.add(occupiedLabel);
		
		
		// Create emptyLabel
		emptyLabel = new WebLabel(Msg.getString("BuildingPanelEVA.empty",
				eva.getNumEmptied()), WebLabel.CENTER);
		labelPanel.add(emptyLabel);
		
		
		// Create airlockStateLabel
		airlockStateLabel = new WebLabel(Msg.getString("BuildingPanelEVA.airlock.state",
				eva.getAirlock().getState().toString()), WebLabel.CENTER);
		labelPanel.add(airlockStateLabel);
		
		
		// Create cycleTimeLabel
		cycleTimeLabel = new WebLabel(Msg.getString("BuildingPanelEVA.airlock.cycleTime",
				Math.round(eva.getAirlock().getRemainingCycleTime()*10.0)/10.0), WebLabel.CENTER);
		labelPanel.add(cycleTimeLabel);
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
			innerDoorLabel.setText(Msg.getString("BuildingPanelEVA.innerDoor.number", innerDoorCache));
		}
		
		// Update outerDoorLabel
		if (outerDoorCache != eva.getNumAwaitingOuterDoor()) {
			outerDoorCache = eva.getNumAwaitingOuterDoor();
			outerDoorLabel.setText(Msg.getString("BuildingPanelEVA.outerDoor.number", outerDoorCache));
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
		if (!airlockStateCache.equalsIgnoreCase(eva.getAirlock().getState().toString())) {
			airlockStateCache = eva.getAirlock().getState().toString();
			airlockStateLabel.setText(Msg.getString("BuildingPanelEVA.airlock.state", airlockStateCache));
		}
		
		// Update cycleTimeLabel
		if (cycleTimeCache != eva.getAirlock().getRemainingCycleTime()) {
			cycleTimeCache = Math.round(eva.getAirlock().getRemainingCycleTime()*10.0)/10.0;
			cycleTimeLabel.setText(Msg.getString("BuildingPanelEVA.airlock.cycleTime", cycleTimeCache));
		}
		
		String innerDoorState = "";
		if (eva.getAirlock().isInnerDoorLocked())
			innerDoorState = LOCKED;
		else {
			innerDoorState = UNLOCKED;
		}
		
		// Update innerDoorStateLabel
		if (!innerDoorStateCache.equalsIgnoreCase(innerDoorState)) {
			innerDoorStateCache = innerDoorState;
			innerDoorStateLabel.setText(Msg.getString("BuildingPanelEVA.innerDoor.state", innerDoorState));
		}
		
		String outerDoorState = "";
		if (eva.getAirlock().isOuterDoorLocked())
			outerDoorState = LOCKED;
		else {
			outerDoorState = UNLOCKED;
		}
		
		// Update outerDoorStateLabel
		if (!outerDoorStateCache.equalsIgnoreCase(outerDoorState)) {
			outerDoorStateCache = outerDoorState;
			outerDoorStateLabel.setText(Msg.getString("BuildingPanelEVA.outerDoor.state", outerDoorState));
		}
	}
}
	
