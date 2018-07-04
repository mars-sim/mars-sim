/**
 * Mars Simulation Project
 * SimpleEvent.java
 * @version 3.1.0 2018-06-28
 * @author Manny Kung
 */

package org.mars_sim.msp.core.events;

import java.io.Serializable;

public class SimpleEvent implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 23982863L;
    
	private static final String ONE_ZERO = "0";
	private static final String TWO_ZEROS = "00";
	private static final String THREE_ZEROS = "000";
	
	short sol;
	float msol;
	byte cat;
	byte type;
	short what;
	short who;
	short loc0;
	short loc1;
	
	public SimpleEvent(short sol, float msol, byte cat, byte type, short what, short who, short loc0, short loc1) {
		this.sol = sol;
		this.msol= msol;
		this.cat = cat;
		this.type = type;
		this.what = what;
		this.who = who;
		this.loc0 = loc0;
		this.loc1 = loc1;
	}
	
	public short getSol() {
		return sol;
	}
	
	public String getMsol() {
		StringBuilder result = new StringBuilder();

		float m = msol;
		
		if (m < 10) // then 000x
			result.append(THREE_ZEROS);
		else if (m < 100) // then 00xx
			result.append(TWO_ZEROS);
		else if (m < 1000) // then 0xxx
			result.append(ONE_ZERO);
		
		return result.toString();
	}
	
	public byte getCat() {
		return cat;
	}
	
	public byte getType() {
		return type;
	}
	
	public short getWhat() {
		return what;
	}
	
	public short getWho() {
		return who;
	}
	
	public short getLoc0() {
		return loc0;
	}
	
	public short getLoc1() {
		return loc1;
	}
}