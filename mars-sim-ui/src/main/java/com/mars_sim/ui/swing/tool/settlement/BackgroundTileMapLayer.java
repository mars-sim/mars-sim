/*
 * Mars Simulation Project
 * BackgroundTileMapLayer.java
 * @date 2022-07-15
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.settlement;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.ui.swing.ImageLoader;

/**
 * A settlement map layer for displaying background tile images.
 * It handles translation, rotation, and scaling of the tiles.
 *
 * <p>Enhancements:
 * <ul>
 *   <li>Bounded LRU cache of scaled background tiles keyed by (imageName, width, height).</li>
 *   <li>Explicit disposal method to promptly release cached images.</li>
 *   <li>Fixed resizeImage(): avoid flushing the freshly created image and use createGraphics().</li>
 * </ul>
 * </p>
 */
public class BackgroundTileMapLayer implements SettlementMapLayer {

	// Static members.
	// This pointer prefix points to an image file in icons.properties
	private static final String MAP_TILE_POINTER = "map_tile/";

	/** Max number of scaled tiles kept in memory (per layer). Tune with profiling. */
	private static final int MAX_CACHE_ENTRIES = 32;

	// Data members.
	private double scaleCache;
	
	private Map<Settlement, String> settlementBackgroundMap;
	private Image backgroundTileImage;
	
	private Settlement currentSettlement;

	private SettlementMapPanel mapPanel;
	
	private MasterClock masterClock;
	
	/** LRU cache for scaled tiles. */
	private final Map<CacheKey, SoftReference<BufferedImage>> scaledTileCache =
		Collections.synchronizedMap(new LinkedHashMap<>(32, 0.75f, true) {
			private static final long serialVersionUID = 1L;
			@Override
			protected boolean removeEldestEntry(Map.Entry<CacheKey, SoftReference<BufferedImage>> eldest) {
				return size() > MAX_CACHE_ENTRIES;
			}
		});

	/** Key for scaled tile cache. */
	private static final class CacheKey {
		final String imageName;
		final int width;
		final int height;

		CacheKey(String imageName, int width, int height) {
			this.imageName = imageName;
			this.width = width;
			this.height = height;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof CacheKey)) return false;
			CacheKey k = (CacheKey) o;
			return width == k.width && height == k.height && Objects.equals(imageName, k.imageName);
		}

		@Override
		public int hashCode() {
			return Objects.hash(imageName, width, height);
		}
	}

	/**
	 * Constructor.
	 */
	public BackgroundTileMapLayer(SettlementMapPanel mapPanel) {
		this.mapPanel = mapPanel;
		settlementBackgroundMap = new LinkedHashMap<>();
		
		masterClock = mapPanel.getDesktop().getSimulation().getMasterClock();
	}

	@Override
	public void displayLayer(Settlement settlement, MapViewPoint viewpoint) {

		if (masterClock.isPaused()) return; 
		
		// Save original graphics transforms.
		var g2d = viewpoint.graphics();
		AffineTransform saveTransform = g2d.getTransform();

		double scale = viewpoint.scale();
		int mapHeight = viewpoint.mapHeight();
		int mapWidth = viewpoint.mapWidth();
		double rotation = viewpoint.rotation();

		// Clear background tile image if settlement has changed.
		if (settlement != null && !settlement.equals(currentSettlement)) {
			backgroundTileImage = null;
			currentSettlement = settlement;
			scaleCache = scale;
		}

		// Clear background tile image if scale has changed (we will reuse/bound via cache).
		if (scaleCache != scale) {
			backgroundTileImage = null;
			scaleCache = scale;
		}

		// Rotate graphics context.
		g2d.rotate(rotation, mapWidth / 2D, mapHeight / 2D);

		double diagonal = Math.hypot(mapWidth, mapHeight);

		Image backgroundTileIcon = null;

		if (backgroundTileImage == null) {
			// Resolve the background image and compute the scaled tile size.
			String imageName = getBackgroundImageName(settlement);
			if (imageName == null) {
				// Restore original transform before exiting.
				g2d.setTransform(saveTransform);
				return;
			}
			backgroundTileIcon = ImageLoader.getImage(imageName);
			if (backgroundTileIcon == null) {
				g2d.setTransform(saveTransform);
				return;
			}

			double imageScale = scale / SettlementMapPanel.DEFAULT_SCALE;
			int tileWidth = (int) Math.round(backgroundTileIcon.getWidth(mapPanel) * imageScale);
			int tileHeight = (int) Math.round(backgroundTileIcon.getHeight(mapPanel) * imageScale);

			// Guard against invalid sizes.
			if (tileWidth <= 0 || tileHeight <= 0) {
				g2d.setTransform(saveTransform);
				return;
			}

			// Look up or create a scaled tile image from the bounded cache.
			CacheKey key = new CacheKey(imageName, tileWidth, tileHeight);
			backgroundTileImage = getOrCreateScaledTile(key, backgroundTileIcon, mapPanel, tileWidth, tileHeight);
		}

		if (backgroundTileImage != null) {

			int offsetX = (int) Math.round(viewpoint.xPos() * scale);
			int tileWidth = backgroundTileImage.getWidth(mapPanel);
			int bufferX = (int) Math.round(diagonal - mapWidth);
			int tileCenterOffsetX = (int) Math.round((mapWidth / 2D) % tileWidth - 1.5F * tileWidth);

			// Calculate starting X position for drawing tile.
			int startX = tileCenterOffsetX;
			while ((startX + offsetX) > (-bufferX)) {
				startX -= tileWidth;
			}
			while ((startX + offsetX) < (-tileWidth - bufferX)) {
				startX += tileWidth;
			}

			// Calculate ending X position for drawing tile.
			int endX = mapWidth;
			while ((endX + offsetX) < (mapWidth + bufferX)) {
				endX += tileWidth;
			}
			while ((endX + offsetX) > (mapWidth + tileWidth + bufferX)) {
				endX -= tileWidth;
			}

			for (int x = startX; x < endX; x += tileWidth) {

				int offsetY = (int) Math.round(viewpoint.yPos() * scale);
				int tileHeight = backgroundTileImage.getHeight(mapPanel);
				int bufferY = (int) Math.round(diagonal - mapHeight);
				int tileCenterOffsetY = (int) Math.round((mapHeight / 2D) % tileHeight - 1.5F * tileHeight);

				// Calculate starting Y position for drawing tile.
				int startY = tileCenterOffsetY;
				while ((startY + offsetY) > (-bufferY)) {
					startY -= tileHeight;
				}
				while ((startY + offsetY) < (-tileHeight - bufferY)) {
					startY += tileHeight;
				}

				// Calculate ending Y position for drawing tile.
				int endY = mapHeight;
				while ((endY + offsetY) < (mapHeight + bufferY)) {
					endY += tileHeight;
				}
				while ((endY + offsetY) > (mapHeight + tileHeight + bufferY)) {
					endY -= tileHeight;
				}

				for (int y = startY; y < endY; y += tileHeight) {
					// Draw tile image.
					g2d.drawImage(backgroundTileImage,
							(x + offsetX),
							(y + offsetY),
							mapPanel);
				}
			}
		}

		// Restore original graphic transforms.
		g2d.setTransform(saveTransform);
	}

	/**
	 * Creates (or retrieves from cache) a scaled instance of a background image.
	 */
	private BufferedImage getOrCreateScaledTile(CacheKey key, Image source, ImageObserver observer,
	                                            int scaleWidth, int scaleHeight) {
		SoftReference<BufferedImage> ref = scaledTileCache.get(key);
		BufferedImage cached = (ref != null) ? ref.get() : null;
		if (cached != null) {
			return cached;
		}
		BufferedImage created = (BufferedImage) resizeImage(source, observer, scaleWidth, scaleHeight);
		scaledTileCache.put(key, new SoftReference<>(created));
		return created;
	}

	/**
	 * Creates a resized instance of a background image.
	 *
	 * @param image the original background tile image.
	 * @param observer image observer (for async loading if any).
	 * @param scaleWidth the resized image tile width.
	 * @param scaleHeight the resized image tile height.
	 * @return image with the new size.
	 */
	private Image resizeImage(Image image, ImageObserver observer, int scaleWidth, int scaleHeight) {

		int w = image.getWidth(observer);
		int h = image.getHeight(observer);

		// Defensive: handle images not yet loaded.
		if (w <= 0 || h <= 0) {
			return null;
		}

		// Iteratively approach the target size (keeps quality decent and perf reasonable).
		do {
			if (w > scaleWidth) {
				w /= 2;
				if (w < scaleWidth) w = scaleWidth;
			}
			else if (w < scaleWidth) {
				w = scaleWidth;
			}

			if (h > scaleHeight) {
				h /= 2;
				if (h < scaleHeight) h = scaleHeight;
			}
			else if (h < scaleHeight) {
				h = scaleHeight;
			}

		} while ((w != scaleWidth) || (h != scaleHeight));

		int bufferWidth = w;
		int bufferHeight = h;
		int xOffset = 0;
		int yOffset = 0;

		// Use ARGB to preserve transparency if present in source.
		BufferedImage tmpImage = new BufferedImage(bufferWidth, bufferHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = tmpImage.createGraphics();

		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.setClip(0, 0, bufferWidth, bufferHeight);
		g2d.drawImage(image, xOffset, yOffset, w, h, observer);

		g2d.dispose();
		// NOTE: Do NOT call tmpImage.flush() here; we are returning this image.

		return tmpImage;
	}

	/**
	 * Returns the background image name for a settlement, caching the mapping.
	 */
	private String getBackgroundImageName(Settlement settlement) {
		if (settlement == null) return null;

		if (settlementBackgroundMap.containsKey(settlement)) {
			return settlementBackgroundMap.get(settlement);
		}
		int id = settlement.getMapImageID();
		String backgroundImageName = MAP_TILE_POINTER + id;
		settlementBackgroundMap.put(settlement, backgroundImageName);
		return backgroundImageName;
	}

	/**
	 * Gets the background tile image icon for a settlement.
	 *
	 * @param settlement the settlement to display.
	 * @return the background tile image icon or null if none found.
	 */
	private Image getBackgroundImage(Settlement settlement) {
		String backgroundImageName = getBackgroundImageName(settlement);
		return (backgroundImageName != null) ? ImageLoader.getImage(backgroundImageName) : null;
	}

	/** Clears and flushes the scaled tile cache. */
	private void clearScaledTileCache() {
		synchronized (scaledTileCache) {
			for (SoftReference<BufferedImage> ref : scaledTileCache.values()) {
				BufferedImage bi = (ref != null) ? ref.get() : null;
				if (bi != null) {
					bi.flush();
				}
			}
			scaledTileCache.clear();
		}
	}

	/** Public dispose hook for owners that want explicit cleanup. */
	public void dispose() {
		clearScaledTileCache();
		backgroundTileImage = null;
	}

	@Override
	public void destroy() {
		// Dispose heavy resources first.
		dispose();

		if (settlementBackgroundMap != null) {
			settlementBackgroundMap.clear();
			settlementBackgroundMap = null;
		}
		currentSettlement = null;
		mapPanel = null;
	}
}
