/*
 * Copyright (c) 2008-2011, Matthias Mann
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

import java.util.Collection;

/**
 * The debug hook class can be used to retrieve more detailed information
 * about missing themes or parameters.
 *
 * @author Matthias Mann
 */
public class DebugHook {

    private static ThreadLocal<DebugHook> tls = new ThreadLocal<DebugHook>() {
        @Override
        protected DebugHook initialValue() {
            return new DebugHook();
        }
    };

    /**
     * Returns the currently active debug hook for this thread.
     * @return the debug hook. Never null.
     */
    public static DebugHook getDebugHook() {
        return tls.get();
    }

    /**
     * Installs a new debug hook.
     *
     * @param hook the new debug hook
     * @return the previous debug hook
     * @throws NullPointerException if hook is null
     */
    public static DebugHook installHook(DebugHook hook) {
        if(hook == null) {
            throw new NullPointerException("hook");
        }
        DebugHook old = tls.get();
        tls.set(hook);
        return old;
    }

    public void beforeApplyTheme(Widget widget) {
    }

    public void afterApplyTheme(Widget widget) {
    }

    public void missingTheme(String themePath) {
        System.err.println("Could not find theme: " + themePath);
    }
    
    public void missingChildTheme(ThemeInfo parent, String theme) {
        System.err.println("Missing child theme \"" + theme + "\" for \"" + parent.getThemePath() + "\"");
    }

    public void missingParameter(ParameterMap map, String paramName, String parentDescription, Class<?> dataType) {
        StringBuilder sb = new StringBuilder("Parameter \"").append(paramName).append("\" ");
        if(dataType != null) {
            sb.append("of type ");
            if(dataType.isEnum()) {
                sb.append("enum ");
            }
            sb.append('"').append(dataType.getSimpleName()).append('"');
        }
        sb.append(" not set");
        if(map instanceof ThemeInfo) {
            sb.append(" for \"").append(((ThemeInfo)map).getThemePath()).append("\"");
        } else {
            sb.append(parentDescription);
        }
        System.err.println(sb.toString());
    }

    public void wrongParameterType(ParameterMap map, String paramName, Class<?> expectedType, Class<?> foundType, String parentDescription) {
        System.err.println("Parameter \"" + paramName + "\" is a " +
                foundType.getSimpleName() + " expected a " +
                expectedType.getSimpleName() + parentDescription);
    }

    public void wrongParameterType(ParameterList map, int idx, Class<?> expectedType, Class<?> foundType, String parentDescription) {
        System.err.println("Parameter at index " + idx + " is a " +
                foundType.getSimpleName() + " expected a " +
                expectedType.getSimpleName() + parentDescription);
    }

    public void replacingWithDifferentType(ParameterMap map, String paramName, Class<?> oldType, Class<?> newType, String parentDescription) {
        System.err.println("Paramter \"" + paramName + "\" of type " +
                oldType + " is replaced with type " + newType + parentDescription);
    }

    public void missingImage(String name) {
        System.err.println("Could not find image: " + name);
    }

    /**
     * Called when GUI has validated the layout tree
     * @param iterations the number of iterations required to solve layout
     * @param loop the widgets involved in a layout loop if the layout could not be solved - is null if layout was solved
     */
    public void guiLayoutValidated(int iterations, Collection<Widget> loop) {
        if(loop != null) {
            System.err.println("WARNING: layout loop detected - printing");
            int index = 1;
            for(Widget w : loop) {
                System.err.println(index+": "+w);
                index++;
            }
        }
    }

    /**
     * Called when wildcard resolution failed to find a theme and the fallback theme was specified
     * @param themePath the requested theme name
     */
    public void usingFallbackTheme(String themePath) {
        System.err.println("Selected fallback theme for missing theme \"" + themePath + "\"");
    }
}
