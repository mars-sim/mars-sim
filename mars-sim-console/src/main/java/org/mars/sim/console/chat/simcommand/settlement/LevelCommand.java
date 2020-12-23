package org.mars.sim.console.chat.simcommand.settlement;

import java.util.Arrays;
import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.goods.GoodsManager;

public class LevelCommand extends AbstractSettlementCommand {
	public static final ChatCommand LEVEL = new LevelCommand();

	private static final String REPAIR = "repair";
	private static final String MAINTENANCE = "maintenance";
	private static final String EVA = "eva";
	private static final List<String> ARGS = Arrays.asList(REPAIR, MAINTENANCE, EVA);
	
	private LevelCommand() {
		super("l", "level", "Change the levels, arguments " + ARGS);
		
		setIntroduction("Change the level of effort");

		// Setup the fixed arguments
		setArguments(ARGS);
	}
	
	@Override
	protected void execute(Conversation context, String input, Settlement settlement) {

		GoodsManager goodsManager = settlement.getGoodsManager();
		if (input == null || input.isEmpty()) {
			context.println("Must enter a level");
		}
		else {
			String subCommand = input.trim().toLowerCase();
	
			String levelName = null;
			int level = -1;
			if (REPAIR.equals(subCommand)) {
				levelName = "Outstanding Repair's Level of Effort";
				level = goodsManager.getRepairLevel();
			}
			else if (MAINTENANCE.equals(subCommand)) {
				levelName = "Outstanding Maintenance Level of Effort";
				level = goodsManager.getMaintenanceLevel();
			}
			else if (EVA.equals(subCommand)) {
				levelName = "Outstanding EVA Suit production Effort";
				level = goodsManager.getEVASuitLevel();
			}
			else {
				context.println("Sorry I don;t understans that level : " + subCommand);
				return;
			}
	
			context.println("Current " + levelName + " is " + level);
			int newLevel = context.getIntInput("Would you like to change it? (0/blank: no change 1: lowest; 5: highest)");
			
			// Need a change?
			if ((newLevel > 0) && (newLevel <= 5) && (newLevel != level)) {
	
				if (REPAIR.equals(subCommand)) {
					goodsManager.setRepairPriority(newLevel);
				}
				else if (MAINTENANCE.equals(subCommand)) {
					goodsManager.setMaintenancePriority(newLevel);
				}
				else if (EVA.equals(subCommand)) {
					goodsManager.setEVASuitPriority(newLevel);
				}
				context.println("New " + levelName + " : " + newLevel);
			}
		}
	}
}
