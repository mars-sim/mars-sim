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

import java.lang.reflect.Array;

/**
 * Hash table building block
 *
 * @param <K> type of the key
 * @param <T> type of subclass
 * @author Matthias Mann
 */
public class HashEntry<K, T extends HashEntry<K, T>> {

    public final K key;
    final int hash;
    T next;

    public HashEntry(K key) {
        this.key = key;
        this.hash = key.hashCode();
    }

    public T next() {
        return next;
    }
    
    public static<K, T extends HashEntry<K, T>> T get(T[] table, Object key) {
        int hash = key.hashCode();
        T e = table[hash & (table.length-1)];
        Object k;
        while(e != null && (e.hash != hash || (((k=e.key) != key) && !key.equals(k)))) {
            e = e.next;
        }
        return e;
    }

    public static<K, T extends HashEntry<K, T>> void insertEntry(T[] table, T newEntry) {
        int idx = newEntry.hash & (table.length-1);
        newEntry.next = table[idx];
        table[idx] = newEntry;
    }

    public static<K, T extends HashEntry<K, T>> T remove(T[] table, Object key) {
        int hash = key.hashCode();
        int idx = hash & (table.length - 1);
        T e = table[idx];
        T p = null;
        Object k;
        while(e != null && (e.hash != hash || (((k=e.key) != key) && !key.equals(k)))) {
            p = e;
            e = e.next;
        }
        if(e != null) {
            if(p != null) {
                p.next = e.next;
            } else {
                table[idx] = e.next;
            }
        }
        return e;
    }

    public static<K, T extends HashEntry<K, T>> void remove(T[] table, T entry) {
        int idx = entry.hash & (table.length - 1);
        T e = table[idx];
        if(e == entry) {
            table[idx] = e.next;
        } else {
            T p;
            do {
                p = e;
                e = e.next;
            } while(e != entry);
            p.next = e.next;
        }
    }

    public static<K, T extends HashEntry<K, T>> T[] maybeResizeTable(T[] table, int usedCount) {
        if(usedCount*4 > table.length*3) {
            table = resizeTable(table, table.length*2);
        }
        return table;
    }

    private static<K, T extends HashEntry<K, T>> T[] resizeTable(T[] table, int newSize) {
        if(newSize < 4 || (newSize & (newSize-1)) != 0) {
            throw new IllegalArgumentException("newSize");
        }
        @SuppressWarnings("unchecked")
        T[] newTable = (T[])Array.newInstance(table.getClass().getComponentType(), newSize);
        for(int i=0,n=table.length ; i<n ; i++) {
            for(T e=table[i] ; e!=null ;) {
                T ne = e.next;
                int ni = e.hash & (newSize-1);
                e.next = newTable[ni];
                newTable[ni] = e;
                e = ne;
            }
        }
        return newTable;
    }
}
