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
 *
 * @author Matthias Mann
 */
public interface TableSelectionModel {

    public void rowsInserted(int index, int count);

    public void rowsDeleted(int index, int count);
    
    public void clearSelection();

    /**
     * Sets the selection to the given interval (both indices inclusive).
     * Single selection should use index1.
     *
     * @param index0 the start index of the interval.
     * @param index1 the end index of the interval.
     */
    public void setSelection(int index0, int index1);
    
    /**
     * Adds the given interval (both indices inclusive) to the selection.
     * Single selection should use index1.
     *
     * @param index0 the start index of the interval.
     * @param index1 the end index of the interval.
     */
    public void addSelection(int index0, int index1);

    /**
     * Inverts the given interval (both indices inclusive) in the selection.
     * Single selection should use index1.
     *
     * @param index0 the start index of the interval.
     * @param index1 the end index of the interval.
     */
    public void invertSelection(int index0, int index1);

    /**
     * Removes the given interval (both indices inclusive) from the selection.
     * Single selection should clear the selection.
     *
     * @param index0 the start index of the interval.
     * @param index1 the end index of the interval.
     */
    public void removeSelection(int index0, int index1);

    public int getLeadIndex();

    public int getAnchorIndex();

    public void setLeadIndex(int index);

    public void setAnchorIndex(int index);

    public boolean isSelected(int index);

    public boolean hasSelection();
    
    public int getFirstSelected();

    public int getLastSelected();
    
    public int[] getSelection();

    public void addSelectionChangeListener(Runnable cb);

    public void removeSelectionChangeListener(Runnable cb);
    
}
