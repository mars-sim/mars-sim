package com.mars_sim.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Helpers to create iteration snapshots of live collections to avoid CME.
 */
public final class Snapshots {
    private Snapshots() { }

    /** Returns an immutable empty list if null/empty, otherwise a fresh copy. */
    public static <T> List<T> list(Collection<? extends T> c) {
        if (c == null || c.isEmpty()) return Collections.emptyList();
        return new ArrayList<>(c);
    }

    /** Returns an immutable empty set if null/empty, otherwise a fresh copy. */
    public static <T> Set<T> set(Collection<? extends T> c) {
        if (c == null || c.isEmpty()) return Collections.emptySet();
        return new HashSet<>(c);
    }
}
