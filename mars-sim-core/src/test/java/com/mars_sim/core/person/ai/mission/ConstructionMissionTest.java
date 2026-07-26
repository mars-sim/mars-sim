package com.mars_sim.core.person.ai.mission;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.LightUtilityVehicle;

class ConstructionMissionTest {

    @Test
    void testConstructionVehiclesAreLightUtilityVehicles() throws Exception {
        var constructor = ConstructionMission.class.getConstructor(Collection.class, Settlement.class,
                ConstructionSite.class, List.class);
        assertEquals(LightUtilityVehicle.class, getListType(constructor.getGenericParameterTypes()[3]),
                "Construction missions should only accept light utility vehicles");

        var getter = ConstructionMission.class.getMethod("getConstructionVehicles");
        assertEquals(LightUtilityVehicle.class, getListType(getter.getGenericReturnType()),
                "Construction missions should only expose light utility vehicles");
    }

    private static Class<?> getListType(java.lang.reflect.Type type) {
        var parameterized = (ParameterizedType) type;
        return (Class<?>) parameterized.getActualTypeArguments()[0];
    }
}
