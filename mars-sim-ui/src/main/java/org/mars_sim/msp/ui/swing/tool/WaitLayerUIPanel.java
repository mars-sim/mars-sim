/**
 * Mars Simulation Project
 * WaitLayerUIPanel.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;

import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.plaf.LayerUI;

@SuppressWarnings("serial")
public class WaitLayerUIPanel extends LayerUI<JPanel> implements ActionListener {

	private boolean mIsRunning;
	private boolean mIsFadingOut;
	private boolean done;

	private Timer mTimer;

	private int mAngle;
	private int mFadeCount;
	private int mFadeLimit = 5;//15;

	@Override
	public void paint(Graphics g, JComponent c) {
		int w = c.getWidth();
		int h = c.getHeight();
		super.paint(g, c); // Paint the view.
		if (!mIsRunning) {
			return;
		}
		Graphics2D g2 = (Graphics2D) g.create();
		float fade = (float) mFadeCount / (float) mFadeLimit;
		Composite urComposite = g2.getComposite(); // Gray it out.
		float f = .5f * fade;
		if (f > 1f)
			f = 1f;
		else if (f < 0)
			f = 0;
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, f));
		g2.fillRect(0, 0, w, h);
		g2.setComposite(urComposite);
		int s = Math.min(w, h) / 5;// Paint the wait indicator.
		int cx = w / 2;
		int cy = h / 2;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setStroke(new BasicStroke(s / 4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g2.setPaint(Color.white);
		g2.rotate(Math.PI * mAngle / 180, cx, cy);
		for (int i = 0; i < 12; i++) {
			float scale = (11.0f - (float) i) / 11.0f;
			g2.drawLine(cx + s, cy, cx + s * 2, cy);
			g2.rotate(-Math.PI / 6, cx, cy);
			f = scale * fade;
			if (f > 1f)
				f = 1.0f;
			else if (f < 0)
				f = 0;
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, f));
		}
		g2.dispose();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (mIsRunning) {
			firePropertyChange("tick", 0, 1);
			mAngle += 3;
			if (mAngle >= 360) {
				mAngle = 0;
			}
			if (mIsFadingOut) {
				if (--mFadeCount == 0) {
					mIsRunning = false;
					mTimer.stop();
				}
			} else if (mFadeCount < mFadeLimit) {
				mFadeCount++;
			}
		}

		done = true;
	}

	public boolean isRunning() {
		return mIsRunning;
	}

	public void start() {
		if (mIsRunning) {
			return;
		}
		mIsRunning = true;// Run a thread for animation.
		mIsFadingOut = false;
		mFadeCount = 0;
		int fps = 24;
		int tick = 1000 / fps;
		mTimer = new Timer(tick, this);
		mTimer.start();
	}

	public void stop() {
		while (true) {
			try {
				Thread.sleep(500L);
			} catch (InterruptedException e) {
			}
			if (done) {
				// wait until done become true before setting mIsFadingOut to true
				mIsFadingOut = true;
				break;
			}
		}
	}

	@Override
	public void applyPropertyChange(PropertyChangeEvent pce, @SuppressWarnings("rawtypes") JLayer l) {
		if ("tick".equals(pce.getPropertyName())) {
			l.repaint();
		}
	}
}
