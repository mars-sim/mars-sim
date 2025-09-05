/*
 * Mars Simulation Project
 * ColorLegendFactory.java
 * @date 2025-06-01
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.map;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Factory class to create coloured legend icons.
 * These are cached.
 */
final class ColorLegendFactory {
    public static final int ICON_SIZE = 12;
    
    private static Map<Color,Icon> icons = new HashMap<>();

    private ColorLegendFactory() {
        // Stop constructor
    }

    /**
	 * Get an icon representing a color.
	 * 
	 * @param color            the color for the icon.
	 * @param displayComponent the component to display the icon on.
	 * @return the color icon.
	 */
	public static Icon getLegend(Color color, Component displayComponent) {
        return icons.computeIfAbsent(color, c -> createColorLegendIcon(c, displayComponent));
    }

	/**
	 * Creates an icon representing a color.
	 * 
	 * @param color            the color for the icon.
	 * @param displayComponent the component to display the icon on.
	 * @return the color icon.
	 */
	private static Icon createColorLegendIcon(Color color, Component displayComponent) {

		return new ImageIcon(createColorSquare(color, displayComponent));
	}

	private static Image createColorSquare(Color color, Component displayComponent) {
				int[] imageArray = new int[ICON_SIZE * ICON_SIZE];
		Arrays.fill(imageArray, color.getRGB());
		return displayComponent.createImage(new MemoryImageSource(ICON_SIZE, ICON_SIZE, imageArray, 0, 10));
	}

	/**
	 * Add a background to an Icon
	 * 
	 * @param color            the color for the icon.
	 * @param displayComponent the component to display the icon on.
	 * @return the color icon.
	 */
	public static Icon addBackground(Color color, Icon overlap, Component displayComponent) {
		Image image = createColorSquare(color, displayComponent);

		Image overlapImage = ((ImageIcon)overlap).getImage();
		var xOffset = (ICON_SIZE - overlapImage.getWidth(displayComponent))/2;
		var yOffset = (ICON_SIZE - overlapImage.getHeight(displayComponent))/2;

		BufferedImage combined = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);

		// paint both images, preserving the alpha channels
		var g = combined.getGraphics();
		g.drawImage(image, 0, 0, null);
		g.drawImage(overlapImage, xOffset, yOffset, null);

		combined.flush();
		g.dispose();
		return new ImageIcon(combined);
	}
}
