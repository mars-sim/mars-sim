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

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;

/**
 * An generic file system abstraction which is used as base for file system
 * widgets like FolderBrowser.
 * 
 * @author Matthias Mann
 *
 * @see de.matthiasmann.twl.FileSelector
 * @see de.matthiasmann.twl.FolderBrowser
 */
public interface FileSystemModel {

    public static final String SPECIAL_FOLDER_HOME = "user.home";
    
    public interface FileFilter {
        public boolean accept(FileSystemModel model, Object file);
    }

    /**
     * The separator character used to separate folder names in a path.
     * This should be a string with one character.
     * @return the separator character
     */
    public String getSeparator();

    /**
     * Returns the object which represents the specified file name.
     *
     * @param path the file path as returned by getPath()
     * @return the object or null if the file was not found
     */
    public Object getFile(String path);

    /**
     * Returns the parent folder of the specified file or folder
     * @param file the file or folder - needs to be a valid file or folder
     * @return the parent folder or null if the file parameter was invalid or was a root node
     */
    public Object getParent(Object file);

    /**
     * Returns true if the object is a valid folder in this file system
     * @param file the object to check
     * @return true if it is a folder
     */
    public boolean isFolder(Object file);

    /**
     * Returns true if the object is a valid file in this file system
     * @param file the object to check
     * @return true if it is a file
     */
    public boolean isFile(Object file);

    /**
     * Checks if the specified object is a hidden file or folder.
     *
     * @param file the object to check
     * @return true if it is a valid file or folder and is hidden
     */
    public boolean isHidden(Object file);

    /**
     * Returns the name of the specified object
     * @param file the object to query
     * @return the name or null if it was not a valid file or folder
     */
    public String getName(Object file);

    /**
     * Returns the path of the specified object
     *
     * @param file the object to query
     * @return the path or null if it was not a valid file or folder
     * @see #getSeparator()
     */
    public String getPath(Object file);

    /**
     * Computes a relative path from {@code from} to {@code to}
     * @param from staring point for the relative path - must be a folder
     * @param to the destination for the relative path
     * @return the relative path or null if it could not be computed
     */
    public String getRelativePath(Object from, Object to);

    /**
     * Returns the size of the file
     * @param file the object to query
     * @return the size of the file or -1 if it's not a valid file
     */
    public long getSize(Object file);

    /**
     * Returns the last modified date/time of the file or folder
     * @param file the object to query
     * @return the last modified date/time or 0
     * @see System#currentTimeMillis()
     */
    public long getLastModified(Object file);

    /**
     * Checks if the two objects specify the same file or folder
     * @param file1 the first object
     * @param file2 the second object
     * @return true if they are equal
     */
    public boolean equals(Object file1, Object file2);

    /**
     * Finds the index of a file or folder in a list of objects. This is
     * potentially faster then looping over the list and calling
     * {@link #equals(java.lang.Object, java.lang.Object) }
     *
     * @param list the list of objects
     * @param file the object to search
     * @return the index or -1 if it was not found
     */
    public int find(Object[] list, Object file);

    /**
     * Lists all file system roots
     * @return the file system roots
     */
    public Object[] listRoots();

    /**
     * Lists all files or folders in the specified folder.
     * @param file the folder to list
     * @param filter an optional filter - can be null
     * @return the (filtered) content of the folder
     */
    public Object[] listFolder(Object file, FileFilter filter);

    /**
     * Locates a special folder like {@link #SPECIAL_FOLDER_HOME}
     * @param key the special folder key
     * @return the object for this folder or null if it couldn't be located
     */
    public Object getSpecialFolder(String key);

    /**
     * Opens an InputStream for the specified file
     * @param file the file object to read
     * @return an InputStream or null if the file object is not valid
     * @throws IOException if the file can't be read
     */
    public InputStream openStream(Object file) throws IOException;

    /**
     * Opens a ReadableByteChannel for the specified file
     * @param file the file object to read
     * @return an ReadableByteChannel or null if the file object is not valid
     *      or a ReadableByteChannel can't be created
     * @throws IOException if the file can't be read
     */
    public ReadableByteChannel openChannel(Object file) throws IOException;
 
}
