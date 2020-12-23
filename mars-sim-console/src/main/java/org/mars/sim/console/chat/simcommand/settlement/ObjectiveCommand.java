package org.mars.sim.console.chat.simcommand.settlement;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.structure.ObjectiveType;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Command to display settlement objective
 * This is a singleton.
 */
public class ObjectiveCommand extends AbstractSettlementCommand {

	public static final ChatCommand OBJECTIVE = new ObjectiveCommand();

	private ObjectiveCommand() {
		super("o", "objective", "Review objective of Settlement");
	}

	/** 
	 * Output the current immediate location of the Unit
	 */
	@Override
	protected void execute(Conversation context, String input, Settlement settlement) {
		ObjectiveType objective = settlement.getObjective();
		double objLevel = settlement.getObjectiveLevel(objective);
        context.println("The current settlement objective is " + objective + " with level " + objLevel); 

		String change = context.getInput("Change (Y/N)?");
        
        if ("Y".equals(change.toUpperCase())) {
        	int id = 1;
        	for (ObjectiveType iterable_element : ObjectiveType.values()) {
				context.println(" #" + id++ + " - " + iterable_element.toString());
			}
            int choosen = context.getIntInput("Enter the objective [1.." + ObjectiveType.values().length + "]");
            if ((1 <= choosen) && (choosen <= ObjectiveType.values().length)) {
            	ObjectiveType newObj = ObjectiveType.values()[choosen-1];

                int newLevel = context.getIntInput("Enter the level [1..3]");
                newLevel = Math.min(3, Math.max(1, newLevel));
                context.println("New objective is " + newObj + " with level " + newLevel); 
            	settlement.setObjective(newObj,  newLevel);
            }
        }
	}

}
