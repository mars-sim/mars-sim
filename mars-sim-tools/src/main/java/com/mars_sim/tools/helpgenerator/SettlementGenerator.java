/*
 * Mars Simulation Project
 * SettlementGenerator.java
 * @date 2024-03-10
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import com.mars_sim.core.resource.ItemType;
import com.mars_sim.core.structure.SettlementSupplies;
import com.mars_sim.core.structure.SettlementTemplate;
import com.mars_sim.core.structure.building.BuildingTemplate;
import com.mars_sim.tools.helpgenerator.HelpGenerator.ItemQuantity;

public class SettlementGenerator extends TypeGenerator<SettlementTemplate> {
    public static final String TYPE_NAME = "settlement";

    protected SettlementGenerator(HelpGenerator parent) {
        super(parent, TYPE_NAME, "Settlement Templates",
                "Settlement templates that can be used in a Scenario");
        
        // Group by sponsor
        setGrouper("Sponsor", t -> t.getSponsor());
    }

    /**
     * Get a list of all the predefined Settlement Templates configured.
     */
    protected List<SettlementTemplate> getEntities() {

        return getParent().getConfig().getSettlementTemplateConfiguration().getKnownItems()
                            .stream()
                            .filter(SettlementTemplate::isBundled)
		 					.sorted(Comparator.comparing(SettlementTemplate::getName))
							.toList();
    }

    
	/**
	 * Generate the file for the Settlement template.
	 * @param v Template being rendered.
     * @param output Destination of content
	 * @throws IOException
	 */
	public void generateEntity(SettlementTemplate v, OutputStream output) throws IOException {
        var generator = getParent();

		// Individual  pages
	    var vScope = generator.createScopeMap("Settlement Template - " + v.getName());
		vScope.put(TYPE_NAME, v);
        addSettlementSupplies(vScope, v); // Uses the settlement supplies template

        // Generate the file
        generator.generateContent("settlement-detail", vScope, output);
	}
    
    /**
     * Take a Settlement Supplies instance and prepare the context to use the settlement-supplies template
     * @param vScope
     * @param v
     */
    private static void addSettlementSupplies(Map<String, Object> vScope, SettlementSupplies v) {
        List<BuildingTemplate> buildings = new ArrayList<>(v.getBuildings());
        Collections.sort(buildings, (o1, o2) -> o1.getBuildingName().compareTo(o2.getBuildingName()));
        vScope.put("buildings", buildings); 

        // Add the resources
        List<ItemQuantity> resources = new ArrayList<>();
        resources.addAll(toQuantityItems(v.getBins(), ItemType.BIN));
        resources.addAll(v.getParts().entrySet().stream()
                            .map(e -> HelpGenerator.createItemQuantity(e.getKey().getName(), ItemType.PART,
                                                                        e.getValue()))
                            .toList());
        resources.addAll(v.getResources().entrySet().stream()
                            .map(e -> HelpGenerator.createItemQuantity(e.getKey().getName(), ItemType.AMOUNT_RESOURCE,
                                                                        e.getValue()))
                            .toList());
        List<ItemQuantity>  sorted = resources.stream()
                    .sorted((o1, o2) -> o1.name().compareTo(o2.name())).toList();
        vScope.put("resources", sorted);

        vScope.put("vehicles", toQuantityItems(v.getVehicles(), ItemType.VEHICLE));
        vScope.put("equipment", toQuantityItems(v.getEquipment(), ItemType.EQUIPMENT));
    }

    @Override
    protected String getEntityName(SettlementTemplate v) {
        return v.getName();
    }
}
