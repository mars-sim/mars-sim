package org.mars_sim.msp.ui.jme3.jme3FX;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import com.jme3.app.Application;

public class CameraDriverInput {
	public static void bindDefaults(JComponent c, CameraDriverInput driver) {
		InputMap im = c.getInputMap(JComponent.WHEN_FOCUSED);
		ActionMap am = c.getActionMap();
		bind(im, am, KeyEvent.VK_PAGE_UP, 0, driver.upPressed, driver.upReleased);
		bind(im, am, KeyEvent.VK_PAGE_DOWN, 0, driver.downPressed, driver.downReleased);

		// arrow
		bind(im, am, KeyEvent.VK_UP, 0, driver.forwardPressed, driver.forwardReleased);
		bind(im, am, KeyEvent.VK_LEFT, 0, driver.leftPressed, driver.leftReleased);
		bind(im, am, KeyEvent.VK_DOWN, 0, driver.backwardPressed, driver.backwardReleased);
		bind(im, am, KeyEvent.VK_RIGHT, 0, driver.rightPressed, driver.rightReleased);

		//WASD
		bind(im, am, KeyEvent.VK_W, 0, driver.forwardPressed, driver.forwardReleased);
		bind(im, am, KeyEvent.VK_A, 0, driver.leftPressed, driver.leftReleased);
		bind(im, am, KeyEvent.VK_S, 0, driver.backwardPressed, driver.backwardReleased);
		bind(im, am, KeyEvent.VK_D, 0, driver.rightPressed, driver.rightReleased);

		// ZQSD
		bind(im, am, KeyEvent.VK_Z, 0, driver.forwardPressed, driver.forwardReleased);
		bind(im, am, KeyEvent.VK_Q, 0, driver.leftPressed, driver.leftReleased);
	}

	public static void bind(InputMap im, ActionMap am,int keyCode, int modifier, Action activate, Action unactivate) {
		bindInput(im, keyCode, modifier, activate, unactivate);
		bindAction(am, activate, unactivate);
	}

	public static void bindInput(InputMap m, int keyCode, int modifier, Action activate, Action unactivate) {
		m.put(KeyStroke.getKeyStroke(keyCode, modifier, false), activate);
		m.put(KeyStroke.getKeyStroke(keyCode, modifier, true), unactivate);
	}

	public static void bindAction(ActionMap m, Action activate, Action unactivate) {
		m.put(activate, activate);
		m.put(unactivate, unactivate);
	}

	// mapping should be improve (allow remapping, multi key mapping, avoid same key for several action, what is done by java Key Bindings ;-))

	public Application jme;
	public float speed = 1.0f;


	protected CameraDriverAppState driver() {
		if (jme == null) return null;
		return jme.getStateManager().getState(CameraDriverAppState.class);
	}

	public final Action upPressed = new AbstractAction(){
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			CameraDriverAppState d = driver();
			if (d != null) d.up = 1.0f * speed;
		}
	};
	public final Action upReleased = new AbstractAction(){
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			CameraDriverAppState d = driver();
			if (d != null) d.up = 0.0f * speed;
		}
	};
	public final Action downPressed = new AbstractAction(){
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			CameraDriverAppState d = driver();
			if (d != null) d.down = 1.0f * speed;
		}
	};
	public final Action downReleased = new AbstractAction(){
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			CameraDriverAppState d = driver();
			if (d != null) d.down = 0.0f * speed;
		}
	};
	public final Action rightPressed = new AbstractAction(){
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			CameraDriverAppState d = driver();
			if (d != null) d.right = 1.0f * speed;
		}
	};
	public final Action rightReleased = new AbstractAction(){
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			CameraDriverAppState d = driver();
			if (d != null) d.right = 0.0f * speed;
		}
	};
	public final Action leftPressed = new AbstractAction(){
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			CameraDriverAppState d = driver();
			if (d != null) d.left = 1.0f * speed;
		}
	};
	public final Action leftReleased = new AbstractAction(){
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			CameraDriverAppState d = driver();
			if (d != null) d.left = 0.0f * speed;
		}
	};
	public final Action forwardPressed = new AbstractAction(){
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			CameraDriverAppState d = driver();
			if (d != null) d.forward = 1.0f * speed;
		}
	};
	public final Action forwardReleased = new AbstractAction(){
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			CameraDriverAppState d = driver();
			if (d != null) d.forward = 0.0f * speed;
		}
	};
	public final Action backwardPressed = new AbstractAction(){
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			CameraDriverAppState d = driver();
			if (d != null) d.backward = 1.0f * speed;
		}
	};
	public final Action backwardReleased = new AbstractAction(){
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			CameraDriverAppState d = driver();
			if (d != null) d.backward = 0.0f * speed;
		}
	};

}
