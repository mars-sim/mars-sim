/*
 * Copyright (c) 2008-2012, Matthias Mann
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

import de.matthiasmann.twl.renderer.AnimationState.StateKey;
import de.matthiasmann.twl.utils.TextUtil;
import de.matthiasmann.twl.renderer.Font;
import de.matthiasmann.twl.renderer.FontCache;

/**
 * Generic text display widget. Supports caching of text for faster rendering.
 *
 * @author Matthias Mann
 */
public class TextWidget extends Widget {

    public static final StateKey STATE_HOVER = StateKey.get("hover");
    public static final StateKey STATE_TEXT_CHANGED = StateKey.get("textChanged");
    public static final StateKey STATE_TEXT_SELECTION = StateKey.get("textSelection");

    private static final int NOT_CACHED = -1;

    private Font font;
    private FontCache cache;
    private CharSequence text;
    private int cachedTextWidth = NOT_CACHED;
    private int numTextLines;
    private boolean useCache = true;
    private boolean cacheDirty;
    private Alignment alignment = Alignment.TOPLEFT;

    public TextWidget() {
        this(null, false);
    }

    /**
     * Creates a TextWidget with a shared animation state
     *
     * @param animState the animation state to share, can be null
     */
    public TextWidget(AnimationState animState) {
        this(animState, false);
    }
    
    /**
     * Creates a TextWidget with a shared or inherited animation state
     *
     * @param animState the animation state to share or inherit, can be null
     * @param inherit true if the animation state should be inherited false for sharing
     */
    public TextWidget(AnimationState animState, boolean inherit) {
        super(animState, inherit);
        this.text = "";
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        if(cache != null) {
            cache.destroy();
            cache = null;
        }
        this.font = font;
        this.cachedTextWidth = NOT_CACHED;
        if(useCache) {
            this.cacheDirty = true;
        }
    }

    /**
     * Sets a new text to be displayed.
     * If CharSequence changes it's content this function must be called
     * again or correct rendering can't be guranteed.
     *
     * @param text The CharSequence to display
     */
    protected void setCharSequence(CharSequence text) {
        if(text == null) {
            throw new NullPointerException("text");
        }
        this.text = text;
        this.cachedTextWidth = NOT_CACHED;
        this.numTextLines = TextUtil.countNumLines(text);
        this.cacheDirty = true;
        getAnimationState().resetAnimationTime(STATE_TEXT_CHANGED);
    }

    protected CharSequence getCharSequence() {
        return text;
    }

    public boolean hasText() {
        return numTextLines > 0;
    }

    public boolean isMultilineText() {
        return numTextLines > 1;
    }

    public int getNumTextLines() {
        return numTextLines;
    }
    
    public Alignment getAlignment() {
        return alignment;
    }

    public void setAlignment(Alignment alignment) {
        if(alignment == null) {
            throw new NullPointerException("alignment");
        }
        if(this.alignment != alignment) {
            this.alignment = alignment;
            this.cacheDirty = true;
        }
    }

    public boolean isCache() {
        return useCache;
    }

    public void setCache(boolean cache) {
        if(this.useCache != cache) {
            this.useCache = cache;
            this.cacheDirty = true;
        }
    }

    protected void applyThemeTextWidget(ThemeInfo themeInfo) {
        setFont(themeInfo.getFont("font"));
        setAlignment(themeInfo.getParameter("textAlignment", Alignment.TOPLEFT));
    }
    
    @Override
    protected void applyTheme(ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        applyThemeTextWidget(themeInfo);
    }

    @Override
    public void destroy() {
        if(cache != null) {
            cache.destroy();
            cache = null;
        }
        super.destroy();
    }

    protected int computeTextX() {
        int x = getInnerX();
        int pos = alignment.hpos;
        if(pos > 0) {
            return x + (getInnerWidth() - computeTextWidth()) * pos / 2;
        }
        return x;
    }

    protected int computeTextY() {
        int y = getInnerY();
        int pos = alignment.vpos;
        if(pos > 0) {
            return y + (getInnerHeight() - computeTextHeight()) * pos / 2;
        }
        return y;
    }

    @Override
    protected void paintWidget(GUI gui) {
        paintLabelText(getAnimationState());
    }

    protected void paintLabelText(de.matthiasmann.twl.renderer.AnimationState animState) {
        if(cacheDirty) {
            updateCache();
        }
        if(hasText() && font != null) {
            int x = computeTextX();
            int y = computeTextY();
            
            paintTextAt(animState, x, y);
        }
    }

    protected void paintTextAt(de.matthiasmann.twl.renderer.AnimationState animState, int x, int y) {
        if(cache != null) {
            cache.draw(animState, x, y);
        } else if(numTextLines > 1) {
            font.drawMultiLineText(animState, x, y, text, computeTextWidth(), alignment.fontHAlignment);
        } else {
            font.drawText(animState, x, y, text);
        }
    }

    protected void paintWithSelection(AnimationState animState, int start, int end) {
        paintWithSelection(animState, start, end, 0, text.length(), computeTextY());
    }
    
    protected void paintWithSelection(AnimationState animState, int start, int end, int lineStart, int lineEnd, int y) {
        if(cacheDirty) {
            updateCache();
        }
        if(hasText() && font != null) {
            int x = computeTextX();

            start = limit(start, lineStart, lineEnd);
            end = limit(end, lineStart, lineEnd);
            
            if(start > lineStart) {
                x += font.drawText(animState, x, y, text, lineStart, start);
            }
            if(end > start) {
                animState.setAnimationState(STATE_TEXT_SELECTION, true);
                x += font.drawText(animState, x, y, text, start, end);
                animState.setAnimationState(STATE_TEXT_SELECTION, false);
            }
            if(end < lineEnd) {
                font.drawText(animState, x, y, text, end, lineEnd);
            }
        }
    }

    private static int limit(int value, int min, int max) {
        if(value < min) {
            return min;
        }
        if(value > max) {
            return max;
        }
        return value;
    }

    @Override
    public int getPreferredInnerWidth() {
        int prefWidth = super.getPreferredInnerWidth();
        if(hasText() && font != null) {
            prefWidth = Math.max(prefWidth, computeTextWidth());
        }
        return prefWidth;
    }

    @Override
    public int getPreferredInnerHeight() {
        int prefHeight = super.getPreferredInnerHeight();
        if(hasText() && font != null) {
            prefHeight = Math.max(prefHeight, computeTextHeight());
        }
        return prefHeight;
    }

    public int computeRelativeCursorPositionX(int charIndex) {
        return computeRelativeCursorPositionX(0, charIndex);
    }

    public int computeRelativeCursorPositionX(int startIndex, int charIndex) {
        if(font != null && charIndex > startIndex) {
            return font.computeTextWidth(text, startIndex, charIndex);
        }
        return 0;
    }

    public int computeTextWidth() {
        if(font != null) {
            if(cachedTextWidth == NOT_CACHED || cacheDirty) {
                if(numTextLines > 1) {
                    cachedTextWidth = font.computeMultiLineTextWidth(text);
                } else {
                    cachedTextWidth = font.computeTextWidth(text);
                }
            }
            return cachedTextWidth;
        }
        return 0;
    }

    public int computeTextHeight() {
        if(font != null) {
            return Math.max(1, numTextLines) * font.getLineHeight();
        }
        return 0;
    }

    private void updateCache() {
        cacheDirty = false;
        if(useCache && hasText() && font != null) {
            if(numTextLines > 1) {
                cache = font.cacheMultiLineText(cache, text,
                        font.computeMultiLineTextWidth(text),
                        alignment.fontHAlignment);
            } else {
                cache = font.cacheText(cache, text);
            }
            if(cache != null) {
                cachedTextWidth = cache.getWidth();
            }
        } else {
            destroy();
        }
    }

    protected void handleMouseHover(Event evt) {
        if(evt.isMouseEvent() && !hasSharedAnimationState()) {
            getAnimationState().setAnimationState(STATE_HOVER, evt.getType() != Event.Type.MOUSE_EXITED);
        }
    }
}
