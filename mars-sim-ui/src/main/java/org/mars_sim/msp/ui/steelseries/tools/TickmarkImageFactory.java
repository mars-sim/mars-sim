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
package org.mars_sim.msp.ui.steelseries.tools;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Gerrit Grunwald <han.solo at muenster.de>
 */
public enum TickmarkImageFactory {
    // <editor-fold defaultstate="collapsed" desc="Variable declarations">
    INSTANCE;
    private final Util UTIL = Util.INSTANCE;
    private static final BasicStroke MAJOR_TICKMARK_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
    private static final BasicStroke MEDIUM_TICKMARK_STROKE = new BasicStroke(0.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
    private static final BasicStroke MINOR_TICKMARK_STROKE = new BasicStroke(0.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
    private static final int BASE = 10;
    private NumberFormat numberFormat = NumberFormat.STANDARD;
    // Buffer variables of radial gauges
    private BufferedImage imageBufferRad = UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
    private int widthBufferRad = 200;
    private double minValueBufferRad = 0;
    private double maxValueBufferRad = 100;
    private int noOfMinorTicksBufferRad = 0;
    private int noOfMajorTicksBufferRad = 0;
    private double minorTickSpacingBufferRad = 10;
    private double majorTickSpacingBufferRad = 10;
    private GaugeType gaugeTypeBufferRad = GaugeType.TYPE4;
    private TickmarkType minorTickmarkTypeBufferRad = TickmarkType.LINE;
    private TickmarkType majorTickmarkTypeBufferRad = TickmarkType.LINE;
    private boolean ticksVisibleBufferRad = true;
    private boolean ticklabelsVisibleBufferRad = true;
    private boolean tickmarkSectionsVisibleBufferRad = false;
    private boolean minorTicksVisibleBufferRad = true;
    private boolean majorTicksVisibleBufferRad = true;
    private NumberFormat numberFormatBufferRad = NumberFormat.AUTO;
    private BackgroundColor backgroundColorBufferRad = BackgroundColor.DARK_GRAY;
    private Color tickmarkColorBufferRad = backgroundColorBufferRad.LABEL_COLOR;
    private boolean tickmarkColorFromThemeBufferRad = true;
    private List<Section> tickmarkSectionsBufferRad = new ArrayList<Section>(10);
    private boolean sectionTickmarksOnlyBufferRad = false;
    private List<Section> sectionsBufferRad = new ArrayList<Section>(10);
    private float radiusFactorBufferRad = 0.38f;
    private float textDistanceFactorBufferRad = 0.09f;
    private Point2D centerBufferRad = new Point2D.Double();
    private Point2D offsetBufferRad = new Point2D.Double();
    private Orientation orientationBufferRad = Orientation.NORTH;
    private TicklabelOrientation ticklabelOrientationBufferRad = TicklabelOrientation.TANGENT;
    private boolean niceScaleRad = true;
    private boolean logScaleRad = false;
    // Buffer variables of linear gauges
    private BufferedImage imageBufferLin = UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
    private int widthBufferLin = 140;
    private int heightBufferLin = 0;
    private double minValueBufferLin = 0;
    private double maxValueBufferLin = 100;
    private int noOfMinorTicksBufferLin = 0;
    private int noOfMajorTicksBufferLin = 0;
    private double minorTickSpacingBufferLin = 10;
    private double majorTickSpacingBufferLin = 10;
    private TickmarkType minorTickmarkTypeBufferLin = TickmarkType.LINE;
    private TickmarkType majorTickmarkTypeBufferLin = TickmarkType.LINE;
    private boolean ticksVisibleBufferLin = true;
    private boolean ticklabelsVisibleBufferLin = true;
    private boolean minorTicksVisibleBufferLin = true;
    private boolean majorTicksVisibleBufferLin = true;
    private NumberFormat numberFormatBufferLin = NumberFormat.AUTO;
    private boolean tickmarkSectionsVisibleBufferLin = false;
    private BackgroundColor backgroundColorBufferLin = BackgroundColor.DARK_GRAY;
    private Color tickmarkColorBufferLin = backgroundColorBufferLin.LABEL_COLOR;
    private boolean tickmarkColorFromThemeBufferLin = true;
    private List<Section> tickmarkSectionsBufferLin = new ArrayList<Section>(10);
    private Point2D offsetBufferLin = new Point2D.Double();
    private Orientation orientationBufferLin = Orientation.VERTICAL;
    private boolean niceScaleLin = true;
    private boolean logScaleLin = false;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Radial tickmark related">
    public BufferedImage create_RADIAL_TICKMARKS_Image(final int WIDTH,
                                                          final double MIN_VALUE,
                                                          final double MAX_VALUE,
                                                          final int NO_OF_MINOR_TICKS,
                                                          final int NO_OF_MAJOR_TICKS,
                                                          final double MINOR_TICK_SPACING,
                                                          final double MAJOR_TICK_SPACING,
                                                          final GaugeType GAUGE_TYPE,
                                                          final TickmarkType MINOR_TICKMARK_TYPE,
                                                          final TickmarkType MAJOR_TICKMARK_TYPE,
                                                          final boolean TICKS_VISIBLE,
                                                          final boolean TICKLABELS_VISIBLE,
                                                          final boolean MINOR_TICKS_VISIBLE,
                                                          final boolean MAJOR_TICKS_VISIBLE,
                                                          final NumberFormat NUMBER_FORMAT,
                                                          final boolean TICKMARK_SECTIONS_VISIBLE,
                                                          final BackgroundColor BACKGROUND_COLOR,
                                                          final Color TICKMARK_COLOR,
                                                          final boolean TICKMARK_COLOR_FROM_THEME,
                                                          List<Section> tickmarkSections,
                                                          final boolean SECTION_TICKMARKS_ONLY,
                                                          List<Section> sections,
                                                          final float RADIUS_FACTOR,
                                                          final float TEXT_DISTANCE_FACTOR,
                                                          final Point2D CENTER,
                                                          final Point2D OFFSET,
                                                          final Orientation ORIENTATION,
                                                          final TicklabelOrientation TICKLABEL_ORIENTATION,
                                                          final boolean NICE_SCALE,
                                                          final boolean LOG_SCALE,
                                                          final BufferedImage BACKGROUND_IMAGE) {
        if (WIDTH <= 0) {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        // Buffer check
        if (WIDTH == widthBufferRad
            && Double.compare(MIN_VALUE, minValueBufferRad) == 0
            && Double.compare(MAX_VALUE, maxValueBufferRad) == 0
            && NO_OF_MINOR_TICKS == noOfMinorTicksBufferRad
            && NO_OF_MAJOR_TICKS == noOfMajorTicksBufferRad
            && Double.compare(MINOR_TICK_SPACING, minorTickSpacingBufferRad) == 0
            && Double.compare(MAJOR_TICK_SPACING, majorTickSpacingBufferRad) == 0
            && GAUGE_TYPE == gaugeTypeBufferRad
            && MINOR_TICKMARK_TYPE == minorTickmarkTypeBufferRad
            && MAJOR_TICKMARK_TYPE == majorTickmarkTypeBufferRad
            && TICKS_VISIBLE == ticksVisibleBufferRad
            && TICKLABELS_VISIBLE == ticklabelsVisibleBufferRad
            && MINOR_TICKS_VISIBLE == minorTicksVisibleBufferRad
            && MAJOR_TICKS_VISIBLE == majorTicksVisibleBufferRad
            && SECTION_TICKMARKS_ONLY == sectionTickmarksOnlyBufferRad
            && sectionsBufferRad.containsAll(sections)
            && TICKMARK_SECTIONS_VISIBLE == tickmarkSectionsVisibleBufferRad
            && NUMBER_FORMAT == numberFormatBufferRad
            && BACKGROUND_COLOR == backgroundColorBufferRad
            && TICKMARK_COLOR.equals(tickmarkColorBufferRad)
            && TICKMARK_COLOR_FROM_THEME == tickmarkColorFromThemeBufferRad
            && tickmarkSections.containsAll(tickmarkSectionsBufferRad)
            && Float.compare(RADIUS_FACTOR, radiusFactorBufferRad) == 0
            && Float.compare(TEXT_DISTANCE_FACTOR, textDistanceFactorBufferRad) == 0
            && CENTER.equals(centerBufferRad)
            && OFFSET.equals(offsetBufferRad)
            && orientationBufferRad == ORIENTATION
            && ticklabelOrientationBufferRad == TICKLABEL_ORIENTATION
            && niceScaleRad == NICE_SCALE
            && logScaleRad == LOG_SCALE) {
            if (BACKGROUND_IMAGE != null) {
                final Graphics2D G = BACKGROUND_IMAGE.createGraphics();
                G.drawImage(imageBufferRad, 0, 0, null);
                G.dispose();
                return imageBufferRad;
            }
        }

        // Create image if it equals null
        if (imageBufferRad != null) {
            imageBufferRad.flush();
        }
        imageBufferRad = UTIL.createImage(WIDTH, WIDTH, Transparency.TRANSLUCENT);

        // Adjust the number format of the ticklabels
        if (NUMBER_FORMAT == NumberFormat.AUTO) {
            if (Math.abs(MAJOR_TICK_SPACING) > 1000) {
                numberFormat = NumberFormat.SCIENTIFIC;
            } else if (MAJOR_TICK_SPACING % 1.0 != 0) {
                numberFormat = NumberFormat.FRACTIONAL;
            } else {
                numberFormat = NumberFormat.STANDARD;
            }
        } else {
            numberFormat = NUMBER_FORMAT;
        }

        // Definitions
        final Font STD_FONT = new Font("Verdana", 0, (int) (0.04 * WIDTH));
        final Font SECTION_FONT = new Font("Verdana", 0, (int) (0.05 * WIDTH));
        final int TEXT_DISTANCE = (int) (TEXT_DISTANCE_FACTOR * WIDTH);
        double ticklabelRotationOffset = 0;
        final int MINOR_TICK_LENGTH = (int) (0.0133333333 * WIDTH);
        final int MEDIUM_TICK_LENGTH = (int) (0.02 * WIDTH);
        final int MAJOR_TICK_LENGTH = (int) (0.03 * WIDTH);
        final int MINOR_DIAMETER = (int) (0.0093457944 * WIDTH);
        //final int MEDIUM_DIAMETER = (int) (0.0186915888 * WIDTH);
        final int MAJOR_DIAMETER = (int) (0.03 * WIDTH);
        final Point2D TEXT_POINT = new Point2D.Double(0, 0);
        final Point2D INNER_POINT = new Point2D.Double(0, 0);
        final Point2D OUTER_POINT = new Point2D.Double(0, 0);
        final Point2D OUTER_POINT_LEFT = new Point2D.Double(0, 0);
        final Point2D OUTER_POINT_RIGHT = new Point2D.Double(0, 0);
        final Line2D TICK_LINE = new Line2D.Double(0, 0, 1, 1);
        final Ellipse2D TICK_CIRCLE = new Ellipse2D.Double(0, 0, 1, 1);
        final GeneralPath TICK_TRIANGLE = new GeneralPath();
        final double ROTATION_OFFSET = GAUGE_TYPE.ROTATION_OFFSET; // Depends on GaugeType
        final float RADIUS = WIDTH * RADIUS_FACTOR;
        final double ANGLE_STEP = (GAUGE_TYPE.ANGLE_RANGE / ((MAX_VALUE - MIN_VALUE) / MINOR_TICK_SPACING));
        double sinValue;
        double cosValue;
        double valueCounter = MIN_VALUE;
        int majorTickCounter = NO_OF_MINOR_TICKS - 1; // Indicator when to draw the major tickmark

        // Create the image
        final Graphics2D G2 = imageBufferRad.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Set some default parameters for the graphics object
        if (OFFSET != null) {
            G2.translate(OFFSET.getX(), OFFSET.getY());
        }
        G2.setFont(STD_FONT);

        G2.rotate(ROTATION_OFFSET - Math.PI, CENTER.getX(), CENTER.getY());
        if (TICKMARK_COLOR_FROM_THEME) {
            G2.setColor(BACKGROUND_COLOR.LABEL_COLOR);
        } else {
            G2.setColor(TICKMARK_COLOR);
        }

        if (SECTION_TICKMARKS_ONLY && sections != null) {
                double alpha = 0;
                G2.setFont(SECTION_FONT);
                // Min Value
                sinValue = Math.sin(alpha);
                cosValue = Math.cos(alpha);
                G2.setStroke(MAJOR_TICKMARK_STROKE);
                INNER_POINT.setLocation(CENTER.getX() + (RADIUS - MAJOR_TICK_LENGTH) * sinValue, CENTER.getY() + (RADIUS - MAJOR_TICK_LENGTH) * cosValue);
                OUTER_POINT.setLocation(CENTER.getX() + RADIUS * sinValue, CENTER.getY() + RADIUS * cosValue);
                TEXT_POINT.setLocation(CENTER.getX() + (RADIUS - TEXT_DISTANCE) * sinValue, CENTER.getY() + (RADIUS - TEXT_DISTANCE) * cosValue);
                drawRadialTicks(G2, INNER_POINT, OUTER_POINT, CENTER, RADIUS, MAJOR_TICKMARK_TYPE, TICK_LINE, TICK_CIRCLE, TICK_TRIANGLE, MAJOR_TICK_LENGTH, MAJOR_DIAMETER, OUTER_POINT_LEFT, OUTER_POINT_RIGHT, alpha);
                G2.fill(UTIL.rotateTextAroundCenter(G2, numberFormat.format(MIN_VALUE), (int) TEXT_POINT.getX(), (int) TEXT_POINT.getY(), Math.PI - GAUGE_TYPE.ROTATION_OFFSET));

                // Max Value
                alpha = -(MAX_VALUE - MIN_VALUE) * ANGLE_STEP;
                sinValue = Math.sin(alpha);
                cosValue = Math.cos(alpha);
                G2.setStroke(MAJOR_TICKMARK_STROKE);
                INNER_POINT.setLocation(CENTER.getX() + (RADIUS - MAJOR_TICK_LENGTH) * sinValue, CENTER.getY() + (RADIUS - MAJOR_TICK_LENGTH) * cosValue);
                OUTER_POINT.setLocation(CENTER.getX() + RADIUS * sinValue, CENTER.getY() + RADIUS * cosValue);
                TEXT_POINT.setLocation(CENTER.getX() + (RADIUS - TEXT_DISTANCE) * sinValue, CENTER.getY() + (RADIUS - TEXT_DISTANCE) * cosValue);
                drawRadialTicks(G2, INNER_POINT, OUTER_POINT, CENTER, RADIUS, MAJOR_TICKMARK_TYPE, TICK_LINE, TICK_CIRCLE, TICK_TRIANGLE, MAJOR_TICK_LENGTH, MAJOR_DIAMETER, OUTER_POINT_LEFT, OUTER_POINT_RIGHT, alpha);
                G2.fill(UTIL.rotateTextAroundCenter(G2, numberFormat.format(MAX_VALUE), (int) TEXT_POINT.getX(), (int) TEXT_POINT.getY(), Math.PI - GAUGE_TYPE.ROTATION_OFFSET));

                for (Section section : sections) {
                    // Section start
                    alpha = -(section.getStart() - MIN_VALUE) * ANGLE_STEP;
                    sinValue = Math.sin(alpha);
                    cosValue = Math.cos(alpha);
                    G2.setStroke(MAJOR_TICKMARK_STROKE);
                    INNER_POINT.setLocation(CENTER.getX() + (RADIUS - MAJOR_TICK_LENGTH) * sinValue, CENTER.getY() + (RADIUS - MAJOR_TICK_LENGTH) * cosValue);
                    OUTER_POINT.setLocation(CENTER.getX() + RADIUS * sinValue, CENTER.getY() + RADIUS * cosValue);
                    TEXT_POINT.setLocation(CENTER.getX() + (RADIUS - TEXT_DISTANCE) * sinValue, CENTER.getY() + (RADIUS - TEXT_DISTANCE) * cosValue);
                    drawRadialTicks(G2, INNER_POINT, OUTER_POINT, CENTER, RADIUS, MAJOR_TICKMARK_TYPE, TICK_LINE, TICK_CIRCLE, TICK_TRIANGLE, MAJOR_TICK_LENGTH, MAJOR_DIAMETER, OUTER_POINT_LEFT, OUTER_POINT_RIGHT, alpha);
                    G2.fill(UTIL.rotateTextAroundCenter(G2, numberFormat.format(section.getStart()), (int) TEXT_POINT.getX(), (int) TEXT_POINT.getY(), Math.PI - GAUGE_TYPE.ROTATION_OFFSET));

                    // Section stop
                    alpha = -(section.getStop() - MIN_VALUE) * ANGLE_STEP;
                    sinValue = Math.sin(alpha);
                    cosValue = Math.cos(alpha);
                    G2.setStroke(MAJOR_TICKMARK_STROKE);
                    INNER_POINT.setLocation(CENTER.getX() + (RADIUS - MAJOR_TICK_LENGTH) * sinValue, CENTER.getY() + (RADIUS - MAJOR_TICK_LENGTH) * cosValue);
                    OUTER_POINT.setLocation(CENTER.getX() + RADIUS * sinValue, CENTER.getY() + RADIUS * cosValue);
                    TEXT_POINT.setLocation(CENTER.getX() + (RADIUS - TEXT_DISTANCE) * sinValue, CENTER.getY() + (RADIUS - TEXT_DISTANCE) * cosValue);
                    drawRadialTicks(G2, INNER_POINT, OUTER_POINT, CENTER, RADIUS, MAJOR_TICKMARK_TYPE, TICK_LINE, TICK_CIRCLE, TICK_TRIANGLE, MAJOR_TICK_LENGTH, MAJOR_DIAMETER, OUTER_POINT_LEFT, OUTER_POINT_RIGHT, alpha);
                    G2.fill(UTIL.rotateTextAroundCenter(G2, numberFormat.format(section.getStop()), (int) TEXT_POINT.getX(), (int) TEXT_POINT.getY(), Math.PI - GAUGE_TYPE.ROTATION_OFFSET));
                }

        } else if(!LOG_SCALE) {
            for (double alpha = 0, counter = MIN_VALUE; Double.compare(counter, MAX_VALUE) <= 0; alpha -= ANGLE_STEP, counter += MINOR_TICK_SPACING) {
                // Set the color
                if (tickmarkSections != null && !tickmarkSections.isEmpty()) {
                    if (TICKMARK_SECTIONS_VISIBLE) {
                        for (Section section : tickmarkSections) {
                            if (Double.compare(valueCounter, section.getStart()) >= 0 && Double.compare(valueCounter, section.getStop()) <= 0) {
                                G2.setColor(Util.INSTANCE.setAlpha(section.getColor(), 1.0f));
                                break;
                            } else if (TICKMARK_COLOR_FROM_THEME) {
                                G2.setColor(BACKGROUND_COLOR.LABEL_COLOR);
                            } else {
                                G2.setColor(TICKMARK_COLOR);
                            }
                        }
                    } else {
                        if (TICKMARK_COLOR_FROM_THEME) {
                            G2.setColor(BACKGROUND_COLOR.LABEL_COLOR);
                        } else {
                            G2.setColor(TICKMARK_COLOR);
                        }
                    }
                }

                sinValue = Math.sin(alpha);
                cosValue = Math.cos(alpha);
                majorTickCounter++;

                // Draw tickmark every major tickmark spacing
                if (majorTickCounter == NO_OF_MINOR_TICKS) {
                    G2.setStroke(MAJOR_TICKMARK_STROKE);
                    INNER_POINT.setLocation(CENTER.getX() + (RADIUS - MAJOR_TICK_LENGTH) * sinValue, CENTER.getY() + (RADIUS - MAJOR_TICK_LENGTH) * cosValue);
                    OUTER_POINT.setLocation(CENTER.getX() + RADIUS * sinValue, CENTER.getY() + RADIUS * cosValue);
                    TEXT_POINT.setLocation(CENTER.getX() + (RADIUS - TEXT_DISTANCE) * sinValue, CENTER.getY() + (RADIUS - TEXT_DISTANCE) * cosValue);

                    // Draw the major tickmarks
                    if (TICKS_VISIBLE && MAJOR_TICKS_VISIBLE) {
                        drawRadialTicks(G2, INNER_POINT, OUTER_POINT, CENTER, RADIUS, MAJOR_TICKMARK_TYPE, TICK_LINE, TICK_CIRCLE, TICK_TRIANGLE, MAJOR_TICK_LENGTH, MAJOR_DIAMETER, OUTER_POINT_LEFT, OUTER_POINT_RIGHT, alpha);
                    }

                    // Draw the standard tickmark labels
                    if (TICKLABELS_VISIBLE) {
                        switch(TICKLABEL_ORIENTATION)
                        {
                            case NORMAL:
                                if (Double.compare(alpha, -GAUGE_TYPE.TICKLABEL_ORIENTATION_CHANGE_ANGLE) > 0) {
                                    G2.fill(UTIL.rotateTextAroundCenter(G2, numberFormat.format(valueCounter), (int) TEXT_POINT.getX(), (int) TEXT_POINT.getY(), (-Math.PI / 2 - alpha)));
                                } else {
                                    G2.fill(UTIL.rotateTextAroundCenter(G2, numberFormat.format(valueCounter), (int) TEXT_POINT.getX(), (int) TEXT_POINT.getY(), (Math.PI / 2 - alpha)));
                                }
                                break;
                            case HORIZONTAL:
                                double orientationOffset;
                                if (Orientation.WEST == ORIENTATION) {
                                    orientationOffset = Math.PI / 2;
                                } else if (Orientation.EAST == ORIENTATION) {
                                    orientationOffset = -Math.PI / 2;
                                } else if (Orientation.SOUTH == ORIENTATION) {
                                    orientationOffset = Math.PI;
                                } else {
                                    orientationOffset = 0;
                                }
                                G2.fill(UTIL.rotateTextAroundCenter(G2, numberFormat.format(valueCounter), (int) TEXT_POINT.getX(), (int) TEXT_POINT.getY(), Math.PI - GAUGE_TYPE.ROTATION_OFFSET + orientationOffset));
                                break;
                            case TANGENT:

                            default:
                                String label = numberFormat.format(valueCounter, MAX_VALUE);

                                if (!label.isEmpty()) {
                                    G2.fill(UTIL.rotateTextAroundCenter(G2, label, (int) TEXT_POINT.getX(), (int) TEXT_POINT.getY(), (Math.PI - alpha + ticklabelRotationOffset)));
                                }
                                break;
                        }
                    }

                    valueCounter += MAJOR_TICK_SPACING;
                    majorTickCounter = 0;
                    continue;
                }

                // Draw tickmark every minor tickmark spacing
                {
                    INNER_POINT.setLocation(CENTER.getX() + (RADIUS - MINOR_TICK_LENGTH) * sinValue, CENTER.getY() + (RADIUS - MINOR_TICK_LENGTH) * cosValue);
                    OUTER_POINT.setLocation(CENTER.getX() + RADIUS * sinValue, CENTER.getY() + RADIUS * cosValue);
                    G2.setStroke(MINOR_TICKMARK_STROKE);
                    if (NO_OF_MINOR_TICKS % 2 == 0 && majorTickCounter == (NO_OF_MINOR_TICKS / 2)) {
                        G2.setStroke(MEDIUM_TICKMARK_STROKE);
                        INNER_POINT.setLocation(CENTER.getX() + (RADIUS - MEDIUM_TICK_LENGTH) * sinValue,
                                                CENTER.getY() + (RADIUS - MEDIUM_TICK_LENGTH) * cosValue);
                        OUTER_POINT.setLocation(CENTER.getX() + RADIUS * sinValue, CENTER.getY() + RADIUS * cosValue);

                    }

                    // Draw the minor tickmarks
                    if (TICKS_VISIBLE && MINOR_TICKS_VISIBLE) {
                        drawRadialTicks(G2, INNER_POINT, OUTER_POINT, CENTER, RADIUS, MINOR_TICKMARK_TYPE, TICK_LINE, TICK_CIRCLE, TICK_TRIANGLE, MINOR_TICK_LENGTH, MINOR_DIAMETER, OUTER_POINT_LEFT, OUTER_POINT_RIGHT, alpha);
                    }
                }
            }
        } else {
            // ****************************** LOGARITHMIC SCALING ******************************************************
            final double LOG_ANGLE_STEP = Math.abs(GAUGE_TYPE.ANGLE_RANGE / UTIL.logOfBase(BASE, (MAX_VALUE - MIN_VALUE)));
            int exponent = 0;
            double angle;
            double valueStep = 1.0;
            for (double value = 1 ; Double.compare(value, MAX_VALUE) <= 0; value += valueStep) {
                if (TICKMARK_COLOR_FROM_THEME) {
                    G2.setColor(BACKGROUND_COLOR.LABEL_COLOR);
                } else {
                    G2.setColor(TICKMARK_COLOR);
                }

                angle = UTIL.logOfBase(BASE, Math.abs(value)) * LOG_ANGLE_STEP;
                sinValue = Math.sin(-angle);
                cosValue = Math.cos(-angle);

                INNER_POINT.setLocation(CENTER.getX() + (RADIUS - MINOR_TICK_LENGTH) * sinValue, CENTER.getY() + (RADIUS - MINOR_TICK_LENGTH) * cosValue);
                OUTER_POINT.setLocation(CENTER.getX() + RADIUS * sinValue, CENTER.getY() + RADIUS * cosValue);
                G2.setStroke(MINOR_TICKMARK_STROKE);

                if (Double.compare(value, Math.pow(BASE, exponent + 1)) == 0) {
                    exponent++;
                    valueStep = Math.pow(BASE, exponent);
                    INNER_POINT.setLocation(CENTER.getX() + (RADIUS - MAJOR_TICK_LENGTH) * sinValue, CENTER.getY() + (RADIUS - MAJOR_TICK_LENGTH) * cosValue);
                    OUTER_POINT.setLocation(CENTER.getX() + RADIUS * sinValue, CENTER.getY() + RADIUS * cosValue);
                    TEXT_POINT.setLocation(CENTER.getX() + (RADIUS - TEXT_DISTANCE) * sinValue, CENTER.getY() + (RADIUS - TEXT_DISTANCE) * cosValue);
                    if (TICKLABELS_VISIBLE) {
                        switch(TICKLABEL_ORIENTATION)
                        {
                            case NORMAL:
                                if (Double.compare(value, -GAUGE_TYPE.TICKLABEL_ORIENTATION_CHANGE_ANGLE) > 0) {
                                    G2.fill(UTIL.rotateTextAroundCenter(G2, numberFormat.format(value), (int) TEXT_POINT.getX(), (int) TEXT_POINT.getY(), (-Math.PI / 2 + angle)));
                                } else {
                                    G2.fill(UTIL.rotateTextAroundCenter(G2, numberFormat.format(value), (int) TEXT_POINT.getX(), (int) TEXT_POINT.getY(), (Math.PI / 2 + angle)));
                                }
                                break;
                            case HORIZONTAL:
                                double orientationOffset;
                                if (Orientation.WEST == ORIENTATION) {
                                    orientationOffset = Math.PI / 2;
                                } else if (Orientation.EAST == ORIENTATION) {
                                    orientationOffset = -Math.PI / 2;
                                } else if (Orientation.SOUTH == ORIENTATION) {
                                    orientationOffset = Math.PI;
                                } else {
                                    orientationOffset = 0;
                                }
                                G2.fill(UTIL.rotateTextAroundCenter(G2, numberFormat.format(value), (int) TEXT_POINT.getX(), (int) TEXT_POINT.getY(), Math.PI - GAUGE_TYPE.ROTATION_OFFSET + orientationOffset));
                                break;
                            case TANGENT:

                            default:
                                G2.fill(UTIL.rotateTextAroundCenter(G2, numberFormat.format(value), (int) TEXT_POINT.getX(), (int) TEXT_POINT.getY(), (Math.PI + angle + ticklabelRotationOffset)));
                                break;
                        }
                    }
                    G2.setStroke(MAJOR_TICKMARK_STROKE);
                }
                if (TICKS_VISIBLE && MAJOR_TICKS_VISIBLE && MINOR_TICKS_VISIBLE) {
                    TICK_LINE.setLine(INNER_POINT, OUTER_POINT);
                    G2.draw(TICK_LINE);
                }
            }
        }
        G2.dispose();

        if (BACKGROUND_IMAGE != null) {
            final Graphics2D G = BACKGROUND_IMAGE.createGraphics();
            G.drawImage(imageBufferRad, 0, 0, null);
            G.dispose();
        }

        // Buffer the current parameters
        widthBufferRad = WIDTH;
        minValueBufferRad = MIN_VALUE;
        maxValueBufferRad = MAX_VALUE;
        noOfMinorTicksBufferRad = NO_OF_MINOR_TICKS;
        noOfMajorTicksBufferRad = NO_OF_MAJOR_TICKS;
        minorTickSpacingBufferRad = MINOR_TICK_SPACING;
        majorTickSpacingBufferRad = MAJOR_TICK_SPACING;
        gaugeTypeBufferRad = GAUGE_TYPE;
        minorTickmarkTypeBufferRad = MINOR_TICKMARK_TYPE;
        majorTickmarkTypeBufferRad = MAJOR_TICKMARK_TYPE;
        ticksVisibleBufferRad = TICKS_VISIBLE;
        ticklabelsVisibleBufferRad = TICKLABELS_VISIBLE;
        minorTicksVisibleBufferRad = MINOR_TICKS_VISIBLE;
        majorTicksVisibleBufferRad = MAJOR_TICKS_VISIBLE;
        tickmarkSectionsVisibleBufferRad = TICKMARK_SECTIONS_VISIBLE;
        numberFormatBufferRad = NUMBER_FORMAT;
        backgroundColorBufferRad = BACKGROUND_COLOR;
        tickmarkColorBufferRad = TICKMARK_COLOR;
        tickmarkColorFromThemeBufferRad = TICKMARK_COLOR_FROM_THEME;
        if (tickmarkSections != null) {
            tickmarkSectionsBufferRad.clear();
            tickmarkSectionsBufferRad.addAll(tickmarkSections);
        }
        if (sections != null) {
            sectionsBufferRad.clear();
            sectionsBufferRad.addAll(sections);
        }
        sectionTickmarksOnlyBufferRad = SECTION_TICKMARKS_ONLY;
        radiusFactorBufferRad = RADIUS_FACTOR;
        textDistanceFactorBufferRad = TEXT_DISTANCE_FACTOR;
        centerBufferRad.setLocation(CENTER);
        if (OFFSET != null) {
            offsetBufferRad.setLocation(OFFSET);
        }
        orientationBufferRad = ORIENTATION;
        ticklabelOrientationBufferRad = TICKLABEL_ORIENTATION;
        niceScaleRad = NICE_SCALE;
        logScaleRad = LOG_SCALE;

        return imageBufferRad;
    }

    private void drawRadialTicks(final Graphics2D G2,
                                 final Point2D INNER_POINT,
                                 final Point2D OUTER_POINT,
                                 final Point2D CENTER,
                                 final double RADIUS,
                                 final TickmarkType TICKMARK_TYPE,
                                 final Line2D TICK_LINE,
                                 final Ellipse2D TICK_CIRCLE,
                                 final GeneralPath TICK_TRIANGLE,
                                 final double TICK_LENGTH,
                                 final double DIAMETER,
                                 final Point2D OUTER_POINT_LEFT,
                                 final Point2D OUTER_POINT_RIGHT,
                                 final double ALPHA) {
        // Draw tickmark every major tickmark spacing
        switch (TICKMARK_TYPE) {
            case CIRCLE:
                TICK_CIRCLE.setFrame(OUTER_POINT.getX() - DIAMETER / 2.0, OUTER_POINT.getY() - DIAMETER / 2.0, DIAMETER, DIAMETER);
                G2.fill(TICK_CIRCLE);
                break;
            case TRIANGLE:
                OUTER_POINT_LEFT.setLocation(CENTER.getX() + RADIUS * Math.sin(ALPHA - Math.toRadians(Math.asin(TICK_LENGTH / 16.0))), CENTER.getY() + RADIUS * Math.cos(ALPHA - Math.toRadians(Math.asin(TICK_LENGTH / 16.0))));
                OUTER_POINT_RIGHT.setLocation(CENTER.getX() + RADIUS * Math.sin(ALPHA + Math.toRadians(Math.asin(TICK_LENGTH / 16.0))), CENTER.getY() + RADIUS * Math.cos(ALPHA + Math.toRadians(Math.asin(TICK_LENGTH / 16.0))));
                TICK_TRIANGLE.reset();
                TICK_TRIANGLE.moveTo(INNER_POINT.getX(), INNER_POINT.getY());
                TICK_TRIANGLE.lineTo(OUTER_POINT_LEFT.getX(), OUTER_POINT_LEFT.getY());
                TICK_TRIANGLE.lineTo(OUTER_POINT_RIGHT.getX(), OUTER_POINT_RIGHT.getY());
                TICK_TRIANGLE.closePath();
                G2.fill(TICK_TRIANGLE);
                break;
            case LINE:

            default:
                TICK_LINE.setLine(INNER_POINT, OUTER_POINT);
                G2.draw(TICK_LINE);
                break;
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Linear tickmark related">
    public BufferedImage create_LINEAR_TICKMARKS_Image(final int WIDTH,
                                                          final int HEIGHT,
                                                          final double MIN_VALUE,
                                                          final double MAX_VALUE,
                                                          final int NO_OF_MINOR_TICKS,
                                                          final int NO_OF_MAJOR_TICKS,
                                                          final double MINOR_TICK_SPACING,
                                                          final double MAJOR_TICK_SPACING,
                                                          final TickmarkType MINOR_TICKMARK_TYPE,
                                                          final TickmarkType MAJOR_TICKMARK_TYPE,
                                                          final boolean TICKS_VISIBLE,
                                                          final boolean TICKLABELS_VISIBLE,
                                                          final boolean MINOR_TICKS_VISIBLE,
                                                          final boolean MAJOR_TICKS_VISIBLE,
                                                          final NumberFormat NUMBER_FORMAT,
                                                          final boolean TICKMARK_SECTIONS_VISIBLE,
                                                          final BackgroundColor BACKGROUND_COLOR,
                                                          final Color TICKMARK_COLOR,
                                                          final boolean TICKMARK_COLOR_FROM_THEME,
                                                          List<Section> tickmarkSections,
                                                          final Point2D OFFSET,
                                                          final Orientation ORIENTATION,
                                                          final boolean NICE_SCALE,
                                                          final boolean LOG_SCALE,
                                                          final BufferedImage BACKGROUND_IMAGE) {
        if (WIDTH <= 0 || HEIGHT <= 0) {
            return UTIL.createImage(1, 1, Transparency.TRANSLUCENT);
        }

        // Buffer check
        if (WIDTH == widthBufferLin
            && HEIGHT == heightBufferLin
            && Double.compare(MIN_VALUE, minValueBufferLin) == 0
            && Double.compare(MAX_VALUE, maxValueBufferLin) == 0
            && NO_OF_MINOR_TICKS == noOfMinorTicksBufferLin
            && NO_OF_MAJOR_TICKS == noOfMajorTicksBufferLin
            && Double.compare(MINOR_TICK_SPACING, minorTickSpacingBufferLin) == 0
            && Double.compare(MAJOR_TICK_SPACING, majorTickSpacingBufferLin) == 0
            && MINOR_TICKMARK_TYPE == minorTickmarkTypeBufferLin
            && MAJOR_TICKMARK_TYPE == majorTickmarkTypeBufferLin
            && TICKS_VISIBLE == ticksVisibleBufferLin
            && MINOR_TICKS_VISIBLE == minorTicksVisibleBufferLin
            && MAJOR_TICKS_VISIBLE == majorTicksVisibleBufferLin
            && TICKLABELS_VISIBLE == ticklabelsVisibleBufferLin
            && NUMBER_FORMAT == numberFormatBufferLin
            && TICKMARK_SECTIONS_VISIBLE == tickmarkSectionsVisibleBufferLin
            && BACKGROUND_COLOR == backgroundColorBufferLin
            && TICKMARK_COLOR.equals(tickmarkColorBufferLin)
            && TICKMARK_COLOR_FROM_THEME == tickmarkColorFromThemeBufferLin
            && tickmarkSections.containsAll(tickmarkSectionsBufferLin)
            && OFFSET.equals(offsetBufferLin)
            && ORIENTATION == orientationBufferLin
            && NICE_SCALE == niceScaleLin
            && LOG_SCALE == logScaleLin) {
            if (BACKGROUND_IMAGE != null) {
                final Graphics2D G = BACKGROUND_IMAGE.createGraphics();
                G.drawImage(imageBufferLin, 0, 0, null);
                G.dispose();

                return imageBufferLin;
            }
        }

        // Create image if it equals null
        if (imageBufferLin != null) {
            imageBufferLin.flush();
        }
        imageBufferLin = UTIL.createImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);

        // Adjust the number format of the ticklabels
        if (NUMBER_FORMAT == NumberFormat.AUTO) {
            if (Math.abs(MAJOR_TICK_SPACING) > 1000) {
                numberFormat = NumberFormat.SCIENTIFIC;
            } else if (MAJOR_TICK_SPACING % 1.0 != 0) {
                numberFormat = NumberFormat.FRACTIONAL;
            }
        } else {
            numberFormat = NUMBER_FORMAT;
        }

        // Definitions
        final Font STD_FONT;
        final Rectangle2D SCALE_BOUNDS;

        final int MINOR_DIAMETER;
        final int MAJOR_DIAMETER;
        final int MINOR_TICK_START;
        final int MINOR_TICK_STOP;
        final int MEDIUM_TICK_START;
        final int MEDIUM_TICK_STOP;
        final int MAJOR_TICK_START;
        final int MAJOR_TICK_STOP;
        double tickSpaceScaling;

        if (ORIENTATION == Orientation.VERTICAL) {
            // Vertical orientation
            STD_FONT = new Font("Verdana", 0, (int) (0.062 * WIDTH));
            SCALE_BOUNDS = new Rectangle2D.Double(0, HEIGHT * 0.12864077669902912, 0, (HEIGHT * 0.8567961165048543 - HEIGHT * 0.12864077669902912));

            MINOR_DIAMETER = (int) (0.0186915888 * WIDTH);
            MAJOR_DIAMETER = (int) (0.0280373832 * WIDTH);
            MINOR_TICK_START = (int) (0.34 * WIDTH);
            MINOR_TICK_STOP = (int) (0.36 * WIDTH);
            MEDIUM_TICK_START = (int) (0.33 * WIDTH);
            MEDIUM_TICK_STOP = (int) (0.36 * WIDTH);
            MAJOR_TICK_START = (int) (0.32 * WIDTH);
            MAJOR_TICK_STOP = (int) (0.36 * WIDTH);

            tickSpaceScaling = SCALE_BOUNDS.getHeight() / (MAX_VALUE - MIN_VALUE);
        } else {
            // Horizontal orientation
            STD_FONT = new Font("Verdana", 0, (int) (0.062 * HEIGHT));
            SCALE_BOUNDS = new Rectangle2D.Double(WIDTH * 0.14285714285714285, 0, (WIDTH * 0.8710124827 - WIDTH * 0.14285714285714285), 0);

            MINOR_DIAMETER = (int) (0.0186915888 * HEIGHT);
            MAJOR_DIAMETER = (int) (0.0280373832 * HEIGHT);
            MINOR_TICK_START = (int) (0.65 * HEIGHT);
            MINOR_TICK_STOP = (int) (0.63 * HEIGHT);
            MEDIUM_TICK_START = (int) (0.66 * HEIGHT);
            MEDIUM_TICK_STOP = (int) (0.63 * HEIGHT);
            MAJOR_TICK_START = (int) (0.67 * HEIGHT);
            MAJOR_TICK_STOP = (int) (0.63 * HEIGHT);

            tickSpaceScaling = SCALE_BOUNDS.getWidth() / (MAX_VALUE - MIN_VALUE);
        }

        final Graphics2D G2 = imageBufferLin.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Set some default parameters for the graphics object
        if (OFFSET != null) {
            G2.translate(OFFSET.getX(), OFFSET.getY());
        }

        G2.setFont(STD_FONT);

        if (TICKMARK_COLOR_FROM_THEME) {
            G2.setColor(BACKGROUND_COLOR.LABEL_COLOR);
        } else {
            G2.setColor(TICKMARK_COLOR);
        }

        final FontRenderContext RENDER_CONTEXT = new FontRenderContext(null, true, true);
        final TextLayout TEXT_LAYOUT = new TextLayout(numberFormat.format(MAX_VALUE), G2.getFont(), RENDER_CONTEXT);
        final Rectangle2D MAX_BOUNDS = TEXT_LAYOUT.getBounds(); // needed to align the numbers on the right (in vertical layout)
        final Line2D TICK_LINE = new Line2D.Double(0, 0, 1, 1);
        final Ellipse2D TICK_CIRCLE = new Ellipse2D.Double(0, 0, 1, 1);
        final GeneralPath TICK_TRIANGLE = new GeneralPath();

        TextLayout currentLayout;
        Rectangle2D currentBounds;
        float textOffset;
        double currentPos;
        double valueCounter = MIN_VALUE;
        int majorTickCounter = NO_OF_MINOR_TICKS - 1; // Indicator when to draw the major tickmark

        if (!LOG_SCALE) {
            // Non logarithmic scaling
            for (double labelCounter = MIN_VALUE, tickCounter = 0; Float.compare((float) labelCounter, (float) MAX_VALUE) <= 0; labelCounter += MINOR_TICK_SPACING, tickCounter += MINOR_TICK_SPACING) {
                // Adjust the color for the tickmark and labels
                if (tickmarkSections != null && !tickmarkSections.isEmpty()) {
                    if (TICKMARK_SECTIONS_VISIBLE) {
                        for (Section section : tickmarkSections) {
                            if ((Double.compare(tickCounter, section.getStart()) >= 0) && (Double.compare(tickCounter, section.getStop()) <= 0)) {
                                G2.setColor(section.getColor());
                                break;
                            } else if (TICKMARK_COLOR_FROM_THEME) {
                                G2.setColor(BACKGROUND_COLOR.LABEL_COLOR);
                            } else {
                                G2.setColor(TICKMARK_COLOR);
                            }
                        }
                    } else {
                        if (TICKMARK_COLOR_FROM_THEME) {
                            G2.setColor(BACKGROUND_COLOR.LABEL_COLOR);
                        } else {
                            G2.setColor(TICKMARK_COLOR);
                        }
                    }
                } else {
                    if (TICKMARK_COLOR_FROM_THEME) {
                        G2.setColor(BACKGROUND_COLOR.LABEL_COLOR);
                    } else {
                        G2.setColor(TICKMARK_COLOR);
                    }
                }

                // Calculate the bounds of the scaling
                if (ORIENTATION == Orientation.VERTICAL) {
                    currentPos = SCALE_BOUNDS.getMaxY() - tickCounter * tickSpaceScaling;
                } else {
                    currentPos = SCALE_BOUNDS.getX() + tickCounter * tickSpaceScaling;
                }

                majorTickCounter++;

                // Draw tickmark every major tickmark spacing
                if (majorTickCounter == NO_OF_MINOR_TICKS) {
                    G2.setStroke(MAJOR_TICKMARK_STROKE);

                    // Draw the major tickmarks
                    if (TICKS_VISIBLE && MAJOR_TICKS_VISIBLE) {
                        drawLinearTicks(G2, WIDTH, HEIGHT, ORIENTATION, currentPos, MAJOR_TICKMARK_TYPE, TICK_LINE, TICK_CIRCLE, TICK_TRIANGLE, MAJOR_TICK_START, MAJOR_TICK_STOP, MAJOR_DIAMETER);
                    }

                    // Draw the standard tickmark labels
                    if (TICKLABELS_VISIBLE) {
                        currentLayout = new TextLayout(numberFormat.format(valueCounter), G2.getFont(), RENDER_CONTEXT);
                        currentBounds = currentLayout.getBounds();
                        if (ORIENTATION == Orientation.VERTICAL) {
                            // Vertical orientation
                            textOffset = (float) (MAX_BOUNDS.getWidth() - currentBounds.getWidth());
                            G2.drawString(numberFormat.format(valueCounter, MAX_VALUE), 0.18f * WIDTH + textOffset, (float) (currentPos - currentBounds.getHeight() / 2.0 + currentBounds.getHeight()));
                        } else {
                            // Horizontal orientation
                            G2.drawString(numberFormat.format(valueCounter, MAX_VALUE), (float) (tickCounter * tickSpaceScaling - currentBounds.getWidth() / 3.0 + SCALE_BOUNDS.getX()), (float) (HEIGHT * 0.68 + 1.5 * currentBounds.getHeight()));
                        }
                    }

                    valueCounter += MAJOR_TICK_SPACING;
                    majorTickCounter = 0;
                    continue;
                }

                // Draw tickmark every minor tickmark spacing
                if (TICKS_VISIBLE && MINOR_TICKS_VISIBLE) {
                    G2.setStroke(MINOR_TICKMARK_STROKE);
                    if (NO_OF_MINOR_TICKS % 2 == 0 && majorTickCounter == (NO_OF_MINOR_TICKS / 2)) {
                        G2.setStroke(MEDIUM_TICKMARK_STROKE);
                        drawLinearTicks(G2, WIDTH, HEIGHT, ORIENTATION, currentPos, MINOR_TICKMARK_TYPE, TICK_LINE, TICK_CIRCLE, TICK_TRIANGLE, MEDIUM_TICK_START, MEDIUM_TICK_STOP, MINOR_DIAMETER);

                    } else {
                        drawLinearTicks(G2, WIDTH, HEIGHT, ORIENTATION, currentPos, MINOR_TICKMARK_TYPE, TICK_LINE, TICK_CIRCLE, TICK_TRIANGLE, MINOR_TICK_START, MINOR_TICK_STOP, MINOR_DIAMETER);
                    }
                }
            }
        } else {
            // Logarithmic scaling
            if (TICKS_VISIBLE) {
                double offset;
                double limit;
                if (ORIENTATION == Orientation.VERTICAL) {
                    offset = SCALE_BOUNDS.getMaxY();
                    limit = SCALE_BOUNDS.getMinY();
                } else {
                    offset = SCALE_BOUNDS.getMinX();
                    limit = SCALE_BOUNDS.getMaxX();
                }

                double stepSize = 1;
                int exponent = 0;
                double factor = Math.abs(limit - offset) / (UTIL.logOfBase(BASE, MAX_VALUE));
                double pos;
                Line2D tick = new Line2D.Double();
                if (TICKMARK_COLOR_FROM_THEME) {
                    G2.setColor(BACKGROUND_COLOR.LABEL_COLOR);
                } else {
                    G2.setColor(TICKMARK_COLOR);
                }
                for (int counter = 0 ; counter <= MAX_VALUE ; counter += stepSize) {
                    G2.setStroke(MEDIUM_TICKMARK_STROKE);
                    if (ORIENTATION == Orientation.VERTICAL) {
                        pos = offset - factor * UTIL.logOfBase(BASE, counter);
                        tick.setLine(MEDIUM_TICK_START, pos, MEDIUM_TICK_STOP, pos);
                    } else {
                        pos = factor * UTIL.logOfBase(BASE, counter) + offset;
                        tick.setLine(pos, MEDIUM_TICK_START, pos, MEDIUM_TICK_STOP);
                    }

                    if (counter == (int)Math.pow(BASE, exponent + 1)) {
                        exponent++;
                        stepSize = Math.pow(BASE, exponent);
                        G2.setStroke(MAJOR_TICKMARK_STROKE);
                        if (ORIENTATION == Orientation.VERTICAL) {
                            tick.setLine(MAJOR_TICK_START, pos, MAJOR_TICK_STOP, pos);
                        } else {
                            tick.setLine(pos, MAJOR_TICK_START, pos, MAJOR_TICK_STOP);
                        }
                        // Ticklabels
                        if (TICKLABELS_VISIBLE) {
                            currentLayout = new TextLayout(numberFormat.format(counter), G2.getFont(), RENDER_CONTEXT);
                            currentBounds = currentLayout.getBounds();
                            if (ORIENTATION == Orientation.VERTICAL) {
                                // Vertical orientation
                                textOffset = (float) (MAX_BOUNDS.getWidth() - currentBounds.getWidth());
                                G2.drawString(numberFormat.format(counter), 0.18f * WIDTH + textOffset, (float) (pos - currentBounds.getHeight() / 2.0 + currentBounds.getHeight()));
                            } else {
                                // Horizontal orientation
                                G2.drawString(numberFormat.format(counter), (float) (pos - currentBounds.getWidth() / 3.0), (float) (HEIGHT * 0.68 + 1.5 * currentBounds.getHeight()));
                            }
                        }
                    }
                    G2.draw(tick);
                }
            }
        }

        G2.dispose();

        if (BACKGROUND_IMAGE != null) {
            final Graphics2D G = BACKGROUND_IMAGE.createGraphics();
            G.drawImage(imageBufferLin, 0, 0, null);
            G.dispose();
        }

        // Buffer the current parameters
        widthBufferLin = WIDTH;
        heightBufferLin = HEIGHT;
        minValueBufferLin = MIN_VALUE;
        maxValueBufferLin = MAX_VALUE;
        noOfMinorTicksBufferLin = NO_OF_MINOR_TICKS;
        noOfMajorTicksBufferLin = NO_OF_MAJOR_TICKS;
        minorTickSpacingBufferLin = MINOR_TICK_SPACING;
        majorTickSpacingBufferLin = MAJOR_TICK_SPACING;
        minorTickmarkTypeBufferLin = MINOR_TICKMARK_TYPE;
        majorTickmarkTypeBufferLin = MAJOR_TICKMARK_TYPE;
        ticksVisibleBufferLin = TICKS_VISIBLE;
        ticklabelsVisibleBufferLin = TICKLABELS_VISIBLE;
        minorTicksVisibleBufferLin = MINOR_TICKS_VISIBLE;
        majorTicksVisibleBufferLin = MAJOR_TICKS_VISIBLE;
        numberFormatBufferLin = NUMBER_FORMAT;
        tickmarkSectionsVisibleBufferLin = TICKMARK_SECTIONS_VISIBLE;
        backgroundColorBufferLin = BACKGROUND_COLOR;
        tickmarkColorBufferLin = TICKMARK_COLOR;
        tickmarkColorFromThemeBufferLin = TICKMARK_COLOR_FROM_THEME;
        if (tickmarkSections != null) {
            tickmarkSectionsBufferLin.clear();
            tickmarkSectionsBufferLin.addAll(tickmarkSections);
        }
        if (OFFSET != null) {
            offsetBufferLin.setLocation(OFFSET);
        }
        orientationBufferLin = ORIENTATION;
        niceScaleLin = NICE_SCALE;
        logScaleLin = LOG_SCALE;

        return imageBufferLin;
    }

    private void drawLinearTicks(final Graphics2D G2,
                                 final int WIDTH,
                                 final int HEIGHT,
                                 final Orientation ORIENTATION,
                                 final double CURRENT_POS,
                                 final TickmarkType TICKMARK_TYPE,
                                 final Line2D TICK_LINE,
                                 final Ellipse2D TICK_CIRCLE,
                                 final GeneralPath TICK_TRIANGLE,
                                 final double TICK_START,
                                 final double TICK_STOP,
                                 final double DIAMETER) {
        switch (TICKMARK_TYPE) {
            case CIRCLE:
                if (ORIENTATION == Orientation.VERTICAL) {
                    TICK_CIRCLE.setFrame(TICK_START, CURRENT_POS - DIAMETER / 2.0, DIAMETER, DIAMETER);
                } else {
                    TICK_CIRCLE.setFrame(CURRENT_POS - DIAMETER / 2.0, TICK_STOP, DIAMETER, DIAMETER);
                }
                G2.fill(TICK_CIRCLE);
                break;
            case TRIANGLE:
                TICK_TRIANGLE.reset();
                if (ORIENTATION == Orientation.VERTICAL) {
                    // Vertical orientation
                    TICK_TRIANGLE.moveTo(TICK_START, CURRENT_POS + WIDTH * 0.005);
                    TICK_TRIANGLE.lineTo(TICK_START, CURRENT_POS - WIDTH * 0.005);
                    TICK_TRIANGLE.lineTo(TICK_STOP, CURRENT_POS);
                    TICK_TRIANGLE.closePath();
                } else {
                    // Horizontal orientation
                    TICK_TRIANGLE.moveTo(CURRENT_POS - HEIGHT * 0.005, TICK_START);
                    TICK_TRIANGLE.lineTo(CURRENT_POS + HEIGHT * 0.005, TICK_START);
                    TICK_TRIANGLE.lineTo(CURRENT_POS, TICK_STOP);
                    TICK_TRIANGLE.closePath();
                }
                G2.fill(TICK_TRIANGLE);
                break;

            case LINE:

            default:
                if (ORIENTATION == Orientation.VERTICAL) {
                    // Vertical orientation
                    TICK_LINE.setLine(TICK_START, CURRENT_POS, TICK_STOP, CURRENT_POS);
                } else {
                    // Horizontal orientation
                    TICK_LINE.setLine(CURRENT_POS, TICK_START, CURRENT_POS, TICK_STOP);
                }
                G2.draw(TICK_LINE);
                break;
        }
    }
    // </editor-fold>
}
