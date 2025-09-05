package com.mars_sim.core.moon;

final class ZoneTest {

    public static void main(String[] args) {
        // 1) area initialized when type set
        Zone z1 = Zone.builder("Biz-1")
                .zoneType(ZoneType.BUSINESS)
                .build();
        check(z1.getArea() > 0.0, "area should be initialized (> 0)");

        // 2) factor affects randomized range
        Zone z2 = Zone.builder("Biz-2")
                .zoneType(ZoneType.BUSINESS)
                .areaFactor(2.0)
                .build();
        double a2 = z2.getArea();
        check(a2 >= 2.0 * 25.0 && a2 <= 2.0 * 50.0,
                "BUSINESS area should be within [50, 100] for factor=2.0, got " + a2);

        // 3) setType recomputes area
        Zone z3 = Zone.builder("Z3").zoneType(ZoneType.BUSINESS).build();
        double before = z3.getArea();
        z3.setType(ZoneType.BUSINESS);
        double after = z3.getArea();
        check(after > 0.0, "area should be > 0 after setType");
        check(before != after, "area should likely change after re-randomization");

        // 4) explicit override respected
        Zone z4 = Zone.builder("Z4").zoneType(ZoneType.BUSINESS).area(1234.0).build();
        check(Math.abs(z4.getArea() - 1234.0) < 1e-6, "explicit area override should win");

        System.out.println("ZoneTest: OK");
    }

    private static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
    }
}
