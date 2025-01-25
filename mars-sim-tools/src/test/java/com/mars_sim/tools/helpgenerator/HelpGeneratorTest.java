package com.mars_sim.tools.helpgenerator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.configuration.ScenarioConfig;
import com.mars_sim.core.resource.ResourceUtil;

public class HelpGeneratorTest {

    private HelpContext createGenerator() {
        var config = SimulationConfig.instance();
        config.loadConfig();

        return new HelpContext(config, HelpContext.HTML_STYLE);
    }

    private <T> Document createDoc(TypeGenerator<T> gen, T entity) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        gen.generateEntity(entity, output);
        output.close();

        // Check output
        return Jsoup.parse(output.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void testManifestHelp() throws IOException {
        var context = createGenerator();

        // Find a resupplt manifest that has mixture of most things
        var config = context.getConfig().getSettlementTemplateConfiguration().getSupplyManifests()
                        .stream()
                        .filter(v -> v.getName().equals("Resupply for Phase 3"))
                        .findAny()
                        .orElseGet(null);
        
        assertNotNull("Resupply manifest found", config);

        var vg = new ManifestGenerator(context);
        var content = createDoc(vg, config);

        assertContent(content, "characteristics");
        assertContent(content, "vehicles");
        assertContent(content, "resources");
        assertContent(content, "equipment");

    }

    @Test
    public void testVehicleHelp() throws IOException {
        var context = createGenerator();
        var vehicleConfig = context.getConfig().getVehicleConfiguration();

        var spec = vehicleConfig.getVehicleSpec("Cargo Rover");
        var vg = new VehicleGenerator(context);
        var content = createDoc(vg, spec);

        assertContent(content, "characteristics");
        assertContent(content, "cargo");
    }

    @Test
    public void testCropHelp() throws IOException {
        var context = createGenerator();
        var manConfig = context.getConfig().getCropConfiguration();

        var spec = (new ArrayList<>(manConfig.getCropTypes())).get(0);
        var vg = new CropGenerator(context);
        var content = createDoc(vg, spec);

        assertContent(content, "characteristics");
    }


    @Test
    public void testComplaintHelp() throws IOException {
        var context = createGenerator();
        var manConfig = context.getConfig().getMedicalConfiguration();

        var spec = (new ArrayList<>(manConfig.getComplaintList())).get(0);
        var vg = new ComplaintGenerator(context);
        var content = createDoc(vg, spec);

        assertContent(content, "characteristics");
    }

    @Test
    public void testTreatmentHelp() throws IOException {
        var context = createGenerator();
        var manConfig = context.getConfig().getMedicalConfiguration();

        var spec = (new ArrayList<>(manConfig.getTreatmentsByLevel(20))).get(0);
        var vg = new TreatmentGenerator(context);
        var content = createDoc(vg, spec);

        assertContent(content, "characteristics");
    }


    @Test
    public void testProcessHelp() throws IOException {
        var context = createGenerator();
        var manConfig = context.getConfig().getManufactureConfiguration();

        var spec = manConfig.getManufactureProcessList().get(0);
        var vg = new ProcessGenerator(context);
        var content = createDoc(vg, spec);

        assertContent(content, "characteristics");
        assertContent(content, "inputs");
        assertContent(content, "outputs");
    }

    @Test
    public void testFoodHelp() throws IOException {
        var context = createGenerator();
        var foodConfig = context.getConfig().getFoodProductionConfiguration();

        var spec = foodConfig.getProcessList().get(0);
        var vg = new FoodGenerator(context);
        var content = createDoc(vg, spec);

        assertContent(content, "characteristics");
        assertContent(content, "inputs");
        assertContent(content, "outputs");
    }

    @Test
    public void testScenarioHelp() throws IOException {
        var context = createGenerator();
        
        // Has both inputs and outputs
        var spec = (new ScenarioConfig()).getItem("Default");

        var vg = new ScenarioGenerator(context);
        var content = createDoc(vg, spec);

        assertContent(content, "settlements");
        assertContent(content, "arriving");
    }

    
    @Test
    public void testResourceHelp() throws IOException {
        var context = createGenerator();
        
        // Has both inputs and outputs
        var spec = ResourceUtil.findAmountResource("Aluminum oxide");

        var vg = new ResourceGenerator(context);
        var content = createDoc(vg, spec);

        assertContent(content, "consumers");
        assertContent(content, "creators");
        assertContent(content, "characteristics");
    }

    @Test
    public void testSettlementHelp() throws IOException {
        var context = createGenerator();
        
        // Has both inputs and outputs
        var spec = context.getConfig().getSettlementTemplateConfiguration().getItem("Alpha Base");

        var vg = new SettlementGenerator(context);
        var content = createDoc(vg, spec);

        assertContent(content, "characteristics");
        assertContent(content, "equipment");
        assertContent(content, "resources");
        assertContent(content, "missions");
        assertContent(content, "buildings");
        assertContent(content, "vehicles");
    }

    /**
     * Search for a tag with an id. The tag should be present and have at least one child.
     * The tag should be a <div>
     * @param doc
     * @param id
     */
    private void assertContent(Document doc, String id) {
        var node = doc.getElementById(id);
        assertNotNull("'" + id + "' section", node);
        assertTrue("'" + id + "' has content", node.childNodeSize() > 0);
        assertEquals("'" + id + "' has a <div>", "div", node.tagName());
    }

    @Test
    public void testFullGeneration() throws IOException {
        var context = createGenerator();

        File output = Files.createTempDirectory("generator").toFile();
        try {
            context.generateAll(output);

            File[] created = output.listFiles();

            // Matches number of type generators plus 1 for index
            assertEquals("Top level content", HelpContext.GENERATORS.length + 1,
                                        created.length);
            boolean indexFound = false;
            for(File f : created) {
                if (f.isFile()) {
                    // Must be index
                    assertEquals("Top Index file", "index.html", f.getName());
                    assertTrue("Index has content", f.length() > 10);
                    indexFound = true;
                }
                else {
                    // Directory
                    assertTrue("Type contents " + f.getName(), f.listFiles().length > 1);
                }
            }
            assertTrue("Found index.html file", indexFound);
        }
        finally {
            // Clean up
            FileUtils.deleteDirectory(output);
        }
    }
}
