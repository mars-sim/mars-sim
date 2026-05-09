package com.mars_sim.core.vehicle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class VehicleTypeTest {
    @ParameterizedTest
    @EnumSource(VehicleType.class)
    void testGetName(VehicleType type) {
        int calcId = VehicleType.getVehicleID(type);
        var calcType = VehicleType.convertID2Type(calcId);
        assertEquals(type, calcType, "Conversion of " + type);
    }

    @ParameterizedTest
    @EnumSource(value = VehicleType.class, names = {"DELIVERY_DRONE", "CARGO_DRONE", "PASSENGER_DRONE"})
    void testDrone(VehicleType type) {
        assertTrue(VehicleType.isDrone(type), "Is Drone " + type);
        assertFalse(VehicleType.isRover(type), "Is not Rover " + type);
    }

    @ParameterizedTest
    @EnumSource(value = VehicleType.class, names = {"EXPLORER_ROVER", "TRANSPORT_ROVER", "CARGO_ROVER"})
    void testVehicle(VehicleType type) {
        assertFalse(VehicleType.isDrone(type), "Is Not Drone " + type);
        assertTrue(VehicleType.isRover(type), "Is Rover " + type);
    }

}
