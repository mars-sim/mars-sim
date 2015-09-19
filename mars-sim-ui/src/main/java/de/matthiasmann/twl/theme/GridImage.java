/*
 * Copyright (c) 2008, Matthias Mann
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
package de.matthiasmann.twl.theme;

import de.matthiasmann.twl.Border;
import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.renderer.AnimationState;
import de.matthiasmann.twl.renderer.Image;

/**
 *
 * @author Matthias Mann
 */
public class GridImage implements Image, HasBorder {

    private final Image[] images;
    private final int[] weightX;
    private final int[] weightY;
    private final Border border;
    private final int width;
    private final int height;
    private final int columnWidth[];
    private final int rowHeight[];
    private final int weightSumX;
    private final int weightSumY;

    GridImage(Image[] images, int[] weightX, int[] weightY, Border border) {
        if(weightX.length == 0 || weightY.length == 0) {
            throw new IllegalArgumentException("zero dimension size not allowed");
        }
        assert weightX.length * weightY.length == images.length;
        this.images = images;
        this.weightX = weightX;
        this.weightY = weightY;
        this.border = border;
        this.columnWidth = new int[weightX.length];
        this.rowHeight = new int[weightY.length];

        int widthTmp = 0;
        for(int x=0 ; x<weightX.length ; x++) {
            int widthColumn = 0;
            for(int y=0 ; y<weightY.length ; y++) {
                widthColumn = Math.max(widthColumn, getImage(x, y).getWidth());
            }
            widthTmp += widthColumn;
            columnWidth[x] = widthColumn;
        }
        this.width = widthTmp;

        int heightTmp = 0;
        for(int y=0 ; y<weightY.length ; y++) {
            int heightRow = 0;
            for(int x=0 ; x<weightX.length ; x++) {
                heightRow = Math.max(heightRow, getImage(x, y).getHeight());
            }
            heightTmp += heightRow;
            rowHeight[y] = heightRow;
        }
        this.height = heightTmp;

        int tmpSumX = 0;
        for(int weight : weightX) {
            if(weight < 0) {
                throw new IllegalArgumentException("negative weight in weightX");
            }
            tmpSumX += weight;
        }
        weightSumX = tmpSumX;

        int tmpSumY = 0;
        for(int weight : weightY) {
            if(weight < 0) {
                throw new IllegalArgumentException("negative weight in weightY");
            }
            tmpSumY += weight;
        }
        weightSumY = tmpSumY;

        if(weightSumX <= 0) {
            throw new IllegalArgumentException("zero weightX not allowed");
        }
        if(weightSumY <= 0) {
            throw new IllegalArgumentException("zero weightX not allowed");
        }
    }

    private GridImage(Image[] images, GridImage src) {
        this.images = images;
        this.weightX = src.weightX;
        this.weightY = src.weightY;
        this.border = src.border;
        this.columnWidth = src.columnWidth;
        this.rowHeight = src.rowHeight;
        this.weightSumX = src.weightSumX;
        this.weightSumY = src.weightSumY;
        this.width = src.width;
        this.height = src.height;
    }


    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void draw(AnimationState as, int x, int y) {
        draw(as, x, y, width, height);
    }

    public void draw(AnimationState as, int x, int y, int width, int height) {
        int deltaY = height - this.height;
        int remWeightY = weightSumY;
        for(int yi=0,idx=0 ; yi<weightY.length ; yi++) {
            int heightRow = rowHeight[yi];
            if(remWeightY > 0) {
                int partY = deltaY * weightY[yi] / remWeightY;
                remWeightY -= weightY[yi];
                heightRow += partY;
                deltaY -= partY;
            }

            int tmpX = x;
            int deltaX = width - this.width;
            int remWeightX = weightSumX;
            for(int xi=0 ; xi<weightX.length ; xi++,idx++) {
                int widthColumn = columnWidth[xi];
                if(remWeightX > 0) {
                    int partX = deltaX * weightX[xi] / remWeightX;
                    remWeightX -= weightX[xi];
                    widthColumn += partX;
                    deltaX -= partX;
                }

                images[idx].draw(as, tmpX, y, widthColumn, heightRow);
                tmpX += widthColumn;
            }

            y += heightRow;
        }
    }

    public Border getBorder() {
        return border;
    }

    public Image createTintedVersion(Color color) {
        Image[] newImages = new Image[images.length];
        for(int i=0 ; i<newImages.length ; i++) {
            newImages[i] = images[i].createTintedVersion(color);
        }
        return new GridImage(newImages, this);
    }

    private Image getImage(int x, int y) {
        return images[x + y * weightX.length];
    }
}
