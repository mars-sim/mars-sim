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
package de.matthiasmann.twl.model;

import de.matthiasmann.twl.utils.CallbackSupport;

/**
 * An {@code EditFieldModel} based on a {@link StringBuilder}
 * 
 * @author Matthias Mann
 */
public class DefaultEditFieldModel implements EditFieldModel {

    private final StringBuilder sb;
    private Callback[] callbacks;

    public DefaultEditFieldModel() {
        this.sb = new StringBuilder();
    }

    public int length() {
        return sb.length();
    }

    public char charAt(int index) {
        return sb.charAt(index);
    }

    public CharSequence subSequence(int start, int end) {
        return sb.subSequence(start, end);
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    public void addCallback(Callback callback) {
        callbacks = CallbackSupport.addCallbackToList(callbacks, callback, Callback.class);
    }

    public void removeCallback(Callback callback) {
        callbacks = CallbackSupport.removeCallbackFromList(callbacks, callback);
    }

    public int replace(int start, int count, String replacement) {
        checkRange(start, count);
        int replacementLength = replacement.length();
        if(count > 0 || replacementLength > 0) {
            sb.replace(start, start+count, replacement);
            fireCallback(start, count, replacementLength);
        }
        return replacementLength;
    }

    public boolean replace(int start, int count, char replacement) {
        checkRange(start, count);
        if(count == 0) {
            sb.insert(start, replacement);
        } else {
            sb.delete(start, start+count-1);
            sb.setCharAt(start, replacement);
        }
        fireCallback(start, count, 1);
        return true;
    }

    public String substring(int start, int end) {
        return sb.substring(start, end);
    }

    private void checkRange(int start, int count) {
        int len = sb.length();
        if(start < 0 || start > len) {
            throw new StringIndexOutOfBoundsException(start);
        }
        if(count < 0 || count > len - start) {
            throw new StringIndexOutOfBoundsException();
        }
    }

    private void fireCallback(int start, int oldCount, int newCount) {
        Callback[] cbs = this.callbacks;
        if(cbs != null) {
            for(Callback cb : cbs) {
                cb.charactersChanged(start, oldCount, newCount);
            }
        }
    }
}
