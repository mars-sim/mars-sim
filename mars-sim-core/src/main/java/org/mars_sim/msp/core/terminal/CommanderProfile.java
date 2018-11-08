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
import org.beryx.textio.swing.SwingTextTerminal;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.ai.job.JobType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.beryx.textio.ReadInterruptionStrategy.Action.ABORT;

/**
 * The class for setting up a customized commander profile. It reads handlers and allow going back to the previous field.
 */
public class CommanderProfile implements BiConsumer<TextIO, RunnerData> {

    private static final String KEY_STROKE_UP = "pressed UP";
    private static final String KEY_STROKE_DOWN = "pressed DOWN";

    private static final String ONE_SPACE = " ";
    
    private int choiceIndex = -1;
    
    private String originalInput = "";

    private String[] choices = {};
    
	private String[] fields = {
			"First Name",
			"Last Name",
			"Gender (M, F)",
			"Age (18-80)",
			"Job (1-16)",
			"Mars Society Affiliated",
			"Country (1-28)"};
	
	private Commander commander;
    	
	private SwingTextTerminal terminal;
	
//	private static TextIO textIO;
	
	private static PersonConfig personConfig;
	

    private final List<Runnable> operations = new ArrayList<>();

    public CommanderProfile(InteractiveTerm term) {	
    	personConfig = SimulationConfig.instance().getPersonConfiguration();
    	commander = personConfig.getCommander();
    	terminal = term.getTerminal();
 //   	textIO = term.getTextIO();
    	
	}

    public void setChoices(String... choices) {
        this.originalInput = "";
        this.choiceIndex = -1;
        this.choices = choices;
    }
    
    public String getFieldName(String field) {
    	StringBuilder s = new StringBuilder();
    	int size = 27 - field.length();
    	for (int i = 0; i < size; i++) {
    		s.append(ONE_SPACE);
    	}
    	s.append(field);
    	return s.toString();
    }
    
    @Override
    public void accept(TextIO textIO, RunnerData runnerData) {    
        String initData = (runnerData == null) ? null : runnerData.getInitData();
        AppUtil.printGsonMessage(terminal, initData);
        
//        setUpMouseCopyKey();
        setUpArrows();
        
        
        addString(textIO, getFieldName(fields[0]), () -> commander.getFirstName(), s -> commander.setFirstName(s));
        addString(textIO, getFieldName(fields[1]), () -> commander.getLastName(), s -> commander.setLastName(s));     
        addGender(textIO, getFieldName(fields[2]), () -> commander.getGender(), s -> commander.setGender(s));
        addAge(textIO, getFieldName(fields[3]), () -> commander.getAge(), s -> commander.setAge(s));	      
        addJobTask(textIO, getFieldName(fields[4]), () -> commander.getJob(), s -> commander.setJob(s));	
        addAffiliation(textIO, getFieldName(fields[5]), () -> commander.isMarsSocietyAffiliated(), s -> commander.setMarsSocietyAffiliated(s));
        addCountryTask(textIO, getFieldName(fields[6]), () -> commander.getCountry(), s -> commander.setCountry(s));
          
        setUpCountryKey();
        setUpJobKey();
        setUpUndoKey();
       
        terminal.println(System.lineSeparator() 
        		+ "                * * *  COMMANDER'S PROFILE * * *" 
        		+ System.lineSeparator()
        		+ commander
        		+ System.lineSeparator());
        UnitManager.setCommander(true);
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
    
    public void setUpMouseCopyKey() {
    	
    	terminal.registerHandler("ctrl C", t -> {
    	    t.getTextPane().copy();
    	    return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
    	});
    	terminal.registerHandler("ctrl V", t -> {
 //   	    t.getTextPane().paste();
    	    String selectedText = t.getTextPane().getSelectedText();
    	    if(selectedText != null) {
    	        t.getTextPane().setCaretPosition(t.getDocument().getLength());
    	        t.appendToInput(selectedText, false);
    	    }
    	    return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
    	});
    }
    
    public void setUpCountryKey() {
        
        String keyCountries = "ctrl O";
        
        boolean isKeyCountries = terminal.registerHandler(keyCountries, t -> {
            terminal.executeWithPropertiesPrefix("country",
                    tt ->   {   
			           	tt.print(System.lineSeparator() 
			           		+ System.lineSeparator() 
			           		+ "    ---------------------- Country Listing ----------------------" 
			           		+ System.lineSeparator() 
			           		+ System.lineSeparator());
			        	List<String> countries = UnitManager.getCountryList();
			        	tt.print(printList(countries));   
                    }
            );
            return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
        });
        
        if (isKeyCountries) {
           	terminal.println("Press Ctrl-O to show a list of countries.");
        }
    }

  
    
    
    public void setUpJobKey() {
        String keyJobs = "ctrl J";
        
        boolean isKeyJobs = terminal.registerHandler(keyJobs, t -> {
            terminal.executeWithPropertiesPrefix("job",
                    tt ->   {   
			           	tt.print(System.lineSeparator() 
			           		+ System.lineSeparator() 
			           		+ "    ----------------------- Job Listing -----------------------" 
			           		+ System.lineSeparator()
			           		+ System.lineSeparator());
			        	List<String> jobs = JobType.getEditedList();
			        	tt.print(printList(jobs));
                    }
            );
            return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
        });
        
        if (isKeyJobs) {
           	terminal.println("Press Ctrl-J to show a list of jobs.");
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

    public void setUpArrows() {
        terminal.registerHandler(KEY_STROKE_UP, t -> {
            if(choiceIndex < 0) {
                originalInput = terminal.getPartialInput();
            }
            if(choiceIndex < choices.length - 1) {
                choiceIndex++;
                t.replaceInput(choices[choiceIndex], false);
            }
            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });

        terminal.registerHandler(KEY_STROKE_DOWN, t -> {
            if(choiceIndex >= 0) {
                choiceIndex--;
                String text = (choiceIndex < 0) ? originalInput : choices[choiceIndex];
                t.replaceInput(text, false);
            }
            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });
    }
   
    private void addString(TextIO textIO, String prompt, Supplier<String> defaultValueSupplier, Consumer<String> valueSetter) {
        operations.add(() -> {
        	setChoices();
        	valueSetter.accept(textIO.newStringInputReader()
                .withDefaultValue(defaultValueSupplier.get())
                .read(prompt));
        	});
    }

    private void addGender(TextIO textIO, String prompt, Supplier<String> defaultValueSupplier, Consumer<String> valueSetter) {
        operations.add(() -> {
        	String[] sex = {"M", "F"};
        	setChoices(sex);
        	valueSetter.accept(textIO.newStringInputReader()
//                    .withInlinePossibleValues(sex)
                    .withIgnoreCase()
//                    .withPromptAdjustments(false)
//				.withInlinePossibleValues("m", "f", "M", "F")
                .withDefaultValue(defaultValueSupplier.get())
                .read(prompt));
        	});
    }
    
//    private void addChar(TextIO textIO, String prompt, Supplier<Character> defaultValueSupplier, Consumer<Character> valueSetter) {
//        setChoices("M", "F");
//        
//        operations.add(() -> valueSetter.accept(textIO.newCharInputReader()
//        		.withDefaultValue('M')
//                .read(prompt)));
//    }
    
    private void addAge(TextIO textIO, String prompt, Supplier<Integer> defaultValueSupplier, Consumer<Integer> valueSetter) {
        operations.add(() -> {
        	setChoices();
        	valueSetter.accept(textIO.newIntInputReader()       
                .withDefaultValue(30) //
//        		.withDefaultValue(defaultValueSupplier.get())
//                .withPromptAdjustments(false)
//				.withNumberedPossibleValues(age)
                .withMaxVal(80)
                .withMinVal(18) 
                .read(prompt));
        	});
    }

    private void addJobTask(TextIO textIO, String prompt, Supplier<Integer> defaultValueSupplier, Consumer<Integer> valueSetter) {
        operations.add(() -> {
        	setChoices();
        	valueSetter.accept(textIO.newIntInputReader()
       			.withDefaultValue(5)
//                .withDefaultValue(defaultValueSupplier.get())
                .withMinVal(1)
                .withMaxVal(16)
                .read(prompt));
        	});
    }
    
    private void addAffiliation(TextIO textIO, String prompt, Supplier<String> defaultValueSupplier, Consumer<String> valueSetter) {
        operations.add(() -> {
        	String[] ans = {"y", "n"};
        	setChoices(ans);
        	valueSetter.accept(textIO.newStringInputReader()
//                  .withPromptAdjustments(false)
//                  .withInlinePossibleValues(ans)
                    .withIgnoreCase()
//                    .withPromptAdjustments(false)
//				.withInlinePossibleValues("m", "f", "M", "F")
//                .withDefaultValue(defaultValueSupplier.get())
              .withDefaultValue("y")
                .read(prompt));
        	});
    }
    
    private void addCountryTask(TextIO textIO, String prompt, Supplier<Integer> defaultValueSupplier, Consumer<Integer> valueSetter) {
        operations.add(() -> {
        	setChoices();
        	valueSetter.accept(textIO.newIntInputReader()
                .withDefaultValue(5)
                .withMinVal(1)
                .withMaxVal(28)//defaultValueSupplier.get())
                .read(prompt));
    		});
    }

//    private void addPhaseTask(TextIO textIO, String prompt, Supplier<Integer> defaultValueSupplier, Consumer<Integer> valueSetter) {
//        operations.add(() -> valueSetter.accept(textIO.newIntInputReader()
//                .withDefaultValue(1)
//                .withMinVal(1)
//                .withMaxVal(4)//defaultValueSupplier.get())
//                .read(prompt)));
//    }
    
    
    /**
     * Generates and prints the list that needs to be processed
     * 
     * @return List<String>
     */
    public static List<String> printList(List<String> list) {
    	
       	List<String> newList = new ArrayList<>();
    	StringBuffer s = new StringBuffer();
    	int SPACES = 18;
    	//int row = 0;
        for (int i=0; i< list.size(); i++) {
        	int column = 0;
        	
        	String c = "";
        	int num = 0;        	
        	
        	// Find out what column
        	if ((i - 1) % 3 == 0)
        		column = 1;
        	else if ((i - 2) % 3 == 0)
        		column = 2;

        	// Look at how many whitespaces needed before printing each column
			if (column == 0) {
				c = list.get(i).toString();
				num = SPACES - c.length();
	
			}
			
			else if (column == 1 || column == 2) {
	        	c = list.get(i).toString();
	        	num = SPACES - list.get(i-1).toString().length();

	        	// Handle the extra space before the parenthesis
	            for (int j=0; j < num; j++) { 
	            	s.append(" ");
	            }    			
    		}

        	if (i+1 < 10)
        		s.append(" ");
        	s.append("(");
        	s.append(i+1);
        	s.append("). ");
        	s.append(c);        		
            
            // if this is the last column
            if (column == 2 || i == list.size()-1) {
            	//s.append(System.lineSeparator());//"\\R");
                newList.add(s.toString());
                //++;
                s = new StringBuffer();
            }
        }
      
        return newList;    
    }
    

    @Override
    public String toString() {
        return "Commander's Profile";
    }
    
    public void disposeTerminal() {
    	terminal.dispose(null);
    }

    public Commander getCommander() {
    	return commander;
    }
    
}
