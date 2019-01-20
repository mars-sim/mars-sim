/**
 * Astronomical Time Class
 */
package org.mars_sim.msp.core.astroarts;

public class ATime {
	private int    nYear, nMonth, nDay;
	private int    nHour, nMin;
	private double fSec;
	private double fJd;
	private double fTimezone;
	private double fT;			// Origin 1974/12/31  0h ET
	private double fT2;			// Origin 2000/01/01 12h ET

	static final private String strMonthAbbr[] = {
		"Jan.", "Feb.", "Mar.", "Apr.", "May ", "June",
		"July", "Aug.", "Sep.", "Oct.", "Nov.", "Dec.",
	};

	// flags for changeDate
	static final public int F_INCTIME =  1;
	static final public int F_DECTIME = -1;

	/**
	 * Get Abbreviated Month Name
	 */
	static public String getMonthAbbr(int nMonth) {
		return strMonthAbbr[nMonth - 1];
	}

	/**
	 * YMD/HMS -> JD
	 */
	private double makeJd() {
		int    nYear  = this.nYear;
		int    nMonth = this.nMonth;
		double fDay = (double)this.nDay
					+ (double)this.nHour / 24.0
					+ (double)this.nMin  / 24.0 / 60.0
					+ this.fSec          / 24.0 / 60.0 / 60.0;
		if (nMonth < 3) {
			nMonth += 12;
			nYear  -=  1;
		}
		double fJd = Math.floor(365.25 * (double)nYear)
					+ Math.floor(30.59 * (double)(nMonth - 2))
					+ fDay +1721086.5;
		if (fJd > 2299160.5) {
			fJd += Math.floor((double)nYear / 400.0)
				 - Math.floor((double)nYear / 100.0) + 2.0;
		}
		return fJd;
	}

	/**
	 * Time Parameter Origin of 1974/12/31  0h ET
	 */
	private double makeT() {
		// 2442412.5 = 1974.12.31 0h ET
		double ft = (this.fJd - 2442412.5) / 365.25;
		double fT = ft + (0.0317 * ft + 1.43) * 0.000001;
		return fT;
	}

	/**
	 * Time Parameter Origin of 2000/01/01 12h ET
	 */
	private double makeT2() {
		double ft = (fJd - Astro.JD2000) / 36525.0;
		return ft;
	}

	/**
	 * JD -> YMD/HMS
	 */
	private void getDate(double fJd) {
		fJd += 0.5;
		double a = Math.floor(fJd);
		if (a >= 2299160.5) {
			double t = Math.floor((a - 1867216.25) / 36524.25);
			a += t - Math.floor(t / 4.0) + 1.0;
		}
		double b = Math.floor(a) + 1524;
		double c = Math.floor((b - 122.1) / 365.25);
		double k = Math.floor((365.25) * c);
		double e = Math.floor((b - k) / 30.6001);
		double fDay = b - k - Math.floor(30.6001 * e)
							+ (fJd - Math.floor(fJd));
		this.nMonth = (int)Math.floor(e - ((e >= 13.5) ? 13 : 1) + 0.5);
		this.nYear  = (int)Math.floor(c - ((this.nMonth > 2)
										   ? 4716 : 4715) + 0.5);
		this.nDay   = (int)Math.floor(fDay);
		double fHour = (fDay - this.nDay) * 24.0;
		this.nHour  = (int)Math.floor(fHour);
		double fMin = (fHour - this.nHour) * 60.0;
		this.nMin   = (int)Math.floor(fMin);
		this.fSec   = (fMin - this.nMin) * 60.0;
	}

	public void changeDate(TimeSpan Span, int nIncOrDec) {
		//
		// First, calculate new Hour,Minute,Second
		//
		double fHms1 = this.nHour * 60.0 * 60.0
			+ this.nMin  * 60.0 + this.fSec;
		double fHms2 = Span.nHour * 60.0 * 60.0
			+ Span.nMin  * 60.0 + Span.fSec;
		fHms1 += (nIncOrDec == F_INCTIME) ? fHms2 : -fHms2;
		int nDay1;
		if (0.0 <= fHms1 && fHms1 < 24.0 * 60.0 * 60.0) {
			nDay1 = 0;
		} else if (fHms1 >= 24.0 * 60.0 * 60.0) {
			nDay1 = (int)Math.floor(fHms1 / 24.0 / 60.0 / 60.0);
			fHms1 = UdMath.fmod(fHms1, 24.0 * 60.0 * 60.0);
		} else {
			nDay1 = (int)Math.ceil(fHms1 / 24.0 / 60.0 / 60.0) - 1;
			fHms1 = UdMath.fmod(fHms1, 24.0 * 60.0 * 60.0)
				+ 24.0 * 60.0 * 60.0;
		}

		int    nNewHour = (int)Math.floor(fHms1 / 60.0 / 60.0);
		int    nNewMin  = (int)Math.floor(fHms1 / 60.0) - nNewHour * 60;
		double fNewSec  = fHms1 - ((double)nNewHour * 60.0 * 60.0
								 + (double)nNewMin * 60.0);

		//
		// Next, calculate new Year, Month, Day
		//
		ATime newDate = new ATime(this.getYear(), this.getMonth(),
								  this.getDay(), 12, 0, 0.0, 0.0);
		double fJd = newDate.getJd();
		fJd += (nIncOrDec == F_INCTIME) ? nDay1 + Span.nDay
										: nDay1 - Span.nDay;
		newDate = new ATime(fJd, 0.0);
		
		int nNewYear  = newDate.getYear();
		int nNewMonth = newDate.getMonth();
		int nNewDay   = newDate.getDay();
		nNewMonth += (nIncOrDec == F_INCTIME) ? Span.nMonth : -Span.nMonth;
		if (1 > nNewMonth) {
			nNewYear -= nNewMonth / 12 + 1;
			nNewMonth = 12 + nNewMonth % 12;
		} else if (nNewMonth > 12) {
			nNewYear += nNewMonth / 12;
			nNewMonth = 1 + (nNewMonth - 1) % 12;
		}
		nNewYear += (nIncOrDec == F_INCTIME) ? Span.nYear : -Span.nYear;
		
		// check bound between julian and gregorian
		if (nNewYear == 1582 && nNewMonth == 10) {
			if (5 <= nNewDay && nNewDay < 10) {
				nNewDay = 4;
			} else if (10 <= nNewDay && nNewDay < 15) {
				nNewDay = 15;
			}
		}
		newDate = new ATime(nNewYear, nNewMonth, nNewDay, 12, 0, 0, 0.0);
		nNewYear  = newDate.getYear();
		nNewMonth = newDate.getMonth();
		nNewDay   = newDate.getDay();

		this.nYear  = nNewYear;
		this.nMonth = nNewMonth;
		this.nDay   = nNewDay;
		this.nHour  = nNewHour;
		this.nMin   = nNewMin;
		this.fSec   = fNewSec;
		this.fJd = makeJd() - fTimezone / 24.0;
		this.fT  = makeT();
		this.fT2 = makeT2();
	}

	/**
	 * Constructor (YMD/HMS,TZ)
	 */
	public ATime(int nYear, int nMonth, int nDay,
				 int nHour, int nMin, double fSec,
				 double fTimezone) {
		this.nYear  = nYear;
		this.nMonth = nMonth;
		this.nDay   = nDay;
		this.nHour  = nHour;
		this.nMin   = nMin;
		this.fSec   = fSec;
		this.fJd = makeJd() - fTimezone / 24.0;
		this.fTimezone = fTimezone;
		this.fT  = makeT();
		this.fT2 = makeT2();
	}
	
	/**
	 * Constructor (YMD,TZ)
	 */
	public ATime(int nYear, int nMonth, double fDay, double fTimezone) {
		this.nYear  = nYear;
		this.nMonth = nMonth;
		this.nDay   = (int)Math.floor(fDay);
		double fHour = (fDay - (double)this.nDay) * 24.0;
		this.nHour  = (int)Math.floor(fHour);
		double fMin = (fHour - (double)this.nHour) * 60.0;
		this.nMin   = (int)Math.floor(fMin);
		this.fSec   = (fMin - (double)this.nMin) * 60.0;
		this.fJd = makeJd() - fTimezone / 24.0;
		this.fTimezone = fTimezone;
		this.fT  = makeT();
		this.fT2 = makeT2();
	}
	
	/**
	 * Constructor (JD,TZ)
	 */
	public ATime(double fJd, double fTimezone) {
		this.fJd = fJd;
		this.fTimezone = fTimezone;
		getDate(fJd + fTimezone / 24.0);
		this.fT  = makeT();
		this.fT2 = makeT2();
	}
	
	/**
	 * Constructor (ATime)
	 */
	public ATime(ATime atime) {
		this.nYear     = atime.nYear;
		this.nMonth    = atime.nMonth;
		this.nDay      = atime.nDay;
		this.nHour     = atime.nHour;
		this.nMin      = atime.nMin;
		this.fSec      = atime.fSec;
		this.fJd       = atime.fJd;
		this.fTimezone = atime.fTimezone;
		this.fT        = atime.fT;
		this.fT2       = atime.fT2;
	}

	/**
	 * Get Values
	 */
	public int    getYear()     { return this.nYear;     }
	public int    getMonth()    { return this.nMonth;    }
	public int    getDay()      { return this.nDay;      }
	public int    getHour()     { return this.nHour;     }
	public int    getMinute()   { return this.nMin;      }
	public double getSecond()   { return this.fSec;      }
	public double getTimezone() { return this.fTimezone; }
	public double getJd()       { return this.fJd;       }
	public double getT()        { return this.fT;        }
	public double getT2()       { return this.fT2;       }

	/**
	 * Obliquity of Ecliptic (Static Function)
	 */
	static public double getEp(double fJd) {
		double ft = (fJd - Astro.JD2000) / 36525.0;
		if (ft > 30.0){		// Out of Calculation Range
			ft = 30.0;
		} else if (ft < -30.0){
			ft = -30.0;
		}
		double fEp =  23.43929111
				- 46.8150  / 60.0 / 60.0 * ft
				- 0.00059  / 60.0 / 60.0 * ft * ft
				+ 0.001813 / 60.0 / 60.0 * ft * ft * ft;
		return fEp * Math.PI / 180.0;
	}

	/**
	 * Print to Standard Output
	 */
	public String toString() {
		return new String(Integer.toString(this.nYear)  + "/"
						  + Integer.toString(this.nMonth) + "/"
						  + Integer.toString(this.nDay)   + " "
						  + Integer.toString(this.nHour)  + ":"
						  + Integer.toString(this.nMin)   + ":"
						  + Double.toString(this.fSec)
						  + " = " + Double.toString(fJd)
						  + " (TZ:" + Double.toString(this.fTimezone) + ")");
	}
}
