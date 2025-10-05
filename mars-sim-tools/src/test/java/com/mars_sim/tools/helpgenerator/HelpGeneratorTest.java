package com.mars_sim.tools.helpgenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.configuration.ScenarioConfig;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.robot.RobotType;

public class HelpGeneratorTest {

	private static final String ALPHA_BASE_1 = "Alpha Base 1";
	
    private SimulationConfig simconfig;
    
    private HelpContext createGenerator() {
        simconfig = SimulationConfig.loadConfig();

        return new HelpContext(simconfig, HelpContext.HTML_STYLE);
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

        // Find a resupply manifest that has mixture of most things
        var config = context.getConfig().getSettlementTemplateConfiguration().getSupplyManifests()
                        .stream()
                        .filter(v -> v.getName().equals("Resupply for Phase 3"))
                        .findAny()
                        .orElseGet(null);
        
        assertNotNull(config, "Resupply manifest found");

        var vg = new ManifestGenerator(context);
        var content = createDoc(vg, config);

        assertTitledTable(content, "characteristics", "Characteristics", false);
        assertTitledTable(content, "vehicles", "Vehicles", true);
        assertTitledTable(content, "resources", "Resources", true);
        assertTitledTable(content, "equipment", "Equipment", true);

    }

    @Test
    public void testRobotHelp() throws IOException {
        var context = createGenerator();
        var vehicleConfig = context.getConfig().getRobotConfiguration();

        var spec = vehicleConfig.getRobotSpec(RobotType.REPAIRBOT, "Standard");
        var vg = new RobotGenerator(context);
        var content = createDoc(vg, spec);

        assertTitledTable(content, "characteristics", "Characteristics", false);
        assertTitledTable(content, "skills", "Skills", true);
        assertTitledTable(content, "attributes", "Attributes", true);
    }

    @Test
    public void testVehicleHelp() throws IOException {
        var context = createGenerator();
        var vehicleConfig = context.getConfig().getVehicleConfiguration();

        var spec = vehicleConfig.getVehicleSpec("Cargo Rover");
        var vg = new VehicleGenerator(context);
        var content = createDoc(vg, spec);

        assertTitledTable(content, "characteristics", "Characteristics", false);
        assertDIV(content, "cargo");
    }

    @Test
    public void testResProcessHelp() throws IOException {
        var context = createGenerator();
        var config = context.getConfig().getResourceProcessConfiguration();

        var spec = config.getProcessSpec("Atmospheric Processing");
        var vg = new ResourceProcessGenerator(context);
        var content = createDoc(vg, spec);

        assertTitledTable(content, "characteristics", "Characteristics", false);
        assertTitledTable(content, "inputs", "Inputs", true);
        assertTitledTable(content, "outputs", "Outputs", true);
    }


    @Test
    public void testCropHelp() throws IOException {
        var context = createGenerator();
        var manConfig = context.getConfig().getCropConfiguration();

        var spec = (new ArrayList<>(manConfig.getCropTypes())).get(0);
        var vg = new CropGenerator(context);
        var content = createDoc(vg, spec);

        assertTitledTable(content, "characteristics", "Characteristics", false);
    }

    @Test
    public void testMealHelp() throws IOException {
        var context = createGenerator();
        var config = context.getConfig().getMealConfiguration();

        var spec = config.getDishList().get(0);
        var vg = new MealGenerator(context);
        var content = createDoc(vg, spec);

        assertTitledTable(content, "characteristics", "Characteristics", false);
        assertTitledTable(content, "ingredients", "Ingredients", true);
    }

    @Test
    public void testAuthorityHelp() throws IOException {
        var context = createGenerator();
        var config = context.getConfig().getReportingAuthorityFactory();

        var name = config.getItemNames().get(0);
        var spec = config.getItem(name);
        var vg = new AuthorityGenerator(context);
        var content = createDoc(vg, spec);

        assertDIV(content, "agenda");
        assertDIV(content, "countries");
    }

    @Test
    public void testMalfunctionHelp() throws IOException {
        var context = createGenerator();
        var config = context.getConfig().getMalfunctionConfiguration();

        var spec = config.getMalfunctionList().get(0);
        var vg = new MalfunctionGenerator(context);
        var content = createDoc(vg, spec);

        assertTitledTable(content, "characteristics", "Characteristics", false);
        assertDIV(content, "systems");

    }

    @Test
    public void testComplaintHelp() throws IOException {
        var context = createGenerator();
        var manConfig = context.getConfig().getMedicalConfiguration();

        var spec = (new ArrayList<>(manConfig.getComplaintList())).get(0);
        var vg = new ComplaintGenerator(context);
        var content = createDoc(vg, spec);

        assertTitledTable(content, "characteristics", "Characteristics", false);
    }

    @Test
    public void testTreatmentHelp() throws IOException {
        var context = createGenerator();
        var manConfig = context.getConfig().getMedicalConfiguration();

        var spec = (new ArrayList<>(manConfig.getTreatmentsByLevel(20))).get(0);
        var vg = new TreatmentGenerator(context);
        var content = createDoc(vg, spec);

        assertTitledTable(content, "characteristics", "Characteristics", false);
    }


    @Test
    public void testProcessHelp() throws IOException {
        var context = createGenerator();
        var manConfig = context.getConfig().getManufactureConfiguration();

        var spec = manConfig.getManufactureProcessList().get(0);
        var vg = new ProcessGenerator(context);
        var content = createDoc(vg, spec);

        assertTitledTable(content, "characteristics", "Characteristics", false);
        assertTitledTable(content, "inputs", "Inputs", false);
        assertTitledTable(content, "outputs", "Products", false);
    }

    @Test
    public void testFoodHelp() throws IOException {
        var context = createGenerator();
        var foodConfig = context.getConfig().getFoodProductionConfiguration();

        var spec = foodConfig.getProcessList().get(0);
        var vg = new FoodGenerator(context);
        var content = createDoc(vg, spec);

        assertTitledTable(content, "characteristics", "Characteristics", false);
        assertTitledTable(content, "inputs", "Ingredients", false);
        assertTitledTable(content, "outputs", "Outcomes", false);
    }

    @Test
    public void testScenarioHelp() throws IOException {
        var context = createGenerator();
        
        // Has both inputs and outputs
        var spec = (new ScenarioConfig(simconfig)).getItem("Default");

        var vg = new ScenarioGenerator(context);
        var content = createDoc(vg, spec);

        assertTitledTable(content, "settlements", "Settlements", true);
        assertTitledTable(content, "arriving", "New Settlements", true);
    }

    
    @Test
    public void testResourceHelp() throws IOException {
        var context = createGenerator();
        
        // Has both inputs and outputs
        var spec = ResourceUtil.findAmountResource("Aluminum oxide");

        var vg = new ResourceGenerator(context);
        var content = createDoc(vg, spec);

        assertDIV(content, "consumers");
        assertDIV(content, "creators");
        assertTitledTable(content, "characteristics", "Characteristics", false);
    }

    @Test
    public void testSettlementHelp() throws IOException {
        var context = createGenerator();
        
        // Has both inputs and outputs
        var spec = context.getConfig().getSettlementTemplateConfiguration().getItem(ALPHA_BASE_1);

        var vg = new SettlementGenerator(context);
        var content = createDoc(vg, spec);

        assertTitledTable(content, "characteristics", "Characteristics", false);
        assertDIV(content, "equipment");
        assertDIV(content, "resources");
        assertDIV(content, "missions");
        assertDIV(content, "buildings");
        assertDIV(content, "vehicles");
    }

    /**
     * Search for a tag with an id. The tag should be present and have at least one child.
     * The tag should be a <div>
     * @param doc
     * @param id
     */
    private Element assertDIV(Document doc, String id) {
        var node = doc.getElementById(id);
        assertNotNull(node, "'" + id + "' section");
        assertTrue(node.childNodeSize() > 0, "'" + id + "' has content");
        assertEquals("div", node.tagName(), "'" + id + "' has a <div>");
        return node;
    }

    /**
     * Searches for a tag with an id. The tag should be present and have a heading and table.
     * The tag should be a <div>. The table should be fully populated with a heading.
     * 
     * @param doc
     * @param id
     */
    private void assertTitledTable(Document doc, String id, String title, boolean hasHeading) {
        var div = assertDIV(doc, id);
        var elems = div.children();
        assertEquals(2, elems.size(), "SubElement count");

        // First should be a title
        var titleElem = elems.get(0);
        assertEquals('h', titleElem.tagName().charAt(0), "Title heading");
        assertEquals(title, titleElem.text(), "Heading Text");

        // Next is table
        var tableElem = elems.get(1);
        assertEquals("table", tableElem.tagName(), "<table>");
        var tableChildren = tableElem.children();

        // Check heading
        int bodyIdx = 0;
        if (hasHeading) {
            // Check THead
            checkTableRow("th", tableChildren.get(0).firstElementChild(), 0);
            bodyIdx++;
        }

        // Parse TBody element
        int rowIdx = 0;
        for(var row : tableChildren.get(bodyIdx).children()) {
            checkTableRow("td", row, rowIdx++);
        }
    }

    /**
     * Check a table row is fully populated
     * @param cellTag
     * @param element
     */
    private void checkTableRow(String cellTag, Element rowElement, int rowId) {
        int i = 0;
        for(var cell : rowElement.children()) {
            var id = rowId + "," + i++;
            assertEquals(cellTag, cell.tagName(), "Cell tag #" + id);
            if (!cell.className().equals("optional"))
                assertFalse(cell.text().isEmpty(), "Cell contents #" + id);
        }
    }

    @Test
    public void testFullGeneration() throws IOException {
        var context = createGenerator();

        File output = Files.createTempDirectory("generator").toFile();
        try {
            context.generateAll(output);

            File[] created = output.listFiles();

            // Matches number of type generators plus 1 for index
            assertEquals(HelpContext.GENERATORS.length + 1, created.length,
                                        "Top level content");
            boolean indexFound = false;
            for(File f : created) {
                if (f.isFile()) {
                    // Must be index
                    assertEquals("index.html", f.getName(), "Top Index file");
                    assertTrue(f.length() > 10, "Index has content");
                    indexFound = true;
                }
                else {
                    // Directory
                    assertTrue(f.listFiles().length > 1, "Type contents " + f.getName());
                }
            }
            assertTrue(indexFound, "Found index.html file");
        }
        finally {
            // Clean up
            FileUtils.deleteDirectory(output);
        }
    }
}
