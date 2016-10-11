/**
 * Mars Simulation Project
 * MarsProjectFX.java
 * @version 3.1.0 2016-09-30
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
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.javafx.svg.SvgImageLoaderFactory;

/**--------------------------------------------------------------
 * Case A : with '-headless' and '-new' switch, it will load the following :
 **--------------------------------------------------------------
 *
 * 1. main() 								-- on Main Thread
 * 2. Default Constructor 					-- on JavaFX Application Thread 
 * 3. init()								-- on JavaFX-Launcher Thread
 * 	- new SimulationTask()					-- on pool-2-thread-1
 * 	- prepare()								-- on pool-2-thread-1
 * 	- SimulationConfig.loadConfig()			-- on pool-2-thread-1
 *  - Simulation.createNewSimulation()		-- on pool-2-thread-1
 *  - startSimulation(true)					-- on pool-2-thread-1
 * 4. Simulation's start() 					-- on JavaFX Application Thread 
 * 
 * Note 1 : clockScheduler is initialized on Simulation's start()
 * Note 2 : autosave will save 
 * 
 **-------------------------------------------------------------- 
 * Case B : with '-headless' and '-load' switch, it will load the following : 
 **--------------------------------------------------------------
 *
 * 1. main() 								-- on Main Thread
 * 2. Default Constructor 					-- on JavaFX Application Thread 
 * 3. init()								-- on JavaFX-Launcher Thread
 * 	- new SimulationTask()					-- on pool-2-thread-1
 * 	- prepare()								-- on pool-2-thread-1
 * 	- SimulationConfig.loadConfig()			-- on pool-2-thread-1
 *  - Simulation.createNewSimulation()		-- on pool-2-thread-1
 *  
 *  Two Scenarios: 
 *  
 *  (a). if user provides a particular filename for the saved sim,
 *  - handleLoadSimulation()				-- on pool-2-thread-1
 *  
 *  (b). if user DO NOT provide the filename, load 'default.sim',
 * 	- handleLoadSimulation()				-- on pool-2-thread-1
 * 
 * 
 *  - startSimulation(true)					-- on pool-2-thread-1
 *  
 * 4. start() 								-- on JavaFX Application Thread 
 * 
 * 
 * 
 * 6. start() 						-- on JavaFX Application Thread 
 * 7. initializeSimulation() 		-- on pool-2-thread-1
 * 8a. handleLoadDefaultSimulation() -- on pool-2-thread-1  
 * OR if user provides the filename of the saved sim 
 * 8b. handleLoadSimulation()		-- on pool-2-thread-1
 * 9. startSimulation() 			-- on pool-2-thread-1
 * 10. Simulation's start()			-- on pool-2-thread-1
 *
 **-------------------------------------------------------------- 
 * Case C : with '-html' switch ('-headless' is optional in this case), it will load the following : 
 **--------------------------------------------------------------
 *
 * 1. main() 						-- on Main Thread
 * 2. Default Constructor 			-- on JavaFX Application Thread 
 * 3. init()						-- on JavaFX-Launcher Thread
 * 	- new SimulationTask()			-- on pool-2-thread-1
 * 	- prepare()						-- on pool-2-thread-1
 * 	- SimulationConfig.loadConfig()	-- on pool-2-thread-1
 * 4. start() 						-- on JavaFX Application Thread 
 * 
 * 
 * 6. SimulationConfig.loadConfig() -- on pool-2-thread-1
 * 7. HelpGenerator.generateHtmlHelpFiles() in handleNewSimulation() 
 * 
 *
 **-------------------------------------------------------------- 
 * Case D_ (D1 or D2) : with only '-new' switch (GUI mode is implied), it will load the Main Menu,
 **--------------------------------------------------------------
 * 
 * 1. main() 						-- on Main Thread
 * 2. Default Constructor 			-- on JavaFX Application Thread 
 * 3. init()						-- on JavaFX-Launcher Thread
 * 	- new SimulationTask()			-- on pool-2-thread-1
 * 	- prepare()						-- on pool-2-thread-1
 * 	- SimulationConfig.loadConfig()	-- on pool-2-thread-1
 * 4. start() 						-- on JavaFX Application Thread 
 * 	- new MainMenu()				-- on JavaFX Application Thread
 * 	- MainMenu's initMainMenu() 				-- on JavaFX Application Thread
 * 	- MainMenu's setupMainSceneStage()			-- on JavaFX Application Thread
 * 
 * 
 * 
 **-------------------------------------------------------------- 
 * Case D1 : if choosing the first option 'New Sim' in the Main Menu to start a new sim,  
 **--------------------------------------------------------------
 *
 * Step 1 to 4 : same as in Case D_
 * 
 * Upon choosing the 'New Sim' option in the Main Menu,
 * 
 * 5. MainMenu's runOne() 		 				-- on JavaFX Application Thread
 * 6. new MainScene()							-- on JavaFX Application Thread
 * 7. MarsProjectFX's handleNewSimulation()		-- on JavaFX Application Thread 
 * 	- ConfigEditorTask's run()					-- on pool-2-thread-1
 * 8. new ScenarioConfigEditorFX()				-- on pool-2-thread-1
 * 	- LoadSimulationTask's run()				-- on pool-2-thread-1
 *  -- Simulation's createNewSimulation()		-- on pool-2-thread-1
 * 	-- Simulation's start()						-- on pool-2-thread-1
 * 9. MainMenu's finalizeMainScene()			-- on JavaFX Application Thread
 * 
 * 
 **-------------------------------------------------------------- 
 * Case D2 : if choosing the second option 'Load Sim' in the Main Menu to load a saved sim,  
 **--------------------------------------------------------------
 * 
 * Step 1 to 4 : same as in Case D_

 * Upon choosing the 'Load Sim' option in the Main Menu, 
 * 
 * 5. MainMenu's runTwo() 		 				-- on JavaFX Application Thread
 * 6. loadSim(false)
 *  - new MainScene()							-- on JavaFX Application Thread
 * 7. In MainMenu's loadSim() 					-- on JavaFX Application Thread
 * 
 *  - Wait for user to select a saved sim to load in loadSim()...
 *  
 *  - Upon choosing a particular saved sim file,
 *  
 * 8. Main Menu's LoadSimulationTask's run()	-- on pool-2-thread-1
 *  - Simulation's createNewSimulation()		-- on pool-2-thread-1
 *  - Simulation's loadSimulation()				-- on pool-2-thread-1
 *  - Simulation's start(autosaveTimer)			-- on pool-2-thread-1     
 *  - finalizeMainScene()						-- on JavaFX Application Thread
 * 
 *-------------------------------------------------------------- 
 * Case E : with '-load' switch (GUI mode is implied), it will load the following : 
 **--------------------------------------------------------------
 * 
 * 1. main() 									-- on Main Thread
 * 2. Default Constructor 						-- on JavaFX Application Thread 
 * 3. init()									-- on JavaFX-Launcher Thread
 * 	- new SimulationTask()						-- on pool-2-thread-1
 * 	- prepare()									-- on pool-2-thread-1
 * 	- SimulationConfig.loadConfig()				-- on pool-2-thread-1
 * 	- Simulation.createNewSimulation()			-- on pool-2-thread-1
 * 4. start()
 * 	- MainMenu's setupMainSceneStage()			-- on JavaFX Application Thread
 * 	- MainMenu's loadSim() 						-- on JavaFX Application Thread
 * 5. In MainMenu's loadSim() 					-- on JavaFX Application Thread
 * 
 *  - Wait for user to select a saved sim to load in loadSim()...
 *  
 *  - Upon choosing a particular saved sim file,
 *  
 * 6. Main Menu's LoadSimulationTask's run()	-- on pool-2-thread-1
 *  - Simulation's loadSimulation()				-- on pool-2-thread-1
 *  - Simulation's start(autosaveTimer)			-- on pool-2-thread-1     
 *  - finalizeMainScene()						-- on JavaFX Application Thread
 *  
 *  
*/


/**
 * MarsProjectFX is the main class for MSP. It creates JavaFX/8 application thread.
 */
public class MarsProjectFX extends Application  {

    /** initialized logger for this class. */
    private static Logger logger = Logger.getLogger(MarsProjectFX.class.getName());

	public static final String OS = System.getProperty("os.name").toLowerCase(); // e.g. 'linux', 'mac os x'
	
    static String[] args;

    /** true if displaying graphic user interface. */
    private boolean headless = false, newSim = false, loadSim = false, savedSim = false;

    /** true if help documents should be generated from config xml files. */
    private boolean generateHTML = false;

    private boolean isDone;

    private String loadFileString;
    
    private MainMenu mainMenu;

    private List<String> argList;

    //private ExecutorService worker;

    private MarsProjectFX marsProjectFX;

    private static Simulation sim = Simulation.instance();
    //private static SimulationConfig simulationConfig = SimulationConfig.instance();
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
    	logger.info(Simulation.title);
    	
		argList = Arrays.asList(args);
        newSim = argList.contains("-new");      
        loadSim = argList.contains("-load");     
		generateHTML = argList.contains("-html");
		//savedSim = argList.contains(".sim");
		
		if (generateHTML)
			headless = true;
		else
			headless = argList.contains("-headless");

    	int size = argList.size();   	
    	boolean flag = true;
		for (int i= 0; i<size; i++) {
			if (argList.get(i).contains(".sim")) {
				if (flag) {
					loadFileString = argList.get(i);
					savedSim = true;
					flag = false;
				}
				else {
					logger.info("Cannot load more than one saved sim. ");
			        Platform.exit();
			        System.exit(1);
				}
			}
		}
  	
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
        SimulationConfig.loadConfig();
        
	    if (!headless) { 
	    	// Using GUI mode
	    	//logger.info("Running " + OS + " in GUI mode");
	    	//System.setProperty("sun.java2d.opengl", "true"); // NOT WORKING IN MACCOSX
	    	//System.setProperty("sun.java2d.ddforcevram", "true");
	       	// Enable capability of loading of svg image using regular method
	    	//SvgImageLoaderFactory.install();
		   	if (newSim) {
		   		// CASE D1 and D2 //
		   		// Note 1 : should NOT run createNewSimulation() until after clicking "Start" in Config Editor
	   		}
	   		else if (loadSim) {
		   		// CASE E //
	   		}
		   	
		} else { 
			// Using -headless (GUI-less mode)
			if (newSim) {
		   		// CASE A //
				logger.info("Starting a new sim in headless mode in " + OS);
				// Initialize the simulation.
	        	Simulation.createNewSimulation(); 
			    // Start the simulation.
			    startSimulation(true);
			}
			else if (loadSim) {
		   		// CASE B //
				// Initialize the simulation.
	        	Simulation.createNewSimulation(); 
	        	
	        	if (savedSim) {
	        		
	                File loadFile = new File(loadFileString);
		                
		            try {     	
		            	// try to see if user enter his own saved sim after the "load" argument
		                handleLoadSimulation(loadFile); 

		            } catch (Exception e) {
		                e.printStackTrace();
		                showError("Could not load the user's saved sim.", e);
/*		                
		                try {
		                	// try loading default.sim instead
		                	handleLoadDefaultSimulation();

			            } catch (Exception e2) {
			                showError("Could not load the default saved sim. Starting a new sim now. ", e2);		                
			                handleNewSimulation();
			            }
*/			               
		            }
	        	}
	        	
	        	else {
	        		// if user wants to load the default saved sim
	        		try {
	                	// try loading default.sim instead
	                	handleLoadDefaultSimulation();

		            } catch (Exception e2) {
		                e2.printStackTrace();
		            	exitWithError("Could not load the default saved sim.", e2);	            	
		                //showError("Could not load the default saved sim. Starting a new sim now. ", e2);           
		                //handleNewSimulation();
		            }     		
	        	}
			}
			// 2016-06-06 Generated html files for in-game help 
			else if (generateHTML) {
		   		// CASE C //
				logger.info("Generating help files in headless mode in " + OS);

				try {					
		            SimulationConfig.loadConfig();
		    	    // this will generate html files for in-game help based on config xml files
		    	    // 2016-04-16 Relocated the following to handleNewSimulation() right before calling ScenarioConfigEditorFX.
		    	    HelpGenerator.generateHtmlHelpFiles();

		        } catch (Exception e) {
		            e.printStackTrace();
		            exitWithError("Could not generate help files ", e);
		        }
			}			
		}
	}

	public void start(Stage primaryStage) {
	   	//logger.info("MarsProjectFX's start() is on " + Thread.currentThread().getName());
	   	if (!headless) {
		   //logger.info("start() : in GUI mode, loading the Main Menu");			
		    
	   		mainMenu = new MainMenu();//this);
	   			   		
	   		if (newSim) {
		   		// CASE D1 and D2//
	   	    	logger.info("Starting a new sim in GUI mode in " + OS);
	   			mainMenu.initMainMenu(primaryStage);
		   		mainMenu.setupMainSceneStage();
		   		
		   		// Now in the Main Menu, wait for user to pick either options 
		   		// 1. 'New Sim' - call runOne(), go to ScenarioConfigEditorFX 
		   		// 2. 'Load Sim' -call runTwo(), need to call sim.runStartTask(false);
	   		}
	   		else if (loadSim) {
		   		// CASE E //

		   		mainMenu.setupMainSceneStage();		   		
		   		
	        	if (savedSim) {
	        		
					logger.info("Loading user's saved sim in GUI mode in " + OS);
					
	        		File loadFile = new File(loadFileString);
	                
		            try {     	
		            	// load loadFile directly without opening the FileChooser
		            	mainMenu.loadSim(loadFile);
		                
		            } catch (Exception e) {
		                e.printStackTrace();
		                exitWithError("Could not load the user's saved sim. ", e);
/*		                
		                try {
		                	// load FileChooser instead
				   			mainMenu.loadSim(null);			   			
				   			// Then wait for user to select a saved sim to load in loadSim();
				   			
			            } catch (Exception e2) {
			            	exitWithError("Could not load the default saved sim. ", e2);                		            	
			            }   
*/			            
		            }
	        	} 
	        	
	        	else { 
	        		// if user wants to load the default saved sim
					logger.info("Loading a saved sim with FileChooser in GUI mode in " + OS);

	        		try {
	                	// load FileChooser instead
			   			mainMenu.loadSim(null);			   			
			   			// Then wait for user to select a saved sim to load in loadSim();
			   			
		            } catch (Exception e2) {
		                //e2.printStackTrace();
		            	exitWithError("Could not load the default saved sim. ", e2);                
		            	
		            }   
	        	}
	   		}		    	    
		}
	   	
		else {
		   	logger.info("loading default.sim in headless mode and skip loading the Main Menu");	
		   	if (newSim) {
		   	// CASE A //
	   		}
	   		else if (loadSim) {
		   		// CASE B //
	   		}
			else if (generateHTML) {
		   		// CASE C //
			}
		}
	}

	public List<String> getArgList() {
		return argList;
	}

    /**
     * Loads the simulation from the default save file.
     * @throws Exception if error loading the default saved simulation.
     */
    private void handleLoadDefaultSimulation() {
		//logger.info("MarsProjectFX's handleLoadDefaultSimulation() is on "+Thread.currentThread().getName());
		logger.info("Loading the default saved sim in headless mode in " + OS);	

    	try {	
            // Load the default saved file "default.sim"
            sim.loadSimulation(null);

        } catch (Exception e) {
            e.printStackTrace();
            exitWithError("Could not load default simulation", e);
        }
    	
	    // Start the simulation.
	    startSimulation(true);
    }

 
    /**
     * Loads the simulation from a save file.
     * @param argList the command argument list.
     * @throws Exception if error loading the saved simulation.
     */
    void handleLoadSimulation(File loadFile) throws Exception {
		//logger.info("MarsProjectFX's handleLoadSimulation() is on "+Thread.currentThread().getName() );
    	// INFO: MarsProjectFX's handleLoadSimulation() is in JavaFX Application Thread Thread
		logger.info("Loading user's saved sim in headless mode in " + OS);	
    	try {

            if (loadFile.exists() && loadFile.canRead()) {

                sim.loadSimulation(loadFile);
			    // Start the simulation.
			    startSimulation(true);

            } else {         	
                exitWithError("Could not load the simulation. The sim file " + loadFile +
                        " could not be read or found.", null);
            }

        } catch (Exception e) {
            e.printStackTrace();
            exitWithError("Error : problem loading the simulation ", e);
            //logger.log(Level.SEVERE, "Problem loading existing simulation", e);
        }
    }

    /**
     * Create a new simulation instance.
     */
    void handleNewSimulation() {
		//logger.info("MarsProjectFX's handleNewSimulation() is on "+Thread.currentThread().getName() );
		// MarsProjectFX's handleNewSimulation() is in JavaFX Application Thread Thread
		//isDone = true;
		logger.info("Creating a new sim in " + OS);	
        try {
            //SimulationConfig.loadConfig(); // located to prepare()
           	sim.getSimExecutor().execute(new ConfigEditorTask());
           	
        } catch (Exception e) {
            e.printStackTrace();
            exitWithError("Error : could not create a new simulation ", e);
        }
    }

	public class ConfigEditorTask implements Runnable { 	
		  public void run() {
			  //logger.info("MarsProjectFX's ConfigEditorTask's run() is on " + Thread.currentThread().getName() );
			  new ScenarioConfigEditorFX(mainMenu); //marsProjectFX, 
		  }
	}

    /**
     * Start the simulation instance.
     * @param autosaveDefaultName use the default name for autosave
     */
    public void startSimulation(boolean autosaveDefaultName) {
		//logger.info("MarsProjectFX's startSimulation() is on "+Thread.currentThread().getName() );
        // Start the simulation.
        sim.start(autosaveDefaultName);
    }

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
        	if (!OS.contains("mac"))
        		JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
        	// Warning: cannot load the editor in macosx if it was a JDialog
        }
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