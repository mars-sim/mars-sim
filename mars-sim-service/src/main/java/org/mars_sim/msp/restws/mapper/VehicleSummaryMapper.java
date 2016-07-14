package org.mars_sim.msp.restws.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.restws.model.VehicleSummary;

/**
 * Mapper for Vehicle Summary using MapStruct
 */
@Mapper(componentModel="spring")
public interface VehicleSummaryMapper {
	@Mappings({
		@Mapping(source="identifier", target="id")
		})
	VehicleSummary vehicleToVehicleSummary(Vehicle vehicle);
	
	/**
	 * List of summary DTOs.
	 * @param vehicles List of model vehicles
	 * @return
	 */
	List<VehicleSummary> vehiclesToVehicleSummarys(List<Vehicle> arrayList);
}
