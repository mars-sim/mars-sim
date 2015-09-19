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
package de.matthiasmann.twl.renderer;

import de.matthiasmann.twl.Widget;

/**
 * An interface to allow offscreen rendering for special effects
 * 
 * @author Matthias Mann
 */
public interface OffscreenRenderer {
    
    /**
     * Starts offscreen rendering. All following rendering operations will render
     * into the returned offscreen surface. Rendering outside the specified area
     * will be ignored.
     * 
     * @param widget the widget which will render to the returned surface - can be null.
     * @param oldSurface the previous offscreen surface to reuse / overwrite
     * @param x the X coordinate of the region, can be negative.
     * @param y the Y coordinate of the region, can be negative.
     * @param width the width, can be larger then the screen size
     * @param height the height, can be larger then the screen size
     * @return the OffscreenSurface or null if offscreen rendering could not be started.
     */
    public OffscreenSurface startOffscreenRendering(Widget widget,
            OffscreenSurface oldSurface, int x, int y, int width, int height);
    
    /**
     * Ends the current offscreen rendering.
     * Only call this method after a sucessful call of
     * {@link #startOffscreenRendering(de.matthiasmann.twl.renderer.OffscreenSurface, int, int, int, int) }
     */
    public void endOffscreenRendering();
}
