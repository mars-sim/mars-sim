/*
 * Mars Simulation Project
 * MapMouseListener.java
 * @date 2024-09-29
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.map;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.mars_sim.core.map.location.IntPoint;

/**
 * This is a listener to mouse event on a MapPanel. It changes the cursor when hovered over a 
 * surface unit or a landmark.
 * If clicked the appropriate detailed window is shown.
 */
class MapMouseListener extends MouseAdapter {
	
	private static final int TOOLTIP_DELAY = 750;
	private static final Cursor DEFAULT = new Cursor(Cursor.DEFAULT_CURSOR);
	private static final Cursor CROSSHAIR = new Cursor(Cursor.CROSSHAIR_CURSOR);

    private MapPanel mapPanel;
	private Point hotspotPoint;
	private String pendingTipText;
	private Popup tipWindow;
	private Timer popupTimer;
	private int dragX;
	private int dragY;
	private boolean dragging = false;

	/*
	 * Create a mouse listener on a map panel that will respond to movement and click events.
	 */
    public MapMouseListener(MapPanel mapPanel) {
        this.mapPanel = mapPanel;
		popupTimer = new Timer(TOOLTIP_DELAY, e -> showTooltip());
		popupTimer.setRepeats(false);
		popupTimer.stop();
    }

	/**
	 * Find if there is a hotspot under the mouse position and execute the clicked method.
	 */
    @Override
    public void mouseClicked(MouseEvent event) {
        if (SwingUtilities.isRightMouseButton(event) && event.getClickCount() == 1) {
            var hs = findHotspot(new IntPoint(event.getX(), event.getY()));
			if (hs != null) {
				updateCursor(CROSSHAIR);
				hs.clicked();
			}
			else
				updateCursor(DEFAULT);
        }
		else {
			// Notify map panel
			mapPanel.notifyMouseClicked(new IntPoint(event.getX(), event.getY()));
		}
    }

	@Override
	public void mouseReleased(MouseEvent e) {
		dragging = false;
		mapPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	/**
	 * Find the hotspot that matches the mouse position
	 * @param mouse Mouse position on screen
	 * @return
	 */
    private MapHotspot findHotspot(IntPoint mouse) {
		var hotspots = mapPanel.getHotspots();
		for(var h : hotspots) {
			if (h.isWithin(mouse)) {
				return h;
			}
		}

		return null;
 	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (!dragging) {
			dragging = true;
			dragX = e.getX();
			dragY = e.getY();
			mapPanel.setCursor(new Cursor(Cursor.MOVE_CURSOR));

			return;
		}

		int x = e.getX();
		int y = e.getY();
		if (x < 0 || y < 0 || x > mapPanel.getWidth() || y > mapPanel.getHeight()) {
			// If the mouse is outside the panel, stop dragging
			dragging = false;
			mapPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			return;
		}

		int dx = dragX - e.getX();
		int dy = dragY - e.getY();

		mapPanel.dragPosition(dx, dy);

		dragX = e.getX();
		dragY = e.getY();
	}

	/**
	 * Track the mouse movement as it cross hotspots and render tooltips.
	 */
    @Override
    public void mouseMoved(MouseEvent event) {
		// Close and previous tooltip window
		if (tipWindow != null) {
			tipWindow.hide();
			tipWindow = null;
		}

		// Stop the popup timer as user has moved mouse
		if (popupTimer.isRunning()) {
			popupTimer.stop();
		}

		// Called any listeners
		var mousePos = new IntPoint(event.getX(), event.getY());
		mapPanel.notifyMouseMoved(mousePos);

		// Find the hotspot under the cursor
		var hs = findHotspot(mousePos);
		if (hs != null) {
			updateCursor(CROSSHAIR);
			hotspotPoint = event.getPoint();
			pendingTipText = hs.getTooltipText();
			if (pendingTipText != null) {
				// Start the popup time
				popupTimer.start();
			}
			return;
		}

		updateCursor(DEFAULT);
	}

	/**
	 * Show the tooltip window
	 */
	private void showTooltip() {
		var tip = mapPanel.createToolTip();
		tip.setTipText(pendingTipText);

		var factory = PopupFactory.getSharedInstance();

		var locn = mapPanel.getLocationOnScreen();
		locn.translate(hotspotPoint.x, hotspotPoint.y);
		tipWindow = factory.getPopup(mapPanel, tip, locn.x, locn.y);
		tipWindow.show();
	}

	/**
	 * Update the cursor for the map panel. The cursor is only updated if it is different.
	 * @param newCursor
	 */
	private void updateCursor(Cursor newCursor) {
		if (!mapPanel.getCursor().equals(newCursor)) {
			mapPanel.setCursor(newCursor);
		}
	}
}