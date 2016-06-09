/**
 * Mars Simulation Project
 * QuotationPopup.java
 * @version 3.08 2016-06-08
 * @author Manny Kung
 */

package org.mars_sim.msp.core.quotation;

import java.io.Serializable;

public class Quotation implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private String name, text;

	public Quotation(String name, String text) {
		this.name = name;
		this.text = text;
	}
	
	public String getName() {
		return name;
	}

	public String getText() {
		return text;
	}
}
