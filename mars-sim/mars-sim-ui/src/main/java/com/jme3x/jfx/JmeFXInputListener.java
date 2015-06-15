/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3x.jfx;

import java.awt.event.KeyEvent;
import java.util.BitSet;

import javafx.application.Platform;

import com.jme3.input.RawInputListener;
import com.jme3.input.awt.AwtKeyInput;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.event.TouchEvent;
import com.sun.javafx.embed.AbstractEvents;

/**
 * Converts JMEEvents to JFXEvents
 * 
 * @author Heist
 */
public class JmeFXInputListener implements RawInputListener {

	JmeFxContainer				jmeFxContainer;
	private BitSet				keyStateSet			= new BitSet(0xFF);
	private char[]				keyCharSet			= new char[Short.MAX_VALUE * 3];
	boolean[]					mouseButtonState	= new boolean[3];
	private RawInputListener	everListeningInputListenerAdapter;

	private JmeFxDNDHandler		jfxdndHandler;

	public JmeFXInputListener(final JmeFxContainer listensOnContainer) {
		this.jmeFxContainer = listensOnContainer;
	}

	@Override
	public void beginInput() {
		if (this.everListeningInputListenerAdapter != null) {
			this.everListeningInputListenerAdapter.beginInput();
		}
	}

	@Override
	public void endInput() {
		if (this.everListeningInputListenerAdapter != null) {
			this.everListeningInputListenerAdapter.endInput();
		}
	}

	@Override
	public void onJoyAxisEvent(final JoyAxisEvent evt) {
		if (this.everListeningInputListenerAdapter != null) {
			this.everListeningInputListenerAdapter.onJoyAxisEvent(evt);
		}
	}

	@Override
	public void onJoyButtonEvent(final JoyButtonEvent evt) {
		if (this.everListeningInputListenerAdapter != null) {
			this.everListeningInputListenerAdapter.onJoyButtonEvent(evt);
		}
	}

	@Override
	public void onMouseMotionEvent(final MouseMotionEvent evt) {
		if (this.everListeningInputListenerAdapter != null) {
			this.everListeningInputListenerAdapter.onMouseMotionEvent(evt);
		}

		if (this.jmeFxContainer.scenePeer == null) {
			return;
		}

		final int x = evt.getX();
		final int y = (int) Math.round(this.jmeFxContainer.getScene().getHeight()) - evt.getY();

		final boolean covered = this.jmeFxContainer.isCovered(x, y);
		if (covered) {
			evt.setConsumed();
		}

		// not sure if should be grabbing focus on mouse motion event
		// grabFocus();

		int type = AbstractEvents.MOUSEEVENT_MOVED;
		int button = AbstractEvents.MOUSEEVENT_NONE_BUTTON;

		final int wheelRotation = (int) Math.round(evt.getDeltaWheel() / -120.0);

		if (wheelRotation != 0) {
			type = AbstractEvents.MOUSEEVENT_WHEEL;
			button = AbstractEvents.MOUSEEVENT_NONE_BUTTON;
		} else if (this.mouseButtonState[0]) {
			type = AbstractEvents.MOUSEEVENT_DRAGGED;
			button = AbstractEvents.MOUSEEVENT_PRIMARY_BUTTON;
		} else if (this.mouseButtonState[1]) {
			type = AbstractEvents.MOUSEEVENT_DRAGGED;
			button = AbstractEvents.MOUSEEVENT_SECONDARY_BUTTON;
		} else if (this.mouseButtonState[2]) {
			type = AbstractEvents.MOUSEEVENT_DRAGGED;
			button = AbstractEvents.MOUSEEVENT_MIDDLE_BUTTON;
		}
		final int ftype = type;
		final int fbutton = button;
		/**
		 * ensure drag and drop is handled before the mouse release event fires
		 */
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (JmeFXInputListener.this.jfxdndHandler != null) {
					JmeFXInputListener.this.jfxdndHandler.mouseUpdate(x, y, JmeFXInputListener.this.mouseButtonState[0]);
				}
				JmeFXInputListener.this.jmeFxContainer.scenePeer.mouseEvent(ftype, fbutton, JmeFXInputListener.this.mouseButtonState[0], JmeFXInputListener.this.mouseButtonState[1], JmeFXInputListener.this.mouseButtonState[2], x, y,
						JmeFXInputListener.this.jmeFxContainer.getXPosition() + x, JmeFXInputListener.this.jmeFxContainer.getYPosition() + y, JmeFXInputListener.this.keyStateSet.get(KeyEvent.VK_SHIFT),
						JmeFXInputListener.this.keyStateSet.get(KeyEvent.VK_CONTROL), JmeFXInputListener.this.keyStateSet.get(KeyEvent.VK_ALT), JmeFXInputListener.this.keyStateSet.get(KeyEvent.VK_META), wheelRotation, false);

			}
		});
	}

	@Override
	public void onMouseButtonEvent(final MouseButtonEvent evt) {

		if (this.everListeningInputListenerAdapter != null) {
			this.everListeningInputListenerAdapter.onMouseButtonEvent(evt);
		}

		// TODO: Process events in separate thread ?
		if (this.jmeFxContainer.scenePeer == null) {
			return;
		}

		final int x = evt.getX();
		final int y = (int) Math.round(this.jmeFxContainer.getScene().getHeight()) - evt.getY();

		int button;

		switch (evt.getButtonIndex()) {
		case 0:
			button = AbstractEvents.MOUSEEVENT_PRIMARY_BUTTON;
			break;
		case 1:
			button = AbstractEvents.MOUSEEVENT_SECONDARY_BUTTON;
			break;
		case 2:
			button = AbstractEvents.MOUSEEVENT_MIDDLE_BUTTON;
			break;
		default:
			return;
		}

		this.mouseButtonState[evt.getButtonIndex()] = evt.isPressed();

		// seems that generating mouse release without corresponding mouse pressed is causing problems in Scene.ClickGenerator

		final boolean covered = this.jmeFxContainer.isCovered(x, y);
		if (!covered) {
			this.jmeFxContainer.loseFocus();
		} else {
			evt.setConsumed();
			this.jmeFxContainer.grabFocus();
		}
		;
		int type;
		if (evt.isPressed()) {
			type = AbstractEvents.MOUSEEVENT_PRESSED;
		} else if (evt.isReleased()) {
			type = AbstractEvents.MOUSEEVENT_RELEASED;
			// and clicked ??
		} else {
			return;
		}
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (JmeFXInputListener.this.jfxdndHandler != null) {
					JmeFXInputListener.this.jfxdndHandler.mouseUpdate(x, y, JmeFXInputListener.this.mouseButtonState[0]);
				}
				JmeFXInputListener.this.jmeFxContainer.scenePeer.mouseEvent(type, button, JmeFXInputListener.this.mouseButtonState[0], JmeFXInputListener.this.mouseButtonState[1], JmeFXInputListener.this.mouseButtonState[2], x, y,
						JmeFXInputListener.this.jmeFxContainer.getXPosition() + x, JmeFXInputListener.this.jmeFxContainer.getYPosition() + y, JmeFXInputListener.this.keyStateSet.get(KeyEvent.VK_SHIFT),
						JmeFXInputListener.this.keyStateSet.get(KeyEvent.VK_CONTROL), JmeFXInputListener.this.keyStateSet.get(KeyEvent.VK_ALT), JmeFXInputListener.this.keyStateSet.get(KeyEvent.VK_META), 0,
						button == AbstractEvents.MOUSEEVENT_SECONDARY_BUTTON);
			}
		});
	}

	@Override
	public void onKeyEvent(final KeyInputEvent evt) {

		if (this.everListeningInputListenerAdapter != null) {
			this.everListeningInputListenerAdapter.onKeyEvent(evt);
		}

		if (this.jmeFxContainer.scenePeer == null) {
			return;
		}

		final char keyChar = evt.getKeyChar();

		int fxKeycode = AwtKeyInput.convertJmeCode(evt.getKeyCode());

		final int keyState = this.retrieveKeyState();
		if (fxKeycode > this.keyCharSet.length) {
			switch (keyChar) {
			case '\\':
				fxKeycode = java.awt.event.KeyEvent.VK_BACK_SLASH;
				break;
			default:
				return;
			}
		}
		if (this.jmeFxContainer.focus) {
			evt.setConsumed();
		}

		if (evt.isRepeating()) {
			final char x = this.keyCharSet[fxKeycode];

			final int eventType;
			if (fxKeycode == KeyEvent.VK_BACK_SPACE || fxKeycode == KeyEvent.VK_DELETE || fxKeycode == KeyEvent.VK_KP_UP || fxKeycode == KeyEvent.VK_KP_DOWN || fxKeycode == KeyEvent.VK_KP_LEFT || fxKeycode == KeyEvent.VK_KP_RIGHT
					|| fxKeycode == KeyEvent.VK_UP || fxKeycode == KeyEvent.VK_DOWN || fxKeycode == KeyEvent.VK_LEFT || fxKeycode == KeyEvent.VK_RIGHT)
				eventType = AbstractEvents.KEYEVENT_PRESSED;
			else
				eventType = AbstractEvents.KEYEVENT_TYPED;

			if (this.jmeFxContainer.focus) {
				this.jmeFxContainer.scenePeer.keyEvent(eventType, fxKeycode, new char[] { x }, keyState);
			}
		} else if (evt.isPressed()) {
			this.keyCharSet[fxKeycode] = keyChar;
			this.keyStateSet.set(fxKeycode);
			if (this.jmeFxContainer.focus) {
				this.jmeFxContainer.scenePeer.keyEvent(AbstractEvents.KEYEVENT_PRESSED, fxKeycode, new char[] { keyChar }, keyState);
				this.jmeFxContainer.scenePeer.keyEvent(AbstractEvents.KEYEVENT_TYPED, fxKeycode, new char[] { keyChar }, keyState);
			}
		} else {
			final char x = this.keyCharSet[fxKeycode];
			this.keyStateSet.clear(fxKeycode);
			if (this.jmeFxContainer.focus) {
				this.jmeFxContainer.scenePeer.keyEvent(AbstractEvents.KEYEVENT_RELEASED, fxKeycode, new char[] { x }, keyState);
			}
		}

	}

	@Override
	public void onTouchEvent(final TouchEvent evt) {
		if (this.everListeningInputListenerAdapter != null) {
			this.everListeningInputListenerAdapter.onTouchEvent(evt);
		}
	}

	public int retrieveKeyState() {
		int embedModifiers = 0;

		if (this.keyStateSet.get(KeyEvent.VK_SHIFT)) {
			embedModifiers |= AbstractEvents.MODIFIER_SHIFT;
		}

		if (this.keyStateSet.get(KeyEvent.VK_CONTROL)) {
			embedModifiers |= AbstractEvents.MODIFIER_CONTROL;
		}

		if (this.keyStateSet.get(KeyEvent.VK_ALT)) {
			embedModifiers |= AbstractEvents.MODIFIER_ALT;
		}

		if (this.keyStateSet.get(KeyEvent.VK_META)) {
			embedModifiers |= AbstractEvents.MODIFIER_META;
		}
		return embedModifiers;
	}

	public void setEverListeningRawInputListener(final RawInputListener rawInputListenerAdapter) {
		this.everListeningInputListenerAdapter = rawInputListenerAdapter;
	}

	/**
	 * set on drag start /nulled on end<br>
	 * necessary so that the drag events can be generated appropiatly
	 * 
	 * @param jfxdndHandler
	 */
	public void setMouseDNDListener(final JmeFxDNDHandler jfxdndHandler) {
		assert this.jfxdndHandler == null || jfxdndHandler == null : "duplicate jfxdndn handler register? ";
		this.jfxdndHandler = jfxdndHandler;
	}
}
