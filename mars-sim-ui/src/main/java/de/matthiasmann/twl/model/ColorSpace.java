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
package de.matthiasmann.twl.model;

/**
 * A color space used by the color selector widget.
 *
 * It supports a variable number of color components.
 * It does not include an alpha channel.
 * 
 * @author Matthias Mann
 */
public interface ColorSpace {

    public String getColorSpaceName();

    /**
     * Returns the number of component for this model. Must be >= 3.
     * @return the number of component for this model. Must be >= 3.
     */
    public int getNumComponents();

    /**
     * Returns the name of the specified color component.
     *
     * @param component the color component index
     * @return the name of the color component
     */
    public String getComponentName(int component);

    /**
     * A short version of the component name for use in UIs. For best results
     * all short names should have the same length.
     * 
     * @param component the color component index
     * @return the name of the color component
     */
    public String getComponentShortName(int component);

    /**
     * Returns the minimum allowed value for the specified component.
     *
     * @param component the color component index
     * @return the minimum value
     */
    public float getMinValue(int component);

    /**
     * Returns the maximum allowed value for the specified component.
     *
     * @param component the color component index
     * @return the maximum value
     */
    public float getMaxValue(int component);

    /**
     * Returns the default component for the initial color
     * @param component the color component index
     * @return the default value
     */
    public float getDefaultValue(int component);

    /**
     * Converts the specified color into a RGB value without alpha part.
     * This convertion is not exact.
     * 
     * bits  0- 7 are blue
     * bits  8-15 are green
     * bits 16-23 are red
     * bits 24-31 must be 0
     *
     * @param color the color values
     * @return the RGB value
     */
    public int toRGB(float[] color);

    /**
     * Converts the given RGB value into color values for this color space.
     *
     * @param rgb the RGB value
     * @return the color values corespondig to the RGB value
     * @see #toRGB(float[]) 
     */
    public float[] fromRGB(int rgb);
    
}
