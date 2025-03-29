/*
 * Mars Simulation Project
 * VehicleGenerator.java
 * @date 2024-02-23
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.util.List;
import java.util.Map;

import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.vehicle.VehicleSpec;
import com.mars_sim.tools.helpgenerator.HelpContext.ValuePair;

/**
 * Help generator for the VehicleSpec configuration item.
 */
public class VehicleGenerator extends TypeGenerator<VehicleSpec> {

    public static final String TYPE_NAME = "vehicle";

    protected VehicleGenerator(HelpContext parent) {
        super(parent, TYPE_NAME, "Vehicle Spec",
                "Types of Vehicles Featured for Mars Surface Operations", "vehicles");
    }
    
    /**
     * Get a list of all the vehicle specifications configured.
     */
    protected List<VehicleSpec> getEntities() {
        return getConfig().getVehicleConfiguration().getVehicleSpecs().stream()
		 							.sorted((o1, o2)->o1.getName().compareTo(o2.getName()))
									 .toList();
    }

	/**
	 * Add properties for cargo capacity of Vehicle specifications.
	 * @param v Spec being rendered.
     * @param scope Properties for template
	 */
    @Override
    protected void addEntityProperties(VehicleSpec v, Map<String,Object> scope) {
        // Convert capacity to a list that contains the resource name
        var cargos = v.getCargoCapacityMap().entrySet().stream()
                            .map(e -> new ValuePair(ResourceUtil.findAmountResourceName(e.getKey()),
                                    e.getValue()))
                            .toList();
        scope.put("cargo", cargos);
	}

    @Override
    protected String getEntityName(VehicleSpec v) {
        return v.getName();
    }
}