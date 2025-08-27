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
import java.util.HashMap;
import java.util.Map;

import com.mars_sim.core.structure.Settlement;
import com.mars_sim.ui.swing.ImageLoader;

/**
 * A settlement map layer for displaying background tile images.
 * It handles translation, rotation, and scaling of the tiles.
 */
public class BackgroundTileMapLayer implements SettlementMapLayer {

	// Static members.
	// This pointer prefix points to an image file in icons.properties
	private static final String MAP_TILE_POINTER = "map_tile/";
	
	// Data members.
	private Map<Settlement, String> settlementBackgroundMap;
	private Image backgroundTileImage;
	private Settlement currentSettlement;
	private double scaleCache;
	private SettlementMapPanel mapPanel;

	/**
	 * Constructor.
	 */
	public BackgroundTileMapLayer(SettlementMapPanel mapPanel) {
		this.mapPanel = mapPanel;
		settlementBackgroundMap = new HashMap<>();
	}

	@Override
	public void displayLayer(Settlement settlement, MapViewPoint viewpoint) {

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

		// Clear background tile image is scale has changed.
		if (scaleCache != scale) {
			backgroundTileImage = null;
			scaleCache = scale;
		}

		// Rotate graphics context.
		g2d.rotate(rotation, mapWidth / 2D, mapHeight / 2D);

		double diagonal = Math.hypot(mapWidth, mapHeight);

		Image backgroundTileIcon = null;
		
		if (backgroundTileImage == null) {
		    backgroundTileIcon = getBackgroundImage(settlement);
		    if (backgroundTileIcon == null) {
		    	return;
		    }
		    
		    double imageScale = scale / SettlementMapPanel.DEFAULT_SCALE;
		    int tileWidth = (int) Math.round(backgroundTileIcon.getWidth(mapPanel) * imageScale);
		    int tileHeight = (int) Math.round(backgroundTileIcon.getHeight(mapPanel) * imageScale);

			// No Image observer so assuming image has already loaded from file
			backgroundTileImage = resizeImage(
		            backgroundTileIcon, 
		            mapPanel,
		            tileWidth, tileHeight
		            );
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

			for (int x = startX; x < endX; x+= tileWidth) {

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

				for (int y = startY; y < endY; y+= tileHeight) {
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
	 * Creates a resized instance of a background image.
	 * 
	 * @param image the original background tile image.
	 * @param scaleWidth the resized image tile width.
	 * @param scaleHeight the resized image tile height.
	 * @return image with the new size.
	 */
	private Image resizeImage(Image image, ImageObserver observer, int scaleWidth, int scaleHeight) {

		int w = image.getWidth(observer);
		int h = image.getHeight(observer);
    
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
		
		BufferedImage tmpImage = new BufferedImage(bufferWidth, bufferHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = (Graphics2D) tmpImage.getGraphics();
		
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		
		g2d.setClip(0, 0, bufferWidth, bufferHeight);
		g2d.drawImage(image, xOffset, yOffset, w, h, observer);
		
		tmpImage.flush();
		g2d.dispose();
	
		return tmpImage;
	}

	/**
	 * Gets the background tile image icon for a settlement.
	 * 
	 * @param settlement the settlement to display.
	 * @return the background tile image icon or null if none found.
	 */
	private Image getBackgroundImage(Settlement settlement) {
		String backgroundImageName = null;

		if (settlementBackgroundMap.containsKey(settlement)) {
			backgroundImageName = settlementBackgroundMap.get(settlement);
		}
		else {
			int id = settlement.getMapImageID();
			backgroundImageName = MAP_TILE_POINTER + id;
			settlementBackgroundMap.put(settlement, backgroundImageName);
		}

		return ImageLoader.getImage(backgroundImageName);
	}

	@Override
	public void destroy() {
		settlementBackgroundMap.clear();
		settlementBackgroundMap = null;
		backgroundTileImage = null;
		currentSettlement = null;
		mapPanel = null;
	}
}
