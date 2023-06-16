/*
 * Mars Simulation Project
 * LoadingCommand.java
 * @date 2023-06-14
 * @author Manny Kung
 */

package org.mars.sim.console.chat.simcommand.person;

import java.util.List;
import java.util.Objects;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;

/** 
 * 
 */
public class LoadingCommand extends AbstractPersonCommand {
	public static final ChatCommand LOADING = new LoadingCommand();
	
	private LoadingCommand() {
		super("ld", "load", "What am I carrying ?");
	}

	@Override
	public boolean execute(Conversation context, String input, Person person) {
		
		context.println("");
		context.println(" ------  TESTING  ------");
		
		StructuredResponse response = new StructuredResponse();
		
 		List<AmountResource> ars = person.getEquipmentInventory()
				.getAllAmountResourceIDs().stream()
				.map(ar -> ResourceUtil.findAmountResource(ar))
				.filter(Objects::nonNull)
				.toList();
   		
   		if (ars.isEmpty())	
   			context.println("I haven't carried anything.");	
   		else
   			response.appendLabeledString("Amount Resources carried including equipment", ars.toString());
		
//		boolean haveFood = person.hasAmountResourceRemainingCapacity(ResourceUtil.foodID);
//		response.appendLabeledString("Have capacity to carry preserved food", (haveFood + "").toLowerCase());
//		
//		Settlement settlement = person.getSettlement();
//		// Testing
//		if (haveFood && settlement != null) {
//			settlement.retrieveAmountResource(ResourceUtil.foodID, 1);
//			person.storeAmountResource(ResourceUtil.foodID, 1);
//		}
//		
//		double amount = person.getAmountResourceStored(ResourceUtil.foodID);
//		response.appendLabeledString("Preserved food hand-carried", Math.round(amount * 10.0)/10.0 + " kg");
//	
//		amount = person.getAllAmountResourceStored(ResourceUtil.foodID);
//		response.appendLabeledString("Preserved food stored", Math.round(amount * 10.0)/10.0 + " kg");
//		
//		double cap = person.getAmountResourceCapacity(ResourceUtil.foodID);
//		response.appendLabeledString("Preserved food capacity", Math.round(cap * 10.0)/10.0 + " kg");
//		
//		ars = person.getAmountResourceIDs().stream()
//				.map(ar -> ResourceUtil.findAmountResource(ar))
//				.filter(Objects::nonNull)
//				.toList();
//   		
//   		if (ars.isEmpty())	
//   			context.println("I haven't carried anything.");	
//   		else
//   			response.appendLabeledString("Amount Resources carried in person", ars.toString());
  
	
		response.appendBlankLine();
	
		context.println(response.getOutput());
		return true;
	}

}
