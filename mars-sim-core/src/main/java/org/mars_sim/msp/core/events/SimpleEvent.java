/**
 * Mars Simulation Project
 * SimpleEvent.java
 * @version 3.1.0 2018-06-28
 * @author Manny Kung
 */

package org.mars_sim.msp.core.events;

import java.io.Serializable;

import org.mars_sim.msp.core.time.ClockUtils;

public class SimpleEvent implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 23982863L;

	private static final String ONE_ZERO = "0";
	private static final String TWO_ZEROS = "00";
//	private static final String THREE_ZEROS = "000";
	private static final String COLON = ":";

	private short sol;
	private float msol;
	private byte cat;
	private byte type;
	private short what;
	private short whileDoing;
	private short who;
	private short loc0;
	private short loc1;
	private String dateTime = null;

	public SimpleEvent(short sol, float msol, byte cat, byte type, short what, short whileDoing, short who, short loc0,
			short loc1) {
		this.sol = sol;
		this.msol = msol;
		this.cat = cat;
		this.type = type;
		this.what = what;
		this.whileDoing = whileDoing;
		this.who = who;
		this.loc0 = loc0;
		this.loc1 = loc1;
	}

	public String getFullDateTimeString() {
		if (dateTime == null) {
			dateTime = ClockUtils.convertMissionSol2Date(sol) + COLON + getMsol();
		}
		return dateTime;
	}

	public short getSol() {
		return sol;
	}

	/**
	 * Returns the sol string in xxx.xxx format
	 * 
	 * @param ratio The time ratio
	 * @return the sol string
	 */
	public String getMsol() {
		StringBuilder result = new StringBuilder();

		float m = (float) (Math.round(msol * 1000.0) / 1000.0);

		if (m < 10) // then 000x
			result.append(TWO_ZEROS).append(m);
		else if (m < 100) // then 00xx
			result.append(ONE_ZERO).append(m);
		else // if (m < 1000) // then 0xxx
			result.append(m);

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

	public short getWhileDoing() {
		return whileDoing;
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