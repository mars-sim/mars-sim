/*
 * Mars Simulation Project
 * MalfunctionCreateCommand.java
 * @date 2022-09-17
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.unit;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.ConversationRole;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.Unit;
import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.malfunction.MalfunctionConfig;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.malfunction.MalfunctionMeta;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;

/**
 * Command to create a malfunction in a Malfunctionable.
 */
public class MalfunctionCreateCommand extends AbstractUnitCommand {


	public MalfunctionCreateCommand(String group) {
		super(group, "mc", "create malfunction", "Create a malfunction");
		
		setInteractive(true);
		addRequiredRole(ConversationRole.EXPERT);
	}

	@Override
	public boolean execute(Conversation context, String input, Unit source) {
		
		MalfunctionManager malfunctionManager = findManager(context, source);
		if (malfunctionManager == null) {
			return false;
		}
		List<MalfunctionMeta> relatedMalfunctions = new ArrayList<>();
		List<String> names = new ArrayList<>();
		
		// Find possible malfunctions
		MalfunctionConfig mc = SimulationConfig.instance().getMalfunctionConfiguration();
		for (MalfunctionMeta m : mc.getMalfunctionList()) {
			if (m.isMatched(malfunctionManager.getScopes())) {
				relatedMalfunctions.add(m);
				names.add(m.getName());
			}
		}

		// Choose one
		int choice = CommandHelper.getOptionInput(context, names, 
				"Pick a malfunction from above by entering a number");
		if (choice < 0) {
			return false;
		}

		MalfunctionMeta malfunction = relatedMalfunctions.get(choice);

		malfunctionManager.triggerMalfunction(malfunction, true, source);

		context.println(malfunctionManager.getEntity().getName() + " just had '" 
				+ malfunction.getName() + "'");
		return true;
	}

	/**
	 * Finds the most appropriate MalfunctionManager according to the connected Unit.
	 * 
	 * @param source
	 * @return
	 */
	private static MalfunctionManager findManager(Conversation context, Unit source) {
		Malfunctionable owner = null;
		
		// If EquipmentHOwner then check if the Equipment should be source
		if (source instanceof EquipmentOwner eo) {
			// Get the smart equipment that have failures
			List<Malfunctionable> smartEqm = eo.getEquipmentSet().stream()
										.filter(Malfunctionable.class::isInstance)
										.map(Malfunctionable.class::cast)
										.collect(Collectors.toList());

			if (!smartEqm.isEmpty() && 
				"Y".equalsIgnoreCase(context.getInput("Use a contained Equipment: Y/N"))) {
					// Pick Equipment
				owner = pickSelection(context, "equipment in " + source.getName(), smartEqm);
			}
		}

		// Create malfunction on the selection
		if (owner == null) {
			if (source instanceof Malfunctionable m) {
				owner = m;
			}
			else if (source instanceof Settlement settlement) {
				// Offer the user a list of buildings		
				List <Building> buildings = settlement.getBuildingManager().getBuildings();
				owner = pickSelection(context, "building in " + settlement.getName(), buildings);
			}
			else {
				context.println("Sorry this unit does not happen malfunctions.");
			}
		}
		
		return (owner == null ? null : owner.getMalfunctionManager());
	}

	/**
	 * Picks a Malfunctionable from a list of options.
	 * 
	 * @param context The conversation taking part
	 * @param optionDesc Description of the options presented
	 * @param options Malfunctionables to choose from
	 * @return Selected option or null if none
	 */
	private static Malfunctionable pickSelection(Conversation context, String optionDesc,
												 List<? extends Malfunctionable> options) {
		List <String> names = new ArrayList<>();
		for (Malfunctionable o : options) {
			names.add(o.getName());
		} 
		int selected = CommandHelper.getOptionInput(context, names, "Select a " + optionDesc);
		Malfunctionable owner = null;
		if (selected >= 0) {
			owner = options.get(selected);
			context.println("Selected " + owner.getName());
		}

		return owner;
	}
}
