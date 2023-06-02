package org.mars_sim.msp.core;

import java.io.File;
import java.io.IOException;

import org.mars_sim.msp.core.configuration.Scenario;
import org.mars_sim.msp.core.configuration.ScenarioConfig;
import org.mars_sim.msp.core.structure.SettlementBuilder;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MasterClock;

import junit.framework.TestCase;

/**
 * Unit test suite for the sving and loading a simulation
 */
public class TestSaving extends TestCase implements SimulationListener {

    private SimulationConfig simConfig;
    private String saveFeedback;
    private File saveFile = null;

    @Override
    public void setUp() throws Exception {
		// Create new simulation instance.
        simConfig = SimulationConfig.instance();
        simConfig.loadConfig();
    }

    public void testSaving() throws IOException {
        Simulation sim = Simulation.instance();
        sim.createNewSimulation(64); 


        // Build a realistic simulation with entities to save
        SettlementBuilder builder = new SettlementBuilder(sim, simConfig);
        ScenarioConfig config = new ScenarioConfig();
        Scenario bootstrap = config.getItem("Single Settlement");
        builder.createInitialSettlements(bootstrap);
        
        saveFile = File.createTempFile("save-test", ".sim");
        sim.requestSave(saveFile, this);

        // Simulate a clock pulse to trigger the save
        MasterClock mc = sim.getMasterClock();
        ClockPulse pulse = new ClockPulse(1, 0.01D, mc.getMarsClock(), mc, false, false);
        sim.clockPulse(pulse);

        // Check simulations saved and it contains data
        assertFalse("Simultion save pending", sim.isSavePending());
        assertEquals("Simulation save status", SimulationListener.SAVE_COMPLETED, saveFeedback);
        assertTrue("Simulation file exists", saveFile.isFile());
        assertTrue("Save file is not empty", saveFile.length() > 0);

    }

    @Override
    public void tearDown() {
        // Delete the saved file
        if ((saveFile != null) && saveFile.isFile()) {
           saveFile.delete();
        }
    }

    @Override
    public void eventPerformed(String action) {
        saveFeedback = action;
    }
}