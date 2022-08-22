/*
 * Mars Simulation Project
 * LineBreakPanel.java
 * @date 2022-08-20
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JPanel;

/**
 * This class demonstrates how to line-break and draw a paragraph 
 * of text using LineBreakMeasurer and TextLayout.
 *
 * This class constructs a LineBreakMeasurer from an
 * AttributedCharacterIterator.  It uses the LineBreakMeasurer
 * to create and draw TextLayouts (lines of text) which fit within 
 * the Component's width.
 */

@SuppressWarnings("serial")
public class LineBreakPanel extends JPanel {

	public static final int MARGIN_WIDTH = 2;
	public static final int MARGIN_HEIGHT = 2;
	
    // The LineBreakMeasurer used to line-break the paragraph.
    private LineBreakMeasurer lineMeasurer;

    // index of the first character in the paragraph.
    private int paragraphStart;

    // index of the first character after the end of the paragraph.
    private int paragraphEnd;

//	private Font FONT_SANS_SERIF_1 = new Font(Font.SANS_SERIF, Font.BOLD, 13);
	
    private static final 
        Hashtable<TextAttribute, Object> map =
           new Hashtable<TextAttribute, Object>();

    static {
        map.put(TextAttribute.FAMILY, Font.SANS_SERIF);
        map.put(TextAttribute.SIZE, 14.0f);
    }  

    private List<AttributedString> vanGogh = new ArrayList<>();
//    	= new AttributedString(
//        "Many people believe that Vincent van Gogh painted his best works " +
//        "during the two-year period he spent in Provence. Here is where he " +
//        "painted The Starry Night--which some consider to be his greatest " +
//        "work of all. However, as his artistic brilliance reached new " +
//        "heights in Provence, his physical and mental health plummeted. ",
//        map);

    public LineBreakPanel(List<String> text) {
		for (int i=0; i<text.size(); i++) {
			vanGogh.add(new AttributedString(text.get(i), map));
		}
    }
    
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

//		setPreferredSize(new Dimension(getWidth() - 10, getHeight() - 10));
		
		setBackground(new Color(0, 0, 0, 128));
//        setBackground(new Color(51, 25, 0, 128));
        
		int x = MARGIN_WIDTH;
		int y = MARGIN_HEIGHT;
		int w = getWidth() - MARGIN_WIDTH * 2;
		int h = getHeight() - MARGIN_HEIGHT * 2;
		int arc = 15;

//		Graphics2D g = (Graphics2D) g;
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(new Color(51, 25, 0, 128));
		g2.fillRoundRect(x, y, w, h, arc, arc);
		g2.setStroke(new BasicStroke(3f));
		g2.setColor(Color.orange);
		g2.drawRoundRect(x, y, w, h, arc, arc);
//		g2.dispose();
//	}
//	
//    public void paintComponent(Graphics g) {

//    	super.paintComponent(g);
//       	setOpaque(false);
//        setBackground(new Color(0,0,0,128));
 
//        Graphics2D g2d = (Graphics2D)g;
//		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		g2d.setRenderingHint(
//		        RenderingHints.KEY_TEXT_ANTIALIASING,
//		        RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		g2.setRenderingHint(
		        RenderingHints.KEY_TEXT_ANTIALIASING,
		        RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g2.setColor(Color.white);
		
		
        // Create a new LineBreakMeasurer from the paragraph.
        // It will be cached and re-used.
		for (int i=0; i<vanGogh.size(); i++) {
			
//	        if (lineMeasurer == null) {
	            AttributedCharacterIterator paragraph = vanGogh.get(i).getIterator();
	            paragraphStart = paragraph.getBeginIndex();
	            paragraphEnd = paragraph.getEndIndex();
	            FontRenderContext frc = g2.getFontRenderContext();
	            lineMeasurer = new LineBreakMeasurer(paragraph, frc);
//	        }
	        // Set break width to width of Component.
	        float breakWidth = (float)getSize().width - MARGIN_WIDTH * 6;
	        float drawPosY = 5L + i * 18;
	        // Set position to the index of the first character in the paragraph.
	        lineMeasurer.setPosition(paragraphStart);
	        // Get lines until the entire paragraph has been displayed.
	        while (lineMeasurer.getPosition() < paragraphEnd) {
	            // Retrieve next layout. A cleverer program would also cache
	            // these layouts until the component is re-sized.
	            TextLayout layout = lineMeasurer.nextLayout(breakWidth);
	            // Compute pen x position. If the paragraph is right-to-left we
	            // will align the TextLayouts to the right edge of the panel.
	            // Note: this won't occur for the English text in this sample.
	            // Note: drawPosX is always where the LEFT of the text is placed.
	            float drawPosX = layout.isLeftToRight()
	                ? MARGIN_WIDTH * 5 : breakWidth - layout.getAdvance();
	
	            // Move y-coordinate by the ascent of the layout.
	            drawPosY += layout.getAscent();
	            // Draw the TextLayout at (drawPosX, drawPosY).
	            layout.draw(g2, drawPosX, drawPosY);
	            // Move y-coordinate in preparation for next layout.
	            drawPosY += layout.getDescent() + layout.getLeading();
	        }
		}
        
		g2.dispose();
    }
	
}
