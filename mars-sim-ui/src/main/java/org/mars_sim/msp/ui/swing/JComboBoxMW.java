package org.mars_sim.msp.ui.swing;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

/**
 * a checkbox that is mousewheel-enabled.
 * @author stpa
 * 2014-01-29
 */
public class JComboBoxMW<T> extends JComboBox<T> implements MouseWheelListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * constructor.
	 */
	public JComboBoxMW() {
		super();
		this.addMouseWheelListener(this);
	}

	/**
	 * constructor.
	 * @param items {@link Vector}<T> the initial items.
	 */
	public JComboBoxMW(Vector<T> items) {
		super(items);
		this.addMouseWheelListener(this);
	}

	/**
	 * constructor.
	 * @param model {@link ComboBoxModel}<T>
	 */
	public JComboBoxMW(ComboBoxModel<T> model) {
		super(model);
		this.addMouseWheelListener(this);
	}

	/**
	 * constructor.
	 * @param items T[]
	 */
	public JComboBoxMW(T[] items) {
		super(items);
		this.addMouseWheelListener(this);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (getItemCount() > 0) {
			boolean up = e.getWheelRotation() < 0;
			int selected = this.getSelectedIndex();
			selected = up ?
				Math.max(
					0,
					selected - 1
				)
			:
				Math.min(
					selected + 1,
					this.getItemCount() - 1
				)
			;
			this.setSelectedIndex(selected);
		}
	}
}
