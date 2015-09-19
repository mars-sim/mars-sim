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
package de.matthiasmann.twl.utils;

import java.util.Arrays;

/**
 *
 * @author Matthias Mann
 */
public class SizeSequence {
    
    private static final int INITIAL_CAPACITY = 64;

    protected int[] table;
    protected int size;
    protected int defaultValue;

    public SizeSequence() {
        this(INITIAL_CAPACITY);
    }

    public SizeSequence(int initialCapacity) {
        table = new int[initialCapacity];
    }

    public int size() {
        return size;
    }

    public int getPosition(int index) {
        int low = 0;
        int high = size;
        int result = 0;
        while(low < high) {
            int mid = (low + high) >>> 1;
            if (index <= mid) {
                high = mid;
            } else {
                result += table[mid];
                low = mid + 1;
            }
        }
        return result;
    }

    public int getEndPosition() {
        int low = 0;
        int high = size;
        int result = 0;
        while(low < high) {
            int mid = (low + high) >>> 1;
            result += table[mid];
            low = mid + 1;
        }
        return result;
    }

    public int getIndex(int position) {
        int low = 0;
        int high = size;
        while(low < high) {
            int mid = (low + high) >>> 1;
            int pos = table[mid];
            if(position < pos) {
                high = mid;
            } else {
                low = mid + 1;
                position -= pos;
            }
        }
        return low;
    }

    public int getSize(int index) {
        return getPosition(index+1) - getPosition(index);
    }

    public boolean setSize(int index, int size) {
        int delta = size - getSize(index);
        if(delta != 0) {
            adjustSize(index, delta);
            return true;
        }
        return false;
    }

    protected void adjustSize(int index, int delta) {
        int low = 0;
        int high = size;

        while(low < high) {
            int mid = (low + high) >>> 1;
            if(index <= mid) {
                table[mid] += delta;
                high = mid;
            } else {
                low = mid + 1;
            }
        }
    }

    protected int toSizes(int low, int high, int[] dst) {
        int subResult = 0;
        while(low < high) {
            int mid = (low + high) >>> 1;
            int pos = table[mid];
            dst[mid] = pos - toSizes(low, mid, dst);
            subResult += pos;
            low = mid + 1;
        }
        return subResult;
    }

    protected int fromSizes(int low, int high) {
        int subResult = 0;
        while(low < high) {
            int mid = (low + high) >>> 1;
            int pos = table[mid] + fromSizes(low, mid);
            table[mid] = pos;
            subResult += pos;
            low = mid + 1;
        }
        return subResult;
    }

    public void insert(int index, int count) {
        int newSize = size + count;
        if(newSize >= table.length) {
            int[] sizes = new int[newSize];
            toSizes(0, size, sizes);
            table = sizes;
        } else {
            toSizes(0, size, table);
        }
        System.arraycopy(table, index, table, index+count, size-index);
        size = newSize;
        initializeSizes(index, count);
        fromSizes(0, newSize);
    }

    public void remove(int index, int count) {
        toSizes(0, size, table);
        int newSize = size - count;
        System.arraycopy(table, index+count, table, index, newSize-index);
        size = newSize;
        fromSizes(0, newSize);
    }

    public void initializeAll(int count) {
        if(table.length < count) {
            table = new int[count];
        }
        size = count;
        initializeSizes(0, count);
        fromSizes(0, count);
    }

    public void setDefaultValue(int defaultValue) {
        this.defaultValue = defaultValue;
    }

    protected void initializeSizes(int index, int count) {
        Arrays.fill(table, index, index+count, defaultValue);
    }
}
