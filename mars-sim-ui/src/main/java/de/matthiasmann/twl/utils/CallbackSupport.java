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
package de.matthiasmann.twl.utils;

import de.matthiasmann.twl.CallbackWithReason;
import java.lang.reflect.Array;

/**
 * Callback list management functions
 *
 * <p>The callback lists are implemented as arrays which are reallocated on
 * each change. This allows to add/remove callbacks while the old list is
 * used to invoke callbacks.</p>
 * 
 * <p>An empty list is represented by {@code null} instead of an empty array.</p>
 * 
 * @author Matthias Mann
 */
public class CallbackSupport {

    private CallbackSupport() {
    }

    private static void checkNotNull(Object callback) {
        if(callback == null) {
            throw new NullPointerException("callback");
        }
    }
    
    /**
     * Adds a new callback to the list.
     * 
     * <p>Does not check if the callback is already in the list.</p>
     * 
     * @param <T> The type of the callback list
     * @param curList the current callback list - can be null
     * @param callback the callback to be added
     * @param clazz the element class of the callback list used to allocate a new array
     * @return a new callback list with the new callback appended at the end
     * @throws NullPointerException when callback or clazz is null
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] addCallbackToList(T[] curList, T callback, Class<T> clazz) {
        checkNotNull(callback);
        final int curLength = (curList == null) ? 0 : curList.length;
        T[] newList = (T[])Array.newInstance(clazz, curLength + 1);
        if(curLength > 0) {
            System.arraycopy(curList, 0, newList, 0, curLength);
        }
        newList[curLength] = callback;
        return newList;
    }

    /**
     * Locates a callback in the list.
     * 
     * @param <T> The type of the callback list
     * @param list the current callback list - can be null
     * @param callback the callback to locate
     * @return the index if found otherwise -1
     */
    public static <T> int findCallbackPosition(T[] list, T callback) {
        checkNotNull(callback);
        if(list != null) {
            for(int i=0,n=list.length ; i<n ; i++) {
                if(list[i] == callback) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Removes a callback from the list.
     * 
     * <p>The new callback list will have the same element type as the passed in list.</p>
     * 
     * @param <T> The type of the callback list
     * @param curList the current callback list
     * @param index the index of the callback to remove
     * @return the new callback list without the specified entry or null if it was the only entry
     * @throws NullPointerException when curList is null
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] removeCallbackFromList(T[] curList, int index) {
        final int curLength = curList.length;
        assert(index >= 0 && index < curLength);
        if(curLength == 1) {
            return null;
        }
        final int newLength = curLength - 1;
        T[] newList = (T[])Array.newInstance(curList.getClass().getComponentType(), newLength);
        System.arraycopy(curList, 0, newList, 0, index);
        System.arraycopy(curList, index+1, newList, index, newLength-index);
        return newList;
    }

    /**
     * Removes a callback from the list.
     * 
     * <p>The new callback list will have the same element type as the passed in list.</p>
     * 
     * @param <T> The type of the callback list
     * @param curList the current callback list - can be null
     * @param callback the callback to remove
     * @return the passed in callback list (curList) when the callback was not found,
     *      the new callback list without the specified entry or null if it was the only entry
     */
    public static <T> T[] removeCallbackFromList(T[] curList, T callback) {
        int idx = findCallbackPosition(curList, callback);
        if(idx >= 0) {
            curList = removeCallbackFromList(curList, idx);
        }
        return curList;
    }

    /**
     * Executes all callbacks in the list.
     * 
     * @param callbacks callback list - can be null
     */
    public static void fireCallbacks(Runnable[] callbacks) {
        if(callbacks != null) {
            for(Runnable cb : callbacks) {
                cb.run();
            }
        }
    }

    /**
     * Executes all callbacks in the list.
     * 
     * @param <T> the type of the reason enum
     * @param callbacks callback list - can be null
     * @param reason the reson to pass to each callback
     */
    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> void fireCallbacks(CallbackWithReason<?>[] callbacks, T reason) {
        if(callbacks != null) {
            for(CallbackWithReason<?> cb : callbacks) {
                ((CallbackWithReason<T>)cb).callback(reason);
            }
        }
    }
}
