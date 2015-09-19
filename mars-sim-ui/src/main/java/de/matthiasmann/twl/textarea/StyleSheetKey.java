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
package de.matthiasmann.twl.textarea;

/**
 *
 * @author Matthias Mann
 */
public class StyleSheetKey {

    final String element;
    final String className;
    final String id;

    public StyleSheetKey(String element, String className, String id) {
        this.element = element;
        this.className = className;
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public String getElement() {
        return element;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof StyleSheetKey) {
            final StyleSheetKey other = (StyleSheetKey)obj;
            return ((this.element == null) ? (other.element == null) : this.element.equals(other.element)) &&
                    ((this.className == null) ? (other.className == null) : this.className.equals(other.className)) &&
                    ((this.id == null) ? (other.id == null) : this.id.equals(other.id));
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.element != null ? this.element.hashCode() : 0);
        hash = 53 * hash + (this.className != null ? this.className.hashCode() : 0);
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    public boolean matches(StyleSheetKey what) {
        if(this.element != null && !this.element.equals(what.element)) {
            return false;
        }
        if(this.className != null && !this.className.equals(what.className)) {
            return false;
        }
        if(this.id != null && !this.id.equals(what.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append(element);
        if(className != null) {
            sb.append('.').append(className);
        }
        if(id != null) {
            sb.append('#').append(id);
        }
        return sb.toString();
    }
}
