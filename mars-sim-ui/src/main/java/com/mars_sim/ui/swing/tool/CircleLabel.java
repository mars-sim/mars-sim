/*
 * Mars Simulation Project
 * CircleLabel.java
 * @date 2026-09-02
 * @author Manny Kung
 */

package com.mars_sim.ui.swing.tool;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.mars_sim.ui.swing.StyleManager;

@SuppressWarnings("serial")
public class CircleLabel extends JPanel {
	
	private String text = "Sol 00";
	int width;
	int height;
	
    public CircleLabel(int width, int height) {
    	this.width = width;
    	this.height = height;
        setPreferredSize(new Dimension(width, height));
        setMaximumSize(new Dimension(width, height));
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
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);	
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
//        Shape circle = new Ellipse2D.Double(0, 0, getWidth() - 1, getHeight() - 1);
        double arc = height / 1.2;
        
        RoundRectangle2D bounds = new RoundRectangle2D.Double(1.5, 1.5, width - 4, height - 4, arc, arc);
        // Optional: Draw border
//        if (StyleManager.isLightTheme()) {
//        	// Gradient: DARK_GRAY (top-left) → LIGHT_GRAY (bottom-right)
//            GradientPaint gradient = new GradientPaint(
//                0, 0, Color.DARK_GRAY,
//                20, 20, Color.LIGHT_GRAY
//            );
//
//            g2d.setPaint(gradient);
//
//        }
//        else {
            // Gradient: ORANGE (top-left) → YELLOW (bottom-right)
            GradientPaint gradient = new GradientPaint(
                0, 0, Color.LIGHT_GRAY,
                27, 27, Color.ORANGE.darker() 
            );

            g2d.setPaint(gradient);
//        }
        
        int thickness = 2;
        
        g2d.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
//        g2d.fill(bounds);
        g2d.draw(bounds);
        
        // Optional: Draw border
        if (StyleManager.isLightTheme()) {
        	g2d.setColor(Color.BLACK);
        }
        else {
        	g2d.setColor(Color.ORANGE);
        }
        
     // Draw text centered in the circle
//        g2.setColor(Color.WHITE);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();
        int x = (int)Math.round(width  - fm.stringWidth(text) + .7) / 2 ;
        int y = (int)Math.round(height - fm.getHeight() + .7) / 2 + fm.getAscent();
        g2d.drawString(text, x, y);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Circle Label");
        frame.setSize(new Dimension(25, 25));
        frame.add(new CircleLabel(25, 25));
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}   
