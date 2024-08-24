/*
 * Mars Simulation Project
 * SpotlightLayerUI.java
 * @date 2021-09-20
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.tool;

import java.awt.AWTEvent;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.JPanel;


import javax.swing.SwingUtilities;
import javax.swing.plaf.LayerUI;

import com.mars_sim.ui.swing.tool.settlement.SettlementMapPanel;
import com.mars_sim.ui.swing.tool.settlement.SettlementMapPanel.DisplayOption;

@SuppressWarnings("serial")
public class SpotlightLayerUI extends LayerUI<JPanel> {

	private boolean mActive;
	private int mX;
	private int mY;

	private SettlementMapPanel settlementMapPanel;

	public SpotlightLayerUI(SettlementMapPanel settlementMapPanel) {
		this.settlementMapPanel = settlementMapPanel;
	}

	  @Override
	  public void installUI(JComponent c) {
	    super.installUI(c);

	    JLayer<?> jlayer = (JLayer<?>)c;
	    jlayer.setLayerEventMask(
	      AWTEvent.MOUSE_EVENT_MASK |
	      AWTEvent.MOUSE_MOTION_EVENT_MASK
	    );


	  }

	  @Override
	  public void uninstallUI(JComponent c) {
		JLayer<?> jlayer = (JLayer<?>)c;
	    jlayer.setLayerEventMask(0);
	    super.uninstallUI(c);
	  }

	  @Override
	  public void paint (Graphics g, JComponent c) {
	    Graphics2D g2 = (Graphics2D)g.create();

	    // Paint the view.
	    super.paint (g2, c);

	    if (mActive && settlementMapPanel.isOptionDisplayed(DisplayOption.DAYLIGHT_LAYER)) {
			// Create a radial gradient, transparent in the middle.
			java.awt.geom.Point2D center = new java.awt.geom.Point2D.Float(mX, mY);
			float radius = 60;
			float[] dist = {0.0f, 1.0f};

			Color[] colors = {
					new Color(.9f, .9f, .9f, .9f),
					new Color(g2.getColor().getRed()/255f, g2.getColor().getGreen()/255f, g2.getColor().getBlue()/255f, 0.0f)

			};

			RadialGradientPaint p =
				new RadialGradientPaint(center, radius, dist, colors);
			g2.setPaint(p);
			g2.setComposite(AlphaComposite.getInstance(
				AlphaComposite.SRC_OVER, 0.4f));
			g2.fillRect(0, 0, c.getWidth(), c.getHeight());
	    }

	    g2.dispose();
	  }

	  @SuppressWarnings("rawtypes")
	  @Override
	  protected void processMouseEvent(MouseEvent e, JLayer l) {
	    if (e.getID() == MouseEvent.MOUSE_ENTERED) mActive = true;
	    if (e.getID() == MouseEvent.MOUSE_EXITED) mActive = false;
	  }

	  @SuppressWarnings("rawtypes")
	  @Override
	  protected void processMouseMotionEvent(MouseEvent e, JLayer l) {
	    Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), l);
	    mX = p.x;
	    mY = p.y;
	    l.repaint();
	  }
	}
