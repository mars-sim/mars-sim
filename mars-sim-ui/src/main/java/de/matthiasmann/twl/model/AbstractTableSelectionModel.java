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

/**
 *
 * @author Matthias Mann
 */
public abstract class AbstractTableSelectionModel implements TableSelectionModel {

    protected int leadIndex;
    protected int anchorIndex;
    protected Runnable[] selectionChangeListener;

    protected AbstractTableSelectionModel() {
        this.leadIndex = -1;
        this.anchorIndex = -1;
    }

    public int getAnchorIndex() {
        return anchorIndex;
    }

    public int getLeadIndex() {
        return leadIndex;
    }

    public void setAnchorIndex(int index) {
        anchorIndex = index;
    }

    public void setLeadIndex(int index) {
        leadIndex = index;
    }

    public void addSelectionChangeListener(Runnable cb) {
        selectionChangeListener = CallbackSupport.addCallbackToList(selectionChangeListener, cb, Runnable.class);
    }

    public void removeSelectionChangeListener(Runnable cb) {
        selectionChangeListener = CallbackSupport.removeCallbackFromList(selectionChangeListener, cb);
    }

    public void rowsDeleted(int index, int count) {
        if(leadIndex >= index) {
            leadIndex = Math.max(index, leadIndex - count);
        }
        if(anchorIndex >= index) {
            anchorIndex = Math.max(index, anchorIndex - count);
        }
    }

    public void rowsInserted(int index, int count) {
        if(leadIndex >= index) {
            leadIndex += count;
        }
        if(anchorIndex >= index) {
            anchorIndex += count;
        }
    }

    protected void fireSelectionChange() {
        CallbackSupport.fireCallbacks(selectionChangeListener);
    }
    
    protected void updateLeadAndAnchor(int index0, int index1) {
        anchorIndex = index0;
        leadIndex = index1;
    }
    
}
