package com.mars_sim.core.moon;

import java.io.Serializable;

public class Colonist implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	/** default logger. */
	// May add back private static final SimLogger logger = SimLogger.getLogger(Colonist.class.getName())
	
	private String name;
	
	private int colonyId;
	
	public Colonist(String name, int colonyId) {
		this.name = name;
		this.colonyId = colonyId;
	}
	
	public String getName() {
		return name;
	}
	
	public int getColonyId() {
		return colonyId;
	}
}
