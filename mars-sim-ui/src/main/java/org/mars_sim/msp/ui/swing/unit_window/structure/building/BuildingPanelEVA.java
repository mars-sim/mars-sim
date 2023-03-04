/*
 * Mars Simulation Project
 * BuildingPanelEVA.java
 * @date 2022-09-12
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Airlock.AirlockMode;
import org.mars_sim.msp.core.structure.building.function.BuildingAirlock;
import org.mars_sim.msp.core.structure.building.function.EVA;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.unit_window.UnitListPanel;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;


/**
 * The BuildingPanelEVA class presents the EVA activities
 * of a building.
 */
@SuppressWarnings("serial")
public class BuildingPanelEVA extends BuildingFunctionPanel {
	
	private static final String SUIT_ICON = "eva";

	private static final String UNLOCKED = "Unlocked";
	private static final String LOCKED = "Locked";

	private int innerDoorCache;
	private int outerDoorCache;
	private int occupiedCache;
	private int emptyCache;
	
	private double cycleTimeCache;
	private boolean activationCache;
	private boolean transitionCache;
	
	private String operatorCache = "";
	private String airlockStateCache = "";
	private String innerDoorStateCache = "";
	private String outerDoorStateCache = "";
	
	private AirlockMode airlockModeCache;

	private JLabel innerDoorLabel;
	private JLabel outerDoorLabel;
	private JLabel occupiedLabel;
	private JLabel emptyLabel;
	private JLabel operatorLabel;
	private JLabel airlockStateLabel;
	private JLabel activationLabel;
	private JLabel transitionLabel;
	private JLabel cycleTimeLabel;
	private JLabel innerDoorStateLabel;
	private JLabel outerDoorStateLabel;
	private JLabel airlockModeLabel;

	private UnitListPanel<Person> occupants;
	private UnitListPanel<Person> reservationList;

	private EVA eva;
	private BuildingAirlock buildingAirlock;

	/**
	 * Constructor.
	 * @param eva the eva function of a building this panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelEVA(EVA eva, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelEVA.title"), 
			ImageLoader.getIconByName(SUIT_ICON), 
			eva.getBuilding(), 
			desktop
		);

		// Initialize data members
		this.eva = eva;
		this.buildingAirlock = (BuildingAirlock)eva.getAirlock();
	}
	
	/**
	 * Build the UI
	 * 
	 * @param content
	 */
	@Override
	protected void buildUI(JPanel content) {

		// Create label panel
		JPanel topPanel = new JPanel(new BorderLayout());
		content.add(topPanel, BorderLayout.NORTH);

		AttributePanel labelGrid = new AttributePanel(6, 2);
		topPanel.add(labelGrid, BorderLayout.NORTH);
		
		// Create innerDoorLabel
		innerDoorLabel = labelGrid.addTextField( Msg.getString("BuildingPanelEVA.innerDoor.number"),
									  Integer.toString(eva.getNumAwaitingInnerDoor()), null);

		if (eva.getAirlock().isInnerDoorLocked())
			innerDoorStateCache = LOCKED;
		else {
			innerDoorStateCache = UNLOCKED;
		}
		// Create innerDoorStateLabel
		innerDoorStateLabel = labelGrid.addTextField( Msg.getString("BuildingPanelEVA.innerDoor.state"),
										   innerDoorStateCache, null);

		// Create outerDoorLabel
		outerDoorLabel = labelGrid.addTextField(Msg.getString("BuildingPanelEVA.outerDoor.number"),
									  Integer.toString(eva.getNumAwaitingOuterDoor()), null);

		if (eva.getAirlock().isOuterDoorLocked())
			outerDoorStateCache = LOCKED;
		else {
			outerDoorStateCache = UNLOCKED;
		}
		// Create outerDoorStateLabel
		outerDoorStateLabel = labelGrid.addTextField(Msg.getString("BuildingPanelEVA.outerDoor.state"),
										   outerDoorStateCache, null);
		
		// Create occupiedLabel
		occupiedLabel = labelGrid.addTextField(Msg.getString("BuildingPanelEVA.occupied"),
									 Integer.toString(eva.getNumInChamber()), null);

		// Create airlockModeLabel
		airlockModeCache = buildingAirlock.getAirlockMode();
		airlockModeLabel = labelGrid.addTextField(Msg.getString("BuildingPanelEVA.airlock.mode"),
				airlockModeCache.getName(), null);

		// Create emptyLabel
		emptyLabel = labelGrid.addTextField( Msg.getString("BuildingPanelEVA.empty"),
								  Integer.toString(eva.getNumEmptied()), null);

		// Create airlockStateLabel
		airlockStateLabel = labelGrid.addTextField( Msg.getString("BuildingPanelEVA.airlock.state"),
										 buildingAirlock.getState().toString(), null);

		// Create cycleTimeLabel
		cycleTimeLabel = labelGrid.addTextField( Msg.getString("BuildingPanelEVA.airlock.cycleTime"),
									  StyleManager.DECIMAL_PLACES1.format(buildingAirlock.getRemainingCycleTime()), null);
		
		// Create transitionLabel
		transitionLabel = labelGrid.addTextField( Msg.getString("BuildingPanelEVA.airlock.transition"),
				 Boolean.toString(buildingAirlock.isTransitioning()), null);

		// Create activationLabel
		activationLabel = labelGrid.addTextField( Msg.getString("BuildingPanelEVA.airlock.activation"),
										 Boolean.toString(buildingAirlock.isActivated()), null);

		// Create OperatorLabel
		operatorLabel = labelGrid.addTextField( Msg.getString("BuildingPanelEVA.operator"),
									 eva.getOperatorName(), null);
		
		
		// Create occupant panel
		JPanel occupantPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		addBorder(occupantPanel, Msg.getString("BuildingPanelEVA.titledB.occupants"));
		content.add(occupantPanel, BorderLayout.CENTER);

		// Create occupant list 
		MainDesktopPane desktop = getDesktop();
		occupants = new UnitListPanel<>(desktop, new Dimension(150, 100)) {
			@Override
			protected Collection<Person> getData() {
				return getUnitsFromIds(buildingAirlock.getAllInsideOccupants());
			}
		};
		occupantPanel.add(occupants);

		// Create reservation panel
		JPanel reservationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		addBorder(reservationPanel, Msg.getString("BuildingPanelEVA.titledB.Reserved"));
		content.add(reservationPanel, BorderLayout.SOUTH);

		reservationList = new UnitListPanel<>(desktop, new Dimension(150, 100)) {
			@Override
			protected Collection<Person> getData() {
				return getUnitsFromIds(buildingAirlock.getReserved());
			}		
		};	
		reservationPanel.add(reservationList);
	}

	@Override
	public void update() {

		// Update innerDoorLabel
		int inner = eva.getNumAwaitingInnerDoor();
		if (innerDoorCache != inner) {
			innerDoorCache = inner;
			innerDoorLabel.setText(Integer.toString(inner));
		}

		// Update outerDoorLabel
		int outer = eva.getNumAwaitingOuterDoor();
		if (outerDoorCache != outer) {
			outerDoorCache = outer;
			outerDoorLabel.setText(Integer.toString(outer));
		}

		// Update occupiedLabel
		int numChamber = eva.getNumInChamber();
		if (occupiedCache != numChamber) {
			occupiedCache = numChamber;
			occupiedLabel.setText(Integer.toString(numChamber));
		}

		// Update emptyLabel
		int emptyNumChamber = eva.getNumEmptied();
		if (emptyCache != emptyNumChamber) {
			emptyCache = emptyNumChamber;
			emptyLabel.setText(Integer.toString(emptyNumChamber));
		}

		// Update operatorLabel
		String name = eva.getOperatorName();
		if (!operatorCache.equalsIgnoreCase(name)) {
			operatorCache = name;
			operatorLabel.setText(name);
		}
		
		// Update cycleTimeLabel
		double time = buildingAirlock.getRemainingCycleTime();
		if (cycleTimeCache != time) {
			cycleTimeCache = time;
			cycleTimeLabel.setText(StyleManager.DECIMAL_PLACES1.format(cycleTimeCache));
		}

		String innerDoorState = "";
		if (buildingAirlock.isInnerDoorLocked())
			innerDoorState = LOCKED;
		else {
			innerDoorState = UNLOCKED;
		}

		// Update innerDoorStateLabel
		if (!innerDoorStateCache.equalsIgnoreCase(innerDoorState)) {
			innerDoorStateCache = innerDoorState;
			innerDoorStateLabel.setText(innerDoorState);
		}

		String outerDoorState = "";
		if (buildingAirlock.isOuterDoorLocked())
			outerDoorState = LOCKED;
		else {
			outerDoorState = UNLOCKED;
		}

		// Update outerDoorStateLabel
		if (!outerDoorStateCache.equalsIgnoreCase(outerDoorState)) {
			outerDoorStateCache = outerDoorState;
			outerDoorStateLabel.setText(outerDoorState);
		}

		// Update airlockStateLabel
		String state = buildingAirlock.getState().toString();
		if (!airlockStateCache.equalsIgnoreCase(state)) {
			airlockStateCache = state;
			airlockStateLabel.setText(state);
		}
		
		// Update activationLabel
		boolean activated = buildingAirlock.isActivated();
		if (activationCache != activated) {
			activationCache = activated;
			activationLabel.setText(Boolean.toString(activated));
		}

		// Update activationLabel
		boolean transition = buildingAirlock.isTransitioning();
		if (transitionCache != transition) {
			transitionCache = transition;
			transitionLabel.setText(Boolean.toString(transition));
		}
		
		// Update airlockModeLabel
		AirlockMode airlockMode = buildingAirlock.getAirlockMode();
		if (airlockModeCache != airlockMode) {
			airlockModeCache = airlockMode;
			airlockModeLabel.setText(airlockMode.getName());
		}
		
		// Update occupant list
		occupants.update();
		reservationList.update();
	}

	@Override
	public void destroy() {
		super.destroy();

		occupants = null;
		reservationList = null;
		
		eva = null;
		buildingAirlock = null;
	}
}