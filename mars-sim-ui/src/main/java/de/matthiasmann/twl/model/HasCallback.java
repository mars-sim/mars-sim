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
package de.matthiasmann.twl.model;

import de.matthiasmann.twl.utils.CallbackSupport;
import de.matthiasmann.twl.utils.WithRunnableCallback;

/**
 * A class to manage callbacks.
 *
 * @author Matthias Mann
 */
public class HasCallback implements WithRunnableCallback {

    private Runnable[] callbacks;

    public HasCallback() {
    }
    
    /**
     * Adds a callback to the list.
     * @param callback the callback
     */
    public void addCallback(Runnable callback) {
        callbacks = CallbackSupport.addCallbackToList(callbacks, callback, Runnable.class);
    }

    /**
     * Removes a callback from the list.
     * @param callback the callback that should be removed
     */
    public void removeCallback(Runnable callback) {
        callbacks = CallbackSupport.removeCallbackFromList(callbacks, callback);
    }

    /**
     * Returns true when the callback list is not empty
     * @return true when the callback list is not empty
     */
    public boolean hasCallbacks() {
        return callbacks != null;
    }
    
    /**
     * Calls all registered callbacks.
     *
     * Callbacks can call {@code addCallback} or {@code removeCallback}.
     * Modification to the callback list will only be visible to the next
     * {@code doCallback} call.
     *
     * @see #addCallback(java.lang.Runnable)
     * @see #removeCallback(java.lang.Runnable)
     */
    protected void doCallback() {
        CallbackSupport.fireCallbacks(callbacks);
    }
}
