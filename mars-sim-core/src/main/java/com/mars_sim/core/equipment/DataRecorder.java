/*
 * Mars Simulation Project
 * DataRecorder.java
 * @date 2026-08-27
 * @author Manny Kung
 */
package com.mars_sim.core.equipment;

import java.util.HashMap;
import java.util.Map;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.SystemType;
import com.mars_sim.core.data.collection.FieldDataSet;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.resource.PartConfig;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;

public class DataRecorder extends Equipment implements Malfunctionable, Temporal {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	/* default logger. */
	private static final SimLogger logger = SimLogger.getLogger(DataRecorder.class.getName());
	
	// Static members
	/** The wear lifetime value is 1 orbit. */
	private static final double WEAR_LIFETIME = 668_000;
	/** The maintenance time in millisols. */
	private static final double MAINTENANCE_TIME = 20D;
	
	public static final String DATA = "Data";
	
	/** String name. */	
	public static final String TYPE = SystemType.DATA_RECORDER.getName();
	
	// Data members
	private static double usualMass = -1;
	
	private Map<Person, FieldDataSet> dataset = new HashMap<>();
	
	/** The equipment's malfunction manager. */
	private MalfunctionManager malfunctionManager;
	
	/**
	 * Constructor 1.
	 * 
	 * @param name
	 * @param settlement the location of the EVA suit.
	 * @throws Exception if error creating EVASuit.
	 */
	protected DataRecorder(String name, Settlement settlement) {
		// Use Equipment constructor.
		super(name, TYPE, settlement);
		
		setDescription("A standard data recorder.");

		// Add scope to malfunction manager.
		malfunctionManager = new MalfunctionManager(this, WEAR_LIFETIME, MAINTENANCE_TIME);
		
		PartConfig partConfig = SimulationConfig.instance().getPartConfiguration();
		
		// Add "Data" to the part scope
		partConfig.addScopes(DATA);

		// Add TYPE to the part scope
		partConfig.addScopes(TYPE);

		// Add "Data" to malfunction manager scope
		malfunctionManager.addScopeString(DATA);
		
		// Add TYPE to malfunction manager scope
		malfunctionManager.addScopeString(TYPE);
		
		// Add computation function type
		malfunctionManager.addScopeString(FunctionType.COMPUTATION.getName());
	
		// Initialize the scope map.
		malfunctionManager.initScopes();
		
		// Sets the base mass.
		setBaseMass(getEmptyMass());
	}
	
	/**
	 * Gets the usual mass of a data recorder.
	 * 
	 * @return
	 */
	public static double getEmptyMass() {
		if (usualMass < 0) {
			usualMass = EquipmentFactory.getEquipmentMass(EquipmentType.DATA_RECORDER);
		}
		return usualMass;
	}
	
	/**
	 * Constructor 2.
	 * 
	 * @param name
	 * @param eType
	 * @param type
	 * @param settlement
	 */
	protected DataRecorder(String name, EquipmentType eType, String type, Settlement settlement) {
		super(name, eType, type, settlement);
		
	}

	/**
	 * Time passing.
	 *
	 * @param pulse the amount of clock pulse passing (in millisols)
	 * @throws Exception if error during time.
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		// It doesn't check the pulse value like other units
		// because it is not called consistently every pulse. It is only
		// called when in use by a Person.
		malfunctionManager.timePassing(pulse);

		return true;
	}


	@Override
	public MalfunctionManager getMalfunctionManager() {
		return malfunctionManager;
	}


	@Override
	public UnitType getUnitType() {
		return UnitType.DATA_RECORDER;
	}


	@Override
	public double getStoredMass() {
		return 0;
	}


	@Override
	public double storeAmountResource(int resource, double quantity) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public double retrieveAmountResource(int resource, double quantity) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public double getSpecificCapacity(int resource) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public double getSpecificAmountResourceStored(int resource) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public boolean isEmpty(boolean brandNew) {
		// TODO Auto-generated method stub
		return false;
	}

}
