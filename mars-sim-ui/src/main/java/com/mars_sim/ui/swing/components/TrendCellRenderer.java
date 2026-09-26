/*
 * Mars Simulation Project
 * TrendCellRenderer.java
 * @date 2026-09-26
 * @author Shalini H R
 */
package com.mars_sim.ui.swing.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;

import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.components.TrendValue.Trend;

/**
 * Renders a {@link TrendValue} as a formatted number with an up/down arrow and a
 * green/red colour. The arrow means the trend is not conveyed by colour alone.
 * The renderer is stateless; the trend comes from the value itself.
 */
public class TrendCellRenderer extends NumberCellRenderer {

	private static final long serialVersionUID = 1L;

	// Shades chosen to be readable on the respective theme backgrounds
	private static final Color LIGHT_UP = new Color(27, 127, 42);
	private static final Color LIGHT_DOWN = new Color(198, 40, 40);
	private static final Color DARK_UP = new Color(91, 214, 107);
	private static final Color DARK_DOWN = new Color(255, 107, 107);

	private static final Icon UP_ICON = new ArrowIcon(true);
	private static final Icon DOWN_ICON = new ArrowIcon(false);

	/**
	 * Constructor.
	 *
	 * @param digits Number of decimal digits to display
	 */
	public TrendCellRenderer(int digits) {
		super(digits);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		// Reset the foreground so the parent picks the table default for this cell
		setForeground(null);

		JLabel cell = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		Trend trend = (value instanceof TrendValue tv) ? tv.getTrend() : Trend.SAME;
		switch (trend) {
			case UP -> {
				cell.setIcon(UP_ICON);
				if (!isSelected) {
					cell.setForeground(StyleManager.isLightTheme() ? LIGHT_UP : DARK_UP);
				}
			}
			case DOWN -> {
				cell.setIcon(DOWN_ICON);
				if (!isSelected) {
					cell.setForeground(StyleManager.isLightTheme() ? LIGHT_DOWN : DARK_DOWN);
				}
			}
			default -> cell.setIcon(null);
		}
		return cell;
	}

	/**
	 * A small triangle drawn in the foreground colour of the component it is painted on.
	 */
	private static class ArrowIcon implements Icon {
		private static final int SIZE = 8;

		private final boolean up;

		ArrowIcon(boolean up) {
			this.up = up;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(c.getForeground());
			int[] xs = {x, x + SIZE / 2, x + SIZE};
			int[] ys = up ? new int[] {y + SIZE, y, y + SIZE} : new int[] {y, y + SIZE, y};
			g2.fillPolygon(xs, ys, 3);
			g2.dispose();
		}

		@Override
		public int getIconWidth() {
			return SIZE;
		}

		@Override
		public int getIconHeight() {
			return SIZE;
		}
	}
}
