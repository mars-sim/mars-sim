/*
 * Mars Simulation Project
 * CommanderProfile.java
 * @date 2021-11-29
 * @author Manny Kung
 */
package org.mars.sim.console;

import static org.beryx.textio.ReadInterruptionStrategy.Action.ABORT;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.SimulationFiles;
import org.mars_sim.msp.core.person.Commander;
import org.mars_sim.msp.core.person.PersonNameSpecConfig;
import org.mars_sim.msp.core.person.ai.job.util.JobType;

/**
 * The class for setting up a customized commander profile. It reads handlers and allow going back to the previous field.
 */
public class CommanderProfile implements BiConsumer<TextIO, RunnerData> {

	private static final Logger logger = Logger.getLogger(CommanderProfile.class.getName());

    private static final String KEY_STROKE_UP = "pressed UP";
    private static final String KEY_STROKE_DOWN = "pressed DOWN";

    private static final String FILENAME = "commander.txt";
    private static final String EXT = ".txt";

    private static final String BOOKMARK = "bookmark_";
    
    private static final int SPACES = 18;

    private int choiceIndex = -1;

    private String originalInput = "";

    private String[] choices = {};

	private String[] fields = {
			"First Name",
			"Last Name",
			"Gender (M, F)",
			"Age (18-80)",
			"Job, Ctrl-J",
			"Country of Origin, Ctrl-O",
			"Sponsor, Ctrl-S"
			};

	private static SimulationConfig config = SimulationConfig.instance();

	private static Commander commander = config.getPersonConfig().getCommander();

	private MarsTerminal terminal;

    private final List<Runnable> operations = new ArrayList<>();

	private List<String> countryList;
	private List<String> authorities;


    public CommanderProfile(InteractiveTerm term) {

    	terminal = term.getTerminal();

    	// Get Country list from known PersonConfig
        PersonNameSpecConfig nameConfig = new PersonNameSpecConfig();
    	countryList = new ArrayList<>(nameConfig.getItemNames());
    	Collections.sort(countryList);

        authorities = new ArrayList<>(config.getReportingAuthorityFactory().getItemNames());
	}

    private void setChoices(String... choices) {
        this.originalInput = "";
        this.choiceIndex = -1;
        this.choices = choices;
    }

    private String getFieldName(String field) {
    	return String.format("%27s", field);
    }

    @Override
    public void accept(TextIO textIO, RunnerData runnerData) {
        String initData = (runnerData == null) ? null : runnerData.getInitData();
        AppUtil.printGsonMessage(terminal, initData);

        setUpArrows();

        addString(textIO, getFieldName(fields[0]), () -> commander.getFirstName(), s -> commander.setFirstName(s));
        addString(textIO, getFieldName(fields[1]), () -> commander.getLastName(), s -> commander.setLastName(s));
        addGender(textIO, getFieldName(fields[2]), () -> commander.getGender(), s -> commander.setGender(s));
        addAge(textIO, getFieldName(fields[3]),  s -> commander.setAge(s));
        addJobTask(textIO, getFieldName(fields[4]), JobType.values().length,
        		i -> {
        			JobType jt = JobType.values()[i-1];
        			commander.setJob(jt.getName());
        			});
        addCountryTask(textIO, getFieldName(fields[5]), countryList.size(),
        		i -> {
        			String s = countryList.get(i-1);
        			commander.setCountryStr(s);
        			});
        addSponsorTask(textIO, getFieldName(fields[6]), authorities.size(),
        		i-> commander.setSponsorStr(authorities.get(i-1))
        			);

        setUpJobKey();
        setUpCountryKey();
        setUpSponsorKey();

        setUpUndoKey();

        StringBuilder details = new StringBuilder();
        commander.outputDetails(details);
        terminal.println(System.lineSeparator()
        		+ "                * * *  Commander's Profile  * * *"
        		+ System.lineSeparator()
        		+ details.toString()
        		+ System.lineSeparator());

        boolean toSave = textIO.newBooleanInputReader().withDefaultValue(true).read("Save this profile ?");
    	if (toSave) {
			terminal.print(System.lineSeparator());
	        try {
				saveProfile();
			} catch (IOException e) {
				logger.severe("Problems saving the profile: " + e.getMessage());
			}
    	}
    	else {
    		terminal.print("Profile not saved.");
    	}
    }

    private void setUpCountryKey() {
        String keyCountries = "ctrl O";
        boolean isKeyCountries = terminal.registerHandler(keyCountries, t -> {
            terminal.executeWithPropertiesPrefix("country",
                    tt ->   {
			           	tt.print(System.lineSeparator()
			           		+ System.lineSeparator()
			           		+ "    ---------------------- Country Listing ----------------------"
			           		+ System.lineSeparator()
			           		+ System.lineSeparator());
			        	tt.print(printList(countryList));
                    }
            );
            return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
        });

        if (isKeyCountries) {
           	terminal.println("Press Ctrl-O to show a list of countries.");
        }
    }


    private void setUpSponsorKey() {
        String key = "ctrl S";
    	final List<String> list = new ArrayList<>();
    	for (String ra : authorities) {
			list.add(ra);
		}

        boolean isKey = terminal.registerHandler(key, t -> {
            terminal.executeWithPropertiesPrefix("sponsor",
                    tt ->   {
			           	tt.print(System.lineSeparator()
			           		+ System.lineSeparator()
			           		+ "    ----------------------- Sponsor Listing -----------------------"
			           		+ System.lineSeparator()
			           		+ System.lineSeparator());
			        	tt.print(printOneColumn(list));
                    }
            );
            return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
        });

        if (isKey) {
           	terminal.println("Press Ctrl-S to show a list of sponsors.");
        }

    }

    private void setUpJobKey() {
        String keyJobs = "ctrl J";
        List<String> jobNames = new ArrayList<>();
        for (JobType jt : JobType.values()) {
			jobNames.add(jt.getName());
		}

        boolean isKeyJobs = terminal.registerHandler(keyJobs, t -> {
            terminal.executeWithPropertiesPrefix("job",
                    tt ->   {
			           	tt.print(System.lineSeparator()
			           		+ System.lineSeparator()
			           		+ "    ----------------------- Job Listing -----------------------"
			           		+ System.lineSeparator()
			           		+ System.lineSeparator());
			        	tt.print(printList(jobNames));
                    }
            );
            return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
        });

        if (isKeyJobs) {
           	terminal.println("Press Ctrl-J to show a list of jobs.");
        }

    }

    private void setUpUndoKey() {
        String backKeyStroke = "ctrl U";

        boolean registeredBackKeyStroke = terminal.registerHandler(backKeyStroke, t -> new ReadHandlerData(ABORT));
        if (registeredBackKeyStroke) {
            terminal.println("Press Ctrl-U to go back to the previous field." + System.lineSeparator());
        }

        int step = 0;
        while(step < operations.size()) {
            terminal.setBookmark(BOOKMARK + step);
            try {
                operations.get(step).run();
            } catch (ReadAbortedException e) {
                if(step > 0) step--;
                terminal.resetToBookmark(BOOKMARK + step);
                continue;
            }
            step++;
        }

    }

    private void setUpArrows() {
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

    private void addAge(TextIO textIO, String prompt,  Consumer<Integer> valueSetter) {
        operations.add(() -> {
        	setChoices();
        	valueSetter.accept(textIO.newIntInputReader()
                .withDefaultValue(30)
                .withMaxVal(80)
                .withMinVal(18)
                .read(prompt));
        	});
    }

    private void addJobTask(TextIO textIO, String prompt, int max, Consumer<Integer> valueSetter) {
        operations.add(() -> {
        	setChoices();
        	valueSetter.accept(textIO.newIntInputReader()
       			.withDefaultValue(2)
                .withMinVal(1)
                .withMaxVal(max)
                .read(prompt));
        	});
    }

    private void addSponsorTask(TextIO textIO, String prompt, int max, Consumer<Integer> valueSetter) {
        operations.add(() -> {
        	setChoices();
        	valueSetter.accept(textIO.newIntInputReader()
        		.withDefaultValue(7)
                .withMinVal(1)
                .withMaxVal(max)
                .read(prompt));
    		});
    }

    private void addCountryTask(TextIO textIO, String prompt, int max, Consumer<Integer> valueSetter) {
        operations.add(() -> {
        	setChoices();
        	valueSetter.accept(textIO.newIntInputReader()
        		.withDefaultValue(28)
                .withMinVal(1)
                .withMaxVal(max)
                .read(prompt));
    		});
    }

    /**
     * Add the parenthesis and the numerical and prints the list
     *
     * @return List<String>
     */
    private static List<String> printOneColumn(List<String> list) {
       	List<String> newList = new ArrayList<>();

        for (int i=0; i< list.size(); i++) {
        	newList.add(String.format("(%1d). %s", (i+1), list.get(i)));
        }

        return newList;
    }

    /**
     * Generates and prints the list that needs to be processed
     *
     * @return List<String>
     */
    private static List<String> printList(List<String> list) {

       	List<String> newList = new ArrayList<>();
       	StringBuilder s = new StringBuilder();

        for (int i=0; i< list.size(); i++) {
        	int column = 0;

        	String c = "";

        	// Find out what column
        	if ((i - 1) % 3 == 0)
        		column = 1;
        	else if ((i - 2) % 3 == 0)
        		column = 2;

        	// Look at how many whitespaces needed before printing each column
			if (column == 0) {
				c = list.get(i);
			}

			else if (column == 1 || column == 2) {
	        	c = list.get(i);
	        	int num = SPACES - list.get(i-1).length();

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
                newList.add(s.toString());
                s = new StringBuilder();
            }
        }

        return newList;
    }

	private static void saveProfile() throws IOException {
		setProperties(commander);
	}

	private static void setProperties(Commander commander) throws IOException {
	   	Properties p = new Properties();
		p.setProperty("commander.lastname", commander.getLastName());
		p.setProperty("commander.firstname", commander.getFirstName());
		p.setProperty("commander.gender", commander.getGender());
		p.setProperty("commander.age", commander.getAge() + "");
		p.setProperty("commander.job", commander.getJob());
		p.setProperty("commander.country", commander.getCountryStr());
		p.setProperty("commander.sponsor", commander.getSponsorStr());
	    storeProperties(p);

	}

	private static void storeProperties(Properties p) throws IOException {
        try (FileOutputStream fr = new FileOutputStream(SimulationFiles.getSaveDir() + "/" + FILENAME + EXT)) {
	        p.store(fr, "Commander's Profile");
	        logger.config("Commander's profile saved: " + p);
        }
    }

    public static boolean loadProfile() throws IOException {
		File f = new File(SimulationFiles.getSaveDir(), "/" + FILENAME + EXT);

		if (f.exists() && f.canRead()) {

	    	Properties p = new Properties();
	        try (FileInputStream fi = new FileInputStream(f)) {
		        p.load(fi);
		        fi.close();

		        commander = loadProperties(p, commander);
		        logger.config("Commander's profile loaded: " + p);
		        return true;
	        }
		}
		else {
	        logger.config("Can't find " + f.getAbsolutePath());
	        return false;
		}
    }

    public static void cancelLoadingProfile() {
    	// Question: should the commander instance be set to null ?
    	commander = null;
    }

    private static Commander loadProperties(Properties p, Commander cc) {
        cc.setLastName(p.getProperty("commander.lastname"));
        cc.setFirstName(p.getProperty("commander.firstname"));
        cc.setGender(p.getProperty("commander.gender"));
        cc.setAge(Integer.parseInt(p.getProperty("commander.age")));
        cc.setJob(p.getProperty("commander.job"));
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
