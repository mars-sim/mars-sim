/**
 * Mars Simulation Project
 * SplashWindow.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing;

import org.mars_sim.msp.core.RandomUtil;

import javax.swing.*;
import java.awt.*;

/** The SplashWindow class is a splash screen shown when the project
 *  is loading.
 */
public class SplashWindow extends Window {

    // Constant data member
    private static String[] IMAGE_NAMES = { "SplashImage.jpg", "SplashImage2.jpg", "SplashImage3.jpg" };

    private Image splashImage;
    private int width;
    private int height;

    public SplashWindow() {
        super(new Frame());

        String imageName = IMAGE_NAMES[RandomUtil.getRandomInt(IMAGE_NAMES.length - 1)];
        splashImage = ImageLoader.getImage(imageName);
        ImageIcon splashIcon = new ImageIcon(splashImage);
        width = splashIcon.getIconWidth();
        height = splashIcon.getIconHeight();
        setSize(width, height);

        // Center the splash window on the screen.
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = new Dimension(width, height);
        setLocation(((screenSize.width - windowSize.width) / 2),
                ((screenSize.height - windowSize.height) / 2));

        setBackground(Color.black);

        // Display the splash window.
        setVisible(true);
    }

    public void paint(Graphics g) {
        super.paint(g);

        // Draw splash image
        g.drawImage(splashImage, 0, 0, this);
    }
}
