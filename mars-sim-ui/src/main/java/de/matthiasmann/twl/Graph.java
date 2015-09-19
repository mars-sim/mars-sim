/*
 * Copyright (c) 2008-2010, Matthias Mann
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Matthias Mann nor the names of its contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.matthiasmann.twl;

import de.matthiasmann.twl.utils.TextUtil;
import de.matthiasmann.twl.model.GraphLineModel;
import de.matthiasmann.twl.model.GraphModel;
import de.matthiasmann.twl.renderer.LineRenderer;
import java.util.Arrays;

/**
 *
 * @author Matthias Mann
 */
public class Graph extends Widget {

    private final GraphArea area;

    GraphModel model;
    private ParameterMap themeLineStyles;
    private int sizeMultipleX = 1;
    private int sizeMultipleY = 1;

    LineStyle[] lineStyles = new LineStyle[8];
    private float[] renderXYBuffer = new float[128];

    public Graph() {
        area = new GraphArea();
        area.setClip(true);
        add(area);
    }

    public Graph(GraphModel model) {
        this();
        setModel(model);
    }
    
    public GraphModel getModel() {
        return model;
    }

    public void setModel(GraphModel model) {
        this.model = model;
        invalidateLineStyles();
    }

    public int getSizeMultipleX() {
        return sizeMultipleX;
    }

    public void setSizeMultipleX(int sizeMultipleX) {
        if(sizeMultipleX < 1) {
            throw new IllegalArgumentException("sizeMultipleX must be >= 1");
        }
        this.sizeMultipleX = sizeMultipleX;
    }

    public int getSizeMultipleY() {
        return sizeMultipleY;
    }

    public void setSizeMultipleY(int sizeMultipleY) {
        if(sizeMultipleY < 1) {
            throw new IllegalArgumentException("sizeMultipleX must be >= 1");
        }
        this.sizeMultipleY = sizeMultipleY;
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        applyThemeGraph(themeInfo);
    }

    protected void applyThemeGraph(ThemeInfo themeInfo) {
        this.themeLineStyles = themeInfo.getParameterMap("lineStyles");
        setSizeMultipleX(themeInfo.getParameter("sizeMultipleX", 1));
        setSizeMultipleY(themeInfo.getParameter("sizeMultipleY", 1));
        invalidateLineStyles();
    }

    protected void invalidateLineStyles() {
        Arrays.fill(lineStyles, null);
    }

    void syncLineStyles() {
        int numLines = model.getNumLines();
        if(lineStyles.length < numLines) {
            LineStyle[] newLineStyles = new LineStyle[numLines];
            System.arraycopy(lineStyles, 0, newLineStyles, 0, lineStyles.length);
            this.lineStyles = newLineStyles;
        }

        for(int i=0 ; i<numLines ; i++) {
            GraphLineModel line = model.getLine(i);
            LineStyle style = lineStyles[i];
            if(style == null) {
                style = new LineStyle();
                lineStyles[i] = style;
            }
            String visualStyle = TextUtil.notNull(line.getVisualStyleName());
            if(!style.name.equals(visualStyle)) {
                ParameterMap lineStyle = null;
                if(themeLineStyles != null) {
                    lineStyle = themeLineStyles.getParameterMap(visualStyle);
                }
                style.setStyleName(visualStyle, lineStyle);
            }
        }
    }

    private static final float EPSILON = 1e-4f;

    void renderLine(LineRenderer lineRenderer, GraphLineModel line,
            float minValue, float maxValue, LineStyle style) {
        int numPoints = line.getNumPoints();
        if(numPoints <= 0) {
            // nothing to render
            return;
        }

        if(renderXYBuffer.length < numPoints*2) {
            // no need to copy - we generate new values anyway
            renderXYBuffer = new float[numPoints*2];
        }

        float[] xy = this.renderXYBuffer;

        float delta = maxValue - minValue;
        if(Math.abs(delta) < EPSILON) {
            // Math.copySign is Java 1.6+
            delta = copySign(EPSILON, delta);
        }

        float yscale = (float)-getInnerHeight() / delta;
        float yoff = getInnerBottom();
        float xscale = (float)getInnerWidth() / (float)Math.max(1, numPoints-1);
        float xoff = getInnerX();

        for(int i=0 ; i<numPoints ; i++) {
            float value = line.getPoint(i);
            xy[i*2 + 0] = i * xscale + xoff;
            xy[i*2 + 1] = (value - minValue) * yscale + yoff;
        }

        if(numPoints == 1) {
            // a single point will be rendered as horizontal line
            // as we never shrink the xy array and the initial size is >= 4 we have enough room left
            xy[2] = xoff + xscale;
            xy[3] = xy[1];
            numPoints = 2;
        }

        lineRenderer.drawLine(xy, numPoints, style.lineWidth, style.color, false);
    }

    private static float copySign(float magnitude, float sign) {
        // this copies the sign bit from sign to magnitude
        // it assumes the magnitude is positive
        int rawMagnitude = Float.floatToRawIntBits(magnitude);
        int rawSign = Float.floatToRawIntBits(sign);
        int rawResult = rawMagnitude | (rawSign & (1 << 31));
        return Float.intBitsToFloat(rawResult);
    }

    @Override
    public boolean setSize(int width, int height) {
        return super.setSize(
                round(width, sizeMultipleX),
                round(height, sizeMultipleY));
    }

    private static int round(int value, int grid) {
        return value - (value % grid);
    }

    @Override
    protected void layout() {
        layoutChildFullInnerArea(area);
    }
    
    static class LineStyle {
        String name = "";
        Color color = Color.WHITE;
        float lineWidth = 1.0f;

        void setStyleName(String name, ParameterMap lineStyle) {
            this.name = name;
            if(lineStyle != null) {
                this.color = lineStyle.getParameter("color", Color.WHITE);
                this.lineWidth = Math.max(EPSILON, lineStyle.getParameter("width", 1.0f));
            }
        }
    }

    class GraphArea extends Widget {

        @Override
        protected void paintWidget(GUI gui) {
            if(model != null) {
                syncLineStyles();
                LineRenderer lineRenderer = gui.getRenderer().getLineRenderer();

                int numLines = model.getNumLines();
                boolean independantScale = model.getScaleLinesIndependant();
                float minValue = Float.MAX_VALUE;
                float maxValue = -Float.MAX_VALUE;
                if(independantScale) {
                    for(int i=0 ; i<numLines ; i++) {
                        GraphLineModel line = model.getLine(i);
                        minValue = Math.min(minValue, line.getMinValue());
                        maxValue = Math.max(maxValue, line.getMaxValue());
                    }
                }

                for(int i=0 ; i<numLines ; i++) {
                    GraphLineModel line = model.getLine(i);
                    LineStyle style = lineStyles[i];
                    if(independantScale) {
                        renderLine(lineRenderer, line, minValue, maxValue, style);
                    } else {
                        renderLine(lineRenderer, line, line.getMinValue(), line.getMaxValue(), style);
                    }
                }
            }
        }

    }
}
