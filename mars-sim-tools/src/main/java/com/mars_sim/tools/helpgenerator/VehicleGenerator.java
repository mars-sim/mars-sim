/*
 * Mars Simulation Project
 * VehicleGenerator.java
 * @date 2024-02-23
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.vehicle.VehicleSpec;
import com.mars_sim.tools.helpgenerator.HelpGenerator.ValuePair;

/**
 * Help generator for the VehicleSpec configuration item.
 */
public class VehicleGenerator extends TypeGenerator<VehicleSpec> {

    public static final String TYPE_NAME = "vehicle";

    protected VehicleGenerator(HelpGenerator parent) {
        super(parent, TYPE_NAME, "Vehicle Specs",
                "Types of Vehicles Featured for Mars Surface Operations");
    }
    
    /**
     * Get a list of all the vehicle specifications configured.
     */
    protected List<VehicleSpec> getEntities() {
        return getParent().getConfig().getVehicleConfiguration().getVehicleSpecs().stream()
		 							.sorted((o1, o2)->o1.getName().compareTo(o2.getName()))
									 .toList();
    }

	/**
	 * Generate the file for the Vehicle specifications.
	 * @param v Spec being rendered.
     * @param output Destination of content
	 * @throws IOException
	 */
	public void generateEntity(VehicleSpec v, OutputStream output) throws IOException {
        var generator = getParent();

		// Individual vehicle pages
	    var vScope = generator.createScopeMap("Vehicle " + v.getName());
		vScope.put(TYPE_NAME, v);

        // Convert capacity to a list that contains the resource name
        var cargos = v.getCargoCapacityMap().entrySet().stream()
                            .map(e -> new ValuePair(ResourceUtil.findAmountResourceName(e.getKey()),
                                    e.getValue()))
                            .toList();
        vScope.put("cargo", cargos);

        // Generate the file
        generator.generateContent("vehicle-detail", vScope, output);
	}

    @Override
    protected String getEntityName(VehicleSpec v) {
        return v.getName();
    }
}