/**
 * Copyright (c) 2011-2013 Bernhard Pauler, Tim Molderez.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the 3-Clause BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/BSD-3-Clause
 */

package net.java.balloontip;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import net.java.balloontip.positioners.BalloonTipPositioner;
import net.java.balloontip.styles.BalloonTipStyle;

/**
 * A balloon tip that can attach itself to an item in a JList
 * @author Tim Molderez
 */
public class ListItemBalloonTip extends CustomBalloonTip {
	protected int index; // Index of the list item that this balloon tip is attached to
	
	// If list data is added or removed, adjust the balloon tip
	private final ListDataListener dataListener = new ListDataListener() {
		public void intervalAdded(ListDataEvent e) {
			// If the balloon tip needs to move down
			if (e.getIndex1() <= index) {
				index+=e.getIndex1()-e.getIndex0()+1;
			}
			setItemPosition(index);
		}

		public void intervalRemoved(ListDataEvent e) {
			// If the balloon tip needs to move up
			if (e.getIndex1() < index) {
				index-=e.getIndex1()-e.getIndex0()+1;
				setItemPosition(index);
			// If the item with the balloon tip is removed
			} else if (index >= e.getIndex0() && index <= e.getIndex1()) {
				closeBalloon();
			} else {
				setItemPosition(index);
			}
		}

		public void contentsChanged(ListDataEvent e) {
			setItemPosition(index); // Refreshes the item's position, in case it might've changed..
		}
	};
	
	/**
	 * @see net.java.balloontip.BalloonTip#BalloonTip(JComponent, JComponent, BalloonTipStyle, Orientation, AttachLocation, int, int, boolean)
	 * @param list		the list to attach the balloon tip to (may not be null)
	 * @param index		index of the list item (must be valid)
	 */
	public ListItemBalloonTip(JList<?> list, JComponent component, int index, BalloonTipStyle style, Orientation alignment, AttachLocation attachLocation, int horizontalOffset, int verticalOffset, boolean useCloseButton) {
		super(list, component, list.getCellBounds(index, index), style, alignment, attachLocation, horizontalOffset, verticalOffset, useCloseButton);
		setup(index);
	}

	/**
	 * @see net.java.balloontip.BalloonTip#BalloonTip(JComponent, JComponent, BalloonTipStyle, BalloonTipPositioner, JButton)
	 * @param list		the list to attach the balloon tip to (may not be null)
	 * @param index		index of the list item (must be valid)
	 */
	public ListItemBalloonTip(JList<?> list, JComponent component, int index, BalloonTipStyle style, BalloonTipPositioner positioner, JButton closeButton) {
		super(list, component, list.getCellBounds(index, index), style, positioner, closeButton);
		setup(index);
	}

	/**
	 * Set the list item the balloon tip should attach to
	 * @param index		index of the list item
	 */
	public void setItemPosition(int index) {
		setOffset(((JList<?>)attachedComponent).getCellBounds(index, index));
	}

	public void closeBalloon() {
		JList<?> list=((JList<?>)attachedComponent);
		list.getModel().removeListDataListener(dataListener);
		super.closeBalloon();
	}

	/*
	 * A helper method needed when constructing a ListItemBalloonTip instance
	 * @param index		the row of the cell to which this balloon tip attaches itself to
	 */
	private void setup(int index) {
		this.index = index;
		JList<?> list=((JList<?>)attachedComponent);
		list.getModel().addListDataListener(dataListener);
	}
	
	private static final long serialVersionUID = -7270789090236631717L;
}
