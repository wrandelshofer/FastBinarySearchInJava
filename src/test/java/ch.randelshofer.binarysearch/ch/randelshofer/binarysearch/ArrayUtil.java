/*
 * @(#)ArrayUtil.java
 * Copyright © 2022. Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.binarysearch;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class ArrayUtil {
    private ArrayUtil() {
    }

    static int[] rndNoDuplicates(int range, int length) {
        int[] a = new int[length];
        Set<Integer> set = new HashSet<>();
        Random rng = new Random(0);
        for (int i = 0; i < length; i++) {
            do {
                a[i] = rng.nextInt(range);
            } while (!set.add(a[i]));
        }
        return a;
    }

    static int[] rndHitKeys(int[] keys, int length) {
        int[] a = new int[length];
        Random rng = new Random(0);
        for (int i = 0; i < a.length; i++) {
            a[i] = keys[rng.nextInt(keys.length)];
        }
        return a;
    }

    static int[] rndMissKeys(int[] keys, int range, int length) {
        int[] a = new int[length];
        Set<Integer> set = new HashSet<>();
        for (int k : keys) set.add(k);
        Random rng = new Random(0);
        for (int i = 0; i < length; i++) {
            do {
                a[i] = rng.nextInt(range);
            } while (set.contains(a[i]));
        }
        return a;
    }

    static int[] rndFiftyFifty(int[] b, int[] c) {
        int[] a = new int[b.length + c.length];
        System.arraycopy(b, 0, a, 0, b.length);
        System.arraycopy(c, 0, a, b.length, c.length);
        Random rng = new Random(0);
        for (int i = a.length; i > 1; i--) {
            swap(a, i - 1, rng.nextInt(i));
        }
        return Arrays.copyOf(a, b.length);
    }

    static void swap(int[] a, int i, int j) {
        int swap = a[i - 1];
        a[i] = a[j];
        a[j] = swap;
    }
}
