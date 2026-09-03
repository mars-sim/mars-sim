/*
 * Mars Simulation Project
 * CircleLabel.java
 * @date 2026-09-02
 * @author Manny Kung
 */

package com.mars_sim.ui.swing.tool;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.mars_sim.ui.swing.StyleManager;

@SuppressWarnings("serial")
public class CircleLabel extends JPanel {
	
	private String text;
	
    public CircleLabel(int width, int height) {
        setPreferredSize(new Dimension(width, height));
    }

    public void setText(String text) {
    	this.text = text;
    	repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);	
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		
//		BufferedImage mask = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
//		Graphics2D g2d = mask.createGraphics();
//		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		g2d.fillOval(0, 0, getWidth() - 2 , getHeight() - 2);
//		g2d.dispose();

//		BufferedImage masked = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//		g2d = masked.createGraphics();
//		g2d.drawImage(master, 0, 0, null);
//		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN));
//		g2d.drawImage(mask, 0, 0, null);
//		g2d.dispose();
        
        // Draw filled circle
        Shape circle = new Ellipse2D.Double(0, 0, getWidth() - 1, getHeight() - 1);
        if (StyleManager.isLightTheme()) {
        	g2d.setColor(getBackground());
        }
        else {
            g2d.setColor(getBackground());
        }

        g2d.fill(circle);
        
        // Optional: Draw border
        if (StyleManager.isLightTheme()) {
        	g2d.setColor(Color.BLACK);
        }
        else {
        	g2d.setColor(Color.ORANGE);
        }
        g2d.draw(circle);
        
     // Draw text centered in the circle
//        g2.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth()  - fm.stringWidth(text)) / 2;
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(text, x, y);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Circle Label");
        frame.add(new CircleLabel(27, 27));
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}   
