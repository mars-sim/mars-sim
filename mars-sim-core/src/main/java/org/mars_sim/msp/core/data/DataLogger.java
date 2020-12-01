package org.mars_sim.msp.core.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.time.ClockPulse;

/**
 * Logs data items according to the current Sol. Each sol can have multiple data entries held in a list.
 * Only a maximum number of sols is retained.
 * The timestamp of the logger is shifted autumatically via the Simulation.
 * @param <T> Data item being recorded
 */
public class DataLogger<T> implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static int currentSol = 0;
	protected static int currentMsol = 0;
	
	private int maxSols = 5;
	private int latestSol = 0;
	private List<T> currentData = null;
	private List<List<T>> data = new LinkedList<List<T>>();
	
	public DataLogger() {
		super();
		newSol(1);
	}
	
	/**
	 * Move time onwards.
	 * @param pulse
	 */
	public static void changeTime(ClockPulse pulse) {
		currentSol = pulse.getMarsTime().getMissionSol();
		currentMsol = pulse.getMarsTime().getMillisolInt();
	}
	
	/**
	 * A new sol should be started in the logger.
	 * @param newSol
	 */
	private void newSol(int newSol) {
		latestSol = newSol;
		currentData = new ArrayList<T>();
		data.add(0, currentData);
		if (data.size() > maxSols) {
			data.remove(maxSols-1);
		}
	}
	
	/**
	 * Add a data item to the current data logger
	 * @param item
	 */
	public void addData(T item) {
		if (latestSol != currentSol) {
			newSol(currentSol);
		}
		currentData.add(item);
	}

	/**
	 * Return a Map if data entries per missionSol. Map is keyed on sol.
	 * @return Sol to daily data entries.
	 */
	public Map<Integer, List<T>> getHistory() {
		Map<Integer, List<T>> results = new HashMap<Integer, List<T>>();
		int sol = latestSol;
		for (List<T> t : data) {
			results.put(sol--, t);
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
