*** /dev/null
--- b/mars-sim-core/src/main/java/com/mars_sim/core/util/Snapshots.java
@@
+/*
+ * Mars Simulation Project
+ * Snapshots.java
+ * @date 2025-08-31
+ * Utility to take CME-safe snapshots of live collections.
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
+public final class Snapshots {
+    private Snapshots() {}
+
+    /** Returns an immutable list snapshot of a collection (or the shared empty list). */
+    public static <T> List<T> list(Collection<? extends T> src) {
+        if (src == null || src.isEmpty()) return Collections.emptyList();
+        return Collections.unmodifiableList(new ArrayList<>(src));
+    }
+
+    /** Returns an immutable set snapshot of a collection (or the shared empty set). */
+    public static <T> Set<T> set(Collection<? extends T> src) {
+        if (src == null || src.isEmpty()) return Collections.emptySet();
+        return Collections.unmodifiableSet(new HashSet<>(src));
+    }
+}
