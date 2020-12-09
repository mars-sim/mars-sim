package org.mars_sim.msp.core.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.time.ClockPulse;


/**
 * Logs data items according to the current Sol. Each sol s a single data item.
 * Only a maximum number of sols is retained.
 * The timestamp of the logger is shifted autumatically via the Simulation.
 * @param <T> Data item being recorded
 */
public abstract class DataLogger<T> implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static int currentSol = 0;
	protected static int currentMsol = 0;
	
	private int maxSols = 5;
	private int latestSol = 0;
	protected T currentData = null;
	private List<T> data = new LinkedList<T>();
	
	public DataLogger(int maxSols) {
		super();
		this.maxSols = maxSols;
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
		currentData = getDataItem(); 
		data.add(0, currentData);
		if (data.size() > maxSols) {
			data.remove(maxSols-1);
		}
	}
	
	/**
	 * Create a new data item for a new sol;
	 * @return
	 */
	protected abstract T getDataItem();

	/**
	 * The logger is updating
	 */
	protected void updating() {
		if (latestSol != currentSol) {
			newSol(currentSol);
		}
	}

	/**
	 * Return a Map if data entries per missionSol. Map is keyed on sol.
	 * @return Sol to daily data entries.
	 */
	public Map<Integer, T> getHistory() {
		Map<Integer, T> results = new HashMap<>();
		int sol = latestSol;
		for (T t : data) {
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

	/**
	 * Get the data heled for a single sol
	 * @param sol Sol
	 * @return List of data items
	 */
	public T getSolData(int sol) {
		if (sol < 1) {
			throw new IllegalArgumentException("Mission Sol cannot be less than 1");
		}
		int idx = sol - latestSol;
		if (idx > data.size()) {
			return null;
		}
		else {
			return data.get(idx);
		}
	}
	
	/**
	 * Get the latest Sol data being captured
	 * @return
	 */
	public T getTodayData() {
		return currentData;
	}
	
	/**
	 * Get yesterdays data if it exists
	 * @return
	 */
	public T getYesterdayData() {
		if (currentSol == 1) {
			// No yesterday yet
			return null;
		}
		// Use the current sol incase this logger has not recorded an data point for today.
		int yesterdaySol = currentSol - 1;
		return getSolData(yesterdaySol);
	}
}
