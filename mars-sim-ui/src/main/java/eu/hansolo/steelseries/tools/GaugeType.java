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

import java.awt.geom.Rectangle2D;


/**
 *
 * @author Gerrit Grunwald <han.solo at muenster.de>
 */
public enum GaugeType {

    TYPE1(0, (1.5 * Math.PI), (0.5 * Math.PI), (Math.PI * 0.5), (Math.PI / 2.0), 180, 90, 0, new Rectangle2D.Double(0.55, 0.55, 0.55, 0.15), PostPosition.CENTER, PostPosition.MAX_CENTER_TOP, PostPosition.MIN_LEFT),
    TYPE2(0, (1.5 * Math.PI), (0.5 * Math.PI), (Math.PI * 0.5), Math.PI, 180, 180, 0, new Rectangle2D.Double(0.55, 0.55, 0.55, 0.15), PostPosition.CENTER, PostPosition.MIN_LEFT, PostPosition.MAX_RIGHT),
    TYPE3(0, Math.PI, 0, Math.PI, (1.5 * Math.PI), 270, 270, -90, new Rectangle2D.Double(0.4, 0.55, 0.4, 0.15), PostPosition.CENTER, PostPosition.MAX_CENTER_BOTTOM, PostPosition.MAX_RIGHT),
    TYPE4((Math.toRadians(60)), (Math.PI + Math.toRadians(30)), 0, Math.PI - Math.toRadians(30), Math.toRadians(300), 240, 300, -60, new Rectangle2D.Double(0.4, 0.55, 0.4, 0.15), PostPosition.CENTER, PostPosition.MIN_BOTTOM, PostPosition.MAX_BOTTOM),
    TYPE5(0, (1.75 * Math.PI), (0.75 * Math.PI), (Math.PI * 0.5), (Math.PI * 0.5), 180, 90, 0, new Rectangle2D.Double(0.55, 0.55, 0.55, 0.15), PostPosition.LOWER_CENTER, PostPosition.SMALL_GAUGE_MIN_LEFT, PostPosition.SMALL_GAUGE_MAX_RIGHT);
    final public double FREE_AREA_ANGLE;
    final public double ROTATION_OFFSET;
    final public double TICKMARK_OFFSET;
    final public double TICKLABEL_ORIENTATION_CHANGE_ANGLE;
    final public double ANGLE_RANGE;
    final public double ORIGIN_CORRECTION;
    final public double APEX_ANGLE;
    final public double BARGRAPH_OFFSET;
    final public PostPosition[] POST_POSITIONS;
    final public Rectangle2D LCD_FACTORS;

    private GaugeType(final double FREE_AREA_ANGLE, final double ROTATION_OFFSET, final double TICKMARK_OFFSET, final double TICKLABEL_ORIENTATION_CHANGE_ANGLE,
                      final double ANGLE_RANGE, final double ORIGIN_CORRECTION, final double APEX_ANGLE,
                      final double BARGRAPH_OFFSET, final Rectangle2D LCD_FACTORS,
                      final PostPosition... POST_POSITIONS) {
        this.FREE_AREA_ANGLE = FREE_AREA_ANGLE;
        this.ROTATION_OFFSET = ROTATION_OFFSET;
        this.TICKMARK_OFFSET = TICKMARK_OFFSET;
        this.TICKLABEL_ORIENTATION_CHANGE_ANGLE = TICKLABEL_ORIENTATION_CHANGE_ANGLE;
        this.ANGLE_RANGE = ANGLE_RANGE;
        this.ORIGIN_CORRECTION = ORIGIN_CORRECTION;
        this.APEX_ANGLE = APEX_ANGLE;
        this.BARGRAPH_OFFSET = BARGRAPH_OFFSET;
        this.POST_POSITIONS = POST_POSITIONS;
        this.LCD_FACTORS = LCD_FACTORS;
    }
}
