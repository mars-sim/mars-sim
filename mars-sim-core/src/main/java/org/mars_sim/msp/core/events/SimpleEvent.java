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
	
	public float getMsol() {
		return msol;
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