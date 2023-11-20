/*
 * Mars Simulation Project
 * SocialCommand.java
 * @date 2022-11-30
 * @author Barry Evans
 */
package com.mars_sim.console.chat.simcommand.settlement;

import java.util.List;

import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.Entity;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.person.ai.task.util.SettlementTaskManager;
import com.mars_sim.core.structure.Settlement;

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
            response.appendTableHeading(true, "Tasks", 35, "Subject", CommandHelper.BUILIDNG_WIDTH,
                            "#", 3, "Score", -30);
            for(SettlementTask t : tasks) {
                String subjectName = null;
                Entity subject = t.getFocus();
                if (subject != null) {
                    subjectName = subject.getName();
                }
                response.appendTableRow(t.getShortName(), subjectName,
                                t.getDemand(),
                                t.getScore().getOutput());
            }
        }
        else {
            response.appendText("No Tasks pending");
        }
		context.println(response.getOutput());
		
		return true;
	}   
}
