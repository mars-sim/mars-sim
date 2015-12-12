/*
 * Copyright (c) 2012, Gerrit Grunwald
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * The names of its contributors may not be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package eu.hansolo.steelseries.extras;

import eu.hansolo.steelseries.tools.Util;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Random;
import javax.swing.JComponent;
import javax.swing.Timer;

/**
 *
 * @author Gerrit Grunwald <han.solo at muenster.de>
 */
public class TrafficLight extends JComponent implements ActionListener {
    public static final String RED_PROPERTY = "red";
    public static final String YELLOW_PROPERTY = "yellow";
    public static final String GREEN_PROPERTY = "green";
    private static final BufferedImage HATCH_TEXTURE = createHatchTexture();
    private boolean blink;
    private boolean redOn;
    private boolean redBlinking;
    private boolean yellowOn;
    private boolean yellowBlinking;
    private boolean greenOn;
    private boolean greenBlinking;
    private PropertyChangeSupport propertySupport;
    private final Rectangle INNER_BOUNDS = new Rectangle(0, 0, 98, 278);
    private final Point2D CENTER;
    private BufferedImage housingImage;
    private BufferedImage greenImage;
    private BufferedImage greenOnImage;
    private BufferedImage greenOffImage;
    private BufferedImage yellowImage;
    private BufferedImage yellowOnImage;
    private BufferedImage yellowOffImage;
    private BufferedImage redImage;
    private BufferedImage redOnImage;
    private BufferedImage redOffImage;
    private boolean square;
    private final Timer TIMER;
    private int timerPeriod;
    private transient final ComponentListener COMPONENT_LISTENER = new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent event) {
            final int SIZE = getWidth() <= getHeight() ? getWidth() : getHeight();
            Container parent = getParent();
            if ((parent != null) && (parent.getLayout() == null)) {
                if (SIZE < getMinimumSize().width || SIZE < getMinimumSize().height) {
                    setSize(getMinimumSize());
                } else if(square) {
					setSize(SIZE, SIZE);
				} else {
                    setSize(getWidth(), getHeight());
                }
            } else {
                if (SIZE < getMinimumSize().width || SIZE < getMinimumSize().height) {
                    setPreferredSize(getMinimumSize());
                } else if(square) {
					setPreferredSize(new Dimension(SIZE, SIZE));
				} else {
                    setPreferredSize(new Dimension(getWidth(), getHeight()));
                }
            }
            calcInnerBounds();
            init(getInnerBounds().width, getInnerBounds().height);
        }
    };

    public TrafficLight() {
        super();
        addComponentListener(COMPONENT_LISTENER);
        propertySupport = new PropertyChangeSupport(this);
        CENTER = new Point2D.Double();
        timerPeriod = 1000;
        TIMER = new Timer(timerPeriod, this);
        housingImage = Util.INSTANCE.createImage(INNER_BOUNDS.width, INNER_BOUNDS.height, Transparency.TRANSLUCENT);
        greenImage = Util.INSTANCE.createImage(INNER_BOUNDS.width, INNER_BOUNDS.height, Transparency.TRANSLUCENT);
        greenOnImage = Util.INSTANCE.createImage(INNER_BOUNDS.width, INNER_BOUNDS.height, Transparency.TRANSLUCENT);
        greenOffImage = Util.INSTANCE.createImage(INNER_BOUNDS.width, INNER_BOUNDS.height, Transparency.TRANSLUCENT);
        yellowImage = Util.INSTANCE.createImage(INNER_BOUNDS.width, INNER_BOUNDS.height, Transparency.TRANSLUCENT);
        yellowOnImage = Util.INSTANCE.createImage(INNER_BOUNDS.width, INNER_BOUNDS.height, Transparency.TRANSLUCENT);
        yellowOffImage = Util.INSTANCE.createImage(INNER_BOUNDS.width, INNER_BOUNDS.height, Transparency.TRANSLUCENT);
        redImage = Util.INSTANCE.createImage(INNER_BOUNDS.width, INNER_BOUNDS.height, Transparency.TRANSLUCENT);
        redOnImage = Util.INSTANCE.createImage(INNER_BOUNDS.width, INNER_BOUNDS.height, Transparency.TRANSLUCENT);
        redOffImage = Util.INSTANCE.createImage(INNER_BOUNDS.width, INNER_BOUNDS.height, Transparency.TRANSLUCENT);
        blink = false;
        redOn = false;
        redBlinking = false;
        yellowOn = false;
        yellowBlinking = false;
        greenOn = false;
        greenBlinking = false;
        square = false;
    }

    public final void init(final int WIDTH, final int HEIGHT) {
        if (WIDTH <= 1 || HEIGHT <= 1) {
            return;
        }
        if (housingImage != null) {
            housingImage.flush();
        }
        housingImage = createHousingImage(WIDTH, HEIGHT);
        if (greenImage != null) {
            greenImage.flush();
        }
        greenImage = createGreenLightImage(WIDTH, HEIGHT);
        if (greenOnImage != null) {
            greenOnImage.flush();
        }
        greenOnImage = createGreenOnImage(WIDTH, HEIGHT);
        if (greenOffImage != null) {
            greenOffImage.flush();
        }
        greenOffImage = createGreenOffImage(WIDTH, HEIGHT);
        if (yellowImage != null) {
            yellowImage.flush();
        }
        yellowImage = createYellowLightImage(WIDTH, HEIGHT);
        if (yellowOnImage != null) {
            yellowOnImage.flush();
        }
        yellowOnImage = createYellowOnImage(WIDTH, HEIGHT);
        if (yellowOffImage != null) {
            yellowOffImage.flush();
        }
        yellowOffImage = createYellowOffImage(WIDTH, HEIGHT);
        if (redImage != null) {
            redImage.flush();
        }
        redImage = createRedLightImage(WIDTH, HEIGHT);
        if (redOnImage != null) {
            redOnImage.flush();
        }
        redOnImage = createRedOnImage(WIDTH, HEIGHT);
        if (redOffImage != null) {
            redOffImage.flush();
        }
        redOffImage = createRedOffImage(WIDTH, HEIGHT);
        CENTER.setLocation(WIDTH / 2.0, HEIGHT / 2.0);
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Create the Graphics2D object
        final Graphics2D G2 = (Graphics2D) g.create();

        // Set the rendering hints
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        // Take insets into account (e.g. used by borders)
        G2.translate(getInnerBounds().x, getInnerBounds().y);

        // Housing
        G2.drawImage(housingImage, 0, 0, null);

        // Green
        G2.drawImage(greenImage, 0, 0, null);
        if (greenOn) {
            G2.drawImage(greenOnImage, 0, 0, null);
        }
        G2.drawImage(greenOffImage, 0, 0, null);

        // Yellow
        G2.drawImage(yellowImage, 0, 0, null);
        if (yellowOn) {
            G2.drawImage(yellowOnImage, 0, 0, null);
        }
        G2.drawImage(yellowOffImage, 0, 0, null);

        // Red
        G2.drawImage(redImage, 0, 0, null);
        if (redOn) {
            G2.drawImage(redOnImage, 0, 0, null);
        }
        G2.drawImage(redOffImage, 0, 0, null);

        // Dispose the temp graphics object
        G2.dispose();
    }

    /**
     * Returns true if the red light is on
     * @return true if the red light is on
     */
    public boolean isRedOn() {
        return redOn;
    }

    /**
     * Enables/disables the red light
     * @param RED_ON
     */
    public void setRedOn(final boolean RED_ON) {
        boolean oldRedOn = redOn;
        redOn = RED_ON;
        propertySupport.firePropertyChange(RED_PROPERTY, oldRedOn, redOn);
        repaint(getInnerBounds());
    }

    /**
     * Returns true if the yellow light is on
     * @return true if the yellow light is on
     */
    public boolean isYellowOn() {
        return yellowOn;
    }

    /**
     * Enables/disables the yellow light
     * @param YELLOW_ON
     */
    public void setYellowOn(final boolean YELLOW_ON) {
        boolean oldYellowOn = yellowOn;
        yellowOn = YELLOW_ON;
        propertySupport.firePropertyChange(YELLOW_PROPERTY, oldYellowOn, yellowOn);
        repaint(getInnerBounds());
    }

    /**
     * Returns true if the green light is on
     * @return true if the green light is on
     */
    public boolean isGreenOn() {
        return greenOn;
    }

    /**
     * Enables/disables the green light
     * @param GREEN_ON
     */
    public void setGreenOn(final boolean GREEN_ON) {
        boolean oldGreenOn = greenOn;
        greenOn = GREEN_ON;
        propertySupport.firePropertyChange(GREEN_PROPERTY, oldGreenOn, greenOn);
        repaint(getInnerBounds());
    }

    /**
     * Returns true if the red light is blinking
     * @return true if the red light is blinking
     */
    public boolean isRedBlinking() {
        return redBlinking;
    }

    /**
     * Enables/disables blinking of red light
     * @param RED_BLINKING
     */
    public void setRedBlinking(final boolean RED_BLINKING) {
        redBlinking = RED_BLINKING;
        if (!RED_BLINKING) {
            redOn = false;
            TIMER.stop();
        } else {
            TIMER.start();
        }
    }

    /**
     * Returns true if the yellow light is blinking
     * @return true if the yellow light is blinking
     */
    public boolean isYellowBlinking() {
        return yellowBlinking;
    }

    /**
     * Enables/disables blinking of the yellow light
     * @param YELLOW_BLINKING
     */
    public void setYellowBlinking(final boolean YELLOW_BLINKING) {
        yellowBlinking = YELLOW_BLINKING;
        if (!YELLOW_BLINKING) {
            yellowOn = false;
            TIMER.stop();
        } else {
            TIMER.start();
        }
    }

    /**
     * Returns true if the green light is blinking
     * @return true if the green light is blinking
     */
    public boolean isGreenBlinking() {
        return greenBlinking;
    }

    /**
     * Enables/disables the blinking of the green light
     * @param GREEN_BLINKING
     */
    public void setGreenBlinking(final boolean GREEN_BLINKING) {
        greenBlinking = GREEN_BLINKING;
        if (!GREEN_BLINKING) {
            greenOn = false;
            TIMER.stop();
        } else {
            TIMER.start();
        }
    }

    /**
     * Returns interval in milliseconds that will be used for the blinking
     * @return interval in milliseconds that will beused for the blinking
     */
    public int getTimerPeriod() {
        return timerPeriod;
    }

    /**
     * Sets the interval in milliseconds that will be used for the blinking
     * Parameter will be within the range from 100 - 10000 ms
     * @param TIMER_PERIOD
     */
    public void setTimerPeriod(final int TIMER_PERIOD) {
        timerPeriod = TIMER_PERIOD < 100 ? 100 : (TIMER_PERIOD > 10000 ? 10000 : TIMER_PERIOD);
        TIMER.setDelay(timerPeriod);
    }

    @Override
    public void addPropertyChangeListener(final PropertyChangeListener LISTENER) {
        if (isShowing()) {
            propertySupport.addPropertyChangeListener(LISTENER);
        }
    }

    @Override
    public void removePropertyChangeListener(final PropertyChangeListener LISTENER) {
        propertySupport.removePropertyChangeListener(LISTENER);
    }

    /**
    * Calculates the area that is available for painting the display
    */
    private void calcInnerBounds() {
        final Insets INSETS = getInsets();
        // 98, 278
        final int PREF_HEIGHT = getWidth() < (int) (getHeight() * 0.3525179856) ? (int) (getWidth() * 2.8367346939) : getHeight();

        INNER_BOUNDS.setBounds(INSETS.left, INSETS.top, (int) (PREF_HEIGHT * 0.3525179856)  - INSETS.left - INSETS.right, PREF_HEIGHT - INSETS.top - INSETS.bottom);
    }

    /**
     * Returns a rectangle representing the available space for drawing the
     * component taking the insets into account (e.g. given through borders etc.)
     * @return a rectangle that represents the area available for rendering the component
     */
    private Rectangle getInnerBounds() {
        return INNER_BOUNDS;
    }

    @Override
    public Dimension getMinimumSize() {
        /* Return the default size of the component
         * which will be used by ui-editors for initialization
         */
        return new Dimension(INNER_BOUNDS.width, INNER_BOUNDS.height);
    }

	@Override
	public void setPreferredSize(final Dimension DIM) {
	    final int SIZE = DIM.width <= DIM.height ? DIM.width : DIM.height;
	    if (square) {
	        super.setPreferredSize(new Dimension(SIZE, SIZE));
	    } else {
	        super.setPreferredSize(DIM);
	    }
	    calcInnerBounds();
	    init(getInnerBounds().width, getInnerBounds().height);
	}

	@Override
	public void setSize(final int WIDTH, final int HEIGHT) {
	    final int SIZE = WIDTH <= HEIGHT ? WIDTH : HEIGHT;
	    if (square) {
	        super.setSize(SIZE, SIZE);
	    } else {
	        super.setSize(WIDTH, HEIGHT);
	    }
	    calcInnerBounds();
	    init(getInnerBounds().width, getInnerBounds().height);
	}

	@Override
	public void setSize(final Dimension DIM) {
	    final int SIZE = DIM.width <= DIM.height ? DIM.width : DIM.height;
	    if (square) {
	        super.setSize(new Dimension(SIZE, SIZE));
	    } else {
	        super.setSize(DIM);
	    }
	    calcInnerBounds();
	    init(getInnerBounds().width, getInnerBounds().height);
	}

	@Override
	public void setBounds(final Rectangle BOUNDS) {
	    final int SIZE = BOUNDS.width <= BOUNDS.height ? BOUNDS.width : BOUNDS.height;
	    if (square) {
	        super.setBounds(BOUNDS.x, BOUNDS.y, SIZE, SIZE);
	    } else {
	        super.setBounds(BOUNDS);
	    }
	    calcInnerBounds();
	    init(getInnerBounds().width, getInnerBounds().height);
	}

	@Override
	public void setBounds(final int X, final int Y, final int WIDTH, final int HEIGHT) {
	    final int SIZE = WIDTH <= HEIGHT ? WIDTH : HEIGHT;
	    if (square) {
	        super.setBounds(X, Y, SIZE, SIZE);
	    } else {
	        super.setBounds(X, Y, WIDTH, HEIGHT);
	    }
	    calcInnerBounds();
	    init(getInnerBounds().width, getInnerBounds().height);
	}

	// Image methods
    public BufferedImage createHousingImage(final int WIDTH, final int HEIGHT) {
        final GraphicsConfiguration GFX_CONF = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        if (WIDTH <= 0 || HEIGHT <= 0) {
            return GFX_CONF.createCompatibleImage(1, 1, java.awt.Transparency.TRANSLUCENT);
        }
        final BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();
        final RoundRectangle2D HOUSING_BACK = new RoundRectangle2D.Double(0.0 * IMAGE_WIDTH, 0.0 * IMAGE_HEIGHT, 1.0 * IMAGE_WIDTH, 1.0 * IMAGE_HEIGHT, 0.21428571428571427 * IMAGE_WIDTH, 0.07553956834532374 * IMAGE_HEIGHT);
        G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.04081632653061224 * IMAGE_WIDTH, 0.007194244604316547 * IMAGE_HEIGHT), new Point2D.Double(0.9521011364730593 * IMAGE_WIDTH, 0.9958824935586308 * IMAGE_HEIGHT), new float[]{0.0f, 0.01f, 0.09f, 0.24f, 0.55f, 0.78f, 0.98f, 1.0f}, new Color[]{new Color(0.5960784314f, 0.5960784314f, 0.6039215686f, 1f), new Color(0.5960784314f, 0.5960784314f, 0.6039215686f, 1f), new Color(0.2f, 0.2f, 0.2f, 1f), new Color(0.5960784314f, 0.5960784314f, 0.6039215686f, 1f), new Color(0.1215686275f, 0.1215686275f, 0.1215686275f, 1f), new Color(0.2117647059f, 0.2117647059f, 0.2117647059f, 1f), new Color(0f, 0f, 0f, 1f), new Color(0f, 0f, 0f, 1f)}));
        G2.fill(HOUSING_BACK);

        final RoundRectangle2D HOUSING_FRONT = new RoundRectangle2D.Double(0.030612244897959183 * IMAGE_WIDTH, 0.01079136690647482 * IMAGE_HEIGHT, 0.9387755102040817 * IMAGE_WIDTH, 0.9784172661870504 * IMAGE_HEIGHT, 0.1683673469387755 * IMAGE_WIDTH, 0.05935251798561151 * IMAGE_HEIGHT);
        G2.setPaint(new LinearGradientPaint(new Point2D.Double(-0.1326530612244898 * IMAGE_WIDTH, -0.0539568345323741 * IMAGE_HEIGHT), new Point2D.Double(2.0614080436330213 * IMAGE_WIDTH, 0.6672932297063833 * IMAGE_HEIGHT), new float[]{0.0f, 0.01f, 0.16f, 0.31f, 0.44f, 0.65f, 0.87f, 0.98f, 1.0f}, new Color[]{new Color(0f, 0f, 0f, 1f), new Color(0f, 0f, 0f, 1f), new Color(0.2156862745f, 0.2156862745f, 0.2078431373f, 1f), new Color(0f, 0f, 0f, 1f), new Color(0.1882352941f, 0.1882352941f, 0.1882352941f, 1f), new Color(0f, 0f, 0f, 1f), new Color(0.2117647059f, 0.2117647059f, 0.2117647059f, 1f), new Color(0f, 0f, 0f, 1f), new Color(0f, 0f, 0f, 1f)}));
        G2.fill(HOUSING_FRONT);

        final Random BW_RND = new Random();
        final Random ALPHA_RND = new Random();
        G2.setClip(HOUSING_FRONT);
        final Color DARK_NOISE = new Color(0.2f, 0.2f, 0.2f);
        final Color BRIGHT_NOISE = new Color(0.8f, 0.8f, 0.8f);
        Color noiseColor;
        int noiseAlpha;
        for (int y = 0 ; y < HOUSING_FRONT.getHeight() ; y ++) {
            for (int x = 0 ; x < HOUSING_FRONT.getWidth() ; x ++) {
                if (BW_RND.nextBoolean()) {
                    noiseColor = BRIGHT_NOISE;
                } else {
                    noiseColor = DARK_NOISE;
                }
                noiseAlpha = 10 + ALPHA_RND.nextInt(10) - 5;
                G2.setColor(new Color(noiseColor.getRed(), noiseColor.getGreen(), noiseColor.getBlue(), noiseAlpha));
                G2.drawLine((int) (x + HOUSING_FRONT.getMinX()), (int) (y + HOUSING_FRONT.getMinY()), (int) (x + HOUSING_FRONT.getMinX()), (int) (y + HOUSING_FRONT.getMinY()));
            }
        }


        G2.dispose();
        return IMAGE;
    }

    public BufferedImage createGreenLightImage(final int WIDTH, final int HEIGHT) {
        final GraphicsConfiguration GFX_CONF = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        if (WIDTH <= 0 || HEIGHT <= 0) {
            return GFX_CONF.createCompatibleImage(1, 1, java.awt.Transparency.TRANSLUCENT);
        }
        final BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();
        final Ellipse2D FRAME = new Ellipse2D.Double(0.10204081632653061 * IMAGE_WIDTH, 0.6654676258992805 * IMAGE_HEIGHT, 0.7959183673469388 * IMAGE_WIDTH, 0.2805755395683453 * IMAGE_HEIGHT);
        G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.6654676258992805 * IMAGE_HEIGHT), new Point2D.Double(0.5 * IMAGE_WIDTH, 0.9460431654676259 * IMAGE_HEIGHT), new float[]{0.0f, 0.05f, 0.1f, 0.17f, 0.27f, 1.0f}, new Color[]{new Color(1f, 1f, 1f, 1f), new Color(0.8f, 0.8f, 0.8f, 1f), new Color(0.6f, 0.6f, 0.6f, 1f), new Color(0.4f, 0.4f, 0.4f, 1f), new Color(0.2f, 0.2f, 0.2f, 1f), new Color(0.0039215686f, 0.0039215686f, 0.0039215686f, 1f)}));
        G2.fill(FRAME);

        final Ellipse2D INNER_CLIP = new Ellipse2D.Double(0.10204081632653061 * IMAGE_WIDTH, 0.6870503597122302 * IMAGE_HEIGHT, 0.7959183673469388 * IMAGE_WIDTH, 0.2589928057553957 * IMAGE_HEIGHT);
        G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.6870503597122302 * IMAGE_HEIGHT), new Point2D.Double(0.5 * IMAGE_WIDTH, 0.9460431654676259 * IMAGE_HEIGHT), new float[]{0.0f, 0.35f, 0.66f, 1.0f}, new Color[]{new Color(0f, 0f, 0f, 1f), new Color(0.0156862745f, 0.0156862745f, 0.0156862745f, 1f), new Color(0f, 0f, 0f, 1f), new Color(0.0039215686f, 0.0039215686f, 0.0039215686f, 1f)}));
        G2.fill(INNER_CLIP);

        final Ellipse2D LIGHT_EFFECT = new Ellipse2D.Double(0.14285714285714285 * IMAGE_WIDTH, 0.6834532374100719 * IMAGE_HEIGHT, 0.7142857142857143 * IMAGE_WIDTH, 0.2517985611510791 * IMAGE_HEIGHT);
        G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.8093525179856115 * IMAGE_HEIGHT), (0.3622448979591837f * IMAGE_WIDTH), new float[]{0.0f, 0.88f, 0.95f, 1.0f}, new Color[]{new Color(0f, 0f, 0f, 1f), new Color(0f, 0f, 0f, 1f), new Color(0.3686274510f, 0.3686274510f, 0.3686274510f, 1f), new Color(0.0039215686f, 0.0039215686f, 0.0039215686f, 1f)}));
        G2.fill(LIGHT_EFFECT);

        final Ellipse2D INNER_SHADOW = new Ellipse2D.Double(0.14285714285714285 * IMAGE_WIDTH, 0.6834532374100719 * IMAGE_HEIGHT, 0.7142857142857143 * IMAGE_WIDTH, 0.2517985611510791 * IMAGE_HEIGHT);
        G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.6870503597122302 * IMAGE_HEIGHT), new Point2D.Double(0.5 * IMAGE_WIDTH, 0.9172661870503597 * IMAGE_HEIGHT), new float[]{0.0f, 1.0f}, new Color[]{new Color(0f, 0f, 0f, 1f), new Color(0.0039215686f, 0.0039215686f, 0.0039215686f, 0f)}));
        G2.fill(INNER_SHADOW);

        G2.dispose();
        return IMAGE;
    }

    public BufferedImage createGreenOnImage(final int WIDTH, final int HEIGHT) {
        final GraphicsConfiguration GFX_CONF = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        if (WIDTH <= 0 || HEIGHT <= 0) {
            return GFX_CONF.createCompatibleImage(1, 1, java.awt.Transparency.TRANSLUCENT);
        }
        final BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();
        final Ellipse2D LIGHT_ON = new Ellipse2D.Double(0.17346938775510204 * IMAGE_WIDTH, 0.6942446043165468 * IMAGE_HEIGHT, 0.6530612244897959 * IMAGE_WIDTH, 0.2302158273381295 * IMAGE_HEIGHT);
        G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.8093525179856115 * IMAGE_HEIGHT), (0.32653061224489793f * IMAGE_WIDTH), new float[]{0.0f, 1.0f}, new Color[]{new Color(0.3333333333f, 0.7254901961f, 0.4823529412f, 1f), new Color(0f, 0.1215686275f, 0f, 1f)}));
        G2.fill(LIGHT_ON);

        final GeneralPath GLOW = new GeneralPath();
        GLOW.setWindingRule(Path2D.WIND_EVEN_ODD);
        GLOW.moveTo(0.0 * IMAGE_WIDTH, 0.8129496402877698 * IMAGE_HEIGHT);
        GLOW.curveTo(0.0 * IMAGE_WIDTH, 0.9100719424460432 * IMAGE_HEIGHT, 0.22448979591836735 * IMAGE_WIDTH, 0.9892086330935251 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 0.9892086330935251 * IMAGE_HEIGHT);
        GLOW.curveTo(0.7755102040816326 * IMAGE_WIDTH, 0.9892086330935251 * IMAGE_HEIGHT, 1.0 * IMAGE_WIDTH, 0.9100719424460432 * IMAGE_HEIGHT, 1.0 * IMAGE_WIDTH, 0.8093525179856115 * IMAGE_HEIGHT);
        GLOW.curveTo(0.9081632653061225 * IMAGE_WIDTH, 0.7517985611510791 * IMAGE_HEIGHT, 0.7040816326530612 * IMAGE_WIDTH, 0.6870503597122302 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 0.6870503597122302 * IMAGE_HEIGHT);
        GLOW.curveTo(0.2857142857142857 * IMAGE_WIDTH, 0.6870503597122302 * IMAGE_HEIGHT, 0.08163265306122448 * IMAGE_WIDTH, 0.7517985611510791 * IMAGE_HEIGHT, 0.0 * IMAGE_WIDTH, 0.8129496402877698 * IMAGE_HEIGHT);
        GLOW.closePath();
        G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.8093525179856115 * IMAGE_HEIGHT), (0.5153061224489796f * IMAGE_WIDTH), new float[]{0.0f, 1.0f}, new Color[]{new Color(0.2549019608f, 0.7333333333f, 0.4941176471f, 1f), new Color(0.0156862745f, 0.1450980392f, 0.0313725490f, 0f)}));
        G2.fill(GLOW);

        G2.dispose();
        return IMAGE;
    }

    public BufferedImage createGreenOffImage(final int WIDTH, final int HEIGHT) {
        final GraphicsConfiguration GFX_CONF = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        if (WIDTH <= 0 || HEIGHT <= 0) {
            return GFX_CONF.createCompatibleImage(1, 1, java.awt.Transparency.TRANSLUCENT);
        }
        final BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();
        final Ellipse2D LIGHT_OFF = new Ellipse2D.Double(0.17346938775510204 * IMAGE_WIDTH, 0.6942446043165468 * IMAGE_HEIGHT, 0.6530612244897959 * IMAGE_WIDTH, 0.2302158273381295 * IMAGE_HEIGHT);
        G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.8093525179856115 * IMAGE_HEIGHT), (0.32653061224489793f * IMAGE_WIDTH), new float[]{0.0f, 1.0f}, new Color[]{new Color(0f, 1f, 0f, 0.2470588235f), new Color(0f, 1f, 0f, 0.0470588235f)}));
        G2.fill(LIGHT_OFF);

        final Ellipse2D INNER_SHADOW = new Ellipse2D.Double(0.17346938775510204 * IMAGE_WIDTH, 0.6942446043165468 * IMAGE_HEIGHT, 0.6530612244897959 * IMAGE_WIDTH, 0.2302158273381295 * IMAGE_HEIGHT);
        G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.8093525179856115 * IMAGE_HEIGHT), (0.32653061224489793f * IMAGE_WIDTH), new float[]{0.0f, 0.55f, 0.5501f, 0.78f, 0.79f, 1.0f}, new Color[]{new Color(0.0039215686f, 0.0039215686f, 0.0039215686f, 0f), new Color(0f, 0f, 0f, 0f), new Color(0f, 0f, 0f, 0f), new Color(0f, 0f, 0f, 0.1215686275f), new Color(0f, 0f, 0f, 0.1294117647f), new Color(0f, 0f, 0f, 0.4980392157f)}));
        G2.fill(INNER_SHADOW);

        final TexturePaint HATCH_PAINT = new TexturePaint(HATCH_TEXTURE, new java.awt.Rectangle(0, 0, 2, 2));
        G2.setPaint(HATCH_PAINT);
        G2.fill(INNER_SHADOW);

        G2.dispose();
        return IMAGE;
    }

    public BufferedImage createYellowLightImage(final int WIDTH, final int HEIGHT) {
        final GraphicsConfiguration GFX_CONF = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        if (WIDTH <= 0 || HEIGHT <= 0) {
            return GFX_CONF.createCompatibleImage(1, 1, java.awt.Transparency.TRANSLUCENT);
        }
        final BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();
        final Ellipse2D FRAME = new Ellipse2D.Double(0.10204081632653061 * IMAGE_WIDTH, 0.35611510791366907 * IMAGE_HEIGHT, 0.7959183673469388 * IMAGE_WIDTH, 0.2805755395683453 * IMAGE_HEIGHT);
        G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.35611510791366907 * IMAGE_HEIGHT), new Point2D.Double(0.5 * IMAGE_WIDTH, 0.6366906474820144 * IMAGE_HEIGHT), new float[]{0.0f, 0.05f, 0.1f, 0.17f, 0.27f, 1.0f}, new Color[]{new Color(1f, 1f, 1f, 1f), new Color(0.8f, 0.8f, 0.8f, 1f), new Color(0.6f, 0.6f, 0.6f, 1f), new Color(0.4f, 0.4f, 0.4f, 1f), new Color(0.2f, 0.2f, 0.2f, 1f), new Color(0.0039215686f, 0.0039215686f, 0.0039215686f, 1f)}));
        G2.fill(FRAME);

        final Ellipse2D INNER_CLIP = new Ellipse2D.Double(0.10204081632653061 * IMAGE_WIDTH, 0.3776978417266187 * IMAGE_HEIGHT, 0.7959183673469388 * IMAGE_WIDTH, 0.2589928057553957 * IMAGE_HEIGHT);
        G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.3776978417266187 * IMAGE_HEIGHT), new Point2D.Double(0.5 * IMAGE_WIDTH, 0.6366906474820144 * IMAGE_HEIGHT), new float[]{0.0f, 0.35f, 0.66f, 1.0f}, new Color[]{new Color(0f, 0f, 0f, 1f), new Color(0.0156862745f, 0.0156862745f, 0.0156862745f, 1f), new Color(0f, 0f, 0f, 1f), new Color(0.0039215686f, 0.0039215686f, 0.0039215686f, 1f)}));
        G2.fill(INNER_CLIP);

        final Ellipse2D LIGHT_EFFECT = new Ellipse2D.Double(0.14285714285714285 * IMAGE_WIDTH, 0.37410071942446044 * IMAGE_HEIGHT, 0.7142857142857143 * IMAGE_WIDTH, 0.2517985611510791 * IMAGE_HEIGHT);
        G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.5 * IMAGE_HEIGHT), (0.3622448979591837f * IMAGE_WIDTH), new float[]{0.0f, 0.88f, 0.95f, 1.0f}, new Color[]{new Color(0f, 0f, 0f, 1f), new Color(0f, 0f, 0f, 1f), new Color(0.3686274510f, 0.3686274510f, 0.3686274510f, 1f), new Color(0.0039215686f, 0.0039215686f, 0.0039215686f, 1f)}));
        G2.fill(LIGHT_EFFECT);

        final Ellipse2D INNER_SHADOW = new Ellipse2D.Double(0.14285714285714285 * IMAGE_WIDTH, 0.37410071942446044 * IMAGE_HEIGHT, 0.7142857142857143 * IMAGE_WIDTH, 0.2517985611510791 * IMAGE_HEIGHT);
        G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.3776978417266187 * IMAGE_HEIGHT), new Point2D.Double(0.5 * IMAGE_WIDTH, 0.6079136690647482 * IMAGE_HEIGHT), new float[]{0.0f, 1.0f}, new Color[]{new Color(0f, 0f, 0f, 1f), new Color(0.0039215686f, 0.0039215686f, 0.0039215686f, 0f)}));
        G2.fill(INNER_SHADOW);

        G2.dispose();
        return IMAGE;
    }

    public BufferedImage createYellowOnImage(final int WIDTH, final int HEIGHT) {
        final GraphicsConfiguration GFX_CONF = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        if (WIDTH <= 0 || HEIGHT <= 0) {
            return GFX_CONF.createCompatibleImage(1, 1, java.awt.Transparency.TRANSLUCENT);
        }
        final BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();
        final Ellipse2D LIGHT_ON = new Ellipse2D.Double(0.17346938775510204 * IMAGE_WIDTH, 0.38489208633093525 * IMAGE_HEIGHT, 0.6530612244897959 * IMAGE_WIDTH, 0.2302158273381295 * IMAGE_HEIGHT);
        G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.5 * IMAGE_HEIGHT), (0.32653061224489793f * IMAGE_WIDTH), new float[]{0.0f, 1.0f}, new Color[]{new Color(0.9960784314f, 0.8313725490f, 0.2039215686f, 1f), new Color(0.5098039216f, 0.2f, 0.0470588235f, 1f)}));
        G2.fill(LIGHT_ON);

        final GeneralPath GLOW = new GeneralPath();
        GLOW.setWindingRule(Path2D.WIND_EVEN_ODD);
        GLOW.moveTo(0.0 * IMAGE_WIDTH, 0.5035971223021583 * IMAGE_HEIGHT);
        GLOW.curveTo(0.0 * IMAGE_WIDTH, 0.6007194244604317 * IMAGE_HEIGHT, 0.22448979591836735 * IMAGE_WIDTH, 0.6798561151079137 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 0.6798561151079137 * IMAGE_HEIGHT);
        GLOW.curveTo(0.7755102040816326 * IMAGE_WIDTH, 0.6798561151079137 * IMAGE_HEIGHT, 1.0 * IMAGE_WIDTH, 0.6007194244604317 * IMAGE_HEIGHT, 1.0 * IMAGE_WIDTH, 0.5 * IMAGE_HEIGHT);
        GLOW.curveTo(0.9081632653061225 * IMAGE_WIDTH, 0.44244604316546765 * IMAGE_HEIGHT, 0.7040816326530612 * IMAGE_WIDTH, 0.3776978417266187 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 0.3776978417266187 * IMAGE_HEIGHT);
        GLOW.curveTo(0.2857142857142857 * IMAGE_WIDTH, 0.3776978417266187 * IMAGE_HEIGHT, 0.08163265306122448 * IMAGE_WIDTH, 0.44244604316546765 * IMAGE_HEIGHT, 0.0 * IMAGE_WIDTH, 0.5035971223021583 * IMAGE_HEIGHT);
        GLOW.closePath();
        G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.5 * IMAGE_HEIGHT), (0.5153061224489796f * IMAGE_WIDTH), new float[]{0.0f, 1.0f}, new Color[]{new Color(0.9960784314f, 0.8313725490f, 0.2039215686f, 1f), new Color(0.5098039216f, 0.2f, 0.0470588235f, 0f)}));
        G2.fill(GLOW);

        G2.dispose();
        return IMAGE;
    }

    public BufferedImage createYellowOffImage(final int WIDTH, final int HEIGHT) {
        final GraphicsConfiguration GFX_CONF = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        if (WIDTH <= 0 || HEIGHT <= 0) {
            return GFX_CONF.createCompatibleImage(1, 1, java.awt.Transparency.TRANSLUCENT);
        }
        final BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();
        final Ellipse2D LIGHT_OFF = new Ellipse2D.Double(0.17346938775510204 * IMAGE_WIDTH, 0.38489208633093525 * IMAGE_HEIGHT, 0.6530612244897959 * IMAGE_WIDTH, 0.2302158273381295 * IMAGE_HEIGHT);
        G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.5 * IMAGE_HEIGHT), (0.32653061224489793f * IMAGE_WIDTH), new float[]{0.0f, 1.0f}, new Color[]{new Color(1f, 1f, 0f, 0.2470588235f), new Color(1f, 1f, 0f, 0.0470588235f)}));
        G2.fill(LIGHT_OFF);

        final Ellipse2D INNER_SHADOW = new Ellipse2D.Double(0.17346938775510204 * IMAGE_WIDTH, 0.38489208633093525 * IMAGE_HEIGHT, 0.6530612244897959 * IMAGE_WIDTH, 0.2302158273381295 * IMAGE_HEIGHT);
        G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.5 * IMAGE_HEIGHT), (0.32653061224489793f * IMAGE_WIDTH), new float[]{0.0f, 0.55f, 0.5501f, 0.78f, 0.79f, 1.0f}, new Color[]{new Color(0.0039215686f, 0.0039215686f, 0.0039215686f, 0f), new Color(0f, 0f, 0f, 0f), new Color(0f, 0f, 0f, 0f), new Color(0f, 0f, 0f, 0.1215686275f), new Color(0f, 0f, 0f, 0.1294117647f), new Color(0f, 0f, 0f, 0.4980392157f)}));
        G2.fill(INNER_SHADOW);

        final TexturePaint HATCH_PAINT = new TexturePaint(HATCH_TEXTURE, new java.awt.Rectangle(0, 0, 2, 2));
        G2.setPaint(HATCH_PAINT);
        G2.fill(INNER_SHADOW);

        G2.dispose();
        return IMAGE;
    }

    public BufferedImage createRedLightImage(final int WIDTH, final int HEIGHT) {
        final GraphicsConfiguration GFX_CONF = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        if (WIDTH <= 0 || HEIGHT <= 0) {
            return GFX_CONF.createCompatibleImage(1, 1, java.awt.Transparency.TRANSLUCENT);
        }
        final BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();
        final Ellipse2D FRAME = new Ellipse2D.Double(0.10204081632653061 * IMAGE_WIDTH, 0.046762589928057555 * IMAGE_HEIGHT, 0.7959183673469388 * IMAGE_WIDTH, 0.2805755395683453 * IMAGE_HEIGHT);
        G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.046762589928057555 * IMAGE_HEIGHT), new Point2D.Double(0.5 * IMAGE_WIDTH, 0.3273381294964029 * IMAGE_HEIGHT), new float[]{0.0f, 0.05f, 0.1f, 0.17f, 0.27f, 1.0f}, new Color[]{new Color(1f, 1f, 1f, 1f), new Color(0.8f, 0.8f, 0.8f, 1f), new Color(0.6f, 0.6f, 0.6f, 1f), new Color(0.4f, 0.4f, 0.4f, 1f), new Color(0.2f, 0.2f, 0.2f, 1f), new Color(0.0039215686f, 0.0039215686f, 0.0039215686f, 1f)}));
        G2.fill(FRAME);

        final Ellipse2D INNER_CLIP = new Ellipse2D.Double(0.10204081632653061 * IMAGE_WIDTH, 0.0683453237410072 * IMAGE_HEIGHT, 0.7959183673469388 * IMAGE_WIDTH, 0.2589928057553957 * IMAGE_HEIGHT);
        G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.0683453237410072 * IMAGE_HEIGHT), new Point2D.Double(0.5 * IMAGE_WIDTH, 0.3273381294964029 * IMAGE_HEIGHT), new float[]{0.0f, 0.35f, 0.66f, 1.0f}, new Color[]{new Color(0f, 0f, 0f, 1f), new Color(0.0156862745f, 0.0156862745f, 0.0156862745f, 1f), new Color(0f, 0f, 0f, 1f), new Color(0.0039215686f, 0.0039215686f, 0.0039215686f, 1f)}));
        G2.fill(INNER_CLIP);

        final Ellipse2D LIGHT_EFFECT = new Ellipse2D.Double(0.14285714285714285 * IMAGE_WIDTH, 0.06474820143884892 * IMAGE_HEIGHT, 0.7142857142857143 * IMAGE_WIDTH, 0.2517985611510791 * IMAGE_HEIGHT);
        G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.1906474820143885 * IMAGE_HEIGHT), (0.3622448979591837f * IMAGE_WIDTH), new float[]{0.0f, 0.88f, 0.95f, 1.0f}, new Color[]{new Color(0f, 0f, 0f, 1f), new Color(0f, 0f, 0f, 1f), new Color(0.3686274510f, 0.3686274510f, 0.3686274510f, 1f), new Color(0.0039215686f, 0.0039215686f, 0.0039215686f, 1f)}));
        G2.fill(LIGHT_EFFECT);

        final Ellipse2D INNER_SHADOW = new Ellipse2D.Double(0.14285714285714285 * IMAGE_WIDTH, 0.06474820143884892 * IMAGE_HEIGHT, 0.7142857142857143 * IMAGE_WIDTH, 0.2517985611510791 * IMAGE_HEIGHT);
        G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.0683453237410072 * IMAGE_HEIGHT), new Point2D.Double(0.5 * IMAGE_WIDTH, 0.29856115107913667 * IMAGE_HEIGHT), new float[]{0.0f, 1.0f}, new Color[]{new Color(0f, 0f, 0f, 1f), new Color(0.0039215686f, 0.0039215686f, 0.0039215686f, 0f)}));
        G2.fill(INNER_SHADOW);

        G2.dispose();
        return IMAGE;
    }

    public BufferedImage createRedOnImage(final int WIDTH, final int HEIGHT) {
        final GraphicsConfiguration GFX_CONF = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        if (WIDTH <= 0 || HEIGHT <= 0) {
            return GFX_CONF.createCompatibleImage(1, 1, java.awt.Transparency.TRANSLUCENT);
        }
        final BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();
        final Ellipse2D LIGHT_ON = new Ellipse2D.Double(0.17346938775510204 * IMAGE_WIDTH, 0.07553956834532374 * IMAGE_HEIGHT, 0.6530612244897959 * IMAGE_WIDTH, 0.2302158273381295 * IMAGE_HEIGHT);
        G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.1906474820143885 * IMAGE_HEIGHT), (0.32653061224489793f * IMAGE_WIDTH), new float[]{0.0f, 1.0f}, new Color[]{new Color(1f, 0f, 0f, 1f), new Color(0.2549019608f, 0f, 0.0156862745f, 1f)}));
        G2.fill(LIGHT_ON);

        final GeneralPath GLOW = new GeneralPath();
        GLOW.setWindingRule(Path2D.WIND_EVEN_ODD);
        GLOW.moveTo(0.0 * IMAGE_WIDTH, 0.19424460431654678 * IMAGE_HEIGHT);
        GLOW.curveTo(0.0 * IMAGE_WIDTH, 0.29136690647482016 * IMAGE_HEIGHT, 0.22448979591836735 * IMAGE_WIDTH, 0.37050359712230213 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 0.37050359712230213 * IMAGE_HEIGHT);
        GLOW.curveTo(0.7755102040816326 * IMAGE_WIDTH, 0.37050359712230213 * IMAGE_HEIGHT, 1.0 * IMAGE_WIDTH, 0.29136690647482016 * IMAGE_HEIGHT, 1.0 * IMAGE_WIDTH, 0.1906474820143885 * IMAGE_HEIGHT);
        GLOW.curveTo(0.9081632653061225 * IMAGE_WIDTH, 0.13309352517985612 * IMAGE_HEIGHT, 0.7040816326530612 * IMAGE_WIDTH, 0.0683453237410072 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 0.0683453237410072 * IMAGE_HEIGHT);
        GLOW.curveTo(0.2857142857142857 * IMAGE_WIDTH, 0.0683453237410072 * IMAGE_HEIGHT, 0.08163265306122448 * IMAGE_WIDTH, 0.13309352517985612 * IMAGE_HEIGHT, 0.0 * IMAGE_WIDTH, 0.19424460431654678 * IMAGE_HEIGHT);
        GLOW.closePath();
        G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.1906474820143885 * IMAGE_HEIGHT), (0.5153061224489796f * IMAGE_WIDTH), new float[]{0.0f, 1.0f}, new Color[]{new Color(1f, 0f, 0f, 1f), new Color(0.4627450980f, 0.0196078431f, 0.0039215686f, 0f)}));
        G2.fill(GLOW);

        G2.dispose();
        return IMAGE;
    }

    public BufferedImage createRedOffImage(final int WIDTH, final int HEIGHT) {
        final GraphicsConfiguration GFX_CONF = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        if (WIDTH <= 0 || HEIGHT <= 0) {
            return GFX_CONF.createCompatibleImage(1, 1, java.awt.Transparency.TRANSLUCENT);
        }
        final BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();
        final Ellipse2D LIGHT_OFF = new Ellipse2D.Double(0.17346938775510204 * IMAGE_WIDTH, 0.07553956834532374 * IMAGE_HEIGHT, 0.6530612244897959 * IMAGE_WIDTH, 0.2302158273381295 * IMAGE_HEIGHT);
        G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.1906474820143885 * IMAGE_HEIGHT), (0.32653061224489793f * IMAGE_WIDTH), new float[]{0.0f, 1.0f}, new Color[]{new Color(1f, 0f, 0f, 0.2470588235f), new Color(1f, 0f, 0f, 0.0470588235f)}));
        G2.fill(LIGHT_OFF);

        final Ellipse2D INNER_SHADOW = new Ellipse2D.Double(0.17346938775510204 * IMAGE_WIDTH, 0.07553956834532374 * IMAGE_HEIGHT, 0.6530612244897959 * IMAGE_WIDTH, 0.2302158273381295 * IMAGE_HEIGHT);
        G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.1906474820143885 * IMAGE_HEIGHT), (0.32653061224489793f * IMAGE_WIDTH), new float[]{0.0f, 0.55f, 0.5501f, 0.78f, 0.79f, 1.0f}, new Color[]{new Color(0.0039215686f, 0.0039215686f, 0.0039215686f, 0f), new Color(0f, 0f, 0f, 0f), new Color(0f, 0f, 0f, 0f), new Color(0f, 0f, 0f, 0.1215686275f), new Color(0f, 0f, 0f, 0.1294117647f), new Color(0f, 0f, 0f, 0.4980392157f)}));
        G2.fill(INNER_SHADOW);

        final TexturePaint HATCH_PAINT = new TexturePaint(HATCH_TEXTURE, new java.awt.Rectangle(0, 0, 2, 2));
        G2.setPaint(HATCH_PAINT);
        G2.fill(INNER_SHADOW);

        G2.dispose();
        return IMAGE;
    }

    private static BufferedImage createHatchTexture() {
        final GraphicsConfiguration GFX_CONF = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final BufferedImage IMAGE = GFX_CONF.createCompatibleImage(2, 2, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setColor(new Color(0.0f, 0.0f, 0.0f, 0.1f));
        G2.drawLine(0, 0, 1, 0);
        G2.drawLine(0, 1, 0, 1);
        G2.dispose();
        return IMAGE;
    }

    @Override
    public void actionPerformed(final ActionEvent EVENT) {
        blink ^= true;

        redOn = redBlinking ? blink : false;
        yellowOn = yellowBlinking ? blink : false;
        greenOn = greenBlinking ? blink : false;

        if (redOn) {
            propertySupport.firePropertyChange(RED_PROPERTY, !redOn, redOn);
        }

        if (yellowOn) {
            propertySupport.firePropertyChange(RED_PROPERTY, !yellowOn, yellowOn);
        }

        if (greenOn) {
            propertySupport.firePropertyChange(RED_PROPERTY, !greenOn, greenOn);
        }

        repaint(INNER_BOUNDS);
    }

	@Override
	public String toString() {
		return "Trafficlight";
	}
}
