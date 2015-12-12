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

import eu.hansolo.steelseries.tools.Shadow;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
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
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

/**
 * @author Gerrit Grunwald <han.solo at muenster.de>
 */
public class LightBulb extends JComponent {
    public static final String STATE_PROPERTY = "state";
    private boolean on;
    private float alpha;
    private int direction;
    private PropertyChangeSupport propertySupport;
    private final Rectangle INNER_BOUNDS = new Rectangle(0, 0, 114, 114);
    private final Point2D CENTER;
    private Color glowColor;
    private BufferedImage offImage;
    private BufferedImage onImage;
    private BufferedImage bulbImage;
    // Alignment related
    private int horizontalAlignment;
    private int verticalAlignment;
    private transient final ComponentListener COMPONENT_LISTENER = new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent event) {
            final int SIZE = getWidth() <= getHeight() ? getWidth() : getHeight();
            Container parent = getParent();
            if ((parent != null) && (parent.getLayout() == null)) {
                if (SIZE < getMinimumSize().width || SIZE < getMinimumSize().height) {
                    setSize(getMinimumSize());
                } else{
					setSize(SIZE, SIZE);
				}
            } else {
                if (SIZE < getMinimumSize().width || SIZE < getMinimumSize().height) {
                    setPreferredSize(getMinimumSize());
                } else{
					setPreferredSize(new Dimension(SIZE, SIZE));
				}
            }
            calcInnerBounds();
            init(getInnerBounds().width, getInnerBounds().height);
        }
    };

    public LightBulb() {
        super();
        propertySupport = new PropertyChangeSupport(this);
        CENTER = new Point2D.Double();
        offImage = createImage(INNER_BOUNDS.width, INNER_BOUNDS.height, Transparency.TRANSLUCENT);
        onImage = createImage(INNER_BOUNDS.width, INNER_BOUNDS.height, Transparency.TRANSLUCENT);
        bulbImage = createImage(INNER_BOUNDS.width, INNER_BOUNDS.height, Transparency.TRANSLUCENT);
        alpha = 1.0f;
        direction = SwingUtilities.NORTH;
        glowColor = new Color(1.0f, 1.0f, 0.0f);
        horizontalAlignment = SwingConstants.CENTER;
		verticalAlignment = SwingConstants.CENTER;
        addComponentListener(COMPONENT_LISTENER);
    }

    public final void init(final int WIDTH, final int HEIGHT) {
        if (WIDTH <= 1 || HEIGHT <= 1) {
            return;
        }
        if (offImage != null) {
            offImage.flush();
        }
        offImage = createOffImage(WIDTH, HEIGHT);
        if (onImage != null) {
            onImage.flush();
        }
        onImage = createOnImage(WIDTH, HEIGHT, glowColor);
        if (bulbImage != null) {
            bulbImage.flush();
        }
        bulbImage = createBulbImage(WIDTH, HEIGHT);

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

        // Take direction into account
        switch (direction) {
            case SwingUtilities.SOUTH:
                G2.rotate(Math.PI, CENTER.getX(), CENTER.getY());
                break;
            case SwingUtilities.EAST:
                G2.rotate(-Math.PI / 2, CENTER.getX(), CENTER.getY());
                break;
            case SwingUtilities.WEST:
                G2.rotate(Math.PI / 2, CENTER.getX(), CENTER.getY());
                break;
        }

        // Take insets into account (e.g. used by borders)
        G2.translate(getInnerBounds().x, getInnerBounds().y);

        if (on) {
            G2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f - alpha));
            G2.drawImage(offImage, 0, 0, null);
            G2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            G2.drawImage(onImage, 0, 0, null);
            G2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        } else {
            G2.drawImage(offImage, 0, 0, null);
        }
        G2.drawImage(bulbImage, 0, 0, null);

        // Dispose the temp graphics object
        G2.dispose();
    }

    /**
     * Returns true if the lightbulb is glowing
     * @return true if the lightbulb is glowing
     */
    public boolean isOn() {
        return on;
    }

    /**
     * Enables/disables the glowing of the lightbulb
     * @param ON
     */
    public void setOn(final boolean ON) {
        boolean oldState = on;
        on = ON;
        propertySupport.firePropertyChange(STATE_PROPERTY, oldState, on);
        repaint(getInnerBounds());
    }

    /**
     * Returns the alpha value of the glow effect (0.0f - 1.0f)
     * 0.0f means the glow is completly invisible and the bulb looks
     * like switched off
     * @return the alpha value of the glow effect
     */
    public float getAlpha() {
        return alpha;
    }

    /**
     * Sets the alpha value of the glow effect. A value of 0.0f makes
     * the glow completly invisible and the bulb will look like switched off
     * @param ALPHA
     */
    public void setAlpha(final float ALPHA) {
        alpha = ALPHA < 0 ? 0 : (ALPHA > 1 ? 1: ALPHA);
        repaint(getInnerBounds());
    }

    /**
     * Returns the direction of the lightbulb. The returned int will use
     * the constants from SwingUtilities
     * SwingUtilities.NORTH
     * SwingUtilities.EAST
     * SwingUtiltites.SOUTH
     * SwingUtilities.WEST
     * @return the direction of the lightbulb
     */
    public int getDirection() {
        return direction;
    }

    /**
     * Sets the direction of the lightbulb. Use the constants defined in SwingUtilities
     * SwingUtilities.NORTH
     * SwingUtilities.EAST
     * SwingUtiltites.SOUTH
     * SwingUtilities.WEST
     * @param DIRECTION
     */
    public void setDirection(final int DIRECTION) {
        switch (DIRECTION) {
            case SwingUtilities.SOUTH:
                direction = SwingUtilities.SOUTH;
                break;
            case SwingUtilities.EAST:
                direction = SwingUtilities.EAST;
                break;
            case SwingUtilities.WEST:
                direction = SwingUtilities.WEST;
                break;
            case SwingUtilities.NORTH:
            default:
                direction = SwingUtilities.NORTH;
                break;
        }
        repaint(getInnerBounds());
    }

    /**
     * Returns the glow color of the lightbulb
     * @return the glow color of the lightbulb
     */
    public Color getGlowColor() {
        return glowColor;
    }

    /**
     * Sets the glow color of the lightbulb
     * @param GLOW_COLOR
     */
    public void setGlowColor(final Color GLOW_COLOR) {
        glowColor = GLOW_COLOR;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
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
        INNER_BOUNDS.setBounds(INSETS.left, INSETS.top, getWidth() - INSETS.left - INSETS.right, getHeight() - INSETS.top - INSETS.bottom);
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
        Dimension dim = super.getMinimumSize();
        if (dim.width < 24 || dim.height < 24) {
            dim = new Dimension(24, 24);
        }
        return dim;
    }

    @Override
    public void setMinimumSize(final Dimension DIM) {
        int  width = DIM.width < 24 ? 24 : DIM.width;
        int height = DIM.height < 24 ? 24 : DIM.height;
        final int SIZE = width <= height ? width : height;
        super.setMinimumSize(new Dimension(SIZE, SIZE));
        calcInnerBounds();
        init(INNER_BOUNDS.width, INNER_BOUNDS.height);
        invalidate();
        repaint();
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension dim = super.getMaximumSize();
        if (dim.width > 1080 || dim.height > 1080) {
            dim = new Dimension(1080, 1080);
        }
        return dim;
    }

    @Override
    public void setMaximumSize(final Dimension DIM) {
        int  width = DIM.width > 1080 ? 1080 : DIM.width;
        int height = DIM.height > 1080 ? 1080 : DIM.height;
        final int SIZE = width <= height ? width : height;
        super.setMaximumSize(new Dimension(SIZE, SIZE));
        calcInnerBounds();
        init(INNER_BOUNDS.width, INNER_BOUNDS.height);
        invalidate();
        repaint();
    }

    @Override
    public void setPreferredSize(final Dimension DIM) {
        final int SIZE = DIM.width <= DIM.height ? DIM.width : DIM.height;
        super.setPreferredSize(new Dimension(SIZE, SIZE));
        calcInnerBounds();
        init(INNER_BOUNDS.width, INNER_BOUNDS.height);
        invalidate();
        repaint();
    }

    @Override
    public void setSize(final int WIDTH, final int HEIGHT) {
        final int SIZE = WIDTH <= HEIGHT ? WIDTH : HEIGHT;
        super.setSize(SIZE, SIZE);
        calcInnerBounds();
        init(INNER_BOUNDS.width, INNER_BOUNDS.height);
    }

    @Override
    public void setSize(final Dimension DIM) {
        final int SIZE = DIM.width <= DIM.height ? DIM.width : DIM.height;
        super.setSize(new Dimension(SIZE, SIZE));
        calcInnerBounds();
        init(INNER_BOUNDS.width, INNER_BOUNDS.height);
    }

    @Override
    public void setBounds(final Rectangle BOUNDS) {
        if (BOUNDS.width <= BOUNDS.height) {
            // vertical
            int yNew;
            switch(verticalAlignment) {
                case SwingConstants.TOP:
                    yNew = BOUNDS.y;
                    break;
                case SwingConstants.BOTTOM:
                    yNew = BOUNDS.y + (BOUNDS.height - BOUNDS.width);
                    break;
                case SwingConstants.CENTER:
                default:
                    yNew = BOUNDS.y + ((BOUNDS.height - BOUNDS.width) / 2);
                    break;
            }
            super.setBounds(BOUNDS.x, yNew, BOUNDS.width, BOUNDS.width);
        } else {
            // horizontal
            int xNew;
            switch(horizontalAlignment) {
                case SwingConstants.LEFT:
                    xNew = BOUNDS.x;
                    break;
                case SwingConstants.RIGHT:
                    xNew = BOUNDS.x + (BOUNDS.width - BOUNDS.height);
                    break;
                case SwingConstants.CENTER:
                default:
                    xNew = BOUNDS.x + ((BOUNDS.width - BOUNDS.height) / 2);
                    break;
            }
            super.setBounds(xNew, BOUNDS.y, BOUNDS.height, BOUNDS.height);
        }
        calcInnerBounds();
        init(INNER_BOUNDS.width, INNER_BOUNDS.height);
    }

    @Override
    public void setBounds(final int X, final int Y, final int WIDTH, final int HEIGHT) {
        if (WIDTH <= HEIGHT) {
            // vertical
            int yNew;
            switch(verticalAlignment) {
                case SwingConstants.TOP:
                    yNew = Y;
                    break;
                case SwingConstants.BOTTOM:
                    yNew = Y + (HEIGHT - WIDTH);
                    break;
                case SwingConstants.CENTER:
                default:
                    yNew = Y + ((HEIGHT - WIDTH) / 2);
                    break;
            }
            super.setBounds(X, yNew, WIDTH, WIDTH);
        } else {
            // horizontal
            int xNew;
            switch(horizontalAlignment) {
                case SwingConstants.LEFT:
                    xNew = X;
                    break;
                case SwingConstants.RIGHT:
                    xNew = X + (WIDTH - HEIGHT);
                    break;
                case SwingConstants.CENTER:
                default:
                    xNew = X + ((WIDTH - HEIGHT) / 2);
                    break;
            }
            super.setBounds(xNew, Y, HEIGHT, HEIGHT);
        }
        calcInnerBounds();
        init(INNER_BOUNDS.width, INNER_BOUNDS.height);
    }

    @Override
    public void setBorder(Border BORDER) {
        super.setBorder(BORDER);
        calcInnerBounds();
        init(INNER_BOUNDS.width, INNER_BOUNDS.height);
    }

    /**
     * Returns the alignment of the radial gauge along the X axis.
     * @return the alignment of the radial gauge along the X axis.
     */
    public int getHorizontalAlignment() {
        return horizontalAlignment;
    }

    /**
     * Sets the alignment of the radial gauge along the X axis.
     * @param HORIZONTAL_ALIGNMENT (SwingConstants.CENTER is default)
     */
    public void setHorizontalAlignment(final int HORIZONTAL_ALIGNMENT) {
        horizontalAlignment = HORIZONTAL_ALIGNMENT;
    }

    /**
     * Returns the alignment of the radial gauge along the Y axis.
     * @return the alignment of the radial gauge along the Y axis.
     */
    public int getVerticalAlignment() {
        return verticalAlignment;
    }

    /**
     * Sets the alignment of the radial gauge along the Y axis.
     * @param VERTICAL_ALIGNMENT (SwingConstants.CENTER is default)
     */
    public void setVerticalAlignment(final int VERTICAL_ALIGNMENT) {
        verticalAlignment = VERTICAL_ALIGNMENT;
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
    public BufferedImage createOffImage(final int WIDTH, final int HEIGHT) {
        final GraphicsConfiguration GFX_CONF = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        if (WIDTH <= 0 || HEIGHT <= 0) {
            return GFX_CONF.createCompatibleImage(1, 1, java.awt.Transparency.TRANSLUCENT);
        }
        final BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();
        final GeneralPath GLAS = new GeneralPath();
        GLAS.setWindingRule(Path2D.WIND_EVEN_ODD);
        GLAS.moveTo(0.2894736842105263 * IMAGE_WIDTH, 0.43859649122807015 * IMAGE_HEIGHT);
        GLAS.curveTo(0.2894736842105263 * IMAGE_WIDTH, 0.5614035087719298 * IMAGE_HEIGHT, 0.38596491228070173 * IMAGE_WIDTH, 0.6052631578947368 * IMAGE_HEIGHT, 0.38596491228070173 * IMAGE_WIDTH, 0.7456140350877193 * IMAGE_HEIGHT);
        GLAS.curveTo(0.38596491228070173 * IMAGE_WIDTH, 0.7456140350877193 * IMAGE_HEIGHT, 0.5877192982456141 * IMAGE_WIDTH, 0.7456140350877193 * IMAGE_HEIGHT, 0.5877192982456141 * IMAGE_WIDTH, 0.7456140350877193 * IMAGE_HEIGHT);
        GLAS.curveTo(0.5877192982456141 * IMAGE_WIDTH, 0.6052631578947368 * IMAGE_HEIGHT, 0.6929824561403509 * IMAGE_WIDTH, 0.5614035087719298 * IMAGE_HEIGHT, 0.6929824561403509 * IMAGE_WIDTH, 0.43859649122807015 * IMAGE_HEIGHT);
        GLAS.curveTo(0.6929824561403509 * IMAGE_WIDTH, 0.32456140350877194 * IMAGE_HEIGHT, 0.6052631578947368 * IMAGE_WIDTH, 0.22807017543859648 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 0.22807017543859648 * IMAGE_HEIGHT);
        GLAS.curveTo(0.38596491228070173 * IMAGE_WIDTH, 0.22807017543859648 * IMAGE_HEIGHT, 0.2894736842105263 * IMAGE_WIDTH, 0.32456140350877194 * IMAGE_HEIGHT, 0.2894736842105263 * IMAGE_WIDTH, 0.43859649122807015 * IMAGE_HEIGHT);
        GLAS.closePath();
        final LinearGradientPaint GLAS_PAINT = new LinearGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.2894736842105263 * IMAGE_HEIGHT), new Point2D.Double(0.5 * IMAGE_WIDTH, 0.7017543859649122 * IMAGE_HEIGHT), new float[]{0.0f, 0.99f, 1.0f}, new Color[]{new Color(0.9333333333f, 0.9333333333f, 0.9333333333f, 1f), new Color(0.6f, 0.6f, 0.6f, 1f), new Color(0.6f, 0.6f, 0.6f, 1f)});
        G2.setPaint(GLAS_PAINT);
        G2.fill(GLAS);
        G2.setPaint(new Color(0.8f, 0.8f, 0.8f, 1f));
        G2.setStroke(new BasicStroke((0.010101010101010102f * IMAGE_WIDTH), 0, 1));
        G2.draw(GLAS);
        G2.drawImage(Shadow.INSTANCE.createInnerShadow((Shape) GLAS, GLAS_PAINT, 0, 0.35f, new Color(0, 0, 0, 50), (int) 10.0, 45), GLAS.getBounds().x, GLAS.getBounds().y, null);

        G2.dispose();
        return IMAGE;
    }

    public BufferedImage createOnImage(final int WIDTH, final int HEIGHT, final Color GLOW_COLOR) {
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
        final GeneralPath GLOW = new GeneralPath();
        GLOW.setWindingRule(Path2D.WIND_EVEN_ODD);
        GLOW.moveTo(0.05263157894736842 * IMAGE_WIDTH, 0.43859649122807015 * IMAGE_HEIGHT);
        GLOW.curveTo(0.05263157894736842 * IMAGE_WIDTH, 0.19298245614035087 * IMAGE_HEIGHT, 0.24561403508771928 * IMAGE_WIDTH, 0.0, 0.49122807017543857 * IMAGE_WIDTH, 0.0);
        GLOW.curveTo(0.7368421052631579 * IMAGE_WIDTH, 0.0, 0.9298245614035088 * IMAGE_WIDTH, 0.19298245614035087 * IMAGE_HEIGHT, 0.9298245614035088 * IMAGE_WIDTH, 0.43859649122807015 * IMAGE_HEIGHT);
        GLOW.curveTo(0.9298245614035088 * IMAGE_WIDTH, 0.6842105263157895 * IMAGE_HEIGHT, 0.7368421052631579 * IMAGE_WIDTH, 0.8771929824561403 * IMAGE_HEIGHT, 0.49122807017543857 * IMAGE_WIDTH, 0.8771929824561403 * IMAGE_HEIGHT);
        GLOW.curveTo(0.24561403508771928 * IMAGE_WIDTH, 0.8771929824561403 * IMAGE_HEIGHT, 0.05263157894736842 * IMAGE_WIDTH, 0.6842105263157895 * IMAGE_HEIGHT, 0.05263157894736842 * IMAGE_WIDTH, 0.43859649122807015 * IMAGE_HEIGHT);
        GLOW.closePath();
        final float[] GLOW_FRACTIONS = {
            0.0f,
            0.08f,
            0.09f,
            0.24f,
            0.25f,
            0.51f,
            0.88f,
            1.0f
        };
        final float GLOW_RED = GLOW_COLOR.getRed() * 1f / 255f;
        final float GLOW_GREEN = GLOW_COLOR.getGreen() * 1f / 255f;
        final float GLOW_BLUE = GLOW_COLOR.getBlue() * 1f / 255f;
        final Color[] GLOW_COLORS = {
            new Color(GLOW_RED, GLOW_GREEN, GLOW_BLUE, 1.0f),
            new Color(GLOW_RED, GLOW_GREEN, GLOW_BLUE, 0.95f),
            new Color(GLOW_RED, GLOW_GREEN, GLOW_BLUE, 0.9f),
            new Color(GLOW_RED, GLOW_GREEN, GLOW_BLUE, 0.7f),
            new Color(GLOW_RED, GLOW_GREEN, GLOW_BLUE, 0.55f),
            new Color(GLOW_RED, GLOW_GREEN, GLOW_BLUE, 0.25f),
            new Color(GLOW_RED, GLOW_GREEN, GLOW_BLUE, 0.1f),
            new Color(GLOW_RED, GLOW_GREEN, GLOW_BLUE, 0f)
        };
        G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.4824561403508772 * IMAGE_WIDTH, 0.43859649122807015 * IMAGE_HEIGHT), (0.44298245614035087f * IMAGE_WIDTH), GLOW_FRACTIONS, GLOW_COLORS));
        //G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.4824561403508772 * IMAGE_WIDTH, 0.43859649122807015 * IMAGE_HEIGHT), (0.44298245614035087f * IMAGE_WIDTH), new float[]{0.0f, 1.0f}, new Color[]{new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 255), new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 0)}));
        G2.fill(GLOW);

        final GeneralPath GLAS = new GeneralPath();
        GLAS.setWindingRule(Path2D.WIND_EVEN_ODD);
        GLAS.moveTo(0.2894736842105263 * IMAGE_WIDTH, 0.43859649122807015 * IMAGE_HEIGHT);
        GLAS.curveTo(0.2894736842105263 * IMAGE_WIDTH, 0.5614035087719298 * IMAGE_HEIGHT, 0.38596491228070173 * IMAGE_WIDTH, 0.6052631578947368 * IMAGE_HEIGHT, 0.38596491228070173 * IMAGE_WIDTH, 0.7456140350877193 * IMAGE_HEIGHT);
        GLAS.curveTo(0.38596491228070173 * IMAGE_WIDTH, 0.7456140350877193 * IMAGE_HEIGHT, 0.5877192982456141 * IMAGE_WIDTH, 0.7456140350877193 * IMAGE_HEIGHT, 0.5877192982456141 * IMAGE_WIDTH, 0.7456140350877193 * IMAGE_HEIGHT);
        GLAS.curveTo(0.5877192982456141 * IMAGE_WIDTH, 0.6052631578947368 * IMAGE_HEIGHT, 0.6929824561403509 * IMAGE_WIDTH, 0.5614035087719298 * IMAGE_HEIGHT, 0.6929824561403509 * IMAGE_WIDTH, 0.43859649122807015 * IMAGE_HEIGHT);
        GLAS.curveTo(0.6929824561403509 * IMAGE_WIDTH, 0.32456140350877194 * IMAGE_HEIGHT, 0.6052631578947368 * IMAGE_WIDTH, 0.22807017543859648 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 0.22807017543859648 * IMAGE_HEIGHT);
        GLAS.curveTo(0.38596491228070173 * IMAGE_WIDTH, 0.22807017543859648 * IMAGE_HEIGHT, 0.2894736842105263 * IMAGE_WIDTH, 0.32456140350877194 * IMAGE_HEIGHT, 0.2894736842105263 * IMAGE_WIDTH, 0.43859649122807015 * IMAGE_HEIGHT);
        GLAS.closePath();
        final float[] HSB = Color.RGBtoHSB(GLOW_COLOR.getRed(), GLOW_COLOR.getGreen(), GLOW_COLOR.getBlue(), null);
        final Color[] GLASS_GLOW_COLORS;
        if (glowColor.getRed() == glowColor.getGreen() && glowColor.getGreen() == glowColor.getBlue()) {
            GLASS_GLOW_COLORS = new Color[]{
                new Color(Color.HSBtoRGB(0.0f, 0.0f, 0.6f)),
                new Color(Color.HSBtoRGB(0.0f, 0.0f, 0.4f))
            };
        } else {
            GLASS_GLOW_COLORS = new Color[]{
                new Color(Color.HSBtoRGB(HSB[0], 0.6f, HSB[2])),
                new Color(Color.HSBtoRGB(HSB[0], 0.4f, HSB[2]))
            };
        }
        final LinearGradientPaint GLAS_PAINT = new LinearGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.2894736842105263 * IMAGE_HEIGHT), new Point2D.Double(0.5 * IMAGE_WIDTH, 0.7017543859649122 * IMAGE_HEIGHT), new float[]{0.0f, 1.0f}, GLASS_GLOW_COLORS);
        G2.setPaint(GLAS_PAINT);
        G2.fill(GLAS);
        G2.setPaint(GLOW_COLOR);
        G2.setStroke(new BasicStroke((0.010101010101010102f * IMAGE_WIDTH), 0, 1));
        G2.draw(GLAS);

        G2.dispose();
        return IMAGE;
    }

    public BufferedImage createBulbImage(final int WIDTH, final int HEIGHT) {
        final GraphicsConfiguration GFX_CONF = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        if (WIDTH <= 0 || HEIGHT <= 0) {
            return GFX_CONF.createCompatibleImage(1, 1, java.awt.Transparency.TRANSLUCENT);
        }
        final BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();
        final GeneralPath HIGHLIGHT = new GeneralPath();
        HIGHLIGHT.setWindingRule(Path2D.WIND_EVEN_ODD);
        HIGHLIGHT.moveTo(0.3508771929824561 * IMAGE_WIDTH, 0.3333333333333333 * IMAGE_HEIGHT);
        HIGHLIGHT.curveTo(0.3508771929824561 * IMAGE_WIDTH, 0.2807017543859649 * IMAGE_HEIGHT, 0.41228070175438597 * IMAGE_WIDTH, 0.23684210526315788 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 0.23684210526315788 * IMAGE_HEIGHT);
        HIGHLIGHT.curveTo(0.5789473684210527 * IMAGE_WIDTH, 0.23684210526315788 * IMAGE_HEIGHT, 0.6403508771929824 * IMAGE_WIDTH, 0.2807017543859649 * IMAGE_HEIGHT, 0.6403508771929824 * IMAGE_WIDTH, 0.3333333333333333 * IMAGE_HEIGHT);
        HIGHLIGHT.curveTo(0.6403508771929824 * IMAGE_WIDTH, 0.38596491228070173 * IMAGE_HEIGHT, 0.5789473684210527 * IMAGE_WIDTH, 0.4298245614035088 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 0.4298245614035088 * IMAGE_HEIGHT);
        HIGHLIGHT.curveTo(0.41228070175438597 * IMAGE_WIDTH, 0.4298245614035088 * IMAGE_HEIGHT, 0.3508771929824561 * IMAGE_WIDTH, 0.38596491228070173 * IMAGE_HEIGHT, 0.3508771929824561 * IMAGE_WIDTH, 0.3333333333333333 * IMAGE_HEIGHT);
        HIGHLIGHT.closePath();
        G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.24561403508771928 * IMAGE_HEIGHT), new Point2D.Double(0.5 * IMAGE_WIDTH, 0.4298245614035088 * IMAGE_HEIGHT), new float[]{0.0f, 0.99f, 1.0f}, new Color[]{Color.WHITE, new Color(1f, 1f, 1f, 0f), new Color(1f, 1f, 1f, 0f)}));
        G2.fill(HIGHLIGHT);

        final GeneralPath WINDING = new GeneralPath();
        WINDING.setWindingRule(Path2D.WIND_EVEN_ODD);
        WINDING.moveTo(0.37719298245614036 * IMAGE_WIDTH, 0.7456140350877193 * IMAGE_HEIGHT);
        WINDING.curveTo(0.37719298245614036 * IMAGE_WIDTH, 0.7456140350877193 * IMAGE_HEIGHT, 0.4298245614035088 * IMAGE_WIDTH, 0.7280701754385965 * IMAGE_HEIGHT, 0.49122807017543857 * IMAGE_WIDTH, 0.7280701754385965 * IMAGE_HEIGHT);
        WINDING.curveTo(0.5614035087719298 * IMAGE_WIDTH, 0.7280701754385965 * IMAGE_HEIGHT, 0.6052631578947368 * IMAGE_WIDTH, 0.7368421052631579 * IMAGE_HEIGHT, 0.6052631578947368 * IMAGE_WIDTH, 0.7368421052631579 * IMAGE_HEIGHT);
        WINDING.lineTo(0.6052631578947368 * IMAGE_WIDTH, 0.7631578947368421 * IMAGE_HEIGHT);
        WINDING.lineTo(0.5964912280701754 * IMAGE_WIDTH, 0.7807017543859649 * IMAGE_HEIGHT);
        WINDING.lineTo(0.6052631578947368 * IMAGE_WIDTH, 0.7982456140350878 * IMAGE_HEIGHT);
        WINDING.lineTo(0.5964912280701754 * IMAGE_WIDTH, 0.8157894736842105 * IMAGE_HEIGHT);
        WINDING.lineTo(0.6052631578947368 * IMAGE_WIDTH, 0.8333333333333334 * IMAGE_HEIGHT);
        WINDING.lineTo(0.5964912280701754 * IMAGE_WIDTH, 0.8508771929824561 * IMAGE_HEIGHT);
        WINDING.lineTo(0.6052631578947368 * IMAGE_WIDTH, 0.868421052631579 * IMAGE_HEIGHT);
        WINDING.lineTo(0.5964912280701754 * IMAGE_WIDTH, 0.8859649122807017 * IMAGE_HEIGHT);
        WINDING.lineTo(0.6052631578947368 * IMAGE_WIDTH, 0.8947368421052632 * IMAGE_HEIGHT);
        WINDING.curveTo(0.6052631578947368 * IMAGE_WIDTH, 0.8947368421052632 * IMAGE_HEIGHT, 0.5701754385964912 * IMAGE_WIDTH, 0.956140350877193 * IMAGE_HEIGHT, 0.5350877192982456 * IMAGE_WIDTH, 0.9912280701754386 * IMAGE_HEIGHT);
        WINDING.curveTo(0.5263157894736842 * IMAGE_WIDTH, 0.9912280701754386 * IMAGE_HEIGHT, 0.5175438596491229 * IMAGE_WIDTH, IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, IMAGE_HEIGHT);
        WINDING.curveTo(0.4824561403508772 * IMAGE_WIDTH, IMAGE_HEIGHT, 0.47368421052631576 * IMAGE_WIDTH, IMAGE_HEIGHT, 0.4649122807017544 * IMAGE_WIDTH, 0.9912280701754386 * IMAGE_HEIGHT);
        WINDING.curveTo(0.42105263157894735 * IMAGE_WIDTH, 0.9473684210526315 * IMAGE_HEIGHT, 0.39473684210526316 * IMAGE_WIDTH, 0.9035087719298246 * IMAGE_HEIGHT, 0.39473684210526316 * IMAGE_WIDTH, 0.9035087719298246 * IMAGE_HEIGHT);
        WINDING.lineTo(0.39473684210526316 * IMAGE_WIDTH, 0.8947368421052632 * IMAGE_HEIGHT);
        WINDING.lineTo(0.38596491228070173 * IMAGE_WIDTH, 0.8859649122807017 * IMAGE_HEIGHT);
        WINDING.lineTo(0.39473684210526316 * IMAGE_WIDTH, 0.868421052631579 * IMAGE_HEIGHT);
        WINDING.lineTo(0.38596491228070173 * IMAGE_WIDTH, 0.8508771929824561 * IMAGE_HEIGHT);
        WINDING.lineTo(0.39473684210526316 * IMAGE_WIDTH, 0.8333333333333334 * IMAGE_HEIGHT);
        WINDING.lineTo(0.38596491228070173 * IMAGE_WIDTH, 0.8157894736842105 * IMAGE_HEIGHT);
        WINDING.lineTo(0.39473684210526316 * IMAGE_WIDTH, 0.7982456140350878 * IMAGE_HEIGHT);
        WINDING.lineTo(0.37719298245614036 * IMAGE_WIDTH, 0.7894736842105263 * IMAGE_HEIGHT);
        WINDING.lineTo(0.39473684210526316 * IMAGE_WIDTH, 0.7719298245614035 * IMAGE_HEIGHT);
        WINDING.lineTo(0.37719298245614036 * IMAGE_WIDTH, 0.7631578947368421 * IMAGE_HEIGHT);
        WINDING.lineTo(0.37719298245614036 * IMAGE_WIDTH, 0.7456140350877193 * IMAGE_HEIGHT);
        WINDING.closePath();
        G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.47368421052631576 * IMAGE_WIDTH, 0.7280701754385965 * IMAGE_HEIGHT), new Point2D.Double(0.4847023065774619 * IMAGE_WIDTH, 0.9383079722290332 * IMAGE_HEIGHT), new float[]{0.0f, 0.04f, 0.19f, 0.24f, 0.31f, 0.4f, 0.48f, 0.56f, 0.64f, 0.7f, 0.78f, 1.0f}, new Color[]{new Color(0.2f, 0.2f, 0.2f, 1f), new Color(0.8509803922f, 0.8470588235f, 0.8392156863f, 1f), new Color(0.8941176471f, 0.8980392157f, 0.8784313725f, 1f), new Color(0.5921568627f, 0.6f, 0.5882352941f, 1f), new Color(0.9843137255f, 1f, 1f, 1f), new Color(0.5058823529f, 0.5215686275f, 0.5176470588f, 1f), new Color(0.9607843137f, 0.9686274510f, 0.9568627451f, 1f), new Color(0.5843137255f, 0.5921568627f, 0.5803921569f, 1f), new Color(0.9490196078f, 0.9490196078f, 0.9411764706f, 1f), new Color(0.5098039216f, 0.5294117647f, 0.5137254902f, 1f), new Color(0.9882352941f, 0.9882352941f, 0.9882352941f, 1f), new Color(0.4f, 0.4f, 0.4f, 1f)}));
        G2.fill(WINDING);

        final GeneralPath WINDING_SHADOW = new GeneralPath();
        WINDING_SHADOW.setWindingRule(Path2D.WIND_EVEN_ODD);
        WINDING_SHADOW.moveTo(0.37719298245614036 * IMAGE_WIDTH, 0.7456140350877193 * IMAGE_HEIGHT);
        WINDING_SHADOW.curveTo(0.37719298245614036 * IMAGE_WIDTH, 0.7456140350877193 * IMAGE_HEIGHT, 0.4298245614035088 * IMAGE_WIDTH, 0.7280701754385965 * IMAGE_HEIGHT, 0.49122807017543857 * IMAGE_WIDTH, 0.7280701754385965 * IMAGE_HEIGHT);
        WINDING_SHADOW.curveTo(0.5614035087719298 * IMAGE_WIDTH, 0.7280701754385965 * IMAGE_HEIGHT, 0.6052631578947368 * IMAGE_WIDTH, 0.7368421052631579 * IMAGE_HEIGHT, 0.6052631578947368 * IMAGE_WIDTH, 0.7368421052631579 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.6052631578947368 * IMAGE_WIDTH, 0.7631578947368421 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.5964912280701754 * IMAGE_WIDTH, 0.7807017543859649 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.6052631578947368 * IMAGE_WIDTH, 0.7982456140350878 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.5964912280701754 * IMAGE_WIDTH, 0.8157894736842105 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.6052631578947368 * IMAGE_WIDTH, 0.8333333333333334 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.5964912280701754 * IMAGE_WIDTH, 0.8508771929824561 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.6052631578947368 * IMAGE_WIDTH, 0.868421052631579 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.5964912280701754 * IMAGE_WIDTH, 0.8859649122807017 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.6052631578947368 * IMAGE_WIDTH, 0.8947368421052632 * IMAGE_HEIGHT);
        WINDING_SHADOW.curveTo(0.6052631578947368 * IMAGE_WIDTH, 0.8947368421052632 * IMAGE_HEIGHT, 0.5701754385964912 * IMAGE_WIDTH, 0.956140350877193 * IMAGE_HEIGHT, 0.5350877192982456 * IMAGE_WIDTH, 0.9912280701754386 * IMAGE_HEIGHT);
        WINDING_SHADOW.curveTo(0.5263157894736842 * IMAGE_WIDTH, 0.9912280701754386 * IMAGE_HEIGHT, 0.5175438596491229 * IMAGE_WIDTH, IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, IMAGE_HEIGHT);
        WINDING_SHADOW.curveTo(0.4824561403508772 * IMAGE_WIDTH, IMAGE_HEIGHT, 0.47368421052631576 * IMAGE_WIDTH, IMAGE_HEIGHT, 0.4649122807017544 * IMAGE_WIDTH, 0.9912280701754386 * IMAGE_HEIGHT);
        WINDING_SHADOW.curveTo(0.42105263157894735 * IMAGE_WIDTH, 0.9473684210526315 * IMAGE_HEIGHT, 0.39473684210526316 * IMAGE_WIDTH, 0.9035087719298246 * IMAGE_HEIGHT, 0.39473684210526316 * IMAGE_WIDTH, 0.9035087719298246 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.39473684210526316 * IMAGE_WIDTH, 0.8947368421052632 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.38596491228070173 * IMAGE_WIDTH, 0.8859649122807017 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.39473684210526316 * IMAGE_WIDTH, 0.868421052631579 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.38596491228070173 * IMAGE_WIDTH, 0.8508771929824561 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.39473684210526316 * IMAGE_WIDTH, 0.8333333333333334 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.38596491228070173 * IMAGE_WIDTH, 0.8157894736842105 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.39473684210526316 * IMAGE_WIDTH, 0.7982456140350878 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.37719298245614036 * IMAGE_WIDTH, 0.7894736842105263 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.39473684210526316 * IMAGE_WIDTH, 0.7719298245614035 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.37719298245614036 * IMAGE_WIDTH, 0.7631578947368421 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.37719298245614036 * IMAGE_WIDTH, 0.7456140350877193 * IMAGE_HEIGHT);
        WINDING_SHADOW.closePath();
        G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.37719298245614036 * IMAGE_WIDTH, 0.7894736842105263 * IMAGE_HEIGHT), new Point2D.Double(0.6052631578947368 * IMAGE_WIDTH, 0.7894736842105263 * IMAGE_HEIGHT), new float[]{0.0f, 0.15f, 0.85f, 1.0f}, new Color[]{new Color(0f, 0f, 0f, 0.4f), new Color(0f, 0f, 0f, 0.0f), new Color(0f, 0f, 0f, 0.0f), new Color(0f, 0f, 0f, 0.4f)}));
        G2.fill(WINDING_SHADOW);

        final GeneralPath CONTACT_PLATE = new GeneralPath();
        CONTACT_PLATE.setWindingRule(Path2D.WIND_EVEN_ODD);
        CONTACT_PLATE.moveTo(0.42105263157894735 * IMAGE_WIDTH, 0.9473684210526315 * IMAGE_HEIGHT);
        CONTACT_PLATE.curveTo(0.43859649122807015 * IMAGE_WIDTH, 0.956140350877193 * IMAGE_HEIGHT, 0.4473684210526316 * IMAGE_WIDTH, 0.9736842105263158 * IMAGE_HEIGHT, 0.4649122807017544 * IMAGE_WIDTH, 0.9912280701754386 * IMAGE_HEIGHT);
        CONTACT_PLATE.curveTo(0.47368421052631576 * IMAGE_WIDTH, IMAGE_HEIGHT, 0.4824561403508772 * IMAGE_WIDTH, IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, IMAGE_HEIGHT);
        CONTACT_PLATE.curveTo(0.5175438596491229 * IMAGE_WIDTH, IMAGE_HEIGHT, 0.5263157894736842 * IMAGE_WIDTH, 0.9912280701754386 * IMAGE_HEIGHT, 0.5350877192982456 * IMAGE_WIDTH, 0.9912280701754386 * IMAGE_HEIGHT);
        CONTACT_PLATE.curveTo(0.543859649122807 * IMAGE_WIDTH, 0.9824561403508771 * IMAGE_HEIGHT, 0.5614035087719298 * IMAGE_WIDTH, 0.956140350877193 * IMAGE_HEIGHT, 0.5789473684210527 * IMAGE_WIDTH, 0.9473684210526315 * IMAGE_HEIGHT);
        CONTACT_PLATE.curveTo(0.5526315789473685 * IMAGE_WIDTH, 0.9385964912280702 * IMAGE_HEIGHT, 0.5263157894736842 * IMAGE_WIDTH, 0.9385964912280702 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 0.9385964912280702 * IMAGE_HEIGHT);
        CONTACT_PLATE.curveTo(0.47368421052631576 * IMAGE_WIDTH, 0.9385964912280702 * IMAGE_HEIGHT, 0.4473684210526316 * IMAGE_WIDTH, 0.9385964912280702 * IMAGE_HEIGHT, 0.42105263157894735 * IMAGE_WIDTH, 0.9473684210526315 * IMAGE_HEIGHT);
        CONTACT_PLATE.closePath();
        G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.9385964912280702 * IMAGE_HEIGHT), new Point2D.Double(0.5 * IMAGE_WIDTH, IMAGE_HEIGHT), new float[]{0.0f, 0.61f, 0.71f, 0.83f, 1.0f}, new Color[]{new Color(0.0196078431f, 0.0392156863f, 0.0235294118f, 1f), new Color(0.0274509804f, 0.0235294118f, 0.0078431373f, 1f), new Color(0.6f, 0.5725490196f, 0.5333333333f, 1f), new Color(0.0039215686f, 0.0039215686f, 0.0039215686f, 1f), new Color(0f, 0f, 0f, 1f)}));
        G2.fill(CONTACT_PLATE);

        G2.dispose();
        return IMAGE;
    }

	@Override
	public String toString() {
		return "LightBulb";
	}
}

