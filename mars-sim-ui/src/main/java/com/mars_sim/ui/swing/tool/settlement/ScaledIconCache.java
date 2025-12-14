/*
 * Mars Simulation Project
 * ScaledIconCache.java
 * @date 2025-11-18
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.settlement;

import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Small LRU cache for rasterized icons/images keyed by (resourceId, scale).
 * Uses {@link SoftReference} values so the GC can reclaim under pressure.
 * <p>
 * This is a hook for map layers (e.g., buildings, vehicles) to reuse
 * rasterizations at the active {@link SettlementMapPanel#getScale()} rather than
 * recreating the same {@link BufferedImage} repeatedly while the user drags
 * the zoom slider.
 * </p>
 */
class ScaledIconCache {
	private static final int MAX_ENTRIES = 256;

	private static final class Key {
		final String resourceId;
		final double scale;

		Key(String resourceId, double scale) {
			this.resourceId = resourceId;
			this.scale = scale;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof Key)) return false;
			Key k = (Key) o;
			return Double.doubleToLongBits(scale) == Double.doubleToLongBits(k.scale)
					&& resourceId.equals(k.resourceId);
		}

		@Override
		public int hashCode() {
			long bits = Double.doubleToLongBits(scale);
			return 31 * resourceId.hashCode() + (int) (bits ^ (bits >>> 32));
		}
	}

	private final LinkedHashMap<Key, SoftReference<BufferedImage>> cache =
			new LinkedHashMap<>(64, 0.75f, true) {
				@Override
				protected boolean removeEldestEntry(Map.Entry<Key, SoftReference<BufferedImage>> e) {
					return size() > MAX_ENTRIES;
				}
			};

	public synchronized BufferedImage get(String id, double scale) {
		SoftReference<BufferedImage> ref = cache.get(new Key(id, scale));
		return ref != null ? ref.get() : null;
	}

	public synchronized void put(String id, double scale, BufferedImage img) {
		cache.put(new Key(id, scale), new SoftReference<>(img));
	}

	public synchronized void clear() {
		cache.clear();
	}
}