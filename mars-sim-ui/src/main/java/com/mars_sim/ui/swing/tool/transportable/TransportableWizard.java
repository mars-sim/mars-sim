/*
 * Mars Simulation Project
 * TransportableWizard.java
 * @date 2026-03-03
 * @author Barry Evans
 */

package com.mars_sim.ui.swing.tool.transportable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.mars_sim.core.interplanetary.transport.TransportManager;
import com.mars_sim.core.interplanetary.transport.resupply.Resupply;
import com.mars_sim.core.interplanetary.transport.settlement.ArrivingSettlement;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.utils.wizard.WizardPane;
import com.mars_sim.ui.swing.utils.wizard.WizardStep;

/**
 * A wizard for creating a new transportable item.
 */
public class TransportableWizard extends WizardPane<TransportState> {

	/** Tool name. */
	public static final String NAME = "transportablewizard";
	public static final String ICON = "transportable";
	public static final String TITLE = "Create a new Transport Item";

	private static final List<String> RESUPPLY_STEPS = List.of(TypeStep.ID,
			ResupplySettlementStep.ID, ResupplyManifestStep.ID);
	private static final List<String> ARRIVING_STEPS = List.of(TypeStep.ID,
			FutureSettlementStep.ID, LandingSiteStep.ID);

	private static final Map<TransportableType, List<String>> TYPE_STEPS = new EnumMap<>(TransportableType.class);
	static {
		TYPE_STEPS.put(TransportableType.RESUPPLY, RESUPPLY_STEPS);
		TYPE_STEPS.put(TransportableType.ARRIVING_SETTLEMENT, ARRIVING_STEPS);
	}

	/**
	 * Create and show a new transportable wizard dialog.
	 *
	 * @param context UI context for the wizard.
	 * @return The active wizard.
	 */
	public static TransportableWizard create(UIContext context) {
		var state = new TransportState();

		var wizard = new TransportableWizard(context, state);

		var frame = wizard.showInDialog(context.getTopFrame());
		frame.setSize(650, 600);
		frame.setLocationRelativeTo(context.getTopFrame());
		frame.setVisible(true);

		return wizard;
	}

	private TransportableWizard(UIContext context, TransportState state) {
		super("Create Transport Item", context, state, TypeStep.ID);
	}

	@Override
	protected WizardStep<TransportState> createStep(String stepID, TransportState state) {
		return switch (stepID) {
			case TypeStep.ID -> new TypeStep(this, state);
			case ResupplyManifestStep.ID -> new ResupplyManifestStep(this, state);
			case FutureSettlementStep.ID -> new FutureSettlementStep(this, state);
			case LandingSiteStep.ID -> new LandingSiteStep(this, state);
			case ResupplySettlementStep.ID -> new ResupplySettlementStep(this, state);
			default -> throw new IllegalArgumentException("Unknown step ID: " + stepID);
		};
	}

	@Override
	protected void finish(TransportState state) {
		var type = state.getType();
		if (type == null) {
			throw new IllegalStateException("No transportable type selected.");
		}

		TransportManager manager = getContext().getSimulation().getTransportManager();
		var created = switch (type) {
			case RESUPPLY -> createResupply(state);
			case ARRIVING_SETTLEMENT -> createArrivingSettlement(state);
		};

		manager.addNewTransportItem(created);
		getContext().showDetails(created);
	}

	private Resupply createResupply(TransportState state) {
		return new Resupply(state.getName(), state.getArrivalDate(), state.getLandingSettlement(), state.getManifest());
	}

	private ArrivingSettlement createArrivingSettlement(TransportState state) {

		ArrivingSettlement arriving = new ArrivingSettlement(state.getName(),
				state.getArrivingTemplate(),
				state.getArrivingSponsor(),
				state.getArrivalDate(),
				state.getLandingSite(),
				state.getArrivingPopulation(),
				state.getArrivingRobots());
		return arriving;
	}

	static List<String> getSteps(TransportableType type) {
		return TYPE_STEPS.get(type);
	}
}
