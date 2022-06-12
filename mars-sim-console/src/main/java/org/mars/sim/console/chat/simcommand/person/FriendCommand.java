/*
 * Mars Simulation Project
 * FriendCommand.java
 * @date 2022-06-11
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.person;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.social.RelationshipUtil;

/** 
 * Display details about pERSON'S FRIEND
 */
public class FriendCommand extends AbstractPersonCommand {
	public static final ChatCommand FRIEND = new FriendCommand();
	
	private FriendCommand() {
		super("fr", "friend", "About my friends");
	}

	@Override
	public boolean execute(Conversation context, String input, Person person) {

		Map<Person, Double> bestFriends = RelationshipUtil.getBestFriends(person);
		if (bestFriends.isEmpty()) {
			context.println("I don't have any friends yet.");
		}
		else {
			StructuredResponse response = new StructuredResponse();
			
			response.appendTableHeading("Person", CommandHelper.PERSON_WIDTH, "Score");
			List<Person> list = new ArrayList<>(bestFriends.keySet());

			for (Person person2 : list) {
				double score = bestFriends.get(person2);					
				response.appendTableRow(person2.getName(), score);
			}
			context.println(response.getOutput());
		}
		
		return true;
	}
}
