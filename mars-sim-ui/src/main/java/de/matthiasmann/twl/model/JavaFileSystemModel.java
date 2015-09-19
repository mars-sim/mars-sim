/*
 * Copyright (c) 2008-2012, Matthias Mann
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;

/**
 * A file system implementation which uses java.io.File as base.
 * 
 * @author Matthias Mann
 */
public class JavaFileSystemModel implements FileSystemModel {

    private static final JavaFileSystemModel instance = new JavaFileSystemModel();

    public static JavaFileSystemModel getInstance() {
        return instance;
    }

    public String getSeparator() {
        return File.separator;
    }

    public Object getFile(String path) {
        File file = new File(path);
        return file.exists() ? file : null;
    }
    
    public Object getParent(Object file) {
        return ((File)file).getParentFile();
    }

    public boolean isFolder(Object file) {
        return ((File)file).isDirectory();
    }

    public boolean isFile(Object file) {
        return ((File)file).isFile();
    }

    public boolean isHidden(Object file) {
        return ((File)file).isHidden();
    }

    public String getName(Object file) {
        String name = ((File)file).getName();
        if(name.length() == 0) {
            return file.toString();
        }
        return name;
    }

    public String getPath(Object file) {
        return ((File)file).getPath();
    }

    public String getRelativePath(Object from, Object to) {
        return getRelativePath(this, from, to);
    }
    
    public static String getRelativePath(FileSystemModel fsm, Object from, Object to) {
        int levelFrom = countLevel(fsm, from);
        int levelTo = countLevel(fsm, to);
        int prefixes = 0;
        StringBuilder sb = new StringBuilder();
        while(!fsm.equals(from, to)) {
            int diff = levelTo - levelFrom;
            if(diff <= 0) {
                ++prefixes;
                --levelFrom;
                from = fsm.getParent(from);
            }
            if(diff >= 0) {
                sb.insert(0, '/');
                sb.insert(0, fsm.getName(to));
                --levelTo;
                to = fsm.getParent(to);
            }
        }
        while(prefixes-- > 0) {
            sb.insert(0, "../");
        }
        return sb.toString();
    }
    
    public static int countLevel(FileSystemModel fsm, Object file) {
        int level = 0;
        while(file != null) {
            file = fsm.getParent(file);
            level++;
        }
        return level;
    }
    
    public static int countLevel(FileSystemModel fsm, Object parent, Object child) {
        int level = 0;
        while(fsm.equals(child, parent)) {
            if(child == null) {
                return -1;
            }
            child = fsm.getParent(child);
            level++;
        }
        return level;
    }

    public long getLastModified(Object file) {
        try {
            return ((File)file).lastModified();
        } catch (Throwable ex) {
            return -1;
        }
    }

    public long getSize(Object file) {
        try {
            return ((File)file).length();
        } catch (Throwable ex) {
            return -1;
        }
    }

    public boolean equals(Object file1, Object file2) {
        return (file1 != null) && file1.equals(file2);
    }

    public int find(Object[] list, Object file) {
        if(file == null) {
            return -1;
        }
        for(int i=0 ; i<list.length ; i++) {
            if(file.equals(list[i])) {
                return i;
            }
        }
        return -1;
    }

    public Object[] listRoots() {
        return File.listRoots();
    }

    public Object[] listFolder(Object file, final FileFilter filter) {
        try {
            if(filter == null) {
                return ((File)file).listFiles();
            }
            return ((File)file).listFiles(new java.io.FileFilter() {
                public boolean accept(File pathname) {
                    return filter.accept(JavaFileSystemModel.this, pathname);
                }
            });
        } catch (Throwable ex) {
            return null;
        }
    }

    public Object getSpecialFolder(String key) {
        File file = null;
        if(SPECIAL_FOLDER_HOME.equals(key)) {
            try {
                file = new File(System.getProperty("user.home"));
            } catch(SecurityException ex) {
                // ignore
            }
        }
        if(file != null && file.canRead() && file.isDirectory()) {
            return file;
        } else {
            return null;
        }
    }

    public ReadableByteChannel openChannel(Object file) throws IOException {
        return new FileInputStream((File)file).getChannel();
    }

    public InputStream openStream(Object file) throws IOException {
        return new FileInputStream((File)file);
    }

}
