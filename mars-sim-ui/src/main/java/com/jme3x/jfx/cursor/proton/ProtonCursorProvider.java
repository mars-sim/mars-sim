package com.jme3x.jfx.cursor.proton;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.cursors.plugins.JmeCursor;
import com.jme3.input.InputManager;
import com.jme3x.jfx.cursor.ICursorDisplayProvider;
import com.sun.javafx.cursor.CursorFrame;
import com.sun.javafx.cursor.CursorType;

/**
 * http://www.rw-designer.com/cursor-set/proton by juanello <br>
 * A cursorProvider that simulates the native JFX one and tries to behave similar,<br>
 * using native cursors and 2D surface logic.
 *
 * @author empire
 *
 */
public class ProtonCursorProvider implements ICursorDisplayProvider {
	private static final Logger							logger	= LoggerFactory.getLogger(ProtonCursorProvider.class);

	private ConcurrentHashMap<CursorType, JmeCursor>	cache	= new ConcurrentHashMap<CursorType, JmeCursor>();
	private AssetManager								assetManager;
	private InputManager								inputManager;
	private Application									app;

	public ProtonCursorProvider(final Application app, final AssetManager assetManager, final InputManager inputManager) {
		this.assetManager = assetManager;
		this.inputManager = inputManager;
		this.app = app;
		assetManager.registerLocator("", ClasspathLocator.class);
	}

	@Override
	public void showCursor(final CursorFrame cursorFrame) {
		CursorType cursorType = cursorFrame.getCursorType();
		if (this.cache.get(cursorType) == null) {
			ProtonCursorProvider.logger.debug("Unkown Cursor! {}", cursorType);
			cursorType = CursorType.DEFAULT;
		}

		final JmeCursor toDisplay = this.cache.get(cursorType);

		if (toDisplay != null) {
			this.app.enqueue(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					ProtonCursorProvider.this.inputManager.setMouseCursor(toDisplay);
					return null;
				}
			});
		}
	}

	@Override
	public void setup(final CursorType ctyp) {
		JmeCursor loaded = null;
		switch (ctyp) {
		case CLOSED_HAND:
			break;
		case CROSSHAIR:
			loaded = (JmeCursor) this.assetManager.loadAsset("com/jme3x/jfx/cursor/proton/aero_cross.cur");
			break;
		case DEFAULT:
			loaded = (JmeCursor) this.assetManager.loadAsset("com/jme3x/jfx/cursor/proton/aero_arrow.cur");
			break;
		case DISAPPEAR:
			break;
		case E_RESIZE:
			loaded = (JmeCursor) this.assetManager.loadAsset("com/jme3x/jfx/cursor/proton/aero_ew.cur");
			loaded.setxHotSpot(16);
			loaded.setyHotSpot(16);
			break;
		case HAND:
			loaded = (JmeCursor) this.assetManager.loadAsset("com/jme3x/jfx/cursor/proton/aero_link.cur");
			break;
		case H_RESIZE:
			loaded = (JmeCursor) this.assetManager.loadAsset("com/jme3x/jfx/cursor/proton/aero_ew.cur");
			loaded.setxHotSpot(16);
			loaded.setyHotSpot(16);
			break;
		case IMAGE:
			break;
		case MOVE:
			loaded = (JmeCursor) this.assetManager.loadAsset("com/jme3x/jfx/cursor/proton/aero_move.cur");
			loaded.setxHotSpot(16);
			loaded.setyHotSpot(16);
			break;
		case NE_RESIZE:
			loaded = (JmeCursor) this.assetManager.loadAsset("com/jme3x/jfx/cursor/proton/aero_nesw.cur");
			loaded.setxHotSpot(16);
			loaded.setyHotSpot(16);
			break;
		case NONE:
			break;
		case NW_RESIZE:
			loaded = (JmeCursor) this.assetManager.loadAsset("com/jme3x/jfx/cursor/proton/aero_nwse.cur");
			loaded.setxHotSpot(16);
			loaded.setyHotSpot(16);
			break;
		case N_RESIZE:
			loaded = (JmeCursor) this.assetManager.loadAsset("com/jme3x/jfx/cursor/proton/aero_ns.cur");
			loaded.setxHotSpot(16);
			loaded.setyHotSpot(16);
			break;
		case OPEN_HAND:
			break;
		case SE_RESIZE:
			loaded = (JmeCursor) this.assetManager.loadAsset("com/jme3x/jfx/cursor/proton/aero_nwse.cur");
			loaded.setxHotSpot(16);
			loaded.setyHotSpot(16);
			break;
		case SW_RESIZE:
			loaded = (JmeCursor) this.assetManager.loadAsset("com/jme3x/jfx/cursor/proton/aero_nesw.cur");
			loaded.setxHotSpot(16);
			loaded.setyHotSpot(16);
			break;
		case S_RESIZE:
			loaded = (JmeCursor) this.assetManager.loadAsset("com/jme3x/jfx/cursor/proton/aero_ns.cur");
			loaded.setxHotSpot(16);
			loaded.setyHotSpot(16);
			break;
		case TEXT:
			loaded = (JmeCursor) this.assetManager.loadAsset("com/jme3x/jfx/cursor/proton/aero_text.cur");
			loaded.setxHotSpot(16);
			loaded.setyHotSpot(16);
			break;
		case V_RESIZE:
			loaded = (JmeCursor) this.assetManager.loadAsset("com/jme3x/jfx/cursor/proton/aero_ns.cur");
			break;
		case WAIT:
			loaded = (JmeCursor) this.assetManager.loadAsset("com/jme3x/jfx/cursor/proton/aero_busy.ani");
			break;
		case W_RESIZE:
			loaded = (JmeCursor) this.assetManager.loadAsset("com/jme3x/jfx/cursor/proton/aero_ew.cur");
			break;
		}
		if (loaded != null) {
			this.cache.put(ctyp, loaded);
		}
	}
}
