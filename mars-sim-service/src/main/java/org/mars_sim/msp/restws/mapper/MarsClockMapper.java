package org.mars_sim.msp.restws.mapper;

import org.mars_sim.msp.core.time.MarsClock;
import org.springframework.stereotype.Component;

@Component
public class MarsClockMapper {

	public String toString(MarsClock clock) {
		if (clock == null) {
			return null;
		}
		return clock.getDateTimeStamp();
	}
}
