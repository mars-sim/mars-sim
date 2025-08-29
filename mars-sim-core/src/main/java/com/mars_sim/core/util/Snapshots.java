+/*
+ * Mars Simulation Project
+ * Snapshots.java
+ * Utility: CME-safe snapshot helpers
+ */
+package com.mars_sim.core.util;
+
+import java.util.ArrayList;
+import java.util.Collection;
+import java.util.Collections;
+import java.util.HashSet;
+import java.util.List;
+import java.util.Set;
+
+/**
+ * Small helpers to create iteration snapshots of live collections.
+ * Using a snapshot prevents ConcurrentModificationException when
+ * writers may add/remove concurrently with readers.
+ */
+public final class Snapshots {
+
+    private Snapshots() {}
+
+    /** Returns an immutable empty list if c is null/empty, otherwise a new ArrayList copy. */
+    public static <T> List<T> list(Collection<? extends T> c) {
+        if (c == null || c.isEmpty()) return Collections.emptyList();
+        return new ArrayList<>(c);
+    }
+
+    /** Returns an immutable empty set if c is null/empty, otherwise a new HashSet copy. */
+    public static <T> Set<T> set(Collection<? extends T> c) {
+        if (c == null || c.isEmpty()) return Collections.emptySet();
+        return new HashSet<>(c);
+    }
+}
diff --git a/mars-sim-core/src/main/java/com/mars_sim/core/person/ai/mission/Mission.java b/mars-sim-core/src/main/java/com/mars_sim/core/person/ai/mission/Mission.java
index 3e5f2c9..b5a1b42 100644
--- a/mars-sim-core/src/main/java/com/mars_sim/core/person/ai/mission/Mission.java
+++ b/mars-sim-core/src/main/java/com/mars_sim/core/person/ai/mission/Mission.java
@@ -1,15 +1,22 @@
 package com.mars_sim.core.person.ai.mission;
 
+import java.util.List;
+import com.mars_sim.core.person.ai.task.util.Worker;
+import com.mars_sim.core.util.Snapshots;
+
 public abstract class Mission {
 
     // ... existing imports, fields, and methods ...
 
     /**
-     * Returns the live list of members.
+     * Returns the live list of members (backed by Mission internals).
      */
     protected abstract List<Worker> getMembers();
 
+    /**
+     * Snapshot of current members for CME-safe iteration.
+     */
+    protected final List<Worker> snapshotMembers() { return Snapshots.list(getMembers()); }
+
     /**
      * Called when a member leaves (implementations typically update lists/state).
      */
     protected abstract void memberLeave(Worker member);
 
     // Example: call sites that iterate members should prefer snapshotMembers()
     // (No behavior change; callers may keep using getMembers() where safe.)
 }
diff --git a/mars-sim-core/src/main/java/com/mars_sim/core/person/ai/mission/EVAMission.java b/mars-sim-core/src/main/java/com/mars_sim/core/person/ai/mission/EVAMission.java
index 9a1ff63..b364ced 100644
--- a/mars-sim-core/src/main/java/com/mars_sim/core/person/ai/mission/EVAMission.java
+++ b/mars-sim-core/src/main/java/com/mars_sim/core/person/ai/mission/EVAMission.java
@@ -6,6 +6,7 @@ package com.mars_sim.core.person.ai.mission;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
+import java.util.ArrayDeque;
 import java.util.Map;
 
 import com.mars_sim.core.equipment.EquipmentType;
@@ -26,6 +27,7 @@ import com.mars_sim.core.vehicle.Rover;
  */
 abstract class EVAMission extends RoverMission {
 
@@ -249,36 +251,62 @@ abstract class EVAMission extends RoverMission {
      * Ensures no "teleported" person is still a member of this mission.
      * Note: still investigating the cause and how to handle this.
      */
     void checkTeleported() {
-
-        for (Iterator<Worker> i = getMembers().iterator(); i.hasNext();) {    
-            Worker member = i.next();
-
-            if (member instanceof Person p
-                && (p.isInSettlement() 
-                || p.isInSettlementVicinity()
-                || p.isRightOutsideSettlement())) {
-
-                logger.severe(p, 10_000, "Invalid 'teleportation' detected. Current location: " 
-                        + p.getLocationTag().getExtendedLocation() + ".");
-                
-                // Use Iterator's remove() method
-//              i.remove();
-                
-                // Call memberLeave to set mission to null will cause this member to drop off the member list
-//              memberLeave(member);
-
-                break;
-            }
-        }
+        // CME-safe: collect evictions, then apply outside the loop.
+        var evict = new ArrayDeque<Worker>();
+        for (Worker m : snapshotMembers()) {
+            if (m instanceof Person p
+                    && (p.isInSettlement()
+                        || p.isInSettlementVicinity()
+                        || p.isRightOutsideSettlement())) {
+                logger.severe(p, 10_000,
+                    "Invalid 'teleportation' detected. Current location: "
+                    + p.getLocationTag().getExtendedLocation() + ".");
+                evict.add(m);
+            }
+        }
+        while (!evict.isEmpty()) {
+            memberLeave(evict.removeFirst());
+        }
     }
 
     // ... remainder of class unchanged ...
 }
diff --git a/mars-sim-core/src/main/java/com/mars_sim/core/structure/Settlement.java b/mars-sim-core/src/main/java/com/mars_sim/core/structure/Settlement.java
index 0f2aa97..af3a8b2 100644
--- a/mars-sim-core/src/main/java/com/mars_sim/core/structure/Settlement.java
+++ b/mars-sim-core/src/main/java/com/mars_sim/core/structure/Settlement.java
@@ -1,11 +1,13 @@
 package com.mars_sim.core.structure;
 
 import java.io.Serializable;
+import java.util.List;
 // ... other imports ...
+import com.mars_sim.core.util.Snapshots;
 import com.mars_sim.core.time.ClockPulse;
 import com.mars_sim.core.person.Person;
 
 // class Settlement { ... }
 
@@ -940,6 +942,7 @@ public class Settlement implements Serializable /*, Temporal? */ {
     // called by timePassing(...)
     private void timePassingCitizens(ClockPulse pulse) {
-        for (Person p : getPeople()) {
+        // CME-safe snapshot iteration; writers may add/remove residents concurrently.
+        for (Person p : Snapshots.list(getPeople())) {
             try {
                 p.timePassing(pulse);
             }
@@ -949,6 +952,38 @@ public class Settlement implements Serializable /*, Temporal? */ {
             }
         }
     }
+
+    /*
+     * NOTE:
+     * If your tree does not have a getPeople() accessor, adapt the snapshot call:
+     *   for (Person p : Snapshots.list(population.getResidents())) { ... }
+     * or
+     *   for (Person p : Snapshots.list(residents)) { ... }
+     * The key change is: iterate a *copy* to avoid ConcurrentModificationException.
+     */
 
     // ... rest of Settlement ...
 }
diff --git a/mars-sim-core/src/main/java/com/mars_sim/core/UnitManager.java b/mars-sim-core/src/main/java/com/mars_sim/core/UnitManager.java
index 2c7a6b1..a2d1f54 100644
--- a/mars-sim-core/src/main/java/com/mars_sim/core/UnitManager.java
+++ b/mars-sim-core/src/main/java/com/mars_sim/core/UnitManager.java
@@ -22,7 +22,7 @@ import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.RejectedExecutionException;
 import java.util.stream.Collectors;
-
+import java.util.concurrent.ConcurrentHashMap;
 import com.google.common.util.concurrent.ThreadFactoryBuilder;
 import com.mars_sim.core.building.Building;
 import com.mars_sim.core.building.construction.ConstructionSite;
@@ -71,7 +71,7 @@ public class UnitManager implements Serializable, Temporal {
     private transient TemporalExecutor executor;
 
     /** Map of equipment types and their numbers. */
-    private Map<String, Integer> unitCounts = new HashMap<>();
+    private final ConcurrentHashMap<String, Integer> unitCounts = new ConcurrentHashMap<>();
@@ -220,11 +220,9 @@ public class UnitManager implements Serializable, Temporal {
      * @param name
      * @return
      */
     public int incrementTypeCount(String name) {
-        synchronized (unitCounts) {
-            return unitCounts.merge(name, 1, Integer::sum);
-        }
+        return unitCounts.merge(name, 1, Integer::sum);
     }
 
     // ...
@@ -365,10 +363,10 @@ public class UnitManager implements Serializable, Temporal {
      * @return
      */
     public float getObjectsLoad() {
-        return (.45f * lookupPerson.entrySet().stream().count() 
-                + .2f * lookupRobot.entrySet().stream().count()
-                + .25f * lookupBuilding.entrySet().stream().count()
-                + .1f * lookupVehicle.entrySet().stream().count()
+        return (.45f * lookupPerson.size()
+                + .2f * lookupRobot.size()
+                + .25f * lookupBuilding.size()
+                + .1f * lookupVehicle.size()
                 );
     }
