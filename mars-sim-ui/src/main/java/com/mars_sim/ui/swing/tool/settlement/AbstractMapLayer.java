/**
 * Mars Simulation Project
 * AbstractMapLayer.java
 * @date 2023-12-10
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.settlement;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import com.mars_sim.mapdata.location.LocalPosition;

/**
 * This class provides basic method to draw on the Settlement Map panel.
 */
public abstract class AbstractMapLayer implements SettlementMapLayer {
    // A data record to represent a color choice.
    protected record ColorChoice(Color text, Color outline) {}

    private Map<String, BufferedImage> labelImageCache = new HashMap<>();

    /**
	 * Draws an oval at a settlement.
	 * 
	 * @param g2d the graphics context.
	 * @param pos Position to draw oval.
     * @param color Color choice
     * @param rotation The current rotation
     * @param scale Map scale
	 */
	protected void drawOval(Graphics2D g2d, LocalPosition pos, ColorChoice color,
                            double rotation, double scale) {

		int size = (int)(Math.round(scale / 3.0));
		size = Math.max(size, 1);
				
		double radius = size / 2.0;
		
		// Save original graphics transforms.
		AffineTransform saveTransform = g2d.getTransform();

		double translationX = -1.0 * pos.getX() * scale - radius;
		double translationY = -1.0 * pos.getY() * scale - radius;

		// Apply graphic transforms for label.
		AffineTransform newTransform = new AffineTransform(saveTransform);
		newTransform.translate(translationX, translationY);
		newTransform.rotate(rotation * -1D, radius, radius);
		g2d.setTransform(newTransform);
		
		// Set circle color.
		g2d.setColor(color.text());

		// Draw circle
		g2d.fillOval(0, 0, size, size);

		// Restore original graphic transforms.
		g2d.setTransform(saveTransform);
	}

    
	/**
	 * Draws a label to the right of an X, Y location.
	 * 
	 * @param g2d the graphics 2D context.
	 * @param isSelected
	 * @param label the label string.
	 * @param loc the location from center of settlement (meters).
	 * @param labelColor the color of the label.
	 * @param xOffset the X pixel offset from the center point.
	 * @param yOffset the Y pixel offset from the center point.
     * @param scale Map scale
	 */
	protected void drawRightLabel(
		Graphics2D g2d, boolean isSelected, String label, LocalPosition loc,
		ColorChoice labelColor, Font labelFont, float xOffset, float yOffset,
        double rotation, double scale) {

		float fontSizeIncrease = Math.round(scale / 2.5);
//		float size = (float)(Math.min(labelFont.getSize() * scale / 100.0, 1.1));
		
		// Save original graphics transforms.
		AffineTransform saveTransform = g2d.getTransform();
		Font saveFont = g2d.getFont();

		// Get the label image.
		Font font = new Font(labelFont.getName(), labelFont.getStyle(),
                             labelFont.getSize() + (int)fontSizeIncrease); 
		g2d.setFont(font);
		
		BufferedImage labelImage = getLabelImage(
			label, font, g2d.getFontRenderContext(),
			labelColor, false
		);

		// Determine transform information.
		double centerX = labelImage.getWidth() / 2;
		double centerY = labelImage.getHeight() / 2;
		double translationX = (-1 * loc.getX() * scale) - centerX;
		double translationY = (-1 * loc.getY() * scale) - centerY;

		// Apply graphic transforms for label.
		AffineTransform newTransform = new AffineTransform(saveTransform);
		newTransform.translate(translationX, translationY);
		newTransform.rotate(rotation * -1D, centerX, centerY);
		g2d.setTransform(newTransform);

		// Gets the width and height offset
		int widthOffset  = (int)Math.round((centerX + fontSizeIncrease) + xOffset);
		int heightOffset = (int)Math.round((centerY + fontSizeIncrease) + yOffset);
		
		if (isSelected) {
			// Draw a white background label
	        g2d.setColor(Color.gray.brighter().brighter().brighter());//.darker().darker().darker());

	        int x = widthOffset; // (int)Math.round(widthOffset * .975);
	        int y = heightOffset;
	       	int w = (int)Math.round(centerX * 2.025);
	       	int h = (int)Math.round(centerY * 2.2);
	        
//	    	 g2d.fillRect(x, y, w, h);  
	       	
	        int thickness = 2;
	
//	        // Draw a frame rect white background label
	        g2d.fill3DRect(x, y, w, h, true);
//	        g2d.fill3DRect(x, y, w, h, false);
	        for (int i = 1; i <= thickness; i++)
	            g2d.draw3DRect(x - i, y - i, w + 2 * i - 1, h + 2 * i - 1, true);
	        
			// Draw a white background label
	        g2d.setColor(labelColor.text());
		}
		
        // Draw image label.
		g2d.drawImage(labelImage, widthOffset, heightOffset, null);
		
		// Restore original graphic transforms.
		g2d.setTransform(saveTransform);
		g2d.setFont(saveFont);
	}

    /**
	 * Draws a label centered at the X, Y location.
	 * 
	 * @param g2d the graphics 2D context.
	 * @param label the label string.
	 * @param loc the location from center of settlement (meters).
	 * @param labelColor the color of the label.
	 */
	protected void drawCenteredLabel(
		Graphics2D g2d, String label, Font labelFont, LocalPosition loc,
		ColorChoice labelColor, float yOffset,
        double rotation, double scale) {

		float fontSize = Math.round(scale * 1.1);
		float size = (float) Math.max(fontSize / 30.0, 1.2);
		Font font = new Font(labelFont.getName(), labelFont.getStyle(), (int)fontSize);

		// Save original graphics transforms.
		AffineTransform saveTransform = g2d.getTransform();
		Font saveFont = g2d.getFont();

		// Get the label image.
		g2d.setFont(font);
		
		BufferedImage labelImage = getLabelImage(
			label, font, g2d.getFontRenderContext(),
			labelColor, true
		);

		// Determine transform information.
		double centerX = labelImage.getWidth() / 2D;
		double centerY = labelImage.getHeight() / 2D;
		double translationX = (-1D * loc.getX() * scale) - centerX;
		double translationY = (-1D * loc.getY() * scale) - centerY;

		// Apply graphic transforms for label.
		AffineTransform newTransform = new AffineTransform(saveTransform);
		newTransform.translate(translationX, translationY);
		newTransform.rotate(rotation * -1D, centerX, centerY);
		g2d.setTransform(newTransform);
    
		// Draw image label with yOffset
		g2d.drawImage(labelImage, 0, Math.round(yOffset * size), null);
		
		// Restore original graphic transforms.
		g2d.setTransform(saveTransform);
		g2d.setFont(saveFont);
	}

    /**
	 * Draws the label for a structure over multiple lines.
	 * 
	 * @param g2d
	 * @param words
	 * @param position
	 * @param frontColor
	 */
	protected void drawCenteredMultiLabel(Graphics2D g2d, String[] words, Font labelFont,
                LocalPosition position,
				ColorChoice frontColor, double rotation, double scale) {
		int s = words.length;

        // If last words is a number then collapse it
        if ((s > 1) && Character.isDigit(words[s-1].charAt(0))) {
            words[s-2] += (" " + words[s-1]);
            s--;
        }

        float rowOffset = s/2f;
        int rowSize = (int)(scale / 1.5f);

		// Split up the name into multiple lines
		for (int j = 0; j < s; j++) {
			int y = (int)((j - rowOffset) * rowSize);
			drawCenteredLabel(g2d, words[j], labelFont, position, frontColor, y, rotation, scale);
		}
	}


	/**
	 * Gets an image of the label from cache or creates one if it doesn't exist.
	 * 
	 * @param label the label string.
	 * @param font the font to use.
	 * @param fontRenderContext the font render context to use.
	 * @param labelColor the color of the label.
	 * @return buffered image of label.
	 */
	private BufferedImage getLabelImage(
		String label, Font font, FontRenderContext fontRenderContext, ColorChoice labelColor,
		boolean hasOutline
	) { 
		BufferedImage labelImage = null;
		String labelId = label + font.toString() + labelColor.toString();
		if (labelImageCache.containsKey(labelId)) {
			labelImage = labelImageCache.get(labelId);
		} else {
			labelImage = createLabelImage(label, font, fontRenderContext, labelColor.text(),
							labelColor.outline(), hasOutline);
			labelImageCache.put(labelId, labelImage);
		}
		return labelImage;
	}

	/**
	 * Creates a label image.
	 * 
	 * @param label the label string.
	 * @param font the font to use.
	 * @param fontRenderContext the font render context to use.
	 * @param labelColor the color of the label.
	 * @param labelOutlineColor the color of the outline of the label.
	 * @return buffered image of label.
	 */
	private BufferedImage createLabelImage(
		String label, Font font, FontRenderContext fontRenderContext, Color labelColor,
		Color labelOutlineColor, boolean hasOutline) {

		// Determine bounds.
		TextLayout textLayout1 = new TextLayout(label, font, fontRenderContext);
		Rectangle2D bounds1 = textLayout1.getBounds();

		// Get label shape.
		Shape labelShape = textLayout1.getOutline(null);
		
		// Create buffered image for label.
		int width = (int) (bounds1.getWidth() + bounds1.getX()) + 4;
		int height = (int) (bounds1.getHeight()) + 4;
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		// Get graphics context from buffered image.
		Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.translate(2D - bounds1.getX(), 2D - bounds1.getY());
	
		Stroke saveStroke = null;
		// Draw label outline.
		if (hasOutline) {
			saveStroke = g2d.getStroke();
			g2d.setColor(labelOutlineColor);
			g2d.setStroke(new BasicStroke(font.getSize()/10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			
			// Draw outline
			g2d.draw(labelShape);

			// Restore stroke
			g2d.setStroke(saveStroke);
		}
		
		g2d.setColor(labelColor);
		// Fill label
		g2d.fill(labelShape);

		// Dispose of image graphics context.
		g2d.dispose();

		return bufferedImage;
	}
}
