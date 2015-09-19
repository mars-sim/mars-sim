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

/**
 * A generic MRU (most recently used) list model.
 *
 * @param <T> the data type stored in this MRU model
 *
 * @author Matthias Mann
 */
public interface MRUListModel<T> extends ListModel<T> {

    /**
     * Returns the maximum number of entries stored in this MRU list
     * @return the maximum number of entries stored in this MRU list
     */
    public int getMaxEntries();
    
    public int getNumEntries();

    public T getEntry(int index);

    /**
     * Adds an entry to this MRU model. If the entry is already in the MRU list,
     * then it is moved to the from.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     * 
     * @param entry the entry
     */
    public void addEntry(T entry);

    /**
     * Removes the entry at the given index
     *
     * @param index
     */
    public void removeEntry(int index);

    public void addChangeListener(ListModel.ChangeListener listener);

    public void removeChangeListener(ListModel.ChangeListener listener);
}
