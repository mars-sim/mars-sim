/*
 * Mars Simulation Project
 * SVGGraphicNodeIcon.java
 * @date 2023-02-25
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.tool.svg;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.RenderingHints;

import java.util.HashMap;
import java.util.Map;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;


import javax.swing.Icon;

import org.apache.batik.gvt.GraphicsNode;

/**
 * This class takes a SVG Graphics node and maps it into a Icon. It auto scales the original image
 * and preserves the aspect ratio.
 */
public class SVGGraphicNodeIcon implements Icon {

    private GraphicsNode source;
    private int width;
    private int height;
    private boolean rotate;
    private static Map<GraphicsNode, BufferedImage> svgImageCache = new HashMap<>();

    /**
     * @param rotate Rotate the source image to fit
     */
    public SVGGraphicNodeIcon(GraphicsNode source, int width, int height, boolean rotate) {
        this.source = source;
        this.width = width;
        this.height = height;
        this.rotate = rotate;
    }



    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {

        Graphics2D g2d = (Graphics2D) g;

        BufferedImage image = getBufferedImage(source, width, height, rotate);
        if (image != null) {
            g2d.drawImage(image, x, y, c);
        }
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }
  
    /**
	 * Gets a buffered image for a given graphics node.
	 * @param svg the graphics node.
	 * @param width the target width.
	 * @param height the target length.
     * @param rotate Rotate the source image
	 * @return buffered image.
	 */
	private BufferedImage getBufferedImage(GraphicsNode svg, double width, double height, boolean rotate) {

		// Get image from image cache or create it if it doesn't exist.
		BufferedImage image = null;
		if (svgImageCache.containsKey(svg))
            image = svgImageCache.get(svg);
		else {
            if (rotate) {
                image = createBufferedImage(svg, height, width);
                image = rotateImageByDegrees(image, 90);
            }
            else {
                image = createBufferedImage(svg, width, height);
            }
			svgImageCache.put(svg, image);
		}

		return image;
	}

	/**
	 * Creates a buffered image from a SVG graphics node.
	 * @param svg the SVG graphics node.
	 * @param width the width of the produced image.
	 * @param length the length of the produced image.
	 * @return the created buffered image.
	 */
	private BufferedImage createBufferedImage(GraphicsNode svg, double width, double length) {

		BufferedImage bufferedImage = new BufferedImage(
			(int)width, (int)length, 
			BufferedImage.TYPE_INT_ARGB
		);

		// Determine bounds.
		Rectangle2D bounds = svg.getBounds();

		// Determine transform information.
		double scalingWidth = width / bounds.getWidth();
		double scalingLength = length / bounds.getHeight();
        double scaling = Math.min(scalingWidth, scalingLength);
                                            

		// Draw the SVG image on the buffered image.
		Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        AffineTransform tx = new AffineTransform();
        tx.setToScale(scaling, scaling);

        // In the co-ordinate frame of the source, not the destination
        double yOffset = ((length/scaling) - bounds.getHeight())/2;
        double xOffset = ((width/scaling) - bounds.getWidth())/2;

        tx.translate(xOffset, yOffset);
        svg.setTransform(tx);
		svg.paint(g2d);

		// Cleanup and return image
		g2d.dispose();

		return bufferedImage;
	}

    private BufferedImage rotateImageByDegrees(BufferedImage img, double angle) {

        double rads = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(rads)), cos = Math.abs(Math.cos(rads));
        int w = img.getWidth();
        int h = img.getHeight();
        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotated.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - w) / 2D, (newHeight - h) / 2D);

        int x = w / 2;
        int y = h / 2;

        at.rotate(rads, x, y);
        g2d.setTransform(at);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        return rotated;
    }
}
