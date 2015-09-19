/*
 * Copyright (c) 2008-2013, Matthias Mann
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
package mines;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;

/**
 *
 * @author Matthias Mann
 */
public class Highscores {
    
    private static final int MAX_ENTRIES = 100;
    
    private final HashMap<MineFieldSize, ArrayList<Entry>> lists;

    public Highscores() {
        lists = new HashMap<MineFieldSize, ArrayList<Entry>>();
    }
    
    private ArrayList<Entry> getEntryList(MineFieldSize size) {
        ArrayList<Entry> entries = lists.get(size);
        if(entries == null) {
            entries = new ArrayList<Entry>();
            lists.put(size, entries);
        }
        return entries;
    }
    
    public List<Entry> getEntries(MineFieldSize size) {
        return Collections.unmodifiableList(getEntryList(size));
    }
    
    public int addEntry(MineFieldSize size, Entry entry) {
        if(entry == null) {
            throw new NullPointerException("entry");
        }
        ArrayList<Entry> entries = getEntryList(size);
        while(entries.size() >= MAX_ENTRIES) {
            entries.remove(entries.size()-1);
        }
        int pos;
        for(pos=0 ; pos<entries.size() ; pos++) {
            if(entry.time < entries.get(pos).time) {
                break;
            }
        }
        entries.add(pos, entry);
        return pos;
    }
    
    public void read(InputStream in) throws IOException {
        lists.clear();
        
        try {
            CRC32 crc = new CRC32();
            BufferedInputStream bis = new BufferedInputStream(in);
            CheckedInputStream cis = new CheckedInputStream(bis, crc);
            DataInputStream dis = new DataInputStream(cis);
            int kCount = dis.readUnsignedShort();
            for(int kIdx=0 ; kIdx<kCount ; kIdx++) {
                MineFieldSize size = MineFieldSize.read(dis);
                if(lists.containsKey(size)) {
                    throw new IOException("duplicate key");
                }

                int prevTime = 0;
                int eCount = dis.readUnsignedShort();
                ArrayList<Entry> entries = new ArrayList<Entry>(eCount);
                for(int eIdx=0 ; eIdx<eCount ; eIdx++) {
                    int time = dis.readInt();
                    String name = dis.readUTF();
                    Date date = new Date(dis.readLong());

                    if(time < prevTime) {
                        throw new IOException("Game time gone backwards");
                    }

                    prevTime = time;
                    entries.add(new Entry(time, name, date));
                }

                lists.put(size, entries);
            }
            int crcValue = (int)crc.getValue();
            if(crcValue != dis.readInt()) {
                throw new IOException("CRC error");
            }
        } catch(IOException ex) {
            lists.clear();
            throw ex;
        }
    }
    
    public void read(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        try {
            read(fis);
        } finally {
            fis.close();
        }
    }
    
    public void write(OutputStream out) throws IOException {
        CRC32 crc = new CRC32();
        BufferedOutputStream bos = new BufferedOutputStream(out);
        CheckedOutputStream cos = new CheckedOutputStream(bos, crc);
        DataOutputStream dos = new DataOutputStream(cos);
        dos.writeShort((short)lists.size());
        for(Map.Entry<MineFieldSize, ArrayList<Entry>> e : lists.entrySet()) {
            e.getKey().write(dos);
            dos.writeShort((short)e.getValue().size());
            for(Entry entry : e.getValue()) {
                dos.writeInt(entry.time);
                dos.writeUTF(entry.name);
                dos.writeLong(entry.date.getTime());
            }
        }
        dos.writeInt((int)crc.getValue());
        dos.flush();
    }
    
    public void secureWrite(File file) throws IOException {
        File tmp = new File(file.getParentFile(), file.getName().concat(".tmp"));
        FileOutputStream fos = new FileOutputStream(tmp);
        try {
            write(fos);
        } finally {
            fos.close();
        }
        
        File bak = new File(file.getParentFile(), file.getName().concat(".bak"));
        bak.delete();
        if(file.exists() && !file.renameTo(bak)) {
            throw new IOException("Could not backup file '"+file+"' to '" + bak + "'");
        }
        if(!tmp.renameTo(file)) {
            throw new IOException("Could not rename file '"+tmp+"' to '" + file + "'");
        }
    }
    
    public static class Entry {
        public final int time;
        public final String name;
        public final Date date;

        public Entry(int time, String name, Date date) {
            this.time = time;
            this.name = name;
            this.date = date;
        }
    }
}
