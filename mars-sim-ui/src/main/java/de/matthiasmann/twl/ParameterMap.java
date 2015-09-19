/*
 * Copyright (c) 2008-2009, Matthias Mann
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

import de.matthiasmann.twl.renderer.MouseCursor;
import de.matthiasmann.twl.renderer.Font;
import de.matthiasmann.twl.renderer.Image;

/**
 *
 * @author Matthias Mann
 */
public interface ParameterMap {

    /**
     * Returns the font with the given name.
     * If no font with that name was found then the default font is returned.
     *
     * @param name The name of the font
     * @return A font object
     */
    public Font getFont(String name);

    /**
     * Returns the image with the given name.
     * If no image with that name was found then null is returned.
     *
     * @param name The name of the image.
     * @return A image object or null.
     */
    public Image getImage(String name);

    /**
     * Returns the mouse cursor with the given name.
     * If no mouse cursor with that name was found then null is returned.
     *
     * @param name The name of the mouse cursor.
     * @return A mouse cursor object or null.
     */
    public MouseCursor getMouseCursor(String name);

    /**
     * Returns a parameter map with the given name.
     * If no parameter map with that name was found then an empty map is returned.
     *
     * @param name The name of the parameter map.
     * @return A parameter map object.
     */
    public ParameterMap getParameterMap(String name);

    /**
     * Returns a parameter list with the given name.
     * If no parameter map with that name was found then an empty list is returned.
     *
     * @param name The name of the parameter list.
     * @return A parameter list object.
     */
    public ParameterList getParameterList(String name);

    public boolean getParameter(String name, boolean defaultValue);

    public int getParameter(String name, int defaultValue);

    public float getParameter(String name, float defaultValue);

    public String getParameter(String name, String defaultValue);

    public Color getParameter(String name, Color defaultValue);

    public <E extends Enum<E>> E getParameter(String name, E defaultValue);

    /**
     * Retrives a parameter.
     * @param name the parameter name
     * @param warnIfNotPresent if true and the parameter was not set then a warning is issued
     * @return the parameter value
     */
    public Object getParameterValue(String name, boolean warnIfNotPresent);

    /**
     * Retrieves a parameter and ensures that it has the desired type.
     * @param <T> The desired return type generic
     * @param name the parameter name
     * @param warnIfNotPresent if true a warning is generated if the parameter was not found or has wrong type
     * @param clazz the required data type
     * @return the parameter value or null if the type does not match
     */
    public <T> T getParameterValue(String name, boolean warnIfNotPresent, Class<T> clazz);

    /**
     * Retrieves a parameter and ensures that it has the desired type.
     * @param <T> The desired return type generic
     * @param name the parameter name
     * @param warnIfNotPresent if true a warning is generated if the parameter was not found or has wrong type
     * @param clazz the required data type
     * @param defaultValue the default value
     * @return the parameter value or the defaultValue if the type does not match
     */
    public <T> T getParameterValue(String name, boolean warnIfNotPresent, Class<T> clazz, T defaultValue);
}
