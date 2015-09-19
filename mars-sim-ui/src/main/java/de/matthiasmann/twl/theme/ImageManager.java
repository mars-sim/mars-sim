/*
 * Copyright (c) 2008-2013, Matthias Mann
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
import de.matthiasmann.twl.renderer.Gradient;
import de.matthiasmann.twl.renderer.MouseCursor;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.renderer.Renderer;
import de.matthiasmann.twl.renderer.Texture;
import de.matthiasmann.twl.utils.AbstractMathInterpreter;
import de.matthiasmann.twl.utils.StateExpression;
import de.matthiasmann.twl.utils.StateSelect;
import de.matthiasmann.twl.utils.TextUtil;
import de.matthiasmann.twl.utils.XMLParser;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author Matthias Mann
 */
class ImageManager {

    final ParameterMapImpl constants;
    private final Renderer renderer;
    private final TreeMap<String, Image> images;
    private final TreeMap<String, MouseCursor> cursors;
    private final MathInterpreter mathInterpreter;

    private Texture currentTexture;
    
    static final EmptyImage NONE = new EmptyImage(0, 0);
    private static final MouseCursor INHERIT_CURSOR = new MouseCursor() {};
    
    ImageManager(ParameterMapImpl constants, Renderer renderer) {
        this.constants = constants;
        this.renderer = renderer;
        this.images = new TreeMap<String, Image>();
        this.cursors = new TreeMap<String, MouseCursor>();
        this.mathInterpreter = new MathInterpreter();

        images.put("none", NONE);
        cursors.put("os-default", MouseCursor.OS_DEFAULT);
        cursors.put("inherit", INHERIT_CURSOR);
    }

    Image getImage(String name) {
        return images.get(name);
    }

    Image getReferencedImage(XMLParser xmlp) throws XmlPullParserException {
        String ref = xmlp.getAttributeNotNull("ref");
        return getReferencedImage(xmlp, ref);
    }

    Image getReferencedImage(XMLParser xmlp, String ref) throws XmlPullParserException {
        if(ref.endsWith(".*")) {
            throw xmlp.error("wildcard mapping not allowed");
        }
        Image img = images.get(ref);
        if(img == null) {
            throw xmlp.error("referenced image \"" + ref + "\" not found");
        }
        return img;
    }

    MouseCursor getReferencedCursor(XMLParser xmlp, String ref) throws XmlPullParserException {
        MouseCursor cursor = cursors.get(ref);
        if(cursor == null) {
            throw xmlp.error("referenced cursor \"" + ref + "\" not found");
        }
        return unwrapCursor(cursor);
    }

    Map<String, Image> getImages(String ref, String name) {
        return ParserUtil.resolve(images, ref, name, null);
    }

    public MouseCursor getCursor(String name) {
        return unwrapCursor(cursors.get(name));
    }

    Map<String, MouseCursor> getCursors(String ref, String name) {
        return ParserUtil.resolve(cursors, ref, name, INHERIT_CURSOR);
    }

    void parseImages(XMLParser xmlp, URL baseUrl) throws XmlPullParserException, IOException {
        xmlp.require(XmlPullParser.START_TAG, null, null);

        Texture texture = null;
        String fileName = xmlp.getAttributeValue(null, "file");
        if(fileName != null) {
            String fmt = xmlp.getAttributeValue(null, "format");
            String filter = xmlp.getAttributeValue(null, "filter");
            // ignore the comment so that it does not cause a warning
            xmlp.getAttributeValue(null, "comment");

            try {
                texture = renderer.loadTexture(new URL(baseUrl, fileName), fmt, filter);
                if(texture == null) {
                    throw new NullPointerException("loadTexture returned null");
                }
            } catch (IOException ex) {
                throw xmlp.error("Unable to load image file: " + fileName, ex);
            }
        }

        this.currentTexture = texture;

        try {
            xmlp.nextTag();
            while(!xmlp.isEndTag()) {
                String name = xmlp.getAttributeNotNull("name");
                checkImageName(name, xmlp);
                String tagName = xmlp.getName();
                if("cursor".equals(xmlp.getName())) {
                    parseCursor(xmlp, name);
                } else {
                    Image image = parseImage(xmlp, tagName);
                    images.put(name, image);
                }
                xmlp.require(XmlPullParser.END_TAG, null, tagName);
                xmlp.nextTag();
            }
        } finally {
            currentTexture = null;
            if(texture != null) {
                texture.themeLoadingDone();
            }
        }
    }

    private MouseCursor unwrapCursor(MouseCursor cursor) {
        return (cursor == INHERIT_CURSOR) ? null : cursor;
    }
    
    private void checkImageName(String name, XMLParser xmlp) throws XmlPullParserException, XmlPullParserException {
        ParserUtil.checkNameNotEmpty(name, xmlp);
        if(images.containsKey(name)) {
            throw xmlp.error("image \"" + name + "\" already defined");
        }
    }

    private static Border getBorder(Image image, Border border) {
        if(border == null && (image instanceof HasBorder)) {
            border = ((HasBorder)image).getBorder();
        }
        return border;
    }

    private void parseCursor(XMLParser xmlp, String name) throws IOException, XmlPullParserException {
        String ref = xmlp.getAttributeValue(null, "ref");
        MouseCursor cursor;
        if(ref != null) {
            cursor = cursors.get(ref);
            if(cursor == null) {
                throw xmlp.error("referenced cursor \"" + ref + "\" not found");
            }
        } else {
            ImageParams imageParams = new ImageParams();
            parseRectFromAttribute(xmlp, imageParams);
            int hotSpotX = xmlp.parseIntFromAttribute("hotSpotX");
            int hotSpotY = xmlp.parseIntFromAttribute("hotSpotY");
            String imageRefStr = xmlp.getAttributeValue(null, "imageRef");

            Image imageRef = null;
            if(imageRefStr != null) {
                imageRef = getReferencedImage(xmlp, imageRefStr);
            }
            cursor = currentTexture.createCursor(imageParams.x, imageParams.y, imageParams.w, imageParams.h, hotSpotX, hotSpotY, imageRef);
            if(cursor == null) {
                cursor = MouseCursor.OS_DEFAULT;
            }
        }
        cursors.put(name, cursor);
        xmlp.nextTag();
    }

    private Image parseImage(XMLParser xmlp, String tagName) throws XmlPullParserException, IOException {
        ImageParams params = new ImageParams();
        params.condition = ParserUtil.parseCondition(xmlp);
        return parseImageNoCond(xmlp, tagName, params);
    }

    private Image parseImageNoCond(XMLParser xmlp, String tagName, ImageParams params) throws XmlPullParserException, IOException {
        parseStdAttributes(xmlp, params);
        Image image = parseImageDelegate(xmlp, tagName, params);
        return adjustImage(image, params);
    }

    private Image adjustImage(Image image, ImageParams params) {
        Border border = getBorder(image, params.border);
        if(params.tintColor != null && !Color.WHITE.equals(params.tintColor)) {
            image = image.createTintedVersion(params.tintColor);
        }
        if(params.repeatX || params.repeatY) {
            image = new RepeatImage(image, border, params.repeatX, params.repeatY);
        }
        Border imgBorder = getBorder(image, null);
        if((border != null && border != imgBorder) || params.inset != null ||
                params.center || params.condition != null ||
                params.sizeOverwriteH >= 0 || params.sizeOverwriteV >= 0) {
            image = new ImageAdjustments(image, border, params.inset,
                    params.sizeOverwriteH, params.sizeOverwriteV,
                    params.center, params.condition);
        }
        return image;
    }

    private Image parseImageDelegate(XMLParser xmlp, String tagName, ImageParams params) throws XmlPullParserException, IOException {
        if("area".equals(tagName)) {
            return parseArea(xmlp, params);
        } else if("alias".equals(tagName)) {
            return parseAlias(xmlp);
        } else if("composed".equals(tagName)) {
            return parseComposed(xmlp, params);
        } else if("select".equals(tagName)) {
            return parseStateSelect(xmlp, params);
        } else if("grid".equals(tagName)) {
            return parseGrid(xmlp, params);
        } else if("animation".equals(tagName)) {
            return parseAnimation(xmlp, params);
        } else if("gradient".equals(tagName)) {
            return parseGradient(xmlp, params);
        } else {
            throw xmlp.error("Unexpected '"+tagName+"'");
        }
    }

    private Image parseComposed(XMLParser xmlp, ImageParams params) throws IOException, XmlPullParserException {
        ArrayList<Image> layers = new ArrayList<Image>();
        xmlp.nextTag();
        while(!xmlp.isEndTag()) {
            xmlp.require(XmlPullParser.START_TAG, null, null);
            String tagName = xmlp.getName();
            Image image = parseImage(xmlp, tagName);
            layers.add(image);
            params.border = getBorder(image, params.border);
            xmlp.require(XmlPullParser.END_TAG, null, tagName);
            xmlp.nextTag();
        }
        switch (layers.size()) {
            case 0:
                return NONE;
            case 1:
                return layers.get(0);
            default:
                return new ComposedImage(
                        layers.toArray(new Image[layers.size()]),
                        params.border);
        }
    }

    private Image parseStateSelect(XMLParser xmlp, ImageParams params) throws IOException, XmlPullParserException {
        ArrayList<Image> stateImages = new ArrayList<Image>();
        ArrayList<StateExpression> conditions = new ArrayList<StateExpression>();
        xmlp.nextTag();
        boolean last = false;
        while(!last && !xmlp.isEndTag()) {
            xmlp.require(XmlPullParser.START_TAG, null, null);
            StateExpression cond = ParserUtil.parseCondition(xmlp);
            String tagName = xmlp.getName();
            Image image = parseImageNoCond(xmlp, tagName, new ImageParams());
            params.border = getBorder(image, params.border);
            xmlp.require(XmlPullParser.END_TAG, null, tagName);
            xmlp.nextTag();
            last = cond == null;
            
            if(image instanceof ImageAdjustments) {
                ImageAdjustments ia = (ImageAdjustments)image;
                if(ia.isSimple()) {
                    cond = and(cond, ia.condition);
                    image = ia.image;
                }
            }
            
            if(StateSelect.isUseOptimizer() && (image instanceof StateSelectImage)) {
                inlineSelect((StateSelectImage)image, cond, stateImages, conditions);
            } else {
                stateImages.add(image);
                if(cond != null) {
                    conditions.add(cond);
                }
            }
        }
        if(conditions.isEmpty()) {
            System.err.println(xmlp.getFilePosition() + ": state select image needs atleast 1 condition");
            
            if(stateImages.isEmpty()) {
                return NONE;
            } else {
                return stateImages.get(0);
            }
        }
        StateSelect select = new StateSelect(conditions);
        Image image = new StateSelectImage(select, params.border, stateImages.toArray(new Image[stateImages.size()]));
        return image;
    }

    private static void inlineSelect(StateSelectImage src, StateExpression cond, ArrayList<Image> stateImages, ArrayList<StateExpression> conditions) {
        int n = src.images.length;
        int m = src.select.getNumExpressions();
        for(int i=0 ; i<n ; i++) {
            StateExpression imgCond = (i < m) ? src.select.getExpression(i) : null;
            imgCond = and(imgCond, cond);
            stateImages.add(src.images[i]);
            if(imgCond != null) {
                conditions.add(imgCond);
            }
        }
        if(n == m && cond != null) {
            // when the src StateSelectImage doesn't have a default entry
            // (which is used when no condition matched) then add one with
            // NONE as image (except when inlining as default entry)
            stateImages.add(NONE);
            conditions.add(cond);
        }
    }

    private static StateExpression and(StateExpression imgCond, StateExpression cond) {
        if(imgCond == null) {
            imgCond = cond;
        } else if(cond != null) {
            imgCond = new StateExpression.Logic('+', imgCond, cond);
        }
        return imgCond;
    }

    private Image parseArea(XMLParser xmlp, ImageParams params) throws IOException, XmlPullParserException {
        parseRectFromAttribute(xmlp, params);
        parseRotationFromAttribute(xmlp, params);
        boolean tiled = xmlp.parseBoolFromAttribute("tiled", false);
        final int[] splitx = parseSplit2(xmlp, "splitx", Math.abs(params.w));
        final int[] splity = parseSplit2(xmlp, "splity", Math.abs(params.h));
        final Image image;
        if(splitx != null || splity != null) {
            boolean noCenter = xmlp.parseBoolFromAttribute("nocenter", false);
            final int columns = (splitx != null) ? 3 : 1;
            final int rows = (splity != null) ? 3 : 1;
            final Image[] imageParts = new Image[columns * rows];
            for(int r=0 ; r<rows ; r++) {
                final int imgY, imgH;
                if(splity != null) {
                    imgY = (params.h < 0) ? (params.y - params.h - splity[r+1]) : (params.y + splity[r]);
                    imgH = (splity[r+1] - splity[r]) * Integer.signum(params.h);
                } else {
                    imgY = params.y;
                    imgH = params.h;
                }
                for(int c=0 ; c<columns ; c++) {
                    final int imgX, imgW;
                    if(splitx != null) {
                        imgX = (params.w < 0) ? (params.x - params.w - splitx[c+1]) : (params.x + splitx[c]);
                        imgW = (splitx[c+1] - splitx[c]) * Integer.signum(params.w);
                    } else {
                        imgX = params.x;
                        imgW = params.w;
                    }

                    boolean isCenter = (r == rows/2) && (c == columns/2);
                    Image img;
                    if(noCenter && isCenter) {
                        img = new EmptyImage(imgW, imgH);
                    } else {
                        img = createImage(xmlp, imgX, imgY, imgW, imgH, params.tintColor, isCenter & tiled, params.rot);
                    }
                    int idx;
                    switch(params.rot) {
                        default:
                            idx = r*columns + c;
                            break;
                        case CLOCKWISE_90:
                            idx = c*rows + (rows-1-r);
                            break;
                        case CLOCKWISE_180:
                            idx = (rows-1-r)*columns + (columns-1-c);
                            break;
                        case CLOCKWISE_270:
                            idx = (columns-1-c)*rows + r;
                            break;
                            
                    }
                    imageParts[idx] = img;
                }
            }
            switch(params.rot) {
                case CLOCKWISE_90:
                case CLOCKWISE_270:
                    image = new GridImage(imageParts,
                            (splity != null) ? SPLIT_WEIGHTS_3 : SPLIT_WEIGHTS_1,
                            (splitx != null) ? SPLIT_WEIGHTS_3 : SPLIT_WEIGHTS_1,
                            params.border);
                    break;
                default:
                    image = new GridImage(imageParts,
                            (splitx != null) ? SPLIT_WEIGHTS_3 : SPLIT_WEIGHTS_1,
                            (splity != null) ? SPLIT_WEIGHTS_3 : SPLIT_WEIGHTS_1,
                            params.border);
                    break;
            }
        } else {
            image = createImage(xmlp, params.x, params.y, params.w, params.h, params.tintColor, tiled, params.rot);
        }
        xmlp.nextTag();
        params.tintColor = null;
        if(tiled) {
            params.repeatX = false;
            params.repeatY = false;
        }
        return image;
    }

    private Image parseAlias(XMLParser xmlp) throws XmlPullParserException, XmlPullParserException, IOException {
        Image image = getReferencedImage(xmlp);
        xmlp.nextTag();
        return image;
    }

    private static int[] parseSplit2(XMLParser xmlp, String attribName, int size) throws XmlPullParserException {
        String splitStr = xmlp.getAttributeValue(null, attribName);
        if(splitStr != null) {
            int comma = splitStr.indexOf(',');
            if(comma < 0) {
                throw xmlp.error(attribName + " requires 2 values");
            }
            try {
                int[] result = new int[4];
                for(int i=0,start=0 ; i<2 ; i++) {
                    String part = TextUtil.trim(splitStr, start, comma);
                    if(part.length() == 0) {
                        throw new NumberFormatException("empty string");
                    }
                    int off = 0;
                    int sign = 1;
                    switch (part.charAt(0)) {
                        case 'b':
                        case 'B':
                        case 'r':
                        case 'R':
                            off = size;
                            sign = -1;
                            // fall through
                        case 't':
                        case 'T':
                        case 'l':
                        case 'L':
                            part = TextUtil.trim(part, 1);
                            break;
                    }
                    int value = Integer.parseInt(part);
                    result[i+1] = Math.max(0, Math.min(size, off + sign*value));

                    start = comma +  1;
                    comma = splitStr.length();
                }
                if(result[1] > result[2]) {
                    int tmp = result[1];
                    result[1] = result[2];
                    result[2] = tmp;
                }
                result[3] = size;
                return result;
            } catch(NumberFormatException ex) {
                throw xmlp.error("Unable to parse " + attribName + ": \"" + splitStr + "\"", ex);
            }
        } else {
            return null;
        }
    }

    private void parseSubImages(XMLParser xmlp, Image[] textures) throws XmlPullParserException, IOException {
        int idx = 0;
        while(xmlp.isStartTag()) {
            if(idx == textures.length) {
                throw xmlp.error("Too many sub images");
            }
            String tagName = xmlp.getName();
            textures[idx++] = parseImage(xmlp, tagName);
            xmlp.require(XmlPullParser.END_TAG, null, tagName);
            xmlp.nextTag();
        }
        if(idx != textures.length) {
            throw xmlp.error("Not enough sub images");
        }
    }

    private Image parseGrid(XMLParser xmlp, ImageParams params) throws IOException, XmlPullParserException {
        try {
            int[] weightsX = ParserUtil.parseIntArrayFromAttribute(xmlp, "weightsX");
            int[] weightsY = ParserUtil.parseIntArrayFromAttribute(xmlp, "weightsY");
            Image[] textures = new Image[weightsX.length * weightsY.length];
            xmlp.nextTag();
            parseSubImages(xmlp, textures);
            Image image = new GridImage(textures, weightsX, weightsY, params.border);
            return image;
        } catch(IllegalArgumentException ex) {
            throw xmlp.error("Invalid value", ex);
        }
    }

    private static final int[] SPLIT_WEIGHTS_3 = {0,1,0};
    private static final int[] SPLIT_WEIGHTS_1 = {1};
    
    private void parseAnimElements(XMLParser xmlp, String tagName, ArrayList<AnimatedImage.Element> frames) throws XmlPullParserException, IOException {
        if("repeat".equals(tagName)) {
            frames.add(parseAnimRepeat(xmlp));
        } else if("frame".equals(tagName)) {
            frames.add(parseAnimFrame(xmlp));
        } else if("frames".equals(tagName)) {
            parseAnimFrames(xmlp, frames);
        } else {
            throw xmlp.unexpected();
        }
    }

    private AnimatedImage.Img parseAnimFrame(XMLParser xmlp) throws XmlPullParserException, IOException {
        int duration = xmlp.parseIntFromAttribute("duration");
        if(duration < 0) {
            throw new IllegalArgumentException("duration must be >= 0 ms");
        }
        AnimParams animParams = parseAnimParams(xmlp);
        Image image = getReferencedImage(xmlp);
        AnimatedImage.Img img = new AnimatedImage.Img(duration, image, animParams.tintColor,
                animParams.zoomX, animParams.zoomY, animParams.zoomCenterX, animParams.zoomCenterY);
        xmlp.nextTag();
        return img;
    }

    private AnimParams parseAnimParams(XMLParser xmlp) throws XmlPullParserException {
        AnimParams params = new AnimParams();
        params.tintColor = ParserUtil.parseColorFromAttribute(xmlp, "tint", constants, Color.WHITE);
        float zoom = xmlp.parseFloatFromAttribute("zoom", 1.0f);
        params.zoomX = xmlp.parseFloatFromAttribute("zoomX", zoom);
        params.zoomY = xmlp.parseFloatFromAttribute("zoomY", zoom);
        params.zoomCenterX = xmlp.parseFloatFromAttribute("zoomCenterX", 0.5f);
        params.zoomCenterY = xmlp.parseFloatFromAttribute("zoomCenterY", 0.5f);
        return params;
    }

    private void parseAnimFrames(XMLParser xmlp, ArrayList<AnimatedImage.Element> frames) throws XmlPullParserException, IOException {
        ImageParams params = new ImageParams();
        parseRectFromAttribute(xmlp, params);
        parseRotationFromAttribute(xmlp, params);
        int duration = xmlp.parseIntFromAttribute("duration");
        if(duration < 1) {
            throw new IllegalArgumentException("duration must be >= 1 ms");
        }
        int count = xmlp.parseIntFromAttribute("count");
        if(count < 1) {
            throw new IllegalArgumentException("count must be >= 1");
        }
        AnimParams animParams = parseAnimParams(xmlp);
        int xOffset = xmlp.parseIntFromAttribute("offsetx", 0);
        int yOffset = xmlp.parseIntFromAttribute("offsety", 0);
        if(count > 1 && (xOffset == 0 && yOffset == 0)) {
            throw new IllegalArgumentException("offsets required for multiple frames");
        }
        for(int i=0 ; i<count ; i++) {
            Image image = createImage(xmlp, params.x, params.y, params.w, params.h, Color.WHITE, false, params.rot);
            AnimatedImage.Img img = new AnimatedImage.Img(duration, image, animParams.tintColor,
                    animParams.zoomX, animParams.zoomY, animParams.zoomCenterX, animParams.zoomCenterY);
            frames.add(img);
            params.x += xOffset;
            params.y += yOffset;
        }

        xmlp.nextTag();
    }

    private AnimatedImage.Repeat parseAnimRepeat(XMLParser xmlp) throws XmlPullParserException, IOException {
        String strRepeatCount = xmlp.getAttributeValue(null, "count");
        int repeatCount = 0;
        if(strRepeatCount != null) {
            repeatCount = Integer.parseInt(strRepeatCount);
            if(repeatCount <= 0) {
                throw new IllegalArgumentException("Invalid repeat count");
            }
        }
        boolean lastRepeatsEndless = false;
        boolean hasWarned = false;
        ArrayList<AnimatedImage.Element> children = new ArrayList<AnimatedImage.Element>();
        xmlp.nextTag();
        while(xmlp.isStartTag()) {
            if(lastRepeatsEndless && !hasWarned) {
                hasWarned = true;
                getLogger().log(Level.WARNING, "Animation frames after an endless repeat won''t be displayed: {0}", xmlp.getPositionDescription());
            }
            String tagName = xmlp.getName();
            parseAnimElements(xmlp, tagName, children);
            AnimatedImage.Element e = children.get(children.size()-1);
            lastRepeatsEndless =
                    (e instanceof AnimatedImage.Repeat) &&
                    ((AnimatedImage.Repeat)e).repeatCount == 0;
            xmlp.require(XmlPullParser.END_TAG, null, tagName);
            xmlp.nextTag();
        }
        return new AnimatedImage.Repeat(children.toArray(new AnimatedImage.Element[children.size()]), repeatCount);
    }

    private Border getBorder(AnimatedImage.Element e) {
        if(e instanceof AnimatedImage.Repeat) {
            AnimatedImage.Repeat r = (AnimatedImage.Repeat)e;
            for(AnimatedImage.Element c : r.children) {
                Border border = getBorder(c);
                if(border != null) {
                    return border;
                }
            }
        } else if(e instanceof AnimatedImage.Img) {
            AnimatedImage.Img i = (AnimatedImage.Img)e;
            if(i.image instanceof HasBorder) {
                return ((HasBorder)i.image).getBorder();
            }
        }
        return null;
    }

    private Image parseAnimation(XMLParser xmlp, ImageParams params) throws XmlPullParserException, IOException {
        try {
            String timeSource = xmlp.getAttributeNotNull("timeSource");
            int frozenTime = xmlp.parseIntFromAttribute("frozenTime", -1);
            AnimatedImage.Repeat root = parseAnimRepeat(xmlp);
            if(params.border == null) {
                params.border = getBorder(root);
            }
            Image image = new AnimatedImage(renderer, root, timeSource, params.border,
                    (params.tintColor == null) ? Color.WHITE : params.tintColor, frozenTime);
            params.tintColor = null;
            return image;
        } catch(IllegalArgumentException ex) {
            throw xmlp.error("Unable to parse", ex);
        }
    }
    
    private Image parseGradient(XMLParser xmlp, ImageParams params) throws XmlPullParserException, IOException {
        try {
            Gradient.Type type = xmlp.parseEnumFromAttribute("type", Gradient.Type.class);
            Gradient.Wrap wrap = xmlp.parseEnumFromAttribute("wrap", Gradient.Wrap.class, Gradient.Wrap.SCALE);

            Gradient gradient = new Gradient(type);
            gradient.setWrap(wrap);

            xmlp.nextTag();
            while(xmlp.isStartTag()) {
                xmlp.require(XmlPullParser.START_TAG, null, "stop");
                float pos = xmlp.parseFloatFromAttribute("pos");
                Color color = ParserUtil.parseColor(xmlp, xmlp.getAttributeNotNull("color"), constants);
                gradient.addStop(pos, color);
                xmlp.nextTag();
                xmlp.require(XmlPullParser.END_TAG, null, "stop");
                xmlp.nextTag();
            }
            
            return renderer.createGradient(gradient);
        } catch(IllegalArgumentException ex) {
            throw xmlp.error("Unable to parse", ex);
        }
    }

    private Image createImage(XMLParser xmlp, int x, int y, int w, int h, Color tintColor, boolean tiled, Texture.Rotation rotation) {
        if(w == 0 || h == 0) {
            return new EmptyImage(Math.abs(w), Math.abs(h));
        }

        final Texture texture = currentTexture;
        final int texWidth = texture.getWidth();
        final int texHeight = texture.getHeight();

        int x1 = x + Math.abs(w);
        int y1 = y + Math.abs(h);

        if(x < 0 || x >= texWidth || x1 < 0 || x1 > texWidth ||
                y < 0 || y >= texHeight || y1 < 0 || y1 > texHeight) {
            getLogger().log(Level.WARNING, "texture partly outside of file: {0}", xmlp.getPositionDescription());
            x = Math.max(0, Math.min(x, texWidth));
            y = Math.max(0, Math.min(y, texHeight));
            w = Integer.signum(w) * (Math.max(0, Math.min(x1, texWidth)) - x);
            h = Integer.signum(h) * (Math.max(0, Math.min(y1, texHeight)) - y);
        }
        
        return texture.getImage(x, y, w, h, tintColor, tiled, rotation);
    }
    
    private void parseRectFromAttribute(XMLParser xmlp, ImageParams params) throws XmlPullParserException {
        if(currentTexture == null) {
            throw xmlp.error("can't create area outside of <imagefile> object");
        }
        String xywh = xmlp.getAttributeNotNull("xywh");
        if("*".equals(xywh)) {
            params.x = 0;
            params.y = 0;
            params.w = currentTexture.getWidth();
            params.h = currentTexture.getHeight();
        } else try {
            int[] coords = TextUtil.parseIntArray(xywh);
            if(coords.length != 4) {
                throw xmlp.error("xywh requires 4 integer arguments");
            }
            params.x = coords[0];
            params.y = coords[1];
            params.w = coords[2];
            params.h = coords[3];
        } catch(IllegalArgumentException ex) {
            throw xmlp.error("can't parse xywh argument", ex);
        }
    }
    
    private void parseRotationFromAttribute(XMLParser xmlp, ImageParams params) throws XmlPullParserException {
        if(currentTexture == null) {
            throw xmlp.error("can't create area outside of <imagefile> object");
        }
        int rot = xmlp.parseIntFromAttribute("rot", 0);
        switch(rot) {
            case 0: params.rot = Texture.Rotation.NONE; break;
            case 90: params.rot = Texture.Rotation.CLOCKWISE_90; break;
            case 180: params.rot = Texture.Rotation.CLOCKWISE_180; break;
            case 270: params.rot = Texture.Rotation.CLOCKWISE_270; break;
            default:
                throw xmlp.error("invalid rotation angle");
        }
    }

    private void parseStdAttributes(XMLParser xmlp, ImageParams params) throws XmlPullParserException {
        params.tintColor = ParserUtil.parseColorFromAttribute(xmlp, "tint", constants, null);
        params.border = ParserUtil.parseBorderFromAttribute(xmlp, "border");
        params.inset = ParserUtil.parseBorderFromAttribute(xmlp, "inset");
        params.repeatX = xmlp.parseBoolFromAttribute("repeatX", false);
        params.repeatY = xmlp.parseBoolFromAttribute("repeatY", false);
        params.sizeOverwriteH = ParserUtil.parseIntExpressionFromAttribute(xmlp, "sizeOverwriteH", -1, mathInterpreter);
        params.sizeOverwriteV = ParserUtil.parseIntExpressionFromAttribute(xmlp, "sizeOverwriteV", -1, mathInterpreter);
        params.center = xmlp.parseBoolFromAttribute("center", false);
    }

    Logger getLogger() {
        return Logger.getLogger(ImageManager.class.getName());
    }
    
    static class ImageParams {
        int x, y, w, h;
        Color tintColor;
        Border border;
        Border inset;
        boolean repeatX;
        boolean repeatY;
        int sizeOverwriteH = -1;
        int sizeOverwriteV = -1;
        boolean center;
        StateExpression condition;
        Texture.Rotation rot;
    }
    
    static class AnimParams {
        Color tintColor;
        float zoomX;
        float zoomY;
        float zoomCenterX;
        float zoomCenterY;
    }
    
    class MathInterpreter extends AbstractMathInterpreter {
        public void accessVariable(String name) {
            Image img = getImage(name);
            if(img != null) {
                push(img);
                return;
            }
            Object obj = constants.getParam(name);
            if(obj != null) {
                push(obj);
                return;
            }
            throw new IllegalArgumentException("variable not found: " + name);
        }

        @Override
        protected Object accessField(Object obj, String field) {
            if(obj instanceof ParameterMapImpl) {
                Object result = ((ParameterMapImpl)obj).getParam(field);
                if(result == null) {
                    throw new IllegalArgumentException("field not found: " + field);
                }
                return result;
            }
            if((obj instanceof Image) && "border".equals(field)) {
                Border border = null;
                if(obj instanceof HasBorder) {
                    border = ((HasBorder)obj).getBorder();
                }
                return (border != null) ? border : Border.ZERO;
            }
            return super.accessField(obj, field);
        }
    }
}
