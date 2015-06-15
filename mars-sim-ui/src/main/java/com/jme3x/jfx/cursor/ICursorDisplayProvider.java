package com.jme3x.jfx.cursor;

import com.sun.javafx.cursor.CursorFrame;
import com.sun.javafx.cursor.CursorType;

public interface ICursorDisplayProvider {

	/**
	 * called by the GuiManager during startup, should be used to create the necessary cursors
	 * 
	 * @param normal
	 */
	void setup(CursorType normal);

	void showCursor(CursorFrame cursorFrame);

}
