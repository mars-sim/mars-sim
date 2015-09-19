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

import java.util.ArrayList;

/**
 * A composite list model which concatinates several other list models.
 * Changes on the contained list models are forwarded to this model.
 * 
 * @param <T> The type of the list entries
 * @author Matthias Mann
 */
public class CombinedListModel<T> extends SimpleListModel<T> {

    private final ArrayList<Sublist> sublists;
    private int[] sublistStarts;
    private SubListsModel subListsModel;

    public CombinedListModel() {
        this.sublists = new ArrayList<Sublist>();
        this.sublistStarts = new int[1];
    }
    
    public int getNumEntries() {
        return sublistStarts[sublistStarts.length-1];
    }

    public T getEntry(int index) {
        Sublist sl = getSublistForIndex(index);
        if(sl != null) {
            return sl.getEntry(index - sl.startIndex);
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public Object getEntryTooltip(int index) {
        Sublist sl = getSublistForIndex(index);
        if(sl != null) {
            return sl.getEntryTooltip(index - sl.startIndex);
        }
        throw new IndexOutOfBoundsException();
    }

    public int getNumSubLists() {
        return sublists.size();
    }
    
    public void addSubList(ListModel<T> model) {
        addSubList(sublists.size(), model);
    }

    public void addSubList(int index, ListModel<T> model) {
        Sublist sl = new Sublist(model);
        sublists.add(index, sl);
        adjustStartOffsets();
        int numEntries = sl.getNumEntries();
        if(numEntries > 0) {
            fireEntriesInserted(sl.startIndex, sl.startIndex+numEntries-1);
        }
        if(subListsModel != null) {
            subListsModel.fireEntriesInserted(index, index);
        }
    }
    
    public int findSubList(ListModel<T> model) {
        for(int i=0 ; i<sublists.size() ; i++) {
            Sublist sl = sublists.get(i);
            if(sl.list == model) {
                return i;
            }
        }
        return -1;
    }
    
    public void removeAllSubLists() {
        for(int i=0 ; i<sublists.size() ; i++) {
            sublists.get(i).removeChangeListener();
        }
        sublists.clear();
        adjustStartOffsets();
        fireAllChanged();
        if(subListsModel != null) {
            subListsModel.fireAllChanged();
        }
    }
    
    public boolean removeSubList(ListModel<T> model) {
        int index = findSubList(model);
        if(index >= 0) {
            removeSubList(index);
            return true;
        }
        return false;
    }
    
    public ListModel<T> removeSubList(int index) {
        Sublist sl = sublists.remove(index);
        sl.removeChangeListener();
        adjustStartOffsets();
        int numEntries = sl.getNumEntries();
        if(numEntries > 0) {
            fireEntriesDeleted(sl.startIndex, sl.startIndex+numEntries-1);
        }
        if(subListsModel != null) {
            subListsModel.fireEntriesDeleted(index, index);
        }
        return sl.list;
    }
    
    public ListModel<ListModel<T>> getModelForSubLists() {
        if(subListsModel == null) {
            subListsModel = new SubListsModel();
        }
        return subListsModel;
    }
    
    public int getStartIndexOfSublist(int sublistIndex) {
        return sublists.get(sublistIndex).startIndex;
    }
    
    private Sublist getSublistForIndex(int index) {
        int[] offsets = sublistStarts;
        int lo = 0;
        int hi = offsets.length - 1;
        while(lo <= hi) {
            int mid = (lo+hi) >>> 1;
            int delta = offsets[mid] - index;
            if(delta <= 0) {
                lo = mid + 1;
            }
            if(delta > 0) {
                hi = mid - 1;
            }
        }
        if(lo > 0 && lo <= sublists.size()) {
            Sublist sl = sublists.get(lo-1);
            assert sl.startIndex <= index;
            return sl;
        }
        return null;
    }
    
    void adjustStartOffsets() {
        int[] offsets = new int[sublists.size()+1];
        int startIdx = 0;
        for(int idx=0 ; idx < sublists.size() ;) {
            Sublist sl = sublists.get(idx);
            sl.startIndex = startIdx;
            startIdx += sl.getNumEntries();
            offsets[++idx] = startIdx;
        }
        this.sublistStarts = offsets;
    }
    
    class Sublist implements ChangeListener {
        final ListModel<T> list;
        int startIndex;

        public Sublist(ListModel<T> list) {
            this.list = list;
            this.list.addChangeListener(this);
        }
        
        public void removeChangeListener() {
            list.removeChangeListener(this);
        }

        public boolean matchPrefix(int index, String prefix) {
            return list.matchPrefix(index, prefix);
        }

        public int getNumEntries() {
            return list.getNumEntries();
        }

        public Object getEntryTooltip(int index) {
            return list.getEntryTooltip(index);
        }

        public T getEntry(int index) {
            return list.getEntry(index);
        }

        public void entriesInserted(int first, int last) {
            adjustStartOffsets();
            fireEntriesInserted(startIndex + first, startIndex + last);
        }

        public void entriesDeleted(int first, int last) {
            adjustStartOffsets();
            fireEntriesDeleted(startIndex + first, startIndex + last);
        }

        public void entriesChanged(int first, int last) {
            fireEntriesChanged(startIndex + first, startIndex + last);
        }

        public void allChanged() {
            fireAllChanged();
        }
    }
    
    class SubListsModel extends SimpleListModel<ListModel<T>> {
        public int getNumEntries() {
            return sublists.size();
        }
        public ListModel<T> getEntry(int index) {
            return sublists.get(index).list;
        }
    }
}
