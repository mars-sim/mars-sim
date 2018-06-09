/**
 * Mars Simulation Project
 * TestSIMDFMT.java
 * @version 3.1.0 2017-11-06
 * @author Manny Kung
 */
package org.mars_sim.msp.core.tool;

/**

 * Test program for {@link SFMT19937} (which is a 32-bit implementation of SFMT;
 * see <a
 * href="http://www.math.sci.hiroshima-u.ac.jp/~m-mat/MT/SFMT/index.html">the
 * website</a>).
 *
 * @author Adrian King (<code>ceroxylon<b> at </b>hotmail.com</code>)
 */
public class TestSIMDFMT {

// Static variables ////////////////////////////////////////////////////////////

    final static int BLOCK_SIZE = 100000;

    final static int COUNT = 1000;

// Static methods //////////////////////////////////////////////////////////////

    /**
     * Performs the same test as <code>check32</code> in the original
     * <code>test.c</code>.
     */
    public static void main (String[] args) {
        SFMT19937 sfmt = new SFMT19937();
        int[] array32 = new int[BLOCK_SIZE / 4],
            array32_2 = new int[10000],
            ini = {0x1234, 0x5678, 0x9abc, 0xdef0};
        if (sfmt.minArraySize() > 10000)
            throw new IllegalStateException("Array size too small!");
        System.out.println("init_gen_rand__________");
        sfmt.setIntSeed(1234);
        sfmt.fillArray(array32,10000);
        sfmt.fillArray(array32_2,10000);
        sfmt.setIntSeed(1234);
        for (int i = 0; i < 10000; i++) {
            if (i < 1000) {
                System.out.print(
                    String.format("%10d ",(long) array32[i] & (-1L >>> 32)));
                if (i % 5 == 4)
                    System.out.println();
            }
            int r32 = sfmt.next();
            if (r32 != array32[i])
                throw new RuntimeException(
                    String.format(
                        "mismatch at %d array32:%x next:%x",i,array32[i],r32));
        }
        for (int i = 0; i < 700; i++) {
            int r32 = sfmt.next();
            if (r32 != array32_2[i])
                throw new RuntimeException(
                    String.format(
                        "mismatch at %d array32_2:%x next:%x",i,array32_2[i],
                        r32));
        }
        System.out.println();
        sfmt.initByArray(ini);
        System.out.println("init_by_array__________");
        sfmt.fillArray(array32,10000);
        sfmt.fillArray(array32_2,10000);
        sfmt.initByArray(ini);
        for (int i = 0; i < 10000; i++) {
            if (i < 1000) {
                System.out.print(
                    String.format("%10d ",(long) array32[i] & (-1L >>> 32)));
                if (i % 5 == 4)
                    System.out.println();
            }
            int r32 = sfmt.next();
            if (r32 != array32[i])
                throw new RuntimeException(
                    String.format(
                        "mismatch at %d array32:%x next:%x",i,array32[i],r32));
        }
        for (int i = 0; i < 700; i++) {
            int r32 = sfmt.next();
            if (r32 != array32_2[i])
                throw new RuntimeException(
                    String.format(
                        "mismatch at %d array32_2:%x next:%x",i,array32_2[i],
                        r32));
        }
    }

}
