/**
 * Mars Simulation Project
 * Gene.java
 * @version 3.1.0 2017-10-03
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import java.io.Serializable;

public class Gene implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	//Chromosome chromosome;
	Person person;
	int id;
	String name;
	boolean paternal;
	boolean dominant;
	String sValue;
	double dValue;
	
	public Gene(Person person, int id, String name, boolean paternal, boolean dominant, String sValue, double dValue) {
		this.person = person; 
		this.id = id;
		this.name = name;
		this.paternal = paternal;
		this.dominant = dominant;
		this.sValue = sValue;
		this.dValue = dValue;		
		
	}

	
}
