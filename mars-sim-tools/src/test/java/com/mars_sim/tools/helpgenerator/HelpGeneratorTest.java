package com.mars_sim.tools.helpgenerator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.resource.ResourceUtil;

public class HelpGeneratorTest {

    private HelpGenerator createGenerator() {
        var config = SimulationConfig.instance();
        config.loadConfig();

        return new HelpGenerator(config, "html-help", "html");
    }

    private <T> Document createDoc(TypeGenerator<T> gen, T entity) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        gen.generateEntity(entity, output);
        output.close();

        // Check output
        return Jsoup.parse(output.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void testVehicleHelp() throws IOException {
        var context = createGenerator();
        var vehicleConfig = context.getConfig().getVehicleConfiguration();

        var spec = vehicleConfig.getVehicleSpec("Cargo Rover");
        var vg = new VehicleGenerator(context);
        var content = createDoc(vg, spec);

        var cargo = content.getElementById("cargo");
        assertNotNull("Cargo section", cargo);
        assertNotNull("Characteristics section", content.getElementById("characteristics"));
    }

    @Test
    public void testProcessHelp() throws IOException {
        var context = createGenerator();
        var manConfig = context.getConfig().getManufactureConfiguration();

        var spec = manConfig.getManufactureProcessList().get(0);
        var vg = new ProcessGenerator(context);
        var content = createDoc(vg, spec);

        assertNotNull("Input section", content.getElementById("inputs"));
        assertNotNull("Output section", content.getElementById("outputs"));
        assertNotNull("Characteristics section", content.getElementById("characteristics"));
    }

    @Test
    public void testFoodHelp() throws IOException {
        var context = createGenerator();
        var foodConfig = context.getConfig().getFoodProductionConfiguration();

        var spec = foodConfig.getFoodProductionProcessList().get(0);
        var vg = new FoodGenerator(context);
        var content = createDoc(vg, spec);

        assertNotNull("Input section", content.getElementById("inputs"));
        assertNotNull("Output section", content.getElementById("outputs"));
        assertNotNull("Characteristics section", content.getElementById("characteristics"));
    }

    
    @Test
    public void testResourceHelp() throws IOException {
        var context = createGenerator();
        
        // Has both inputs and outputs
        var spec = ResourceUtil.findAmountResource("Aluminum oxide");

        var vg = new ResourceGenerator(context);
        var content = createDoc(vg, spec);

        assertNotNull("Consumer section", content.getElementById("consumers"));
        assertNotNull("Creator section", content.getElementById("creators"));
        assertNotNull("Characteristics section", content.getElementById("characteristics"));
    }

    @Test public void testFullGeneration() throws IOException {
        var context = createGenerator();

        File output = Files.createTempDirectory("generator").toFile();
        try {
            context.generateAll(output);

            File[] created = output.listFiles();

            // Matches number of type generators plus 1 for index
            assertEquals("Top level content", 6, created.length);
            for(File f : created) {
                if (f.isFile()) {
                    // Must be index
                    assertEquals("Top Index file", "index.html", f.getName());
                    assertTrue("Index has content", f.length() > 10);
                }
                else {
                    // Directory
                    assertTrue("Type contents " + f.getName(), f.listFiles().length > 1);
                }
            }
        }
        finally {
            // Clean up
            FileUtils.deleteDirectory(output);
        }
    }
}
