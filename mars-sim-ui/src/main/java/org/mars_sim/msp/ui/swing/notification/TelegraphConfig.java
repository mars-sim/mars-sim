/*
 *   JTelegraph -- a Java message notification library
 *   Copyright (c) 2012, Paulo Roberto Massa Cereda
 *   All rights reserved.
 *
 *   Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions
 *   are met:
 *
 *   1. Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 *   2. Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 *   3. Neither the name of the project's author nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 *   FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 *   COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 *   INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *   BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *   CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *   LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 *   WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *   POSSIBILITY OF SUCH DAMAGE.
 */
package org.mars_sim.msp.ui.swing.notification;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseListener;
import java.io.BufferedInputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.ImageIcon;

import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.notification.icons.SimplicioIconProvider;

/**
 * Holds the telegraph configuration.
 * 
 * @author Paulo Roberto Massa Cereda
 * @version 2.1
 * @since 2.0
 */
public class TelegraphConfig {
	/**
	 * The {@link Font} to use for the title of the {@link Telegraph} object
	 */
	private Font titleFont;
	/**
	 * The {@link Font} to use for the description of the {@link Telegraph}
	 * object
	 */
	private Font descriptionFont;
	/**
	 * The {@link Color} to use for the title of the {@link Telegraph} object
	 */
	private Color titleColor;
	/**
	 * The {@link Color} to use for the description of the {@link Telegraph}
	 * object
	 */
	private Color descriptionColor;
	/**
	 * The {@link ImageIcon} to use as a background image for the whole
	 * {@link Telegraph} object
	 */
	private ImageIcon backgroundImage;
	/**
	 * The {@link Color} to use as a background color for the whole
	 * {@link Telegraph} object
	 */
	private Color backgroundColor;
	/**
	 * The {@link IconProvider} to use in order to get icons to display in the
	 * {@link Telegraph} object
	 */
	private IconProvider iconProvider;
	/**
	 * The {@link ImageIcon} to display as icon of the {@link Telegraph} object.
	 * Please notice that this element sperseds the usage of an
	 * {@link IconProvider}...
	 */
	private ImageIcon icon;
	/**
	 * The {@link Color} to use for the borders of the {@link Telegraph} object
	 */
	private Color borderColor;
	/**
	 * The Thickness to use for the borders of the {@link Telegraph} object
	 */
	private int borderThickness;
	/**
	 * The time (in ms) to set for the apparition animation of the
	 * {@link Telegraph} object
	 */
	private long inDuration;
	/**
	 * The time (in ms) to set for the disparition animation of the
	 * {@link Telegraph} object
	 */
	private long outDuration;
	/**
	 * The time (in ms) the {@link Telegraph} object will stay visible on the
	 * screen
	 */
	private long duration;
	/**
	 * Defines either the button is enabled (so {@link Telegraph} object is
	 * blocked on the screen until user clicked on the button) or not
	 */
	private boolean buttonEnabled;
	/**
	 * The caption (text) to be written on the button of the {@link Telegraph}
	 * object
	 */
	private String buttonCaption;
	/**
	 * The {@link ImageIcon} to be displayed on the button of the
	 * {@link Telegraph} object
	 */
	private ImageIcon buttonIcon;
	/**
	 * The {@link TelegraphButtonAction} action to execute while clicking on the
	 * button of the {@link Telegraph} object. Please see
	 * {@link TelegraphButtonAction} documentation for usage.
	 */
	private TelegraphButtonAction buttonAction;
	/**
	 * Defines either the {@link Telegraph} object should stop while putting the
	 * mouse pointer over it or not
	 */
	private boolean stopOnMouseOver;
	/**
	 * The distance the {@link Telegraph} object will appear from the screen
	 */
	private int distanceFromScreen;
	/**
	 * The {@link TelegraphPosition} of the {@link Telegraph} object's window
	 */
	private TelegraphPosition telegraphPosition;
	/**
	 * The actual screen's width
	 */
	private double screenWidth;
	/**
	 * The actual screen's height
	 */
	private double screenHeight;
	/**
	 * The {@link Telegraph} object's window width
	 */
	private int windowWidth;
	/**
	 * The {@link Telegraph} object's window height
	 */
	private int windowHeight;
	/**
	 * Defines either the audio notification are enabled for the current
	 * {@link Telegraph} object or not
	 */
	private boolean audioEnabled;
	/**
	 * The {@link AudioInputStream} to be used to play the audio notification
	 * for the current {@link Telegraph} object
	 */
	private AudioInputStream audioInputStream;
	/**
	 * The {@link MouseListener} to link to the whole {@link Telegraph} window
	 * object in order to add interactions with the telegraphs.
	 */
	private MouseListener globalListener;

	/**
	 * Default constructor, which is used to define all the default
	 * configuration
	 */
	public TelegraphConfig() {
		titleFont = null;
		descriptionFont = null;
		titleColor = Color.BLACK;
		descriptionColor = Color.BLACK;
		backgroundImage = null;
		backgroundColor = Color.WHITE;
		iconProvider = SimplicioIconProvider.MESSAGE;
		icon = null;
		borderColor = Color.BLACK;
		borderThickness = 2;
		inDuration = 250;
		outDuration = 250;
		duration = 2000;
		buttonEnabled = false;
		buttonCaption = " OK ";
		buttonIcon = null;
		buttonAction = null;
		stopOnMouseOver = false;
		distanceFromScreen = 20;
		telegraphPosition = TelegraphPosition.TOP_LEFT;
		{
			// Get the screen size and set it...
			final Rectangle rect = getScreenResolution();
			screenWidth = rect.getWidth();
			screenHeight = rect.getHeight();
		}
		audioEnabled = false;
		try {
			audioInputStream = AudioSystem
					.getAudioInputStream(new BufferedInputStream(ImageLoader.class
							.getResourceAsStream("/notification/notify.wav")));
		} catch (final Exception e) {
			audioEnabled = false;
			e.printStackTrace();
		}
		globalListener = null;
	}

	/**
	 * @return {@link #titleFont}
	 */
	public Font getTitleFont() {
		return titleFont;
	}

	/**
	 * @param titleFont
	 *            {@link #titleFont}
	 */
	public void setTitleFont(final Font titleFont) {
		this.titleFont = titleFont;
	}

	/**
	 * @return {@link #descriptionFont}
	 */
	public Font getDescriptionFont() {
		return descriptionFont;
	}

	/**
	 * @param descriptionFont
	 *            {@link #descriptionFont}
	 */
	public void setDescriptionFont(final Font descriptionFont) {
		this.descriptionFont = descriptionFont;
	}

	/**
	 * @return {@link #titleColor}
	 */
	public Color getTitleColor() {
		return titleColor;
	}

	/**
	 * @param titleColor
	 *            {@link #titleColor}
	 */
	public void setTitleColor(final Color titleColor) {
		this.titleColor = titleColor;
	}

	/**
	 * @return {@link #descriptionColor}
	 */
	public Color getDescriptionColor() {
		return descriptionColor;
	}

	/**
	 * @param descriptionColor
	 *            {@link #descriptionColor}
	 */
	public void setDescriptionColor(final Color descriptionColor) {
		this.descriptionColor = descriptionColor;
	}

	/**
	 * @return {@link #backgroundImage}
	 */
	public ImageIcon getBackgroundImage() {
		return backgroundImage;
	}

	/**
	 * @param backgroundImage
	 *            {@link #backgroundImage}
	 */
	public void setBackgroundImage(final ImageIcon backgroundImage) {
		this.backgroundImage = backgroundImage;
	}

	/**
	 * @return {@link #backgroundColor}
	 */
	public Color getBackgroundColor() {
		return backgroundColor;
	}

	/**
	 * @param backgroundColor
	 *            {@link #backgroundColor}
	 */
	public void setBackgroundColor(final Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	/**
	 * @return {@link #iconProvider}
	 */
	public IconProvider getIconProvider() {
		return iconProvider;
	}

	/**
	 * @param iconProvider
	 *            {@link #iconProvider}
	 */
	public void setIconProvider(final IconProvider iconProvider) {
		this.iconProvider = iconProvider;
	}

	/**
	 * This method allows to send the icon. If no icon has been precisely
	 * defined, it'll check if an {@link IconProvider} has been defined to get
	 * the icon and get the {@link ImageIcon} from it.
	 * 
	 * @return the {@link ImageIcon} defined either in the {@link #icon} field,
	 *         or the one coming from the {@link IconProvider} defined in the
	 *         {@link #iconProvider} field
	 */
	public ImageIcon getIcon() {
		return icon == null ? iconProvider != null ? iconProvider.getIcon()
				: SimplicioIconProvider.MESSAGE.getIcon() : icon;
	}

	/**
	 * @param icon
	 *            {@link #icon}
	 */
	public void setIcon(final ImageIcon icon) {
		this.icon = icon;
	}

	/**
	 * @return {@link #borderColor}
	 */
	public Color getBorderColor() {
		return borderColor;
	}

	/**
	 * @param borderColor
	 *            {@link #borderColor}
	 */
	public void setBorderColor(final Color borderColor) {
		this.borderColor = borderColor;
	}

	/**
	 * @return {@link #borderThickness}
	 */
	public int getBorderThickness() {
		return borderThickness;
	}

	/**
	 * @param borderThickness
	 *            {@link #borderThickness}
	 */
	public void setBorderThickness(final int borderThickness) {
		this.borderThickness = borderThickness;
	}

	/**
	 * @return {@link #inDuration}
	 */
	public long getInDuration() {
		return inDuration;
	}

	/**
	 * @param inDuration
	 *            {@link #inDuration}
	 */
	public void setInDuration(final long inDuration) {
		this.inDuration = inDuration;
	}

	/**
	 * @return {@link #outDuration}
	 */
	public long getOutDuration() {
		return outDuration;
	}

	/**
	 * @param outDuration
	 *            {@link #outDuration}
	 */
	public void setOutDuration(final long outDuration) {
		this.outDuration = outDuration;
	}

	/**
	 * @return {@link #duration}
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * @param duration
	 *            {@link #duration}
	 */
	public void setDuration(final long duration) {
		this.duration = duration;
	}

	/**
	 * @return {@link #buttonEnabled}
	 */
	public boolean isButtonEnabled() {
		return buttonEnabled;
	}

	/**
	 * @param buttonEnabled
	 *            {@link #buttonEnabled}
	 */
	public void setButtonEnabled(final boolean buttonEnabled) {
		this.buttonEnabled = buttonEnabled;
	}

	/**
	 * @return {@link #buttonCaption}
	 */
	public String getButtonCaption() {
		return buttonCaption;
	}

	/**
	 * @param buttonCaption
	 *            {@link #buttonCaption}
	 */
	public void setButtonCaption(final String buttonCaption) {
		this.buttonCaption = buttonCaption;
	}

	/**
	 * @return {@link #buttonIcon}
	 */
	public ImageIcon getButtonIcon() {
		return buttonIcon;
	}

	/**
	 * @param buttonIcon
	 *            {@link #buttonIcon}
	 */
	public void setButtonIcon(final ImageIcon buttonIcon) {
		this.buttonIcon = buttonIcon;
	}

	/**
	 * @return {@link #buttonAction}
	 */
	public TelegraphButtonAction getButtonAction() {
		return buttonAction;
	}

	/**
	 * @param action
	 *            {@link #buttonAction}
	 */
	public void setButtonAction(final TelegraphButtonAction action) {
		buttonAction = action;
	}

	/**
	 * @return {@link #stopOnMouseOver}
	 */
	public boolean isStoppedOnMouseOver() {
		return stopOnMouseOver;
	}

	/**
	 * @param stopOnMouseOver
	 *            {@link #stopOnMouseOver}
	 */
	public void setStopOnMouseOver(final boolean stopOnMouseOver) {
		this.stopOnMouseOver = stopOnMouseOver;
	}

	/**
	 * @param distanceFromScreen
	 *            {@link #distanceFromScreen}
	 */
	public void setDistanceFromScreen(final int distanceFromScreen) {
		this.distanceFromScreen = distanceFromScreen;
	}

	/**
	 * @param telegraphPosition
	 *            {@link #telegraphPosition}
	 */
	public void setTelegraphPosition(final TelegraphPosition telegraphPosition) {
		this.telegraphPosition = telegraphPosition;
	}

	/**
	 * @param windowWidth
	 *            {@link #windowWidth}
	 */
	public void setWindowWidth(final int windowWidth) {
		this.windowWidth = windowWidth;
	}

	/**
	 * @param windowHeight
	 *            {@link #windowHeight}
	 */
	public void setWindowHeight(final int windowHeight) {
		this.windowHeight = windowHeight;
	}

	/**
	 * @return {@link #audioEnabled}
	 */
	public boolean isAudioEnabled() {
		return audioEnabled;
	}

	/**
	 * @param audioEnabled
	 *            {@link #audioEnabled}
	 */
	public void setAudioEnabled(final boolean audioEnabled) {
		this.audioEnabled = audioEnabled;
	}

	/**
	 * @return {@link #audioInputStream}
	 */
	public AudioInputStream getAudioInputStream() {
		return audioInputStream;
	}

	/**
	 * @param audioInputStream
	 *            {@link #audioInputStream}
	 */
	public void setAudioInputStream(final AudioInputStream audioInputStream) {
		this.audioInputStream = audioInputStream;
	}

	/**
	 * @return {@link #globalListener}
	 */
	public MouseListener getGlobalListener() {
		return globalListener;
	}

	/**
	 * @param listener
	 *            {@link #globalListener}
	 */
	public void setGlobalListener(final MouseListener listener) {
		globalListener = listener;
	}

	/**
	 * Calculates the initial coordinates.
	 * 
	 * @return A point.
	 */
	protected Point getInitialCoordinates() {
		// the points
		int positionX;
		int positionY;

		// check the option
		switch (telegraphPosition) {
		case BOTTOM_CENTER:
			positionX = (int) (screenWidth / 2 - windowWidth / 2);
			positionY = (int) (screenHeight + windowHeight);
			break;
		case TOP_CENTER:
			positionX = (int) (screenWidth / 2 - windowWidth / 2);
			positionY = -windowHeight;
			break;
		case TOP_LEFT:
			positionX = -windowWidth;
			positionY = -windowHeight;
			break;
		case BOTTOM_LEFT:
			positionX = -windowWidth;
			positionY = (int) screenHeight;
			break;
		case TOP_RIGHT:
			positionX = (int) screenWidth;
			positionY = -windowHeight;
			break;
		case BOTTOM_RIGHT:
			positionX = (int) screenWidth;
			positionY = (int) (screenHeight + windowHeight);
			break;
		default:
			positionX = 0;
			positionY = 0;
		}
		// return new point
		return new Point(positionX, positionY);
	}

	/**
	 * Calculates the final coordinates.
	 * 
	 * @return The point.
	 */
	protected Point getFinalCoordinates() {
		// the points
		int positionX;
		int positionY;

		// check the option
		switch (telegraphPosition) {
		case BOTTOM_CENTER:
			positionX = (int) (screenWidth / 2 - windowWidth / 2);
			positionY = (int) (screenHeight - windowHeight - distanceFromScreen);
			break;
		case TOP_CENTER:
			positionX = (int) (screenWidth / 2 - windowWidth / 2);
			positionY = distanceFromScreen;
			break;
		case TOP_LEFT:
			positionX = distanceFromScreen;
			positionY = distanceFromScreen;
			break;
		case BOTTOM_LEFT:
			positionX = distanceFromScreen;
			positionY = (int) (screenHeight - windowHeight - distanceFromScreen);
			break;
		case TOP_RIGHT:
			positionX = (int) (screenWidth - windowWidth - distanceFromScreen);
			positionY = distanceFromScreen;
			break;
		case BOTTOM_RIGHT:
			positionX = (int) (screenWidth - windowWidth - distanceFromScreen);
			positionY = (int) (screenHeight - windowHeight - distanceFromScreen);
			break;
		default:
			positionX = 0;
			positionY = 0;
		}

		// return the new point
		return new Point(positionX, positionY);
	}

	/**
	 * Gets the screen resolution.
	 * 
	 * @return The screen resolution.
	 */
	private Rectangle getScreenResolution() {
		// get the environment
		final GraphicsEnvironment environment = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		// return the bounds
		return environment.getMaximumWindowBounds();
	}

}