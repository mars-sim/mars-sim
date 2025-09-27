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

	private record SplashImage(String filename, String source) {}

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Constant data member
	private static final String SPLASH_FOLDER = "splash/";
	private static final String VERSION_STRING = SimulationRuntime.VERSION.getVersionTag();
	private static final String BUILD_STRING = "Build " + SimulationRuntime.VERSION.getBuild();
	private static final String MSP_STRING = Msg.getString("SplashWindow.title"); //$NON-NLS-1$
	private static final SplashImage[] IMAGES = {
			new SplashImage("Mars_Canyon.jpg", "A picture from NASA Ames Research Center. 2005"), 
			new SplashImage("nasa_langley_advanced_concepts_lab.jpg", "Water Ice drilling. NASA Langley Advanced Concepts Lab AMA"),
			new SplashImage("News_Terraforming_Mars.jpg", "Family Watching News on Terraforming Mars. Tiago da Silva"),
			new SplashImage("Underground_Oasis_Martian_Lava_Tube.jpg", "Underground Oasis in Martian Lava Tube. Marstopia Design Contest"),
			new SplashImage("3D_printed_habitat.jpg", "Light enters through trough-shaped ports. Team SEArch+/Apis Cor"),
			new SplashImage("Interior_home.jpg", "Internal view of Mars Habitat. Hassell + Eckersley O’Callaghan"),
			new SplashImage("greenhouse_lady.jpg", "Desolate life at a homestead. Settlers (2021) UK movie. humanmars.net"),
			new SplashImage("MSC-AgriDomes-on-Mars.jpg", "Agridome for growing food on Mars. Mars Society. 2020"), 
			new SplashImage("Cyanobacteria_terraforming.jpg", "Cyanobacteria help detoxify the environment")
	};

	private static final int STATUS_BOX_H = 30;
	private static final int STATUS_BOX_W = 300;

	private JFrame window;
	private int w;
	private int h;

	private Font authorStringFont = new Font("Bell MT", Font.ITALIC, 17);
	private int authorY;

	private Font buildStringFont = new Font("Bell MT", Font.BOLD, 16);
	private int buildX;
	private int buildY;

	private int versionY;
	private int versionX;
	private Color versionColour;
	private Font versionFont;

	private Font titleFont = new Font("Bookman Old Style", Font.PLAIN, 42);
	private Color titleColour;
	private int titleX;

	private String imageSource;

	private String statusMessage = null;
	private Font statusFont;
	private Color statusColour;
	private int statusX;
	private int statusY;

	private boolean firstDraw = true;
	private boolean drawImage = true;

	/**
	 * Get the width of a string in pixels for the specified font.
	 * @param font
	 * @param text
	 * @return
	 */
	private  int getStringWidth(Font font, String text) {
		FontMetrics metrics = getFontMetrics(font);
		return metrics.stringWidth(text);
	}

	@SuppressWarnings("serial")
	public SplashWindow() {
		int rand = RandomUtil.getRandomInt(IMAGES.length - 1);
		imageSource = IMAGES[rand].source();
		var splashImage = ImageLoader.getImage(SPLASH_FOLDER + IMAGES[rand].filename());
		ImageIcon splashIcon = new ImageIcon(splashImage);
		w = splashIcon.getIconWidth();
		h = splashIcon.getIconHeight();

		statusY = h - (STATUS_BOX_H + 50);
		statusX = (w - STATUS_BOX_W)/2;

		titleX = (w - getStringWidth(titleFont, MSP_STRING))/2;
		buildX = w - getStringWidth(buildStringFont, BUILD_STRING) - 10;

		// Select decoration			
		if (rand == 1)
			titleColour = Color.DARK_GRAY;
		else
			titleColour = Color.ORANGE;

		if (rand == 2) {
			authorY = h - 20;
			buildY = h - 35;
			versionFont = new Font(Font.SANS_SERIF, Font.BOLD, 30);
			versionColour = Color.ORANGE;
			versionX = (w - getStringWidth(versionFont, VERSION_STRING))/2;
			versionY = 90;
		}
		else {
			authorY = h - 15;
			buildY = h - 15;
			versionFont = new Font("Bell MT", Font.BOLD, 18);
			versionColour = Color.WHITE;
			versionX = w - getStringWidth(versionFont, VERSION_STRING) - 10;
			versionY = h - 45;
		}
		
		statusFont = buildStringFont;
		statusColour = versionColour;

		window = new JFrame() {
			@Override
			public void paint(Graphics g) {
				Graphics2D g2d = (Graphics2D) g;
				g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				if (firstDraw || drawImage) {
					// Draw splash image and superimposed text
					g2d.drawImage(splashImage, 0, 0, this);
					drawTitleVersion(g2d);
					drawAuthorBuild(g2d);

					firstDraw = false;
				}

				// Always draw status the status message
				drawImage = true;
				drawStatus(g2d);
				
				g2d.dispose();
			}
		};

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

	public void setStatusMessage(String message) {
		statusMessage = message;
		drawImage = false; // Do not redraw image, just status
		window.repaint();
	}
	
	private void drawStatus(Graphics2D g2d) {
		if (statusMessage == null)
			return;

		g2d.setColor(Color.GRAY);
		g2d.fillRect(statusX, statusY, STATUS_BOX_W, STATUS_BOX_H);
		g2d.setColor(Color.BLACK);
		g2d.drawRect(statusX, statusY, STATUS_BOX_W, STATUS_BOX_H);

		g2d.setFont(statusFont);
		g2d.setColor(statusColour);

		// Draw centered string
		g2d.drawString(statusMessage, statusX + ((STATUS_BOX_W - getStringWidth(buildStringFont, statusMessage))/2),
				statusY + STATUS_BOX_H - 8);
	}

	private void drawAuthorBuild(Graphics2D g2d) {

		g2d.setColor(Color.WHITE);
		g2d.setFont(authorStringFont);
		g2d.drawString(imageSource, 15, authorY);
		
		g2d.setFont(buildStringFont);
		g2d.drawString(BUILD_STRING, buildX, buildY);
	}
	
	private void drawTitleVersion(Graphics2D g2d) {

		g2d.setColor(titleColour);
		g2d.setFont(titleFont);
		paintTextWithOutline(g2d, MSP_STRING, Color.ORANGE, Color.DARK_GRAY, titleFont, titleX, 50);
		
		g2d.setFont(versionFont);
		g2d.setColor(versionColour);
		g2d.drawString(VERSION_STRING, versionX, versionY);
		
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
}
