/**
 * Mars Simulation Project
 * FunctionType.java
 * @version 3.1.0 2017-08-16
 * @author stpa				
 */
package org.mars_sim.msp.core.structure.building.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mars_sim.msp.core.Msg;

public enum FunctionType {

    ADMINISTRATION              (Msg.getString("FunctionType.administration")), //$NON-NLS=1$
	ASTRONOMICAL_OBSERVATIONS	(Msg.getString("FunctionType.astronomicalObservations")), //$NON-NLS-1$
	BUILDING_CONNECTION			(Msg.getString("FunctionType.buildingConnection")), //$NON-NLS-1$
	COMMUNICATION				(Msg.getString("FunctionType.communication")), //$NON-NLS-1$
	COOKING						(Msg.getString("FunctionType.cooking")), //$NON-NLS-1$
	DINING						(Msg.getString("FunctionType.dining")), //$NON-NLS-1$
	EARTH_RETURN				(Msg.getString("FunctionType.earthReturn")), //$NON-NLS-1$
	EVA							(Msg.getString("FunctionType.eva")), //$NON-NLS-1$
	EXERCISE					(Msg.getString("FunctionType.exercise")), //$NON-NLS-1$
	FARMING						(Msg.getString("FunctionType.farming")), //$NON-NLS-1$
	FOOD_PRODUCTION  			(Msg.getString("FunctionType.foodProduction")), //$NON-NLS-1$	
	GROUND_VEHICLE_MAINTENANCE	(Msg.getString("FunctionType.groundVehicleMaintenance")), //$NON-NLS-1$
	LIFE_SUPPORT				(Msg.getString("FunctionType.lifeSupport")), //$NON-NLS-1$
	LIVING_ACCOMODATIONS		(Msg.getString("FunctionType.livingAccomodations")), //$NON-NLS-1$
	MANAGEMENT					(Msg.getString("FunctionType.management")), //$NON-NLS-1$
	MANUFACTURE					(Msg.getString("FunctionType.manufacture")), //$NON-NLS-1$
	MEDICAL_CARE				(Msg.getString("FunctionType.medicalCare")), //$NON-NLS-1$
	POWER_GENERATION			(Msg.getString("FunctionType.powerGeneration")), //$NON-NLS-1$
	POWER_STORAGE				(Msg.getString("FunctionType.powerStorage")), //$NON-NLS-1$
	PREPARING_DESSERT			(Msg.getString("FunctionType.preparingDessert")),  //$NON-NLS-1$
	RECREATION					(Msg.getString("FunctionType.recreation")), //$NON-NLS-1$
	RESEARCH					(Msg.getString("FunctionType.research")), //$NON-NLS-1$
	RESOURCE_PROCESSING			(Msg.getString("FunctionType.resourceProcessing")), //$NON-NLS-1$
	ROBOTIC_STATION				(Msg.getString("FunctionType.roboticStation")), //$NON-NLS-1$
	STORAGE						(Msg.getString("FunctionType.storage")),  //$NON-NLS-1$
	THERMAL_GENERATION			(Msg.getString("FunctionType.thermalGeneration")), //$NON-NLS-1$
	WASTE_DISPOSAL				(Msg.getString("FunctionType.wasteDisposal")), //$NON-NLS-1$
	// TODO: implement FIELD_STUDY
	FIELD_STUDY					(Msg.getString("FunctionType.fieldStudy")), //$NON-NLS-1$
	UNKNOWN						(Msg.getString("FunctionType.unknown")), //$NON-NLS-1$
	;

	private String name;

	/** hidden constructor. */
	private FunctionType(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
	
	public static String[] getNames() {
		List<String> list = new ArrayList<String>();
		for (FunctionType value : FunctionType.values()) {
			list.add(value.getName());
		}
		Collections.sort(list);
		return list.toArray(new String[] {});
	}
	
	public static List<FunctionType> getFunctionTypes() {
		List<FunctionType> list = new ArrayList<>();
		for (FunctionType value : FunctionType.values()) {
			list.add(value);
		}
		Collections.sort(list);
		return list;
	}
	
}
