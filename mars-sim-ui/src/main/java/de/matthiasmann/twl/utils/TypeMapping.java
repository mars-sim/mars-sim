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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Maps a data type to a value. Searches for interface and subclass mappings.
 * 
 * @param <V> the type of the values in this mapping
 * @author Matthias Mann
 */
public class TypeMapping<V> {

    Entry<V>[] table;
    int size;

    @SuppressWarnings("unchecked")
    public TypeMapping() {
        table = new Entry[16];
    }

    public void put(Class<?> clazz, V value) {
        if(value == null) {
            throw new NullPointerException("value");
        }
        removeCached();
        Entry<V> entry = HashEntry.get(table, clazz);
        if(entry != null) {
            HashEntry.remove(table, entry);
            size--;
        }
        insert(new Entry<V>(clazz, value, false));
    }

    public V get(Class<?> clazz) {
        Entry<V> entry = HashEntry.get(table, clazz);
        if(entry != null) {
            return entry.value;
        }
        return slowGet(clazz);
    }
    
    public boolean remove(Class<?> clazz) {
        if(HashEntry.remove(table, clazz) != null) {
            removeCached();
            size--;
            return true;
        }
        return false;
    }

    public Set<V> getUniqueValues() {
        HashSet<V> result = new HashSet<V>();
        for(Entry<V> e : table) {
            while(e != null) {
                if(!e.isCache) {
                    result.add(e.value);
                }
                e = e.next();
            }
        }
        return result;
    }

    public Map<Class<?>, V> getEntries() {
        HashMap<Class<?>, V> result = new HashMap<Class<?>, V>();
        for(Entry<V> e : table) {
            while(e != null) {
                if(!e.isCache) {
                    result.put(e.key, e.value);
                }
                e = e.next();
            }
        }
        return result;
    }

    /**
     * Searches the class hierarchy for a regsitered type.
     * The match is cached in the hash table to speed up future lookups.
     * If no match was found then a null mapping is created.
     *
     * @param clazz the clazz which was requested.
     * @return the found mapping or null.
     */
    private V slowGet(final Class<?> clazz) {
        Entry<V> entry = null;
        Class<?> baseClass = clazz;
        outer: do {
            for(Class<?> ifClass : baseClass.getInterfaces()) {
                entry = HashEntry.get(table, ifClass);
                if(entry != null) {
                    break outer;
                }
            }

            baseClass = baseClass.getSuperclass();
            if(baseClass == null) {
                break;
            }

            entry = HashEntry.get(table, baseClass);
        } while(entry == null);

        V value = (entry != null) ? entry.value : null;
        insert(new Entry<V>(clazz, value, true));
        
        return value;
    }

    private void insert(Entry<V> newEntry) {
        table = HashEntry.maybeResizeTable(table, size);
        HashEntry.insertEntry(table, newEntry);
        size++;
    }

    private void removeCached() {
        for(Entry<V> e : table) {
            while(e != null) {
                Entry<V> n = e.next();
                if(e.isCache) {
                    HashEntry.remove(table, e);
                    size--;
                }
                e = n;
            }
        }
    }
    
    static class Entry<V> extends HashEntry<Class<?>, Entry<V>> {
        final V value;
        final boolean isCache;

        public Entry(Class<?> key, V value, boolean isCache) {
            super(key);
            this.value = value;
            this.isCache = isCache;
        }
    }
}
