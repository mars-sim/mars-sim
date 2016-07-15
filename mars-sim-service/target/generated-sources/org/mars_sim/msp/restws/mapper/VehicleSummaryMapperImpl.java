package org.mars_sim.msp.restws.mapper;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.restws.model.VehicleSummary;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2016-07-14T17:06:44-0700",
    comments = "version: 1.0.0.Final, compiler: javac, environment: Java 1.8.0_92 (Oracle Corporation)"
)
@Component
public class VehicleSummaryMapperImpl implements VehicleSummaryMapper {

    @Override
    public VehicleSummary vehicleToVehicleSummary(Vehicle vehicle) {
        if ( vehicle == null ) {
            return null;
        }

        VehicleSummary vehicleSummary = new VehicleSummary();

        vehicleSummary.setId( vehicle.getIdentifier() );
        vehicleSummary.setName( vehicle.getName() );
        vehicleSummary.setStatus( vehicle.getStatus() );
        vehicleSummary.setVehicleType( vehicle.getVehicleType() );

        return vehicleSummary;
    }

    @Override
    public List<VehicleSummary> vehiclesToVehicleSummarys(List<Vehicle> arrayList) {
        if ( arrayList == null ) {
            return null;
        }

        List<VehicleSummary> list = new ArrayList<VehicleSummary>();
        for ( Vehicle vehicle : arrayList ) {
            list.add( vehicleToVehicleSummary( vehicle ) );
        }

        return list;
    }
}
