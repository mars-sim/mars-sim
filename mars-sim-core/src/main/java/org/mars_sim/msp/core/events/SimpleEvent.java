/*
 * Mars Simulation Project
 * SimpleEvent.java
 * @date 2022-09-24
 * @author Manny Kung
 */

package org.mars_sim.msp.core.events;

import java.io.Serializable;
import java.text.DecimalFormat;

import org.mars_sim.msp.core.time.ClockUtils;

public class SimpleEvent implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final String COLON = ":";

	private short missionSol;
	private float msol;
	private byte cat;
	private byte type;
	private short what;
	private short whileDoing;
	private short who;
	private short loc0;
	private short loc1;
	private short settlementID;
	private String dateTime = null;

	private DecimalFormat df = new DecimalFormat("000.000");

	public SimpleEvent(short missionSol, float msol, byte cat, byte type, short what, short whileDoing, short who, short loc0,
			short loc1, short settlementID) {
		this.missionSol = missionSol;
		this.msol = msol;
		this.cat = cat;
		this.type = type;
		this.what = what;
		this.whileDoing = whileDoing;
		this.who = who;
		this.loc0 = loc0;
		this.loc1 = loc1;
		this.settlementID = settlementID;
		
		df.setMinimumFractionDigits(3);
		df.setMinimumIntegerDigits(3);
		
	}

	public String getFullDateTimeString() {
		if (dateTime == null) {
			dateTime = ClockUtils.convertMissionSol2Date(missionSol) + COLON + getDecimalMillisol();
		}
		return dateTime;
	}

	public short getSol() {
		return missionSol;
	}

	/**
	 * Returns the time string in the decimal format of xxx.xxx
	 * 
	 * @return the time string
	 */
	public String getDecimalMillisol() {
		return df.format(msol);
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
	
	public short getSettlementID() {
		return settlementID;
	}
}
