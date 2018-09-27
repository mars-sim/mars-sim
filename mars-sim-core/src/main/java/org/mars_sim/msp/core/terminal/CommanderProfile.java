/**
 * Mars Simulation Project
 * CommanderInfo.java
 * @version 3.1.0 2018-09-24
 * @author Manny Kung
 */

package org.mars_sim.msp.core.terminal;

//import org.beryx.textio.ReadAbortedException;
//import org.beryx.textio.ReadHandlerData;
//import org.beryx.textio.TextIO;
//import org.beryx.textio.TextIoFactory;
//import org.beryx.textio.TextTerminal;
//import org.beryx.textio.app.AppUtil;

import org.beryx.textio.*;
import org.mars_sim.msp.core.UnitManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.beryx.textio.ReadInterruptionStrategy.Action.ABORT;

/**
 * Illustrates how to use read handlers to allow going back to a previous field.
 */
public class CommanderProfile implements BiConsumer<TextIO, RunnerData> {

	private Contact contact = new Contact();
    
	TextTerminal<?> terminal;
	
    private final List<Runnable> operations = new ArrayList<>();

    public CommanderProfile(TextIO textIO) {
        textIO = TextIoFactory.getTextIO();
        terminal = textIO.getTextTerminal();
    }
    
    public static void main(String[] args) {
    	TextIO textIO = TextIoFactory.getTextIO();
        new CommanderProfile(textIO).accept(textIO, null);
    }

    @Override
    public void accept(TextIO textIO, RunnerData runnerData) { 
 //       TextTerminal<?> terminal = textIO.getTextTerminal();
        String initData = (runnerData == null) ? null : runnerData.getInitData();
        AppUtil.printGsonMessage(terminal, initData);
	
        addTask(textIO, "First Name", () -> contact.firstName, s -> contact.firstName = s);
        addTask(textIO, "Last Name", () -> contact.lastName, s -> contact.lastName = s);
        addTask(textIO, "Gender [m/f]", () -> contact.gender, s -> contact.gender = s);
        addIntTask(textIO, "Age", () -> contact.age, s -> contact.age = s);	
        
        String backKeyStroke = "ctrl U";
        boolean registered = terminal.registerHandler(backKeyStroke, t -> new ReadHandlerData(ABORT));
        if(registered) {
            terminal.println("(Note : press '" + backKeyStroke + "' to go back to the previous field)\n");
        }
        int step = 0;
        while(step < operations.size()) {
            terminal.setBookmark("bookmark_" + step);
            try {
                operations.get(step).run();
            } catch (ReadAbortedException e) {
                if(step > 0) step--;
                terminal.resetToBookmark("bookmark_" + step);
                continue;
            }
            step++;
        }
        
        terminal.println("\nCommander's Profile: " + contact);
        
        UnitManager.isProfileRetrieved = false;
        
        textIO.newStringInputReader().withMinLength(0).read("\nPress enter to continue...");     
		textIO.dispose();
    }
    
    private void addTask(TextIO textIO, String prompt, Supplier<String> defaultValueSupplier, Consumer<String> valueSetter) {
        operations.add(() -> valueSetter.accept(textIO.newStringInputReader()
                .withDefaultValue(defaultValueSupplier.get())
                .read(prompt)));
    }

    private void addIntTask(TextIO textIO, String prompt, Supplier<Integer> defaultValueSupplier, Consumer<Integer> valueSetter) {
        operations.add(() -> valueSetter.accept(textIO.newIntInputReader()
                .withDefaultValue(30)
                .withMinVal(21) //defaultValueSupplier.get())
                .read(prompt)));
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": reading commander's profile.\n" +
                "(Illustrates how to use read handlers to allow going back to a previous field.)";
    }
    
    public Contact getContact() {
    	return contact;
    }
    
	public class Contact {
        private String firstName;
        private String lastName;
        private String gender;
        private int age;
        
        public String getFullName() {
        	if (firstName == null || lastName == null)
        		return null;
        	else {
        		return firstName + " " + lastName;
        	}
        }

        public String getGender() {
        	return gender;
        }

        public int getAge() {
        	return age;
        }

        
        @Override
        public String toString() {
            return "\n   First Name: " + firstName +
                   "\n   Last Name: " + lastName +
                   "\n   Gender: " + gender +
                   "\n   Age: " + age;
        }
    }
}
