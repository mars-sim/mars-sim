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
package test;

import java.security.AccessControlException;
import java.util.HashMap;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.Preferences;

/**
 * A non persistent implementation of the Preferences API to get the demos
 * running as an unsigend applet.
 *
 * @author Matthias Mann
 */
public class AppletPreferences extends AbstractPreferences {

    public static Preferences userNodeForPackage(Class<?> clazz) {
        try {
            return Preferences.userNodeForPackage(clazz);
        } catch (AccessControlException ex) {
            return root.node(clazz.getName().replace('.', '/'));
        }
    }

    private AppletPreferences(AbstractPreferences parent, String name) {
        super(parent, name);
    }

    private static final AppletPreferences root = new AppletPreferences(null, "");
    private final HashMap<String, String> values = new HashMap<String, String>();
    
    @Override
    protected AbstractPreferences childSpi(String name) {
        // this depends on the kidCache in AbstractPreferences
        return new AppletPreferences(this, name);
    }

    @Override
    protected String[] childrenNamesSpi() {
        // this depends on the kidCache in AbstractPreferences
        return new String[0];
    }

    @Override
    protected void flushSpi() {
    }

    @Override
    protected String getSpi(String key) {
        return values.get(key);
    }

    @Override
    protected String[] keysSpi() {
        return values.keySet().toArray(new String[0]);
    }

    @Override
    protected void putSpi(String key, String value) {
        values.put(key, value);
    }

    @Override
    protected void removeNodeSpi() {
    }

    @Override
    protected void removeSpi(String key) {
        values.remove(key);
    }

    @Override
    protected void syncSpi() {
    }
}
