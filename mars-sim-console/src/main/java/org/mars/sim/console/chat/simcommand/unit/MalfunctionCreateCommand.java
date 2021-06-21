/**
 * Mars Simulation Project
 * MalfunctionCreateCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.unit;

import java.util.ArrayList;
import java.util.List;

import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.ConversationRole;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.malfunction.MalfunctionConfig;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.MalfunctionMeta;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

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
		int choice = CommandHelper.getOptionInput(context, names, "Pick a malfunction from above by entering a number");
		if (choice < 0) {
			return false;
		}

		MalfunctionMeta malfunction = relatedMalfunctions.get(choice);

		malfunctionManager.triggerMalfunction(malfunction, true);

		context.println(malfunctionManager.getUnit().getName() + " just had '" 
				+ malfunction.getName() + "'");
		return true;
	}

	/**
	 * Find the most appropriate MalfunctionManager according to the connected Unit.
	 * @param source
	 * @return
	 */
	static MalfunctionManager findManager(Conversation context, Unit source) {
		Malfunctionable owner = null;

		if (source instanceof Malfunctionable) {
			owner = (Malfunctionable) source;
		}
		else if (source instanceof Settlement) {
			// Offer the user a lis tof buildings
			Settlement settlement = (Settlement) source;
			
			List <Building> buildings = settlement.getBuildingManager().getBuildings();
			List <String> names = new  ArrayList<>();
			for (Building building : buildings) {
				names.add(building.getName());
			} 
			int selectedBuilding = CommandHelper.getOptionInput(context, names, "Select a building in " + settlement.getName());
			if (selectedBuilding >= 0) {
				owner = buildings.get(selectedBuilding);
				context.println("Selected " + owner.getNickName());
			}
		}
		else {
			context.println("Sorry this unit does not happen malfunctions.");
		}
		
		return (owner == null ? null : owner.getMalfunctionManager());
	}
}
