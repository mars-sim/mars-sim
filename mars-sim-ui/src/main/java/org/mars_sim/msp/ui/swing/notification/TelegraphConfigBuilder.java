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
import java.awt.event.MouseListener;

import javax.sound.sampled.AudioInputStream;
import javax.swing.ImageIcon;

/**
 * {@link TelegraphConfigBuilder} is a fluent API which allows to create
 * {@link TelegraphConfig} objects easily. It's based on a builder pattern, and
 * allows to create complex {@link TelegraphConfig} objects from a single line
 * of code.
 * 
 * @author Antoine Neveux
 * @version 2.1
 * @since 2.1
 * 
 * @see TelegraphConfig
 */
public class TelegraphConfigBuilder {

	/**
	 * Contains the {@link TelegraphConfig} result, the object that'll be built
	 * while using this fluent API.
	 */
	private final TelegraphConfig result;

	/**
	 * Default constructor, that'll initialize the {@link TelegraphConfig}
	 * object with default values.
	 */
	public TelegraphConfigBuilder() {
		result = new TelegraphConfig();
	}

	/**
	 * Allows to define the {@link Font} to use for the title of your
	 * {@link Telegraph} object
	 * 
	 * @see TelegraphConfig#setTitleFont(Font)
	 * @param font
	 *            sets the {@link Font} to use for the title
	 * @return the current {@link TelegraphConfigBuilder}
	 */
	public TelegraphConfigBuilder withTitleFont(final Font font) {
		result.setTitleFont(font);
		return this;
	}

	/**
	 * Allows to define the {@link Font} to use for the description of your
	 * {@link Telegraph} object
	 * 
	 * @see TelegraphConfig#setDescriptionFont(Font)
	 * @param font
	 *            sets the {@link Font} to use for the description
	 * @return the current {@link TelegraphConfigBuilder}
	 */
	public TelegraphConfigBuilder withDescriptionFont(final Font font) {
		result.setDescriptionFont(font);
		return this;
	}

	/**
	 * Allows to define the {@link Color} to use for the title of your
	 * {@link Telegraph} object
	 * 
	 * @see TelegraphConfig#setTitleColor(Color)
	 * @param color
	 *            sets the {@link Color} to use for the title
	 * @return the current {@link TelegraphConfigBuilder}
	 */
	public TelegraphConfigBuilder withTitleColor(final Color color) {
		result.setTitleColor(color);
		return this;
	}

	/**
	 * Allows to define the {@link Color} to use for the description of your
	 * {@link Telegraph} object
	 * 
	 * @see TelegraphConfig#setDescriptionColor(Color)
	 * @param color
	 *            sets the {@link Color} to use for the description
	 * @return the current {@link TelegraphConfigBuilder}
	 */
	public TelegraphConfigBuilder withDescriptionColor(final Color color) {
		result.setDescriptionColor(color);
		return this;
	}

	/**
	 * Allows to define the {@link ImageIcon} to use for the background of your
	 * {@link Telegraph} window
	 * 
	 * @see TelegraphConfig#setBackgroundImage(ImageIcon)
	 * @param icon
	 *            sets the {@link ImageIcon} to use for the background of your
	 *            window
	 * @return the current {@link TelegraphConfigBuilder}
	 */
	public TelegraphConfigBuilder withBackgroundImage(final ImageIcon icon) {
		result.setBackgroundImage(icon);
		return this;
	}

	/**
	 * Allows to define the {@link Color} to use for the background of your
	 * {@link Telegraph} window
	 * 
	 * @see TelegraphConfig#setBackgroundColor(Color)
	 * @param color
	 *            sets the {@link Color} to use for the background of your
	 *            window
	 * @return the current {@link TelegraphConfigBuilder}
	 */
	public TelegraphConfigBuilder withBackgroundColor(final Color color) {
		result.setBackgroundColor(color);
		return this;
	}

	/**
	 * Allows to define the {@link IconProvider} to use for getting icons in
	 * your {@link Telegraph} window
	 * 
	 * @see TelegraphConfig#setIconProvider(IconProvider)
	 * @param iconProvider
	 *            sets the {@link IconProvider} to use to get icons for your
	 *            {@link Telegraph}
	 * @return the current {@link TelegraphConfigBuilder}
	 */
	public TelegraphConfigBuilder withIconProvider(
			final IconProvider iconProvider) {
		result.setIconProvider(iconProvider);
		return this;
	}

	/**
	 * Allows to define the {@link ImageIcon} to use as an icon for your
	 * {@link Telegraph} window
	 * 
	 * @see TelegraphConfig#setIcon(ImageIcon)
	 * @param icon
	 *            sets the {@link ImageIcon} to use for your {@link Telegraph}
	 *            window
	 * @return the current {@link TelegraphConfigBuilder}
	 */
	public TelegraphConfigBuilder withIcon(final ImageIcon icon) {
		result.setIcon(icon);
		return this;
	}

	/**
	 * Allows to define the {@link Color} to use for the borders of your
	 * {@link Telegraph} window
	 * 
	 * @see TelegraphConfig#setBorderColor(Color)
	 * @param color
	 *            sets the {@link Color} to use for the borders of your window
	 * @return the current {@link TelegraphConfigBuilder}
	 */
	public TelegraphConfigBuilder withBorderColor(final Color color) {
		result.setBorderColor(color);
		return this;
	}

	/**
	 * Allows to define the thickness of the borders for your {@link Telegraph}
	 * window
	 * 
	 * @see TelegraphConfig#setBorderThickness(int)
	 * @param borderThickness
	 *            sets the thickness to use for the borders of your window
	 * @return the current {@link TelegraphConfigBuilder}
	 */
	public TelegraphConfigBuilder withBorderThickness(final int borderThickness) {
		result.setBorderThickness(borderThickness);
		return this;
	}

	/**
	 * Allows to define the in-duration for your {@link Telegraph} window
	 * animation
	 * 
	 * @see TelegraphConfig#setInDuration(long)
	 * @param duration
	 *            sets the duration to use for the in-animation of the window
	 * @return the current {@link TelegraphConfigBuilder}
	 */
	public TelegraphConfigBuilder withInDuration(final long duration) {
		result.setInDuration(duration);
		return this;
	}

	/**
	 * Allows to define the out-duration for your {@link Telegraph} window
	 * animation
	 * 
	 * @see TelegraphConfig#setOutDuration(long)
	 * @param duration
	 *            sets the duration to use for out-animation of the window
	 * @return the current {@link TelegraphConfigBuilder}
	 */
	public TelegraphConfigBuilder withOutDuration(final long duration) {
		result.setOutDuration(duration);
		return this;
	}

	/**
	 * Allows to define the duration of appearance for your {@link Telegraph}
	 * window
	 * 
	 * @see TelegraphConfig#setDuration(long)
	 * @param duration
	 *            sets the duration of appearance for your {@link Telegraph}
	 * @return the current {@link TelegraphConfigBuilder}
	 */
	public TelegraphConfigBuilder withDuration(final long duration) {
		result.setDuration(duration);
		return this;
	}

	/**
	 * Allows to define if the button should be enabled or not on the
	 * {@link Telegraph} window
	 * 
	 * @see TelegraphConfig#setButtonEnabled(boolean)
	 * @param buttonEnabled
	 *            sets either if the button should be enabled or not
	 * @return the current {@link TelegraphConfigBuilder}
	 */
	public TelegraphConfigBuilder withButtonEnabled(final boolean buttonEnabled) {
		result.setButtonEnabled(buttonEnabled);
		return this;
	}

	/**
	 * Allows to define the caption of the button to be used on your
	 * {@link Telegraph} window
	 * 
	 * @see TelegraphConfig#setButtonCaption(String)
	 * @param caption
	 *            sets the caption to be used by the button of the window
	 * @return the current {@link TelegraphConfigBuilder}
	 */
	public TelegraphConfigBuilder withButtonCaption(final String caption) {
		result.setButtonCaption(caption);
		return this;
	}

	/**
	 * Allows to define the {@link ImageIcon} to be used on your
	 * {@link Telegraph} window button
	 * 
	 * @see TelegraphConfig#setButtonIcon(ImageIcon)
	 * @param icon
	 *            sets the icon to be used by the button of the window
	 * @return the current {@link TelegraphConfigBuilder}
	 */
	public TelegraphConfigBuilder withButtonIcon(final ImageIcon icon) {
		result.setButtonIcon(icon);
		return this;
	}

	/**
	 * Allows to define the {@link TelegraphButtonAction} to be used while
	 * clicking on the button of the {@link Telegraph} window
	 * 
	 * @see TelegraphConfig#setButtonAction(TelegraphButtonAction)
	 * @param action
	 *            sets the action to be executed while clicking on the window's
	 *            button
	 * @return the current {@link TelegraphConfigBuilder}
	 */
	public TelegraphConfigBuilder withButtonAction(
			final TelegraphButtonAction action) {
		result.setButtonAction(action);
		return this;
	}

	/**
	 * Allows to define either if the window should be stopped when the mouse is
	 * over it or not
	 * 
	 * @see TelegraphConfig#setStopOnMouseOver(boolean)
	 * @param stopOnMouseOver
	 *            sets if the window should be stopped while the mouse is over
	 *            it or not
	 * @return the current {@link TelegraphConfigBuilder}
	 */
	public TelegraphConfigBuilder withStopOnMouseOver(
			final boolean stopOnMouseOver) {
		result.setStopOnMouseOver(stopOnMouseOver);
		return this;
	}

	/**
	 * Allows to define the distance of the window regarding to the screen
	 * 
	 * @see TelegraphConfig#setDistanceFromScreen(int)
	 * @param distanceFromScreen
	 *            sets the distance of the window regarding to the screen
	 * @return the current {@link TelegraphConfigBuilder}
	 */
	public TelegraphConfigBuilder withDistanceFromScreen(
			final int distanceFromScreen) {
		result.setDistanceFromScreen(distanceFromScreen);
		return this;
	}

	/**
	 * Allows to define the {@link TelegraphPosition} while appearing on the
	 * screen
	 * 
	 * @see TelegraphConfig#setTelegraphPosition(TelegraphPosition)
	 * @param position
	 *            sets where on the screen should the window appear
	 * @return the current {@link TelegraphConfigBuilder}
	 */
	public TelegraphConfigBuilder withTelegraphPosition(
			final TelegraphPosition position) {
		result.setTelegraphPosition(position);
		return this;
	}

	/**
	 * Allows to define the width of the {@link Telegraph} window
	 * 
	 * @see TelegraphConfig#setWindowWidth(int)
	 * @param width
	 *            sets the width of the window
	 * @return the current {@link TelegraphConfigBuilder}
	 */
	public TelegraphConfigBuilder withWindowWidth(final int width) {
		result.setWindowWidth(width);
		return this;
	}

	/**
	 * Allows to define the height of the {@link Telegraph} window
	 * 
	 * @see TelegraphConfig#setWindowHeight(int)
	 * @param height
	 *            sets the height of the window
	 * @return the current {@link TelegraphConfigBuilder}
	 */
	public TelegraphConfigBuilder withWindowHeight(final int height) {
		result.setWindowHeight(height);
		return this;
	}

	/**
	 * Allows to define if audio should be enabled while displaying the
	 * {@link Telegraph} objects
	 * 
	 * @see TelegraphConfig#setAudioEnabled(boolean)
	 * @param audioEnabled
	 *            sets if audio notifications should be enabled or not
	 * @return the current {@link TelegraphConfigBuilder}
	 */
	public TelegraphConfigBuilder withAudioEnabled(final boolean audioEnabled) {
		result.setAudioEnabled(audioEnabled);
		return this;
	}

	/**
	 * Allows to define the audio stream to be used for the audio notifications
	 * 
	 * @see TelegraphConfig#setAudioInputStream(AudioInputStream)
	 * @param stream
	 *            sets the audio stream to use for audio notifications
	 * @return the current {@link TelegraphConfigBuilder}
	 */
	public TelegraphConfigBuilder withAudioInputStream(
			final AudioInputStream stream) {
		result.setAudioInputStream(stream);
		return this;
	}

	/**
	 * Allows to define the global listener to use on the {@link Telegraph}
	 * windows
	 * 
	 * @see TelegraphConfig#setGlobalListener(MouseListener)
	 * @param listener
	 *            sets the {@link MouseListener} to be used on the
	 *            {@link Telegraph} windows
	 * @return the current {@link TelegraphConfigBuilder}
	 */
	public TelegraphConfigBuilder withGlobalListener(
			final MouseListener listener) {
		result.setGlobalListener(listener);
		return this;
	}

	/**
	 * Allows to build the actual {@link TelegraphConfig} object from all the
	 * parameters received through the <b>with*</b> methods.
	 * 
	 * @see TelegraphConfig
	 * @return {@link #result} : a {@link TelegraphConfig} instance built from
	 *         the information provided by the user
	 */
	public TelegraphConfig build() {
		return result;
	}

}
