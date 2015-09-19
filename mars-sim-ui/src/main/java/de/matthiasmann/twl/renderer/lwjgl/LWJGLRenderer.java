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
package de.matthiasmann.twl.renderer.lwjgl;

import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.Rect;
import de.matthiasmann.twl.input.Input;
import de.matthiasmann.twl.input.lwjgl.LWJGLInput;
import de.matthiasmann.twl.renderer.AnimationState;
import de.matthiasmann.twl.renderer.AnimationState.StateKey;
import de.matthiasmann.twl.renderer.CacheContext;
import de.matthiasmann.twl.renderer.DynamicImage;
import de.matthiasmann.twl.renderer.FontParameter;
import de.matthiasmann.twl.renderer.Gradient;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.renderer.MouseCursor;
import de.matthiasmann.twl.renderer.Font;
import de.matthiasmann.twl.renderer.FontMapper;
import de.matthiasmann.twl.renderer.LineRenderer;
import de.matthiasmann.twl.renderer.OffscreenRenderer;
import de.matthiasmann.twl.renderer.Renderer;
import de.matthiasmann.twl.renderer.Texture;
import de.matthiasmann.twl.utils.ClipStack;
import de.matthiasmann.twl.utils.StateSelect;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.EXTTextureRectangle;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

/**
 * A renderer using only GL11 features.
 *
 * <p>For correct rendering the OpenGL viewport size must be synchronized.</p>
 *
 * @author Matthias Mann
 * 
 * @see #syncViewportSize()
 */
public class LWJGLRenderer implements Renderer, LineRenderer {

    public static final StateKey STATE_LEFT_MOUSE_BUTTON = StateKey.get("leftMouseButton");
    public static final StateKey STATE_MIDDLE_MOUSE_BUTTON = StateKey.get("middleMouseButton");
    public static final StateKey STATE_RIGHT_MOUSE_BUTTON = StateKey.get("rightMouseButton");

    public static final FontParameter.Parameter<Integer> FONTPARAM_OFFSET_X = FontParameter.newParameter("offsetX", 0);
    public static final FontParameter.Parameter<Integer> FONTPARAM_OFFSET_Y = FontParameter.newParameter("offsetY", 0);
    public static final FontParameter.Parameter<Integer> FONTPARAM_UNDERLINE_OFFSET = FontParameter.newParameter("underlineOffset", 0);  
    
    private final IntBuffer ib16;
    final int maxTextureSize;

    private int viewportX;
    private int viewportBottom;
    private int width;
    private int height;
    private boolean hasScissor;
    private final TintStack tintStateRoot;
    private final Cursor emptyCursor;
    private boolean useQuadsForLines;
    private boolean useSWMouseCursors;
    private SWCursor swCursor;
    private int mouseX;
    private int mouseY;
    private LWJGLCacheContext cacheContext;
    private FontMapper fontMapper;

    final SWCursorAnimState swCursorAnimState;
    final ArrayList<TextureArea> textureAreas;
    final ArrayList<TextureAreaRotated> rotatedTextureAreas;
    final ArrayList<LWJGLDynamicImage> dynamicImages;
    
    protected TintStack tintStack;
    protected final ClipStack clipStack;
    protected final Rect clipRectTemp;
    
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public LWJGLRenderer() throws LWJGLException {
        this.ib16 = BufferUtils.createIntBuffer(16);
        this.textureAreas = new ArrayList<TextureArea>();
        this.rotatedTextureAreas = new ArrayList<TextureAreaRotated>();
        this.dynamicImages = new ArrayList<LWJGLDynamicImage>();
        this.tintStateRoot = new TintStack();
        this.tintStack = tintStateRoot;
        this.clipStack = new ClipStack();
        this.clipRectTemp = new Rect();
        syncViewportSize();

        GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE, ib16);
        maxTextureSize = ib16.get(0);

        if(Mouse.isCreated()) {
            int minCursorSize = Cursor.getMinCursorSize();
            IntBuffer tmp = BufferUtils.createIntBuffer(minCursorSize * minCursorSize);
            emptyCursor = new Cursor(minCursorSize, minCursorSize,
                    minCursorSize/2, minCursorSize/2, 1, tmp, null);
        } else {
            emptyCursor = null;
        }

        swCursorAnimState = new SWCursorAnimState();
    }

    public boolean isUseQuadsForLines() {
        return useQuadsForLines;
    }

    public void setUseQuadsForLines(boolean useQuadsForLines) {
        this.useQuadsForLines = useQuadsForLines;
    }

    public boolean isUseSWMouseCursors() {
        return useSWMouseCursors;
    }

    /**
     * Controls if the mouse cursor is rendered via SW or HW cursors.
     * HW cursors have reduced support for transparency and cursor size.
     *
     * This must be set before loading a theme !
     * 
     * @param useSWMouseCursors
     */
    public void setUseSWMouseCursors(boolean useSWMouseCursors) {
        this.useSWMouseCursors = useSWMouseCursors;
    }

    public CacheContext createNewCacheContext() {
        return new LWJGLCacheContext(this);
    }

    private LWJGLCacheContext activeCacheContext() {
        if(cacheContext == null) {
            setActiveCacheContext(createNewCacheContext());
        }
        return cacheContext;
    }

    public CacheContext getActiveCacheContext() {
        return activeCacheContext();
    }

    public void setActiveCacheContext(CacheContext cc) throws IllegalStateException {
        if(cc == null) {
            throw new NullPointerException();
        }
        if(!cc.isValid()) {
            throw new IllegalStateException("CacheContext is invalid");
        }
        if(!(cc instanceof LWJGLCacheContext)) {
            throw new IllegalArgumentException("CacheContext object not from this renderer");
        }
        LWJGLCacheContext lwjglCC = (LWJGLCacheContext)cc;
        if(lwjglCC.renderer != this) {
            throw new IllegalArgumentException("CacheContext object not from this renderer");
        }
        this.cacheContext = lwjglCC;
        try {
            for(TextureArea ta : textureAreas) {
                ta.destroyRepeatCache();
            }
            for(TextureAreaRotated tar : rotatedTextureAreas) {
                tar.destroyRepeatCache();
            }
        } finally {
            textureAreas.clear();
            rotatedTextureAreas.clear();
        }
    }

    /**
     * <p>Queries the current view port size & position and updates all related
     * internal state.</p>
     *
     * <p>It is important that the internal state matches the OpenGL viewport or
     * clipping won't work correctly.</p>
     *
     * <p>This method should only be called when the viewport size has changed.
     * It can have negative impact on performance to call every frame.</p>
     * 
     * @see #getWidth()
     * @see #getHeight()
     */
    public void syncViewportSize() {
        ib16.clear();
        GL11.glGetInteger(GL11.GL_VIEWPORT, ib16);
        viewportX = ib16.get(0);
        width = ib16.get(2);
        height = ib16.get(3);
        viewportBottom = ib16.get(1) + height;
    }
    
    /**
     * Sets the viewport size & position.
     * <p>This method is preferred over {@link #syncViewportSize() } as it avoids
     * calling {@link GL11#glGetInteger(int, java.nio.IntBuffer) }.</p>
     * 
     * @param x the X position (GL_VIEWPORT index 0)
     * @param y the Y position (GL_VIEWPORT index 1)
     * @param width the width (GL_VIEWPORT index 2)
     * @param height the height (GL_VIEWPORT index 3)
     */
    public void setViewport(int x, int y, int width, int height) {
        this.viewportX = x;
        this.viewportBottom = y + height;
        this.width = width;
        this.height = height;
    }

    public long getTimeMillis() {
        long res = Sys.getTimerResolution();
        long time = Sys.getTime();
        if(res != 1000) {
            time = (time * 1000) / res;
        }
        return time;
    }
    
    /**
     * Creates a new instance of {@link LWJGLInput}
     * @return a new instance of {@code LWJGLInput}
     */
    public Input getInput() {
        return new LWJGLInput();
    }
    
    protected void setupGLState() {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_TRANSFORM_BIT|GL11.GL_HINT_BIT|
                GL11.GL_COLOR_BUFFER_BIT|GL11.GL_SCISSOR_BIT|GL11.GL_LINE_BIT|GL11.GL_TEXTURE_BIT);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(0, width, height, 0, -1.0, 1.0);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
    }
    
    protected void revertGLState() {
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }
    
    /**
     * Setup GL to start rendering the GUI. It assumes default GL state.
     */
    public boolean startRendering() {
        if(width <= 0 || height <= 0) {
            return false;
        }
        
        prepareForRendering();
        setupGLState();
        
        return true;
    }

    public void endRendering() {
        renderSWCursor();
        revertGLState();
    }
    
    /**
     * Call to revert the GL state to the state before calling
     * {@link #startRendering()}.
     * @see #resumeRendering() 
     */
    public void pauseRendering() {
        revertGLState();
    }
    
    /**
     * Resume rendering after a call to {@link #pauseRendering()}.
     */
    public void resumeRendering() {
        hasScissor = false;
        setupGLState();
        setClipRect();
    }
    
    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
    
    /**
     * Retrieves the X position of the OpenGL viewport (index 0 of GL_VIEWPORT)
     * @return the X position of the OpenGL viewport
     */
    public int getViewportX() {
        return viewportX;
    }
    
    /**
     * Retrieves the Y position of the OpenGL viewport (index 1 of GL_VIEWPORT)
     * @return the Y position of the OpenGL viewport
     */
    public int getViewportY() {
        return viewportBottom - height;
    }

    public Font loadFont(URL url, StateSelect select, FontParameter ... parameterList) throws IOException {
        if(url == null) {
            throw new NullPointerException("url");
        }
        if(select == null) {
            throw new NullPointerException("select");
        }
        if(parameterList == null) {
            throw new NullPointerException("parameterList");
        }
        if(select.getNumExpressions() + 1 != parameterList.length) {
            throw new IllegalArgumentException("select.getNumExpressions() + 1 != parameterList.length");
        }
        BitmapFont bmFont = activeCacheContext().loadBitmapFont(url);
        return new LWJGLFont(this, bmFont, select, parameterList);
    }

    public Texture loadTexture(URL url, String formatStr, String filterStr) throws IOException {
        LWJGLTexture.Format format = LWJGLTexture.Format.COLOR;
        LWJGLTexture.Filter filter = LWJGLTexture.Filter.LINEAR;
        if(formatStr != null) {
            try {
                format = LWJGLTexture.Format.valueOf(formatStr.toUpperCase(Locale.ENGLISH));
            } catch(IllegalArgumentException ex) {
                getLogger().log(Level.WARNING, "Unknown texture format: {0}", formatStr);
            }
        }
        if(filterStr != null) {
            try {
                filter = LWJGLTexture.Filter.valueOf(filterStr.toUpperCase(Locale.ENGLISH));
            } catch(IllegalArgumentException ex) {
                getLogger().log(Level.WARNING, "Unknown texture filter: {0}", filterStr);
            }
        }
        return load(url, format, filter);
    }

    public LineRenderer getLineRenderer() {
        return this;
    }

    public OffscreenRenderer getOffscreenRenderer() {
        return null;
    }

    public FontMapper getFontMapper() {
        return fontMapper;
    }
    
    /**
     * Installs a font mapper. It is the responsibility of the font mapper to
     * manage the OpenGL state correctly so that normal rendering by LWJGLRenderer
     * is not disturbed.
     * 
     * @param fontMapper the font mapper object - can be null.
     */
    public void setFontMapper(FontMapper fontMapper) {
        this.fontMapper = fontMapper;
    }
    
    public DynamicImage createDynamicImage(int width, int height) {
        if(width <= 0) {
            throw new IllegalArgumentException("width");
        }
        if(height <= 0) {
            throw new IllegalArgumentException("height");
        }
        if(width > maxTextureSize || height > maxTextureSize) {
            getLogger().log(Level.WARNING, "requested size {0} x {1} exceeds maximum texture size {3}",
                    new Object[]{ width, height, maxTextureSize });
            return null;
        }

        int texWidth = width;
        int texHeight = height;
        
        ContextCapabilities caps = GLContext.getCapabilities();
        boolean useTextureRectangle = caps.GL_EXT_texture_rectangle || caps.GL_ARB_texture_rectangle;

        if(!useTextureRectangle && !caps.GL_ARB_texture_non_power_of_two) {
            texWidth = nextPowerOf2(width);
            texHeight = nextPowerOf2(height);
        }

        // ARB and EXT versions use the same enum !
        int proxyTarget = useTextureRectangle ?
            EXTTextureRectangle.GL_PROXY_TEXTURE_RECTANGLE_EXT : GL11.GL_PROXY_TEXTURE_2D;

        GL11.glTexImage2D(proxyTarget, 0, GL11.GL_RGBA, texWidth, texHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer)null);
        ib16.clear();
        GL11.glGetTexLevelParameter(proxyTarget, 0, GL11.GL_TEXTURE_WIDTH, ib16);
        if(ib16.get(0) != texWidth) {
            getLogger().log(Level.WARNING, "requested size {0} x {1} failed proxy texture test",
                    new Object[]{ texWidth, texHeight });
            return null;
        }

        // ARB and EXT versions use the same enum !
        int target = useTextureRectangle ?
            EXTTextureRectangle.GL_TEXTURE_RECTANGLE_EXT : GL11.GL_TEXTURE_2D;
        int id = GL11.glGenTextures();

        GL11.glBindTexture(target, id);
        GL11.glTexImage2D(target, 0, GL11.GL_RGBA, texWidth, texHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer)null);
        GL11.glTexParameteri(target, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(target, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

        LWJGLDynamicImage image = new LWJGLDynamicImage(this, target, id, width, height, texWidth, texHeight, Color.WHITE);
        dynamicImages.add(image);
        return image;
    }

    public Image createGradient(Gradient gradient) {
        return new GradientImage(this, gradient);
    }

    public void clipEnter(int x, int y, int w, int h) {
        clipStack.push(x, y, w, h);
        setClipRect();
    }

    public void clipEnter(Rect rect) {
        clipStack.push(rect);
        setClipRect();
    }

    public void clipLeave() {
        clipStack.pop();
        setClipRect();
    }

    public boolean clipIsEmpty() {
        return clipStack.isClipEmpty();
    }

    public void setCursor(MouseCursor cursor) {
        try {
            swCursor = null;
            if(isMouseInsideWindow()) {
                if(cursor instanceof LWJGLCursor) {
                    setNativeCursor(((LWJGLCursor)cursor).cursor);
                } else if(cursor instanceof SWCursor) {
                    setNativeCursor(emptyCursor);
                    swCursor = (SWCursor)cursor;
                } else {
                    setNativeCursor(null);
                }
            }
        } catch(LWJGLException ex) {
            getLogger().log(Level.WARNING, "Could not set native cursor", ex);
        }
    }

    public void setMousePosition(int mouseX, int mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public void setMouseButton(int button, boolean state) {
        swCursorAnimState.setAnimationState(button, state);
    }

    public LWJGLTexture load(URL textureUrl, LWJGLTexture.Format fmt, LWJGLTexture.Filter filter) throws IOException {
        return load(textureUrl, fmt, filter, null);
    }

    public LWJGLTexture load(URL textureUrl, LWJGLTexture.Format fmt, LWJGLTexture.Filter filter, TexturePostProcessing tpp) throws IOException {
        if(textureUrl == null) {
            throw new NullPointerException("textureUrl");
        }
        LWJGLCacheContext cc = activeCacheContext();
        if(tpp != null) {
            return cc.createTexture(textureUrl, fmt, filter, tpp);
        } else {
            return cc.loadTexture(textureUrl, fmt, filter);
        }
    }

    public void pushGlobalTintColor(float r, float g, float b, float a) {
        tintStack = tintStack.push(r, g, b, a);
    }

    public void popGlobalTintColor() {
        tintStack = tintStack.pop();
    }
    
    /**
     * Pushes a white entry on the tint stack which ignores the previous
     * tint color. It must be removed by calling {@link #popGlobalTintColor()}.
     * <p>This is useful when rendering to texture</p>
     */
    public void pushGlobalTintColorReset() {
        tintStack = tintStack.pushReset();
    }

    /**
     * Calls GL11.glColor4f() with the specified color multiplied by the current global tint color.
     *
     * @param color the color to set
     */
    public void setColor(Color color) {
        tintStack.setColor(color);
    }

    public void drawLine(float[] pts, int numPts, float width, Color color, boolean drawAsLoop) {
        if(numPts*2 > pts.length) {
            throw new ArrayIndexOutOfBoundsException(numPts*2);
        }
        if(numPts >= 2) {
            tintStack.setColor(color);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            if(useQuadsForLines) {
                drawLinesAsQuads(numPts, pts, width, drawAsLoop);
            } else {
                drawLinesAsLines(numPts, pts, width, drawAsLoop);
            }
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }
    }
    
    private void drawLinesAsLines(int numPts, float[] pts, float width, boolean drawAsLoop) {
        GL11.glLineWidth(width);
        GL11.glBegin(drawAsLoop ? GL11.GL_LINE_LOOP : GL11.GL_LINE_STRIP);
        for(int i=0 ; i<numPts ; i++) {
            GL11.glVertex2f(pts[i*2+0], pts[i*2+1]);
        }
        GL11.glEnd();
    }

    private void drawLinesAsQuads(int numPts, float[] pts, float width, boolean drawAsLoop) {
        width *= 0.5f;
        GL11.glBegin(GL11.GL_QUADS);
        for(int i = 1 ; i < numPts ; i++) {
            drawLineAsQuad(pts[i * 2 - 2], pts[i * 2 - 1], pts[i * 2 + 0], pts[i * 2 + 1], width);
        }
        if(drawAsLoop) {
            int idx = numPts * 2;
            drawLineAsQuad(pts[idx], pts[idx + 1], pts[0], pts[1], width);
        }
        GL11.glEnd();
    }

    private static void drawLineAsQuad(float x0, float y0, float x1, float y1, float w) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        float l = (float)Math.sqrt(dx*dx + dy*dy) / w;
        dx /= l;
        dy /= l;
        GL11.glVertex2f(x0 - dx + dy, y0 - dy - dx);
        GL11.glVertex2f(x0 - dx - dy, y0 - dy + dx);
        GL11.glVertex2f(x1 + dx - dy, y1 + dy + dx);
        GL11.glVertex2f(x1 + dx + dy, y1 + dy - dx);
    }

    protected void prepareForRendering() {
        hasScissor = false;
        tintStack = tintStateRoot;
        clipStack.clearStack();
    }

    protected void renderSWCursor() {
        if(swCursor != null) {
            tintStack = tintStateRoot;
            swCursor.render(mouseX, mouseY);
        }
    }
    
    protected void setNativeCursor(Cursor cursor) throws LWJGLException {
        Mouse.setNativeCursor(cursor);
    }
    
    protected boolean isMouseInsideWindow() {
        return Mouse.isInsideWindow();
    }

    protected void getTintedColor(Color color, float[] result) {
        result[0] = tintStack.r*color.getRed();
        result[1] = tintStack.g*color.getGreen();
        result[2] = tintStack.b*color.getBlue();
        result[3] = tintStack.a*color.getAlpha();
    }
    
    /**
     * Computes the tinted color from the given color.
     * @param color the input color in RGBA order, value range is 0.0 (black) to 255.0 (white).
     * @param result the tinted color in RGBA order, can be the same array as color.
     */
    protected void getTintedColor(float[] color, float[] result) {
        result[0] = tintStack.r*color[0];
        result[1] = tintStack.g*color[1];
        result[2] = tintStack.b*color[2];
        result[3] = tintStack.a*color[3];
    }

    protected void setClipRect() {
        final Rect rect = clipRectTemp;
        if(clipStack.getClipRect(rect)) {
            GL11.glScissor(viewportX + rect.getX(), viewportBottom - rect.getBottom(), rect.getWidth(), rect.getHeight());
            if(!hasScissor) {
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
                hasScissor = true;
            }
        } else if(hasScissor) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            hasScissor = false;
        }
    }

    /**
     * Retrieves the active clip region from the top of the stack
     * @param rect the rect coordinates - may not be updated when clipping is disabled
     * @return true if clipping is active, false if clipping is disabled
     */
    public boolean getClipRect(Rect rect) {
        return clipStack.getClipRect(rect);
    }

    Logger getLogger() {
        return Logger.getLogger(LWJGLRenderer.class.getName());
    }
    
    /**
     * If the passed value is not a power of 2 then return the next highest power of 2
     * otherwise the value is returned unchanged.
     * 
     * <p> Warren Jr., Henry S. (2002). Hacker's Delight. Addison Wesley. pp. 48. ISBN 978-0201914658</p>
     * 
     * @param i a non negative number &lt;= 2^31
     * @return the smallest power of 2 which is &gt;= i
     */
    private static int nextPowerOf2(int i) {
        i--;
        i |= (i >>  1);
        i |= (i >>  2);
        i |= (i >>  4);
        i |= (i >>  8);
        i |= (i >> 16);
        return i+1;
    }

    private static class SWCursorAnimState implements AnimationState {
        private final long[] lastTime;
        private final boolean[] active;

        SWCursorAnimState() {
            lastTime = new long[3];
            active = new boolean[3];
        }

        void setAnimationState(int idx, boolean isActive) {
            if(idx >= 0 && idx < 3 && active[idx] != isActive) {
                lastTime[idx] = Sys.getTime();
                active[idx] = isActive;
            }
        }

        public int getAnimationTime(StateKey state) {
            long curTime = Sys.getTime();
            int idx = getMouseButton(state);
            if(idx >= 0) {
                curTime -= lastTime[idx];
            }
            return (int)curTime & Integer.MAX_VALUE;
        }

        public boolean getAnimationState(StateKey state) {
            int idx = getMouseButton(state);
            if(idx >= 0) {
                return active[idx];
            }
            return false;
        }

        public boolean getShouldAnimateState(StateKey state) {
            return true;
        }

        private int getMouseButton(StateKey key) {
            if(key == STATE_LEFT_MOUSE_BUTTON) {
                return Event.MOUSE_LBUTTON;
            }
            if(key == STATE_MIDDLE_MOUSE_BUTTON) {
                return Event.MOUSE_MBUTTON;
            }
            if(key == STATE_RIGHT_MOUSE_BUTTON) {
                return Event.MOUSE_RBUTTON;
            }
            return -1;
        }
    }
}
