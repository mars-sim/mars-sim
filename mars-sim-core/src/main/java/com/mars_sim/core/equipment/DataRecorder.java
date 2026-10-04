/*
 * Mars Simulation Project
 * DataRecorder.java
 * @date 2026-08-27
 * @author Manny Kung
 */
package com.mars_sim.core.equipment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.SystemType;
import com.mars_sim.core.data.History;
import com.mars_sim.core.data.collection.DataType;
import com.mars_sim.core.data.collection.FieldDataSet;
import com.mars_sim.core.data.collection.WaterIceData;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.malfunction.MalfunctionManager.MaintenanceParameters;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.PartConfig;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.core.tool.MsgContext;

public class DataRecorder extends Equipment implements Malfunctionable, Temporal {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	/* default logger. */
	// Will add back: private static final SimLogger logger = SimLogger.getLogger(DataRecorder.class.getName())
	
	// Static members
	/** The wear lifetime value is 1 orbit, maintenance time is 20 millisols. */
	private static final MaintenanceParameters MAINT_PARAMS = new MaintenanceParameters(668_000,
						20D, 0.5D, false);
	
	
	public static final String INSTRUMENT = "Instrument";
	
	/** String name. */	
	public static final String TYPE = SystemType.DATA_RECORDER.getName();
	
	// Data members
	private static double usualMass = -1;
	
	static {
		// Initialize the parts
		ItemResourceUtil.initDataRecorder();
	}
	
	private Map<Worker, List<FieldDataSet>> dataSetMap = new HashMap<>();
	
	/** The equipment's malfunction manager. */
	private MalfunctionManager malfunctionManager;
	/** The recorder's event history. */
	private History<MsgContext> eventHistory = new History<>(28);
	
	
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
		malfunctionManager = new MalfunctionManager(this, MAINT_PARAMS);
		
		PartConfig partConfig = SimulationConfig.instance().getPartConfiguration();
		
		// Add "Data" to the part scope
		partConfig.addScopes(INSTRUMENT);

		// Add TYPE to the part scope
		partConfig.addScopes(TYPE);

		// Add "Data" to malfunction manager scope
		malfunctionManager.addScopeString(INSTRUMENT);
		
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
	 * Gets the data set map.
	 * 
	 * @return
	 */
	public Map<Worker, List<FieldDataSet>> getDataSetMap() {
		return dataSetMap;
	}
	
	/**
	 * Checks the registered owner id.
	 * 
	 * @param ownerID
	 * @return
	 */
	public boolean checkRegisteredOwnerID(int ownerID) {
		return dataSetMap.keySet().stream().anyMatch(p -> p.getIdentifier() == ownerID);

//		Set<Integer> ids = dataset.keySet().stream()
//				.map(p -> p.getIdentifier()) 
//			    .collect(Collectors.toSet());
//		
//		if (ids.contains(ownerID))
//			return true;
//		
//		return false;
	}
	
	/**
	 * Records the data.
	 * 
	 * @param worker
	 * @param workTime
	 * @param initialQuality
	 * @param isNewRecording
	 */
	public void recordData(Worker worker, double workTime, int initialQuality, boolean isNewRecording) {
		if (dataSetMap.isEmpty()) {
			createNewFieldDataSet(worker, workTime, initialQuality);
		}
		else {
			FieldDataSet data = null;
			List<FieldDataSet> list = dataSetMap.get(worker);
			if (list.isEmpty()) {
				data = createNewFieldDataSet(worker, workTime, initialQuality);
				list = new ArrayList<>();
			}
			else if (isNewRecording) {
				data = createNewFieldDataSet(worker, workTime, initialQuality);
			}
			else {
				int size = list.size();
				data = list.get(size - 1);
			}
			data.addWorkTime(workTime);
			list.add(data);
			dataSetMap.put(worker, list);
		}
	}
	
	/**
	 * Creates a new dataset.
	 * 
	 * @param worker
	 * @param workTime
	 * @param initialQuality
	 * @return
	 */
	private FieldDataSet createNewFieldDataSet(Worker worker, double workTime, int initialQuality) {
		FieldDataSet data = new WaterIceData(
				DataType.GROUND_DATA,
				masterClock.getMarsTime(), 
				initialQuality);
		data.addWorkTime(workTime);
		return data;
	}
	
	/**
	 * Adds a dataset.
	 *  
	 * @param person
	 * @param dataSet
	 */
	public void addDataset(Worker worker, FieldDataSet dataSet) {
		List<FieldDataSet> list = null;
		if (dataSetMap.containsKey(worker)) {
			list = dataSetMap.get(worker);
			for (FieldDataSet fds: list) {
				if (fds.getIdentifier() == dataSet.getIdentifier()) {
					// Overwrite the dataset
					fds = dataSet;
					break;
				}
			}
		}
		else {
			list = new ArrayList<>();
		}
		// Add the dataset
		list.add(dataSet);
		// Add the list
		dataSetMap.put(worker, list);
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
		return malfunctionManager.timePassing(pulse);
	}


	@Override
	public MalfunctionManager getMalfunctionManager() {
		return malfunctionManager;
	}

	/**
	 * Gets the data recorder history.
	 *
	 * @return List of interesting events for this data recorder.
	 */
	@Override
	public History<MsgContext> getHistory() {
		return eventHistory;
	}

	/**
	 * Adds an entry to the data recorder's history.
	 * @param entry the history entry to add.
	 */
	@Override
	public void addHistoryEntry(MsgContext entry) {
		eventHistory.add(entry);
		fireUnitUpdate(EntityEventType.HISTORY_EVENT);
	}

	@Override
	public UnitType getUnitType() {
		return UnitType.DATA_RECORDER;
	}

	@Override
	public boolean isEmpty(boolean brandNew) {
		return false;
	}
}
