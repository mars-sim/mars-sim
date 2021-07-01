/**
 * Mars Simulation Project
 * SplashWindow.java
 * @version 3.2.0 2021-06-20
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
	private static final String AUTHOR_STRING = "by Patrick Leger";
	
	/** stores the font for displaying {@link #MSP_STRING}. */
	private final Font titleFont = new Font("Bookman Old Style", Font.PLAIN, 48);
	/** stores the font for displaying {@link #VERSION_STRING}. */
	private final Font versionStringFont = new Font(Font.MONOSPACED, Font.BOLD, 18);
	/** measures the pixels needed to display text. */
	private final FontMetrics versionMetrics = getFontMetrics(versionStringFont);
	/** stores the displayed length of {@link #VERSION_STRING} in pixels. */
	private final int versionStringWidth = versionMetrics.stringWidth(VERSION_STRING);
	
	/** stores the font for displaying {@link #BUILD_STRING}. */
	private final Font buildStringFont = new Font(Font.MONOSPACED, Font.BOLD, 16);
	/** measures the pixels needed to display text. */
	private final FontMetrics buildMetrics = getFontMetrics(buildStringFont);
	/** stores the displayed length of {@link #BUILD_STRING} in pixels. */
	private final int buildStringWidth = buildMetrics.stringWidth(BUILD_STRING);
	
	/** stores the font for displaying {@link #AUTHOR_STRING}. */
	private final Font authorStringFont = new Font("Bell MT", Font.PLAIN, 18);
//	/** measures the pixels needed to display text. */
//	private final FontMetrics authorMetrics = getFontMetrics(authorStringFont);
//	/** stores the displayed length of {@link #AUTHOR_STRING} in pixels. */
//	private final int authorStringWidth = authorMetrics.stringWidth(AUTHOR_STRING);
	
	
	private static String PIC_NAME = "splash/marsfamily.jpg";

	private Image splashImage;
	private int width;
	private int height;


	public SplashWindow() {
		window = new JFrame() {
			/** default serial id. */
			private static final long serialVersionUID = 1L;

			@Override
			public void paint(Graphics g) {

				// Draw splash image and superimposed text
				Graphics2D g2d = (Graphics2D) g;
				g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g2d.drawImage(splashImage, 0, 0, this);
				g2d.setColor(Color.WHITE);
				g2d.setFont(titleFont);
				g2d.drawString(MSP_STRING, 30, 60);
				g2d.setFont(versionStringFont);
				g2d.drawString(VERSION_STRING, splashImage.getWidth(this) - versionStringWidth - 70, height - 80);
				g2d.setFont(buildStringFont);
				g2d.drawString(BUILD_STRING, splashImage.getWidth(this) - buildStringWidth - 50, height - 55);
				g2d.setFont(authorStringFont);
				g2d.drawString(AUTHOR_STRING, 40, height - 55);
			}
		};

		splashImage = ImageLoader.getImage(PIC_NAME);
		ImageIcon splashIcon = new ImageIcon(splashImage);
		width = splashIcon.getIconWidth();
		height = splashIcon.getIconHeight();
		window.setSize(width, height);

		// Center the splash window on the screen.
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension windowSize = new Dimension(width, height);
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
//		window.setIconImage(MainWindow.getIconImage());
		
//		String fullImageName = MainWindow.LANDER_PNG;
//		URL resource = ImageLoader.class.getResource(fullImageName);
//		Toolkit kit = Toolkit.getDefaultToolkit();
//		Image img = kit.createImage(resource);
//		window.setIconImage(img);
	}
}
