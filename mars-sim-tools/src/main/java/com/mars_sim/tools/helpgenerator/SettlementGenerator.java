/*
 * Mars Simulation Project
 * SettlementGenerator.java
 * @date 2024-03-10
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.mars_sim.core.building.BuildingTemplate;
import com.mars_sim.core.resource.ItemType;
import com.mars_sim.core.structure.SettlementSupplies;
import com.mars_sim.core.structure.SettlementTemplate;
import com.mars_sim.tools.helpgenerator.HelpContext.ItemQuantity;

public class SettlementGenerator extends TypeGenerator<SettlementTemplate> {
    public static final String TYPE_NAME = "settlement";

    protected SettlementGenerator(HelpContext parent) {
        super(parent, TYPE_NAME, "Settlement Template",
                "Settlement templates that can be used in a Scenario",
                "settlement_name");
        
        // Group by sponsor
        setGrouperByKey("Sponsor", r-> new GroupKey(r.getSponsor().getName(),
                                        r.getSponsor().getDescription()));
    }

    /**
     * Get a list of all the predefined Settlement Templates configured.
     */
    protected List<SettlementTemplate> getEntities() {

        return getConfig().getSettlementTemplateConfiguration().getKnownItems()
                            .stream()
                            .filter(SettlementTemplate::isBundled)
		 					.sorted(Comparator.comparing(SettlementTemplate::getName))
							.toList();
    }

    
	/**
	 * Add properties for teh initial suppliers for this Settlement
	 * @param v Template being rendered.
     * @param scope Properties
	 * @throws IOException
	 */
    @Override
    protected void addEntityProperties(SettlementTemplate st, Map<String,Object> scope) {
        addSupplies(getParent(), st.getSupplies(), scope);
    }

    /**
     * Load a scope object with properties that are used with the settlement-supplies template.
     * @param helpContext 
     * @param v Supplies to render
     * @param scope The scope to hold properties
     */
    static void addSupplies(HelpContext hc, SettlementSupplies v, Map<String,Object> scope) {
        
        List<BuildingTemplate> buildings = new ArrayList<>(v.getBuildings());
        Collections.sort(buildings, (o1, o2) -> o1.getBuildingName().compareTo(o2.getBuildingName()));
        scope.put("buildings", buildings); 

        // Add the resources
        List<ItemQuantity> resources = new ArrayList<>();
        resources.addAll(toQuantityItems(hc, v.getBins(), ItemType.BIN));
        resources.addAll(v.getParts().entrySet().stream()
                            .map(e -> hc.createItemQuantity(e.getKey().getName(), ItemType.PART,
                                                                        e.getValue()))
                            .toList());
        resources.addAll(v.getResources().entrySet().stream()
                            .map(e -> hc.createItemQuantity(e.getKey().getName(), ItemType.AMOUNT_RESOURCE,
                                                                        e.getValue()))
                            .toList());
        List<ItemQuantity>  sorted = resources.stream()
                    .sorted((o1, o2) -> o1.name().compareTo(o2.name())).toList();
        scope.put("resources", sorted);

        scope.put("vehicles", toQuantityItems(hc, v.getVehicles(), ItemType.VEHICLE));
        scope.put("equipment", toQuantityItems(hc, v.getEquipment(), ItemType.EQUIPMENT));
    }

    @Override
    protected String getEntityName(SettlementTemplate v) {
        return v.getName();
    }
}
