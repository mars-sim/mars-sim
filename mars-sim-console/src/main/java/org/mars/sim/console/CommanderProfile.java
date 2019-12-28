/**
 * Mars Simulation Project
 * CommanderInfo.java
 * @version 3.1.0 2018-09-24
 * @author Manny Kung
 */

package org.mars.sim.console;

import static org.beryx.textio.ReadInterruptionStrategy.Action.ABORT;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;

import org.beryx.textio.ReadAbortedException;
import org.beryx.textio.ReadHandlerData;
import org.beryx.textio.ReadInterruptionStrategy;
import org.beryx.textio.TextIO;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.Commander;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityType;

/**
 * The class for setting up a customized commander profile. It reads handlers and allow going back to the previous field.
 */
public class CommanderProfile implements BiConsumer<TextIO, RunnerData> {

	private static Logger logger = Logger.getLogger(CommanderProfile.class.getName());

	private static final int MAX = 27;
    private static final String KEY_STROKE_UP = "pressed UP";
    private static final String KEY_STROKE_DOWN = "pressed DOWN";

    private static final String ONE_SPACE = " ";

    private static final String FILENAME = "/commander.txt";
	private static final String DIR = Simulation.SAVE_DIR;
	private static final String PATH = DIR + FILENAME;
	
    private int choiceIndex = -1;
    
    private String originalInput = "";

    private String[] choices = {};
    
	private String[] fields = {
			"                      First Name",
			"                       Last Name",
			"                   Gender (M, F)",
			"                     Age (18-80)",
			"              Job (1-16), Ctrl-J",
			"Country of Origin (1-28), Ctrl-O",
			"           Sponsor (1-9), Ctrl-S"
			};
	
	private static Commander commander;
    	
	private MarsTerminal terminal;
	
//	private static TextIO textIO;
	
	private static PersonConfig personConfig;	

    private final List<Runnable> operations = new ArrayList<>();

    public CommanderProfile(InteractiveTerm term) {	
    	personConfig = SimulationConfig.instance().getPersonConfig();
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
    	int size = MAX - field.length();
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
        addCountryTask(textIO, getFieldName(fields[5]), () -> commander.getCountryInt(), s -> commander.setCountryInt(s));
        addSponsorTask(textIO, getFieldName(fields[6]), () -> commander.getSponsorInt(), s -> commander.setSponsorInt(s));
          
        setUpJobKey();
        setUpCountryKey();
        setUpSponsorKey();
  
        setUpUndoKey();
        
        terminal.println(System.lineSeparator() 
        		+ "                * * *  Commander's Profile  * * *" 
        		+ System.lineSeparator()
        		+ commander.toString()
        		+ System.lineSeparator());
//        UnitManager.setCommanderMode(true);
        
        boolean toSave = textIO.newBooleanInputReader().withDefaultValue(true).read("Save this profile");
        
    	if (toSave) {
			terminal.print(System.lineSeparator());
	        try {
				saveProfile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
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
			        	List<String> countries = UnitManager.getAllCountryList();
			        	tt.print(printList(countries));   
                    }
            );
            return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
        });
        
        if (isKeyCountries) {
           	terminal.println("Press Ctrl-O to show a list of countries.");
        }
    }

  
    public void setUpSponsorKey() {
        String key = "ctrl S";
        
        boolean isKey = terminal.registerHandler(key, t -> {
            terminal.executeWithPropertiesPrefix("sponsor",
                    tt ->   {   
			           	tt.print(System.lineSeparator() 
			           		+ System.lineSeparator() 
			           		+ "    ----------------------- Sponsors Listing -----------------------" 
			           		+ System.lineSeparator()
			           		+ System.lineSeparator());
			        	List<String> list = UnitManager.getAllLongSponsors();//ReportingAuthorityType.getLongSponsorList();
//			        	System.out.println(list);
			        	tt.print(printOneColumn(list));
                    }
            );
            return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
        });
        
        if (isKey) {
           	terminal.println("Press Ctrl-S to show a list of sponsors.");
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
    
    private void addSponsorTask(TextIO textIO, String prompt, Supplier<Integer> defaultValueSupplier, Consumer<Integer> valueSetter) {
        operations.add(() -> {
        	setChoices();
        	valueSetter.accept(textIO.newIntInputReader()
//                .withDefaultValue(8)
                .withMinVal(1)
                .withMaxVal(9)//defaultValueSupplier.get())
                .read(prompt));
    		});
    }
    
    private void addCountryTask(TextIO textIO, String prompt, Supplier<Integer> defaultValueSupplier, Consumer<Integer> valueSetter) {
        operations.add(() -> {
        	setChoices();
        	valueSetter.accept(textIO.newIntInputReader()
//                .withDefaultValue(5)
                .withMinVal(1)
                .withMaxVal(28)//defaultValueSupplier.get())
                .read(prompt));
    		});
    }

    /**
     * Add the parenthesis and the numerical and prints the list
     * 
     * @return List<String>
     */
    public static List<String> printOneColumn(List<String> list) {
//    	System.out.println(list);
       	List<String> newList = new ArrayList<>();
    	StringBuffer s = null;
  
        for (int i=0; i< list.size(); i++) {  
            s = new StringBuffer();
        	String c = list.get(i);

			// Look at how many white spaces needed before printing each column
        	if (i+1 < 10)
        		s.append(" ");
        	s.append("(");
        	s.append(i+1);
        	s.append("). ");
        	s.append(c);        		
            
            newList.add(s.toString());
        }
      
        return newList;    
    }
    
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
    
	public static void saveProfile() throws IOException {
		setProperties(commander);
	}

	public static void setProperties(Commander commander) throws IOException {
	   	Properties p = new Properties();
		p.setProperty("commander.lastname", commander.getLastName());
		p.setProperty("commander.firstname", commander.getFirstName());
		p.setProperty("commander.gender", commander.getGender());
		p.setProperty("commander.age", commander.getAge() + "");
		p.setProperty("commander.job", commander.getJobStr());
		p.setProperty("commander.country", commander.getCountryStr());
		p.setProperty("commander.sponsor", commander.getSponsorStr());
	    storeProperties(p);

	}
	
	public static void storeProperties(Properties p) throws IOException {
        FileOutputStream fr = new FileOutputStream(PATH);
        p.store(fr, "Commander's Profile");
        fr.close();
        logger.config("Commander's profile saved: " + p);
    }

    public static boolean loadProfile() throws IOException { 	
		File f = new File(DIR, FILENAME);

		if (f.exists() && f.canRead()) {
	    	
	    	Properties p = new Properties();
	        FileInputStream fi = new FileInputStream(PATH);
	        p.load(fi);
	        fi.close();

	        commander = loadProperties(p, commander);
	        
	        logger.config("Commander's profile loaded: " + p);
	        
	        return true;
		}
		else {
	        logger.config("Can't find " + FILENAME);
	        return false;
		}
    }
    
    
    public static Commander loadProperties(Properties p, Commander cc) {
        cc.setLastName(p.getProperty("commander.lastname"));
        cc.setFirstName(p.getProperty("commander.firstname"));
        cc.setGender(p.getProperty("commander.gender"));
        cc.setAge(Integer.parseInt(p.getProperty("commander.age")));
        cc.setJobStr(p.getProperty("commander.job"));
        cc.setCountryStr(p.getProperty("commander.country"));  
        cc.setSponsorStr(p.getProperty("commander.sponsor")); 
        
        return cc;
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
