/**
 * Mars Simulation Project
 * SFMT19937.java
 * @version 3.1.0 2017-11-06
 * @author Manny Kung
 */
package org.mars_sim.msp.core.tool;

/**
 * An adapation of <a
 * href="http://www.math.sci.hiroshima-u.ac.jp/~m-mat/MT/SFMT/index.html">
 * SFMT (SIMD oriented Fast Mersenne Twister)</a> version 1.3 by Mutsuo Saito
 * (Hiroshima University) and Makoto Matsumoto (Hiroshima University). This
 * adapation supports only the period 2<sup>19937</sup> &minus; 1; the original
 * supports some longer and shorter periods.
 *
 * <p>
 * The license (a modified BSD License) for the original C code from which this
 * code is adapted:
 *
 * <pre>
 * Copyright (c) 2006,2007 Mutsuo Saito, Makoto Matsumoto and Hiroshima
 * University. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *     * Neither the name of the Hiroshima University nor the names of
 *       its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written
 *       permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 *
 * @author Adrian King (<code>ceroxylon<b> at </b>hotmail.com</code>)
 */
public class SFMT19937 {

// Static variables ////////////////////////////////////////////////////////////

    /**
     * Mersenne Exponent. The period of the sequence is a multiple of
     * 2<sup><code>MEXP</code></sup> &minus; 1. If you adapt this code to
     * support a different exponent, you must change many of the other constants
     * here as well; consult the original C code.
     */
    public final static int MEXP = 19937;

    /**
     * The SFMT generator has an internal state array of 128-bit integers, and
     * <code>N</code> is its size.
     */
    final static int N = MEXP / 128 + 1;

    /**
     * <code>N32</code> is the size of internal state array when regarded as an
     * array of 32-bit integers.
     */
    public final static int N32 = N * 4;

    /**
     * The pick up position of the array.
     */
    final static int POS1 = 122;

    /**
     * The parameter of shift left as four 32-bit registers.
     */
    final static int SL1 = 18;

    /**
     * The parameter of shift left as one 128-bit register. The 128-bit integer
     * is shifted by <code>SL2 * 8</code> bits.
     */
    final static int SL2 = 1;

    /**
     * The parameter of shift right as four 32-bit registers.
     */
    final static int SR1 = 11;

    /**
     * The parameter of shift right as one 128-bit register. The 128-bit integer
     * is shifted by <code>SL2 * 8</code> bits.
     */
    final static int SR2 = 1;

    /**
     * A bitmask parameter, used in the recursion to break symmetry of SIMD.
     */
    final static int MSK1 = 0xdfffffef;

    /**
     * A bitmask parameter, used in the recursion to break symmetry of SIMD.
     */
    final static int MSK2 = 0xddfecb7f;

    /**
     * A bitmask parameter, used in the recursion to break symmetry of SIMD.
     */
    final static int MSK3 = 0xbffaffff;

    /**
     * A bitmask parameter, used in the recursion to break symmetry of SIMD.
     */
    final static int MSK4 = 0xbffffff6;

    /**
     * Part of a 128-bit period certification vector.
     */
    final static int PARITY1 = 0x00000001;

    /**
     * Part of a 128-bit period certification vector.
     */
    final static int PARITY2 = 0x00000000;

    /**
     * Part of a 128-bit period certification vector.
     */
    final static int PARITY3 = 0x00000000;

    /**
     * Part of a 128-bit period certification vector.
     */
    final static int PARITY4 = 0x13c9e684;

    /**
     * A parity check vector which certifies the period of 2<sup>{@link
     * #MEXP}</sup>.
     */
    final static int parity[] = {PARITY1, PARITY2, PARITY3, PARITY4};

    /**
     * A number mixed with the time of day to provide a unique seed to each
     * generator of this type allocated.
     */
    static long uniquifier = 314159265358979L;

// Instance variables //////////////////////////////////////////////////////////

    /**
     * The internal state array. Blocks of four consecutive <code>int</code>s
     * are often treated as a single 128-bit integer that is
     * little-endian&mdash;that is, its low-order bits are at lower indices in
     * the array, and high-order bits at higher indices.
     */
    final int[] sfmt = new int[N32];

    /**
     * Index counter of the next <code>int</code> to return from {@link #next}.
     */
    int idx = N32;

// Constructors ////////////////////////////////////////////////////////////////

    /**
     * Constructor that initially uses a seed based on the time of day.
     */
    public SFMT19937 () {
        this(System.nanoTime() + uniquifier++);
    }


    /**
     * Constructor that initializes the internal state array by calling {@link
     * #setSeed} with the specified argument.
     *
     * @param seed      initial seed for this generator.
     */
    public SFMT19937 (long seed) {
        setSeed(seed);
    }

// Instance methods ////////////////////////////////////////////////////////////

    /**
     * Applies the recursion formula.
     *
     * @param r         output array.
     * @param rI        index in <code>r</code>.
     * @param a         state array.
     * @param aI        index in <code>a</code>.
     * @param b         state array.
     * @param bI        index in <code>b</code>.
     * @param c         state array.
     * @param cI        index in <code>c</code>.
     * @param d         state array.
     * @param dI        index in <code>d</code>.
     */
    void doRecursion (
            int[] r, int rI, int[] a, int aI, int[] b, int bI, int[] c, int cI,
            int[] d, int dI) {
        // 128-bit shift: x = a << SL2 * 8:
        final int lShift = SL2 * 8;
        int a0 = a[aI],
            a1 = a[aI + 1],
            a2 = a[aI + 2],
            a3 = a[aI + 3];
        // for SL2 <= 3, this is more concise, but possibly not as fast (haven't
        // timed it):
        //  int x0 = a0 << lShift,
        //      x1 = (a1 << lShift) | (a0 >>> (32 - lShift)),
        //      x2 = (a2 << lShift) | (a1 >>> (32 - lShift)),
        //      x3 = (a3 << lShift) | (a2 >>> (32 - lShift));
        long hi = ((long) a3 << 32) | (a2 & (-1L >>> 32)),
            lo = ((long) a1 << 32) | (a0 & (-1L >>> 32)),
            outLo = lo << lShift,
            outHi = (hi << lShift) | (lo >>> (64 - lShift));
        int x0 = (int) outLo,
            x1 = (int) (outLo >>> 32),
            x2 = (int) outHi,
            x3 = (int) (outHi >>> 32);
        // 128-bit shift: y = c >>> SR2 * 8:
        final int rShift = SR2 * 8;
        hi = ((long) c[cI + 3] << 32) | (c[cI + 2] & (-1L >>> 32));
        lo = ((long) c[cI + 1] << 32) | (c[cI] & (-1L >>> 32));
        outHi = hi >>> rShift;
        outLo = (lo >>> rShift) | (hi << (64 - rShift));
        int y0 = (int) outLo,
            y1 = (int) (outLo >>> 32),
            y2 = (int) outHi,
            y3 = (int) (outHi >>> 32);
        // rest of forumula:
        r[rI] = a0 ^ x0 ^ ((b[bI] >>> SR1) & MSK1) ^ y0 ^ (d[dI] << SL1);
        r[rI + 1] =
            a1 ^ x1 ^ ((b[bI + 1] >>> SR1) & MSK2) ^ y1 ^ (d[dI + 1] << SL1);
        r[rI + 2] =
            a2 ^ x2 ^ ((b[bI + 2] >>> SR1) & MSK3) ^ y2 ^ (d[dI + 2] << SL1);
        r[rI + 3] =
            a3 ^ x3 ^ ((b[bI + 3] >>> SR1) & MSK4) ^ y3 ^ (d[dI + 3] << SL1);
    }


    /**
     * Fills the internal state array with pseudorandom integers.
     */
    void genRandAll () {
        int i = 0,
            r1 = 4 * (N - 2),
            r2 = 4 * (N - 1);
        for (; i < 4 * (N - POS1); i += 4) {
            doRecursion(sfmt,i,sfmt,i,sfmt,i + 4 * POS1,sfmt,r1,sfmt,r2);
            r1 = r2;
            r2 = i;
        }
        for (; i < 4 * N; i += 4) {
            doRecursion(sfmt,i,sfmt,i,sfmt,i + 4 * (POS1 - N),sfmt,r1,sfmt,r2);
            r1 = r2;
            r2 = i;
        }
    }


    /**
     * Fills a user-specified array with pseudorandom integers.
     *
     * @param array     128-bit array to be filled with pseudorandom numbers.
     * @param size      number of elements of <code>array</code> to fill.
     * @throws IllegalArgumentException
     *                  if <code>size</code> is greater than the length of
     *                  <code>array</code>, or if <code>size</code> is less than
     *                  {@link #N32}, or is not a multiple of 4.
     */
    void genRandArray (int[] array, int size) {
        if (size > array.length)
            throw new IllegalArgumentException(
                    "Given size " + size + " exceeds array length "
                        + array.length);
        if (size < N32)
            throw new IllegalArgumentException(
                    "Size must be at least " + N32 + ", but is " + size);
        if (size % 4 != 0)
            throw new IllegalArgumentException(
                    "Size must be a multiple of 4: " + size);
        int i = 0,
            j = 0,
            r1I = 4 * (N - 2),
            r2I = 4 * (N - 1);
        int[] r1 = sfmt,
            r2 = sfmt;
        for (; i < 4 * (N - POS1); i += 4) {
            doRecursion(array,i,sfmt,i,sfmt,i + 4 * POS1,r1,r1I,r2,r2I);
            r1 = r2;
            r1I = r2I;
            r2 = array;
            r2I = i;
        }
        for (; i < 4 * N; i += 4) {
            doRecursion(array,i,sfmt,i,array,i + 4 * (POS1 - N),r1,r1I,r2,r2I);
            assert r1 == r2;
            r1I = r2I;
            assert r2 == array;
            r2I = i;
        }
        for (; i < size - 4 * N; i += 4) {
            doRecursion(
                array,i,array,i - 4 * N,array,i + 4 * (POS1 - N),r1,r1I,r2,r2I);
            assert r1 == r2;
            r1I = r2I;
            assert r2 == array;
            r2I = i;
        }
        for (; j < 4 * 2 * N - size; j++)
            sfmt[j] = array[j + size - 4 * N];
        for (; i < size; i += 4, j += 4) {
            doRecursion(
                array,i,array,i - 4 * N,array,i + 4 * (POS1 - N),r1,r1I,r2,r2I);
            assert r1 == r2;
            r1I = r2I;
            assert r2 == array;
            r2I = i;
            sfmt[j] = array[i];
            sfmt[j + 1] = array[i + 1];
            sfmt[j + 2] = array[i + 2];
            sfmt[j + 3] = array[i + 3];
        }
    }


    /**
     * Used by {@link #initByArray}.
     *
     * @param x         32-bit integer.
     * @return          32-bit integer.
     */
    int func1 (int x) {
        return (x ^ (x >>> 27)) * 1664525;
    }


    /**
     * Used by {@link #initByArray}.
     *
     * @param x         32-bit integer.
     * @return          32-bit integer.
     */
    int func2 (int x) {
        return (x ^ (x >>> 27)) * 1566083941;
    }


    /**
     * Certifies the period of 2<sup>{@link #MEXP}</sup>.
     */
    void periodCertification () {
        int inner = 0;
        for (int i = 0; i < 4; i++)
            inner ^= sfmt[i] & parity[i];
        for (int i = 16; i > 0; i >>= 1)
            inner ^= inner >> i;
        if ((inner & 1) != 0) // check OK
            return;
        for (int i = 0; i < 4; i++) {
            int work = 1;
            for (int j = 0; j < 32; j++) {
                if ((work & parity[i]) != 0) {
                    sfmt[i] ^= work;
                    return;
                }
                work <<= 1;
            }
        }
    }


    /**
     * Returns the smallest array size allowed as an argument to {@link
     * #fillArray}.
     */
    public int minArraySize () {
        return N32;
    }


    /**
     * Generates and returns the next 32-bit pseudorandom number.
     *
     * @return          next number.
     */
    public int next () {
        if (idx >= N32) {
            genRandAll();
            idx = 0;
        }
        return sfmt[idx++];
    }


    /**
     * Fills the given array with pseudorandom 32-bit integers. Equivalent to
     * {@link #fillArray(int[],int)} applied to
     * <code>(array,array.length)</code>.
     *
     * @param array     array to fill.
     */
    public void fillArray (int[] array) {
        genRandArray(array,array.length);
    }


    /**
     * Fills the given array with the specified number of pseudorandom 32-bit
     * integers. The specified number of elements must be a multiple of four.
     *
     * @param array     array to fill.
     * @param elems     the number of elements of <code>array</code> (starting
     *                  at index zero) to fill; subsequent elements are not
     *                  modified.
     * @throws IllegalArgumentException
     *                  if <code>elems</code> is greater than the length of
     *                  <code>array</code>, or is less than {@link #N32}, or is
     *                  not a multiple of 4.
     */
    public void fillArray (int[] array, int elems) {
        genRandArray(array,elems);
        idx = N32;
    }


    /**
     * Initializes the internal state array with a 32-bit seed.
     *
     * @param seed      32-bit seed.
     */
    public void setIntSeed (int seed) {
        sfmt[0] = seed;
        for (int i = 1; i < N32; i++) {
            int prev = sfmt[i - 1];
            sfmt[i] = 1812433253 * (prev ^ (prev >>> 30)) + i;
        }
        periodCertification();
        idx = N32;
    }


    /**
     * Initializes the internal state array with a 64-bit seed.
     *
     * @param seed      64-bit seed.
     */
    public void setSeed (long seed) {
        initByArray((int) seed, (int) (seed >>> 32));
    }


    /**
     * Initializes the internal state array with an array of 32-bit integers.
     *
     * @param key       array of 32-bit integers, used as a seed.
     */
    public void initByArray (int... key) {
        int lag = N32 >= 623 ? 11 : N32 >= 68 ? 7 : N32 >= 39 ? 5 : 3,
            mid = (N32 - lag) / 2;
        for (int i = sfmt.length - 1; i >= 0; i--)
            sfmt[i] = 0x8b8b8b8b;
        int count = key.length >= N32 ? key.length : N32 - 1,
            r = func1(0x8b8b8b8b);
        sfmt[mid] += r;
        r += key.length;
        sfmt[mid + lag] += r;
        sfmt[0] = r;
        int i = 1,
            j = 0;
        for (; j < count && j < key.length; j++) {
            r = func1(
                sfmt[i] ^ sfmt[(i + mid) % N32] ^ sfmt[(i + N32 - 1) % N32]);
            sfmt[(i + mid) % N32] += r;
            r += key[j] + i;
            sfmt[(i + mid + lag) % N32] += r;
            sfmt[i] = r;
            i = (i + 1) % N32;
        }
        for (; j < count; j++) {
            r = func1(
                sfmt[i] ^ sfmt[(i + mid) % N32] ^ sfmt[(i + N32 - 1) % N32]);
            sfmt[(i + mid) % N32] += r;
            r += i;
            sfmt[(i + mid + lag) % N32] += r;
            sfmt[i] = r;
            i = (i + 1) % N32;
        }
        for (j = 0; j < N32; j++) {
            r = func2(
                sfmt[i] + sfmt[(i + mid) % N32] + sfmt[(i + N32 - 1) % N32]);
            sfmt[(i + mid) % N32] ^= r;
            r -= i;
            sfmt[(i + mid + lag) % N32] ^= r;
            sfmt[i] = r;
            i = (i + 1) % N32;
        }
        periodCertification();
        idx = N32;
    }

}
