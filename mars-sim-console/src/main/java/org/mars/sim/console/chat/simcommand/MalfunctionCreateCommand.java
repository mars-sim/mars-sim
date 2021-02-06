package org.mars.sim.console.chat.simcommand;

import java.util.ArrayList;
import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.ConversationRole;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionConfig;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * Command to create a malfunction in a Malfunctionable.
 */
public class MalfunctionCreateCommand extends ChatCommand {


	public MalfunctionCreateCommand(String group) {
		super(group, "mc", "create malfunction", "Create a malfunction");
		
		setInteractive(true);
		addRequiredRole(ConversationRole.EXPERT);
	}

	@Override
	public boolean execute(Conversation context, String input) {
		
		MalfunctionManager malfunctionManager = findManager(context);
		if (malfunctionManager == null) {
			return false;
		}
		List<Malfunction> relatedMalfunctions = new ArrayList<>();
		List<String> names = new ArrayList<>();
		
		// Find possible malfunctions
		for (Malfunction m : MalfunctionConfig.getMalfunctionList()) {
			if (m.isMatched(malfunctionManager.getScopes())) {
				relatedMalfunctions.add(m);
				names.add(m.getName());
			}
		}

		// Choose one
		int choice = getOptionInput(context, names, "Pick a malfunction from above by entering a number");
		if (choice < 0) {
			return false;
		}

		Malfunction malfunction = relatedMalfunctions.get(choice);

		malfunctionManager.triggerMalfunction(malfunction, true);

		context.println(malfunctionManager.getUnit().getName() + " just had '" 
				+ malfunction.getName() + "'");
		return true;
	}

	/**
	 * Find the most appropriate MalfunctionManager according to the connected Unit.
	 * @param context
	 * @return
	 */
	static MalfunctionManager findManager(Conversation context) {
		Malfunctionable owner = null;

		if (context.getCurrentCommand() instanceof ConnectedUnitCommand) {
			Unit source = ((ConnectedUnitCommand) context.getCurrentCommand()).getUnit();

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
				int selectedBuilding = getOptionInput(context, names, "Select a building in " + settlement.getName());
				if (selectedBuilding >= 0) {
					owner = buildings.get(selectedBuilding);
					context.println("Selected " + owner.getNickName());
				}
			}
			else {
				context.println("Sorry this unit does not happen malfunctions.");
			}
		}
		else {
			context.println("Sorry you are not connected to any Unit");
		}
		
		return (owner == null ? null : owner.getMalfunctionManager());
	}

	/**
	 * This could be a shared method.
	 * @param context
	 * @param names
	 * @param string
	 * @return
	 */
	static int getOptionInput(Conversation context, List<String> names, String prompt) {
		int idx = 1;
		for (String name : names) {
			context.println(idx++ + " - " + name);
		}
		int choice = context.getIntInput(prompt + " >");
		if ((choice < 1) || (choice >= idx)) {
			context.println("Invalid choice");
			choice = -1;
		}
		else {
			choice--;
		}
		return choice;
	}
}
