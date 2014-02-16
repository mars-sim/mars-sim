package org.mars_sim.msp.ui.ogl.sandbox.scene;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.jogamp.opengl.util.FPSAnimator;

/**
 * @author Pepijn Van Eeckhoudt -- originally taken from some downloaded tutorial examples.
 * @author st.pa. -- changed a bit.
 */
public class GLDisplay {

	private static final int TARGET_FRAME_RATE = 30;
	private static final int DEFAULT_WIDTH = 640;
	private static final int DEFAULT_HEIGHT = 480;

	private static final int DONT_CARE = -1;

	private JFrame frame;
	private GLCanvas glCanvas;
	private FPSAnimator animator;
	private boolean fullscreen;
	private int width;
	private int height;
	private GraphicsDevice usedDevice;

	private MyGLEventListener glEventListener = new MyGLEventListener();
	private MyExceptionHandler exceptionHandler = new MyExceptionHandler();

	public static GLDisplay createGLDisplay(String title) {
		return createGLDisplay(title, new GLCapabilities(null));
	}

	public static GLDisplay createGLDisplay(String title, GLCapabilities caps) {
		boolean fullscreen = false;
		return new GLDisplay(title, DEFAULT_WIDTH, DEFAULT_HEIGHT, fullscreen, caps);
	}

	private GLDisplay(String title, int width, int height, boolean fullscreen, GLCapabilities caps) {
		glCanvas = new GLCanvas(caps);
		glCanvas.setSize(width, height);
		glCanvas.setIgnoreRepaint(true);
		glCanvas.addGLEventListener(glEventListener);

		frame = new JFrame(title);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(glCanvas, BorderLayout.CENTER);

		this.fullscreen = fullscreen;
		this.width = width;
		this.height = height;

		animator = new FPSAnimator(glCanvas, TARGET_FRAME_RATE);
//		animator.setRunAsFastAsPossible(false);
	}

	public void start() {
		try {
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			frame.setUndecorated(fullscreen);

			frame.addWindowListener(new MyWindowAdapter());

			if (fullscreen) {
				usedDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
				usedDevice.setFullScreenWindow(frame);
				usedDevice.setDisplayMode(
					findDisplayMode(
						usedDevice.getDisplayModes(),
						width, height,
						usedDevice.getDisplayMode().getBitDepth(),
						usedDevice.getDisplayMode().getRefreshRate()
					)
				);
			} else {
				frame.setSize(frame.getContentPane().getPreferredSize());
				frame.setLocation(
					(screenSize.width - frame.getWidth()) / 2,
					(screenSize.height - frame.getHeight()) / 2
				);
				frame.setVisible(true);
			}

			glCanvas.requestFocus();

			animator.start();
		} catch (Exception e) {
			exceptionHandler.handleException(e);
		}
	}

	public void stop() {
		try {
			animator.stop();
			if (fullscreen) {
				usedDevice.setFullScreenWindow(null);
				usedDevice = null;
			}
			frame.dispose();
		} catch (Exception e) {
			exceptionHandler.handleException(e);
		} finally {
			System.exit(0);
		}
	}

	private DisplayMode findDisplayMode(
		DisplayMode[] displayModes,
		int requestedWidth,
		int requestedHeight,
		int requestedDepth,
		int requestedRefreshRate
	) {
		// Try to find an exact match
		DisplayMode displayMode = findDisplayModeInternal(displayModes, requestedWidth, requestedHeight, requestedDepth, requestedRefreshRate);

		// Try again, ignoring the requested bit depth
		if (displayMode == null)
			displayMode = findDisplayModeInternal(displayModes, requestedWidth, requestedHeight, DONT_CARE, DONT_CARE);

		// Try again, and again ignoring the requested bit depth and height
		if (displayMode == null)
			displayMode = findDisplayModeInternal(displayModes, requestedWidth, DONT_CARE, DONT_CARE, DONT_CARE);

		// If all else fails try to get any display mode
		if (displayMode == null)
			displayMode = findDisplayModeInternal(displayModes, DONT_CARE, DONT_CARE, DONT_CARE, DONT_CARE);

		return displayMode;
	}

	private DisplayMode findDisplayModeInternal(
		DisplayMode[] displayModes,
		int requestedWidth,
		int requestedHeight,
		int requestedDepth,
		int requestedRefreshRate
	) {
		DisplayMode displayModeToUse = null;
		for (int i = 0; i < displayModes.length; i++) {
			DisplayMode displayMode = displayModes[i];
			if (
				(requestedWidth == DONT_CARE || displayMode.getWidth() == requestedWidth) &&
				(requestedHeight == DONT_CARE || displayMode.getHeight() == requestedHeight) &&
				(requestedHeight == DONT_CARE || displayMode.getRefreshRate() == requestedRefreshRate) &&
				(requestedDepth == DONT_CARE || displayMode.getBitDepth() == requestedDepth)
			) {
				displayModeToUse = displayMode;
			}
		}
		return displayModeToUse;
	}

	public void addKeyListener(KeyListener l) {
		glCanvas.addKeyListener(l);
	}

	public void addMouseListener(MouseListener l) {
		glCanvas.addMouseListener(l);
	}

	public void addMouseWheelListener(MouseWheelListener l) {
		glCanvas.addMouseWheelListener(l);
	}

	public void addMouseMotionListener(MouseMotionListener l) {
		glCanvas.addMouseMotionListener(l);
	}

	public void removeKeyListener(KeyListener l) {
		glCanvas.removeKeyListener(l);
	}

	public void removeMouseListener(MouseListener l) {
		glCanvas.removeMouseListener(l);
	}

	public void removeMouseMotionListener(MouseMotionListener l) {
		glCanvas.removeMouseMotionListener(l);
	}

	public String getTitle() {
		return frame.getTitle();
	}

	public void setTitle(String title) {
		frame.setTitle(title);
	}

	private class MyWindowAdapter extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			stop();
		}
	}

	private class MyExceptionHandler {
		public void handleException(final Exception e) {
			SwingUtilities.invokeLater(
					new Runnable() {
						public void run() {
							StringWriter stringWriter = new StringWriter();
							PrintWriter printWriter = new PrintWriter(stringWriter);
							e.printStackTrace(printWriter);
							JOptionPane.showMessageDialog(frame, stringWriter.toString(), "Exception occurred", JOptionPane.ERROR_MESSAGE);
							stop();
						}
					}
					);
		}
	}

	public void addGLEventListener(DisplayAbstract display) {
		this.glEventListener.addGLEventListener(display);
	}

	private static class MyGLEventListener implements GLEventListener {

		private java.util.List<GLEventListener> eventListeners = new ArrayList<GLEventListener>();

		public void addGLEventListener(GLEventListener glEventListener) {
			eventListeners.add(glEventListener);
		}

		public void display(GLAutoDrawable glDrawable) {
			for (int i = 0; i < eventListeners.size(); i++) {
				eventListeners.get(i).display(glDrawable);
			}
		}

		public void init(GLAutoDrawable glDrawable) {
			for (int i = 0; i < eventListeners.size(); i++) {
				eventListeners.get(i).init(glDrawable);
			}
		}

		public void reshape(GLAutoDrawable glDrawable, int i0, int i1, int i2, int i3) {
			for (int i = 0; i < eventListeners.size(); i++) {
				eventListeners.get(i).reshape(glDrawable, i0, i1, i2, i3);
			}
		}
		/*
		public void displayChanged(GLAutoDrawable glDrawable, boolean b, boolean b1) {
            for (int i = 0; i < eventListeners.size(); i++) {
                eventListeners.get(i).displayChanged(glDrawable,b,b1);
            }
		}
		 */
		@Override
		public void dispose(GLAutoDrawable arg0) {
			for (GLEventListener el : eventListeners) {
				el.dispose(arg0);
			}
		}
	}
}
