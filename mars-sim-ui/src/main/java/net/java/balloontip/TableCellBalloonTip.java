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
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;

import net.java.balloontip.positioners.BalloonTipPositioner;
import net.java.balloontip.styles.BalloonTipStyle;

/**
 * Provides similar functionality as a CustomBalloonTip, but attaches itself to a cell in a JTable
 * @author Tim Molderez
 */
public class TableCellBalloonTip extends CustomBalloonTip {
	protected int row;
	protected int column;

	// Adjust the balloon tip's position when the table's columns are modified
	private final TableColumnModelListener columnListener = new TableColumnModelListener()  {
		public void columnAdded(TableColumnModelEvent e) {
			if (e.getToIndex() <= column) {
				setCellPosition(row, column+1);
			} else {
				setCellPosition(row, column);
			}
		}
		public void columnMarginChanged(ChangeEvent e) {setCellPosition(row, column);}
		public void columnMoved(TableColumnModelEvent e) {
			// If the column with the balloon tip is being moved
			if (column == e.getFromIndex()) {
				setCellPosition(row, e.getToIndex());
			// If both source and target columns are before/after the column with the balloon tip
			} else if ((e.getFromIndex() > column && e.getToIndex() > column)
					|| (e.getFromIndex() < column && e.getToIndex() < column)) {
				setCellPosition(row, column);
			// Moving a column before the balloon tip to a column after the balloon tip
			} else if(e.getFromIndex() < column && e.getToIndex() >= column) {
				setCellPosition(row, column-1);
			// Moving a column after the balloon tip to a column before the balloon tip
			} else if(e.getFromIndex() > column && e.getToIndex() <= column) {
				setCellPosition(row, column+1);
			}
		}
		public void columnRemoved(TableColumnModelEvent e) {
			if (e.getFromIndex() == column) {
				closeBalloon();
			} else if (e.getFromIndex() < column) {
				setCellPosition(row, column-1);
			} else {
				setCellPosition(row, column);
			}
		}
		public void columnSelectionChanged(ListSelectionEvent e) {}
	};
	
	/**
	 * @see net.java.balloontip.BalloonTip#BalloonTip(JComponent, JComponent, BalloonTipStyle, Orientation, AttachLocation, int, int, boolean)
	 * @param table		the table to attach the balloon tip to (may not be null)
	 * @param row		which row is the balloon tip attached to
	 * @param column	which column is the balloon tip attached to
	 */
	public TableCellBalloonTip(JTable table, JComponent component, int row, int column, BalloonTipStyle style, Orientation alignment, AttachLocation attachLocation, int horizontalOffset, int verticalOffset, boolean useCloseButton) {
		super(table, component, table.getCellRect(row, column, true), style, alignment, attachLocation, horizontalOffset, verticalOffset, useCloseButton);
		setup(row, column);
	}

	/**
	 * @see net.java.balloontip.BalloonTip#BalloonTip(JComponent, JComponent, BalloonTipStyle, BalloonTipPositioner, JButton)
	 * @param table		the table to attach the balloon tip to (may not be null)
	 * @param row		which row is the balloon tip attached to
	 * @param column	which column is the balloon tip attached to
	 */
	public TableCellBalloonTip(JTable table, JComponent component, int row, int column, BalloonTipStyle style, BalloonTipPositioner positioner, JButton closeButton) {
		super(table, component, table.getCellRect(row, column, true), style, positioner, closeButton);
		setup(row, column);
	}

	/**
	 * Set the table cell the balloon tip should attach to
	 * @param row		row of the table cell
	 * @param column	column of the table cell
	 */
	public void setCellPosition(int row, int column) {
		this.row = row;
		this.column = column;
		setOffset(((JTable)attachedComponent).getCellRect(row, column, true));
	}

	public void closeBalloon() {
		JTable table=((JTable)attachedComponent);
		table.getColumnModel().removeColumnModelListener(columnListener);
		super.closeBalloon();
	}

	/*
	 * A helper method needed when constructing a TablecellBalloonTip instance
	 * @param row		the row of the cell to which this balloon tip attaches itself to
	 * @param column	the column of the cell to which this balloon tip attaches itself to
	 */
	private void setup(int row, int column) {
		this.row = row;
		this.column = column;

		JTable table=((JTable)attachedComponent);
		table.getColumnModel().addColumnModelListener(columnListener);
	}

	private static final long serialVersionUID = -8760012691273527057L;
}
