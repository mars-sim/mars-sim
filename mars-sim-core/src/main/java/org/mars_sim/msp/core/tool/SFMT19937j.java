/**
 * Mars Simulation Project
 * SFMT19937j.java
 * @version 3.1.0 2017-11-06
 * @author Manny Kung
 */
package org.mars_sim.msp.core.tool;

import java.util.Arrays;

/**
 * A SIMD-oriented Fast Mersenne Twister generator.
 * <dl>
 * <dt>
 * Includes code from:
 * </dt>
 * <dd>
 * <pre>
 * <b>SIMD-oriented Fast Mersenne Twister (SFMT)</b>
 * Copyright (c) 2006,2007 Mutsuo Saito, Makoto Matsumoto and Hiroshima
 * University.
 * Copyright (c) 2012 Mutsuo Saito, Makoto Matsumoto, Hiroshima University
 * and The University of Tokyo.
 * All rights reserved.
 * http://www.math.sci.hiroshima-u.ac.jp/~m-mat/MT/SFMT/index.html
 *
 * <b>SFMT Jump</b>
 * Copyright (c) 2006,2007 Mutsuo Saito, Makoto Matsumoto and Hiroshima
 * University.
 * Copyright (c) 2012 Mutsuo Saito, Makoto Matsumoto, Hiroshima University
 * and The University of Tokyo.
 * All rights reserved.
 * http://www.math.sci.hiroshima-u.ac.jp/~m-mat/MT/SFMT/JUMP/index.html
 * </pre>
 * </dd>
 * </dl>
 */
public class SFMT19937j {

    private static final int MEXP = 19937;
    private static final int N = MEXP / 128 + 1;
    private static final int N32 = N * 4;
    private static final int POS1 = 122;
    private static final int SL1 = 18;
    private static final int SL2 = 1;
    private static final int SR1 = 11;
    private static final int SR2 = 1;
    private static final int MSK1 = 0xdfffffef;
    private static final int MSK2 = 0xddfecb7f;
    private static final int MSK3 = 0xbffaffff;
    private static final int MSK4 = 0xbffffff6;
    private static final int PARITY1 = 0x00000001;
    private static final int PARITY2 = 0x00000000;
    private static final int PARITY3 = 0x00000000;
    private static final int PARITY4 = 0x13c9e684;
    private static int[] parity = {PARITY1, PARITY2, PARITY3, PARITY4};
    private int[] psfmt32 = new int[N32];
    private int idx;

    /**
     * Creates a new generator.
     * @param seed the seed
     */
    public SFMT19937j(int seed) {
        setSeed(seed);
    }

    /**
     * Creates a new generator.
     * @param seed the seed
     * @throws NullPointerException if {@code seed == null}
     */
    public SFMT19937j(int... seed) {
        setArraySeed(seed);
    }

    private SFMT19937j(SFMT19937j r) {
        System.arraycopy(r.psfmt32, 0, psfmt32, 0, N32);
        idx = r.idx;
    }

    /**
     * Initializes this generator's state using the specified seed.
     * @param seed the seed
     */
    public void setSeed(int seed) {
        psfmt32[0] = seed;
        for (int i = 1; i < N32; i++) {
            psfmt32[i] = 1812433253 * (psfmt32[i - 1] ^ (psfmt32[i - 1] >>> 30)) + i;
        }
        idx = N32;
        period_certification();
    }

    /**
     * Initializes this generator's state using the specified seed.
     * @param seed the seed
     * @throws NullPointerException if {@code seed == null}
     */
    public void setArraySeed(int... init_key) {
        final int lag = 11;
        final int mid = 306;
        Arrays.fill(psfmt32, 0x8b8b8b8b);
        int count;
        if (init_key.length + 1 > N32) {
            count = init_key.length + 1;
        }
        else {
            count = N32;
        }
        int r = func1(psfmt32[0] ^ psfmt32[mid] ^ psfmt32[N32 - 1]);
        psfmt32[mid] += r;
        r += init_key.length;
        psfmt32[mid + lag] += r;
        psfmt32[0] = r;
        count--;
        int i, j;
        for (i = 1, j = 0; (j < count) && (j < init_key.length); j++) {
            r = func1(psfmt32[i] ^ psfmt32[(i + mid) % N32] ^ psfmt32[(i + N32 - 1) % N32]);
            psfmt32[(i + mid) % N32] += r;
            r += init_key[j] + i;
            psfmt32[(i + mid + lag) % N32] += r;
            psfmt32[i] = r;
            i = (i + 1) % N32;
        }
        for (; j < count; j++) {
            r = func1(psfmt32[i] ^ psfmt32[(i + mid) % N32] ^ psfmt32[(i + N32 - 1) % N32]);
            psfmt32[(i + mid) % N32] += r;
            r += i;
            psfmt32[(i + mid + lag) % N32] += r;
            psfmt32[i] = r;
            i = (i + 1) % N32;
        }
        for (j = 0; j < N32; j++) {
            r = func2(psfmt32[i] + psfmt32[(i + mid) % N32] + psfmt32[(i + N32 - 1) % N32]);
            psfmt32[(i + mid) % N32] ^= r;
            r -= i;
            psfmt32[(i + mid + lag) % N32] ^= r;
            psfmt32[i] = r;
            i = (i + 1) % N32;
        }
        idx = N32;
        period_certification();
    }

    private void period_certification() {
        int inner = 0;
        for (int i = 0; i < 4; i++) {
            inner ^= psfmt32[i] & parity[i];
        }
        for (int i = 16; i > 0; i >>= 1) {
            inner ^= inner >> i;
        }
        inner &= 1;
        if (inner == 1) {
            return;
        }
        for (int i = 0; i < 4; i++) {
            int work = 1;
            for (int j = 0; j < 32; j++) {
                if ((work & parity[i]) != 0) {
                    psfmt32[i] ^= work;
                    return;
                }
                work = work << 1;
            }
        }
    }

    private static int func1(int x) {
        return (x ^ (x >>> 27)) * 1664525;
    }

    private static int func2(int x) {
        return (x ^ (x >>> 27)) * 1566083941;
    }

    /**
     * Generates the next random bits.
     * @return the next random bits
     */
    public int nextBits() {
        if (idx >= N32) {
            gen_rand_all();
            idx = 0;
        }
        return psfmt32[idx++];
    }

    private void gen_rand_all() {
        int r1 = (N - 2) * 4;
        int r2 = (N - 1) * 4;
        int i;
        for (i = 0; i < N - POS1; i++) {
            do_recursion(i * 4, i * 4, (i + POS1) * 4, r1, r2);
            r1 = r2;
            r2 = i * 4;
        }
        for (; i < N; i++) {
            do_recursion(i * 4, i * 4, (i + POS1 - N) * 4, r1, r2);
            r1 = r2;
            r2 = i * 4;
        }
    }

    private void do_recursion(int r, int a, int b, int c, int d) {
        int x0 = psfmt32[a] << (SL2 * 8);
        int x1 = (psfmt32[a + 1] << (SL2 * 8)) | (psfmt32[a] >>> (32 - SL2 * 8));
        int x2 = (psfmt32[a + 2] << (SL2 * 8)) | (psfmt32[a + 1] >>> (32 - SL2 * 8));
        int x3 = (psfmt32[a + 3] << (SL2 * 8)) | (psfmt32[a + 2] >>> (32 - SL2 * 8));
        int y0 = (psfmt32[c + 1] << (32 - SR2 * 8)) | (psfmt32[c] >>> (SR2 * 8));
        int y1 = (psfmt32[c + 2] << (32 - SR2 * 8)) | (psfmt32[c + 1] >>> (SR2 * 8));
        int y2 = (psfmt32[c + 3] << (32 - SR2 * 8)) | (psfmt32[c + 2] >>> (SR2 * 8));
        int y3 = psfmt32[c + 3] >>> (SR2 * 8);
        psfmt32[r] = psfmt32[a] ^ x0 ^ ((psfmt32[b] >>> SR1) & MSK1) ^ y0 ^ (psfmt32[d] << SL1);
        psfmt32[r + 1] = psfmt32[a + 1] ^ x1 ^ ((psfmt32[b + 1] >>> SR1) & MSK2) ^ y1 ^ (psfmt32[d + 1] << SL1);
        psfmt32[r + 2] = psfmt32[a + 2] ^ x2 ^ ((psfmt32[b + 2] >>> SR1) & MSK3) ^ y2 ^ (psfmt32[d + 2] << SL1);
        psfmt32[r + 3] = psfmt32[a + 3] ^ x3 ^ ((psfmt32[b + 3] >>> SR1) & MSK4) ^ y3 ^ (psfmt32[d + 3] << SL1);
    }

    /**
     * Generates the next random bits.
     * @param bits the number of bits
     * @return the next random bits
     */
    public int nextBits(int bits) {
        return nextBits() >>> (32 - bits);
    }

    /**
     * Generates the next random bits.
     * @return the next random bits
     */
    public long nextLongBits() {
        idx += idx & 0x1;
        final int lowBits = nextBits();
        final int highBits = nextBits();
        return ((long)highBits << 32) | ((long)lowBits & 0xffffffffL);
    }

    /**
     * Generates the next random bits.
     * @param bits the number of bits
     * @return the next random bits
     */
    public long nextLongBits(int bits) {
        idx += idx & 0x1;
        final int lowBits = nextBits();
        final int highBits = nextBits();
        final long longBits = ((long)highBits << 32) | ((long)lowBits & 0xffffffffL);
        return longBits >>> (64 - bits);
    }

    /**
     * Generates the next random, uniformly distributed value.
     * <br>
     * All 2<sup>53</sup> possible values are distributed between 0 (inclusive) and 1 (exclusive).
     */
    public double nextUniform() {
        idx += idx & 0x1;
        final int lowBits = nextBits();
        final int highBits = nextBits();
        final long longBits = ((long)highBits << 32) | ((long)lowBits & 0xffffffffL);
        return (longBits >>> 11) * 0x1.0P-53;
    }

    /**
     * Advances this generator's state with a small amount of calculation.
     * @param jumpPoly the jump polynomial
     * @throws NullPointerException if {@code jumpPoly == null}
     */
    public void jump(JumpPolynomial jumpPoly) {
        int[] work = new int[N32];
        int index = idx;
        idx = N32;
        for (int i = 0; i < jumpPoly.degree(); i++) {
            int bits = jumpPoly.coefficientAt(i);
            for (int j = 0; j < 4; j++) {
                if ((bits & 1) != 0) {
                    add(work, 0, psfmt32, idx);
                }
                next_state();
                bits = bits >> 1;
            }
        }
        psfmt32 = work;
        idx = index;
    }

    private static void add(int[] dest, int dest_idx, int[] src, int src_idx) {
        int dp = dest_idx / 4;
        int sp = src_idx / 4;
        int diff = (sp - dp + N) % N;
        int i;
        for (i = 0; i < N - diff; i++) {
            int p = i + diff;
            for (int j = 0; j < 4; j++) {
                dest[i * 4 + j] ^= src[p * 4 + j];
            }
        }
        for (; i < N; i++) {
            int p = i + diff - N;
            for (int j = 0; j < 4; j++) {
                dest[i * 4 + j] ^= src[p * 4 + j];
            }
        }
    }

    private void next_state() {
        int i = (idx / 4) % N;
        int r1 = ((i + N - 2) % N) * 4;
        int r2 = ((i + N - 1) % N) * 4;
        do_recursion(i * 4, i * 4, ((i + POS1) % N) * 4, r1, r2);
        r1 = r2;
        r2 = i * 4;
        idx = idx + 4;
    }

    /**
     * Returns a new jumped generator.
     * @param jumpPoly the jump polynomial
     * @return a new generator
     * @throws NullPointerException if {@code jumpPoly == null}
     */
    public SFMT19937j copyAndJump(JumpPolynomial jumpPoly) {
        SFMT19937j r = new SFMT19937j(this);
        r.jump(jumpPoly);
        return r;
    }

}
