package org.mars_sim.msp.restws;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;

/**
 * This is a bootstrap class that uses classes from the main Mars-Sim project to bootstrap a Simulation instance. It uses the config classes already provided.
 */
public class MarsBootstrap {

	// Location of file holding simulation state
    private static final String SIM_FILE = "simulation.state";

    // Duration between saves in seconds
	private static final int SAVE_DURATION = 300; //60 * 5;
    
	/** initialized logger for this class. */
	private static Log log = LogFactory.getLog(MarsBootstrap.class);

	// Saves a simulation periodically
	private static final class Saver implements Runnable {

		private String savePath;
		private int saveDuration;
		private Simulation simulation;
		
		public Saver(String savePath, int saveDuration, Simulation simulation) {
			super();
			this.savePath = savePath;
			this.saveDuration = saveDuration;
			this.simulation = simulation;
		}
		
		@Override
		public void run() {
			// Need a way to stop saving thread
			while (true) {
				File saveFile = new File(savePath);
				
				// Wait 
				try {
					Thread.sleep(saveDuration * 1000);
				} catch (InterruptedException e1) {
					log.warn("Saving thread interrupted", e1);
				}
				
				try {
					log.info("Background save");
					simulation.saveSimulation(Simulation.SAVE_DEFAULT, saveFile);
				} catch (IOException e) {
					log.error("Problem saving simulation to " + savePath, e);
				}
			}
		}
	}
	
    /**
     * Loads the simulation from a save file.
     * @param argList the command argument list.
     * @throws Exception if error loading the saved simulation.
     */
    public Simulation loadSimulation(File loadFile) throws Exception {
    	log.info("Loading simulation from " + loadFile.getPath());
       	Simulation.instance().loadSimulation(loadFile);
        return Simulation.instance();
    }

    /**
     * Create a new simulation instance.
     * @return 
     */
    public Simulation loadNewSimulation() {
    	log.info("Creating new simulation");
    	
        SimulationConfig.loadConfig();
        Simulation.createNewSimulation(-1);     
        
        return Simulation.instance();
    }

    /**
     * OPrepares a simulation for the web service layer. This method decides whether to create a new Simulation or reload.
     * @return
     */
	public Simulation buildSimulation() {
		
		String createProp =  System.getProperty("createSim");
		boolean create = (createProp != null ? Boolean.getBoolean(createProp) : false);

		File loadFile = new File(SIM_FILE);
		
		Simulation created = null;
		
		//created = loadNewSimulation();
		
		
		if (create || !loadFile.exists()) {
			created = loadNewSimulation();
		}
		else {
			try {
				created = loadSimulation(loadFile);
			} catch (Exception e) {
				log.error("Problem loading simulation " + loadFile.getAbsolutePath(), e);
				return null;
			}
		}
	
		// Set up Thread for background save
		Thread saver = new Thread(new Saver(SIM_FILE, SAVE_DURATION, created), "SavingThread");
		saver.start();
		
		return created;
	}
}