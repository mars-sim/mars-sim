package org.mars_sim.msp.ui.swing;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;

/**
 * A Combobox that is mousewheel-enabled.
 * @version 3.07 2014-10-14
 * @author stpa
 * 2014-01-29
 */
public class JComboBoxMW<T>
extends JComboBox<T>
implements MouseWheelListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private boolean layingOut = false;

	private boolean wide = true;

	/**
	 * constructor.
	 */
	public JComboBoxMW() {
		super();
		this.addMouseWheelListener(this);
		//((JTextField)this.getEditor().getEditorComponent())
		//	.setBorder(BorderFactory.createCompoundBorder(
		//		this.getBorder(),BorderFactory.createEmptyBorder(0,1,0,1)));
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
	 * Constructor.
	 * @param model {@link ComboBoxModel}<T>
	 */
	public JComboBoxMW(ComboBoxModel<T> model) {
		super(model);
		this.addMouseWheelListener(this);
	}

	/**
	 * Constructor.
	 * @param items T[]
	 */
	public JComboBoxMW(T[] items) {
		super(items);
		this.addMouseWheelListener(this);
	}

	/** Use mouse wheel to cycle through items if any. */
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (getItemCount() > 0) {
			boolean up = e.getWheelRotation() < 0;
			this.setSelectedIndex(
				(this.getSelectedIndex() + (up ? -1 : 1) + this.getItemCount()) % this.getItemCount()
			);
		}
	}

    public void doLayout(){
        try{
            layingOut = true;
            super.doLayout();
        }finally{
            layingOut = false;
        }
    }

	public boolean isWide() {
		return wide;
	}

	public void setWide(boolean wide) {
		this.wide = wide;
	}

    public Dimension getSize(){
        Dimension dim = super.getSize();
        if(!layingOut && isWide())
            //dim.width = Math.max(dim.width, getPreferredSize().width);
        	dim.width = Math.min(dim.width, Toolkit.getDefaultToolkit().getScreenSize().width);
        return dim;
    }

}
