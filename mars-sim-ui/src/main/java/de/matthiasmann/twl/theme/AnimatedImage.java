/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.matthiasmann.twl.theme;

import de.matthiasmann.twl.Border;
import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.renderer.AnimationState;
import de.matthiasmann.twl.renderer.AnimationState.StateKey;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.renderer.Renderer;

/**
 *
 * @author mam
 */
public class AnimatedImage implements Image, HasBorder {

    static abstract class Element {
        int duration;

        abstract int getWidth();
        abstract int getHeight();
        abstract Img getFirstImg();
        abstract void render(int time, Img next, int x, int y,
                int width, int height, AnimatedImage ai, AnimationState as);
    }

    static class Img extends Element {
        final Image image;
        final float r;
        final float g;
        final float b;
        final float a;
        final float zoomX;
        final float zoomY;
        final float zoomCenterX;
        final float zoomCenterY;

        Img(int duration, Image image, Color tintColor, float zoomX, float zoomY, float zoomCenterX, float zoomCenterY) {
            if(duration < 0) {
                throw new IllegalArgumentException("duration");
            }
            this.duration = duration;
            this.image = image;
            this.r = tintColor.getRedFloat();
            this.g = tintColor.getGreenFloat();
            this.b = tintColor.getBlueFloat();
            this.a = tintColor.getAlphaFloat();
            this.zoomX = zoomX;
            this.zoomY = zoomY;
            this.zoomCenterX = zoomCenterX;
            this.zoomCenterY = zoomCenterY;
        }

        int getWidth() {
            return image.getWidth();
        }

        int getHeight() {
            return image.getHeight();
        }

        Img getFirstImg() {
            return this;
        }

        void render(int time, Img next, int x, int y, int width, int height, AnimatedImage ai, AnimationState as) {
            float rr=r, gg=g, bb=b, aa=a;
            float zx=zoomX, zy=zoomY, cx=zoomCenterX, cy=zoomCenterY;
            if(next != null) {
                float t = time / (float)duration;
                rr = blend(rr, next.r, t);
                gg = blend(gg, next.g, t);
                bb = blend(bb, next.b, t);
                aa = blend(aa, next.a, t);
                zx = blend(zx, next.zoomX, t);
                zy = blend(zy, next.zoomY, t);
                cx = blend(cx, next.zoomCenterX, t);
                cy = blend(cy, next.zoomCenterY, t);
            }
            ai.renderer.pushGlobalTintColor(rr*ai.r, gg*ai.g, bb*ai.b, aa*ai.a);
            try {
                int zWidth = (int)(width * zx);
                int zHeight = (int)(height * zy);
                image.draw(as,
                        x + (int)((width - zWidth) * cx),
                        y + (int)((height - zHeight) * cy),
                        zWidth, zHeight);
            } finally {
                ai.renderer.popGlobalTintColor();
            }
        }

        private static float blend(float a, float b, float t) {
            return a + (b-a) * t;
        }
    }

    static class Repeat extends Element {
        final Element[] children;
        final int repeatCount;
        final int singleDuration;

        Repeat(Element[] children, int repeatCount) {
            this.children = children;
            this.repeatCount = repeatCount;
            assert repeatCount >= 0;
            assert children.length > 0;

            for(Element e : children) {
                duration += e.duration;
            }
            singleDuration = duration;
            if(repeatCount == 0) {
                duration = Integer.MAX_VALUE;
            } else {
                duration *= repeatCount;
            }
        }

        @Override
        int getHeight() {
            int tmp = 0;
            for(Element e : children) {
                tmp = Math.max(tmp, e.getHeight());
            }
            return tmp;
        }

        @Override
        int getWidth() {
            int tmp = 0;
            for(Element e : children) {
                tmp = Math.max(tmp, e.getWidth());
            }
            return tmp;
        }

        Img getFirstImg() {
            return children[0].getFirstImg();
        }

        void render(int time, Img next, int x, int y, int width, int height, AnimatedImage ai, AnimationState as) {
            if(singleDuration == 0) {
                // animation data is invalid - don't crash
                return;
            }
            
            int iteration = 0;
            if(repeatCount == 0) {
                time %= singleDuration;
            } else {
                iteration = time / singleDuration;
                time -= Math.min(iteration, repeatCount-1) * singleDuration;
            }

            Element e = null;
            for(int i=0 ; i<children.length ; i++) {
                e = children[i];
                if(time < e.duration && e.duration > 0) {
                    if(i+1 < children.length) {
                        next = children[i+1].getFirstImg();
                    } else if(repeatCount == 0 || iteration+1 < repeatCount) {
                        next = getFirstImg();
                    }
                    break;
                }

                time -= e.duration;
            }

            if(e != null) {
                e.render(time, next, x, y, width, height, ai, as);
            }
        }
    }

    final Renderer renderer;
    final Element root;
    final StateKey timeSource;
    final Border border;
    final float r;
    final float g;
    final float b;
    final float a;
    final int width;
    final int height;
    final int frozenTime;

    AnimatedImage(Renderer renderer, Element root, String timeSource, Border border, Color tintColor,int frozenTime) {
        this.renderer = renderer;
        this.root = root;
        this.timeSource = StateKey.get(timeSource);
        this.border = border;
        this.r = tintColor.getRedFloat();
        this.g = tintColor.getGreenFloat();
        this.b = tintColor.getBlueFloat();
        this.a = tintColor.getAlphaFloat();
        this.width = root.getWidth();
        this.height = root.getHeight();
        this.frozenTime = frozenTime;
    }

    AnimatedImage(AnimatedImage src, Color tintColor) {
        this.renderer = src.renderer;
        this.root = src.root;
        this.timeSource = src.timeSource;
        this.border = src.border;
        this.r = src.r * tintColor.getRedFloat();
        this.g = src.g * tintColor.getGreenFloat();
        this.b = src.b * tintColor.getBlueFloat();
        this.a = src.a * tintColor.getAlphaFloat();
        this.width = src.width;
        this.height = src.height;
        this.frozenTime = src.frozenTime;
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
        int time = 0;
        if(as != null) {
            if(frozenTime < 0 || as.getShouldAnimateState(timeSource)) {
                time = as.getAnimationTime(timeSource);
            } else {
                time = frozenTime;
            }
        }
        root.render(time, null, x, y, width, height, this, as);
    }

    public Border getBorder() {
        return border;
    }

    public Image createTintedVersion(Color color) {
        return new AnimatedImage(this, color);
    }

}
