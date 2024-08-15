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

import org.apache.batik.gvt.GraphicsNode;

import com.mars_sim.core.map.location.LocalBoundedObject;
import com.mars_sim.core.map.location.LocalPosition;

/**
 * This class provides basic method to draw on the Settlement Map panel.
 */
public abstract class AbstractMapLayer implements SettlementMapLayer {
    // A data record to represent a color choice.
    protected record ColorChoice(Color text, Color outline) {}
	
    // A data record to represent a structure key.
    private record StructureKey(GraphicsNode svg, double width, double length) {}

	private static final float[] DASHES = {50.0f, 20.0f, 10.0f, 20.0f};
    
	private static final String H = "H ";
	private static final String T = "T ";
	
    // See https://docstore.mik.ua/orelly/java-ent/jfc/ch04_05.htm for instructions on BasicStroke
    private static final BasicStroke THIN_DASH = new BasicStroke(2.0f,
    	      BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, DASHES, 0.0f);
	private static final BasicStroke THICK_DASH = new BasicStroke(10.0f,
			  BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 50.0f, DASHES, 0.0f);

    private Map<String, BufferedImage> labelImageCache = new HashMap<>();
	private Map<Double, Map<StructureKey, BufferedImage>> svgImageCache = new HashMap<>();

    /**
	 * Draws an oval at a settlement.
	 * 
	 * @param g2d the graphics context.
	 * @param pos Position to draw oval.
     * @param color Color choice
     * @param viewpoint Map viewpoint to used for rendering
	 */
	protected void drawOval(LocalPosition pos, ColorChoice color,
                            MapViewPoint viewpoint) {
		
		double scale = viewpoint.scale();
		int size = (int)(Math.round(scale / 3.0));
		size = Math.max(size, 1);
				
		double radius = size / 2.0;
		
		// Save original graphics transforms.
		var g2d = viewpoint.graphics();
		AffineTransform saveTransform = g2d.getTransform();

		double translationX = -1.0 * pos.getX() * scale - radius;
		double translationY = -1.0 * pos.getY() * scale - radius;

		// Apply graphic transforms for label.
		AffineTransform newTransform = new AffineTransform(saveTransform);
		newTransform.translate(translationX, translationY);
		newTransform.rotate(viewpoint.rotation() * -1D, radius, radius);
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
	 * @param viewpoint Map view point used for rendering
	 */
	protected void drawRightLabel(boolean isSelected, String label, LocalPosition loc,
		ColorChoice labelColor, Font labelFont, float xOffset, float yOffset,
        MapViewPoint viewpoint) {

		double scale = viewpoint.scale();
		double rotation = viewpoint.rotation();
		var g2d = viewpoint.graphics();
		float fontSizeIncrease = Math.round(scale / 2.5);
		
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
		double centerX = labelImage.getWidth() / 2D;
		double centerY = labelImage.getHeight() / 2D;
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
	        g2d.setColor(Color.gray.brighter().brighter().brighter());

	        int x = widthOffset; 
	        int y = heightOffset;
	       	int w = (int)Math.round(centerX * 2.025);
	       	int h = (int)Math.round(centerY * 2.2);
	        	       	
	        int thickness = 2;
	
	        // Draw a frame rect white background label
	        g2d.fill3DRect(x, y, w, h, true);
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
	protected void drawCenteredLabel(String label, Font labelFont, LocalPosition loc,
		ColorChoice labelColor, float yOffset, MapViewPoint viewpoint) {

		double scale = viewpoint.scale();
		var g2d = viewpoint.graphics();

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
		newTransform.rotate(viewpoint.rotation() * -1D, centerX, centerY);
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
	 * @param words
	 * @param position
	 * @param frontColor
	 */
	protected void drawCenteredMultiLabel(String[] words, Font labelFont,
                LocalPosition position, ColorChoice frontColor, MapViewPoint viewpoint) {
		int s = words.length;

        // If last words is a number then collapse it
        if ((s > 1) && Character.isDigit(words[s-1].charAt(0))) {
            words[s-2] += (" " + words[s-1]);
            s--;
        }

        if (words[0].contains(H)) {
        	words[0] = words[0].replace(" ", "");
        }
        else if (words[0].contains(T)) {
        	words[0] = words[0].replace(" ", "");
        }	
        	
        float rowOffset = s - 1f;
        int rowSize = (int)(viewpoint.scale() / 1.5f);

		// Split up the name into multiple lines
		for (int j = 0; j < s; j++) {
			int y = (int)((j - rowOffset) * rowSize);
			drawCenteredLabel(words[j], labelFont, position, frontColor, y, viewpoint);
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

	/**
     * Gets a buffered image for a given graphics node.
     * 
     * @param svg the SVG graphics node.
     * @param width the structure width (meters).
     * @param length the structure length (meters).
     * @param patternSVG the pattern SVG graphics node (null if no pattern).
     * @return buffered image.
     */
    protected BufferedImage getBufferedImage(
            GraphicsNode svg, double width, double length,
            GraphicsNode patternSVG, double scale) {

        // Get image cache for current scale or create it if it doesn't exist.
        Map<StructureKey, BufferedImage> imageCache = null;
        if (svgImageCache.containsKey(scale)) {
            imageCache = svgImageCache.get(scale);
        }
        else {
            imageCache = new HashMap<>(100);
            svgImageCache.put(scale, imageCache);
        }

        // Get image from image cache or create it if it doesn't exist.
        BufferedImage image = null;
        StructureKey buildingKey = new StructureKey(svg, width, length);
        if (imageCache.containsKey(buildingKey)) {
            image = imageCache.get(buildingKey);
        }
        else {
            image = createBufferedImage(svg, width, length, patternSVG, scale);
            imageCache.put(buildingKey, image);
        }

        return image;
    }

    /**
     * Creates a buffered image from a SVG graphics node.
     * 
     * @param svg the SVG graphics node.
     * @param width the structure width (meters).
     * @param length the structure length (meters).
     * @param patternSVG the pattern SVG graphics node (null if no pattern).
     * @return the created buffered image.
     */
    private BufferedImage createBufferedImage(GraphicsNode svg, double width, double length,
            GraphicsNode patternSVG, double scale) {

        int imageWidth = (int) (width * scale);
        if (imageWidth <= 0) {
            imageWidth = 1;
        }
        int imageLength = (int) (length * scale);
        if (imageLength <= 0) {
            imageLength = 1;
        }
        BufferedImage bufferedImage = new BufferedImage(
                imageWidth, imageLength,
                BufferedImage.TYPE_INT_ARGB
                );

        // Determine bounds.
        Rectangle2D bounds = svg.getBounds();

        // Determine transform information.
        double scalingWidth = width / bounds.getWidth() * scale;
        double scalingLength = length / bounds.getHeight() * scale;

        // Draw the SVG image on the buffered image.
        Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        svg.setTransform(AffineTransform.getScaleInstance(scalingWidth, scalingLength));
        svg.paint(g2d);

        // Draw repeating pattern SVG image on the buffered image.
        if (patternSVG != null) {
            double patternScaling;
            double patternWidth;
            double patternLength;

            double originalProportions = bounds.getWidth() / bounds.getHeight();
            double finalProportions = width / length;
            Rectangle2D patternBounds = patternSVG.getBounds();
            if ((finalProportions / originalProportions) >= 1D) {
                patternScaling = scalingLength;
                patternLength = length * (patternBounds.getHeight() / bounds.getHeight());
                patternWidth = patternLength * (patternBounds.getWidth() / patternBounds.getHeight());
            }
            else {
                patternScaling = scalingWidth;
                patternWidth = width * (patternBounds.getWidth() / bounds.getWidth());
                patternLength = patternWidth * (patternBounds.getHeight() / patternBounds.getWidth());
            }

            AffineTransform patternTransform = new AffineTransform();
            patternTransform.scale(patternScaling, patternScaling);
            for (double x = 0D; x < length; x += patternLength) {
                patternTransform.translate(0D, x * bounds.getHeight());
                double y = 0D;
                for (; y < width; y += patternWidth) {
                    patternTransform.translate(y * bounds.getWidth(), 0D);
                    patternSVG.setTransform(patternTransform);
                    patternSVG.paint(g2d);
                    patternTransform.translate(y * bounds.getWidth() * -1D, 0D);
                }
                patternTransform.translate(0D, x * bounds.getHeight() * -1D);
            }
        }

        // Cleanup and return image
        g2d.dispose();

        return bufferedImage;
    }

	/**
     * Draws a structure using SVG on the map.
     * 
     * @param placement Placement of structure
     * @param svg the SVG graphics node.
     * @param patternSVG the pattern SVG graphics node (null if no pattern).
	 * @param selectedColor the color to draw as selected
     */
    protected void drawStructure(LocalBoundedObject placement, GraphicsNode svg,
								GraphicsNode patternSVG, Color selectedColor,
								MapViewPoint viewpoint) {
		
		var g2d = viewpoint.graphics();
		double scale = viewpoint.scale();

        double xLoc = placement.getXLocation();
        double yLoc = placement.getYLocation();
        double width = placement.getWidth();
        double length = placement.getLength();
        double facing = placement.getFacing();

        // Save original graphics transforms.
        AffineTransform saveTransform = g2d.getTransform();
   
        // Determine bounds.
        Rectangle2D bounds = svg.getBounds();

        // Determine transform information.
        double scalingWidth = width / bounds.getWidth() * scale;
        double scalingLength = length / bounds.getHeight() * scale;
        double boundsPosX = bounds.getX() * scalingWidth;
        double boundsPosY = bounds.getY() * scalingLength;
        double centerX = width * scale / 2D;
        double centerY = length * scale / 2D;
        double translationX = (-1D * xLoc * scale) - centerX - boundsPosX;
        double translationY = (-1D * yLoc * scale) - centerY - boundsPosY;
        double facingRadian = facing / 180D * Math.PI;
  
        AffineTransform newTransform = new AffineTransform();
        
		// Draw buffered image of structure.
		BufferedImage image = getBufferedImage(svg, width, length, patternSVG, scale);
		if (image != null) {
			
			// Apply graphic transforms for structure.		
			newTransform.translate(translationX, translationY);
			newTransform.rotate(facingRadian, centerX + boundsPosX, centerY + boundsPosY);
			
			g2d.transform(newTransform);
			g2d.drawImage(image, 0, 0, null);
		}

        if (selectedColor != null) {   
            AffineTransform newTransform1 = new AffineTransform();
        	newTransform1.scale(scalingWidth, scalingLength);
            g2d.transform(newTransform1);
            
			// Draw the dashed border over the selected building
			g2d.setPaint(selectedColor);

			// Save original stroke
        	Stroke oldStroke = g2d.getStroke();
			g2d.setStroke(THICK_DASH);                                           
			g2d.draw(bounds);
			
			// Restore the stroke
			g2d.setStroke(oldStroke);
        }
        
        // Restore original graphic transforms.
        g2d.setTransform(saveTransform);
    }

	/**
     * Draws a rectangle on the map.
     * 
     * @param placement Placement of structure
     * @param color the color to display the rectangle.
	 * @param selectedColor If not null then the highlight color

     */
    protected void drawRectangle(LocalBoundedObject placement,
            Color color, Color selectedColor, MapViewPoint viewpoint) {

		var g2d = viewpoint.graphics();
		double scale = viewpoint.scale();

        double width = placement.getWidth();
        double length = placement.getLength();

        // Save original graphics transforms.
        AffineTransform saveTransform = g2d.getTransform();
        // Save original stroke
        Stroke oldStroke = g2d.getStroke();
   
        // Determine bounds.
        Rectangle2D bounds = new Rectangle2D.Double(0, 0, width, length);

        // Determine transform information.
        double scalingWidth = width / bounds.getWidth() * scale;
        double scalingLength = length / bounds.getHeight() * scale;
  
        AffineTransform newTransform = new AffineTransform();
       
		// Draw filled rectangle.
		newTransform.scale(scalingWidth, scalingLength);
		g2d.transform(newTransform);
		
		g2d.setColor(color);
		g2d.fill(bounds);
		
		if (selectedColor != null) {
			// Draw the dashed border
			g2d.setPaint(selectedColor);
			g2d.setStroke(THIN_DASH);
			g2d.draw(bounds);
			g2d.setStroke(oldStroke);
		}
        
        // Restore original graphic transforms.
        g2d.setTransform(saveTransform);
    }

	
    @Override
    public void destroy() {
		labelImageCache.clear();
        svgImageCache.clear();
    }   
}
