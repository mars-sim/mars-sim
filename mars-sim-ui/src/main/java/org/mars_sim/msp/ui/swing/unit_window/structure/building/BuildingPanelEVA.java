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

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Airlock.AirlockMode;
import org.mars_sim.msp.core.structure.building.function.BuildingAirlock;
import org.mars_sim.msp.core.structure.building.function.EVA;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.unit_window.UnitListPanel;


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

	private JTextField innerDoorLabel;
	private JTextField outerDoorLabel;
	private JTextField occupiedLabel;
	private JTextField emptyLabel;
	private JTextField operatorLabel;
	private JTextField airlockStateLabel;
	private JTextField activationLabel;
	private JTextField transitionLabel;
	private JTextField cycleTimeLabel;
	private JTextField innerDoorStateLabel;
	private JTextField outerDoorStateLabel;
	private JTextField airlockModeLabel;

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

		JPanel labelGrid = new JPanel(new SpringLayout());
		topPanel.add(labelGrid, BorderLayout.NORTH);
		
		// Create innerDoorLabel
		innerDoorLabel = addTextField(labelGrid, Msg.getString("BuildingPanelEVA.innerDoor.number"),
									  eva.getNumAwaitingInnerDoor(), 4, null);

		if (eva.getAirlock().isInnerDoorLocked())
			innerDoorStateCache = LOCKED;
		else {
			innerDoorStateCache = UNLOCKED;
		}
		// Create innerDoorStateLabel
		innerDoorStateLabel = addTextField(labelGrid, Msg.getString("BuildingPanelEVA.innerDoor.state"),
										   innerDoorStateCache, 8, null);

		// Create outerDoorLabel
		outerDoorLabel = addTextField(labelGrid, Msg.getString("BuildingPanelEVA.outerDoor.number"),
									  eva.getNumAwaitingOuterDoor(), 4, null);

		if (eva.getAirlock().isOuterDoorLocked())
			outerDoorStateCache = LOCKED;
		else {
			outerDoorStateCache = UNLOCKED;
		}
		// Create outerDoorStateLabel
		outerDoorStateLabel = addTextField(labelGrid, Msg.getString("BuildingPanelEVA.outerDoor.state"),
										   outerDoorStateCache, 8, null);
		
		// Create occupiedLabel
		occupiedLabel = addTextField(labelGrid, Msg.getString("BuildingPanelEVA.occupied"),
									 eva.getNumInChamber(), 4, null);

		// Create airlockModeLabel
		airlockModeCache = buildingAirlock.getAirlockMode();
		airlockModeLabel = addTextField(labelGrid, Msg.getString("BuildingPanelEVA.airlock.mode"),
				airlockModeCache.getName() + "", 8, null);

		// Create emptyLabel
		emptyLabel = addTextField(labelGrid, Msg.getString("BuildingPanelEVA.empty"),
								  eva.getNumEmptied(), 4, null);

		// Create airlockStateLabel
		airlockStateLabel = addTextField(labelGrid, Msg.getString("BuildingPanelEVA.airlock.state"),
										 buildingAirlock.getState().toString(), 8, null);

		// Create cycleTimeLabel
		cycleTimeLabel = addTextField(labelGrid, Msg.getString("BuildingPanelEVA.airlock.cycleTime"),
									  StyleManager.DECIMAL_PLACES1.format(buildingAirlock.getRemainingCycleTime()), 4, null);
		
		// Create transitionLabel
		transitionLabel = addTextField(labelGrid, Msg.getString("BuildingPanelEVA.airlock.transition"),
				 Boolean.toString(buildingAirlock.isTransitioning()), 8, null);

		// Create activationLabel
		activationLabel = addTextField(labelGrid, Msg.getString("BuildingPanelEVA.airlock.activation"),
										 Boolean.toString(buildingAirlock.isActivated()), 4, null);

		// Create OperatorLabel
		operatorLabel = addTextField(labelGrid, Msg.getString("BuildingPanelEVA.operator"),
									 eva.getOperatorName(), 12, null);
		
		SpringUtilities.makeCompactGrid(labelGrid,
                6, 4, //rows, cols
                10, INITY_DEFAULT,        //initX, initY
                XPAD_DEFAULT, YPAD_DEFAULT);       //xPad, yPad	
		
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