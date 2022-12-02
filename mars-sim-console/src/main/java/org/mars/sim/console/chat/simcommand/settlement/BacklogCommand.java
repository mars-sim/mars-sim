/*
 * Mars Simulation Project
 * SocialCommand.java
 * @date 2022-11-30
 * @author Barry Evans
 */
package org.mars.sim.console.chat.simcommand.settlement;

import java.util.List;

import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.ai.task.util.SettlementTask;
import org.mars_sim.msp.core.person.ai.task.util.SettlementTaskManager;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Displays the status of the task backlog in a Settlement
 */
class BacklogCommand extends AbstractSettlementCommand {
    public static final BacklogCommand BACKLOG = new BacklogCommand();

	private BacklogCommand() {
		super("bk", "backlog", "Backlog of Tasks in the Settlement");
	}

	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {

        SettlementTaskManager stm = settlement.getTaskManager();

        StructuredResponse response = new StructuredResponse();
        response.appendLabeledString("Reuse ratio", String.format(CommandHelper.DOUBLE_FORMAT, stm.getReuseScore()));
        response.appendLabelledDigit("Tasks Executed", stm.getExecutedCount());

        List<SettlementTask> tasks = stm.getAvailableTasks();
        if (tasks != null) {
            response.appendTableHeading("Tasks", 55, "#", 3, "Score");
            for(SettlementTask t : tasks) {
                response.appendTableRow(t.getDescription(), t.getDemand(),
                                String.format(CommandHelper.DOUBLE_FORMAT, t.getScore()));
            }
        }
        else {
            response.appendText("No Tasks pending");
        }
		context.println(response.getOutput());
		
		return true;
	}   
}
