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
package de.matthiasmann.twl.model;

import de.matthiasmann.twl.utils.CallbackSupport;
import java.util.ArrayList;

/**
 * A non persistent MRU list implementation
 *
 * @param <T> the data type stored in this MRU model
 * 
 * @author Matthias Mann
 */
public class SimpleMRUListModel<T> implements MRUListModel<T> {

    protected final ArrayList<T> entries;
    protected final int maxEntries;
    protected ChangeListener[] listeners;

    public SimpleMRUListModel(int maxEntries) {
        if(maxEntries <= 1) {
            throw new IllegalArgumentException("maxEntries <= 1");
        }
        this.entries = new ArrayList<T>();
        this.maxEntries = maxEntries;
    }

    public int getMaxEntries() {
        return maxEntries;
    }

    public int getNumEntries() {
        return entries.size();
    }

    public T getEntry(int index) {
        return entries.get(index);
    }

    public void addEntry(T entry) {
        int idx = entries.indexOf(entry);
        if(idx >= 0) {
            doDeleteEntry(idx);
        } else if(entries.size() == maxEntries) {
            doDeleteEntry(maxEntries-1);
        }
        
        entries.add(0, entry);

        if(listeners != null) {
            for(ChangeListener cl : listeners) {
                cl.entriesInserted(0, 0);
            }
        }

        saveEntries();
    }

    public void removeEntry(int index) {
        if(index < 0 && index >= entries.size()) {
            throw new IndexOutOfBoundsException();
        }
        doDeleteEntry(index);
        
        saveEntries();
    }

    public void addChangeListener(ChangeListener listener) {
        listeners = CallbackSupport.addCallbackToList(listeners, listener, ChangeListener.class);
    }

    public void removeChangeListener(ChangeListener listener) {
        listeners = CallbackSupport.removeCallbackFromList(listeners, listener);
    }

    protected void doDeleteEntry(int idx) {
        entries.remove(idx);
        
        if(listeners != null) {
            for(ChangeListener cl : listeners) {
                cl.entriesDeleted(idx, idx);
            }
        }
    }

    protected void saveEntries() {
    }

    public Object getEntryTooltip(int index) {
        return null;
    }

    public boolean matchPrefix(int index, String prefix) {
        return false;
    }
}
