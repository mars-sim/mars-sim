/*
 * Mars Simulation Project
 * MissionWizard.java
 * @date 2026-02-01
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.missionwizard;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.utils.wizard.WizardPane;
import com.mars_sim.ui.swing.utils.wizard.WizardStep;

/**
 * A wizard for creating new missions.
 */
@SuppressWarnings("serial")
public class MissionCreate extends WizardPane<MissionDataBean> {
	/** Tool name. */
	public static final String NAME = "createmission";
	public static final String ICON = "mission";
	public static final String TITLE = "Create a new Mission";

	// Share error messages
	static final String VEHICLE_OUT_OF_RANGE = "Out of range";		
	static final String ALREADY_ON_MISSION = "Already on Mission";
    static final String VEHICLE_WRONG_STATUS = "Not parked or garaged";

	/**
	 * The predefined step sequences for Mission types.
	 */
	private static final List<String> EXPLORATION_STEPS = List.of(TypePanel.ID, StartingSettlementPanel.ID,
								RoverPanel.ID, RoutePanel.ID,  MembersPanel.ID);
	private static final List<String> SCIENCE_STEPS = List.of(TypePanel.ID, StartingSettlementPanel.ID,
								SciencePanel.ID, RoverPanel.ID, RoutePanel.ID, MembersPanel.ID);

	// Too many to use Map.of method.
	private static final Map<MissionType, List<String>> MISSION_STEPS = new EnumMap<>(MissionType.class);
	static {
		MISSION_STEPS.put(MissionType.AREOLOGY, SCIENCE_STEPS);
		MISSION_STEPS.put(MissionType.BIOLOGY, SCIENCE_STEPS);
		MISSION_STEPS.put(MissionType.COLLECT_ICE, EXPLORATION_STEPS);
		MISSION_STEPS.put(MissionType.COLLECT_REGOLITH, EXPLORATION_STEPS);
		MISSION_STEPS.put(MissionType.CONSTRUCTION, List.of(TypePanel.ID, StartingSettlementPanel.ID,
				ConstructionPanel.ID, LightUtilityVehiclePanel.ID, MembersPanel.ID));
		MISSION_STEPS.put(MissionType.DELIVERY, List.of(TypePanel.ID, StartingSettlementPanel.ID, DronePanel.ID,
				DestinationSettlementPanel.ID, TradeGoodsPanel.BUY_ID, TradeGoodsPanel.SELL_ID, MembersPanel.ID,
				BotsPanel.ID));
		MISSION_STEPS.put(MissionType.EMERGENCY_SUPPLY, List.of(TypePanel.ID, StartingSettlementPanel.ID, RoverPanel.ID,
				DestinationSettlementPanel.ID, TradeGoodsPanel.SUPPLY_ID, MembersPanel.ID));	
		MISSION_STEPS.put(MissionType.METEOROLOGY, SCIENCE_STEPS);
		MISSION_STEPS.put(MissionType.MINING, List.of(TypePanel.ID, StartingSettlementPanel.ID, RoverPanel.ID, MineSitePanel.ID,
				LightUtilityVehiclePanel.ID,  MembersPanel.ID));
		MISSION_STEPS.put(MissionType.RESCUE_SALVAGE_VEHICLE, List.of(
				TypePanel.ID, StartingSettlementPanel.ID, RoverPanel.ID, RescueVehiclePanel.ID, MembersPanel.ID));
		MISSION_STEPS.put(MissionType.TRADE, List.of(TypePanel.ID, StartingSettlementPanel.ID, RoverPanel.ID,
				DestinationSettlementPanel.ID, TradeGoodsPanel.BUY_ID, TradeGoodsPanel.SELL_ID, MembersPanel.ID));		
		MISSION_STEPS.put(MissionType.TRAVEL_TO_SETTLEMENT, List.of(TypePanel.ID, StartingSettlementPanel.ID,
				RoverPanel.ID, DestinationSettlementPanel.ID, MembersPanel.ID));
		}

	/**
	 * Create a show a new mission wizard dialog.
	 * @param context UI context for the wizard.
	 * @return
	 */
	public static MissionCreate create(UIContext context) {
		var state = new MissionDataBean();
		var wizard = new MissionCreate(context, state);
		
		var frame = wizard.showInDialog(context.getTopFrame());
		frame.setSize(600, 600);
		frame.setLocationRelativeTo(context.getTopFrame());
		frame.setVisible(true);

		return wizard;
	}

	/**
	 * Constructor.
	 * 
	 * @param context The UI context
	 */
	public MissionCreate(UIContext context, MissionDataBean status) {
		super("Create Mission", context, status, TypePanel.ID);
	}

	/**
	 * Finalizes the button click.
	 */
	@Override
	protected void finish(MissionDataBean state) {
		var mission = state.createMission();

		// Open the mission details dialog for the created mission
		if (mission != null) {
			getContext().showDetails(mission);
		}
	}
	
	/**
	 * Creates the wizard step for the given step ID.
	 */
	@Override
	protected WizardStep<MissionDataBean> createStep(String stepID, MissionDataBean state) {
		return switch(stepID) {
			case BotsPanel.ID -> new BotsPanel(this, state);
			case ConstructionPanel.ID -> new ConstructionPanel(this, state);
			case DestinationSettlementPanel.ID -> new DestinationSettlementPanel(this, state);
			case DronePanel.ID -> new DronePanel(this, state);
			case LightUtilityVehiclePanel.ID -> new LightUtilityVehiclePanel(this, state);
			case MembersPanel.ID -> new MembersPanel(this, state);
			case MineSitePanel.ID -> new MineSitePanel(this, state);
			case RescueVehiclePanel.ID -> new RescueVehiclePanel(this, state);
			case RoverPanel.ID -> new RoverPanel(this, state);
			case RoutePanel.ID -> new RoutePanel(this, state, getContext());
			case StartingSettlementPanel.ID ->new StartingSettlementPanel(this, state);
			case SciencePanel.ID -> new SciencePanel(this, state);
			case TradeGoodsPanel.BUY_ID, TradeGoodsPanel.SUPPLY_ID, TradeGoodsPanel.SELL_ID ->
								new TradeGoodsPanel(stepID, this, state);
			case TypePanel.ID ->new TypePanel(this);
			default ->
				throw new IllegalArgumentException("Unknown step ID: " + stepID);
		};
	}

	/**
	 * This returns the step sequence for the given mission type.
	 * @param type Mission type to create
	 * @return List of the step IDs in order.
	 */
	static List<String> getSteps(MissionType type) {
		return MISSION_STEPS.getOrDefault(type, null);
	}

	/**
	 * Get the available mission types that can be created.
	 * @return Mission types supported by the wizard.
	 */
    static Set<MissionType> availableTypes() {
        return MISSION_STEPS.keySet();
    }
}