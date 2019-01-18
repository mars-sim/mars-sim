/**
 * Mars Simulation Project
 * SpotlightLayterUI.java
 * @version 3.1.0 2017-04-15
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.tool;

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

import javax.swing.SwingUtilities;
import javax.swing.plaf.LayerUI;

import org.mars_sim.msp.ui.swing.tool.settlement.SettlementMapPanel;

import com.alee.laf.panel.WebPanel;

public class SpotlightLayerUI extends LayerUI<WebPanel> {

	//private static final long serialVersionUID = 1L;

	private boolean mActive;
	private int mX, mY;

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

	    if (mActive) {
		    if (settlementMapPanel.isDaylightTrackingOn()) {
			      // Create a radial gradient, transparent in the middle.
			      java.awt.geom.Point2D center = new java.awt.geom.Point2D.Float(mX, mY);
			      float radius = 60;
			      float[] dist = {0.0f, 1.0f};
			      //Color[] colors = {new Color(0.0f, 0.0f, 0.0f, 0.0f), Color.gray};

			      Color[] colors = {
			    		  //new Color(0.0f, 0.0f, 0.0f, 0.0f),
			    		  //new Color(0, 0, 0, 0)
			    		  //new Color(g2.getColor().getRed() * 0.25f, g2.getColor().getGreen() * 0.25f, g2.getColor().getBlue() * 0.25f, 0f),//, g.getColor().getAlpha()),
			    		 //new Color(g2.getColor().getRed(), g2.getColor().getGreen(), g2.getColor().getBlue(), 0.0f)
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
	    }

	    g2.dispose();
	  }

	  @Override
	  protected void processMouseEvent(MouseEvent e, JLayer l) {
	    if (e.getID() == MouseEvent.MOUSE_ENTERED) mActive = true;
	    if (e.getID() == MouseEvent.MOUSE_EXITED) mActive = false;
	  }

	  @Override
	  protected void processMouseMotionEvent(MouseEvent e, JLayer l) {
	    Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), l);
	    mX = p.x;
	    mY = p.y;
	    l.repaint();
	  }
	}
