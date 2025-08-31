+/*
+ * Mars Simulation Project
+ * Snapshots.java
+ * Utility: tiny helpers for CME-safe iteration
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
+/** Small helpers to create iteration snapshots of live collections. */
+public final class Snapshots {
+    private Snapshots() {}
+
+    /** Returns an immutable empty list if c is null/empty, else a fresh ArrayList copy. */
+    public static <T> List<T> list(Collection<? extends T> c) {
+        if (c == null || c.isEmpty()) return Collections.emptyList();
+        return new ArrayList<>(c);
+    }
+
+    /** Returns an immutable empty set if c is null/empty, else a fresh HashSet copy. */
+    public static <T> Set<T> set(Collection<? extends T> c) {
+        if (c == null || c.isEmpty()) return Collections.emptySet();
+        return new HashSet<>(c);
+    }
+}
