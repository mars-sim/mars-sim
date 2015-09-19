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

import java.util.BitSet;

/**
 * A table selection model for multi selection
 *
 * @author Matthias Mann
 */
public class DefaultTableSelectionModel extends AbstractTableSelectionModel {

    private final BitSet value;
    private int minIndex;
    private int maxIndex;
    
    public DefaultTableSelectionModel() {
        this.value = new BitSet();
        this.minIndex = Integer.MAX_VALUE;
        this.maxIndex = Integer.MIN_VALUE;
    }

    public int getFirstSelected() {
        return minIndex;
    }

    public int getLastSelected() {
        return maxIndex;
    }

    public boolean hasSelection() {
        return maxIndex >= minIndex;
    }

    public boolean isSelected(int index) {
        return value.get(index);
    }

    private void clearBit(int idx) {
        if(value.get(idx)) {
            value.clear(idx);

            if(idx == minIndex) {
                minIndex = value.nextSetBit(minIndex+1);
                if(minIndex < 0) {
                    minIndex = Integer.MAX_VALUE;
                    maxIndex = Integer.MIN_VALUE;
                    return;
                }
            }

            if(idx == maxIndex) {
                do {
                    maxIndex--;
                }while(maxIndex >= minIndex && !value.get(maxIndex));
            }
        }
    }

    private void setBit(int idx) {
        if(!value.get(idx)) {
            value.set(idx);

            if(idx < minIndex) {
                minIndex = idx;
            }
            if(idx > maxIndex) {
                maxIndex = idx;
            }
        }
    }

    private void toggleBit(int idx) {
        if(value.get(idx)) {
            clearBit(idx);
        } else {
            setBit(idx);
        }
    }

    public void clearSelection() {
        if(hasSelection()) {
            minIndex = Integer.MAX_VALUE;
            maxIndex = Integer.MIN_VALUE;
            value.clear();
            fireSelectionChange();
        }
    }

    public void setSelection(int index0, int index1) {
        updateLeadAndAnchor(index0, index1);
        minIndex = Math.min(index0, index1);
        maxIndex = Math.max(index0, index1);
        value.clear();
        value.set(minIndex, maxIndex+1);
        fireSelectionChange();
    }

    public void addSelection(int index0, int index1) {
        updateLeadAndAnchor(index0, index1);
        int min = Math.min(index0, index1);
        int max = Math.max(index0, index1);
        for(int i=min ; i<=max ; i++) {
            setBit(i);
        }
        fireSelectionChange();
    }

    public void invertSelection(int index0, int index1) {
        updateLeadAndAnchor(index0, index1);
        int min = Math.min(index0, index1);
        int max = Math.max(index0, index1);
        for(int i=min ; i<=max ; i++) {
            toggleBit(i);
        }
        fireSelectionChange();
    }

    public void removeSelection(int index0, int index1) {
        updateLeadAndAnchor(index0, index1);
        if(hasSelection()) {
            int min = Math.min(index0, index1);
            int max = Math.max(index0, index1);
            for(int i=min ; i<=max ; i++) {
                clearBit(i);
            }
            fireSelectionChange();
        }
    }

    public int[] getSelection() {
        int result[] = new int[value.cardinality()];
        int idx=-1;
        for(int i=0 ; (idx=value.nextSetBit(idx+1)) >= 0 ; i++) {
            result[i] = idx;
        }
        return result;
    }

    @Override
    public void rowsInserted(int index, int count) {
        if(index <= maxIndex) {
            for(int i=maxIndex ; i>=index ; i--) {
                if(value.get(i)) {
                    value.set(i+count);
                } else {
                    value.clear(i+count);
                }
            }
            value.clear(index, index+count);
            maxIndex += count;
            if(index <= minIndex) {
                minIndex += count;
            }
        }
        super.rowsInserted(index, count);
    }

    @Override
    public void rowsDeleted(int index, int count) {
        if(index <= maxIndex) {
            for(int i=index ; i<=maxIndex ; i++) {
                if(value.get(i+count)) {
                    value.set(i);
                } else {
                    value.clear(i);
                }
            }
            minIndex = value.nextSetBit(0);
            if(minIndex < 0) {
                minIndex = Integer.MAX_VALUE;
                maxIndex = Integer.MIN_VALUE;
            } else {
                while(maxIndex >= minIndex && !value.get(maxIndex)) {
                    maxIndex--;
                }
            }
        }
        super.rowsDeleted(index, count);
    }
}
