/**
 * Mars Simulation Project
 * MarsProjectFX.java
 * @version 3.08 2016-04-28
 * @author Manny Kung
 */
package org.mars_sim.msp.javafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.awt.GraphicsEnvironment;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.mars_sim.msp.MarsProject;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.javafx.MarsProjectUtility.AppLaunch;
import org.mars_sim.msp.javafx.configEditor.ScenarioConfigEditorFX;
import org.mars_sim.msp.ui.helpGenerator.HelpGenerator;
import org.mars_sim.msp.ui.javafx.svg.SvgImageLoaderFactory;

/**--------------------------------------------------------------
 * Case A : with 'headless' and 'new' switch, it will load the following :
 **--------------------------------------------------------------
 *
 * 1. main() 						-- on Main Thread
 * 2. Default Constructor 			-- on JavaFX Application Thread 
 * 3. init()						-- on JavaFX-Launcher Thread
 * 4. SimulationTask's run() 		-- on pool-2-thread-1
 * 5. prepare()						-- on pool-2-thread-1
 * 6. start() 						-- on JavaFX Application Thread 
 * 7. initializeSimulation() 		-- on pool-2-thread-1
 * 8. handleLoadDefaultSimulation() -- on pool-2-thread-1  
 * 9. startSimulation() 			-- on pool-2-thread-1
 * 10. Simulation's start()			-- on pool-2-thread-1
 *
 * Note0 : clockScheduler is initialized on Simulation's start()
 * 
 * 
 **-------------------------------------------------------------- 
 * Case B : with 'headless' and 'load' switch, it will load the following : 
 **--------------------------------------------------------------
 *
 * * Note2 : if the switch "-helpGenerator" is included in the eclipse launcher, 
 * 		selecting "New Sim" in the Main Menu will run HelpGenerator.generateHtmlHelpFiles()
 *  	in handleNewSimulation() right before calling ScenarioConfigEditorFX.
 *  
 *  
 *
 **-------------------------------------------------------------- 
 * Case C : with 'headless' and 'html' switch, it will load the following : 
 **--------------------------------------------------------------
 *
 * 1. HelpGenerator.generateHtmlHelpFiles() in handleNewSimulation() 
 * ...
 *
 **-------------------------------------------------------------- 
 * Case D : with only 'new' switch (GUI mode is implied), it will load the Main Menu,
 **--------------------------------------------------------------
 * 
 * 1. main() 						-- on Main Thread
 * 2. Default Constructor 			-- on JavaFX Application Thread 
 * 3. init()						-- on JavaFX-Launcher Thread
 * 4. SimulationTask's run() 		-- on pool-2-thread-1
 * 5. prepare()						-- on pool-2-thread-1
 * 6. start() 						-- on JavaFX Application Thread 
 * 7. MainMenu's constructor 		-- on JavaFX Application Thread
 * 8. MainMenu's initAndShowGUI() 	-- on JavaFX Application Thread
 *
 * 
 * *-------------------------------------------------------------- 
 * Case D1 : if choosing the first option 'New Sim' in the Main Menu to start a new sim,  
 **--------------------------------------------------------------
 *
 * Step 1 to 8 : same as in Case D
 * 9. MainMenu's runOne() 		 	-- on JavaFX Application Thread
 * 10. handleNewSimulation()		-- on JavaFX Application Thread 
 * 11. ConfigEditorTask's run()		-- on pool-2-thread-1
 * 12. ScenarioConfigEditorFX's 
 * 		constructor 				-- on pool-2-thread-1
 * 13. ScenarioConfigEditorFX's
 *  	LoadSimulationTask's run()	-- on pool-2-thread-1
 * 14. Simulation's start()			-- on pool-2-thread-1
 * 
 * 
 **-------------------------------------------------------------- 
 * Case D2 : if choosing the second option 'Load Sim' in the Main Menu to load a saved sim,  
 **--------------------------------------------------------------
 * 
 * Step 1 to 8 : same as in Case D
 * 9. MainMenu's runTwo() 		 	-- on JavaFX Application Thread
 * 10. Simulation loadSimulation() 	-- on JavaFX Application Thread
 * 11. Simulation's start()			-- on pool-2-thread-1
 * 
  */
      

/**
 * MarsProjectFX is the main class for MSP. It creates JavaFX/8 application thread.
 */
public class MarsProjectFX extends Application  {

    /** initialized logger for this class. */
    private static Logger logger = Logger.getLogger(MarsProjectFX.class.getName());

    static String[] args;

    /** true if displaying graphic user interface. */
    private boolean headless = false, newSim = false, loadSim = false;

    /** true if help documents should be generated from config xml files. */
    private boolean generateHTML = false;

    private boolean isDone;

    private MainMenu mainMenu;

    private List<String> argList;

    //private ExecutorService worker;

    private MarsProjectFX marsProjectFX;

    private static Simulation sim = Simulation.instance();
    private static SimulationConfig simulationConfig = SimulationConfig.instance();
    //private static ExecutorService simExecutor;// = sim.getSimExecutor();
    /*
     * Default Constructor
     */
    public MarsProjectFX() {
	   	//logger.info("MarsProjectFX's constructor is on " + Thread.currentThread().getName());
    	marsProjectFX = this;
/*
		JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
        System.out.println(javaCompiler.toString());

        Set<SourceVersion> sourceVersion;
        sourceVersion = javaCompiler.getSourceVersions();

        for (SourceVersion version : sourceVersion) {
            System.out.print(version.name() + "\n");
        }

        System.out.print("availableProcessors = " + Runtime.getRuntime().availableProcessors() + "\n");
*/
    }

	/*
     * Initiates any tasks or methods on a JavaFX-Launcher Thread
     * @see javafx.application.Application#init()
     */
    @Override
    public void init() throws Exception {
	   	//logger.info("MarsProjectFX's init() is on " + Thread.currentThread().getName() );
	   	// INFO: MarsProjectFX's init() is on JavaFX-Launcher Thread
		setLogging();
		setDirectory();
        // general text antialiasing
        System.setProperty("swing.aatext", "true");    
        //System.setProperty("awt.useSystemAAFontSettings","lcd"); // for newer VMs
        //Properties props = System.getProperties();
        //props.setProperty("swing.jlf.contentPaneTransparent", "true");
    	logger.info("Starting " + Simulation.title);
    	
		argList = Arrays.asList(args);
        newSim = argList.contains("-new");      
        loadSim = argList.contains("-load");     
		generateHTML = argList.contains("-html");
		
		if (generateHTML)
			headless = true;
		else
			headless = argList.contains("-headless");

        //System.out.println("headless is " + headless);
        //System.out.println("newSim is " + newSim); 	
        //System.out.println("loadSim is " + loadSim); 	
        //System.out.println("generateHTML is " + generateHTML); 	  
        	
	   	sim.startSimExecutor();
	   	sim.getSimExecutor().execute(new SimulationTask());
    }

	public class SimulationTask implements Runnable {

		public void run() {
		   	//logger.info("MarsProjectFX's SimulationTask's run() is on " + Thread.currentThread().getName());
		   	//INFO: MarsProjectFX's SimulationTask's run() is on pool-2-thread-1 Thread
			prepare();
		}
    }

	public void prepare() {
	   	//logger.info("MarsProjectFX's prepare() is on " + Thread.currentThread().getName());
	   	//INFO: MarsProjectFX's prepare() is on pool-2-thread-1 Thread
	   	//new Simulation(); // NOTE: NOT supposed to start another instance of the singleton Simulation

	    if (!headless) { 
	    	// Using GUI mode
	    	logger.info("prepare() : Running MarsProjectFX in GUI mode");
	    	//System.setProperty("sun.java2d.opengl", "true"); // NOT WORKING IN MACCOSX
	    	//System.setProperty("sun.java2d.ddforcevram", "true");

	       	// Enable capability of loading of svg image using regular method
	    	//SvgImageLoaderFactory.install();

		} else { 
			// Using -headless arg (GUI-less)
			if (newSim) {
				logger.info("prepare() : Starting a new MarsProjectFX in headless mode");
				// Initialize the simulation.
			    initializeSimulation();
			    // Start the simulation.
			    startSimulation(true);
			}
			else if (loadSim) {
				logger.info("prepare() : Loading a saved sim and running MarsProjectFX in headless mode");
				// Initialize the simulation.
			    initializeSimulation();
			    // Start the simulation.
			    startSimulation(true);
			}

			// 2016-06-06 Generated html files for in-game help 
			else if (generateHTML) {
				logger.info("prepare() : Generating html help files in headless mode");

				try {					
		            SimulationConfig.loadConfig();
		    	    HelpGenerator.generateHtmlHelpFiles();

		        } catch (Exception e) {
		            e.printStackTrace();
		            exitWithError("Could not create a new simulation, startup cannot continue", e);
		        }
			}
			
		}

	    // this will generate html files for in-game help based on config xml files
	    // 2016-04-16 Relocated the following to handleNewSimulation() right before calling ScenarioConfigEditorFX.
	    //if (generateHTML) {
	    //	HelpGenerator.generateHtmlHelpFiles();
	    //

	}

	public void start(Stage primaryStage) {
	   	//logger.info("MarsProjectFX's start() is on " + Thread.currentThread().getName() );
	   	if (!headless) {
		   //	logger.info("start() : in GUI mode, loading the Main Menu");			
		    
	   		mainMenu = new MainMenu(this);
	   			   		
	   		if (newSim) {
	   			mainMenu.initAndShowGUI(primaryStage);
		   		mainMenu.setupMainSceneStage();
	   		}
	   		else {
		   		mainMenu.setupMainSceneStage();		   		
	   			mainMenu.loadSim();
	   		}
		    	    
		}
		else {
		   	logger.info("start() : in headless mode, not loading the Main Menu");			
		}
	}

	public List<String> getArgList() {
		return argList;
	}

    /**
     * Initialize the simulation.
     * @param args the command arguments.
     * @return true if new simulation (not loaded)
     */
	//2016-04-28 Modified to handle starting a new sim in headless mode
    boolean initializeSimulation() {//String[] args) {
		//logger.info("initializeSimulation() is on " + Thread.currentThread().getName() );
        boolean result = false;
        
        if (newSim) {

        	//SimulationConfig.instance();
        	SimulationConfig.loadConfig();

        	Simulation.createNewSimulation();
        	
            result = true;

        } else if (loadSim) {
            // If load argument, load simulation from file.
            try {
                handleLoadSimulation(argList);
            } catch (Exception e) {
                showError("Could not load the desired simulation, trying to create a new Simulation...", e);
                handleNewSimulation();
                result = true;
            }
            
        } else {
            try {
                handleLoadDefaultSimulation();
            } catch (Exception e) {
//                showError("Could not load the default simulation, trying to create a new Simulation...", e);
                handleNewSimulation();
                result = true;
            }
        }

        return result;
    }

    /**
     * Initialize the simulation.
     * @param args the command arguments.
     * @return true if new simulation (not loaded)
     
    boolean initializeNewSimulation() {
        boolean result = false;

        handleNewSimulation(); // if this fails we always exit, continuing is useless
        result = true;

        return result;
    }
*/
    
    /**
     * Exit the simulation with an error message.
     * @param message the error message.
     * @param e the thrown exception or null if none.
     */
    private void exitWithError(String message, Exception e) {
        showError(message, e);
        Platform.exit();
        System.exit(1);
    }

    /**
     * Show a modal error message dialog.
     * @param message the error message.
     * @param e the thrown exception or null if none.
     */
    private void showError(String message, Exception e) {
        if (e != null) {
            logger.log(Level.SEVERE, message, e);
        }
        else {
            logger.log(Level.SEVERE, message);
        }

        if (!headless) {
            JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Loads the simulation from the default save file.
     * @throws Exception if error loading the default saved simulation.
     */
    void handleLoadDefaultSimulation() throws Exception {
		//logger.info("MarsProjectFX's handleLoadDefaultSimulation() is on "+Thread.currentThread().getName());

    	try {
            // Load a the default simulation
            sim.loadSimulation(null);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not load default simulation", e);
            throw e;
        }
    }

    /**
     * Calls handleLoadSimulation(argList). Used by MainMenu to load th default save sim.
     * @throws Exception if error loading the default saved simulation.

    void handleLoadDefaultSavedSimulation() {
		//logger.info("MarsProjectFX's handleLoadDefaultSavedSimulation() is on "+Thread.currentThread().getName() );
    	try {
    		List<String> argList = new ArrayList<String>(1);
    		argList.add("-load");
			handleLoadSimulation(argList);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
*/
 
    /**
     * Loads the simulation from a save file.
     * @param argList the command argument list.
     * @throws Exception if error loading the saved simulation.
     */
    void handleLoadSimulation(List<String> argList) throws Exception {
		//logger.info("MarsProjectFX's handleLoadSimulation() is on "+Thread.currentThread().getName() );
    	// INFO: MarsProjectFX's handleLoadSimulation() is in JavaFX Application Thread Thread
        try {
            int index = argList.indexOf("-load");
            // Get the next argument as the filename.
            File loadFile = new File(argList.get(index + 1));
            if (loadFile.exists() && loadFile.canRead()) {
                sim.loadSimulation(loadFile);


            } else {
                exitWithError("Problem loading simulation. " + argList.get(index + 1) +
                        " not found.", null);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Problem loading existing simulation", e);
            throw e;
        }
    }

    /**
     * Create a new simulation instance.
     */
    void handleNewSimulation() {
		//logger.info("MarsProjectFX's handleNewSimulation() is on "+Thread.currentThread().getName() );
		// MarsProjectFX's handleNewSimulation() is in JavaFX Application Thread Thread
		//isDone = true;
        try {
            SimulationConfig.loadConfig();
            
           	sim.getSimExecutor().execute(new ConfigEditorTask());
          	// Warning: cannot load the editor in macosx if it was a JDialog

        } catch (Exception e) {
            e.printStackTrace();
            exitWithError("Could not create a new simulation, startup cannot continue", e);
        }
    }

    /**
     * Start the simulation instance.
     */
    public void startSimulation(boolean useDefaultName) {
		//logger.info("MarsProjectFX's startSimulation() is on "+Thread.currentThread().getName() );
        // Start the simulation.
        sim.start(useDefaultName);
    }

    public void setDirectory() {
        new File(System.getProperty("user.home"), Simulation.MARS_SIM_DIRECTORY + File.separator + "logs").mkdirs();
    }


    public void setLogging() {
    	//logger.info("setLogging() is on " + Thread.currentThread().getName() );
        try {
            LogManager.getLogManager().readConfiguration(MarsProjectFX.class.getResourceAsStream("/logging.properties"));
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not load logging properties", e);
            try {
                LogManager.getLogManager().readConfiguration();
            } catch (IOException e1) {
                logger.log(Level.WARNING, "Could read logging default config", e);
            }
        }
    }

	public class ConfigEditorTask implements Runnable {
	   	
		  public void run() {
			  //logger.info("MarsProjectFX's ConfigEditorTask's run() is on " + Thread.currentThread().getName() );
			  new ScenarioConfigEditorFX(marsProjectFX, mainMenu);
		  }
	}

/*
    @Override
    public void start(Stage primaryStage) throws Exception {
        if (appLaunch != null) {
            appLaunch.start(this, primaryStage);
        }
    }
*/

    @Override
    public void stop() throws Exception {
	   	//logger.info("MarsProjectFX's stop is on " + Thread.currentThread().getName() );
    }

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException{
    	//logger.info("MarsProjectFX's main() is on " + Thread.currentThread().getName() + " Thread" );
    	MarsProjectFX.args = args;
/*
        // 2015-10-13  Added command prompt console
        Console console = System.console();
        if(console == null && !GraphicsEnvironment.isHeadless()){
            String filename = MarsProjectFX.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(6);
            Runtime.getRuntime().exec(new String[]{"cmd","/c","start","cmd","/k","java -jar \"" + filename + "\""});
        }else{
        	//MarsProjectFX.main(new String[0]);
            System.out.println("Program has ended, please type 'exit' to close the console");
        }
*/
        launch(args);
    }

}