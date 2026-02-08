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
	
	public static MissionCreate create(UIContext context) {
		var state = new MissionDataBean();
		var wizard = new MissionCreate(context, state);
		wizard.setVisible(true);
		wizard.setSize(600, 400);

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
		state.createMission();
	}
	
	/**
	 * Creates the wizard step for the given step ID.
	 */
	@Override
	protected WizardStep<MissionDataBean> createStep(String stepID, MissionDataBean state) {
		return switch(stepID) {
			case TypePanel.ID ->new TypePanel(this);
			case StartingSettlementPanel.ID ->new StartingSettlementPanel(this, state);
			case RoverPanel.ID -> new RoverPanel(this, state);
			case ConstructionPanel.ID -> new ConstructionPanel(this, state);
			case ProspectingSitePanel.NAME -> new ProspectingSitePanel(this, state, getContext());
			case DestinationSettlementPanel.ID -> new DestinationSettlementPanel(this, state);
			case LightUtilityVehiclePanel.ID -> new LightUtilityVehiclePanel(this, state);
			default ->
				throw new IllegalArgumentException("Unknown step ID: " + stepID);
		};
	}

	/**
	 * The predefiend step sequences for Mission types.
	 */
	private static final Map<MissionType, List<String>> missionSteps = Map.of(
			MissionType.CONSTRUCTION, List.of(
				TypePanel.ID, StartingSettlementPanel.ID, ConstructionPanel.ID, LightUtilityVehiclePanel.ID, ConstructionPanel.ID),
			// MissionType.COLLECT_ICE, List.of(
			// 	TypePanel.ID, StartingSettlementPanel.ID, RoverPanel.ID, MembersPanel.ID, ProspectingSitePanel.NAME),
			// MissionType.COLLECT_REGOLITH, List.of(
			// 	TypePanel.ID, StartingSettlementPanel.ID, RoverPanel.ID, MembersPanel.ID, ProspectingSitePanel.NAME),
			MissionType.TRAVEL_TO_SETTLEMENT, List.of(
				TypePanel.ID, StartingSettlementPanel.ID, RoverPanel.ID, ConstructionPanel.ID, DestinationSettlementPanel.ID)	
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
