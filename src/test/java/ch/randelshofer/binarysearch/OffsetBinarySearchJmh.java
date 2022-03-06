/*
 * @(#)OffsetBinarySearchJmh.java
 * Copyright © 2022. Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.binarysearch;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * # JMH version: 1.28
 * # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 *
 *
 * Benchmark                                      Mode  Cnt   Score   Error  Units
 * OffsetBinarySearchJmh.mArraysBinarySearchHit   avgt   25  42.394 ± 0.166  ns/op
 * OffsetBinarySearchJmh.mArraysBinarySearchMiss  avgt   25  41.987 ± 1.637  ns/op
 * OffsetBinarySearchJmh.mOffsetBinarySearchHit   avgt   25  13.906 ± 0.013  ns/op
 * OffsetBinarySearchJmh.mOffsetBinarySearchMiss  avgt   25  13.890 ± 0.015  ns/op
 * </pre>
 */
//@Measurement(iterations = 2)
//@Warmup(iterations = 2)
//@Fork(value = 2)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class OffsetBinarySearchJmh {
    private static final int[] hitKeys = rndNoDuplicates(1023);
    private static final int[] missKeys = rndNoDuplicates(1023, hitKeys);
    private static final int[] a = hitKeys.clone();

    static {
        Arrays.sort(a);
    }

    private static int index;

    private static int[] rndNoDuplicates(int n) {
        int[] a = new int[n];
        Set<Integer> set = new HashSet<>();
        Random rng = new Random(0);
        for (int i = 0; i < n; i++) {
            do {
                a[i] = rng.nextInt();
            } while (!set.add(a[i]));
        }
        return a;
    }

    private static int[] rndNoDuplicates(int n, int[] keys) {
        int[] a = new int[n];
        Set<Integer> set = new HashSet<>(keys.length);
        for (int k : keys) set.add(k);
        Random rng = new Random(0);
        for (int i = 0; i < n; i++) {
            do {
                a[i] = rng.nextInt();
            } while (!set.contains(a[i]));
        }
        return a;
    }

    @Benchmark
    public int mOffsetBinarySearchHit() {
        index = (index + 1) % hitKeys.length;
        return OffsetBinarySearch.offsetBinarySearch(a, 0, a.length, hitKeys[index]);
    }

    //@Benchmark
    public int mArraysBinarySearchHit() {
        index = (index + 1) % hitKeys.length;
        return Arrays.binarySearch(a, 0, a.length, hitKeys[index]);
    }

    @Benchmark
    public int mOffsetBinarySearchMiss() {
        index = (index + 1) % missKeys.length;
        return OffsetBinarySearch.offsetBinarySearch(a, 0, a.length, missKeys[index]);
    }

    // @Benchmark
    public int mArraysBinarySearchMiss() {
        index = (index + 1) % missKeys.length;
        return Arrays.binarySearch(a, 0, a.length, missKeys[index]);
    }
}
