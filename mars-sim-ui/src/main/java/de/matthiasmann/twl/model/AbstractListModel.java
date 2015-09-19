/*
 * Copyright (c) 2008, Matthias Mann
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
 * Abstract base class to simplify implementating ListModels.
 *
 * @param <T> the type of a list entry
 *
 * @author Matthias Mann
 */
public abstract class AbstractListModel<T> implements ListModel<T> {

    private ChangeListener[] listeners;
    
    public void addChangeListener(ChangeListener listener) {
        listeners = CallbackSupport.addCallbackToList(listeners, listener, ChangeListener.class);
    }

    public void removeChangeListener(ChangeListener listener) {
        listeners = CallbackSupport.removeCallbackFromList(listeners, listener);
    }

    protected void fireEntriesInserted(int first, int last) {
        if(listeners != null) {
            for(ChangeListener cl : listeners) {
                cl.entriesInserted(first, last);
            }
        }
    }

    protected void fireEntriesDeleted(int first, int last) {
        if(listeners != null) {
            for(ChangeListener cl : listeners) {
                cl.entriesDeleted(first, last);
            }
        }
    }

    protected void fireEntriesChanged(int first, int last) {
        if(listeners != null) {
            for(ChangeListener cl : listeners) {
                cl.entriesChanged(first, last);
            }
        }
    }

    protected void fireAllChanged() {
        if(listeners != null) {
            for(ChangeListener cl : listeners) {
                cl.allChanged();
            }
        }
    }
}
