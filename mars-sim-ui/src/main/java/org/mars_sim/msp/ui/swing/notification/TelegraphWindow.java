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

import java.awt.Point;

import javax.swing.JWindow;
import javax.swing.border.MatteBorder;

//import net.miginfocom.swing.MigLayout;

import org.pushingpixels.trident.Timeline;

/**
 * Implements the telegraph window.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 2.1
 * @since 2.0
 */
@SuppressWarnings("serial")
public class TelegraphWindow extends JWindow {
	/**
	 * The {@link TelegraphConfig} configuration to be used by the
	 * {@link Telegraph} window
	 */
	private final TelegraphConfig config;
	/**
	 * The title of the {@link Telegraph} object
	 */
	String title;
	/**
	 * The description of the {@link Telegraph} object
	 */
	String description;
	/**
	 * The {@link Timeline} to be used for the animation...
	 */
	private Timeline timeline;
	/**
	 * Defines either the window has been discarded by clicking on the button or
	 * not...
	 */
	private boolean discarded = false;

	private Telegraph telegraph;

	//private Timer pauseTimer;
	/**
	 * Default constructor which initializes everything...
	 *
	 * @param theTitle
	 *            {@link #title}
	 * @param theDescription
	 *            {@link #description}
	 * @param theConfig
	 *            {@link #config}
*/
	public TelegraphWindow(final String theTitle, final String theDescription,
			final TelegraphConfig theConfig, Telegraph telegraph) {
		super();
		title = theTitle;
		description = theDescription;
		config = theConfig;
		this.telegraph = telegraph;

		// Creating borders...
		getRootPane().setBorder(
				new MatteBorder(config.getBorderThickness(), config
						.getBorderThickness(), config.getBorderThickness(),
						config.getBorderThickness(), config.getBorderColor()));
/*
		// Setting layout
		setLayout(new MigLayout());

		// Setting background color
		getRootPane().setBackground(config.getBackgroundColor());

		// Setting background image
		if (config.getBackgroundImage() != null) {
			// Need to use a label with the image...
			final JLabel labelBackground = new JLabel(
					config.getBackgroundImage());
			// Set the bounds
			labelBackground.setBounds(0, 0, config.getBackgroundImage()
					.getIconWidth(), config.getBackgroundImage()
					.getIconHeight());
			// And add it to the panel
			getLayeredPane().add(labelBackground,
					new Integer(Integer.MIN_VALUE));
		}
		// Creating a new Panel
		final JPanel contentPanel = new JPanel();
		contentPanel.setOpaque(false);

		// Setting new Layout
		contentPanel.setLayout(new MigLayout("ins dialog, gapx 15, hidemode 3",
				"15[][grow]15", "15[][grow][]15"));

		// Creating the icon
		final JLabel icon = new JLabel(config.getIcon());
		contentPanel.add(icon, "cell 0 0 0 2, align center");

		// Creating the title
		final String strTitle = String.format(
				"<html><div style=\"width:%dpx;\">%s</div><html>", 200, title);
		final JLabel lblTitle = new JLabel("<html>" + strTitle + "</html>");

		// Setting default font if nothing's provided
		if (config.getTitleFont() == null)
			lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 16f));
		else
			// Set the one provided in the configuration
			lblTitle.setFont(config.getTitleFont());

		// Setting font's color
		lblTitle.setForeground(config.getTitleColor());

		// Creating the description
		final String strDescription = String.format(
				"<html><div style=\"width:%dpx;\">%s</div><html>", 190,
				description);
		final JLabel lblDescription = new JLabel(strDescription);

		// If font has been provided in configuration, then set it...
		if (config.getDescriptionFont() != null)
			lblDescription.setFont(config.getDescriptionFont());

		// Setting the description color
		lblDescription.setForeground(config.getDescriptionColor());

		// Adding both title and description
		contentPanel.add(lblTitle, "cell 1 0, aligny center");
		contentPanel.add(lblDescription,
				"cell 1 1, aligny center, growy, width 260!");

		// If the button is enabled
		if (config.isButtonEnabled()) {
			// Create a new button
			final JButton button = new JButton(config.getButtonCaption());
			// If there's an icon
			if (config.getButtonIcon() != null)
				// Add it to the button
				button.setIcon(config.getButtonIcon());
				//2014-12-17 Added modifiers to button
				//button.setOpaque(false);
				button.setContentAreaFilled(false);
				button.setBorder(new LineBorder(Color.gray, 1, true));
				//button.setBorderPainted(true);
			// Add listener
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					button.setEnabled(false);
					if (config.getButtonAction() != null)
						config.getButtonAction().doSomething();
					timeline.play();
					TelegraphWindow.this.discard();
				}
			});
			// Adding the button to the panel
			contentPanel.add(button, "cell 1 2, align right");
		//}

		}
		// Setting content to the window
		setContentPane(contentPanel);

		// Setting the windows always on top
		setAlwaysOnTop(true);

		// Packing everything
		//pack();

		// Putting the window away
		setBounds(-getWidth(), -getHeight(), getWidth(), getHeight());

		// Applying mouselistener from config if needed
		if (config.getGlobalListener() != null)
			addMouseListener(config.getGlobalListener());
*/
	}

	/**
	 * @param timeline
	 *            {@link #timeline}
	 */
	public void setTimeline(final Timeline timeline) {
		this.timeline = timeline;
	}

	/**
	 * @return {@link #discarded}
	 */
	public boolean isDiscarded() {
		return discarded;
	}

	/**
	 * Allows to precise that window has been discarded by clicking on the
	 * button...
	 */
	protected void discard() {
		discarded = true;
	}

	/**
	 * Sets position on screen.
	 *
	 * @param p
	 *            The new position.
	 */
	public void setPosition(final Point p) {
		if (!isVisible())
			// show window
			setVisible(true);
		// set new location
		setBounds(p.x, p.y, getWidth(), getHeight());
	}
}