/*
 * Mars Simulation Project
 * BackgroundTileMapLayer.java
 * @date 2022-07-15
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.HashMap;
import java.util.Map;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.ui.swing.ImageLoader;

/**
 * A settlement map layer for displaying background tile images.
 * It handles translation, rotation, and scaling of the tiles.
 */
public class BackgroundTileMapLayer
implements SettlementMapLayer {

	// Static members.
	private static final int MAX_BACKGROUND_IMAGE_NUM = 20;
	private static final int MAX_BACKGROUND_DIMENSION = 1600;

	private static final String MAP_TILE = "settlement_map/";
	
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
	public void displayLayer(Graphics2D g2d, Settlement settlement, Building building, double xPos, 
			double yPos, int mapWidth, int mapHeight, double rotation, double scale) {

		// Save original graphics transforms.
		AffineTransform saveTransform = g2d.getTransform();

		// Clear background tile image if settlement has changed.
		if (settlement != null && !settlement.equals(currentSettlement)) {
			backgroundTileImage = null;
			currentSettlement = settlement;
			scaleCache = -1;
		}

		// Clear background tile image is scale has changed.
		if (scaleCache != scale) {
			backgroundTileImage = null;
			scaleCache = scale;
		}

		// Rotate graphics context.
		g2d.rotate(rotation, mapWidth / 2D, mapHeight / 2D);

		double diagonal = Math.hypot(mapWidth, mapHeight);

		ImageObserver imageObserver = null;

		if (backgroundTileImage == null) {
		    Image backgroundTileIcon = getBackgroundImage(settlement);
		    if (backgroundTileIcon == null) {
		    	return;
		    }
		    
		    double imageScale = scale / SettlementMapPanel.DEFAULT_SCALE;
		    int imageWidth = (int) Math.round(backgroundTileIcon.getWidth(imageObserver) * imageScale);
		    int imageHeight = (int) Math.round(backgroundTileIcon.getHeight(imageObserver) * imageScale);

			// No Image observer so assuming image has already loaded from file
			backgroundTileImage = resizeImage(
		            backgroundTileIcon, 
		            imageObserver,
		            imageWidth, imageHeight
		            );
		}

		if (backgroundTileImage != null) {

			int offsetX = (int) Math.round(xPos * scale);
			int tileWidth = backgroundTileImage.getWidth(mapPanel);
			int bufferX = (int) Math.round(diagonal - mapWidth);
			int tileCenterOffsetX = (int) Math.round((mapWidth / 2) % tileWidth - 1.5F * tileWidth);

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

				int offsetY = (int) Math.round(yPos * scale);
				int tileHeight = backgroundTileImage.getHeight(mapPanel);
				int bufferY = (int) Math.round(diagonal - mapHeight);
				int tileCenterOffsetY = (int) Math.round((mapHeight / 2) % tileHeight - 1.5F * tileHeight);

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
	 * @param image the original background image.
	 * @param width the resized image width.
	 * @param height the resized image height.
	 * @return image with the new size.
	 */
	private Image resizeImage(Image image, ImageObserver observer, int width, int height) {
		Image result = image;

		int w = image.getWidth(observer);
		int h = image.getHeight(observer);

		do {
			if (w > width) {
				w /= 2;
				if (w < width) w = width;
			}
			else if (w < width) {
				w = width;
			}

			if (h > height) {
				h /= 2;
				if (h < height) h = height;
			}
			else if (h < height) {
				h = height;
			}

			int bufferWidth = w;
			int bufferHeight = h;
			int xOffset = 0;
			int yOffset = 0;
			if ((w > MAX_BACKGROUND_DIMENSION) || (h > MAX_BACKGROUND_DIMENSION)) {
				float reductionW = (float) MAX_BACKGROUND_DIMENSION / (float) w;
				float reductionH = (float) MAX_BACKGROUND_DIMENSION / (float) h;
				float reduction = Math.min(reductionH, reductionW);

				bufferWidth = (int) (w * reduction);
				bufferHeight = (int) (h * reduction);

				xOffset = (w - bufferWidth) / -2;
				yOffset = (h - bufferHeight) / -2;
			}

			BufferedImage tmpImage = new BufferedImage(bufferWidth, bufferHeight, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = (Graphics2D) tmpImage.getGraphics();
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2d.setClip(0, 0, bufferWidth, bufferHeight);
			g2d.drawImage(result, xOffset, yOffset, w, h, null);
			g2d.dispose();

			result = tmpImage;

		} while ((w != width) || (h != height));

		return result;
	}

	/**
	 * Gets the background tile image icon for a settlement.
	 * 
	 * @param settlement the settlement to display.
	 * @return the background tile image icon or null if none found.
	 */
	private Image getBackgroundImage(Settlement settlement) {
		Image result = null;

		if (settlementBackgroundMap.containsKey(settlement)) {
			String backgroundImageName = settlementBackgroundMap.get(settlement);
			result = ImageLoader.getImage(backgroundImageName);
		}
		else {
			int count = settlementBackgroundMap.size() + 1;
			count = count % MAX_BACKGROUND_IMAGE_NUM;

			String backgroundImageName = MAP_TILE + count;
			settlementBackgroundMap.put(settlement, backgroundImageName);
			result = ImageLoader.getImage(backgroundImageName);
		}

		return result;
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
