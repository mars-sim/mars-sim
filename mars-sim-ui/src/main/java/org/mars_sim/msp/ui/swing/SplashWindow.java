/*
 * Mars Simulation Project
 * SplashWindow.java
 * @date 2022-08-05
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
import org.mars_sim.msp.core.tool.RandomUtil;

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
	private static final String[] AUTHOR_STRING = {
			"Picture from NASA Ames Research Center, 2005", 
			"Family Watching News on Terraforming Mars, Tiago da Silva",
			"Underground Oasis in Martian Lava Tube, Shane Powers & Linjie Wang, Marstopia Design Contest"
	};
	
	/** The font for displaying {@link #MSP_STRING}. */
	private final Font titleFont = new Font("Bookman Old Style", Font.PLAIN, 42);
	/** Measures the pixels needed to display text. */
	private final FontMetrics titleMetrics = getFontMetrics(titleFont);
	/** The displayed length of {@link #MSP_STRING} in pixels. */
	private final int titleWidth = titleMetrics.stringWidth(MSP_STRING);
	
	/** The font for displaying {@link #VERSION_STRING}. */
	private final Font versionStringFont = new Font(Font.MONOSPACED, Font.BOLD, 30);
	/** Measures the pixels needed to display text. */
	private final FontMetrics versionMetrics = getFontMetrics(versionStringFont);
	/** The displayed length of {@link #VERSION_STRING} in pixels. */
	private final int versionStringWidth = versionMetrics.stringWidth(VERSION_STRING);
	
	/** The font for displaying {@link #VERSION_STRING}. */
	private final Font versionStringFont1 = new Font("Bell MT", Font.BOLD, 16);
	/** Measures the pixels needed to display text. */
	private final FontMetrics versionMetrics1 = getFontMetrics(versionStringFont1);
	/** The displayed length of {@link #VERSION_STRING} in pixels. */
	private final int versionStringWidth1 = versionMetrics1.stringWidth(VERSION_STRING);
	
	/** The font for displaying {@link #BUILD_STRING}. */
	private final Font buildStringFont = new Font("Bell MT", Font.BOLD, 16);
	/** Measures the pixels needed to display text. */
	private final FontMetrics buildMetrics = getFontMetrics(buildStringFont);
	/** The displayed length of {@link #BUILD_STRING} in pixels. */
	private final int buildStringWidth = buildMetrics.stringWidth(BUILD_STRING);
	
	/** The font for displaying {@link #AUTHOR_STRING}. */
	private final Font authorStringFont = new Font("Bell MT", Font.PLAIN, 16);

	private static String[] FILE_NAME = {
			"splash/Mars_Canyon.jpg",
			"splash/News_Terraforming_Mars.jpg",
			"splash/Underground_Oasis_Martian_Lava_Tube.jpg"
	};

	private Image splashImage;
	private int w;
	private int h;


	@SuppressWarnings("serial")
	public SplashWindow() {
		int rand = RandomUtil.getRandomInt(FILE_NAME.length - 1);
		
		window = new JFrame() {
			@Override
			public void paint(Graphics g) {
				// Draw splash image and superimposed text
				Graphics2D g2d = (Graphics2D) g;
				g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g2d.drawImage(splashImage, 0, 0, this);
				
				int x = splashImage.getWidth(this);
				
				if (rand > 0)
					g2d.setColor(Color.ORANGE);
				else
					g2d.setColor(Color.black);
				
				g2d.setFont(titleFont);
				
				if (rand == 0) {
					g2d.drawString(MSP_STRING, (x - titleWidth)/2, 70);
				}
				else
					g2d.drawString(MSP_STRING, (x - titleWidth)/2, 40);
				
//				g2d.setColor(Color.black);
				
				g2d.setFont(versionStringFont);
				
				if (rand == 0) {
					g2d.drawString(VERSION_STRING, (x - versionStringWidth)/2 , 110);
				}
				else if (rand == 1) {
					g2d.setColor(Color.ORANGE);
					g2d.drawString(VERSION_STRING, (x - versionStringWidth)/2 , 70);
				}
				else {
					g2d.setColor(Color.WHITE);
					g2d.drawString(VERSION_STRING, x - versionStringWidth1 - 10, h - 70);
				}
				
				g2d.setColor(Color.WHITE);

				g2d.setFont(authorStringFont);
				
				if (rand == 2)
					g2d.drawString(AUTHOR_STRING[rand], 15, h - 15);
				else
					g2d.drawString(AUTHOR_STRING[rand], 15, h - 15);
				
				g2d.setFont(buildStringFont);
				
				if (rand == 2)
					g2d.drawString(BUILD_STRING, x - buildStringWidth - 10, h - 45);
				else 
					g2d.drawString(BUILD_STRING, x - buildStringWidth - 10, h - 15);

			}
		};

		splashImage = ImageLoader.getImage(FILE_NAME[rand]);
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
