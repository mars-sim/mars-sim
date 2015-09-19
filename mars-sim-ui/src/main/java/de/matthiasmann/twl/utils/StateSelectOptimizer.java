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

import de.matthiasmann.twl.AnimationState;
import de.matthiasmann.twl.renderer.AnimationState.StateKey;
import java.util.BitSet;

/**
 *
 * @author Matthias Mann
 */
final class StateSelectOptimizer {
    
    private final StateKey[] keys;
    private final byte[] matrix;

    final StateKey[] programKeys;
    final short[] programCodes;
    int programIdx;
    
    static StateSelectOptimizer optimize(StateExpression ... expressions) {
        final int numExpr = expressions.length;
        if(numExpr == 0 || numExpr >= 255) {
            return null;
        }
        
        BitSet bs = new BitSet();
        for(StateExpression e : expressions) {
            e.getUsedStateKeys(bs);
        }
        
        final int numKeys = bs.cardinality();
        if(numKeys == 0 || numKeys > 16) {
            return null;
        }
        
        final StateKey[] keys = new StateKey[numKeys];
        for(int keyIdx=0,keyID=-1 ; (keyID=bs.nextSetBit(keyID+1)) >= 0 ; keyIdx++) {
            keys[keyIdx] = StateKey.get(keyID);
        }
        
        final int matrixSize = 1 << numKeys;
        final byte[] matrix = new byte[matrixSize];
        AnimationState as = new AnimationState(null, keys[numKeys-1].getID()+1);

        for(int matrixIdx=0 ; matrixIdx<matrixSize ; matrixIdx++) {
            for(int keyIdx=0 ; keyIdx<numKeys ; keyIdx++) {
                as.setAnimationState(keys[keyIdx], (matrixIdx & (1 << keyIdx)) != 0);
            }
            int exprIdx = 0;
            for(; exprIdx<numExpr ; exprIdx++) {
                if(expressions[exprIdx].evaluate(as)) {
                    break;
                }
            }
            matrix[matrixIdx] = (byte)exprIdx;
        }
        
        StateSelectOptimizer sso = new StateSelectOptimizer(keys, matrix);
        sso.compute(0, 0);
        return sso;
    }
    
    private StateSelectOptimizer(StateKey[] keys, byte[] matrix) {
        this.keys = keys;
        this.matrix = matrix;
        
        programKeys = new StateKey[matrix.length-1];
        programCodes = new short[matrix.length*2-2];
    }

    private int compute(int bits, int mask) {
        if(mask == matrix.length-1) {
            return (matrix[bits]&255) | StateSelect.CODE_RESULT;
        }

        int best = -1;
        int bestScore = -1;
        int bestSet0 = 0;
        int bestSet1 = 0;
        
        int matrixIdxInc = (bits == 0) ? 1 : Integer.lowestOneBit(bits);
        
        for(int keyIdx=0 ; keyIdx<keys.length ; keyIdx++) {
            int test = 1 << keyIdx;
            
            if((mask & test) == 0) {
                int set0 = 0;
                int set1 = 0;
                
                for(int matrixIdx=bits ; matrixIdx<matrix.length ; matrixIdx+=matrixIdxInc) {
                    if((matrixIdx & mask) == bits) {
                        int resultMask = 1 << (matrix[matrixIdx]&255);
                        if((matrixIdx & test) == 0) {
                            set0 |= resultMask;
                        } else {
                            set1 |= resultMask;
                        }
                    }
                }
                
                int score = Integer.bitCount(set0 ^ set1);
                if(score > bestScore) {
                    bestScore = score;
                    bestSet0 = set0;
                    bestSet1 = set1;
                    best = keyIdx;
                }
            }
        }

        if(best < 0) {
            throw new AssertionError();
        }

        if(bestSet0 == bestSet1 && (bestSet0 & (bestSet0-1)) == 0) {
            int result = Integer.numberOfTrailingZeros(bestSet0);
            return result | StateSelect.CODE_RESULT;
        }

        int bestMask = 1 << best;
        mask |= bestMask;
        
        int idx = programIdx;
        programIdx += 2;
        programKeys[idx >> 1] = keys[best];
        programCodes[idx + 0] = (short)compute(bits | bestMask, mask);
        programCodes[idx + 1] = (short)compute(bits, mask);
        
        return idx;
    }
}
