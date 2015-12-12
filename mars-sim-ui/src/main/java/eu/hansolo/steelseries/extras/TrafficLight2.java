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

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
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
import javax.swing.JComponent;
import javax.swing.Timer;

/**
 *
 * @author Gerrit Grunwald <han.solo at muenster.de>
 */
public class TrafficLight2 extends JComponent implements ActionListener {
    public static final String RED_PROPERTY = "red";
    public static final String YELLOW_PROPERTY = "yellow";
    public static final String GREEN_PROPERTY = "green";
    private boolean blink;
    private boolean blinking;
    private boolean redOn;
    private boolean redBlinkEnabled;
    private boolean yellowVisible;
    private boolean yellowOn;
    private boolean yellowBlinkEnabled;
    private boolean greenOn;
    private boolean greenBlinkEnabled;
    private PropertyChangeSupport propertySupport;
    private final Rectangle INNER_BOUNDS = new Rectangle(0, 0, 80, 200);
    private final Point2D CENTER;
    private BufferedImage housingImage;
    private BufferedImage greenOnImage;
    private BufferedImage greenOffImage;
    private BufferedImage yellowOnImage;
    private BufferedImage yellowOffImage;
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

    public TrafficLight2() {
        super();
        addComponentListener(COMPONENT_LISTENER);
        propertySupport = new PropertyChangeSupport(this);
        CENTER = new Point2D.Double();
        timerPeriod = 1000;
        TIMER = new Timer(timerPeriod, this);
        housingImage = createImage(INNER_BOUNDS.width, INNER_BOUNDS.height, Transparency.TRANSLUCENT);
        greenOnImage = createImage(INNER_BOUNDS.width, INNER_BOUNDS.height, Transparency.TRANSLUCENT);
        greenOffImage = createImage(INNER_BOUNDS.width, INNER_BOUNDS.height, Transparency.TRANSLUCENT);
        yellowOnImage = createImage(INNER_BOUNDS.width, INNER_BOUNDS.height, Transparency.TRANSLUCENT);
        yellowOffImage = createImage(INNER_BOUNDS.width, INNER_BOUNDS.height, Transparency.TRANSLUCENT);
        redOnImage = createImage(INNER_BOUNDS.width, INNER_BOUNDS.height, Transparency.TRANSLUCENT);
        redOffImage = createImage(INNER_BOUNDS.width, INNER_BOUNDS.height, Transparency.TRANSLUCENT);
        blink = false;
        blinking = false;
        redOn = false;
        redBlinkEnabled = false;
        yellowVisible = true;
        yellowOn = false;
        yellowBlinkEnabled = false;
        greenOn = false;
        greenBlinkEnabled = false;
        square = false;
    }

    public final void init(final int WIDTH, final int HEIGHT) {
        if (WIDTH <= 1 || HEIGHT <= 1) {
            return;
        }
        if (housingImage != null) {
            housingImage.flush();
        }
        housingImage = yellowVisible ? create3LightsHousingImage(WIDTH, HEIGHT) : create2LightsHousingImage(WIDTH, HEIGHT);
        if (greenOnImage != null) {
            greenOnImage.flush();
        }
        greenOnImage = yellowVisible ? create3LightsGreenImage(WIDTH, HEIGHT, true) : create2LightsGreenImage(WIDTH, HEIGHT, true);
        if (greenOffImage != null) {
            greenOffImage.flush();
        }
        greenOffImage = yellowVisible ? create3LightsGreenImage(WIDTH, HEIGHT, false) : create2LightsGreenImage(WIDTH, HEIGHT, false);
        if (yellowOnImage != null) {
            yellowOnImage.flush();
        }
        yellowOnImage = create3LightsYellowImage(WIDTH, HEIGHT, true);
        if (yellowOffImage != null) {
            yellowOffImage.flush();
        }
        yellowOffImage = create3LightsYellowImage(WIDTH, HEIGHT, false);
        if (redOnImage != null) {
            redOnImage.flush();
        }
        redOnImage = yellowVisible ? create3LightsRedImage(WIDTH, HEIGHT, true) : create2LightsRedImage(WIDTH, HEIGHT, true);
        if (redOffImage != null) {
            redOffImage.flush();
        }
        redOffImage = yellowVisible ? create3LightsRedImage(WIDTH, HEIGHT, false) : create2LightsRedImage(WIDTH, HEIGHT, false);
        CENTER.setLocation(WIDTH / 2.0, HEIGHT / 2.0);
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Create the Graphics2D object
        final Graphics2D G2 = (Graphics2D) g.create();

        // Set the rendering hints
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        // Take insets into account (e.g. used by borders)
        G2.translate(getInnerBounds().x, getInnerBounds().y);

        // Housing
        G2.drawImage(housingImage, 0, 0, null);

        // Green
        if (greenOn) {
            G2.drawImage(greenOnImage, 0, 0, null);
        } else {
            G2.drawImage(greenOffImage, 0, 0, null);
        }

        // Yellow
        if (yellowVisible) {
            if (yellowOn) {
                G2.drawImage(yellowOnImage, 0, 0, null);
            } else {
                G2.drawImage(yellowOffImage, 0, 0, null);
            }
        }

        // Red
        if (redOn) {
            G2.drawImage(redOnImage, 0, 0, null);
        } else {
            G2.drawImage(redOffImage, 0, 0, null);
        }

        // Dispose the temp graphics object
        G2.dispose();
    }

    /**
     * Returns true if the trafficlight is blinking
     * @return true if the trafficlight is blinking
     */
    public boolean isBlinking() {
        return blinking;
    }

    /**
     * Enables/disables the blinking of the traffic light
     * @param BLINKING
     */
    public void setBlinking(final boolean BLINKING) {
        blinking = BLINKING;
        if (blinking) {
            TIMER.start();
        } else {
            TIMER.stop();
        }
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
     * Returns true if blinking of the red light is enabled
     * @return true if blinking of the red light is enabled
     */
    public boolean isRedBlinkEnabled() {
        return redBlinkEnabled;
    }

    /**
     * Enable/disable the blinking of the red light
     * @param RED_BLINK_ENABLED
     */
    public void setRedBlinkEnabled(final boolean RED_BLINK_ENABLED) {
        redBlinkEnabled = RED_BLINK_ENABLED;
        redOn = redBlinkEnabled;
    }

    /**
     * Returns true if the yellow light is visible
     * @return true if the yellow light is visible
     */
    public boolean isYellowVisible() {
        return yellowVisible;
    }

    /**
     * Enables/disables the visibility of the yellow light
     * @param YELLOW_VISIBLE
     */
    public void setYellowVisible(final boolean YELLOW_VISIBLE) {
        yellowVisible = YELLOW_VISIBLE;
        calcInnerBounds();
        init(getInnerBounds().width, getInnerBounds().height);
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
     * Returns true if blinking of the yellow light is enabled
     * @return true if blinking of the yellow light is enabled
     */
    public boolean isYellowBlinkEnabled() {
        return yellowBlinkEnabled;
    }

    /**
     * Enables/disables the blinking of the yellow light
     * @param YELLOW_BLINK_ENABLED
     */
    public void setYellowBlinkEnabled(final boolean YELLOW_BLINK_ENABLED) {
        yellowBlinkEnabled = YELLOW_BLINK_ENABLED;
        yellowOn = yellowBlinkEnabled;
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
     * Returns true if blinking of the green light is enabled
     * @return true if blinking of the green light is enabled
     */
    public boolean isGreenBlinkEnabled() {
        return greenBlinkEnabled;
    }

    /**
     * Enables/disables blinking of the green light
     * @param GREEN_BLINK_ENABLED
     */
    public void setGreenBlinkEnabled(final boolean GREEN_BLINK_ENABLED) {
        greenBlinkEnabled = GREEN_BLINK_ENABLED;
        greenOn = greenBlinkEnabled;
    }

    /**
     * Returns the interval in milliseconds that will be used for the blinking
     * @return the interval in milliseconds that will be used for the blinking
     */
    public int getTimerPeriod() {
        return timerPeriod;
    }

    /**
     * Sets the interval that will be used for the blinking
     * Values will be in the range of 100 - 10000 milliseconds
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
        if (yellowVisible) {
            final int PREF_HEIGHT = getWidth() < (int) (getHeight() * 0.4) ? (int) (getWidth() * 2.5) : getHeight();
            INNER_BOUNDS.setBounds(INSETS.left, INSETS.top, (int) (PREF_HEIGHT * 0.4)  - INSETS.left - INSETS.right, PREF_HEIGHT - INSETS.top - INSETS.bottom);
        } else {
            final int PREF_HEIGHT = getWidth() < (int) (getHeight() * 0.5714285714) ? (int) (getWidth() * 1.75) : getHeight();
            INNER_BOUNDS.setBounds(INSETS.left, INSETS.top, (int) (PREF_HEIGHT * 0.5714285714)  - INSETS.left - INSETS.right, PREF_HEIGHT - INSETS.top - INSETS.bottom);
        }
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

    /**
     * Returns a compatible image of the given size and transparency
     * @param WIDTH
     * @param HEIGHT
     * @param TRANSPARENCY
     * @return a compatible image of the given size and transparency
     */
    private BufferedImage createImage(final int WIDTH, final int HEIGHT, final int TRANSPARENCY) {
        final GraphicsConfiguration GFX_CONF = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        if (WIDTH <= 0 || HEIGHT <= 0) {
            return GFX_CONF.createCompatibleImage(1, 1, TRANSPARENCY);
        }
        final BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, HEIGHT, TRANSPARENCY);
        return IMAGE;
    }

	// Image methods
    public BufferedImage create2LightsHousingImage(final int WIDTH, final int HEIGHT) {
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
        final GeneralPath HOUSING_FRAME = new GeneralPath();
        HOUSING_FRAME.setWindingRule(Path2D.WIND_EVEN_ODD);
        HOUSING_FRAME.moveTo(0.125 * IMAGE_WIDTH, 0.2857142857142857 * IMAGE_HEIGHT);
        HOUSING_FRAME.curveTo(0.125 * IMAGE_WIDTH, 0.16428571428571428 * IMAGE_HEIGHT, 0.2875 * IMAGE_WIDTH, 0.07142857142857142 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 0.07142857142857142 * IMAGE_HEIGHT);
        HOUSING_FRAME.curveTo(0.7125 * IMAGE_WIDTH, 0.07142857142857142 * IMAGE_HEIGHT, 0.875 * IMAGE_WIDTH, 0.16428571428571428 * IMAGE_HEIGHT, 0.875 * IMAGE_WIDTH, 0.2857142857142857 * IMAGE_HEIGHT);
        HOUSING_FRAME.curveTo(0.875 * IMAGE_WIDTH, 0.2857142857142857 * IMAGE_HEIGHT, 0.875 * IMAGE_WIDTH, 0.7142857142857143 * IMAGE_HEIGHT, 0.875 * IMAGE_WIDTH, 0.7142857142857143 * IMAGE_HEIGHT);
        HOUSING_FRAME.curveTo(0.875 * IMAGE_WIDTH, 0.8357142857142857 * IMAGE_HEIGHT, 0.7125 * IMAGE_WIDTH, 0.9285714285714286 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 0.9285714285714286 * IMAGE_HEIGHT);
        HOUSING_FRAME.curveTo(0.2875 * IMAGE_WIDTH, 0.9285714285714286 * IMAGE_HEIGHT, 0.125 * IMAGE_WIDTH, 0.8357142857142857 * IMAGE_HEIGHT, 0.125 * IMAGE_WIDTH, 0.7142857142857143 * IMAGE_HEIGHT);
        HOUSING_FRAME.curveTo(0.125 * IMAGE_WIDTH, 0.7142857142857143 * IMAGE_HEIGHT, 0.125 * IMAGE_WIDTH, 0.2857142857142857 * IMAGE_HEIGHT, 0.125 * IMAGE_WIDTH, 0.2857142857142857 * IMAGE_HEIGHT);
        HOUSING_FRAME.closePath();
        HOUSING_FRAME.moveTo(0.0 * IMAGE_WIDTH, 0.2857142857142857 * IMAGE_HEIGHT);
        HOUSING_FRAME.curveTo(0.0 * IMAGE_WIDTH, 0.2857142857142857 * IMAGE_HEIGHT, 0.0 * IMAGE_WIDTH, 0.7142857142857143 * IMAGE_HEIGHT, 0.0 * IMAGE_WIDTH, 0.7142857142857143 * IMAGE_HEIGHT);
        HOUSING_FRAME.curveTo(0.0 * IMAGE_WIDTH, 0.8714285714285714 * IMAGE_HEIGHT, 0.225 * IMAGE_WIDTH, 1.0 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 1.0 * IMAGE_HEIGHT);
        HOUSING_FRAME.curveTo(0.775 * IMAGE_WIDTH, 1.0 * IMAGE_HEIGHT, 1.0 * IMAGE_WIDTH, 0.8714285714285714 * IMAGE_HEIGHT, 1.0 * IMAGE_WIDTH, 0.7142857142857143 * IMAGE_HEIGHT);
        HOUSING_FRAME.curveTo(1.0 * IMAGE_WIDTH, 0.7142857142857143 * IMAGE_HEIGHT, 1.0 * IMAGE_WIDTH, 0.2857142857142857 * IMAGE_HEIGHT, 1.0 * IMAGE_WIDTH, 0.2857142857142857 * IMAGE_HEIGHT);
        HOUSING_FRAME.curveTo(1.0 * IMAGE_WIDTH, 0.12857142857142856 * IMAGE_HEIGHT, 0.775 * IMAGE_WIDTH, 0.0 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 0.0 * IMAGE_HEIGHT);
        HOUSING_FRAME.curveTo(0.225 * IMAGE_WIDTH, 0.0 * IMAGE_HEIGHT, 0.0 * IMAGE_WIDTH, 0.12857142857142856 * IMAGE_HEIGHT, 0.0 * IMAGE_WIDTH, 0.2857142857142857 * IMAGE_HEIGHT);
        HOUSING_FRAME.closePath();
        G2.setPaint(new Color(0.2f, 0.2f, 0.2f, 0.5f));
        G2.fill(HOUSING_FRAME);

        final RoundRectangle2D HOUSING_BACKGROUND = new RoundRectangle2D.Double(0.125 * IMAGE_WIDTH, 0.07142857142857142 * IMAGE_HEIGHT, 0.75 * IMAGE_WIDTH, 0.8571428571428571 * IMAGE_HEIGHT, 0.75 * IMAGE_WIDTH, 0.42857142857142855 * IMAGE_HEIGHT);
        G2.setPaint(new Color(0.8f, 0.8f, 0.8f, 0.5f));
        G2.fill(HOUSING_BACKGROUND);

        G2.dispose();
        return IMAGE;
    }

    public BufferedImage create2LightsGreenImage(final int WIDTH, final int HEIGHT, final boolean IS_ON) {
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

        if (!IS_ON) {
            final Ellipse2D GREEN_OFF = new Ellipse2D.Double(0.1875 * IMAGE_WIDTH, 0.5285714285714286 * IMAGE_HEIGHT, 0.625 * IMAGE_WIDTH, 0.35714285714285715 * IMAGE_HEIGHT);
            G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.8214285714285714 * IMAGE_HEIGHT), (0.59375f * IMAGE_WIDTH), new float[]{0.0f, 0.98f, 0.99f, 1.0f}, new Color[]{new Color(0.0980392157f, 0.3372549020f, 0f, 1f), new Color(0f, 0.0039215686f, 0f, 1f), new Color(0f, 0f, 0f, 1f), new Color(0f, 0f, 0f, 1f)}));
            G2.fill(GREEN_OFF);

            final Ellipse2D GREEN_HIGHLIGHT_OFF = new Ellipse2D.Double(0.2625 * IMAGE_WIDTH, 0.5357142857142857 * IMAGE_HEIGHT, 0.4625 * IMAGE_WIDTH, 0.14285714285714285 * IMAGE_HEIGHT);
            G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.5642857142857143 * IMAGE_HEIGHT), (0.2125f * IMAGE_WIDTH), new float[]{0.0f, 0.98f, 1.0f}, new Color[]{new Color(1f, 1f, 1f, 0.2235294118f), new Color(1f, 1f, 1f, 0.0274509804f), new Color(1f, 1f, 1f, 0.0274509804f)}));
            G2.fill(GREEN_HIGHLIGHT_OFF);
        } else {
            final Ellipse2D GREEN_GLOW = new Ellipse2D.Double(0.0 * IMAGE_WIDTH, 0.42857142857142855 * IMAGE_HEIGHT, 1.0 * IMAGE_WIDTH, 0.5714285714285714 * IMAGE_HEIGHT);
            G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.7142857142857143 * IMAGE_HEIGHT), (0.5f * IMAGE_WIDTH), new float[]{0.0f, 0.98f, 0.99f, 1.0f}, new Color[]{new Color(0f, 1f, 0f, 1f), new Color(0f, 1f, 0f, 0f), new Color(0f, 1f, 0f, 0f), new Color(0f, 1f, 0f, 0f)}));
            G2.fill(GREEN_GLOW);

            final Ellipse2D GREEN_ON = new Ellipse2D.Double(0.1875 * IMAGE_WIDTH, 0.5285714285714286 * IMAGE_HEIGHT, 0.625 * IMAGE_WIDTH, 0.35714285714285715 * IMAGE_HEIGHT);
            G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.8214285714285714 * IMAGE_HEIGHT), (0.59375f * IMAGE_WIDTH), new float[]{0.0f, 0.98f, 0.99f, 1.0f}, new Color[]{new Color(0f, 1f, 0f, 1f), new Color(0.1254901961f, 0.2784313725f, 0.1411764706f, 1f), new Color(0.1254901961f, 0.2705882353f, 0.1411764706f, 1f), new Color(0.1254901961f, 0.2705882353f, 0.1411764706f, 1f)}));
            G2.fill(GREEN_ON);

            final Ellipse2D GREEN_HIGHLIGHT_ON = new Ellipse2D.Double(0.2625 * IMAGE_WIDTH, 0.5357142857142857 * IMAGE_HEIGHT, 0.4625 * IMAGE_WIDTH, 0.14285714285714285 * IMAGE_HEIGHT);
            G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.5642857142857143 * IMAGE_HEIGHT), (0.2125f * IMAGE_WIDTH), new float[]{0.0f, 0.98f, 1.0f}, new Color[]{new Color(1f, 1f, 1f, 0.6745098039f), new Color(1f, 1f, 1f, 0.0862745098f), new Color(1f, 1f, 1f, 0.0862745098f)}));
            G2.fill(GREEN_HIGHLIGHT_ON);
        }

        final Ellipse2D GREEN_INNER_SHADOW = new Ellipse2D.Double(0.1875 * IMAGE_WIDTH, 0.5285714285714286 * IMAGE_HEIGHT, 0.625 * IMAGE_WIDTH, 0.35714285714285715 * IMAGE_HEIGHT);
        G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.7071428571428572 * IMAGE_HEIGHT), (0.3125f * IMAGE_WIDTH), new float[]{0.0f, 0.55f, 0.5501f, 0.78f, 0.79f, 1.0f}, new Color[]{new Color(0.0039215686f, 0.0039215686f, 0.0039215686f, 0f), new Color(0f, 0f, 0f, 0f), new Color(0f, 0f, 0f, 0f), new Color(0f, 0f, 0f, 0.1215686275f), new Color(0f, 0f, 0f, 0.1294117647f), new Color(0f, 0f, 0f, 0.4980392157f)}));
        G2.fill(GREEN_INNER_SHADOW);

        G2.dispose();
        return IMAGE;
    }

    public BufferedImage create2LightsRedImage(final int WIDTH, final int HEIGHT, final boolean IS_ON) {
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
        if (!IS_ON) {
            final Ellipse2D RED_OFF = new Ellipse2D.Double(0.1875 * IMAGE_WIDTH, 0.11428571428571428 * IMAGE_HEIGHT, 0.625 * IMAGE_WIDTH, 0.35714285714285715 * IMAGE_HEIGHT);
            G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.40714285714285714 * IMAGE_HEIGHT), (0.59375f * IMAGE_WIDTH), new float[]{0.0f, 0.98f, 0.99f, 1.0f}, new Color[]{new Color(0.3019607843f, 0f, 0f, 1f), new Color(0.0039215686f, 0f, 0f, 1f), new Color(0f, 0f, 0f, 1f), new Color(0f, 0f, 0f, 1f)}));
            G2.fill(RED_OFF);

            final Ellipse2D RED_HIGHLIGHT_OFF = new Ellipse2D.Double(0.2625 * IMAGE_WIDTH, 0.12142857142857143 * IMAGE_HEIGHT, 0.4625 * IMAGE_WIDTH, 0.14285714285714285 * IMAGE_HEIGHT);
            G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.15 * IMAGE_HEIGHT), (0.2125f * IMAGE_WIDTH), new float[]{0.0f, 0.98f, 1.0f}, new Color[]{new Color(1f, 1f, 1f, 0.2235294118f), new Color(1f, 1f, 1f, 0.0274509804f), new Color(1f, 1f, 1f, 0.0274509804f)}));
            G2.fill(RED_HIGHLIGHT_OFF);
        } else {
            final Ellipse2D RED_GLOW = new Ellipse2D.Double(0.0 * IMAGE_WIDTH, 0.0 * IMAGE_HEIGHT, 1.0 * IMAGE_WIDTH, 0.5714285714285714 * IMAGE_HEIGHT);
            G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.2857142857142857 * IMAGE_HEIGHT), (0.5f * IMAGE_WIDTH), new float[]{0.0f, 0.98f, 0.99f, 1.0f}, new Color[]{new Color(1f, 0f, 0f, 1f), new Color(1f, 0f, 0f, 0f), new Color(1f, 0f, 0f, 0f), new Color(1f, 0f, 0f, 0f)}));
            G2.fill(RED_GLOW);

            final Ellipse2D RED_ON = new Ellipse2D.Double(0.1875 * IMAGE_WIDTH, 0.11428571428571428 * IMAGE_HEIGHT, 0.625 * IMAGE_WIDTH, 0.35714285714285715 * IMAGE_HEIGHT);
            G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.40714285714285714 * IMAGE_HEIGHT), (0.59375f * IMAGE_WIDTH), new float[]{0.0f, 0.98f, 0.99f, 1.0f}, new Color[]{new Color(1f, 0f, 0f, 1f), new Color(0.2549019608f, 0f, 0f, 1f), new Color(0.2470588235f, 0f, 0f, 1f), new Color(0.2470588235f, 0f, 0f, 1f)}));
            G2.fill(RED_ON);

            final GeneralPath RED_HIGHLIGHT_ON = new GeneralPath();
            RED_HIGHLIGHT_ON.setWindingRule(Path2D.WIND_EVEN_ODD);
            RED_HIGHLIGHT_ON.moveTo(0.2625 * IMAGE_WIDTH, 0.18571428571428572 * IMAGE_HEIGHT);
            RED_HIGHLIGHT_ON.curveTo(0.2625 * IMAGE_WIDTH, 0.17142857142857143 * IMAGE_HEIGHT, 0.375 * IMAGE_WIDTH, 0.12142857142857143 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 0.12142857142857143 * IMAGE_HEIGHT);
            RED_HIGHLIGHT_ON.curveTo(0.625 * IMAGE_WIDTH, 0.12142857142857143 * IMAGE_HEIGHT, 0.725 * IMAGE_WIDTH, 0.16428571428571428 * IMAGE_HEIGHT, 0.725 * IMAGE_WIDTH, 0.18571428571428572 * IMAGE_HEIGHT);
            RED_HIGHLIGHT_ON.curveTo(0.725 * IMAGE_WIDTH, 0.22857142857142856 * IMAGE_HEIGHT, 0.625 * IMAGE_WIDTH, 0.2642857142857143 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 0.2642857142857143 * IMAGE_HEIGHT);
            RED_HIGHLIGHT_ON.curveTo(0.375 * IMAGE_WIDTH, 0.2642857142857143 * IMAGE_HEIGHT, 0.2625 * IMAGE_WIDTH, 0.2357142857142857 * IMAGE_HEIGHT, 0.2625 * IMAGE_WIDTH, 0.18571428571428572 * IMAGE_HEIGHT);
            RED_HIGHLIGHT_ON.closePath();
            G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.15 * IMAGE_HEIGHT), (0.2125f * IMAGE_WIDTH), new float[]{0.0f, 0.98f, 1.0f}, new Color[]{new Color(1f, 1f, 1f, 0.6745098039f), new Color(1f, 1f, 1f, 0.0862745098f), new Color(1f, 1f, 1f, 0.0862745098f)}));
            G2.fill(RED_HIGHLIGHT_ON);
        }
        final Ellipse2D RED_INNER_SHADOW = new Ellipse2D.Double(0.1875 * IMAGE_WIDTH, 0.11428571428571428 * IMAGE_HEIGHT, 0.625 * IMAGE_WIDTH, 0.35714285714285715 * IMAGE_HEIGHT);
        G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.29285714285714287 * IMAGE_HEIGHT), (0.3125f * IMAGE_WIDTH), new float[]{0.0f, 0.55f, 0.5501f, 0.78f, 0.79f, 1.0f}, new Color[]{new Color(0.0039215686f, 0.0039215686f, 0.0039215686f, 0f), new Color(0f, 0f, 0f, 0f), new Color(0f, 0f, 0f, 0f), new Color(0f, 0f, 0f, 0.1215686275f), new Color(0f, 0f, 0f, 0.1294117647f), new Color(0f, 0f, 0f, 0.4980392157f)}));
        G2.fill(RED_INNER_SHADOW);

        G2.dispose();
        return IMAGE;
    }

    public BufferedImage create3LightsHousingImage(final int WIDTH, final int HEIGHT) {
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
        final RoundRectangle2D BACKGROUND = new RoundRectangle2D.Double(0.125 * IMAGE_WIDTH, 0.055 * IMAGE_HEIGHT, 0.75 * IMAGE_WIDTH, 0.9 * IMAGE_HEIGHT, 0.75 * IMAGE_WIDTH, 0.3 * IMAGE_HEIGHT);
        G2.setPaint(new Color(0.8f, 0.8f, 0.8f, 0.5f));
        G2.fill(BACKGROUND);

        final GeneralPath FRAME = new GeneralPath();
        FRAME.setWindingRule(Path2D.WIND_EVEN_ODD);
        FRAME.moveTo(0.125 * IMAGE_WIDTH, 0.205 * IMAGE_HEIGHT);
        FRAME.curveTo(0.125 * IMAGE_WIDTH, 0.12 * IMAGE_HEIGHT, 0.2875 * IMAGE_WIDTH, 0.055 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 0.055 * IMAGE_HEIGHT);
        FRAME.curveTo(0.7125 * IMAGE_WIDTH, 0.055 * IMAGE_HEIGHT, 0.875 * IMAGE_WIDTH, 0.12 * IMAGE_HEIGHT, 0.875 * IMAGE_WIDTH, 0.205 * IMAGE_HEIGHT);
        FRAME.curveTo(0.875 * IMAGE_WIDTH, 0.205 * IMAGE_HEIGHT, 0.875 * IMAGE_WIDTH, 0.805 * IMAGE_HEIGHT, 0.875 * IMAGE_WIDTH, 0.805 * IMAGE_HEIGHT);
        FRAME.curveTo(0.875 * IMAGE_WIDTH, 0.89 * IMAGE_HEIGHT, 0.7125 * IMAGE_WIDTH, 0.955 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 0.955 * IMAGE_HEIGHT);
        FRAME.curveTo(0.2875 * IMAGE_WIDTH, 0.955 * IMAGE_HEIGHT, 0.125 * IMAGE_WIDTH, 0.89 * IMAGE_HEIGHT, 0.125 * IMAGE_WIDTH, 0.805 * IMAGE_HEIGHT);
        FRAME.curveTo(0.125 * IMAGE_WIDTH, 0.805 * IMAGE_HEIGHT, 0.125 * IMAGE_WIDTH, 0.205 * IMAGE_HEIGHT, 0.125 * IMAGE_WIDTH, 0.205 * IMAGE_HEIGHT);
        FRAME.closePath();
        FRAME.moveTo(0.0 * IMAGE_WIDTH, 0.2 * IMAGE_HEIGHT);
        FRAME.curveTo(0.0 * IMAGE_WIDTH, 0.2 * IMAGE_HEIGHT, 0.0 * IMAGE_WIDTH, 0.8 * IMAGE_HEIGHT, 0.0 * IMAGE_WIDTH, 0.8 * IMAGE_HEIGHT);
        FRAME.curveTo(0.0 * IMAGE_WIDTH, 0.91 * IMAGE_HEIGHT, 0.225 * IMAGE_WIDTH, 1.0 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 1.0 * IMAGE_HEIGHT);
        FRAME.curveTo(0.775 * IMAGE_WIDTH, 1.0 * IMAGE_HEIGHT, 1.0 * IMAGE_WIDTH, 0.91 * IMAGE_HEIGHT, 1.0 * IMAGE_WIDTH, 0.8 * IMAGE_HEIGHT);
        FRAME.curveTo(1.0 * IMAGE_WIDTH, 0.8 * IMAGE_HEIGHT, 1.0 * IMAGE_WIDTH, 0.2 * IMAGE_HEIGHT, 1.0 * IMAGE_WIDTH, 0.2 * IMAGE_HEIGHT);
        FRAME.curveTo(1.0 * IMAGE_WIDTH, 0.09 * IMAGE_HEIGHT, 0.775 * IMAGE_WIDTH, 0.0 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 0.0 * IMAGE_HEIGHT);
        FRAME.curveTo(0.225 * IMAGE_WIDTH, 0.0 * IMAGE_HEIGHT, 0.0 * IMAGE_WIDTH, 0.09 * IMAGE_HEIGHT, 0.0 * IMAGE_WIDTH, 0.2 * IMAGE_HEIGHT);
        FRAME.closePath();
        G2.setPaint(new Color(0.2f, 0.2f, 0.2f, 0.5f));
        G2.fill(FRAME);

        G2.dispose();
        return IMAGE;
    }

    public BufferedImage create3LightsGreenImage(final int WIDTH, final int HEIGHT, final boolean IS_ON) {
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

        if (!IS_ON) {
            final Ellipse2D OFF = new Ellipse2D.Double(0.1875 * IMAGE_WIDTH, 0.68 * IMAGE_HEIGHT, 0.625 * IMAGE_WIDTH, 0.25 * IMAGE_HEIGHT);
            G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.885 * IMAGE_HEIGHT), (0.59375f * IMAGE_WIDTH), new float[]{0.0f, 0.98f, 0.99f, 1.0f}, new Color[]{new Color(0.0980392157f, 0.3372549020f, 0f, 1f), new Color(0f, 0.0039215686f, 0f, 1f), new Color(0f, 0f, 0f, 1f), new Color(0f, 0f, 0f, 1f)}));
            G2.fill(OFF);

            final Ellipse2D HIGHLIGHT_OFF = new Ellipse2D.Double(0.2625 * IMAGE_WIDTH, 0.685 * IMAGE_HEIGHT, 0.4625 * IMAGE_WIDTH, 0.1 * IMAGE_HEIGHT);
            G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.705 * IMAGE_HEIGHT), (0.2125f * IMAGE_WIDTH), new float[]{0.0f, 0.98f, 1.0f}, new Color[]{new Color(1f, 1f, 1f, 0.2235294118f), new Color(1f, 1f, 1f, 0.0274509804f), new Color(1f, 1f, 1f, 0.0274509804f)}));
            G2.fill(HIGHLIGHT_OFF);
        } else {
            final Ellipse2D GLOW = new Ellipse2D.Double(0.0 * IMAGE_WIDTH, 0.6 * IMAGE_HEIGHT, 1.0 * IMAGE_WIDTH, 0.4 * IMAGE_HEIGHT);
            G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.8 * IMAGE_HEIGHT), (0.5f * IMAGE_WIDTH), new float[]{0.0f, 0.98f, 0.99f, 1.0f}, new Color[]{new Color(0f, 1f, 0f, 1f), new Color(0f, 1f, 0f, 0f), new Color(0f, 1f, 0f, 0f), new Color(0f, 1f, 0f, 0f)}));
            G2.fill(GLOW);

            final Ellipse2D ON = new Ellipse2D.Double(0.1875 * IMAGE_WIDTH, 0.68 * IMAGE_HEIGHT, 0.625 * IMAGE_WIDTH, 0.25 * IMAGE_HEIGHT);
            G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.885 * IMAGE_HEIGHT), (0.59375f * IMAGE_WIDTH), new float[]{0.0f, 0.98f, 0.99f, 1.0f}, new Color[]{new Color(0f, 1f, 0f, 1f), new Color(0.1254901961f, 0.2784313725f, 0.1411764706f, 1f), new Color(0.1254901961f, 0.2705882353f, 0.1411764706f, 1f), new Color(0.1254901961f, 0.2705882353f, 0.1411764706f, 1f)}));
            G2.fill(ON);

            final Ellipse2D HIGHLIGHT_ON = new Ellipse2D.Double(0.2625 * IMAGE_WIDTH, 0.685 * IMAGE_HEIGHT, 0.4625 * IMAGE_WIDTH, 0.1 * IMAGE_HEIGHT);
            G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.705 * IMAGE_HEIGHT), (0.2125f * IMAGE_WIDTH), new float[]{0.0f, 0.98f, 1.0f}, new Color[]{new Color(1f, 1f, 1f, 0.6745098039f), new Color(1f, 1f, 1f, 0.0862745098f), new Color(1f, 1f, 1f, 0.0862745098f)}));
            G2.fill(HIGHLIGHT_ON);
        }
        final Ellipse2D INNER_SHADOW = new Ellipse2D.Double(0.1875 * IMAGE_WIDTH, 0.68 * IMAGE_HEIGHT, 0.625 * IMAGE_WIDTH, 0.25 * IMAGE_HEIGHT);
        G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.805 * IMAGE_HEIGHT), (0.3125f * IMAGE_WIDTH), new float[]{0.0f, 0.55f, 0.5501f, 0.78f, 0.79f, 1.0f}, new Color[]{new Color(0.0039215686f, 0.0039215686f, 0.0039215686f, 0f), new Color(0f, 0f, 0f, 0f), new Color(0f, 0f, 0f, 0f), new Color(0f, 0f, 0f, 0.1215686275f), new Color(0f, 0f, 0f, 0.1294117647f), new Color(0f, 0f, 0f, 0.4980392157f)}));
        G2.fill(INNER_SHADOW);

        G2.dispose();
        return IMAGE;
    }

    public BufferedImage create3LightsYellowImage(final int WIDTH, final int HEIGHT, final boolean IS_ON) {
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
        if (!IS_ON) {
            final Ellipse2D OFF = new Ellipse2D.Double(0.1875 * IMAGE_WIDTH, 0.38 * IMAGE_HEIGHT, 0.625 * IMAGE_WIDTH, 0.25 * IMAGE_HEIGHT);
            G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.585 * IMAGE_HEIGHT), (0.59375f * IMAGE_WIDTH), new float[]{0.0f, 0.98f, 0.99f, 1.0f}, new Color[]{new Color(0.3254901961f, 0.3333333333f, 0f, 1f), new Color(0.0039215686f, 0.0039215686f, 0f, 1f), new Color(0f, 0f, 0f, 1f), new Color(0f, 0f, 0f, 1f)}));
            G2.fill(OFF);

            final Ellipse2D HIGHLIGHT_OFF = new Ellipse2D.Double(0.2625 * IMAGE_WIDTH, 0.385 * IMAGE_HEIGHT, 0.4625 * IMAGE_WIDTH, 0.1 * IMAGE_HEIGHT);
            G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.405 * IMAGE_HEIGHT), (0.2125f * IMAGE_WIDTH), new float[]{0.0f, 0.98f, 1.0f}, new Color[]{new Color(1f, 1f, 1f, 0.2235294118f), new Color(1f, 1f, 1f, 0.0274509804f), new Color(1f, 1f, 1f, 0.0274509804f)}));
            G2.fill(HIGHLIGHT_OFF);
        } else {
            final Ellipse2D GLOW = new Ellipse2D.Double(0.0 * IMAGE_WIDTH, 0.3 * IMAGE_HEIGHT, 1.0 * IMAGE_WIDTH, 0.4 * IMAGE_HEIGHT);
            G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.5 * IMAGE_HEIGHT), (0.5f * IMAGE_WIDTH), new float[]{0.0f, 0.98f, 0.99f, 1.0f}, new Color[]{new Color(1f, 1f, 0f, 1f), new Color(1f, 1f, 0f, 0f), new Color(1f, 1f, 0f, 0f), new Color(1f, 1f, 0f, 0f)}));
            G2.fill(GLOW);

            final Ellipse2D ON = new Ellipse2D.Double(0.1875 * IMAGE_WIDTH, 0.38 * IMAGE_HEIGHT, 0.625 * IMAGE_WIDTH, 0.25 * IMAGE_HEIGHT);
            G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.585 * IMAGE_HEIGHT), (0.59375f * IMAGE_WIDTH), new float[]{0.0f, 0.98f, 0.99f, 1.0f}, new Color[]{new Color(1f, 1f, 0f, 1f), new Color(0.3333333333f, 0.3411764706f, 0f, 1f), new Color(0.3254901961f, 0.3333333333f, 0f, 1f), new Color(0.3254901961f, 0.3333333333f, 0f, 1f)}));
            G2.fill(ON);

            final Ellipse2D HIGHLIGHT_ON = new Ellipse2D.Double(0.2625 * IMAGE_WIDTH, 0.385 * IMAGE_HEIGHT, 0.4625 * IMAGE_WIDTH, 0.1 * IMAGE_HEIGHT);
            G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.405 * IMAGE_HEIGHT), (0.2125f * IMAGE_WIDTH), new float[]{0.0f, 0.98f, 1.0f}, new Color[]{new Color(1f, 1f, 1f, 0.6745098039f), new Color(1f, 1f, 1f, 0.0862745098f), new Color(1f, 1f, 1f, 0.0862745098f)}));
            G2.fill(HIGHLIGHT_ON);
        }
        final Ellipse2D INNER_SHADOW = new Ellipse2D.Double(0.1875 * IMAGE_WIDTH, 0.38 * IMAGE_HEIGHT, 0.625 * IMAGE_WIDTH, 0.25 * IMAGE_HEIGHT);
        G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.505 * IMAGE_HEIGHT), (0.3125f * IMAGE_WIDTH), new float[]{0.0f, 0.55f, 0.5501f, 0.78f, 0.79f, 1.0f}, new Color[]{new Color(0.0039215686f, 0.0039215686f, 0.0039215686f, 0f), new Color(0f, 0f, 0f, 0f), new Color(0f, 0f, 0f, 0f), new Color(0f, 0f, 0f, 0.1215686275f), new Color(0f, 0f, 0f, 0.1294117647f), new Color(0f, 0f, 0f, 0.4980392157f)}));
        G2.fill(INNER_SHADOW);

        G2.dispose();
        return IMAGE;
    }

    public BufferedImage create3LightsRedImage(final int WIDTH, final int HEIGHT, final boolean IS_ON) {
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

        if (!IS_ON) {
            final Ellipse2D OFF = new Ellipse2D.Double(0.1875 * IMAGE_WIDTH, 0.08 * IMAGE_HEIGHT, 0.625 * IMAGE_WIDTH, 0.25 * IMAGE_HEIGHT);
            G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.285 * IMAGE_HEIGHT), (0.59375f * IMAGE_WIDTH), new float[]{0.0f, 0.98f, 0.99f, 1.0f}, new Color[]{new Color(0.3019607843f, 0f, 0f, 1f), new Color(0.0039215686f, 0f, 0f, 1f), new Color(0f, 0f, 0f, 1f), new Color(0f, 0f, 0f, 1f)}));
            G2.fill(OFF);

            final Ellipse2D HIGHLIGHT_OFF = new Ellipse2D.Double(0.2625 * IMAGE_WIDTH, 0.085 * IMAGE_HEIGHT, 0.4625 * IMAGE_WIDTH, 0.1 * IMAGE_HEIGHT);
            G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.105 * IMAGE_HEIGHT), (0.2125f * IMAGE_WIDTH), new float[]{0.0f, 0.98f, 1.0f}, new Color[]{new Color(1f, 1f, 1f, 0.2235294118f), new Color(1f, 1f, 1f, 0.0274509804f), new Color(1f, 1f, 1f, 0.0274509804f)}));
            G2.fill(HIGHLIGHT_OFF);
        } else {
            final Ellipse2D GLOW = new Ellipse2D.Double(0.0 * IMAGE_WIDTH, 0.0 * IMAGE_HEIGHT, 1.0 * IMAGE_WIDTH, 0.4 * IMAGE_HEIGHT);
            G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.2 * IMAGE_HEIGHT), (0.5f * IMAGE_WIDTH), new float[]{0.0f, 0.98f, 0.99f, 1.0f}, new Color[]{new Color(1f, 0f, 0f, 1f), new Color(1f, 0f, 0f, 0f), new Color(1f, 0f, 0f, 0f), new Color(1f, 0f, 0f, 0f)}));
            G2.fill(GLOW);

            final Ellipse2D ON = new Ellipse2D.Double(0.1875 * IMAGE_WIDTH, 0.08 * IMAGE_HEIGHT, 0.625 * IMAGE_WIDTH, 0.25 * IMAGE_HEIGHT);
            G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.285 * IMAGE_HEIGHT), (0.59375f * IMAGE_WIDTH), new float[]{0.0f, 0.98f, 0.99f, 1.0f}, new Color[]{new Color(1f, 0f, 0f, 1f), new Color(0.2549019608f, 0f, 0f, 1f), new Color(0.2470588235f, 0f, 0f, 1f), new Color(0.2470588235f, 0f, 0f, 1f)}));
            G2.fill(ON);

            final GeneralPath HIGHLIGHT_ON = new GeneralPath();
            HIGHLIGHT_ON.setWindingRule(Path2D.WIND_EVEN_ODD);
            HIGHLIGHT_ON.moveTo(0.2625 * IMAGE_WIDTH, 0.13 * IMAGE_HEIGHT);
            HIGHLIGHT_ON.curveTo(0.2625 * IMAGE_WIDTH, 0.12 * IMAGE_HEIGHT, 0.375 * IMAGE_WIDTH, 0.085 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 0.085 * IMAGE_HEIGHT);
            HIGHLIGHT_ON.curveTo(0.625 * IMAGE_WIDTH, 0.085 * IMAGE_HEIGHT, 0.725 * IMAGE_WIDTH, 0.115 * IMAGE_HEIGHT, 0.725 * IMAGE_WIDTH, 0.13 * IMAGE_HEIGHT);
            HIGHLIGHT_ON.curveTo(0.725 * IMAGE_WIDTH, 0.16 * IMAGE_HEIGHT, 0.625 * IMAGE_WIDTH, 0.185 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 0.185 * IMAGE_HEIGHT);
            HIGHLIGHT_ON.curveTo(0.375 * IMAGE_WIDTH, 0.185 * IMAGE_HEIGHT, 0.2625 * IMAGE_WIDTH, 0.165 * IMAGE_HEIGHT, 0.2625 * IMAGE_WIDTH, 0.13 * IMAGE_HEIGHT);
            HIGHLIGHT_ON.closePath();
            G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.105 * IMAGE_HEIGHT), (0.2125f * IMAGE_WIDTH), new float[]{0.0f, 0.98f, 1.0f}, new Color[]{new Color(1f, 1f, 1f, 0.6745098039f), new Color(1f, 1f, 1f, 0.0862745098f), new Color(1f, 1f, 1f, 0.0862745098f)}));
            G2.fill(HIGHLIGHT_ON);
        }
        final Ellipse2D INNER_SHADOW = new Ellipse2D.Double(0.1875 * IMAGE_WIDTH, 0.08 * IMAGE_HEIGHT, 0.625 * IMAGE_WIDTH, 0.25 * IMAGE_HEIGHT);
        G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.205 * IMAGE_HEIGHT), (0.3125f * IMAGE_WIDTH), new float[]{0.0f, 0.55f, 0.5501f, 0.78f, 0.79f, 1.0f}, new Color[]{new Color(0.0039215686f, 0.0039215686f, 0.0039215686f, 0f), new Color(0f, 0f, 0f, 0f), new Color(0f, 0f, 0f, 0f), new Color(0f, 0f, 0f, 0.1215686275f), new Color(0f, 0f, 0f, 0.1294117647f), new Color(0f, 0f, 0f, 0.4980392157f)}));
        G2.fill(INNER_SHADOW);

        G2.dispose();
        return IMAGE;
    }

    @Override
    public void actionPerformed(final ActionEvent EVENT) {
        blink ^= true;

        redOn = redBlinkEnabled ? blink : redOn;
        yellowOn = yellowBlinkEnabled ? blink : yellowOn;
        greenOn = greenBlinkEnabled ? blink : greenOn;

        repaint(INNER_BOUNDS);
    }

	@Override
	public String toString() {
		return "Trafficlight2";
	}
}
