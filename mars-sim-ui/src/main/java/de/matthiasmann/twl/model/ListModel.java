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

/**
 * @author Matthias Mann
 * 
 * @param <T> The type of a list entry
 */
public interface ListModel<T> {

    public interface ChangeListener {
        /**
         * New entries have been inserted. The existing entries starting at first
         * have been shifted. The range first-last (inclusive) are new.
         * 
         * @param first the first new entry
         * @param last the last new entry. Must be >= first.
         */
        public void entriesInserted(int first, int last);
        
        /**
         * Entries that were at the range first to last (inclusive) have been removed.
         * Entries that were following last (starting with last+1) have been shifted
         * to first.
         * @param first the first removed entry
         * @param last the last removed entry. Must be >= first.
         */
        public void entriesDeleted(int first, int last);
        
        /**
         * Entries in the range first to last (inclusive) have been changed.
         * @param first the first changed entry
         * @param last the last changed entry. Must be >= first.
         */
        public void entriesChanged(int first, int last);
        
        /**
         * The complete list was recreated. There is no known relation between
         * old and new entries. Also the number of entries has complete changed.
         */
        public void allChanged();
    }
    
    public int getNumEntries();
    
    public T getEntry(int index);
    
    public Object getEntryTooltip(int index);
    
    public boolean matchPrefix(int index, String prefix);
    
    public void addChangeListener(ChangeListener listener);
    
    public void removeChangeListener(ChangeListener listener);
    
}
