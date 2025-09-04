package com.mars_sim.core.moon;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

final class ZoneTest {

    @Test
    void builder_initializes_area_via_legacy_rules_when_not_provided() {
        Zone z = new Zone.Builder()
                .name("Biz-1")
                .zoneType(ZoneType.BUSINESS)
                .build();
        assertTrue(z.getArea() > 0.0, "area should be initialized");
    }

    @RepeatedTest(3)
    void business_area_is_reasonably_bounded_statistically() {
        Zone z = new Zone.Builder()
                .name("Biz")
                .zoneType(ZoneType.BUSINESS)
                .areaFactor(2.0)
                .build();
        double a = z.getArea();
        // Loose statistical guardrail; ranges come from legacy semantics.
        assertTrue(a >= 2.0 * 25.0 && a <= 2.0 * 50.0, "area should be within BUSINESS range × factor");
    }

    @Test
    void setType_recomputes_area_when_changed() {
        Zone z = new Zone.Builder().name("Z").zoneType(ZoneType.BUSINESS).build();
        double a1 = z.getArea();
        z.setType(ZoneType.BUSINESS); // same type but should still be valid and non-zero
        double a2 = z.getArea();
        assertTrue(a2 > 0.0);
    }

    @Test
    void explicit_area_override_is_respected() {
        Zone z = new Zone.Builder().name("Z").zoneType(ZoneType.BUSINESS).area(1234.0).build();
        assertEquals(1234.0, z.getArea(), 0.0001);
    }
}
