/**
 * Mars Simulation Project
 * ZoomSliderPanel.java
 * @version 3.1.0 2019-02-10
 * Modified by Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
  
@SuppressWarnings("serial")
public class ZoomSliderPanel extends JPanel implements ChangeListener {
    Shape[] shapes;
    Dimension size;
    double scale = 1.0;
  
    public ZoomSliderPanel() {
        size = new Dimension(10,10);
        setBackground(new Color(240,200,200));
    }
  
    public void stateChanged(ChangeEvent e) {
        int value = ((JSlider)e.getSource()).getValue();
        scale = value/100.0;
        repaint();
        revalidate();
    }
  
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        if(shapes == null) initShapes();
        // Keep shapes centered on panel.
        double x = (getWidth()  - scale*size.width)/2;
        double y = (getHeight() - scale*size.height)/2;
        AffineTransform at = AffineTransform.getTranslateInstance(x, y);
        at.scale(scale, scale);
        g2.setPaint(Color.blue);
        g2.draw(at.createTransformedShape(shapes[0]));
        g2.setPaint(Color.green.darker());
        g2.draw(at.createTransformedShape(shapes[1]));
        g2.setPaint(new Color(240,240,200));
        g2.fill(at.createTransformedShape(shapes[2]));
        g2.setPaint(Color.red);
        g2.draw(at.createTransformedShape(shapes[2]));
    }
  
    public Dimension getPreferredSize() {
        int w = (int)(scale*size.width);
        int h = (int)(scale*size.height);
        return new Dimension(w, h);
    }
  
    private void initShapes() {
        shapes = new Shape[3];
        int w = getWidth();
        int h = getHeight();
        shapes[0] = new Rectangle2D.Double(w/16, h/16, w*7/8, h*7/8);
        shapes[1] = new Line2D.Double(w/16, h*15/16, w*15/16, h/16);
        shapes[2] = new Ellipse2D.Double(w/4, h/4, w/2, h/2);
        size.width = w;
        size.height = h;
    }
  
    private JSlider getControl() {
        JSlider slider = new JSlider(JSlider.HORIZONTAL, 50, 200, 100);
        slider.setMajorTickSpacing(50);
        slider.setMinorTickSpacing(10);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(this);
        return slider;        
    }
  
    public static void main(String[] args) {
        ZoomSliderPanel app = new ZoomSliderPanel();
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(new JScrollPane(app));
        f.getContentPane().add(app.getControl(), "Last");
        f.setSize(400, 400);
        f.setLocation(200,200);
        f.setVisible(true);
    }
}