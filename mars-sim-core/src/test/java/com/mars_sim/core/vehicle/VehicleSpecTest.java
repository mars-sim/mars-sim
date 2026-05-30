package com.mars_sim.core.vehicle;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

class VehicleSpecTest {

	@Test
	void calculateDetailsDoesNotRequireManufactureProcess() {
		VehicleSpec spec = new VehicleSpec("Unit Test Rover", VehicleType.EXPLORER_ROVER, "A", "Test", "test",
				"fuel_power", "methanol", 5D,
				2, 1D, 1,
				.85D, 30D, 90D,
				1234D, 2);
		spec.setCargoCapacity(1000D, Map.of());

		assertDoesNotThrow(() -> spec.calculateDetails(null));
		assertEquals(1234D, spec.getEmptyMass(), "Should use configured empty mass");
		assertTrue(spec.getParts().isEmpty(), "Parts list should be empty when process is unavailable");
	}
}
