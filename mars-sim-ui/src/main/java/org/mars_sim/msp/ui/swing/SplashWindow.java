/*
 * Mars Simulation Project
 * SplashWindow.java
 * @date 2022-07-11
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;

/**
 * The SplashWindow class is a splash screen shown when the project is loading.
 */
public class SplashWindow extends JComponent {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private JFrame window;

	// Constant data member
	private static final String VERSION_STRING = Simulation.VERSION;
	private static final String BUILD_STRING = "Build " + Simulation.BUILD;
	private static final String MSP_STRING = Msg.getString("SplashWindow.title"); //$NON-NLS-1$
	private static final String AUTHOR_STRING = "Picture from NASA Ames Research Center, 2005";
	
	/** The font for displaying {@link #MSP_STRING}. */
	private final Font titleFont = new Font("Bookman Old Style", Font.PLAIN, 36);
	/** Measures the pixels needed to display text. */
	private final FontMetrics titleMetrics = getFontMetrics(titleFont);
	/** The displayed length of {@link #MSP_STRING} in pixels. */
	private final int titleWidth = titleMetrics.stringWidth(MSP_STRING);
	
	/** The font for displaying {@link #VERSION_STRING}. */
	private final Font versionStringFont = new Font(Font.MONOSPACED, Font.BOLD, 24);
	/** Measures the pixels needed to display text. */
	private final FontMetrics versionMetrics = getFontMetrics(versionStringFont);
	/** The displayed length of {@link #VERSION_STRING} in pixels. */
	private final int versionStringWidth = versionMetrics.stringWidth(VERSION_STRING);
	
	/** The font for displaying {@link #BUILD_STRING}. */
	private final Font buildStringFont = new Font("Bell MT", Font.BOLD, 16);
	/** Measures the pixels needed to display text. */
	private final FontMetrics buildMetrics = getFontMetrics(buildStringFont);
	/** The displayed length of {@link #BUILD_STRING} in pixels. */
	private final int buildStringWidth = buildMetrics.stringWidth(BUILD_STRING);
	
	/** The font for displaying {@link #AUTHOR_STRING}. */
	private final Font authorStringFont = new Font("Bell MT", Font.PLAIN, 16);

	private static String PIC_NAME = "splash/Mars_Canyon.jpg";

	private Image splashImage;
	private int w;
	private int h;


	@SuppressWarnings("serial")
	public SplashWindow() {
		window = new JFrame() {
			@Override
			public void paint(Graphics g) {
				// Draw splash image and superimposed text
				Graphics2D g2d = (Graphics2D) g;
				g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g2d.drawImage(splashImage, 0, 0, this);
				
				g2d.setColor(Color.black);
				g2d.setFont(titleFont);
				g2d.drawString(MSP_STRING, (splashImage.getWidth(this) - titleWidth)/2, 60);
				
				g2d.setFont(versionStringFont);
				g2d.drawString(VERSION_STRING, (splashImage.getWidth(this) - versionStringWidth)/2 , 90);
			
				g2d.setColor(Color.white);
				g2d.setFont(buildStringFont);
				g2d.drawString(BUILD_STRING, splashImage.getWidth(this) - buildStringWidth - 10, h - 15);
				
				g2d.setFont(authorStringFont);
				g2d.drawString(AUTHOR_STRING, 15, h - 15);
			}
		};

		splashImage = ImageLoader.getImage(PIC_NAME);
		ImageIcon splashIcon = new ImageIcon(splashImage);
		w = splashIcon.getIconWidth();
		h = splashIcon.getIconHeight();
		window.setSize(w, h);

		// Center the splash window on the screen.
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension windowSize = new Dimension(w, h);
		window.setLocation(((screenSize.width - windowSize.width) / 2), ((screenSize.height - windowSize.height) / 2));

		window.setBackground(Color.black);

		window.setUndecorated(true);

		// Set icon image for window.
		setIconImage();

		// Set cursor style.
//		window.setCursor(new Cursor(Cursor.WAIT_CURSOR));

		// Display the splash window.
		window.setVisible(true);
	}

	public void display() {
		window.setVisible(true);
	}

	public void remove() {
		window.dispose();
	}

	public JFrame getJFrame() {
		return window;
	}

	public void setIconImage() {
		window.setIconImage(ImageLoader.getImage(MainWindow.LANDER_PNG));
	}
}
