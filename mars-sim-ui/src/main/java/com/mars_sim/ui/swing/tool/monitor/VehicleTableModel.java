/*
 * Mars Simulation Project
 * VehicleTableModel.java
 * @date 2025-09-25
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.utils.model.BaseVehicleModel;

/**
 * The VehicleTableModel that maintains a list of Vehicle objects.
 * It maps key attributes of the Vehicle into Columns.
 */
@SuppressWarnings("serial")
public class VehicleTableModel extends BaseVehicleModel implements MonitorModel {

	private static final String VEHICLES = Msg.getString("vehicle.plural");

	// Displayed resources
	private static final Set<Integer> RESOURCES = Set.of(ResourceUtil.METHANE_ID, ResourceUtil.METHANOL_ID, ResourceUtil.OXYGEN_ID,
													ResourceUtil.WATER_ID, ResourceUtil.FOOD_ID);
	
	public VehicleTableModel() {
		super(RESOURCES, NAME, TYPE, SETTLEMENT, LOCATION, DESTINATION, DESTDIST, MISSION, DRIVER,
				STATUS, BEACON, RESERVED, SPEED, MALFUNCTION, BATTERY, FUEL);
	}

	@Override
	public String getName() {
		return VEHICLES;
	}

	@Override
	public int getSettlementColumn() {
		return 2;
	}

	/**
	 * Filters the vehicles to a settlement.
	 */
	@Override
	public boolean setSettlementFilter(Set<Settlement> filter) {
		
		Collection<Vehicle> vehicles = filter.stream()
				.flatMap(s -> s.getAllAssociatedVehicles().stream())
				.sorted(Comparator.comparing(Vehicle::getName))
				.toList();
	
		setEntities(vehicles);
		return true;
	}
}
