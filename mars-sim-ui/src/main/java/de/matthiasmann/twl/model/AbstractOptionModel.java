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

import de.matthiasmann.twl.utils.CallbackSupport;

/**
 * A base class for option boolean model.
 *
 * This class handles the callback filtering from the source model.
 * The callback on the source model is installed when the first
 * callback on this model has been added, and is removed again
 * when the last callback has been removed. This allows instances
 * of {@code AbstractOptionModel} to be GCed when no longer needed.
 * 
 * Without this dynamic subscription the callback on the source model
 * would form a cycle of strong references between the
 * {@code AbstractOptionModel} instance and it's source model which
 * would prevent all instances from beeing GCed until the source model
 * is also GCed.
 *
 * @author Matthias Mann
 */
public abstract class AbstractOptionModel implements BooleanModel {

    Runnable[] callbacks;
    Runnable srcCallback;

    public void addCallback(Runnable callback) {
        if(callback == null) {
            throw new NullPointerException("callback");
        }
        if(callbacks == null) {
            srcCallback = new Runnable() {
                boolean lastValue = getValue();
                public void run() {
                    boolean value = getValue();
                    if(lastValue != value) {
                        lastValue = value;
                        CallbackSupport.fireCallbacks(callbacks);
                    }
                }
            };
            callbacks = new Runnable[] { callback };
            installSrcCallback(srcCallback);
        } else {
            callbacks = CallbackSupport.addCallbackToList(callbacks, callback, Runnable.class);
        }
    }

    public void removeCallback(Runnable callback) {
        callbacks = CallbackSupport.removeCallbackFromList(callbacks, callback);
        if(callbacks == null && srcCallback != null) {
            removeSrcCallback(srcCallback);
            srcCallback = null;
        }
    }

    protected abstract void installSrcCallback(Runnable cb);
    protected abstract void removeSrcCallback(Runnable cb);
}
