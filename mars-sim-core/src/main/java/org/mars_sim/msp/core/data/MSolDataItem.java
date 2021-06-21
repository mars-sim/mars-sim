/**
 * Mars Simulation Project
 * MSolDataItem.java
 * @version 3.2.0 2021-06-20
 * @author Barry Evans
 */

package org.mars_sim.msp.core.data;

import java.io.Serializable;

/**
 * Timestamped Data item used for data logging.
 * @param <T> The data component
 * @see MSolDataLogger
 */
public class MSolDataItem<T> implements Comparable<MSolDataItem<T>>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int msol;
	private T data;
	
	MSolDataItem(int msol, T data) {
		super();
		this.msol = msol;
		this.data = data;
	}

	public int getMsol() {
		return msol;
	}

	public T getData() {
		return data;
	}

	public int compareTo(MSolDataItem<T> item) {
		int diff = msol - item.msol;
		
		return diff;
	}

	@Override
	public String toString() {
		return "MSolDataItem [msol=" + msol + ", data=" + data + "]";
	}
}
