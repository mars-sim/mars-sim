/*
 * Copyright (c) 2015 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.enzo.common;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Shape;

import java.util.Random;
import java.util.stream.IntStream;


/**
 * Created by
 * User: hansolo
 * Date: 01.03.13
 * Time: 11:16
 */
public class BrushedMetalPaint {
    private int     radius;
    private double  amount;
    private int     color;
    private double  shine;
    private boolean monochrome;
    private Random randomNumbers;


    // ******************** Constructors **************************************
    public BrushedMetalPaint() {
        this(Color.rgb(136, 136, 136), 5, 0.1, true, 0.3);
    }

    public BrushedMetalPaint(final Color COLOR) {
        this(COLOR, 5, 0.1, true, 0.3);
    }

    public BrushedMetalPaint(final Color COLOR, final int RADIUS, final double AMOUNT, final boolean MONOCHROME, final double SHINE) {
        color      = getIntFromColor(COLOR);
        radius     = RADIUS;
        amount     = AMOUNT;
        monochrome = MONOCHROME;
        shine      = SHINE;
    }


    // ******************** Methods *******************************************
    public Image getImage(final double W, final double H) {
        final int WIDTH  = (int) W;
        final int HEIGHT = (int) H;

        WritableImage DESTINATION = new WritableImage(WIDTH, HEIGHT);

        final int[] IN_PIXELS  = new int[WIDTH];
        final int[] OUT_PIXELS = new int[WIDTH];

        randomNumbers   = new Random(0);
        final int ALPHA = color & 0xff000000;
        final int RED   = (color >> 16) & 0xff;
        final int GREEN = (color >> 8) & 0xff;
        final int BLUE  = color & 0xff;

        IntStream.range(0, HEIGHT).parallel().forEachOrdered(
            y -> {
                IntStream.range(0, WIDTH).parallel().forEachOrdered(
                    x -> {
                        int tr = RED;
                        int tg = GREEN;
                        int tb = BLUE;
                        if (shine != 0) {
                            int f = (int) (255 * shine * Math.sin((double) x / WIDTH * Math.PI));
                            tr += f;
                            tg += f;
                            tb += f;
                        }
                        if (monochrome) {
                            int n = (int) (255 * (2 * randomNumbers.nextFloat() - 1) * amount);
                            IN_PIXELS[x] = ALPHA | (clamp(tr + n) << 16) | (clamp(tg + n) << 8) | clamp(tb + n);
                        } else {
                            IN_PIXELS[x] = ALPHA | (random(tr) << 16) | (random(tg) << 8) | random(tb);
                        }
                    }
                );
                if (radius != 0) {
                    blur(IN_PIXELS, OUT_PIXELS, WIDTH, radius);
                    setRGB(DESTINATION, 0, y, OUT_PIXELS);
                } else {
                    setRGB(DESTINATION, 0, y, IN_PIXELS);
                }
            }
        );                
        return DESTINATION;
    }

    public ImageView getImageView(final double W, final double H, final Shape CLIP) {
        final Image IMAGE = getImage(W, H);
        final ImageView IMAGE_VIEW = new ImageView(IMAGE);
        IMAGE_VIEW.setClip(CLIP);
        return IMAGE_VIEW;
    }

    public ImagePattern apply(final Shape SHAPE) {
        double x      = SHAPE.getLayoutBounds().getMinX();
        double y      = SHAPE.getLayoutBounds().getMinY();
        double width  = SHAPE.getLayoutBounds().getWidth();
        double height = SHAPE.getLayoutBounds().getHeight();
        return new ImagePattern(getImage(width, height), x, y, width, height, false);
    }

    public void blur(final int[] IN, final int[] OUT, final int WIDTH, final int RADIUS) {
        final int WIDTH_MINUS_1 = WIDTH - 1;
        final int R2 = 2 * RADIUS + 1;
        int tr = 0, tg = 0, tb = 0;

        for (int i = -RADIUS; i <= RADIUS; i++) {
            int rgb = IN[mod(i, WIDTH)];
            tr += (rgb >> 16) & 0xff;
            tg += (rgb >> 8) & 0xff;
            tb += rgb & 0xff;
        }        

        for (int x = 0; x < WIDTH; x++) {
            OUT[x] = 0xff000000 | ((tr / R2) << 16) | ((tg / R2) << 8) | (tb / R2);

            int i1 = x + RADIUS + 1;
            if (i1 > WIDTH_MINUS_1) {
                i1 = mod(i1, WIDTH);
            }
            int i2 = x - RADIUS;
            if (i2 < 0) {
                i2 = mod(i2, WIDTH);
            }
            int rgb1 = IN[i1];
            int rgb2 = IN[i2];

            tr += ((rgb1 & 0xff0000) - (rgb2 & 0xff0000)) >> 16;
            tg += ((rgb1 & 0xff00) - (rgb2 & 0xff00)) >> 8;
            tb += (rgb1 & 0xff) - (rgb2 & 0xff);
        }
    }

    public void setRadius(final int RADIUS) {
        radius = RADIUS;
    }
    public int getRadius() {
        return radius;
    }

    public void setAmount(final double AMOUNT) {
        amount = AMOUNT;
    }
    public double getAmount() {
        return amount;
    }

    public void setColor(final int COLOR) {
        color = COLOR;
    }
    public int getColor() {
        return color;
    }

    public void setMonochrome(final boolean MONOCHROME) {
        monochrome = MONOCHROME;
    }
    public boolean isMonochrome() {
        return monochrome;
    }

    public void setShine(final double SHINE) {
        shine = SHINE;
    }
    public double getShine() {
        return shine;
    }

    private int random(int x) {
        x += (int) (255 * (2 * randomNumbers.nextFloat() - 1) * amount);
        if (x < 0) {
            x = 0;
        } else if (x > 0xff) {
            x = 0xff;
        }
        return x;
    }

    private int clamp(final int C) {
        int ret = C;
        if (C < 0) {
            ret = 0;
        }
        if (C > 255) {
            ret = 255;
        }
        return ret;
    }

    private int mod(int a, final int B) {
        final int N = a / B;
        a -= N * B;
        if (a < 0) {
            return a + B;
        }
        return a;
    }

    private void setRGB(final WritableImage IMAGE, final int X, final int Y, final int[] PIXELS) {
        final PixelWriter RASTER = IMAGE.getPixelWriter();
        for (int x = 0 ; x < PIXELS.length ; x++) {
            RASTER.setColor(X + x, Y, Color.rgb((PIXELS[x] >> 16) & 0xFF, (PIXELS[x] >> 8) & 0xFF, (PIXELS[x] &0xFF)));
        }
    }

    private int getIntFromColor(final Color COLOR) {
        String hex = COLOR.toString();
        StringBuilder intValue = new StringBuilder(10);
        intValue.append(hex.substring(8, 10));
        intValue.append(hex.substring(2, 8));
        return (int) Long.parseLong(intValue.toString(), 16);
    }
}
