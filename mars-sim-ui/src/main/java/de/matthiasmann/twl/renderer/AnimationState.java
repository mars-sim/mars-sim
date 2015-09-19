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
package de.matthiasmann.twl.renderer;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Time source for animations.
 * @author Matthias Mann
 */
public interface AnimationState {

    /**
     * Returns the time since the specified state has changed in ms.
     * If the specified state was never changed then a free running time is returned.
     * 
     * @param state the state key.
     * @return time since last state change is ms.
     */
    public int getAnimationTime(StateKey state);

    /**
     * Checks if the given state is active.
     * 
     * @param state the state key.
     * @return true if the state is set
     */
    public boolean getAnimationState(StateKey state);

    /**
     * Checks if this state was changed based on user interaction or not.
     * If this method returns false then the animation time should not be used
     * for single shot animations.
     *
     * @param state the state key.
     * @return true if single shot animations should run or not.
     */
    public boolean getShouldAnimateState(StateKey state);

    /**
     * An animation state key which maps each animation state name to
     * an unique ID. This allows to implement faster lookups based on
     * the unique ID instead of performing a string lookup.
     */
    public static final class StateKey {
        private final String name;
        private final int id;

        private static final HashMap<String, StateKey> keys =
                new HashMap<String, AnimationState.StateKey>();
        private static final ArrayList<StateKey> keysByID =
                new ArrayList<StateKey>();

        private StateKey(String name, int id) {
            this.name = name;
            this.id = id;
        }

        /**
         * The name of this animation state key
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * The unique ID of this StateKey.
         * The first StateKey has ID 0.
         *
         * @return the unique ID
         */
        public int getID() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof StateKey) {
                final StateKey other = (StateKey)obj;
                return this.id == other.id;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return id;
        }

        /**
         * Returns the StateKey for the specified name.
         * The StateKey is created if it didn't exist.
         *
         * @param name the name to look up
         * @return the StateKey - never null.
         * @throws IllegalArgumentException if name is empty
         * @throws NullPointerException if name is {@code null}
         */
        public synchronized static StateKey get(String name) {
            if(name.length() == 0) {
                throw new IllegalArgumentException("name");
            }
            StateKey key = keys.get(name);
            if(key == null) {
                key = new StateKey(name, keys.size());
                keys.put(name, key);
                keysByID.add(key);
            }
            return key;
        }
        
        /**
         * Returns the StateKey for the specified id.
         * @param id the ID to lookup
         * @return the StateKey
         * @throws IndexOutOfBoundsException if the ID is invalid
         */
        public synchronized static StateKey get(int id) {
            return keysByID.get(id);
        }

        public synchronized static int getNumStateKeys() {
            return keys.size();
        }
    }
}
