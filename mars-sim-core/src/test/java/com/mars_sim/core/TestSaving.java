package com.mars_sim.core;

import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.mars_sim.core.configuration.Scenario;
import com.mars_sim.core.configuration.ScenarioConfig;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.health.Complaint;
import com.mars_sim.core.person.health.ComplaintType;
import com.mars_sim.core.person.health.MedicalManager;
import com.mars_sim.core.structure.SettlementBuilder;

import junit.framework.TestCase;

/**
 * Unit test suite for the saving and loading a simulation
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
        
        // Find a person and add a medical complaint
        Complaint complaint = sim.getMedicalManager().getComplaintByName(ComplaintType.APPENDICITIS);
        Person p = (new ArrayList<>(sim.getUnitManager().getPeople())).get(0);
        p.getPhysicalCondition().addMedicalComplaint(complaint);

        saveFile = File.createTempFile("save-test", ".sim");
        sim.saveSimulation(Simulation.SaveType.SAVE_AS, saveFile, this);


        // Check simulations saved and it contains data
        assertEquals("Simulation save status", SimulationListener.SAVE_COMPLETED, saveFeedback);
        assertTrue("Simulation file exists", saveFile.isFile());
        assertTrue("Save file is not empty", saveFile.length() > 0);

        // Reload it
        MedicalManager origMgr = sim.getMedicalManager();
        sim.loadSimulation(saveFile);
        assertNotEquals("Changed Medical Manager", origMgr, sim.getMedicalManager());


        Person laterP = sim.getUnitManager().getPersonByID(p.getIdentifier());
        assertEquals("Has complaint", complaint, laterP.getPhysicalCondition().getMostSerious().getComplaint());
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