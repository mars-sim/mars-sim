package org.mars_sim.msp.core.person.ai.mission;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Logs data items according to the current Sol. Each sol can have mulitple data entries.
 * Only a maximum number of sols is retained.
 * @param <T> Data item being recorded
 */
public class DataLogger<T> implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	public static final int MAX_SOL = 5;
	private int currentSol = 0;
	private List<T> currentData = null;
	private List<List<T>> data = new LinkedList<List<T>>();
	
	public DataLogger() {
		super();
		newSol(1);
	}
	
	/**
	 * A new sol should be started in the logger.
	 * @param newSol
	 */
	public void newSol(int newSol) {
		if (newSol == currentSol) {
			return;
		}
		currentData = new ArrayList<T>();
		data.add(0, currentData);
		if (data.size() > MAX_SOL) {
			data.remove(MAX_SOL-1);
		}
	}
	
	/**
	 * Add a data item to the current data logger
	 * @param item
	 */
	public void addData(T item) {
		currentData.add(item);
	}

	/**
	 * Return a Map if data entries per missionSol.
	 * @return Sol to daily data entries.
	 */
	public Map<Integer, List<T>> getHistory() {
		Map<Integer, List<T>> results = new HashMap<Integer, List<T>>();
		int sol = currentSol;
		for (List<T> t : data) {
			results.put(sol++, t);
		}
		return results;
	}

	/**
	 * The current sol the Data Logger is recording
	 * @return
	 */
	public int getCurrentSol() {
		return currentSol;
	}

}
