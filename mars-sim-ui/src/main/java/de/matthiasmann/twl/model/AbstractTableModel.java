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

/**
 *
 * @author Matthias Mann
 */
public abstract class AbstractTableModel extends AbstractTableColumnHeaderModel implements TableModel {

    private ChangeListener[] callbacks;

    public Object getTooltipContent(int row, int column) {
        return null;
    }

    public void addChangeListener(ChangeListener listener) {
        callbacks = CallbackSupport.addCallbackToList(callbacks, listener, ChangeListener.class);
    }

    public void removeChangeListener(ChangeListener listener) {
        callbacks = CallbackSupport.removeCallbackFromList(callbacks, listener);
    }

    protected boolean hasCallbacks() {
        return callbacks != null;
    }
    
    protected void fireRowsInserted(int idx, int count) {
        if(callbacks != null) {
            for(ChangeListener cl : callbacks) {
                cl.rowsInserted(idx, count);
            }
        }
    }

    protected void fireRowsDeleted(int idx, int count) {
        if(callbacks != null) {
            for(ChangeListener cl : callbacks) {
                cl.rowsDeleted(idx, count);
            }
        }
    }

    protected void fireRowsChanged(int idx, int count) {
        if(callbacks != null) {
            for(ChangeListener cl : callbacks) {
                cl.rowsChanged(idx, count);
            }
        }
    }

    protected void fireColumnInserted(int idx, int count) {
        if(callbacks != null) {
            for(ChangeListener cl : callbacks) {
                cl.columnInserted(idx, count);
            }
        }
    }

    protected void fireColumnDeleted(int idx, int count) {
        if(callbacks != null) {
            for(ChangeListener cl : callbacks) {
                cl.columnDeleted(idx, count);
            }
        }
    }

    protected void fireColumnHeaderChanged(int column) {
        if(callbacks != null) {
            for(ChangeListener cl : callbacks) {
                cl.columnHeaderChanged(column);
            }
        }
    }

    protected void fireCellChanged(int row, int column) {
        if(callbacks != null) {
            for(ChangeListener cl : callbacks) {
                cl.cellChanged(row, column);
            }
        }
    }

    protected void fireAllChanged() {
        if(callbacks != null) {
            for(ChangeListener cl : callbacks) {
                cl.allChanged();
            }
        }
    }
}
