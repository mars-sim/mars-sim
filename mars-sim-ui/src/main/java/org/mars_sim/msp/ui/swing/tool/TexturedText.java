package org.mars_sim.msp.ui.swing.tool;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.alee.extended.label.WebStyledLabel;
import com.alee.laf.WebLookAndFeel;

public class TexturedText extends JPanel {
  public void paint(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;

    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    Font font = new Font("Times New Roman", Font.PLAIN, 72);
    g2.setFont(font);

    String s = "Java Source and Support";
    Dimension d = getSize();
    float x = 20, y = 100;

    BufferedImage bi = getTextureImage();
    Rectangle r = new Rectangle(0, 0, bi.getWidth(), bi.getHeight());
    TexturePaint tp = new TexturePaint(bi, r);
    g2.setPaint(tp);

    g2.drawString(s, x, y);
  }

  private BufferedImage getTextureImage() {
    // Create the test image.
    int size = 8;
    BufferedImage bi = new BufferedImage(size, size,
        BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    g2.setPaint(Color.red);
    g2.fillRect(0, 0, size / 2, size / 2);
    g2.setPaint(Color.yellow);
    g2.fillRect(size / 2, 0, size, size / 2);
    g2.setPaint(Color.green);
    g2.fillRect(0, size / 2, size / 2, size);
    g2.setPaint(Color.blue);
    g2.fillRect(size / 2, size / 2, size, size);
    return bi;
  }

  public static void main(String[] args) {
	    JFrame f = new JFrame();
	    WebLookAndFeel.install();
	    JPanel panel = new JPanel(new GridLayout(1, 17));
	    f.getContentPane().add(panel);
//	for (int i=0; i<17; i++) {
//		WebStyledLabel icon = new WebStyledLabel (WebLookAndFeel.getIcon(i));
//		panel.add(icon);
//	}
   

//    f.getContentPane().add(new TexturedText());
	
    f.setSize(800, 200);
    f.show();

  }
}