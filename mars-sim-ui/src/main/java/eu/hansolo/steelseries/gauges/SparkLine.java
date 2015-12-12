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
package eu.hansolo.steelseries.gauges;

import eu.hansolo.steelseries.tools.ColorDef;
import eu.hansolo.steelseries.tools.DataPoint;
import eu.hansolo.steelseries.tools.LcdColor;
import eu.hansolo.steelseries.tools.SmoothingFunction;
import eu.hansolo.steelseries.tools.Util;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JComponent;


/**
 *
 * @author hansolo
 */
public class SparkLine extends JComponent {
    // <editor-fold defaultstate="collapsed" desc="Variable declarations">

    private static final Util UTIL = Util.INSTANCE;
    private static final DecimalFormat DF = new DecimalFormat("0.00");
    private final Rectangle INNER_BOUNDS = new Rectangle(0, 0, 128, 48);
    private boolean recreateImages;
    private final LinkedList<DataPoint> DATA_LIST;
    private List<Double> sortedList = new ArrayList<Double>(3600);
    private List<DataPoint> trashList;
    private List<Point2D> pointList;
    private final Color DISABLED_COLOR;
    private double start;
    private double stop;
    private double lo;
    private double hi;
    private int loIndex;
    private int hiIndex;
    private double offset;
    private double scaleY;
    private double rangeY;
    private boolean filled;
    private SmoothingFunction smoothFunction;
    private boolean smoothing;
    private float lineWidth;
    private LcdColor sparkLineColor;
    private Paint customSparkLineColor;
    private Color lineColor;
    private ColorDef areaFill;
    private Color customAreaFillTop;
    private Color customAreaFillBottom;
    private boolean lineShadowVisible;
    private boolean startStopIndicatorVisible;
    private boolean hiLoIndicatorVisible;
    private boolean backgroundVisible;
    private boolean infoLabelsVisible;
    private final Font INFO_LABEL_FONT;
    private BufferedImage sparkLineBackgroundImage;
    private BufferedImage startIndicatorImage;
    private BufferedImage stopIndicatorImage;
    private BufferedImage loIndicatorImage;
    private BufferedImage hiIndicatorImage;
    private BufferedImage sparkLineImage;
    private java.awt.Shape disabledShape;
    private final RoundRectangle2D CLIP_SHAPE;
    private long timeFrame;
    private double pixelResolution;
    private double baseLineY;
    private boolean baseLineVisible;
    private boolean averageVisible;
    private boolean normalAreaVisible;
    private final Line2D AVERAGE_LINE;
    private final Rectangle2D NORMAL_AREA;
    private Color averageColor;
    private Color normalAreaColor;
    private final transient ComponentListener COMPONENT_LISTENER = new ComponentAdapter() {

        @Override
        public void componentResized(java.awt.event.ComponentEvent event) {
            java.awt.Container parent = getParent();

            if (getWidth() < getMinimumSize().width && getHeight() < getMinimumSize().height) {
                if (parent != null && getParent().getLayout() == null) {
                    setSize(getMinimumSize());
                } else {
                    setPreferredSize(getMinimumSize());
                }
            }

            if (parent != null && getParent().getLayout() == null) {
                setSize(getWidth(), getHeight());
            } else {
                setPreferredSize(new java.awt.Dimension(getWidth(), getHeight()));
            }

            calcInnerBounds();
            recreateAllImages();
            init(INNER_BOUNDS.width, INNER_BOUNDS.height);
            //revalidate();
            //repaint();
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public SparkLine() {
        super();
        recreateImages = true;
        DATA_LIST = new LinkedList<DataPoint>();
        trashList = new ArrayList<DataPoint>(512);
        pointList = new ArrayList<Point2D>(INNER_BOUNDS.width);
        DISABLED_COLOR = new Color(102, 102, 102, 178);
        scaleY = 1.0;
        rangeY = 0;
        filled = false;
        smoothFunction = SmoothingFunction.COSINUS;
        smoothing = false;
        lineWidth = 1.0f;
        sparkLineColor = LcdColor.WHITE_LCD;
        customSparkLineColor = Color.WHITE;
        lineColor = sparkLineColor.TEXT_COLOR;
        areaFill = ColorDef.RED;
        customAreaFillTop = new Color(1.0f, 1.0f, 1.0f, 0.4f);
        customAreaFillBottom = new Color(1.0f, 1.0f, 1.0f, 0.0f);
        lineShadowVisible = false;
        startStopIndicatorVisible = true;
        hiLoIndicatorVisible = false;
        backgroundVisible = true;
        infoLabelsVisible = false;
        INFO_LABEL_FONT = new Font("Verdana", 0, 12);
        CLIP_SHAPE = new RoundRectangle2D.Double();
        timeFrame = 3600000; // Default to 1 hour == 3600 sec => 3600000 ms
        baseLineVisible = false;
        averageVisible = false;
        normalAreaVisible = false;
        AVERAGE_LINE = new Line2D.Double(INNER_BOUNDS.x, 0, INNER_BOUNDS.x + INNER_BOUNDS.width, 0);
        NORMAL_AREA = new Rectangle2D.Double(INNER_BOUNDS.x, 0, INNER_BOUNDS.width, 0);
        averageColor = new Color(102, 216, 29);
        normalAreaColor = new Color(216, 29, 68, 25);
        createInitialImages();
        init(INNER_BOUNDS.width, INNER_BOUNDS.height);
        addComponentListener(COMPONENT_LISTENER);
        repaint(INNER_BOUNDS);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Initialization">
    /**
     * Initializes the sparkline component with the given width and height
     * @param WIDTH
     * @param HEIGHT
     */
    private void init(final int WIDTH, final int HEIGHT) {
        if (WIDTH <= 1 || HEIGHT <= 1) {
            return;
        }

        final double CORNER_RADIUS = WIDTH > HEIGHT ? (HEIGHT * 0.095) : (WIDTH * 0.095);
        if (recreateImages) {
            if (sparkLineBackgroundImage != null) {
                sparkLineBackgroundImage.flush();
            }
            sparkLineBackgroundImage = create_SPARK_LINE_BACKGROUND_Image(WIDTH, HEIGHT);

            // set the clip shape
            CLIP_SHAPE.setRoundRect(1, 1, WIDTH - 2, HEIGHT - 2, CORNER_RADIUS - 1, CORNER_RADIUS - 1);

            if (startIndicatorImage != null) {
                startIndicatorImage.flush();
            }
            //startIndicatorImage = createIndicatorImage(WIDTH, HEIGHT, ColorDef.GRAY);
            startIndicatorImage = create_START_STOP_INDICATOR_Image(WIDTH);

            if (stopIndicatorImage != null) {
                stopIndicatorImage.flush();
            }
            //stopIndicatorImage = createIndicatorImage(WIDTH, HEIGHT, ColorDef.GRAY);
            stopIndicatorImage = create_START_STOP_INDICATOR_Image(WIDTH);

            if (loIndicatorImage != null) {
                loIndicatorImage.flush();
            }
            //loIndicatorImage = createIndicatorImage(WIDTH, HEIGHT, ColorDef.BLUE);
            loIndicatorImage = create_LO_INDICATOR_Image(WIDTH);

            if (hiIndicatorImage != null) {
                hiIndicatorImage.flush();
            }
            //hiIndicatorImage = createIndicatorImage(WIDTH, HEIGHT, ColorDef.RED);
            hiIndicatorImage = create_HI_INDICATOR_Image(WIDTH);
        }
        recreateImages = false;

        disabledShape = new java.awt.geom.RoundRectangle2D.Double(0, 0, WIDTH, HEIGHT, CORNER_RADIUS, CORNER_RADIUS);

        // Calculation and creation of sparkline itself
        pixelResolution = INNER_BOUNDS.getWidth() / (double) timeFrame;
        //offset = (int)(0.015 * WIDTH) < 4 ? 4 : (int)(0.015 * WIDTH);
        offset = (int) (0.06 * WIDTH) < 8 ? 8 : (int) (0.06 * WIDTH);

        baseLineY = INNER_BOUNDS.y + INNER_BOUNDS.height - ((0 - lo) * (1 / scaleY) + offset);

        if (!DATA_LIST.isEmpty()) {
            calculate(WIDTH, HEIGHT);
        }

        if (sparkLineImage != null) {
            sparkLineImage.flush();
        }
        sparkLineImage = createSparkLineImage(WIDTH, HEIGHT);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Visualization">
    @Override
    protected void paintComponent(Graphics g) {
        final Graphics2D G2 = (Graphics2D) g.create();

        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        G2.translate(INNER_BOUNDS.x, INNER_BOUNDS.y);

        if (backgroundVisible) {
            // Draw SparkLineBackground
            G2.drawImage(sparkLineBackgroundImage, 0, 0, null);

            // Set the clip
            G2.setClip(CLIP_SHAPE);
        }

        // Draw info label text
        if (infoLabelsVisible && INNER_BOUNDS.height > 40) {
            G2.setColor(sparkLineColor.TEXT_COLOR);
            G2.setFont(INFO_LABEL_FONT.deriveFont(0.12f * INNER_BOUNDS.height));
            G2.drawString("hi: " + DF.format(hi), (int) (INNER_BOUNDS.width * 0.0277777778), 2 + G2.getFont().getSize());
            G2.drawString("lo: " + DF.format(lo), (int) (INNER_BOUNDS.width * 0.0277777778), (INNER_BOUNDS.height - 4));
        }

        // Draw the sparkline
        G2.drawImage(sparkLineImage, 0, 0, null);

        // Draw baseline
        if (baseLineVisible) {
            G2.setColor(Color.BLACK);
            G2.drawLine(INNER_BOUNDS.x, (int) baseLineY, INNER_BOUNDS.x + INNER_BOUNDS.width, (int) baseLineY);
        }

        // Draw normal area
        if (normalAreaVisible) {
            G2.setColor(normalAreaColor);
            G2.fill(NORMAL_AREA);
        }

        // Draw average
        if (averageVisible) {
            G2.setColor(averageColor);
            G2.draw(AVERAGE_LINE);
        }

        // Draw disabled image if needed
        if (!isEnabled()) {
            G2.setColor(DISABLED_COLOR);
            G2.fill(disabledShape);
        }

        G2.translate(-INNER_BOUNDS.x, -INNER_BOUNDS.y);

        G2.dispose();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters / Setters">
    /**
     * Adds a new value to the DATA_LIST of the sparkline
     * @param DATA
     */
    public void addDataPoint(final double DATA) {
        for (DataPoint dataPoint : DATA_LIST) {
            if (System.currentTimeMillis() - dataPoint.getTimeStamp() > timeFrame) {
                trashList.add(dataPoint);
            }
        }

        for (DataPoint dataPoint : trashList) {
            DATA_LIST.remove(dataPoint);
        }
        trashList.clear();
        DATA_LIST.add(new DataPoint(System.currentTimeMillis(), DATA));
        init(INNER_BOUNDS.width, INNER_BOUNDS.height);
        repaint(INNER_BOUNDS);
    }

    /**
     * Adds a new value to the DATA_LIST with the given timestamp
     * @param DATA
     * @param TIMESTAMP (type long like you get it with System.currentTimeMillis())
     */
    public void addDataPoint(final double DATA, final long TIMESTAMP) {
        for (DataPoint dataPoint : DATA_LIST) {
            if (TIMESTAMP - dataPoint.getTimeStamp() > timeFrame) {
                trashList.add(dataPoint);
            }
        }

        for (DataPoint dataPoint : trashList) {
            DATA_LIST.remove(dataPoint);
        }
        trashList.clear();
        DATA_LIST.add(new DataPoint(TIMESTAMP, DATA));
        init(INNER_BOUNDS.width, INNER_BOUNDS.height);
        repaint(INNER_BOUNDS);
    }

    /**
     * Returns the linked list that contains the current data of the sparkline
     * @return the linked list that contains the current data of the sparkline
     */
    public List<DataPoint> getDataList() {
        List<DataPoint> dataListCopy = new LinkedList<eu.hansolo.steelseries.tools.DataPoint>();
        dataListCopy.addAll(DATA_LIST);
        return dataListCopy;
    }

    /**
     * Clears the existing DATA_LIST and adds all elements from the given LinkedList to it
     * @param dataList
     */
    public void setDataList(LinkedList<DataPoint> dataList) {
        DATA_LIST.clear();
        DATA_LIST.addAll(dataList);
        repaint(INNER_BOUNDS);
    }

    /**
     * Returns the first entry in the sparkline DATA_LIST
     * @return the first entry in the sparkline DATA_LIST
     */
    public double getStart() {
        return this.start;
    }

    /**
     * Returns the first last in the sparkline DATA_LIST
     * @return the last entry in the sparkline DATA_LIST
     */
    public double getStop() {
        return this.stop;
    }

    /**
     * Returns the entry with the lowest value in the sparkline DATA_LIST
     * @return the entry with the lowest value in the sparkline DATA_LIST
     */
    public double getLo() {
        return this.lo;
    }

    /**
     * Returns the entry with the highest value in the sparkline DATA_LIST
     * @return the entry with the highest value in the sparkline DATA_LIST
     */
    public double getHi() {
        return this.hi;
    }

    /**
     * Returns the calculated varianz of the current data
     * @return the calculated varianz of the current data
     */
    public double getVariance() {
        if (!DATA_LIST.isEmpty()) {
            double sum = 0;
            double sumOfSquares = 0;
            double average = 0;
            for (DataPoint dataPoint : DATA_LIST) {
                sumOfSquares += (dataPoint.getValue() * dataPoint.getValue());
                sum += dataPoint.getValue();
            }
            average = sum / DATA_LIST.size();
            return (sumOfSquares / DATA_LIST.size()) - average * average;
        }
        return 0;
    }

    /**
     * Returns the calculated average of the current data
     * @return the calculated average of the current data
     */
    public double getAverage() {
        if (!DATA_LIST.isEmpty()) {
            double sum = 0;
            for (DataPoint dataPoint : DATA_LIST) {
                sum += dataPoint.getValue();
            }
            return sum / DATA_LIST.size();
        }
        return 0;
    }

    /**
     * Returns the calculated standard deviation of the current data
     * @return the calculated standard deviation of the current data
     */
    public double getStandardDeviation() {
        return Math.sqrt(getVariance());
    }

    /**
     * Returns the range of values which means hi - low
     * @return the range of values which means hi - low
     */
    public double getRange() {
        return this.rangeY;
    }

    public double getQ1() {
        if (DATA_LIST.size() > 2) {
            sortData();
            int stopIndex;
            if (sortedList.size() % 2 != 0) {
                stopIndex = sortedList.size() / 2;
            } else {
                stopIndex = sortedList.size() / 2 - 1;
            }
            return (sortedList.subList(0, stopIndex)).get(((sortedList.subList(0, stopIndex)).size() / 2));
        }
        return 0;
    }

    public double getQ2() {
        return getMedian();
    }

    public double getQ3() {
        if (DATA_LIST.size() > 2) {
            sortData();
            int startIndex = sortedList.size() / 2;
            return (sortedList.subList(startIndex, sortedList.size() - 1)).get(((sortedList.subList(startIndex, sortedList.size() - 1)).size() / 2));
        }
        return 0;
    }

    /**
     * Returns the median of the measured values
     * @return the median of the measured values
     */
    public double getMedian() {
        if (DATA_LIST.size() > 2) {
            sortData();

            if (sortedList.size() % 2 != 0) {
                return sortedList.get((sortedList.size() / 2));
            } else {
                return (sortedList.get(sortedList.size() / 2 - 1) + sortedList.get(sortedList.size() / 2)) / 2.0;
            }
        }
        return 0;
    }

    /**
     * Returns the current timeframe of the sparkline in milliseconds
     * @return the current timeframe of the sparkline in milliseconds
     */
    public long getTimeFrame() {
        return this.timeFrame;
    }

    /**
     * Defines the current timeframe of the sparkline in milliseconds
     * @param TIME_FRAME
     */
    public void setTimeFrame(final long TIME_FRAME) {
        this.timeFrame = TIME_FRAME;
        init(INNER_BOUNDS.width, INNER_BOUNDS.height);
        repaint(INNER_BOUNDS);
    }

    /**
     * Returns the timestamp of the first value in the list of
     * datapoints as a long.
     * @return the timestamp of the first value in the datalist as a long
     */
    public long getStartTimestamp() {
        if (DATA_LIST.isEmpty()) {
            return 0;
        }
        return DATA_LIST.getFirst().getTimeStamp();
    }

    /**
     * Returns the timestamp of the the last value in the list of
     * datapoints as a long.
     * @return the timestamp of the last value in the datalist as a long
     */
    public long getStopTimestamp() {
        if (DATA_LIST.isEmpty()) {
            return 0;
        }
        return DATA_LIST.getLast().getTimeStamp();
    }

    /**
     * Returns true if the area under the sparkline will be filled with a gradient
     * @return true if the area under the sparkline will be filled with a gradient
     */
    public boolean isFilled() {
        return this.filled;
    }

    /**
     * Enables or disables the filling of the area below the sparkline
     * @param FILLED
     */
    public void setFilled(final boolean FILLED) {
        this.filled = FILLED;
        init(INNER_BOUNDS.width, INNER_BOUNDS.height);
        repaint(INNER_BOUNDS);
    }

    /**
     * Returns true if the sparkline data will be smoothed
     * @return true if the sparkline data will be smoothed
     */
    public boolean isSmoothing() {
        return this.smoothing;
    }

    /**
     * Enables or disables the smoothing of the POINT_LIST and so of the sparkline
     * @param SMOOTHING
     */
    public void setSmoothing(final boolean SMOOTHING) {
        this.smoothing = SMOOTHING;
        if (SMOOTHING) {
            hiLoIndicatorVisible = false;
        }
        init(INNER_BOUNDS.width, INNER_BOUNDS.height);
        repaint(INNER_BOUNDS);
    }

    /**
     * Returns the current active color scheme of the sparkline (type eu.hansolo.tools.LcdColor)
     * @return the current active color scheme of the sparkline (type eu.hansolo.tools.LcdColor)
     */
    public LcdColor getSparkLineColor() {
        return this.sparkLineColor;
    }

    /**
     * Sets the color theme for the sparkline.
     * @param LCD_COLOR
     */
    public void setSparkLineColor(final LcdColor LCD_COLOR) {
        this.lineColor = LCD_COLOR.TEXT_COLOR;
        this.sparkLineColor = LCD_COLOR;
        recreateImages = true;
        init(INNER_BOUNDS.width, INNER_BOUNDS.height);
        repaint(INNER_BOUNDS);
    }

    /**
     * Returns the custom sparkline color of type java.awt.Paint which
     * will be used if the sparkLineColor is set to eu.hansolo.steelseries.tools.LcdColor.CUSTOM
     * @return the custom sparkline color of type java.awt.Paint.
     */
    public Paint getCustomSparkLineColor() {
        return this.customSparkLineColor;
    }

    public void setCustomSparkLineColor(final Paint CUSTOM_SPARKLINE_COLOR) {
        this.customSparkLineColor = CUSTOM_SPARKLINE_COLOR;
        recreateImages = true;
        init(INNER_BOUNDS.width, INNER_BOUNDS.height);
        repaint(INNER_BOUNDS);
    }

    /**
     * Returns the color of the sparkline itself
     * @return the color of the sparkline itself
     */
    public Color getLineColor() {
        return this.lineColor;
    }

    /**
     * Sets the color of the sparkline
     * @param LINE_COLOR
     */
    public void setLineColor(final Color LINE_COLOR) {
        this.lineColor = LINE_COLOR;
        init(INNER_BOUNDS.width, INNER_BOUNDS.height);
        repaint(INNER_BOUNDS);
    }

    /**
     * Returns the colordefinition of the area below the sparkline
     * @return the colordefinition of the area below the sparkline
     */
    public ColorDef getAreaFill() {
        return this.areaFill;
    }

    /**
     * Sets the colordefinition of the area below the sparkline
     * @param AREA_FILL_COLOR
     */
    public void setAreaFill(final ColorDef AREA_FILL_COLOR) {
        this.areaFill = AREA_FILL_COLOR;
        init(INNER_BOUNDS.width, INNER_BOUNDS.height);
        repaint(INNER_BOUNDS);
    }

    /**
     * Returns the color that will be used for a custom area gradient at top
     * @return the color that will be used for a custom area gradient at top
     */
    public Color getCustomAreaFillTop() {
        return this.customAreaFillTop;
    }

    /**
     * Sets the color that will be used for a custom area gradient at top
     * @param CUSTOM_AREA_FILL_COLOR_TOP
     */
    public void setCustomAreaFillTop(final Color CUSTOM_AREA_FILL_COLOR_TOP) {
        customAreaFillTop = CUSTOM_AREA_FILL_COLOR_TOP;
        init(INNER_BOUNDS.width, INNER_BOUNDS.height);
        repaint(INNER_BOUNDS);
    }

    /**
     * Returns the color that will be used for a custom area gradient at bottom
     * @return the color that will be used for a custom area gradient at bottom
     */
    public Color getCustomAreaFillBottom() {
        return this.customAreaFillBottom;
    }

    /**
     * Sets the color that will be used for a custom area gradient at bottom
     * @param CUSTOM_AREA_FILL_COLOR_BOTTOM
     */
    public void setCustomAreaFillBottom(final Color CUSTOM_AREA_FILL_COLOR_BOTTOM) {
        customAreaFillBottom = CUSTOM_AREA_FILL_COLOR_BOTTOM;
        init(INNER_BOUNDS.width, INNER_BOUNDS.height);
        repaint(INNER_BOUNDS);
    }

    /**
     * Returns the width of the sparkline
     * @return the width of the sparkline
     */
    public float getLineWidth() {
        return this.lineWidth;
    }

    /**
     * Defines the width of the sparkline
     * @param LINE_WIDTH
     */
    public void setLineWidth(final float LINE_WIDTH) {
        lineWidth = LINE_WIDTH;
        init(INNER_BOUNDS.width, INNER_BOUNDS.height);
        repaint(INNER_BOUNDS);
    }

    /**
     * Returns true if the sparkline shadow is visible
     * @return true if the sparkline shadow is visible
     */
    public boolean isLineShadowVisible() {
        return this.lineShadowVisible;
    }

    /**
     * Enables or disables the visibility of the sparkline shadow
     * @param LINE_SHADOW_VISIBLE
     */
    public void setLineShadow(final boolean LINE_SHADOW_VISIBLE) {
        lineShadowVisible = LINE_SHADOW_VISIBLE;
        init(INNER_BOUNDS.width, INNER_BOUNDS.height);
        repaint(INNER_BOUNDS);
    }

    /**
     * Returns true if the start/stop indicators are visible.
     * @return true if the start/stop indicators are visible
     */
    public boolean isStartStopIndicatorVisible() {
        return this.startStopIndicatorVisible;
    }

    /**
     * Defines the visibility of the start/stop indicators
     * @param START_STOP_INDICATOR_VISIBLE
     */
    public void setStartStopIndicatorVisible(final boolean START_STOP_INDICATOR_VISIBLE) {
        startStopIndicatorVisible = START_STOP_INDICATOR_VISIBLE;
        init(INNER_BOUNDS.width, INNER_BOUNDS.height);
        repaint(INNER_BOUNDS);
    }

    /**
     * Returns true if the hi/lo indicators are visible.
     * They will be disabled automaticaly if smoothing is applied to
     * the data.
     * @return true if the hi/lo indicators are visible
     */
    public boolean isHiLoIndicatorVisible() {
        return this.hiLoIndicatorVisible;
    }

    /**
     * Defines the visiblity of the hi/lo indicators
     * @param HI_LO_INDICATOR_VISIBLE
     */
    public void setHiLoIndicatorVisible(final boolean HI_LO_INDICATOR_VISIBLE) {
        hiLoIndicatorVisible = HI_LO_INDICATOR_VISIBLE;
        init(INNER_BOUNDS.width, INNER_BOUNDS.height);
        repaint(INNER_BOUNDS);
    }

    /**
     * Returns true if the backgroundimage of the sparkline is visible
     * @return true if the backgroundimage of the sparkline is visible
     */
    public boolean isBackgroundVisible() {
        return this.backgroundVisible;
    }

    /**
     * Defines the visibility of the background image
     * @param BACKGROUND_VISIBLE
     */
    public void setBackgroundVisible(final boolean BACKGROUND_VISIBLE) {
        this.backgroundVisible = BACKGROUND_VISIBLE;
        repaint(INNER_BOUNDS);
    }

    /**
     * Returns true if the info labels are visible
     * @return true if the info labels are visible
     */
    public boolean isInfoLabelsVisible() {
        return this.infoLabelsVisible;
    }

    /**
     * Enables or disables the visibility of the info labels.
     * They won't be drawn if the sparkline is too small.
     * @param INFO_LABELS_VISIBLE
     */
    public void setInfoLabelsVisible(final boolean INFO_LABELS_VISIBLE) {
        this.infoLabelsVisible = INFO_LABELS_VISIBLE;
        repaint(INNER_BOUNDS);
    }

    /**
     * Returns the smoothing function that is selected
     * @return ths smoothing function that is selected
     */
    public SmoothingFunction getSmoothFunction() {
        return this.smoothFunction;
    }

    /**
     * Defines the smoothing function that will be applied to the data if selected
     * @param SMOOTHING_FUNCTION
     */
    public void setSmoothFunction(final SmoothingFunction SMOOTHING_FUNCTION) {
        this.smoothFunction = SMOOTHING_FUNCTION;
        init(INNER_BOUNDS.width, INNER_BOUNDS.height);
        repaint(INNER_BOUNDS);
    }

    /**
     * Returns true if the baseline (value = 0) is visible
     * @return true if the baseline (value = 0) is visible
     */
    public boolean isBaseLineVisible() {
        return baseLineVisible;
    }

    /**
     * Enables or disables the visiblity of the baseline
     * @param BASE_LINE_VISIBLE
     */
    public void setBaseLineVisible(final boolean BASE_LINE_VISIBLE) {
        baseLineVisible = BASE_LINE_VISIBLE;
        repaint(INNER_BOUNDS);
    }

    /**
     * Returns true if the average is visible
     * @return true if the average is visible
     */
    public boolean isAverageVisible() {
        return averageVisible;
    }

    /**
     * Enables or disables the visibility of the average
     * @param AVERAGE_VISIBLE
     */
    public void setAverageVisible(final boolean AVERAGE_VISIBLE) {
        averageVisible = AVERAGE_VISIBLE;
        repaint(INNER_BOUNDS);
    }

    /**
     * Returns true if the normal area visible
     * @return true if the normal area visible
     */
    public boolean isNormalAreaVisible() {
        return normalAreaVisible;
    }

    /**
     * Enables or disables the visibility of the normal area
     * @param NORMAL_AREA_VISIBLE
     */
    public void setNormalAreaVisible(final boolean NORMAL_AREA_VISIBLE) {
        normalAreaVisible = NORMAL_AREA_VISIBLE;
        repaint(INNER_BOUNDS);
    }

    /**
     * Returns the color of the average
     * @return the color of the average
     */
    public Color getAverageColor() {
        return averageColor;
    }

    /**
     * Defines the color for the average
     * @param AVERAGE_COLOR
     */
    public void setAverageColor(final Color AVERAGE_COLOR) {
        averageColor = AVERAGE_COLOR;
        repaint(INNER_BOUNDS);
    }

    /**
     * Returns the color that will be used for the normal color
     * @return the color that will be used for the normal color
     */
    public Color getNormalAreaColor() {
        return normalAreaColor;
    }

    /**
     * Defines the color that will be used for the normal area. The given
     * color will be used with an alpha of 0.2f
     * @param NORMAL_AREA_COLOR
     */
    public void setNormalAreaColor(final Color NORMAL_AREA_COLOR) {
        normalAreaColor = UTIL.setAlpha(NORMAL_AREA_COLOR, 0.2f);
        repaint(INNER_BOUNDS);
    }

    /**
     * Returns the current component as buffered image.
     * To save this buffered image as png you could use for example:
     * File file = new File("image.png");
     * ImageIO.write(Image, "png", file);
     * @return the current component as buffered image
     */
    public BufferedImage getAsImage() {
        final BufferedImage IMAGE = UTIL.createImage(getWidth(), getHeight(), Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        paintAll(G2);
        G2.dispose();
        return IMAGE;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Calculation methods">
    /**
     * Calculates the sparkline with all it's parameters. This methods
     * will be called everytime a new value was added to the DATA_LIST
     * @param WIDTH
     * @param HEIGHT
     */
    private void calculate(final int WIDTH, final int HEIGHT) {
        // Set start and stop values
        start = DATA_LIST.getFirst().getValue();
        stop = DATA_LIST.getLast().getValue();

        // Find min and max values
        lo = DATA_LIST.getFirst().getValue();
        hi = DATA_LIST.getFirst().getValue();
        loIndex = 0;
        hiIndex = 0;
        final int SIZE = DATA_LIST.size();
        double y;
        for (int index = 0; index < SIZE; index++) {
            y = DATA_LIST.get(index).getValue();
            calcHiLoValues(y, index);
        }

        // Calculate the range from min to max
        rangeY = hi - lo;

        // Calculate the offset between the graph and the border
        //offset = (double) HEIGHT * OFFSET_FACTOR;

        // Calculate the scaling in x- and y-direction
        scaleY = rangeY / ((double) HEIGHT - (offset * 2));

        // Fill the pointlist with smoothing if possible
        pointList.clear();
        if (DATA_LIST.size() > 5 && smoothing) {
            smoothData();
        } else {
            for (int index = 0; index < SIZE; index++) {
                pointList.add(new Point2D.Double((DATA_LIST.get(index).getTimeStamp() - DATA_LIST.getFirst().getTimeStamp()) * pixelResolution, ((DATA_LIST.get(index).getValue() - lo) * (1 / scaleY) + offset)));
            }
        }

        // Calculate average and normal area if one of them is visible
        if (averageVisible || normalAreaVisible) {
            final double AVERAGE = (getAverage() - lo) * (1 / scaleY) + offset;
            final double STANDARD_DEVIATION = (getStandardDeviation() * (1 / scaleY));
            NORMAL_AREA.setRect(INNER_BOUNDS.x, AVERAGE - STANDARD_DEVIATION, INNER_BOUNDS.width, 2 * STANDARD_DEVIATION);
            AVERAGE_LINE.setLine(INNER_BOUNDS.x, AVERAGE, INNER_BOUNDS.x + INNER_BOUNDS.width, AVERAGE);
        }
    }

    /**
     * Calls the selected smoothing functions and fills the POINT_LIST
     * with the smoothed data
     */
    private void smoothData() {
        final int SIZE = DATA_LIST.size();
        double y;

        switch (smoothFunction) {
            case CONTINUOUS_AVERAGE:
                // Add first point
                pointList.add(new Point2D.Double(0, ((DATA_LIST.getFirst().getValue() - lo) * (1 / scaleY) + offset)));

                // Add the averaged points
                for (int i = 1; i < SIZE - 1; i++) {
                    //System.out.println((a * dataList.get(i - 1) + b * dataList.get(i) + c * (dataList.get( i + 1))) / (a + b + c));
                    y = continuousAverage(DATA_LIST.get(i - 1).getValue(), DATA_LIST.get(i).getValue(), DATA_LIST.get(i + 1).getValue());
                    pointList.add(new Point2D.Double((DATA_LIST.get(i).getTimeStamp() - DATA_LIST.getFirst().getTimeStamp()) * pixelResolution, ((y - lo) * (1 / scaleY) + offset)));
                }

                // Add last point
                pointList.add(new Point2D.Double((DATA_LIST.getLast().getTimeStamp() - DATA_LIST.getFirst().getTimeStamp()) * pixelResolution, ((DATA_LIST.getLast().getValue() - lo) * (1 / scaleY) + offset)));
                break;

            case CUBIC_SPLINE:
                for (int i = 2; i < SIZE - 1; i++) {
                    y = cubicInterpolate(DATA_LIST.get(i - 2).getValue(), DATA_LIST.get(i - 1).getValue(), DATA_LIST.get(i).getValue(), DATA_LIST.get(i + 1).getValue(), 0.5);
                    pointList.add(new Point2D.Double((DATA_LIST.get(i).getTimeStamp() - DATA_LIST.getFirst().getTimeStamp()) * pixelResolution, ((y - lo) * (1 / scaleY) + offset)));
                }
                break;

            case HERMITE:
                for (int i = 2; i < SIZE - 1; i++) {
                    y = hermiteInterpolate(DATA_LIST.get(i - 2).getValue(), DATA_LIST.get(i - 1).getValue(), DATA_LIST.get(i - 0).getValue(), DATA_LIST.get(i + 1).getValue(), 0.5, 0, 0);
                    pointList.add(new Point2D.Double((DATA_LIST.get(i).getTimeStamp() - DATA_LIST.getFirst().getTimeStamp()) * pixelResolution, ((y - lo) * (1 / scaleY) + offset)));
                }
                break;

            case COSINUS:

            default:
                for (int i = 0; i < SIZE - 1; i++) {
                    y = cosInterpolate(DATA_LIST.get(i).getValue(), DATA_LIST.get(i + 1).getValue(), 0.5);
                    pointList.add(new Point2D.Double((DATA_LIST.get(i).getTimeStamp() - DATA_LIST.getFirst().getTimeStamp()) * pixelResolution, ((y - lo) * (1 / scaleY) + offset)));
                }
                break;
        }
    }

    /**
     * Calculates the max and min measured values and stores the index of the
     * related values in in loIndex and hiIndex.
     * @param y
     * @param index
     */
    private void calcHiLoValues(double y, int index) {
        if (y < lo) {
            lo = y;
            loIndex = index;
        }

        if (y > hi) {
            hi = y;
            hiIndex = index;
        }
    }

    /**
     * Puts all values in a ArrayList and sorts them
     */
    private void sortData() {
        sortedList.clear();
        for (DataPoint dataPoint : DATA_LIST) {
            sortedList.add(dataPoint.getValue());
        }
        Collections.sort(sortedList);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Smoothing functions">
    /**
     * Returns the value smoothed by a continuous average function
     * @param Y0
     * @param Y1
     * @param Y2
     * @return the value smoothed by a continous average function
     */
    private double continuousAverage(final double Y0, final double Y1, final double Y2) {
        final double A = 1;
        final double B = 1;
        final double C = 1;

        return ((A * Y0) + (B * Y1) + (C * Y2)) / (A + B + C);
    }

    /**
     * Returns the value smoothed by a cubic spline interpolation function
     * @param Y0
     * @param Y1
     * @param Y2
     * @param Y3
     * @param MU
     * @return the value smoothed by a cubic spline interpolation function
     */
    private double cubicInterpolate(final double Y0, final double Y1, final double Y2, final double Y3, final double MU) {
        final double A0;
        final double A1;
        final double A2;
        final double A3;
        final double MU2;

        MU2 = MU * MU;
        A0 = Y3 - Y2 - Y0 + Y1;
        A1 = Y0 - Y1 - A0;
        A2 = Y2 - Y0;
        A3 = Y1;

        return ((A0 * MU * MU2) + (A1 * MU2) + (A2 * MU) + A3);
    }

    /**
     * Returns the value smoothed by a cosinus interpolation function
     * @param Y1
     * @param Y2
     * @param MU
     * @return the value smoothed by a cosinus interpolation function
     */
    private double cosInterpolate(final double Y1, final double Y2, final double MU) {
        final double MU2;

        MU2 = (1 - Math.cos(MU * Math.PI)) / 2;
        return (Y1 * (1 - MU2) + Y2 * MU2);
    }

    /**
     * Returns the value smoothed by a hermite interpolation function
     * @param Y0
     * @param Y1
     * @param Y2
     * @param Y3
     * @param MU
     * @param TENSION
     * @param BIAS
     * @return the value smoothed by a hermite interpolation function
     */
    private double hermiteInterpolate(final double Y0, final double Y1, final double Y2, final double Y3,
                                      final double MU, final double TENSION, final double BIAS) {
        double m0;
        double m1;
        final double MU2;
        final double Mu3;
        final double A0;
        final double A1;
        final double A2;
        final double A3;

        MU2 = MU * MU;
        Mu3 = MU2 * MU;
        m0 = (Y1 - Y0) * (1 + BIAS) * (1 - TENSION) / 2;
        m0 += (Y2 - Y1) * (1 - BIAS) * (1 - TENSION) / 2;
        m1 = (Y2 - Y1) * (1 + BIAS) * (1 - TENSION) / 2;
        m1 += (Y3 - Y2) * (1 - BIAS) * (1 - TENSION) / 2;
        A0 = (2 * Mu3) - (3 * MU2) + 1;
        A1 = Mu3 - (2 * MU2) + MU;
        A2 = Mu3 - MU2;
        A3 = (-2 * Mu3) + (3 * MU2);

        return ((A0 * Y1) + (A1 * m0) + (A2 * m1) + (A3 * Y2));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Size related methods">
    /**
     * Calculates the area that is available for painting the display
     */
    private void calcInnerBounds() {
        final java.awt.Insets INSETS = getInsets();
        INNER_BOUNDS.setBounds(INSETS.left, INSETS.top, getWidth() - INSETS.left - INSETS.right, getHeight() - INSETS.top - INSETS.bottom);
    }

    /**
     * Returns a rectangle2d representing the available space for drawing the
     * component taking the insets into account (e.g. given through borders etc.)
     * @return rectangle2d that represents the area available for rendering the component
     */
    public Rectangle getInnerBounds() {
        return INNER_BOUNDS;
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension dim = super.getMinimumSize();
        if (dim.width < 64 || dim.height < 24) {
            dim = new Dimension(64, 24);
        }
        return dim;
    }

    @Override
    public void setMinimumSize(final Dimension DIM) {
        int width = DIM.width < 64 ? 64 : DIM.width;
        int height = DIM.height < 24 ? 24 : DIM.height;
        super.setMinimumSize(new Dimension(width, height));
        calcInnerBounds();
        init(getInnerBounds().width, getInnerBounds().height);
        invalidate();
        repaint();
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension dim = super.getMaximumSize();
        if (dim.width > 1920 || dim.height > 720) {
            dim = new Dimension(1920, 720);
        }
        return dim;
    }

    @Override
    public void setMaximumSize(final Dimension DIM) {
        int width = DIM.width > 1920 ? 1920 : DIM.width;
        int height = DIM.height > 720 ? 720 : DIM.height;
        super.setMaximumSize(new Dimension(width, height));
        calcInnerBounds();
        init(getInnerBounds().width, getInnerBounds().height);
        invalidate();
        repaint();
    }

    @Override
    public void setPreferredSize(final Dimension DIM) {
        super.setPreferredSize(DIM);
        calcInnerBounds();
        init(getInnerBounds().width, getInnerBounds().height);
        invalidate();
        repaint();
    }

    @Override
    public void setSize(final int WIDTH, final int HEIGHT) {
        super.setSize(WIDTH, HEIGHT);
        calcInnerBounds();
        init(WIDTH, HEIGHT);
    }

    @Override
    public void setSize(final Dimension DIM) {
        super.setSize(DIM);
        calcInnerBounds();
        init(DIM.width, DIM.height);
    }

    @Override
    public void setBounds(final Rectangle BOUNDS) {
        super.setBounds(BOUNDS);
        calcInnerBounds();
        init(BOUNDS.width, BOUNDS.height);
    }

    @Override
    public void setBounds(final int X, final int Y, final int WIDTH, final int HEIGHT) {
        super.setBounds(X, Y, WIDTH, HEIGHT);
        calcInnerBounds();
        init(WIDTH, HEIGHT);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Image related methods">
    /**
     * Returns a buffered image that contains the background of the sparkline component
     * @param WIDTH
     * @param HEIGHT
     * @return a buffered image that contains the background of the sparkline component
     */
    private BufferedImage create_SPARK_LINE_BACKGROUND_Image(final int WIDTH, final int HEIGHT) {
        if (WIDTH <= 0 || HEIGHT <= 0) {
            return null;
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        // Background rectangle
        final Point2D BACKGROUND_START = new Point2D.Double(0.0, 0.0);
        final Point2D BACKGROUND_STOP = new Point2D.Double(0.0, IMAGE_HEIGHT);
        if (BACKGROUND_START.equals(BACKGROUND_STOP)) {
            BACKGROUND_STOP.setLocation(0.0, BACKGROUND_START.getY() + 1);
        }

        final float[] BACKGROUND_FRACTIONS = {
            0.0f,
            0.08f,
            0.92f,
            1.0f
        };

        final Color[] BACKGROUND_COLORS = {
            new Color(0.4f, 0.4f, 0.4f, 1.0f),
            new Color(0.5f, 0.5f, 0.5f, 1.0f),
            new Color(0.5f, 0.5f, 0.5f, 1.0f),
            new Color(0.9f, 0.9f, 0.9f, 1.0f)
        };

        final LinearGradientPaint BACKGROUND_GRADIENT = new LinearGradientPaint(BACKGROUND_START, BACKGROUND_STOP, BACKGROUND_FRACTIONS, BACKGROUND_COLORS);
        final double BACKGROUND_CORNER_RADIUS = WIDTH > HEIGHT ? (HEIGHT * 0.12) : (WIDTH * 0.12);
        final RoundRectangle2D BACKGROUND = new RoundRectangle2D.Double(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, BACKGROUND_CORNER_RADIUS, BACKGROUND_CORNER_RADIUS);
        G2.setPaint(BACKGROUND_GRADIENT);
        G2.fill(BACKGROUND);

        // Foreground rectangle
        final Point2D FOREGROUND_START = new Point2D.Double(0.0, 1.0);
        final Point2D FOREGROUND_STOP = new Point2D.Double(0.0, IMAGE_HEIGHT - 1);
        if (FOREGROUND_START.equals(FOREGROUND_STOP)) {
            FOREGROUND_STOP.setLocation(0.0, FOREGROUND_START.getY() + 1);
        }

        final float[] FOREGROUND_FRACTIONS = {
            0.0f,
            0.03f,
            0.49f,
            0.5f,
            1.0f
        };

        final Color[] FOREGROUND_COLORS = {
            sparkLineColor.GRADIENT_START_COLOR,
            sparkLineColor.GRADIENT_FRACTION1_COLOR,
            sparkLineColor.GRADIENT_FRACTION2_COLOR,
            sparkLineColor.GRADIENT_FRACTION3_COLOR,
            sparkLineColor.GRADIENT_STOP_COLOR
        };

        if (customSparkLineColor != null && sparkLineColor == LcdColor.CUSTOM) {
            G2.setPaint(customSparkLineColor);
        } else {
            final LinearGradientPaint FOREGROUND_GRADIENT = new LinearGradientPaint(FOREGROUND_START, FOREGROUND_STOP, FOREGROUND_FRACTIONS, FOREGROUND_COLORS);
            G2.setPaint(FOREGROUND_GRADIENT);
        }
        final double FOREGROUND_CORNER_RADIUS = BACKGROUND.getArcWidth() - 1;
        final RoundRectangle2D FOREGROUND = new RoundRectangle2D.Double(1, 1, IMAGE_WIDTH - 2, IMAGE_HEIGHT - 2, FOREGROUND_CORNER_RADIUS, FOREGROUND_CORNER_RADIUS);
        G2.fill(FOREGROUND);

        G2.dispose();

        return IMAGE;
    }

    /**
     * Returns a buffered image that contains the sparkline itself. This image will be calculated
     * everytime a new value was added to the DATA_LIST
     * @param WIDTH
     * @param HEIGHT
     * @return a buffered image that contains the sparkline itself
     */
    private BufferedImage createSparkLineImage(final int WIDTH, final int HEIGHT) {
        if (WIDTH <= 0 || HEIGHT <= 0) {
            return null;
        }

        final BufferedImage IMAGE = UTIL.createImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();

        if (!pointList.isEmpty()) {
            G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            //G2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

            // Generate sparkline and filled sparkline
            final GeneralPath SPARK_LINE = new GeneralPath();
            final GeneralPath SPARK_LINE_FILLED = new GeneralPath();

            SPARK_LINE_FILLED.moveTo(pointList.get(0).getX(), baseLineY);

            SPARK_LINE.moveTo(pointList.get(0).getX(), HEIGHT - pointList.get(0).getY());
            SPARK_LINE_FILLED.lineTo(pointList.get(0).getX(), HEIGHT - pointList.get(0).getY());

            for (Point2D point : pointList) {
                SPARK_LINE.lineTo(point.getX(), HEIGHT - point.getY());
                SPARK_LINE_FILLED.lineTo(point.getX(), HEIGHT - point.getY());
            }

            SPARK_LINE_FILLED.lineTo(pointList.get(pointList.size() - 1).getX(), baseLineY);
            SPARK_LINE_FILLED.closePath();

            // Draw sparkline
            G2.setStroke(new BasicStroke(this.lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));

            if (filled) {
                final Point2D START_POINT = new Point2D.Double(0, INNER_BOUNDS.y);
                final Point2D END_POINT = new Point2D.Double(0, INNER_BOUNDS.y + INNER_BOUNDS.height);
                if (START_POINT.equals(END_POINT)) {
                    END_POINT.setLocation(0.0, START_POINT.getY() + 1);
                }
                float baseLineFraction = (float) (baseLineY / INNER_BOUNDS.height);
                if (baseLineFraction < 0f || baseLineFraction > 1f) {
                    baseLineFraction = 0.5f;
                }
                if (START_POINT.distance(END_POINT) != 0) {
                    float[] fractions = {
                        0.0f,
                        baseLineFraction,
                        1.0f
                    };

                    // Set appropriate filling gradient
                    final Color[] COLORS;
                    if (areaFill == ColorDef.CUSTOM) {
                        COLORS = new Color[]{
                            customAreaFillTop,
                            customAreaFillBottom,
                            customAreaFillTop
                        };
                    } else {
                        COLORS = new Color[]{
                            UTIL.setAlpha(areaFill.LIGHT, 0.75f),
                            UTIL.setAlpha(areaFill.DARK, 0.0f),
                            UTIL.setAlpha(areaFill.LIGHT, 0.75f)
                        };
                    }
                    final LinearGradientPaint SPARK_LINE_GRADIENT = new LinearGradientPaint(START_POINT, END_POINT, fractions, COLORS);
                    G2.setPaint(SPARK_LINE_GRADIENT);
                    G2.fill(SPARK_LINE_FILLED);
                }
            } else {
                // Draw shadow of line which looks good on dark backgrounds
                if (lineShadowVisible) {
                    G2.translate(1, 1);
                    G2.setColor(new Color(0x000000));
                    G2.draw(SPARK_LINE);
                    G2.translate(-1, -1);
                }
            }

            // Set appropriate line color
            G2.setColor(lineColor);
            G2.draw(SPARK_LINE);

            // Draw indicators
            if (startStopIndicatorVisible) {
                G2.drawImage(startIndicatorImage, (int) pointList.get(0).getX() - startIndicatorImage.getWidth() / 2, HEIGHT - (int) pointList.get(0).getY() - startIndicatorImage.getHeight() / 2, null);
                G2.drawImage(stopIndicatorImage, (int) pointList.get(pointList.size() - 1).getX() - stopIndicatorImage.getWidth() / 2, HEIGHT - (int) pointList.get(pointList.size() - 1).getY() - stopIndicatorImage.getHeight() / 2, null);
            }
            if (hiLoIndicatorVisible) {
                if (loIndex < pointList.size()) {
                    G2.drawImage(loIndicatorImage, (int) pointList.get(loIndex).getX() - loIndicatorImage.getWidth() / 2, HEIGHT - (int) pointList.get(loIndex).getY() - loIndicatorImage.getHeight() / 2, null);
                }
                if (hiIndex < pointList.size()) {
                    G2.drawImage(hiIndicatorImage, (int) pointList.get(hiIndex).getX() - hiIndicatorImage.getWidth() / 2, HEIGHT - (int) pointList.get(hiIndex).getY() - hiIndicatorImage.getHeight() / 2, null);
                }
            }

        }
        G2.dispose();

        return IMAGE;
    }

    /**
     * Returns a buffered image that contains the hi value indicator
     * @param WIDTH
     * @return a buffered image that contains the hi value indicator
     */
    private BufferedImage create_HI_INDICATOR_Image(final int WIDTH) {
        if (WIDTH <= 0) {
            return null;
        }

        // Define the size of the indicator
        int indicatorSize = (int) (0.015 * WIDTH);
        if (indicatorSize < 4) {
            indicatorSize = 4;
        }
        if (indicatorSize > 8) {
            indicatorSize = 8;
        }

        final BufferedImage IMAGE = UTIL.createImage(indicatorSize, indicatorSize, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        G2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final GeneralPath THRESHOLD_TRIANGLE = new GeneralPath();
        THRESHOLD_TRIANGLE.setWindingRule(Path2D.WIND_EVEN_ODD);
        THRESHOLD_TRIANGLE.moveTo(IMAGE_WIDTH * 0.5, 0);
        THRESHOLD_TRIANGLE.lineTo(0, IMAGE_HEIGHT);
        THRESHOLD_TRIANGLE.lineTo(IMAGE_WIDTH, IMAGE_HEIGHT);
        THRESHOLD_TRIANGLE.lineTo(IMAGE_WIDTH * 0.5, 0);
        THRESHOLD_TRIANGLE.closePath();
        final Point2D THRESHOLD_TRIANGLE_START = new Point2D.Double(0, THRESHOLD_TRIANGLE.getBounds2D().getMinY());
        final Point2D THRESHOLD_TRIANGLE_STOP = new Point2D.Double(0, THRESHOLD_TRIANGLE.getBounds2D().getMaxY());
        final float[] THRESHOLD_TRIANGLE_FRACTIONS = {
            0.0f,
            0.3f,
            0.59f,
            1.0f
        };
        final Color[] THRESHOLD_TRIANGLE_COLORS = {
            new Color(82, 0, 0, 255),
            new Color(252, 29, 0, 255),
            new Color(252, 29, 0, 255),
            new Color(82, 0, 0, 255)
        };
        final LinearGradientPaint THRESHOLD_TRIANGLE_GRADIENT = new LinearGradientPaint(THRESHOLD_TRIANGLE_START, THRESHOLD_TRIANGLE_STOP, THRESHOLD_TRIANGLE_FRACTIONS, THRESHOLD_TRIANGLE_COLORS);
        G2.setPaint(THRESHOLD_TRIANGLE_GRADIENT);
        G2.fill(THRESHOLD_TRIANGLE);
        G2.setColor(Color.RED);
        G2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        G2.draw(THRESHOLD_TRIANGLE);

        G2.dispose();

        return IMAGE;
    }

    /**
     * Returns a buffered image that contains the lo value indicator
     * @param WIDTH
     * @return a buffered image that contains the lo valuw indicator
     */
    private BufferedImage create_LO_INDICATOR_Image(final int WIDTH) {
        if (WIDTH <= 0) {
            return null;
        }

        // Define the size of the indicator
        int indicatorSize = (int) (0.015 * WIDTH);
        if (indicatorSize < 4) {
            indicatorSize = 4;
        }
        if (indicatorSize > 8) {
            indicatorSize = 8;
        }

        final BufferedImage IMAGE = UTIL.createImage(indicatorSize, indicatorSize, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        G2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final GeneralPath THRESHOLD_TRIANGLE = new GeneralPath();
        THRESHOLD_TRIANGLE.setWindingRule(Path2D.WIND_EVEN_ODD);
        THRESHOLD_TRIANGLE.moveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT);
        THRESHOLD_TRIANGLE.lineTo(0, 0);
        THRESHOLD_TRIANGLE.lineTo(IMAGE_WIDTH, 0);
        THRESHOLD_TRIANGLE.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT);
        THRESHOLD_TRIANGLE.closePath();
        final Point2D THRESHOLD_TRIANGLE_START = new Point2D.Double(0, THRESHOLD_TRIANGLE.getBounds2D().getMaxY());
        final Point2D THRESHOLD_TRIANGLE_STOP = new Point2D.Double(0, THRESHOLD_TRIANGLE.getBounds2D().getMinY());
        final float[] THRESHOLD_TRIANGLE_FRACTIONS = {
            0.0f,
            0.3f,
            0.59f,
            1.0f
        };
        final Color[] THRESHOLD_TRIANGLE_COLORS = {
            new Color(0, 0, 72, 255),
            new Color(0, 29, 255, 255),
            new Color(0, 29, 255, 255),
            new Color(0, 0, 72, 255)
        };
        final LinearGradientPaint THRESHOLD_TRIANGLE_GRADIENT = new LinearGradientPaint(THRESHOLD_TRIANGLE_START, THRESHOLD_TRIANGLE_STOP, THRESHOLD_TRIANGLE_FRACTIONS, THRESHOLD_TRIANGLE_COLORS);
        G2.setPaint(THRESHOLD_TRIANGLE_GRADIENT);
        G2.fill(THRESHOLD_TRIANGLE);
        G2.setColor(new Color(0x001DFF));
        G2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        G2.draw(THRESHOLD_TRIANGLE);

        G2.dispose();

        return IMAGE;
    }

    /**
     * Returns a buffered image that contains the start/stop indicator
     * @param WIDTH
     * @return a buffered image that contains the start/stop indicator
     */
    private BufferedImage create_START_STOP_INDICATOR_Image(final int WIDTH) {
        if (WIDTH <= 0) {
            return null;
        }

        // Define the size of the indicator
        int indicatorSize = (int) (0.015 * WIDTH);
        if (indicatorSize < 4) {
            indicatorSize = 4;
        }
        if (indicatorSize > 8) {
            indicatorSize = 8;
        }

        final BufferedImage IMAGE = UTIL.createImage(indicatorSize, indicatorSize, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        G2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final Ellipse2D ELLIPSE = new Ellipse2D.Double(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        final Point2D ELLIPSE_CENTER = new Point2D.Double((0.42857142857142855 * IMAGE_WIDTH), (0.2857142857142857 * IMAGE_HEIGHT));
        final float[] ELLIPSE_FRACTIONS = {
            0.0f,
            0.01f,
            0.99f,
            1.0f
        };
        final Color[] ELLIPSE_COLORS = {
            new Color(204, 204, 204, 255),
            new Color(204, 204, 204, 255),
            new Color(51, 51, 51, 255),
            new Color(51, 51, 51, 255)
        };
        final RadialGradientPaint ELLIPSE_GRADIENT = new RadialGradientPaint(ELLIPSE_CENTER, (float) (0.5 * IMAGE_WIDTH), ELLIPSE_FRACTIONS, ELLIPSE_COLORS);
        G2.setPaint(ELLIPSE_GRADIENT);
        G2.fill(ELLIPSE);

        G2.dispose();

        return IMAGE;
    }

    /**
     * Calling this method will add all relevant imagetypes to the list of image types
     * that are needed to initialize the component
     */
    private void createInitialImages() {
        recreateImages = true;
    }

    /**
     * Calling this method will add all imagetypes to the list of imagetypes
     * so that the next time the init method will be called all images will
     * be recreated
     */
    private void recreateAllImages() {
        recreateImages = true;
    }
    // </editor-fold>

    @Override
    public String toString() {
        return "Sparkline";
    }
}
