package org.mars_sim.msp.restws.mapper;

import javax.annotation.Generated;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.restws.model.VehicleDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2016-07-14T17:06:44-0700",
    comments = "version: 1.0.0.Final, compiler: javac, environment: Java 1.8.0_92 (Oracle Corporation)"
)
@Component
public class VehicleDetailsMapperImpl implements VehicleDetailsMapper {

    @Autowired
    private UnitReferenceMapper unitReferenceMapper;

    @Override
    public VehicleDetails vehicleToVehicleDetails(Vehicle vehicle) {
        if ( vehicle == null ) {
            return null;
        }

        VehicleDetails vehicleDetails = new VehicleDetails();

        vehicleDetails.setId( vehicle.getIdentifier() );
        vehicleDetails.setName( vehicle.getName() );
        vehicleDetails.setStatus( vehicle.getStatus() );
        vehicleDetails.setVehicleType( vehicle.getVehicleType() );
        vehicleDetails.setSpeed( vehicle.getSpeed() );
        vehicleDetails.setDistanceLastMaintenance( vehicle.getDistanceLastMaintenance() );
        vehicleDetails.setFuelEfficiency( vehicle.getFuelEfficiency() );
        vehicleDetails.setTowingVehicle( vehicleToVehicleDetails( vehicle.getTowingVehicle() ) );
        vehicleDetails.setSettlement( unitReferenceMapper.unitToUnitReference( vehicle.getSettlement() ) );

        vehicleDetails.setNumResources( vehicle.getInventory().getAllAmountResourcesStored(false).size() );
        vehicleDetails.setNumPersons( vehicle.getAffectedPeople().size() );
        vehicleDetails.setNumItems( vehicle.getInventory().getAllItemResourcesStored().size() );

        return vehicleDetails;
    }
}
