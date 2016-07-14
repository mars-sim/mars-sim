package org.mars_sim.msp.restws.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.restws.model.VehicleDetails;

@Mapper(componentModel="spring",
uses=UnitReferenceMapper.class)
public interface VehicleDetailsMapper {

	@Mappings({
		@Mapping(source="identifier", target="id"),
		
		// Have to use expression because the Set class does not follow JavaBeans style
		@Mapping(expression="java(vehicle.getInventory().getAllAmountResourcesStored(false).size())", target="numResources"),
		@Mapping(expression="java(vehicle.getInventory().getAllItemResourcesStored().size())", target="numItems"),
		@Mapping(expression="java(vehicle.getAffectedPeople().size())", target="numPersons")
		})
	VehicleDetails vehicleToVehicleDetails(Vehicle vehicle);

}
