package org.mars_sim.msp.restws.mapper;
/*
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.restws.Application;
import org.mars_sim.msp.restws.model.CoordinateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

//@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Application.class)
public class CoordinatesMapperTest {

	// auto wiring is not working in the test without JUnit4 runner. But this runner
	// throws error about the springfox missing annotation.
	//@Autowired
	private CoordinatesMapper mapper = new CoordinatesMapperImpl();
	
	@Test
	public void map() {
		Coordinates source = new Coordinates(1.0, 2.0);
		
		CoordinateDTO target = mapper.coordinatesToCoordinateDTO(source);
		assertEquals("Theta", source.getTheta(), target.getTheta(),0.1);
		assertEquals("Phi", source.getPhi(), target.getPhi(), 0.1);
		assertEquals("Lat", source.getFormattedLatitudeString(), target.getLatitude());
		assertEquals("LOng", source.getFormattedLongitudeString(), target.getLongitude());

	}

}
*/