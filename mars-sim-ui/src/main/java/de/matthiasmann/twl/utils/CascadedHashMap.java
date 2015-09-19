/*
 * Copyright (c) 2008-2011, Matthias Mann
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

/**
 * An add only hash map which support for a fallback hash map.
 * <p>The primary use for this class is to store hierarchic data structures.</p>
 *
 * @param <K> the key type
 * @param <V> the value type
 * @author Matthias Mann
 */
public class CascadedHashMap<K, V> {
    
    private Entry<K,V> table[];
    private int size;
    private CascadedHashMap<K, V> fallback;

    public CascadedHashMap() {
    }
    
    /**
     * Retrieves a value from this map or it's fallback map when present.
     * 
     * @param key the key to lookup
     * @return the value or null when not found
     */
    public V get(K key) {
        Entry<K,V> entry = getEntry(this, key);
        if(entry != null) {
            return entry.value;
        }
        return null;
    }
    
    /**
     * Puts an entry into this map
     * 
     * @param key the key
     * @param value the value
     * @return the old (replaced) value or null if no entry was replaced
     * @throws NullPointerException when key is null
     */
    public V put(K key, V value) {
        if(key == null) {
            throw new NullPointerException("key");
        }
        
        V oldValue = null;
        if(table != null) {
            Entry<K,V> entry = HashEntry.get(table, key);
            if(entry != null) {
                oldValue = entry.value;
                entry.value = value;
                return oldValue;
            }
            if(fallback != null) {
                oldValue = fallback.get(key);
            }
        }
        
        insertEntry(key, value);
        return oldValue;
    }
    
    /**
     * Collapses the existing fallback (by copying it into this map) and
     * sets a new fallback map.
     * 
     * @param map 
     */
    public void collapseAndSetFallback(CascadedHashMap<K,V> map) {
        if(fallback != null) {
            collapsePutAll(fallback);
            fallback = null;
        }
        fallback = map;
    }
    
    protected static<K,V> Entry<K,V> getEntry(CascadedHashMap<K,V> map, K key) {
        do {
            if(map.table != null) {
                Entry<K,V> entry = HashEntry.get(map.table, key);
                if(entry != null) {
                    return entry;
                }
            }
            map = map.fallback;
        } while(map != null);
        return null;
    }
    
    private void collapsePutAll(CascadedHashMap<K,V> map) {
        do {
            Entry<K,V> tab[] = map.table;
            if(tab != null) {
                for(int i=0,n=tab.length ; i<n ; i++) {
                    Entry<K,V> e = tab[i];
                    while(e != null) {
                        if(HashEntry.get(table, e.key) == null) {
                            insertEntry(e.key, e.value);
                        }
                        e = e.next;
                    }
                }
            }
            map = map.fallback;
        } while(map != null);
    }

    @SuppressWarnings("unchecked")
    private void insertEntry(K key, V value) {
        if(table == null) {
            table = (Entry<K, V>[])new Entry<?,?>[16];
        }
        table = HashEntry.maybeResizeTable(table, ++size);
        Entry<K,V> entry = new Entry<K, V>(key, value);
        HashEntry.insertEntry(table, entry);
    }

    protected static class Entry<K, V> extends HashEntry<K, Entry<K, V>> {
        V value;

        public Entry(K key, V value) {
            super(key);
            this.value = value;
        }
    }
}
