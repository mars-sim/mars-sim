/**
 * Mars Simulation Project
 * SplashWindow.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;

/**
 * The SplashWindow class is a splash screen shown when the project
 * is loading.
 */
public class SplashWindow {

//	private Window window;
	private JFrame window;

	// Constant data member
	private static final String VERSION_STRING = Simulation.VERSION;
	private static final String MSP_STRING = Msg.getString("SplashWindow.title");

	private static String IMAGE_NAME = "splash.png";
	
	private Image splashImage;
	private int width;
	private int height;

	public SplashWindow() {
		window = new JFrame() {
			@Override
			public void paint(Graphics g) {
//				window.paint(g);

				// Draw splash image and superimposed text
				g.drawImage(splashImage, 0, 0, this);
				window.setForeground(Color.white);
				g.setFont(new Font("SansSerif", Font.PLAIN, 35));
				g.drawString(MSP_STRING, 30, 60);
				g.setFont(new Font("SansSerif", Font.PLAIN, 11));
				g.drawString(VERSION_STRING, 930, 20);
			}
		};

		splashImage = ImageLoader.getImage(IMAGE_NAME);
		ImageIcon splashIcon = new ImageIcon(splashImage);
		width = splashIcon.getIconWidth();
		height = splashIcon.getIconHeight();
		window.setSize(width, height);

		// Center the splash window on the screen.
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension windowSize = new Dimension(width, height);
		window.setLocation(
			((screenSize.width - windowSize.width) / 2),
			((screenSize.height - windowSize.height) / 2)
		);

		window.setBackground(Color.black);

		window.setUndecorated(true);
		// Display the splash window.
		window.setVisible(true);
	}

	public void show() {
		window.setVisible(true);
	}

	public void hide() {
		window.dispose();
	}

	public JFrame getJFrame() {
		return window;
	}

}
