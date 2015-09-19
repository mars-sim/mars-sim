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
 * A single selection model
 * 
 * @author Matthias Mann
 */
public class TableSingleSelectionModel extends AbstractTableSelectionModel {

    public static final int NO_SELECTION = -1;

    private int selection;

    @Override
    public void rowsInserted(int index, int count) {
        boolean changed = false;
        if(selection >= index) {
            selection += count;
            changed = true;
        }
        super.rowsInserted(index, count);
        if(changed) {
            fireSelectionChange();
        }
    }

    @Override
    public void rowsDeleted(int index, int count) {
        boolean changed = false;
        if(selection >= index) {
            if(selection < index + count) {
                selection = NO_SELECTION;
            } else {
                selection -= count;
            }
            changed = true;
        }
        super.rowsDeleted(index, count);
        if(changed) {
            fireSelectionChange();
        }
    }

    public void clearSelection() {
        if(hasSelection()) {
            selection = NO_SELECTION;
            fireSelectionChange();
        }
    }

    public void setSelection(int index0, int index1) {
        updateLeadAndAnchor(index0, index1);
        selection = index1;
        fireSelectionChange();
    }

    public void addSelection(int index0, int index1) {
        updateLeadAndAnchor(index0, index1);
        selection = index1;
        fireSelectionChange();
    }

    public void invertSelection(int index0, int index1) {
        updateLeadAndAnchor(index0, index1);
        if(selection == index1) {
            selection = NO_SELECTION;
        } else {
            selection = index1;
        }
        fireSelectionChange();
    }

    public void removeSelection(int index0, int index1) {
        updateLeadAndAnchor(index0, index1);
        if(hasSelection()) {
            int first = Math.min(index0, index1);
            int last = Math.max(index0, index1);
            if(selection >= first && selection <= last) {
                selection = NO_SELECTION;
            }
            fireSelectionChange();
        }
    }

    public boolean isSelected(int index) {
        return selection == index;
    }

    public boolean hasSelection() {
        return selection >= 0;
    }

    public int getFirstSelected() {
        return selection;
    }

    public int getLastSelected() {
        return selection;
    }

    public int[] getSelection() {
        if(selection >= 0) {
            return new int[] { selection };
        }
        return new int[0];
    }

}
