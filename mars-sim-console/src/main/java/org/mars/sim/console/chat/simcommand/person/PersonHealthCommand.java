/*
 * Mars Simulation Project
 * PersonHealthCommand.java
 * @date 2022-08-24
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.person;

import java.util.List;
import java.util.stream.Collectors;

import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.ConversationRole;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.health.Complaint;

/** 
 * Reports on a Persons health.
 */
public class PersonHealthCommand extends AbstractPersonCommand {
	
	// Characteristics that can be set
	private static final String FATIGUE = "Fatigue";
	private static final String HUNGER = "Hunger";
	private static final String THIRST = "Thirst";
	private static final String STRESS = "Stress";
	private static final String PERFORMANCE = "Performance";
	private static final String ENERGY = "Energy";
	private static final String PROBLEMS = "Health problems";

	private static final List<String> CHARACTERISTICS = List.of(
													FATIGUE, 
													HUNGER,
													STRESS,
													THIRST,
													PROBLEMS);

	public PersonHealthCommand() {
		super("h", "health", "About health");
	}

	@Override
	public boolean execute(Conversation context, String input, Person person) {
		boolean isExpert = context.getRoles().contains(ConversationRole.EXPERT);
		boolean active = isExpert; // If expert then stay looping until told otherwise
				
		do {
			StructuredResponse responseText = new StructuredResponse();		
			responseText.appendHeading("Health Indicators");
			
			PhysicalCondition pc = person.getPhysicalCondition();
			
			double energy = Math.round(pc.getEnergy()*10.0)/10.0;
	        double stress = pc.getStress();
	        double perf = pc.getPerformanceFactor()*100D;
	                
			String h = !pc.isHungry() ? "(Not Hungry)" : "(Hungry)";
			String t = !pc.isThirsty() ? "(Not Thirsty)" : "(Thirsty)";
			
			responseText.appendLabeledString(THIRST, String.format(CommandHelper.MILLISOL_FORMAT, pc.getThirst()) + " " + t);		
			responseText.appendLabeledString(HUNGER, String.format(CommandHelper.MILLISOL_FORMAT, pc.getHunger()) + " " + h);
			responseText.appendLabeledString(ENERGY, energy + " kJ");
			responseText.appendLabeledString(FATIGUE, String.format(CommandHelper.MILLISOL_FORMAT, pc.getFatigue()));		
			responseText.appendLabeledString(PERFORMANCE, String.format(CommandHelper.PERC_FORMAT, perf));
			responseText.appendLabeledString(STRESS,  String.format(CommandHelper.PERC_FORMAT, stress));		
			responseText.appendLabeledString("Surplus Ghrelin", String.format(CommandHelper.MILLISOL_FORMAT,
											person.getCircadianClock().getSurplusGhrelin()));
			responseText.appendLabeledString("Surplus Leptin", String.format(CommandHelper.MILLISOL_FORMAT,
											 person.getCircadianClock().getSurplusLeptin()));
			
			List<String> probs = pc.getProblems().stream().map(hp -> hp.getIllness().getType().getName())
												 .collect(Collectors.toList());
			responseText.appendNumberedList("Problems", probs);
			
			context.println(responseText.getOutput());
			
			// If expert then maybe change values
			if (isExpert) {
				String change = context.getInput("Change values (Y/N)?");
		        
		        if ("Y".equalsIgnoreCase(change)) {
		        	changeCondition(context, pc);
		        }
		        else {
		        	active = false;
		        }
			}
		}
		while (active);
		return true;
	}

	/**
	 * Changes certain characteristics of a condition.
	 * 
	 * @param context
	 * @param pc
	 */
	private void changeCondition(Conversation context, PhysicalCondition pc) {
		// Choose characteristic
		int choice = CommandHelper.getOptionInput(context, CHARACTERISTICS, "Which attribute to change?");
		if (choice < 0) {
			return;
		}

		String choosen = CHARACTERISTICS.get(choice);
		if (choosen.equals(PROBLEMS)) {
			addHealthProblem(context, pc);
		}
		else {
			// Simple numeric value
 			int newValue = context.getIntInput("New value:");
			
			context.println("Setting characteristic " + CHARACTERISTICS.get(choice)
							+ " to the new value " + newValue);
			switch(CHARACTERISTICS.get(choice)) {
			case FATIGUE:
				pc.setFatigue(newValue);
				break;
				
			case STRESS:
				pc.setStress(newValue);
				break;
				
			case HUNGER:
				pc.setHunger(newValue);
				break;
				
			case THIRST:
				pc.setThirst(newValue);
				break;
	
			default:
				context.println("Invalid input !");
			}
		}
	}
	
	/**
	 * Adds a new medical problem, to a person.
	 * 
	 * @param context
	 * @param pc
	 */
	private void addHealthProblem(Conversation context, PhysicalCondition pc) {
	
		// Choose one
		List<Complaint> complaints = SimulationConfig.instance().getMedicalConfiguration().getComplaintList();
		List<String> problems = complaints.stream().map(c -> c.getType().getName()).collect(Collectors.toList());
		int choice = CommandHelper.getOptionInput(context, problems, "Choose a new health complaint");
		if (choice <= 0) {
			return;
		}
	
		Complaint choosen = complaints.get(choice);
		context.println("Adding new Complaint " + choosen.getType().getName());
		pc.addMedicalComplaint(choosen);
	}
}
