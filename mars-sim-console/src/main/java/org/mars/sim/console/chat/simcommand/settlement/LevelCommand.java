/**
 * Mars Simulation Project
 * LevelCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.settlement;

import java.util.Arrays;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.goods.GoodsManager;

public class LevelCommand extends AbstractSettlementCommand {
	public static final ChatCommand LEVEL = new LevelCommand();

	private static final String REPAIR = "repair";
	private static final String MAINTENANCE = "maintenance";
	private static final String EVA = "eva";
	
	private LevelCommand() {
		super("lv", "level", "Change the levels, argument is the level ");
		
		setIntroduction("Change the level of effort");

		// Setup the fixed arguments
		setArguments(Arrays.asList(REPAIR, MAINTENANCE, EVA));
		
		setInteractive(true);
	}
	
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		boolean result = false;
		GoodsManager goodsManager = settlement.getGoodsManager();
		if (input == null || input.isEmpty()) {
			context.println("Must enter a level " + getArguments(context));
		}
		else {
			String subCommand = input.trim().toLowerCase();
	
			String levelName = null;
			int level = -1;
			switch (subCommand) {
			case REPAIR:
				levelName = "Outstanding Repair's Level of Effort";
				level = goodsManager.getRepairLevel();
				break;
				
			case MAINTENANCE:
				levelName = "Outstanding Maintenance Level of Effort";
				level = goodsManager.getMaintenanceLevel();
				break;

			case EVA:
				levelName = "Outstanding EVA Suit production Effort";
				level = goodsManager.getEVASuitLevel();
				break;
			
			default:
				context.println("Sorry I don;t understans that level : " + subCommand);
				break;
			}
	
			context.println("Current " + levelName + " is " + level);
			int newLevel = context.getIntInput("Would you like to change it? (0/blank: no change 1: lowest; 5: highest)");
			
			// Need a change?
			if ((newLevel > 0) && (newLevel <= 5) && (newLevel != level)) {
				result = true;
				
				switch (subCommand) {
				case REPAIR:
					goodsManager.setRepairPriority(newLevel);
					break;
				
				case MAINTENANCE:
					goodsManager.setMaintenancePriority(newLevel);
					break;
					
				case EVA:
					goodsManager.setEVASuitPriority(newLevel);
					break;
				
				default:
					// Should never get here
					throw new IllegalStateException("Don't know to apply level " + subCommand);
				}
				
				context.println("New " + levelName + " : " + newLevel);
			}
		}
		
		return result;
	}
}
