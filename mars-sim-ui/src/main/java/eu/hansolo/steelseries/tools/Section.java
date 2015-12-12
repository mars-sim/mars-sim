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
package eu.hansolo.steelseries.tools;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;


/**
 *
 * @author hansolo
 */
public class Section {
    // <editor-fold defaultstate="collapsed" desc="Variable declarations">
    private double start;
    private double stop;
    private Color color;
    private Color transparentColor;
    private Color highlightColor;
    private Color transparentHighlightColor;
    private Paint paint;
    private Area sectionArea;
    private Arc2D filledArea;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public Section() {
        this(-1, -1, Color.RED, null, null);
    }

    public Section(final double START, final double STOP, final Color COLOR) {
        this(START, STOP, COLOR, null, null);
    }

    public Section(final double START, final double STOP, final Color COLOR, final Color HIGHLIGHT_COLOR) {
        this(START, STOP, COLOR, HIGHLIGHT_COLOR, null, null);
    }

    public Section(final double START, final double STOP, final Color COLOR, final Arc2D FILLED_AREA) {
        this(START, STOP, COLOR, null, FILLED_AREA);
    }

    public Section(final double START, final double STOP, final Color COLOR, final Area SECTION_AREA, final Arc2D FILLED_AREA) {
        this(START, STOP, COLOR, COLOR.brighter().brighter(), SECTION_AREA, FILLED_AREA);
    }

    public Section(final double START, final double STOP, final Color COLOR, final Color HIGHLIGHT_COLOR, final Area SECTION_AREA, final Arc2D FILLED_AREA) {
        start = START;
        stop = STOP;
        color = COLOR;
        transparentColor = Util.INSTANCE.setAlpha(COLOR, 0.25f);
        highlightColor = HIGHLIGHT_COLOR;
        transparentHighlightColor = Util.INSTANCE.setAlpha(HIGHLIGHT_COLOR, 0.5f);
        sectionArea = SECTION_AREA;
        filledArea = FILLED_AREA;
        paint = COLOR;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    public double getStart() {
        return start;
    }

    public void setStart(final double START) {
        start = START;
    }

    public double getStop() {
        return stop;
    }

    public void setStop(final double STOP) {
        stop = STOP;
    }

    public Color getColor() {
        return color;
    }

    public Color getTransparentColor() {
        return transparentColor;
    }

    public void setColor(final Color COLOR) {
        color = COLOR;
        transparentColor = Util.INSTANCE.setAlpha(COLOR, 0.25f);
    }

    public Color getHighlightColor() {
        return highlightColor;
    }

    public Color getTransparentHighlightColor() {
        return transparentHighlightColor;
    }

    public void setHighlightColor(final Color HIGHLIGHT_COLOR) {
        highlightColor = HIGHLIGHT_COLOR;
        transparentHighlightColor = Util.INSTANCE.setAlpha(HIGHLIGHT_COLOR, 0.5f);
    }

    public Area getSectionArea() {
        return sectionArea;
    }

    public void setSectionArea(final Area SECTION_AREA) {
        sectionArea = SECTION_AREA;
    }

    public Arc2D getFilledArea() {
        return filledArea;
    }

    public void setFilledArea(final Arc2D FILLED_AREA) {
        filledArea = FILLED_AREA;
    }

    public Paint getPaint() {
        return paint;
    }

    public void setPaint(final Paint PAINT) {
        paint = PAINT;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Misc">
    public boolean contains(final double VALUE) {
        return ((Double.compare(VALUE, start) >= 0 && Double.compare(VALUE, stop) <= 0)) ? true : false;
    }
    // </editor-fold>

    @Override
    public String toString() {
        return "Section";
    }
}
