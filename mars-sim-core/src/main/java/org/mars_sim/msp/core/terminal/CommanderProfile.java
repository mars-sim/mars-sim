/**
 * Mars Simulation Project
 * CommanderInfo.java
 * @version 3.1.0 2018-09-24
 * @author Manny Kung
 */

package org.mars_sim.msp.core.terminal;

import org.beryx.textio.ReadAbortedException;
import org.beryx.textio.ReadHandlerData;
import org.beryx.textio.ReadInterruptionStrategy;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.JobManager;
import org.mars_sim.msp.core.person.ai.job.JobType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.beryx.textio.ReadInterruptionStrategy.Action.ABORT;

/**
 * The class for setting up a customized commander profile. It reads handlers and allow going back to the previous field.
 */
public class CommanderProfile implements BiConsumer<TextIO, RunnerData> {


	private Contact contact = new Contact();
    
	private static TextTerminal<?> terminal;
	
    private final List<Runnable> operations = new ArrayList<>();

    public CommanderProfile(TextIO textIO) {
        textIO = TextIoFactory.getTextIO();
        terminal = textIO.getTextTerminal();
    }
    
    public static void main(String[] args) {
    	TextIO textIO = TextIoFactory.getTextIO();
        new CommanderProfile(textIO).accept(textIO, null);
    }
    
    public void disposeTerminal() {
    	terminal.dispose(null);
    }

    public String printJobs() {
//        List<Job> jobs = JobManager.getJobs();

		List<String> jobs = JobType.getEditedList();

    	String s = "";
        for (int i=0; i< jobs.size(); i++) {  	
        	s = s + "(" + i + "). " + jobs.get(i) + "  \t";
        	if (jobs.size() < i+1) {
        		s = s + "(" + i+1 + "). " + jobs.get(i+1) + System.lineSeparator();
        	}
        }
        
        return s;
        //terminal.println(s);
        
//        for (int i=0; i< jobs.size(); i++) {
//        	terminal.println("(" + i + "). " + jobs.get(i).getClass().getSimpleName() + " \t\t");
//        	if (jobs.size() < i+1) {
//        		terminal.print("(" + i+1 + "). " + jobs.get(i+1).getClass().getSimpleName() + "\t\t");//+ System.lineSeparator());
//        		terminal.println();
//        	}
//        }
    }
    
    public String printCountries() {
    	List<String> countries = UnitManager.getCountryList();
    	String s = "";
        for (int i=0; i< countries.size(); i++) {  	
        	s = s + "(" + i + "). " + countries.get(i).toString() + "  \t";
        	if (countries.size() < i+1) {
        		s = s + "(" + i+1 + "). " + countries.get(i+1).toString() + System.lineSeparator();
        	}
        }
        
        return s;
        //terminal.println(s);
        
//        for (int i=0; i< countries.size(); i++) {
//        	
//        	terminal.println("(" + i + "). " + countries.get(i).toString() + "\t\t");
//        	if (countries.size() < i+1) {
//        		terminal.print("(" + i+1 + "). " + countries.get(i+1).toString() + "\t\t");//+ System.lineSeparator());
//        		terminal.println();
//        	}
//        }
    }

    
    @Override
    public void accept(TextIO textIO, RunnerData runnerData) { 
 //       TextTerminal<?> terminal = textIO.getTextTerminal();
        String initData = (runnerData == null) ? null : runnerData.getInitData();
        AppUtil.printGsonMessage(terminal, initData);
	
        addString(textIO, "First Name", () -> contact.firstName, s -> contact.firstName = s);
        addString(textIO, "Last Name", () -> contact.lastName, s -> contact.lastName = s);
        addChar(textIO, "Gender (M/F)", () -> contact.gender, s -> contact.gender = s);
        addAge(textIO, "Age", () -> contact.age, s -> contact.age = s);	      
//        terminal.println(System.lineSeparator() + "Job List : ");
//        printJobs();
//        terminal.println();
        addJobTask(textIO, "Job (0-15)", () -> contact.job, s -> contact.job = s);	
//        terminal.println("Country List : ");
//        printCountries();
//        terminal.println();
        addCountryTask(textIO, "Country (0-27)", () -> contact.country, s -> contact.country = s);
//        addPhaseTask(textIO, "Settlement Phase (1-4)", () -> contact.phase, s -> contact.phase = s);	
     
//        terminal.println(System.lineSeparator());
             
        setUpCountryKey();
        setUpJobKey();
        setUpUndoKey();
        
        terminal.println(System.lineSeparator() + "Commander's Profile: " + contact);
        UnitManager.setCommander(true);
        
//        textIO.newStringInputReader().withMinLength(0).read("\nPress enter to continue...\n");     
//		textIO.dispose();

    }
    
    public void setUpAbortKey() {

//      String keyStrokeAbort = "alt Z";
//      
//      boolean registeredAbort = terminal.registerHandler(keyStrokeAbort,
//              t -> new ReadHandlerData(ReadInterruptionStrategy.Action.ABORT)
//                      .withPayload(System.getProperty("user.name", "nobody")));
//      
//      if (registeredAbort) {
//          terminal.println("Press Alt-Z to abort the program.");
//      }
     
 
    }
    
    public void setUpCountryKey() {
        
        String keyCountries = "ctrl C";
        
        boolean isKeyCountries = terminal.registerHandler(keyCountries, t -> {
            terminal.executeWithPropertiesPrefix("country",
                    tt ->   {   
			           	tt.print(System.lineSeparator() 
			           		+ System.lineSeparator() 
			           		+ "--------------List of Countries --------------" 
			           		+ System.lineSeparator());
			        	tt.print(printCountries());   
                    }
            );
            return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
        });
        
        if (isKeyCountries) {
           	terminal.println("Press Ctrl-C to show a list of countries.");
        }
    }

    public void setUpJobKey() {
        String keyJobs = "ctrl J";
        
        boolean isKeyJobs = terminal.registerHandler(keyJobs, t -> {
            terminal.executeWithPropertiesPrefix("job",
                    tt ->   {   
			           	tt.print(System.lineSeparator() 
			           		+ System.lineSeparator() 
			           		+ "--------------List of Job Type --------------" 
			           		+ System.lineSeparator());
			        	tt.print(printJobs());   
                    }
            );
            return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
        });
        
        if (isKeyJobs) {
           	terminal.println("Press Ctrl-J to show a list of job type.");
        }
        
    }

    public void setUpUndoKey() {
        String backKeyStroke = "ctrl U";
        
        boolean registeredBackKeyStroke = terminal.registerHandler(backKeyStroke, t -> new ReadHandlerData(ABORT));
        if (registeredBackKeyStroke) {
            terminal.println("Press Ctrl-U to go back to the previous field." + System.lineSeparator());
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

    }

    
    private void addString(TextIO textIO, String prompt, Supplier<String> defaultValueSupplier, Consumer<String> valueSetter) {
        operations.add(() -> valueSetter.accept(textIO.newStringInputReader()
                .withDefaultValue(defaultValueSupplier.get())
                .read(prompt)));
    }

    private void addChar(TextIO textIO, String prompt, Supplier<Character> defaultValueSupplier, Consumer<Character> valueSetter) {
        operations.add(() -> valueSetter.accept(textIO.newCharInputReader()
        		.withDefaultValue('M')
                .read(prompt)));
    }
    
    private void addAge(TextIO textIO, String prompt, Supplier<Integer> defaultValueSupplier, Consumer<Integer> valueSetter) {
        operations.add(() -> valueSetter.accept(textIO.newIntInputReader()
                .withDefaultValue(30)
                .withMinVal(21) //defaultValueSupplier.get())
                .read(prompt)));
    }

    private void addJobTask(TextIO textIO, String prompt, Supplier<Integer> defaultValueSupplier, Consumer<Integer> valueSetter) {
        operations.add(() -> valueSetter.accept(textIO.newIntInputReader()
                .withDefaultValue(4)
                .withMinVal(0)
                .withMaxVal(15)//defaultValueSupplier.get())
                .read(prompt)));
    }
    
    private void addCountryTask(TextIO textIO, String prompt, Supplier<Integer> defaultValueSupplier, Consumer<Integer> valueSetter) {
        operations.add(() -> valueSetter.accept(textIO.newIntInputReader()
                .withDefaultValue(4)
                .withMinVal(0)
                .withMaxVal(27)//defaultValueSupplier.get())
                .read(prompt)));
    }
    
//    private void addPhaseTask(TextIO textIO, String prompt, Supplier<Integer> defaultValueSupplier, Consumer<Integer> valueSetter) {
//        operations.add(() -> valueSetter.accept(textIO.newIntInputReader()
//                .withDefaultValue(1)
//                .withMinVal(1)
//                .withMaxVal(4)//defaultValueSupplier.get())
//                .read(prompt)));
//    }
    
    
    @Override
    public String toString() {
        return "Commander's Profile";
    }
    
    public Contact getContact() {
    	return contact;
    }
    
	public class Contact {
		
        private String firstName;
        private String lastName;
        private Character gender;
        private int age;
        private int job;
        private int phase;
        private int country;
        
        
        public String getFullName() {
        	if (firstName == null || lastName == null)
        		return null;
        	else {
        		return firstName + " " + lastName;
        	}
        }

        public String getGender() {
        	return gender.toString();
        }

        public int getCountry() {
        	return country;
        }
        
        public int getAge() {
        	return age;
        }

        public int getJob() {
        	return job;
        }
        
        public int getPhase() {
        	return phase;
        }
        
        @Override
        public String toString() {
            return System.lineSeparator() + "   First Name: " + firstName +
            	   System.lineSeparator() + "   Last Name: " + lastName +
            	   System.lineSeparator() + "   Gender: " + gender +
            	   System.lineSeparator() + "   Age: " + age +
            	   System.lineSeparator() + "   Job: " + JobType.getEditedJobString(job) +
            	   System.lineSeparator() + "   Country: " + UnitManager.getCountryByID(country) + " (" + UnitManager.getSponsorByCountryID(country) + ")" 
//            	   System.lineSeparator() + "   Settlement Phase: " + phase
            	   ;
            
        }
    }
}
