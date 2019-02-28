/**
 * Mars Simulation Project
 * JSliderMW.java
 * @version 3.1.0 2019-02-28
 * @author stpa
 */
package org.mars_sim.msp.ui.swing;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JSlider;

/**
 * A slider control that reacts to mouse wheel events.
 */
public class JSliderMW
extends JSlider
implements MouseWheelListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * constructor.
	 */
	public JSliderMW() {
		super();
		this.addMouseWheelListener(this);
	}

	/**
	 * constructor.
	 * @param min {@link Integer}
	 * @param max {@link Integer}
	 * @param value {@link Integer}
	 */
	public JSliderMW(int min, int max, int value) {
		super(min,max,value);
		this.addMouseWheelListener(this);
	}

	/**
	 * constructor.
	 * @param orientation {@link Integer}
	 * @param min {@link Integer}
	 * @param max {@link Integer}
	 * @param value {@link Integer}
	 */
	public JSliderMW(int orientation, int min, int max, int value) {
		super(orientation,min,max,value);
		this.addMouseWheelListener(this);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		boolean up = e.getWheelRotation() > 0;
		int delta = (minorTickSpacing == 0) ? majorTickSpacing : minorTickSpacing;
		int value = this.getValue();
		value = up ?
			Math.max(
				this.getMinimum(),
				value - delta
			)
		:
			Math.min(
				value + delta,
				this.getMaximum()
			)
		;
		this.setValue(value);
	}
}
