/**
 * Copyright (c) 2011-2013 Bernhard Pauler, Tim Molderez.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the 3-Clause BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/BSD-3-Clause
 */

package net.java.balloontip;

import java.awt.AlphaComposite;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.java.balloontip.positioners.BalloonTipPositioner;
import net.java.balloontip.positioners.BasicBalloonTipPositioner;
import net.java.balloontip.positioners.LeftAbovePositioner;
import net.java.balloontip.positioners.LeftBelowPositioner;
import net.java.balloontip.positioners.RightAbovePositioner;
import net.java.balloontip.positioners.RightBelowPositioner;
import net.java.balloontip.styles.BalloonTipStyle;
import net.java.balloontip.styles.RoundedBalloonStyle;

/**
 * A balloon tip Swing component that is attached to a JComponent and uses another JComponent as contents
 * @author Bernhard Pauler
 * @author Tim Molderez
 * @author Thierry Blind
 */
public class BalloonTip extends JPanel {
	/** Should the balloon be placed above, below, right or left of the attached component? */
	public enum Orientation {LEFT_ABOVE, RIGHT_ABOVE, LEFT_BELOW, RIGHT_BELOW}

	/** Where should the balloon's tip be located, relative to the attached component
	 * ; ALIGNED makes sure the balloon's edge is aligned with the attached component */
	public enum AttachLocation {ALIGNED, CENTER, NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST}

	protected JComponent contents = null;
	protected JButton closeButton = null;
	protected VisibilityControl visibilityControl = new VisibilityControl();
	protected BalloonTipStyle style;					// Determines the balloon's looks
	protected int padding = 0;							// Amount of pixels padding around the contents
	protected float opacity = 1.0f;						// The balloon tip's opacity (1.0 is opaque)
	protected BalloonTipPositioner positioner;			// Determines the balloon tip's position
	protected JLayeredPane topLevelContainer = null;	// The balloon tip is drawn on this pane
	protected JComponent attachedComponent;				// The balloon tip is attached to this component

	private static Icon defaultCloseIcon  = new ImageIcon(BalloonTip.class.getResource("/net/java/balloontip/images/close_default.png"));
	private static Icon rolloverCloseIcon = new ImageIcon(BalloonTip.class.getResource("/net/java/balloontip/images/close_rollover.png"));
	private static Icon pressedCloseIcon  = new ImageIcon(BalloonTip.class.getResource("/net/java/balloontip/images/close_pressed.png"));

	// Only show a balloon tip when the component it's attached to is visible
	private final ComponentListener componentListener = new ComponentListener() {
		public void componentMoved(ComponentEvent e) {
			refreshLocation();
		}
		public void componentResized(ComponentEvent e) {
			/* We're assuming here that components can only resize when they are visible!
			 * (If we would use isAttachedComponentShowing(), the JApplet test will fail.
			 * Perhaps this indicates a bug in Component.isShowing() when using components in a JApplet..) */
			visibilityControl.setCriterionAndUpdate("attachedComponentShowing",
					attachedComponent.getWidth() > 0 && attachedComponent.getHeight() > 0);
			refreshLocation();
		}
		public void componentShown(ComponentEvent e) {
			visibilityControl.setCriterionAndUpdate("attachedComponentShowing",isAttachedComponentShowing());
			refreshLocation();
		}
		public void componentHidden(ComponentEvent e) {
			visibilityControl.setCriterionAndUpdate("attachedComponentShowing",false);
		}
	};

	// Adjust the balloon tip when the top-level container is resized
	private final ComponentAdapter topLevelContainerListener = new ComponentAdapter() {
		public void componentResized(ComponentEvent e) {
			refreshLocation();
		}
	};

	// Adjust the balloon tip's visibility when switching tabs
	private ComponentAdapter tabbedPaneListener = null;

	// Hide the balloon tip when its tip is outside a viewport
	protected NestedViewportListener viewportListener = null;

	// Behaviour when the balloon tip is clicked
	private MouseAdapter clickListener = null;
	
	// Delays construction of a balloon tip in case the top-level container is not available yet
	private AncestorListener ancestorListener = null;

	/**
	 * Constructor
	 * The simplest constructor, a balloon tip with some text and a default look
	 * @param attachedComponent		attach the balloon tip to this component (may not be null)
	 * @param text					the contents of the balloon tip (may contain HTML)
	 */
	public BalloonTip(JComponent attachedComponent, String text) {
		this(attachedComponent, text, new RoundedBalloonStyle(5,5,Color.WHITE, Color.BLACK), true);
	}

	/**
	 * Constructor
	 * A simple constructor for a balloon tip containing text, a custom look and optionally a close button
	 * @param attachedComponent		attach the balloon tip to this component (may not be null)
	 * @param text					the contents of the balloon tip (may contain HTML)
	 * @param style					the balloon tip's looks (may not be null)
	 * @param useCloseButton		if true, the balloon tip gets a default close button
	 */
	public BalloonTip(JComponent attachedComponent, String text, BalloonTipStyle style, boolean useCloseButton) {
		this(attachedComponent, new JLabel(text), style, useCloseButton);
	}

	/**
	 * Constructor
	 * @param attachedComponent		attach the balloon tip to this component (may not be null)
	 * @param contents				the balloon tip's contents (may be null)
	 * @param style					the balloon tip's looks (may not be null)
	 * @param useCloseButton		if true, the balloon tip gets a close button
	 */
	public BalloonTip(JComponent attachedComponent, JComponent contents, BalloonTipStyle style, boolean useCloseButton) {
		this(attachedComponent, contents, style, Orientation.LEFT_ABOVE, AttachLocation.ALIGNED, 15, 15, useCloseButton);
	}

	/**
	 * Constructor
	 * @param attachedComponent		attach the balloon tip to this component (may not be null)
	 * @param contents				the balloon tip's contents (may be null)
	 * @param style					the balloon tip's looks (may not be null)
	 * @param orientation			orientation of the balloon tip
	 * @param attachLocation		location of the balloon's tip  within the attached component
	 * @param horizontalOffset		horizontal offset for the balloon's tip
	 * @param verticalOffset		vertical offset for the balloon's tip
	 * @param useCloseButton		if true, the balloon tip gets a close button
	 */
	public BalloonTip(final JComponent attachedComponent, final JComponent contents, final BalloonTipStyle style, Orientation orientation, AttachLocation attachLocation,
			int horizontalOffset, int verticalOffset, final boolean useCloseButton) {
		super();
		setup(attachedComponent, contents, style, setupPositioner(orientation, attachLocation, horizontalOffset, verticalOffset),
				useCloseButton?getDefaultCloseButton():null);
	}

	/**
	 * Constructor - the most customizable balloon tip constructor
	 * @param attachedComponent		attach the balloon tip to this component (may not be null)
	 * @param contents				the contents of the balloon tip (may be null)
	 * @param style					the balloon tip's looks (may not be null)
	 * @param positioner			determines the way the balloon tip is positioned (may not be null)
	 * @param closeButton			the close button to be used for the balloon tip (may be null)
	 */
	public BalloonTip(JComponent attachedComponent, JComponent contents, BalloonTipStyle style, BalloonTipPositioner positioner, JButton closeButton) {
		super();
		setup(attachedComponent, contents, style, positioner, closeButton);
	}

	/**
	 * Sets the contents of this balloon tip
	 * (Calling this method will fire a "contents" property change event.)
	 * @param contents		a JComponent that represents the balloon tip's contents
	 * 						If the contents is null, the balloon tip will not be shown
	 */
	public void setContents(JComponent contents) {
		JComponent oldContents = this.contents;
		if (oldContents!=null) {
			remove(this.contents);
		}
		this.contents=contents;

		if (contents!=null) {
			setPadding(getPadding());
			add(this.contents, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			visibilityControl.setCriterionAndUpdate("hasContents", true);
		} else {
			visibilityControl.setCriterionAndUpdate("hasContents", false);
		}

		// Notify property listeners that the contents has changed
		firePropertyChange("contents", oldContents, this.contents);
		refreshLocation();
	}
	
	/**
	 * Sets the contents of this balloon tip
	 * (Calling this method will fire a "contents" property change event.)
	 * @param text		the text to be shown in the balloon tip (may contain HTML)
	 */
	public void setTextContents(String text) {
		setContents(new JLabel(text));
	}

	/**
	 * Retrieve this balloon tip's contents
	 * @return				the JComponent representing the contents of this balloon tip (can be null)
	 */
	public JComponent getContents() {
		return this.contents;
	}

	/**
	 * Set the amount of padding in this balloon tip
	 * (by attaching an empty border to the balloon tip's contents...)
	 * @param padding	the amount of padding in pixels
	 */
	public void setPadding(int padding) {
		this.padding=padding;
		contents.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
		refreshLocation();
	}

	/**
	 * Get the amount of padding in this balloon tip
	 * @return			the amount of padding in pixels
	 */
	public int getPadding() {
		return padding;
	}

	/**
	 * Set the balloon tip's style
	 * (Calling this method will fire a "style" property change event.)
	 * @param style			a BalloonTipStyle (may not be null)
	 */
	public void setStyle(BalloonTipStyle style) {
		BalloonTipStyle oldStyle = this.style;
		this.style = style;
		setBorder(this.style);

		// Notify property listeners that the style has changed
		firePropertyChange("style", oldStyle, style);
		refreshLocation();
	}

	/**
	 * Get the balloon tip's style
	 * @return				the balloon tip's style
	 */
	public BalloonTipStyle getStyle() {
		return style;
	}

	/**
	 * Set a new BalloonTipPositioner, repsonsible for the balloon tip's positioning
	 * (Calling this method will fire a "positioner" property change event.)
	 * @param positioner	a BalloonTipPositioner (may not be null)
	 */
	public void setPositioner(BalloonTipPositioner positioner) {
		BalloonTipPositioner oldPositioner = this.positioner;
		this.positioner = positioner;
		this.positioner.setBalloonTip(this);

		// Notify property listeners that the positioner has changed
		firePropertyChange("positioner", oldPositioner, positioner);
		refreshLocation();
	}

	/**
	 * Retrieve the BalloonTipPositioner that is used by this balloon tip
	 * @return The balloon tip's positioner
	 */
	public BalloonTipPositioner getPositioner() {
		return positioner;
	}

	/**
	 * If you want to permanently close the balloon, you can use this method.
	 * (It will be called automatically once Java's garbage collector can clean up this balloon tip...)
	 * Please note, you shouldn't use this instance anymore after calling this method!
	 * (If you just want to hide the balloon tip, simply use setVisible(false);)
	 */
	public void closeBalloon() {
		forceSetVisible(false);
		setCloseButton(null); // Remove the close button
		for(MouseListener m : getMouseListeners()) {
			removeMouseListener(m);
		}
		tearDownHelper();
	}

	/**
	 * Sets this balloon tip's close button
	 * Note that this method will not alter the button's behaviour. You're expected to set it yourself.
	 * @param button		the new close button; if null, the balloon tip's close button is removed (if it had one)
	 */
	public void setCloseButton(JButton button) {
		// Remove the current button
		// TODO: why if (closeButton != null) {
		if (closeButton != null) {
			for (ActionListener a: closeButton.getActionListeners()) {
				closeButton.removeActionListener(a);
			}
			remove(closeButton);
			closeButton = null;
		}
		
		// Set the new button
		if (button!=null) {
			closeButton = button;
			add(closeButton, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		}
		
		refreshLocation();
	}

	/**
	 * Sets this balloon tip's close button and sets its behaviour to either close or hide the balloon tip
	 * @param button			the new close button; if null, the balloon tip's close button is removed (if it had one)
	 * @param permanentClose	if true, the button's behaviour is to close the balloon tip permanently by calling closeBalloon()
	 * 							if false, the button's behaviour is to hide the balloon tip by calling setVisible(false)
	 */
	public void setCloseButton(JButton button, boolean permanentClose) {
		if (button!=null) {
			if (permanentClose) {
				button.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						closeBalloon();
					}
				});
			} else {
				button.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
					}
				});
			}
		}
		setCloseButton(button);
	}

	/**
	 * Retrieve this balloon tip's close button
	 * @return		the close button (null if not present)
	 */
	public JButton getCloseButton() {
		return closeButton;
	}

	/**
	 * Creates a default close button (without any behaviour)
	 * @return	the close button
	 */
	public static JButton getDefaultCloseButton() {
		JButton button = new JButton();
		button.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		button.setContentAreaFilled(false);
		button.setIcon(defaultCloseIcon);
		button.setRolloverIcon(rolloverCloseIcon);
		button.setPressedIcon(pressedCloseIcon);
		return button;
	}

	/**
	 * Set the icons for the default close button
	 * (This only affects balloon tips created after calling this method.)
	 * @param normal		regular icon
	 * @param pressed		icon when clicked
	 * @param rollover		icon when hovering over the button
	 */
	public static void setDefaultCloseButtonIcons(Icon normal, Icon pressed, Icon rollover) {
		defaultCloseIcon  = normal;
		rolloverCloseIcon = rollover;
		pressedCloseIcon  = pressed;
	}

	/**
	 * Adds a mouse listener that will close this balloon tip when clicked.
	 * @param permanentClose	if true, the default behaviour is to close the balloon tip permanently by calling closeBalloon()
	 * 							if false, the default behaviour is to just hide the balloon tip by calling setVisible(false)
	 */
	public void addDefaultMouseListener(boolean permanentClose) {
		removeMouseListener(clickListener);
		if (permanentClose) {
			clickListener = new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					e.consume();
					closeBalloon();
				}
			};
		} else {
			clickListener = new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					e.consume();
					setVisible(false);
				}
			};
		}
		addMouseListener(clickListener);
	}

	/**
	 * Change the component this balloon tip is attached to
	 * (The top-level container will be re-determined during this process;
	 * if you set it manually, you'll have to set it again...)
	 * (Calling this method will fire an "attachedComponent" property change event.)
	 * @param newComponent		the new component to attach to (may not be null)
	 * @exception NullPointerException if parameter newComponent is null
	 */
	public void setAttachedComponent(JComponent newComponent) {
		JComponent oldComponent = this.attachedComponent;

		tearDownHelper(); // Remove any listeners related to the old attached component
		this.attachedComponent = newComponent;
		setupHelper(); // Reinstall the listeners

		// Notify property listeners that the attached component has changed
		firePropertyChange("attachedComponent", oldComponent, attachedComponent);
		refreshLocation();
	}

	/**
	 * Retrieve the component this balloon tip is attached to
	 * @return		The attached component
	 */
	public JComponent getAttachedComponent() {
		return attachedComponent;
	}

	/**
	 * Set the container on which this balloon tip should be drawn
	 * @param tlc			the top-level container; must be valid (isValid() must return true) ()
	 * 						(may not be null)
	 */
	public void setTopLevelContainer(JLayeredPane tlc) {
		if (topLevelContainer != null) {
			topLevelContainer.remove(this);
			topLevelContainer.removeComponentListener(topLevelContainerListener);
		}

		this.topLevelContainer = tlc;
		// We use the popup layer of the top level container (frame or dialog) to show the balloon tip
	    topLevelContainer.setLayer(this, JLayeredPane.POPUP_LAYER);
		// If the window is resized, we should check if the balloon still fits
		topLevelContainer.addComponentListener(topLevelContainerListener);
		// Add the balloon tip to the top-level container (This must be the last step; see BALLOONTIP-10!)
		topLevelContainer.add(this);
	}

	/**
	 * Retrieve the container this balloon tip is drawn on
	 * If the balloon tip hasn't determined this container yet, null is returned
	 * @return	The balloon tip's top level container
	 */
	public JLayeredPane getTopLevelContainer() {
		return topLevelContainer;
	}

	/**
	 * Retrieves the rectangle to which this balloon tip is attached
	 * @return		the rectangle to which this balloon tip is attached, in the coordinate system of the balloon tip	
	 */
	public Rectangle getAttachedRectangle() {
		Point location = SwingUtilities.convertPoint(attachedComponent, getLocation(), this);
		return new Rectangle(location.x, location.y, attachedComponent.getWidth(), attachedComponent.getHeight());
	}

	/**
	 * Refreshes the balloon tip's location
	 * (Is able to update balloon tip's location even if the balloon tip is not shown.)
	 */
	public void refreshLocation() {
		if (topLevelContainer!=null) {
			positioner.determineAndSetLocation(getAttachedRectangle());
		}
	}

	/**
	 * Sets the opacity of this balloon tip and repaints it
	 * Note: Setting the opacity to 0 won't make isVisible() return false.
	 * @param opacity	the opacity, where 0.0f is completely invisible and 1.0f is opaque
	 */
	public void setOpacity(float opacity) {
		this.opacity = opacity;
		repaint();
	}

	/**
	 * Get the opacity of this balloon tip
	 * @return			the opacity, where 0.0f is completely invisible and 1.0f is opaque
	 */
	public float getOpacity() {
		return this.opacity;
	}

	public void paintComponent(Graphics g) {
		if (opacity!=1.0f) {
			((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
		}
		super.paintComponent(g);
	}

	/**
	 * Set this balloon tip's visibility
	 * @param visible		visible if true (and if the listeners associated with this balloon tip have no reason to hide the balloon tip!
	 * 						For example, it makes no sense to show balloon tip if the component it's attached to is hidden...); invisible otherwise
	 */
	public void setVisible(boolean visible) {
		visibilityControl.setCriterionAndUpdate("manual", visible);
	}

	protected void finalize() throws Throwable {
		closeBalloon(); // This will remove all of the listeners a balloon tip uses...
		super.finalize();
	}

	/*
	 * Sets the balloon tip's visibility by calling super.setVisible()
	 * (This bypasses the balloon tip's visibility control.)
	 * @param visible	true if the balloon tip should be visible
	 */
	protected void forceSetVisible(boolean visible) {
		super.setVisible(visible);
	}

	/*
	 * Helper method that checks whether the attached component is visible or not
	 * (i.e. its area is greater than 0 and it really is visible..)
	 * @return 		true if the component is showing; false otherwise
	 */
	protected boolean isAttachedComponentShowing() {
		return attachedComponent.isShowing()
		&& attachedComponent.getWidth() > 0
		&& attachedComponent.getHeight() > 0; // The area of the attached component must be > 0 in order to be visible..
	}
	
	/*
	 * Fire a state change event to the viewportlistener (if any)
	 */
	protected void notifyViewportListener() {
		if (viewportListener!=null) {
			viewportListener.stateChanged(null);
		}
	}

	/*
	 * Default constructor; does nothing but call the super-constructor
	 */
	protected BalloonTip() {
		super();
	}

	/*
	 * Helper method to construct the right positioner given a particular orientation, attach location and offset
	 */
	protected BalloonTipPositioner setupPositioner(Orientation orientation, AttachLocation attachLocation, int horizontalOffset, int verticalOffset) {
		BasicBalloonTipPositioner positioner = null;
		float attachX = 0.0f;
		float attachY = 0.0f;
		boolean fixedAttachLocation = true;

		switch (attachLocation) {
		case ALIGNED:
			fixedAttachLocation = false;
			break;
		case CENTER:
			attachX = 0.5f;
			attachY = 0.5f;
			break;
		case NORTH:
			attachX = 0.5f;
			break;
		case NORTHEAST:
			attachX = 1.0f;
			break;
		case EAST:
			attachX = 1.0f;
			attachY = 0.5f;
			break;
		case SOUTHEAST:
			attachX = 1.0f;
			attachY = 1.0f;
			break;
		case SOUTH:
			attachX = 0.5f;
			attachY = 1.0f;
			break;
		case SOUTHWEST:
			attachY = 1.0f;
			break;
		case WEST:
			attachY = 0.5f;
			break;
		case NORTHWEST:
			break;
		}

		switch (orientation) {
		case LEFT_ABOVE:
			positioner = new LeftAbovePositioner(horizontalOffset, verticalOffset);
			break;
		case LEFT_BELOW:
			positioner = new LeftBelowPositioner(horizontalOffset, verticalOffset);
			break;
		case RIGHT_ABOVE:
			positioner = new RightAbovePositioner(horizontalOffset, verticalOffset);
			break;
		case RIGHT_BELOW:
			positioner = new RightBelowPositioner(horizontalOffset, verticalOffset);
			break;
		}

		positioner.enableFixedAttachLocation(fixedAttachLocation);
		positioner.setAttachLocation(attachX, attachY);

		return positioner;
	}

	/*
	 * Sets up a BalloonTip instance
	 */
	protected void setup(final JComponent attachedComponent, JComponent contents, BalloonTipStyle style, BalloonTipPositioner positioner, JButton closeButton) {
		this.attachedComponent = attachedComponent;
		this.contents = contents;
		this.style = style;
		this.positioner = positioner;

		positioner.setBalloonTip(this);
		setBorder(this.style);
		setOpaque(false);
		setLayout(new GridBagLayout());
		setPadding(4);

		add(this.contents, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		// TODO: if changing it setCloseButton(closeButton,true);
		setCloseButton(closeButton,true);

		// Don't allow to click 'through' the balloon tip
		clickListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {e.consume();}
		};
		addMouseListener(clickListener);

		// Attempt to run setupHelper() ...
		if (attachedComponent.isDisplayable()) {
			setupHelper();
		} else {
			/* We can't determine the top-level container yet.
			 * We'll just have to wait until the parent is set and try again... */
			ancestorListener = new AncestorListener() {
				public void ancestorAdded(AncestorEvent e) {
					setupHelper();
					e.getComponent().removeAncestorListener(this); // Remove yourself
					ancestorListener = null;
				}
				public void ancestorMoved(AncestorEvent e) {}
				public void ancestorRemoved(AncestorEvent e) {}
			};
			attachedComponent.addAncestorListener(ancestorListener);
		}
	}

	/*
	 * Helper method for setup() and changeAttachedComponent()
	 */
	private void setupHelper() {
		// Set the pane on which the balloon tip is drawn
		if (topLevelContainer == null) {
			setTopLevelContainer(attachedComponent.getRootPane().getLayeredPane());
		}

		// If the attached component is moved/hidden/shown, the balloon tip should act accordingly
		attachedComponent.addComponentListener(componentListener);
		// Update balloon tip's visibility
		visibilityControl.setCriterionAndUpdate("attachedComponentShowing",isAttachedComponentShowing());

		// Follow the path of parent components to see if there are any we should listen to
		Container current = attachedComponent.getParent();
		Container previous = attachedComponent;
		while (current!=null) {
			if (current instanceof JTabbedPane || current.getLayout() instanceof CardLayout) {
				/* Switching tabs only tells the JPanel representing the contents of each tab whether it went invisible or not.
				 * It doesn't propagate such events to each and every component within each tab.
				 * Because of this, we'll have to add a listener to the JPanel of this tab. If it goes invisible, so should the balloon tip. */
				if (tabbedPaneListener == null) {
					tabbedPaneListener = getTabbedPaneListener();
				}
				previous.addComponentListener(tabbedPaneListener);
			} else if (current instanceof JViewport) {
				if (viewportListener == null) {
					viewportListener = new NestedViewportListener();
				}
				viewportListener.viewports.add((JViewport) current);
				((JViewport) current).addChangeListener(viewportListener);
			} else if (current instanceof BalloonTip) {
				// In the rare case where this balloon tip is attached to a component within another balloon tip...
				// Monitor the parent balloon tip's movements and visibility
				current.addComponentListener(componentListener);
				// Draw this balloon tip one layer higher; otherwise it would be overlapping the parent balloon tip
				topLevelContainer.setLayer(this, JLayeredPane.getLayer(this) + 1);
				// Quit the loop here; any other parent components that should be listened to are meant for the parent balloon tip
				break;
			}
			previous = current;
			current = current.getParent();
		}

		// We can now calculate and set the balloon tip's initial position
		refreshLocation();

		// Check whether the balloon tip is currently visible within its viewports (if there are any)
		if (viewportListener != null) {
			viewportListener.stateChanged(new ChangeEvent(this));
		}
	}

	/*
	 * Helper method for closeBalloon() and changeAttachedComponent()
	 * Removes a number of listeners attached to the balloon tip.
	 */
	private void tearDownHelper() {
		// In case you're trying to close a balloon tip before it's fully constructed
		if(ancestorListener!=null) {
			attachedComponent.removeAncestorListener(ancestorListener);
			ancestorListener = null;
		}
		
		attachedComponent.removeComponentListener(componentListener);

		// Remove any listeners that were attached to parent components
		if (tabbedPaneListener!=null) {
			Container current = attachedComponent.getParent();
			Container previous = attachedComponent;
			while (current!=null) {
				if (current instanceof JTabbedPane || current.getLayout() instanceof CardLayout) {
					previous.removeComponentListener(tabbedPaneListener);
				} else if (current instanceof BalloonTip) {
					current.removeComponentListener(componentListener);
					break;
				}
				previous = current;
				current = current.getParent();
			}
			tabbedPaneListener = null;
		}

		if (topLevelContainer != null) {
			topLevelContainer.remove(this);
			topLevelContainer.removeComponentListener(topLevelContainerListener);
			topLevelContainer = null;
		}

		if (viewportListener!=null) {
			for (JViewport viewport : viewportListener.viewports) {
				viewport.removeChangeListener(viewportListener);
			}
			viewportListener.viewports.clear();
			viewportListener = null;
		}

		// Clean up our criterias
		visibilityControl.criteria.clear();
	}

	/*
	 * Creates a Component Listener that will adjust this balloon tip's visibility when switching tabs
	 * @return		the tabbed pane listener
	 */
	private ComponentAdapter getTabbedPaneListener() {
		return new ComponentAdapter() {
			public void componentShown(ComponentEvent e) {
				visibilityControl.setCriterionAndUpdate("tabShowing",true);
				/* We must also recheck whether the attached component is visible!
				 * While this tab *was* invisible, the component might've been resized, hidden, shown, ... ,
				 * but no events were fired because the tab was hidden! */
				visibilityControl.setCriterionAndUpdate("attachedComponentShowing",isAttachedComponentShowing());
				refreshLocation();
			}
			public void componentHidden(ComponentEvent e) {
				visibilityControl.setCriterionAndUpdate("tabShowing",false);
			}
		};
	}

	/*
	 * If a balloon tip is nested in one or more viewports, this listener ensures
	 * the balloon tip is hidden if it is no longer visible within the viewports' boundaries
	 */
	protected class NestedViewportListener implements ChangeListener {
		private Vector<JViewport> viewports = new Vector<JViewport>();

		public void stateChanged(ChangeEvent e) {
			refreshLocation();
			Point tipLocation = positioner.getTipLocation();

			boolean isWithinViewport = false;
			for (JViewport viewport:viewportListener.viewports) {
				Rectangle view = new Rectangle(SwingUtilities.convertPoint(viewport, viewport.getLocation(), getTopLevelContainer()), viewport.getSize());
				// If the viewport is embedded in a JScrollPane, take into acount the column and row headers
				if (viewport.getParent() instanceof JScrollPane) {
					JScrollPane scrollPane = (JScrollPane)viewport.getParent();
					if (scrollPane.getColumnHeader()!=null) {
						view.y-=scrollPane.getColumnHeader().getHeight();
					}
					if (scrollPane.getRowHeader()!=null) {
						view.x-=scrollPane.getColumnHeader().getWidth();
					}
				}
				
				if (tipLocation.y >= view.y-1 // -1 because we still want to allow balloons that are attached to the very top...
						&& tipLocation.y <= (view.y + view.height)
						&& (tipLocation.x) >= view.x
						&& (tipLocation.x) <= (view.x + view.width)) {
					isWithinViewport = true;
				} else {
					isWithinViewport = false;
					break;
				}
			}
			if (!viewports.isEmpty()) {
				visibilityControl.setCriterionAndUpdate("withinViewport", isWithinViewport);
			}
		}
	}

	/*
	 * Controls when a balloon tip should be shown or hidden
	 */
	protected class VisibilityControl {
		private HashMap<String, Boolean> criteria = new HashMap<String, Boolean>(); // A list of criteria determining a balloon tip's visibility

		/**
		 * Sets the value of a particular visibility criterion and checks whether the balloon tip should still be visible or not
		 * @param criterion		the visibility criterion
		 * @param value			value of the criterion
		 */
		public void setCriterionAndUpdate(String criterion, Boolean value) {
			criteria.put(criterion, value);
			update();
		}

		/**
		 * Makes sure the balloon tip's visibility is updated by checking all visibility criteria
		 * If any of the visibility criteria is false, the balloon tip should be invisible.
		 * Only if all criteria are true, the balloon tip can be visible.
		 */
		public void update() {
			Iterator<Boolean> i = criteria.values().iterator();
			while (i.hasNext()) {
				if (!i.next()) {
					forceSetVisible(false);
					return;
				}
			}
			forceSetVisible(true);
		}
	}
	
	private static final long serialVersionUID = 8883837312240932652L;
}
