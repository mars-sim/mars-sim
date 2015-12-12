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

import eu.hansolo.steelseries.gauges.AbstractGauge;
import eu.hansolo.steelseries.gauges.AbstractRadial;
import eu.hansolo.steelseries.tools.BackgroundColor;
import eu.hansolo.steelseries.tools.ColorDef;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.Border;


/**
 * @author Gerrit Grunwald <han.solo at muenster.de>
 */
public class StopWatch extends AbstractRadial implements ActionListener {
    // <editor-fold defaultstate="collapsed" desc="Variable declaration">
    private static final double ANGLE_STEP = 6;
    private final Timer CLOCK_TIMER;
    private double minutePointerAngle = 0;
    private double secondPointerAngle = 0;
    private final Rectangle INNER_BOUNDS;
    // Background
    private final Point2D MAIN_CENTER = new Point2D.Double();
    private final Point2D SMALL_CENTER = new Point2D.Double();
    // Images used to combine layers for background and foreground
    private BufferedImage bImage;
    private BufferedImage fImage;
    private BufferedImage smallTickmarkImage;
    private BufferedImage mainPointerImage;
    private BufferedImage mainPointerShadowImage;
    private BufferedImage smallPointerImage;
    private BufferedImage smallPointerShadowImage;
    private BufferedImage disabledImage;
    private long start = 0;
    private long currentMilliSeconds = 0;
    private long minutes = 0;
    private long seconds = 0;
    private long milliSeconds = 0;
    private boolean running = false;
    private boolean flatNeedle = false;
    private final Color SHADOW_COLOR = new Color(0.0f, 0.0f, 0.0f, 0.65f);
    // Alignment related
    private int horizontalAlignment;
    private int verticalAlignment;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public StopWatch() {
        super();
        CLOCK_TIMER = new Timer(100, this);
        INNER_BOUNDS = new Rectangle(200, 200);
        init(INNER_BOUNDS.width, INNER_BOUNDS.height);
        setPointerColor(ColorDef.BLACK);
        setBackgroundColor(BackgroundColor.LIGHT_GRAY);
        horizontalAlignment = SwingConstants.CENTER;
		verticalAlignment = SwingConstants.CENTER;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Initialization">
    @Override
    public AbstractGauge init(final int WIDTH, final int HEIGHT) {
        if (WIDTH <= 1 || HEIGHT <= 1) {
            return this;
        }

        if (!isFrameVisible()) {
            setFramelessOffset(-getInnerBounds().width * 0.0841121495, -getInnerBounds().width * 0.0841121495);
        } else {
            setFramelessOffset(getInnerBounds().x, getInnerBounds().y);
        }

        // Create Background Image
        if (bImage != null) {
            bImage.flush();
        }
        bImage = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);

        // Create Foreground Image
        if (fImage != null) {
            fImage.flush();
        }
        fImage = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);

        if (isFrameVisible()) {
            switch (getFrameType()) {
                case SQUARE:
                    FRAME_FACTORY.createLinearFrame(WIDTH, WIDTH, getFrameDesign(), getCustomFrameDesign(), getFrameEffect(), bImage);
                    break;
                case ROUND:

                default:
                    FRAME_FACTORY.createRadialFrame(WIDTH, getFrameDesign(), getCustomFrameDesign(), getFrameEffect(), bImage);
                    break;
            }
        }

        if (isBackgroundVisible()) {
            switch (getFrameType()) {
                case SQUARE:
                    BACKGROUND_FACTORY.createLinearBackground(WIDTH, WIDTH, getBackgroundColor(), getCustomBackground(), getModel().getTextureColor(), bImage);
                    break;
                case ROUND:

                default:
                    BACKGROUND_FACTORY.createRadialBackground(WIDTH, getBackgroundColor(), getCustomBackground(), getModel().getTextureColor(), bImage);
                    break;
            }
        }

        create_TICKMARKS_Image(WIDTH, 60f, 0.075, 0.14, bImage);

        if (smallTickmarkImage != null) {
            smallTickmarkImage.flush();
        }
        smallTickmarkImage = create_TICKMARKS_Image((int) (0.285 * WIDTH), 30f, 0.095, 0.17, null);

        if (mainPointerImage != null) {
            mainPointerImage.flush();
        }
        mainPointerImage = create_MAIN_POINTER_Image(WIDTH);

        if (mainPointerShadowImage != null) {
            mainPointerShadowImage.flush();
        }
        mainPointerShadowImage = create_MAIN_POINTER_SHADOW_Image(WIDTH);

        if (smallPointerImage != null) {
            smallPointerImage.flush();
        }
        smallPointerImage = create_SMALL_POINTER_Image(WIDTH);

        if (smallPointerShadowImage != null) {
            smallPointerShadowImage.flush();
        }
        smallPointerShadowImage = create_SMALL_POINTER_SHADOW_Image(WIDTH);

        if (isForegroundVisible()) {
            switch (getFrameType()) {
                case SQUARE:
                    FOREGROUND_FACTORY.createLinearForeground(WIDTH, WIDTH, false, bImage);
                    break;

                case ROUND:

                default:
                    FOREGROUND_FACTORY.createRadialForeground(WIDTH, false, getForegroundType(), fImage);
                    break;
            }
        }

        if (disabledImage != null) {
            disabledImage.flush();
        }
        disabledImage = DISABLED_FACTORY.createRadialDisabled(WIDTH);

        return this;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Visualization">
    @Override
    protected void paintComponent(Graphics g) {
        final Graphics2D G2 = (Graphics2D) g.create();

        MAIN_CENTER.setLocation(INNER_BOUNDS.getCenterX(), INNER_BOUNDS.getCenterX());
        SMALL_CENTER.setLocation(INNER_BOUNDS.getCenterX(), INNER_BOUNDS.width * 0.3130841121);

        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Translate the coordinate system related to the insets
        G2.translate(getFramelessOffset().getX(), getFramelessOffset().getY());

        final AffineTransform OLD_TRANSFORM = G2.getTransform();

        // Draw combined background image
        G2.drawImage(bImage, 0, 0, null);

        G2.drawImage(smallTickmarkImage, ((INNER_BOUNDS.width - smallTickmarkImage.getWidth()) / 2), (int) (SMALL_CENTER.getY() - smallTickmarkImage.getHeight() / 2.0), null);

        // Draw the small pointer
        G2.rotate(Math.toRadians(minutePointerAngle + (2 * Math.sin(Math.toRadians(minutePointerAngle)))), SMALL_CENTER.getX(), SMALL_CENTER.getY());
        G2.drawImage(smallPointerShadowImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);
        G2.rotate(Math.toRadians(minutePointerAngle), SMALL_CENTER.getX(), SMALL_CENTER.getY());
        G2.drawImage(smallPointerImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);

        // Draw the main pointer
        G2.rotate(Math.toRadians(secondPointerAngle + (2 * Math.sin(Math.toRadians(secondPointerAngle)))), MAIN_CENTER.getX(), MAIN_CENTER.getY());
        G2.drawImage(mainPointerShadowImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);
        G2.rotate(Math.toRadians(secondPointerAngle), MAIN_CENTER.getX(), MAIN_CENTER.getY());
        G2.drawImage(mainPointerImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);

        // Draw combined foreground image
        G2.drawImage(fImage, 0, 0, null);

        if (!isEnabled()) {
            G2.drawImage(disabledImage, 0, 0, null);
        }

        // Translate the coordinate system back to original
        G2.translate(-getInnerBounds().x, -getInnerBounds().y);

        G2.dispose();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    /**
     * Returns true if the stopwatch is running
     * @return true if the stopwatch is running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Start or stop the stopwatch
     * @param RUNNING
     */
    public void setRunning(final boolean RUNNING) {
        running = RUNNING;
        if (RUNNING) {
            if (!CLOCK_TIMER.isRunning()) {
                CLOCK_TIMER.start();
                start = System.currentTimeMillis();
                repaint(INNER_BOUNDS);
            }
        } else {
            if (CLOCK_TIMER.isRunning()) {
                CLOCK_TIMER.stop();
            }
        }
    }

    /**
     * Starts the stopwatch
     */
    public void start() {
        setRunning(true);
    }

    /**
     * Stops the stopwatch
     */
    public void stop() {
        setRunning(false);
    }

    /**
     * Resets the stopwatch
     */
    public void reset() {
        setRunning(false);
        start = 0;
        repaint(INNER_BOUNDS);
    }

    /**
     * Returns a string that contains MIN:SEC:MILLISEC of the measured time
     * @return a string that contains MIN:SEC:MILLISEC of the measured time
     */
    public String getMeasuredTime() {
        return (minutes + ":" + seconds + ":" + milliSeconds);
    }

    public boolean isFlatNeedle() {
        return flatNeedle;
    }

    public void setFlatNeedle(final boolean FLAT_NEEDLE) {
        flatNeedle = FLAT_NEEDLE;
        init(getWidth(), getWidth());
        repaint(INNER_BOUNDS);
    }

    @Override
    public Point2D getCenter() {
        return new Point2D.Double(bImage.getWidth() / 2.0 + getInnerBounds().x, bImage.getHeight() / 2.0 + getInnerBounds().y);
    }

    @Override
    public Rectangle2D getBounds2D() {
        return new Rectangle2D.Double(bImage.getMinX(), bImage.getMinY(), bImage.getWidth(), bImage.getHeight());
    }

    @Override
    public Rectangle getLcdBounds() {
        return new Rectangle();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Image related">
    private BufferedImage create_TICKMARKS_Image(final int WIDTH, final float RANGE,
                                                                final double TEXT_SCALE,
                                                                final double TEXT_DISTANCE_FACTOR,
                                                                BufferedImage image) {
        if (WIDTH <= 0) {
            return null;
        }
        if (image == null) {
            image = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        }

        final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);

        final Graphics2D G2 = image.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        G2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = image.getWidth();
        final int IMAGE_HEIGHT = image.getHeight();

        final Font STD_FONT = new Font("Verdana", 0, (int) (TEXT_SCALE * WIDTH));
        final BasicStroke THIN_STROKE = new BasicStroke(0.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
        final BasicStroke MEDIUM_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
        final BasicStroke THICK_STROKE = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
        final int TEXT_DISTANCE = (int) (TEXT_DISTANCE_FACTOR * WIDTH);
        final int MIN_LENGTH = (int) (0.025 * WIDTH);
        final int MED_LENGTH = (int) (0.035 * WIDTH);
        final int MAX_LENGTH = (int) (0.045 * WIDTH);
        final Color TEXT_COLOR = getBackgroundColor().LABEL_COLOR;
        final Color TICK_COLOR = getBackgroundColor().LABEL_COLOR;

        // Create the ticks itself
        final float RADIUS = IMAGE_WIDTH * 0.4f;
        final Point2D IMAGE_CENTER = new Point2D.Double(IMAGE_WIDTH / 2.0f, IMAGE_HEIGHT / 2.0f);

        // Draw ticks
        Point2D innerPoint;
        Point2D outerPoint;
        Point2D textPoint = null;
        Line2D tick;
        int counter = 0;
        int numberCounter = 0;
        int tickCounter = 0;

        G2.setFont(STD_FONT);

        double sinValue = 0;
        double cosValue = 0;

        double alpha; // angle for the tickmarks
        final double ALPHA_START = -Math.PI;
        float valueCounter; // value for the tickmarks

        final double ANGLE_STEPSIZE = (2 * Math.PI) / (RANGE);

        for (alpha = ALPHA_START, valueCounter = 0; Float.compare(valueCounter, RANGE + 1) <= 0; alpha -= ANGLE_STEPSIZE * 0.1, valueCounter += 0.1f) {
            G2.setStroke(THIN_STROKE);
            sinValue = Math.sin(alpha);
            cosValue = Math.cos(alpha);

            // tickmark every 2 units
            if (counter % 2 == 0) {
                G2.setStroke(THIN_STROKE);
                innerPoint = new Point2D.Double(IMAGE_CENTER.getX() + (RADIUS - MIN_LENGTH) * sinValue, IMAGE_CENTER.getY() + (RADIUS - MIN_LENGTH) * cosValue);
                outerPoint = new Point2D.Double(IMAGE_CENTER.getX() + RADIUS * sinValue, IMAGE_CENTER.getY() + RADIUS * cosValue);
                // Draw ticks
                G2.setColor(TICK_COLOR);
                tick = new Line2D.Double(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                G2.draw(tick);
            }

            // Different tickmark every 10 units
            if (counter == 10 || counter == 0) {
                G2.setColor(TEXT_COLOR);
                G2.setStroke(MEDIUM_STROKE);
                outerPoint = new Point2D.Double(IMAGE_CENTER.getX() + RADIUS * sinValue, IMAGE_CENTER.getY() + RADIUS * cosValue);
                textPoint = new Point2D.Double(IMAGE_CENTER.getX() + (RADIUS - TEXT_DISTANCE + STD_FONT.getSize() / 2f) * sinValue, IMAGE_CENTER.getY() + (RADIUS - TEXT_DISTANCE + STD_FONT.getSize() / 2f) * cosValue + TEXT_DISTANCE / 2.5f);

                // Draw text
                if (numberCounter == 5) {
                    final TextLayout TEXT_LAYOUT = new TextLayout(String.valueOf(Math.round(valueCounter)), G2.getFont(), RENDER_CONTEXT);
                    final Rectangle2D TEXT_BOUNDARY = TEXT_LAYOUT.getBounds();

                    if (Float.compare(valueCounter, RANGE) != 0) {
                        if (Math.ceil(valueCounter) != 60) {
                            G2.drawString(String.valueOf(Math.round(valueCounter)), (int) (textPoint.getX() - TEXT_BOUNDARY.getWidth() / 2.0), (int) ((textPoint.getY() - TEXT_BOUNDARY.getHeight() / 2.0)));
                        }
                    }
                    G2.setStroke(THICK_STROKE);
                    innerPoint = new Point2D.Double(IMAGE_CENTER.getX() + (RADIUS - MAX_LENGTH) * sinValue, IMAGE_CENTER.getY() + (RADIUS - MAX_LENGTH) * cosValue);
                    numberCounter = 0;
                } else {
                    G2.setStroke(MEDIUM_STROKE);
                    innerPoint = new Point2D.Double(IMAGE_CENTER.getX() + (RADIUS - MED_LENGTH) * sinValue, IMAGE_CENTER.getY() + (RADIUS - MED_LENGTH) * cosValue);
                }

                // Draw ticks
                G2.setColor(TICK_COLOR);
                tick = new Line2D.Double(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                G2.draw(tick);

                counter = 0;
                tickCounter++;
                numberCounter++;
            }

            counter++;
        }

        G2.dispose();

        return image;
    }

    private BufferedImage create_MAIN_POINTER_Image(final int WIDTH) {
        if (WIDTH <= 0) {
            return null;
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        //G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final GeneralPath STOPWATCHPOINTER = new GeneralPath();
        STOPWATCHPOINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
        STOPWATCHPOINTER.moveTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.45794392523364486);
        STOPWATCHPOINTER.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.102803738317757);
        STOPWATCHPOINTER.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.45794392523364486);
        STOPWATCHPOINTER.curveTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.45794392523364486);
        STOPWATCHPOINTER.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.46261682242990654, IMAGE_WIDTH * 0.45794392523364486, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.45794392523364486, IMAGE_HEIGHT * 0.5);
        STOPWATCHPOINTER.curveTo(IMAGE_WIDTH * 0.45794392523364486, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.5373831775700935, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5420560747663551);
        STOPWATCHPOINTER.curveTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5420560747663551, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5420560747663551, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5420560747663551);
        STOPWATCHPOINTER.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.6214953271028038);
        STOPWATCHPOINTER.lineTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.6214953271028038);
        STOPWATCHPOINTER.lineTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.5420560747663551);
        STOPWATCHPOINTER.curveTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.5420560747663551, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.5420560747663551, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.5420560747663551);
        STOPWATCHPOINTER.curveTo(IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.5373831775700935, IMAGE_WIDTH * 0.5420560747663551, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.5420560747663551, IMAGE_HEIGHT * 0.5);
        STOPWATCHPOINTER.curveTo(IMAGE_WIDTH * 0.5420560747663551, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.46261682242990654, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.45794392523364486);
        STOPWATCHPOINTER.curveTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.45794392523364486);
        STOPWATCHPOINTER.closePath();
        if (flatNeedle) {
            G2.setColor(getPointerColor().MEDIUM);
            G2.fill(STOPWATCHPOINTER);
        } else {
            final Point2D POINTER_START = new Point2D.Double(STOPWATCHPOINTER.getBounds2D().getMinX(), 0);
            final Point2D POINTER_STOP = new Point2D.Double(STOPWATCHPOINTER.getBounds2D().getMaxX(), 0);
            final float[] POINTER_FRACTIONS = {
                0.0f,
                0.3888888889f,
                0.5f,
                0.6111111111f,
                1.0f
            };
            final Color[] POINTER_COLORS = {
                getPointerColor().MEDIUM,
                getPointerColor().MEDIUM,
                getPointerColor().LIGHT,
                getPointerColor().MEDIUM,
                getPointerColor().MEDIUM
            };
            final LinearGradientPaint GRADIENT = new LinearGradientPaint(POINTER_START, POINTER_STOP, POINTER_FRACTIONS, POINTER_COLORS);
            G2.setPaint(GRADIENT);
            G2.fill(STOPWATCHPOINTER);
            G2.setPaint(getPointerColor().DARK);
            G2.draw(STOPWATCHPOINTER);
        }

        final Ellipse2D SWBRASSRING = new Ellipse2D.Double(IMAGE_WIDTH * 0.4672897160053253, IMAGE_HEIGHT * 0.4672897160053253, IMAGE_WIDTH * 0.06542053818702698, IMAGE_HEIGHT * 0.06542053818702698);
        final Point2D SWBRASSRING_START = new Point2D.Double(0, SWBRASSRING.getBounds2D().getMaxY());
        final Point2D SWBRASSRING_STOP = new Point2D.Double(0, SWBRASSRING.getBounds2D().getMinY());
        final float[] SWBRASSRING_FRACTIONS = {
            0.0f,
            0.01f,
            0.99f,
            1.0f
        };
        final Color[] SWBRASSRING_COLORS = {
            new Color(230, 179, 92, 255),
            new Color(230, 179, 92, 255),
            new Color(196, 130, 0, 255),
            new Color(196, 130, 0, 255)
        };
        final LinearGradientPaint SWBRASSRING_GRADIENT = new LinearGradientPaint(SWBRASSRING_START, SWBRASSRING_STOP, SWBRASSRING_FRACTIONS, SWBRASSRING_COLORS);
        G2.setPaint(SWBRASSRING_GRADIENT);
        G2.fill(SWBRASSRING);

        final Ellipse2D SWRING1 = new Ellipse2D.Double(IMAGE_WIDTH * 0.47663551568984985, IMAGE_HEIGHT * 0.47663551568984985, IMAGE_WIDTH * 0.04672896862030029, IMAGE_HEIGHT * 0.04672896862030029);
        final Point2D SWRING1_CENTER = new Point2D.Double((0.5 * IMAGE_WIDTH), (0.5 * IMAGE_HEIGHT));
        final float[] SWRING1_FRACTIONS = {
            0.0f,
            0.19f,
            0.22f,
            0.8f,
            0.99f,
            1.0f
        };
        final Color[] SWRING1_COLORS = {
            new Color(197, 197, 197, 255),
            new Color(197, 197, 197, 255),
            new Color(0, 0, 0, 255),
            new Color(0, 0, 0, 255),
            new Color(112, 112, 112, 255),
            new Color(112, 112, 112, 255)
        };
        final RadialGradientPaint SWRING1_GRADIENT = new RadialGradientPaint(SWRING1_CENTER, (float) (0.02336448598130841 * IMAGE_WIDTH), SWRING1_FRACTIONS, SWRING1_COLORS);
        G2.setPaint(SWRING1_GRADIENT);
        G2.fill(SWRING1);

        G2.dispose();

        return IMAGE;
    }

    private BufferedImage create_MAIN_POINTER_SHADOW_Image(final int WIDTH) {
        if (WIDTH <= 0) {
            return null;
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final GeneralPath STOPWATCHPOINTER = new GeneralPath();
        STOPWATCHPOINTER.setWindingRule(Path2D.WIND_EVEN_ODD);
        STOPWATCHPOINTER.moveTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.45794392523364486);
        STOPWATCHPOINTER.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.102803738317757);
        STOPWATCHPOINTER.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.45794392523364486);
        STOPWATCHPOINTER.curveTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.45794392523364486);
        STOPWATCHPOINTER.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.46261682242990654, IMAGE_WIDTH * 0.45794392523364486, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.45794392523364486, IMAGE_HEIGHT * 0.5);
        STOPWATCHPOINTER.curveTo(IMAGE_WIDTH * 0.45794392523364486, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.5373831775700935, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5420560747663551);
        STOPWATCHPOINTER.curveTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5420560747663551, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5420560747663551, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5420560747663551);
        STOPWATCHPOINTER.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.6214953271028038);
        STOPWATCHPOINTER.lineTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.6214953271028038);
        STOPWATCHPOINTER.lineTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.5420560747663551);
        STOPWATCHPOINTER.curveTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.5420560747663551, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.5420560747663551, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.5420560747663551);
        STOPWATCHPOINTER.curveTo(IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.5373831775700935, IMAGE_WIDTH * 0.5420560747663551, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.5420560747663551, IMAGE_HEIGHT * 0.5);
        STOPWATCHPOINTER.curveTo(IMAGE_WIDTH * 0.5420560747663551, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.46261682242990654, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.45794392523364486);
        STOPWATCHPOINTER.curveTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.45794392523364486);
        STOPWATCHPOINTER.closePath();

        G2.setPaint(SHADOW_COLOR);
        G2.fill(STOPWATCHPOINTER);

        G2.dispose();

        return IMAGE;
    }

    private BufferedImage create_SMALL_POINTER_Image(final int WIDTH) {
        if (WIDTH <= 0) {
            return null;
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        //G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final GeneralPath STOPWATCHPOINTERSMALL = new GeneralPath();
        STOPWATCHPOINTERSMALL.setWindingRule(Path2D.WIND_EVEN_ODD);
        STOPWATCHPOINTERSMALL.moveTo(IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.3130841121495327);
        STOPWATCHPOINTERSMALL.curveTo(IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.32242990654205606, IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.3317757009345794, IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.3364485981308411);
        STOPWATCHPOINTERSMALL.curveTo(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.3364485981308411, IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.35046728971962615, IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.35046728971962615);
        STOPWATCHPOINTERSMALL.lineTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.35046728971962615);
        STOPWATCHPOINTERSMALL.curveTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.35046728971962615, IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.3364485981308411, IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.3364485981308411);
        STOPWATCHPOINTERSMALL.curveTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.3317757009345794, IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.32242990654205606, IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.3130841121495327);
        STOPWATCHPOINTERSMALL.curveTo(IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.3037383177570093, IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.29439252336448596, IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.2897196261682243);
        STOPWATCHPOINTERSMALL.curveTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.2897196261682243, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.20093457943925233, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.20093457943925233);
        STOPWATCHPOINTERSMALL.curveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.20093457943925233, IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.2897196261682243, IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.2897196261682243);
        STOPWATCHPOINTERSMALL.curveTo(IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.29439252336448596, IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.3037383177570093, IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.3130841121495327);
        STOPWATCHPOINTERSMALL.closePath();
        if (flatNeedle) {
            G2.setColor(getPointerColor().MEDIUM);
            G2.fill(STOPWATCHPOINTERSMALL);
        } else {
            final Point2D POINTER_START = new Point2D.Double(STOPWATCHPOINTERSMALL.getBounds2D().getMinX(), 0);
            final Point2D POINTER_STOP = new Point2D.Double(STOPWATCHPOINTERSMALL.getBounds2D().getMaxX(), 0);
            final float[] POINTER_FRACTIONS = {
                0.0f,
                0.3888888889f,
                0.5f,
                0.6111111111f,
                1.0f
            };
            final Color[] POINTER_COLORS = {
                getPointerColor().MEDIUM,
                getPointerColor().MEDIUM,
                getPointerColor().LIGHT,
                getPointerColor().MEDIUM,
                getPointerColor().MEDIUM
            };
            final LinearGradientPaint GRADIENT = new LinearGradientPaint(POINTER_START, POINTER_STOP, POINTER_FRACTIONS, POINTER_COLORS);
            G2.setPaint(GRADIENT);
            G2.fill(STOPWATCHPOINTERSMALL);
            G2.setPaint(getPointerColor().DARK);
            G2.draw(STOPWATCHPOINTERSMALL);
        }

        final Ellipse2D SWBRASSRINGSMALL = new Ellipse2D.Double(IMAGE_WIDTH * 0.4813084006309509, IMAGE_HEIGHT * 0.29439252614974976, IMAGE_WIDTH * 0.037383198738098145, IMAGE_HEIGHT * 0.03738316893577576);
        G2.setColor(new Color(0xC48200));
        G2.fill(SWBRASSRINGSMALL);

        final Ellipse2D SWRING1SMALL = new Ellipse2D.Double(IMAGE_WIDTH * 0.4859813153743744, IMAGE_HEIGHT * 0.29906541109085083, IMAGE_WIDTH * 0.02803739905357361, IMAGE_HEIGHT * 0.02803739905357361);
        G2.setColor(new Color(0x999999));
        G2.fill(SWRING1SMALL);

        final Ellipse2D SWRING1SMALL0 = new Ellipse2D.Double(IMAGE_WIDTH * 0.49065420031547546, IMAGE_HEIGHT * 0.3037383258342743, IMAGE_WIDTH * 0.018691569566726685, IMAGE_HEIGHT * 0.018691569566726685);
        G2.setColor(Color.BLACK);
        G2.fill(SWRING1SMALL0);

        G2.dispose();

        return IMAGE;
    }

    private BufferedImage create_SMALL_POINTER_SHADOW_Image(final int WIDTH) {
        if (WIDTH <= 0) {
            return null;
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final GeneralPath STOPWATCHPOINTERSMALL = new GeneralPath();
        STOPWATCHPOINTERSMALL.setWindingRule(Path2D.WIND_EVEN_ODD);
        STOPWATCHPOINTERSMALL.moveTo(IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.3130841121495327);
        STOPWATCHPOINTERSMALL.curveTo(IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.32242990654205606, IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.3317757009345794, IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.3364485981308411);
        STOPWATCHPOINTERSMALL.curveTo(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.3364485981308411, IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.35046728971962615, IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.35046728971962615);
        STOPWATCHPOINTERSMALL.lineTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.35046728971962615);
        STOPWATCHPOINTERSMALL.curveTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.35046728971962615, IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.3364485981308411, IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.3364485981308411);
        STOPWATCHPOINTERSMALL.curveTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.3317757009345794, IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.32242990654205606, IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.3130841121495327);
        STOPWATCHPOINTERSMALL.curveTo(IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.3037383177570093, IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.29439252336448596, IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.2897196261682243);
        STOPWATCHPOINTERSMALL.curveTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.2897196261682243, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.20093457943925233, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.20093457943925233);
        STOPWATCHPOINTERSMALL.curveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.20093457943925233, IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.2897196261682243, IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.2897196261682243);
        STOPWATCHPOINTERSMALL.curveTo(IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.29439252336448596, IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.3037383177570093, IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.3130841121495327);
        STOPWATCHPOINTERSMALL.closePath();

        G2.setPaint(SHADOW_COLOR);
        G2.fill(STOPWATCHPOINTERSMALL);

        G2.dispose();

        return IMAGE;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Size related">
    @Override
    public Dimension getMinimumSize() {
        Dimension dim = super.getMinimumSize();
        if (dim.width < 50 || dim.height < 50) {
            dim = new Dimension(50, 50);
        }
        return dim;
    }

    @Override
    public void setMinimumSize(final Dimension DIM) {
        int  width = DIM.width < 50 ? 50 : DIM.width;
        int height = DIM.height < 50 ? 50 : DIM.height;
        final int SIZE = width <= height ? width : height;
        super.setMinimumSize(new Dimension(SIZE, SIZE));
        calcInnerBounds();
        init(getGaugeBounds().width, getGaugeBounds().height);
        setInitialized(true);
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
        init(getGaugeBounds().width, getGaugeBounds().height);
        setInitialized(true);
        invalidate();
        repaint();
    }

    @Override
    public void setPreferredSize(final Dimension DIM) {
        final int SIZE = DIM.width <= DIM.height ? DIM.width : DIM.height;
        super.setPreferredSize(new Dimension(SIZE, SIZE));
        calcInnerBounds();
        init(getGaugeBounds().width, getGaugeBounds().height);
        setInitialized(true);
        invalidate();
        repaint();
    }

    @Override
    public void setSize(final int WIDTH, final int HEIGHT) {
        final int SIZE = WIDTH <= HEIGHT ? WIDTH : HEIGHT;
        super.setSize(SIZE, SIZE);
        calcInnerBounds();
        init(getGaugeBounds().width, getGaugeBounds().height);
        setInitialized(true);
    }

    @Override
    public void setSize(final Dimension DIM) {
        final int SIZE = DIM.width <= DIM.height ? DIM.width : DIM.height;
        super.setSize(new Dimension(SIZE, SIZE));
        calcInnerBounds();
        init(getGaugeBounds().width, getGaugeBounds().height);
        setInitialized(true);
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
        init(getGaugeBounds().width, getGaugeBounds().height);
        setInitialized(true);
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
        init(getGaugeBounds().width, getGaugeBounds().height);
        setInitialized(true);
    }

    @Override
    public void setBorder(Border BORDER) {
        super.setBorder(BORDER);
        calcInnerBounds();
        init(getInnerBounds().width, getInnerBounds().height);
    }

    /**
     * Returns the alignment of the radial gauge along the X axis.
     * @return the alignment of the radial gauge along the X axis.
     */
    @Override
    public int getHorizontalAlignment() {
        return horizontalAlignment;
    }

    /**
     * Sets the alignment of the radial gauge along the X axis.
     * @param HORIZONTAL_ALIGNMENT (SwingConstants.CENTER is default)
     */
    @Override
    public void setHorizontalAlignment(final int HORIZONTAL_ALIGNMENT) {
        horizontalAlignment = HORIZONTAL_ALIGNMENT;
    }

    /**
     * Returns the alignment of the radial gauge along the Y axis.
     * @return the alignment of the radial gauge along the Y axis.
     */
    @Override
    public int getVerticalAlignment() {
        return verticalAlignment;
    }

    /**
     * Sets the alignment of the radial gauge along the Y axis.
     * @param VERTICAL_ALIGNMENT (SwingConstants.CENTER is default)
     */
    @Override
    public void setVerticalAlignment(final int VERTICAL_ALIGNMENT) {
        verticalAlignment = VERTICAL_ALIGNMENT;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Misc">
    @Override
    public void dispose() {
        CLOCK_TIMER.removeActionListener(this);
        super.dispose();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ActionListener">
    @Override
    public void actionPerformed(final ActionEvent EVENT) {
        if (EVENT.getSource().equals(CLOCK_TIMER)) {
            currentMilliSeconds = (System.currentTimeMillis() - start);
            secondPointerAngle = (currentMilliSeconds * ANGLE_STEP / 1000);
            minutePointerAngle = (secondPointerAngle % 1000) / 30;

            minutes = (currentMilliSeconds) % 60000;
            seconds = (currentMilliSeconds) % 60;
            milliSeconds = (currentMilliSeconds) % 1000;

            repaint(INNER_BOUNDS);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ComponentListener">
    @Override
    public void componentResized(ComponentEvent event) {
        final int SIZE = getWidth() < getHeight() ? getWidth() : getHeight();
        setPreferredSize(new java.awt.Dimension(SIZE, SIZE));

        if (SIZE < getMinimumSize().width || SIZE < getMinimumSize().height) {
            setPreferredSize(getMinimumSize());
        }
        calcInnerBounds();

        init(INNER_BOUNDS.width, INNER_BOUNDS.height);

        //revalidate();
        //repaint();
    }
    // </editor-fold>

    @Override
    public String toString() {
        return "StopWatch";
    }
}
