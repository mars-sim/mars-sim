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

import java.util.Comparator;
import java.util.Random;

/**
 * A reordering list model - forwards changes of the base model.
 * 
 * @param <T> The type of the list entries
 * @author Matthias Mann
 */
public class ReorderListModel<T> extends AbstractListModel<T> {

    private final ListModel<T> base;
    private final ListModel.ChangeListener listener;
    private int[] reorderList;
    private int size;

    public ReorderListModel(ListModel<T> base) {
        this.base = base;
        this.reorderList = new int[0];

        this.listener = new ListModel.ChangeListener() {
            public void entriesInserted(int first, int last) {
                ReorderListModel.this.entriesInserted(first, last);
            }

            public void entriesDeleted(int first, int last) {
                ReorderListModel.this.entriesDeleted(first, last);
            }

            public void entriesChanged(int first, int last) {
            }

            public void allChanged() {
                ReorderListModel.this.buildNewList();
            }
        };
        
        base.addChangeListener(listener);
        buildNewList();
    }
    
    public void destroy() {
        base.removeChangeListener(listener);
    }

    public int getNumEntries() {
        return size;
    }

    public T getEntry(int index) {
        int remappedIndex = reorderList[index];
        return base.getEntry(remappedIndex);
    }

    public Object getEntryTooltip(int index) {
        int remappedIndex = reorderList[index];
        return base.getEntryTooltip(remappedIndex);
    }

    public boolean matchPrefix(int index, String prefix) {
        int remappedIndex = reorderList[index];
        return base.matchPrefix(remappedIndex, prefix);
    }

    public int findEntry(Object o) {
        int[] list = this.reorderList;
        for(int i=0,n=size ; i<n ; i++) {
            T entry = base.getEntry(list[i]);
            if(entry == o || (entry != null && entry.equals(o))) {
                return i;
            }
        }
        return -1;
    }

    public void shuffle() {
        Random r = new Random();
        for(int i=size ; i>1 ;) {
            int j = r.nextInt(i--);
            int temp = reorderList[i];
            reorderList[i] = reorderList[j];
            reorderList[j] = temp;
        }
        fireAllChanged();
    }

    public void sort(Comparator<T> c) {
        // need to use own version of sort because we need to sort a int[] with a sort callback
        int[] aux = new int[size];
        System.arraycopy(reorderList, 0, aux, 0, size);
        mergeSort(aux, reorderList, 0, size, c);
        fireAllChanged();
    }
    
    /**
     * Tuning parameter: list size at or below which insertion sort will be
     * used in preference to mergesort.
     */
    private static final int INSERTIONSORT_THRESHOLD = 7;

    /**
     * Src is the source array that starts at index 0
     * Dest is the (possibly larger) array destination with a possible offset
     * low is the index in dest to start sorting
     * high is the end index in dest to end sorting
     * off is the offset into src corresponding to low in dest
     */
    private void mergeSort(int[] src, int[] dest,
            int low, int high, Comparator<T> c) {
        int length = high - low;

        // Insertion sort on smallest arrays
        if(length < INSERTIONSORT_THRESHOLD) {
            for(int i = low; i < high; i++) {
                for(int j = i; j > low && compare(dest, j - 1, j, c) > 0; j--) {
                    swap(dest, j, j - 1);
                }
            }
            return;
        }

        // Recursively sort halves of dest into src
        int mid = (low + high) >>> 1;
        mergeSort(dest, src, low, mid, c);
        mergeSort(dest, src, mid, high, c);

        // If list is already sorted, just copy from src to dest.  This is an
        // optimization that results in faster sorts for nearly ordered lists.
        if(compare(src, mid-1, mid, c) <= 0) {
            System.arraycopy(src, low, dest, low, length);
            return;
        }

        // Merge sorted halves (now in src) into dest
        for(int i = low,  p = low,  q = mid; i < high; i++) {
            if(q >= high || p < mid && compare(src, p, q, c) <= 0) {
                dest[i] = src[p++];
            } else {
                dest[i] = src[q++];
            }
        }
    }

    private int compare(int[] list, int a, int b, Comparator<T> c) {
        int aIdx = list[a];
        int bIdx = list[b];
        T objA = base.getEntry(aIdx);
        T objB = base.getEntry(bIdx);
        return c.compare(objA, objB);
    }

    /**
     * Swaps x[a] with x[b].
     */
    private static void swap(int x[], int a, int b) {
        int t = x[a];
        x[a] = x[b];
        x[b] = t;
    }

    private void buildNewList() {
        size = base.getNumEntries();
        reorderList = new int[size + 1024];
        for(int i = 0; i < size; i++) {
            reorderList[i] = i;
        }
        fireAllChanged();
    }

    private void entriesInserted(int first, int last) {
        final int delta = last - first + 1;
        for(int i = 0; i < size; i++) {
            if(reorderList[i] >= first) {
                reorderList[i] += delta;
            }
        }
        if(size + delta > reorderList.length) {
            int[] newList = new int[Math.max(size*2, size+delta+1024)];
            System.arraycopy(reorderList, 0, newList, 0, size);
            reorderList = newList;
        }
        int oldSize = size;
        for(int i = 0; i < delta; i++) {
            reorderList[size++] = first + i;
        }
        fireEntriesInserted(oldSize, size-1);
    }

    private void entriesDeleted(int first, int last) {
        final int delta = last - first + 1;
        for(int i = 0; i < size; i++) {
            final int entry = reorderList[i];
            if(entry >= first) {
                if(entry <= last) {
                    // we have to remove entries - enter copy loop
                    entriesDeletedCopy(first, last, i);
                    return;
                }
                reorderList[i] = entry - delta;
            }
        }
    }

    private void entriesDeletedCopy(int first, int last, int i) {
        int j, delta = last - first + 1;
        int oldSize = size;
        for(j=i ; i<oldSize ; i++) {
            int entry = reorderList[i];
            if(entry >= first) {
                if(entry <= last) {
                    size--;
                    fireEntriesDeleted(j, j);
                    continue;
                }
                entry -= delta;
            }
            reorderList[j++] = entry;
        }
        assert size == j;
    }
}
