/*
 * Mars Simulation Project
 * MissionWizard.java
 * @date 2026=02-01
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.missionwizard;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.tool.mission.create.MissionDataBean;
import com.mars_sim.ui.swing.utils.wizard.WizardPane;
import com.mars_sim.ui.swing.utils.wizard.WizardStep;

/**
 * A wizard for creating new missions.
 */
@SuppressWarnings("serial")
public class MissionCreate extends WizardPane<MissionDataBean> {
	// Error messages
	static final String VEHICLE_OUT_OF_RANGE = "Out of range";		
	static final String ALREADY_ON_MISSION = "Already on Mission";
    static final String VEHICLE_WRONG_STATUS = "Not parked or garaged";

	/**
	 * Create a show a new mission wizard dialog.
	 * @param context UI context for the wizard.
	 * @return
	 */
	public static MissionCreate create(UIContext context) {
		var state = new MissionDataBean();
		var wizard = new MissionCreate(context, state);
		
		var frame = wizard.showInDialog(context.getTopFrame());
		frame.setSize(600, 400);
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
			//case ProspectingSitePanel.NAME -> new ProspectingSitePanel(this, state, getContext());
			case RescueVehiclePanel.ID -> new RescueVehiclePanel(this, state);
			case RoverPanel.ID -> new RoverPanel(this, state);
			case StartingSettlementPanel.ID ->new StartingSettlementPanel(this, state);
			case TradeGoodsPanel.BUY_ID, TradeGoodsPanel.SUPPLY_ID, TradeGoodsPanel.SELL_ID ->
								new TradeGoodsPanel(stepID, this, state);
			case TypePanel.ID ->new TypePanel(this);
			default ->
				throw new IllegalArgumentException("Unknown step ID: " + stepID);
		};
	}

	/**
	 * The predefined step sequences for Mission types.
	 */
	private static final Map<MissionType, List<String>> missionSteps = Map.of(
		MissionType.CONSTRUCTION, List.of(
				TypePanel.ID, StartingSettlementPanel.ID, ConstructionPanel.ID, MembersPanel.ID,
				LightUtilityVehiclePanel.ID),
		MissionType.DELIVERY, List.of(
				TypePanel.ID, StartingSettlementPanel.ID, DronePanel.ID, MembersPanel.ID, BotsPanel.ID,
				DestinationSettlementPanel.ID, TradeGoodsPanel.BUY_ID, TradeGoodsPanel.SELL_ID),
		MissionType.EMERGENCY_SUPPLY, List.of(
				TypePanel.ID, StartingSettlementPanel.ID, RoverPanel.ID, MembersPanel.ID,
				DestinationSettlementPanel.ID, TradeGoodsPanel.SUPPLY_ID),
		MissionType.MINING, List.of(
				TypePanel.ID, StartingSettlementPanel.ID, RoverPanel.ID, MembersPanel.ID,
				LightUtilityVehiclePanel.ID, MineSitePanel.ID),
		MissionType.RESCUE_SALVAGE_VEHICLE, List.of(
				TypePanel.ID, StartingSettlementPanel.ID, RoverPanel.ID, MembersPanel.ID, RescueVehiclePanel.ID),
			// MissionType.COLLECT_ICE, List.of(
			// 	TypePanel.ID, StartingSettlementPanel.ID, RoverPanel.ID, MembersPanel.ID, ProspectingSitePanel.NAME),
			// MissionType.COLLECT_REGOLITH, List.of(
			// 	TypePanel.ID, StartingSettlementPanel.ID, RoverPanel.ID, MembersPanel.ID, ProspectingSitePanel.NAME),
		MissionType.TRADE, List.of(
				TypePanel.ID, StartingSettlementPanel.ID, RoverPanel.ID, MembersPanel.ID,
				DestinationSettlementPanel.ID, TradeGoodsPanel.BUY_ID, TradeGoodsPanel.SELL_ID),
		MissionType.TRAVEL_TO_SETTLEMENT, List.of(
				TypePanel.ID, StartingSettlementPanel.ID, RoverPanel.ID, MembersPanel.ID,
				DestinationSettlementPanel.ID)
		);

	/**
	 * This returns the step sequence for the given mission type.
	 * @param type Mission type to create
	 * @return List of the step IDs in order.
	 */
	static List<String> getSteps(MissionType type) {
		return missionSteps.getOrDefault(type, null);
	}

	/**
	 * Get the available mission types that can be created.
	 * @return Mission types supproted by wizard.
	 */
    static Set<MissionType> availableTypes() {
        return missionSteps.keySet();
    }
}