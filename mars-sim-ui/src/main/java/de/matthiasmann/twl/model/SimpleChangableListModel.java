/*
 * Copyright (c) 2008-2010, Matthias Mann
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
import java.util.Arrays;
import java.util.Collection;

/**
 * A simple list model which manages it's content and provides method
 * to modify that content.
 *
 * @param <T> The type of the list entries
 * @author Matthias Mann
 */
public class SimpleChangableListModel<T> extends SimpleListModel<T> {

    private final ArrayList<T> content;

    public SimpleChangableListModel() {
        this.content = new ArrayList<T>();
    }

    public SimpleChangableListModel(Collection<T> content) {
        this.content = new ArrayList<T>(content);
    }

    public SimpleChangableListModel(T ... content) {
        this.content = new ArrayList<T>(Arrays.asList(content));
    }

    public T getEntry(int index) {
        return content.get(index);
    }

    public int getNumEntries() {
        return content.size();
    }

    public void addElement(T element) {
        insertElement(getNumEntries(), element);
    }

    public void addElements(Collection<T> elements) {
        insertElements(getNumEntries(), elements);
    }

    public void addElements(T ... elements) {
        insertElements(getNumEntries(), elements);
    }
    
    public void insertElement(int idx, T element) {
        content.add(idx, element);
        fireEntriesInserted(idx, idx);
    }

    public void insertElements(int idx, Collection<T> elements) {
        content.addAll(idx, elements);
        fireEntriesInserted(idx, idx+elements.size()-1);
    }

    public void insertElements(int idx, T ... elements) {
        insertElements(idx, Arrays.asList(elements));
    }

    public T removeElement(int idx) {
        T result = content.remove(idx);
        fireEntriesDeleted(idx, idx);
        return result;
    }

    public T setElement(int idx, T element) {
        T result = content.set(idx, element);
        fireEntriesChanged(idx, idx);
        return result;
    }

    public int findElement(Object element) {
        return content.indexOf(element);
    }
    
    public void clear() {
        content.clear();
        fireAllChanged();
    }
}
