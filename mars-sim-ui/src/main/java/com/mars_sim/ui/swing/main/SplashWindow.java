/*
 * Mars Simulation Project
 * SplashWindow.java
 * @date 2025-07-30
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;

import com.mars_sim.core.SimulationRuntime;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainWindow;

/**
 * The SplashWindow class is a splash screen shown when the project is loading.
 */
public class SplashWindow extends JComponent {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private JFrame window;

	// Constant data member
	private static final String SPLASH_FOLDER = "splash/";
	private static final String VERSION_STRING = SimulationRuntime.VERSION.getVersionTag();
	private static final String BUILD_STRING = "Build " + SimulationRuntime.VERSION.getBuild();
	private static final String MSP_STRING = Msg.getString("SplashWindow.title"); //$NON-NLS-1$
	private static final String[] AUTHOR_STRING = {
			"A picture from NASA Ames Research Center. 2005", 
			"Water Ice drilling. NASA Langley Advanced Concepts Lab AMA",
			"Family Watching News on Terraforming Mars. Tiago da Silva",
			"Underground Oasis in Martian Lava Tube. Marstopia Design Contest",
			"Light enters through trough-shaped ports. Team SEArch+/Apis Cor",
			"Internal view of Mars Habitat. Hassell + Eckersley O’Callaghan",
			"Desolate life at a homestead. Settlers (2021) UK movie. humanmars.net",
			"Agridome for growing food on Mars. Mars Society. 2020", 
			"Cyanobacteria help detoxify the environment" // Astronomy Magazine. 2023
	};
	
	private static final String[] FILE_NAME = {
			"Mars_Canyon.jpg",
			"nasa_langley_advanced_concepts_lab.jpg",
			"News_Terraforming_Mars.jpg",
			"Underground_Oasis_Martian_Lava_Tube.jpg",
			"3D_printed_habitat.jpg",
			"Interior_home.jpg",
			"greenhouse_lady.jpg",
			"MSC-AgriDomes-on-Mars.jpg",
			"Cyanobacteria_terraforming.jpg"
	};
	
	/** The font for displaying {@link #MSP_STRING}. */
	private final Font titleFont = new Font("Bookman Old Style", Font.PLAIN, 42);
	/** Measures the pixels needed to display text. */
	private final FontMetrics titleMetrics = getFontMetrics(titleFont);
	/** The displayed length of {@link #MSP_STRING} in pixels. */
	private final int titleWidth = titleMetrics.stringWidth(MSP_STRING);
	
	/** The font for displaying {@link #VERSION_STRING}. */
	private final Font versionStringFont = new Font(Font.SANS_SERIF, Font.BOLD, 30);
	/** Measures the pixels needed to display text. */
	private final FontMetrics versionMetrics = getFontMetrics(versionStringFont);
	/** The displayed length of {@link #VERSION_STRING} in pixels. */
	private final int versionStringWidth = versionMetrics.stringWidth(VERSION_STRING);
	
	/** The font for displaying {@link #VERSION_STRING}. */
	private final Font versionStringFont1 = new Font("Bell MT", Font.BOLD, 18);
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
	private final Font authorStringFont = new Font("Bell MT", Font.ITALIC, 17);

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
				
				drawTitleVersion(g2d, rand, x);
				
				drawAuthorBuild(g2d, rand, x);
				
				splashImage.flush();
				g2d.dispose();
			}
		};

		splashImage = ImageLoader.getImage(SPLASH_FOLDER + FILE_NAME[rand]);
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
		window.setIconImage(ImageLoader.getImage(MainWindow.LANDER_64_PNG));

		// Set cursor style.
		window.setCursor(new Cursor(Cursor.WAIT_CURSOR));

		// Display the splash window.
		window.setVisible(true);
	}

	private void drawAuthorBuild(Graphics2D g2d, int rand, int x) {

		g2d.setColor(Color.WHITE);

		g2d.setFont(authorStringFont);
		
		if (rand == 2)
			g2d.drawString(AUTHOR_STRING[rand], 15, h - 20);
		else
			g2d.drawString(AUTHOR_STRING[rand], 15, h - 15);
		
		g2d.setFont(buildStringFont);
		
		if (rand == 2)
			g2d.drawString(BUILD_STRING, x - buildStringWidth - 10, h - 35);
		else 
			g2d.drawString(BUILD_STRING, x - buildStringWidth - 10, h - 15);
	}
	
	private void drawTitleVersion(Graphics2D g2d, int rand, int x) {
		
		if (rand == 1)
			g2d.setColor(Color.DARK_GRAY);
		
		else if (rand > 1)
			g2d.setColor(Color.ORANGE);
		
		g2d.setFont(titleFont);

		paintTextWithOutline(g2d, MSP_STRING, Color.ORANGE, Color.DARK_GRAY, titleFont, (x - titleWidth)/2, 50);
	
		if (rand == 2) {
			g2d.setFont(versionStringFont);
			g2d.setColor(Color.ORANGE);
			g2d.drawString(VERSION_STRING, (x - versionStringWidth)/2 , 90);
		}
		else {
			g2d.setFont(versionStringFont1);
			g2d.setColor(Color.WHITE);
			g2d.drawString(VERSION_STRING, x - versionStringWidth1 - 10, h - 45);
		}
		
	}
	
	private void paintTextWithOutline(Graphics2D g2d, String text, Color fillColor, Color outlineColor, Font font, int x, int y) {
	
	    BasicStroke outlineStroke = new BasicStroke(2.0f);

        g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        // remember original settings
        Color originalColor = g2d.getColor();
        Stroke originalStroke = g2d.getStroke();
        RenderingHints originalHints = g2d.getRenderingHints();

        // create a glyph vector from your text
        GlyphVector glyphVector = font.createGlyphVector(g2d.getFontRenderContext(), text);
        // get the shape object
        Shape textShape = glyphVector.getOutline();

        // activate anti aliasing for text rendering (if you want it to look nice)
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        
        AffineTransform saveTransform = g2d.getTransform();
        
		// Apply graphic transforms for label.
		AffineTransform newTransform = new AffineTransform(saveTransform);
		newTransform.translate(x, y);
		g2d.setTransform(newTransform);
    
        g2d.setColor(outlineColor);
        g2d.setStroke(outlineStroke);
        g2d.draw(textShape); // draw outline

        g2d.setColor(fillColor);
        g2d.fill(textShape); // fill the shape

        // Restore original graphic transforms.
		g2d.setTransform(saveTransform);
        g2d.setColor(originalColor);
        g2d.setStroke(originalStroke);
        g2d.setRenderingHints(originalHints);
	}
	
	public void display() {
		window.setVisible(true);
	}

	public void remove() {
		window.dispose();
	}

	public void destroy() {
		splashImage = null;
		window = null;
	}
}
