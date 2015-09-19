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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * A persistent MRU list model.
 *
 * Entries are stored compressed (deflate) using serialization and
 * <code>putByteArray</code> except Strings which use <code>put</code>
 *
 * @param <T> the data type stored in this MRU model
 * 
 * @see java.util.zip.Deflater
 * @see java.util.prefs.Preferences#putByteArray(java.lang.String, byte[])
 * @see java.util.prefs.Preferences#put(java.lang.String, java.lang.String)
 * 
 * @author Matthias Mann
 */
public class PersistentMRUListModel<T extends Serializable> extends SimpleMRUListModel<T> {

    private final Class<T> clazz;
    private final Preferences prefs;
    private final String prefKey;

    public PersistentMRUListModel(int maxEntries, Class<T> clazz, Preferences prefs, String prefKey) {
        super(maxEntries);
        if(clazz == null) {
            throw new NullPointerException("clazz");
        }
        if(prefs == null) {
            throw new NullPointerException("prefs");
        }
        if(prefKey == null) {
            throw new NullPointerException("prefKey");
        }
        this.clazz = clazz;
        this.prefs = prefs;
        this.prefKey = prefKey;

        int numEntries = Math.min(prefs.getInt(keyForNumEntries(), 0), maxEntries);
        for(int i=0 ; i<numEntries ; ++i) {
            T entry = null;
            if(clazz == String.class) {
                entry = clazz.cast(prefs.get(keyForIndex(i), null));
            } else {
                byte[] data = prefs.getByteArray(keyForIndex(i), null);
                if(data != null && data.length > 0) {
                    entry = deserialize(data);
                }
            }
            if(entry != null) {
                entries.add(entry);
            }
        }
    }

    @Override
    public void addEntry(T entry) {
        if(!clazz.isInstance(entry)) {
            throw new ClassCastException();
        }
        super.addEntry(entry);
    }

    @Override
    protected void saveEntries() {
        for(int i=0 ; i<entries.size() ; ++i) {
            T obj = entries.get(i);
            if(clazz == String.class) {
                prefs.put(keyForIndex(i), (String)obj);
            } else {
                byte[] data = serialize(obj);
                assert(data != null);
                prefs.putByteArray(keyForIndex(i), data);
            }
        }
        prefs.putInt(keyForNumEntries(), entries.size());
    }

    protected byte[] serialize(T obj) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DeflaterOutputStream dos = new DeflaterOutputStream(baos, new Deflater(9));
            try {
                ObjectOutputStream oos = new ObjectOutputStream(dos);
                oos.writeObject(obj);
                oos.close();
            } finally {
                close(dos);
            }
            return baos.toByteArray();
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Unable to serialize MRU entry", ex);
            return new byte[0];
        }
    }

    protected T deserialize(byte[] data) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            InflaterInputStream iis = new InflaterInputStream(bais);
            try {
                ObjectInputStream ois = new ObjectInputStream(iis);
                Object obj = ois.readObject();
                if(clazz.isInstance(obj)) {
                    return clazz.cast(obj);
                }
                getLogger().log(Level.WARNING, "Deserialized object of type " + obj.getClass() + " expected " + clazz);
            } finally {
                close(iis);
            }
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "Unable to deserialize MRU entry", ex);
        }
        return null;
    }
    
    protected String keyForIndex(int idx) {
        return prefKey + "_" + idx;
    }
    protected String keyForNumEntries() {
        return prefKey + "_entries";
    }

    private void close(Closeable c) {
        try {
            c.close();
        } catch (IOException ex) {
            getLogger().log(Level.WARNING, "exception while closing stream", ex);
        }
    }

    Logger getLogger() {
        return Logger.getLogger(PersistentMRUListModel.class.getName());
    }
}
