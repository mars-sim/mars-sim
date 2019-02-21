/**
 * Mars Simulation Project
 * SimpleEvent.java
 * @version 3.1.0 2018-06-28
 * @author Manny Kung
 */

package org.mars_sim.msp.core.events;

import java.io.Serializable;
import java.text.DecimalFormat;

import org.mars_sim.msp.core.time.ClockUtils;

public class SimpleEvent implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 23982863L;

//	private static final String ONE_ZERO = "0";
//	private static final String TWO_ZEROS = "00";
//	private static final String THREE_ZEROS = "000";
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
			dateTime = ClockUtils.convertMissionSol2Date(missionSol) + COLON + getDecimalMillisol();//getMsol();
		}
		return dateTime;
	}

	public short getSol() {
		return missionSol;
	}

//	/**
//	 * Returns the time string in the non-decimal format of xxx
//	 * 
//	 * @return the time string
//	 */
//	public String getMillisol() {
//		
//		StringBuilder s = new StringBuilder();
//
//		int m = (int) (Math.round(msol));
//
//		if (m < 10) // then 000x
//			s.append(TWO_ZEROS).append(m);
//		else if (m < 100) // then 00xx
//			s.append(ONE_ZERO).append(m);
//		else // if (m < 1000) // then 0xxx
//			s.append(m);
//
//		return s.toString();
//	}
	
//	/**
//	 * Returns the time string in xxx.xxx decimal format
//	 * 
//	 * @return the time string
//	 */
//	public String getMissionSol() {
//		StringBuilder s = new StringBuilder();
//
//		float m = (float) (Math.round(msol * 1000.0) / 1000.0);
//
//		if (m < 10) // then 000x
//			s.append(TWO_ZEROS).append(m);
//		else if (m < 100) // then 00xx
//			s.append(ONE_ZERO).append(m);
//		else // if (m < 1000) // then 0xxx
//			s.append(m);
//
//		return s.toString();
//	}

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