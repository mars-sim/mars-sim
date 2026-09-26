/*
 * Mars Simulation Project
 * SiteVisit.java
 * @date 2026-09-17
 * @author Manny Kung
 */

package com.mars_sim.core.data.collection.task;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.mars_sim.core.equipment.DataRecorder;
import com.mars_sim.core.person.ai.task.util.Worker;

public class SiteVisit implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private boolean isClosed = false;
	
	private Map<WorkType, Double> workTime;
	
	private Set<Integer> solVisited;
	
	private Set<Integer> instrumentAvailability;

	private Set<DataRecorder> dataRecorders;

	private Set<Worker> investigators;
	
	public SiteVisit(int firstSol, Worker worker) {
		
		workTime = new HashMap<>();
		
		solVisited = new HashSet<>();
		instrumentAvailability = new HashSet<>();
		dataRecorders = new HashSet<>();
		investigators = new HashSet<>();
		
		solVisited.add(firstSol);
		investigators.add(worker);
	}
	
	public boolean isClosed() {
		return isClosed;
	}
	
	public void setClosed(boolean value) {
		isClosed = value;
	}
	
	/**
	 * Checks if this worker is the only investigator left in the site.
	 * 
	 * @param worker
	 * @return
	 */
	public boolean isOnlyInvestigatorLeft(Worker worker) {
		Set<Worker> newSet = new HashSet<>(investigators);
		newSet.remove(worker);
		if (newSet.isEmpty())
			return true;
		return false ;
	}
	
	public Set<DataRecorder> getDataRecorders() {
		return dataRecorders;
	}
	
	/**
	 * Gets the instrument availability list.
	 * 
	 * @return
	 */
	public Set<Integer> getInstrumentAvailability() {
		return instrumentAvailability;
	}
	
	/**
	 * Gets the number of instruments available.
	 * 
	 * @return
	 */
	public int getNumInstrumentAvailable() {
		if (instrumentAvailability.isEmpty())
			return 0;
		else {
			return instrumentAvailability.size();
		}
	}
	
	/**
	 * Adds an instrument.
	 * 
	 * @param id
	 * @return
	 */
	public boolean addInstrument(Integer id) {
		return instrumentAvailability.add(id);
	}

	/**
	 * Adds a data recorder.
	 * 
	 * @param id
	 * @return
	 */
	public boolean addDataRecorder(DataRecorder dr) {
		return dataRecorders.add(dr);
	}
	
	/**
	 * Removes an instrument.
	 * 
	 * @param id
	 * @return
	 */
	public boolean removeInstrument(Integer id) {
		return instrumentAvailability.remove(id);
	}
	
	/**
	 * Removes a data recorder.
	 * 
	 * @param id
	 * @return
	 */
	public boolean removeDataRecorder(DataRecorder dr) {
		return dataRecorders.remove(dr);
	}
	
	/**
	 * Checks if it has an instrument.
	 * 
	 * @param id
	 * @return
	 */
	public boolean hasInstrument(Integer id) {
		return instrumentAvailability.contains(id);
	}
	
	/**
	 * Checks if it has a data recorder.
	 * 
	 * @param id
	 * @return
	 */
	public boolean hasDataRecorder(DataRecorder dr) {
		return dataRecorders.contains(dr);
	}
	
	public void addworkTime(WorkType type, double time) {
		if (workTime.containsKey(type)) {
			double oldTime = workTime.get(type);
			workTime.put(type, oldTime + time);
		}
		else {
			workTime.put(type, time);
		}
	}
}
