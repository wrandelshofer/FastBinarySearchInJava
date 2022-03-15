/*
 * @(#)OffsetBinarySearchJmh.java
 * Copyright © 2022. Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.binarysearch;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;

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
 * Benchmark                                      Mode  Cnt   Score   Error  Units
 * ArraysBinarySearchHit                          avgt   25  32.194 ± 1.065  ns/op
 * ArraysBinarySearchMiss                         avgt   25  31.425 ± 1.161  ns/op
 *
 * OffsetBinarySearchHit                          avgt   25  14.007 ±   0.316  ns/op
 * OffsetBinarySearchMiss                         avgt   25  13.647 ±   0.027  ns/op
 *
 * ArrayBinarySearchAllHitScalar                  avgt   25  34,655.962 ± 300.325  ns/op
 * ArrayBinarySearchAllMissScalar                 avgt   25  34,751.219 ± 683.322  ns/op
 *
 * OffsetBinarySearchAllHitScalar                 avgt   25  12,480.853 ± 100.051  ns/op
 * OffsetBinarySearchAllMissScalar                avgt   25  12,505.338 ±  45.492  ns/op
 * OffsetBinarySearchAllHitVectorized             avgt   25  10,241.447 ± 249.028  ns/op
 * OffsetBinarySearchAllMissVectorized            avgt   25   9,930.400 ± 173.538  ns/op
 * </pre>
 * <pre>
 * # JMH version: 1.34
 * # VM version: JDK 1.8.0_261, Java HotSpot(TM) 64-Bit Server VM, 25.261-b12
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 *
 * Benchmark                                      Mode  Cnt   Score   Error  Units
 * OffsetBinarySearchJmh.mArraysBinarySearchHit   avgt   25  34.583 ± 1.330  ns/op
 * OffsetBinarySearchJmh.mArraysBinarySearchMiss  avgt   25  36.206 ± 0.412  ns/op
 * OffsetBinarySearchJmh.mOffsetBinarySearchHit   avgt   25  16.229 ± 0.057  ns/op
 * OffsetBinarySearchJmh.mOffsetBinarySearchMiss  avgt   25  16.272 ± 0.065  ns/op
 * </pre>
 */
@Fork(value = 5, jvmArgsAppend = {"-XX:+UnlockExperimentalVMOptions", "--add-modules", "jdk.incubator.vector"})
@Measurement(iterations = 5)
@Warmup(iterations = 4)
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
        return OffsetBinarySearch.binarySearch(a, 0, a.length, hitKeys[index]);
    }

    @Benchmark
    public int mOffsetBinarySearchMiss() {
        index = (index + 1) % missKeys.length;
        return OffsetBinarySearch.binarySearch(a, 0, a.length, missKeys[index]);
    }

    @Benchmark
    public int[] mOffsetBinarySearchAllMissVectorized() {
        int[] result = new int[missKeys.length];
        OffsetBinarySearch.binarySearch(a, 0, a.length, missKeys, 0, missKeys.length, result);
        return result;
    }

    @Benchmark
    public int[] mOffsetBinarySearchAllHitVectorized() {
        int[] result = new int[hitKeys.length];
        OffsetBinarySearch.binarySearch(a, 0, a.length, hitKeys, 0, hitKeys.length, result);
        return result;
    }

    @Benchmark
    public int[] mOffsetBinarySearchAllMissScalar() {
        int[] result = new int[missKeys.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = OffsetBinarySearch.binarySearch(a, 0, a.length, missKeys[i]);
        }
        return result;
    }

    @Benchmark
    public int[] mOffsetBinarySearchAllHitScalar() {
        int[] result = new int[hitKeys.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = OffsetBinarySearch.binarySearch(a, 0, a.length, hitKeys[i]);
        }
        return result;
    }

    @Benchmark
    public int mArraysBinarySearchHit() {
        index = (index + 1) % hitKeys.length;
        return Arrays.binarySearch(a, 0, a.length, hitKeys[index]);
    }

    @Benchmark
    public int mArraysBinarySearchMiss() {
        index = (index + 1) % missKeys.length;
        return Arrays.binarySearch(a, 0, a.length, missKeys[index]);
    }

    @Benchmark
    public int[] mArrayBinarySearchAllMissScalar() {
        int[] result = new int[missKeys.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = Arrays.binarySearch(a, 0, a.length, missKeys[i]);
        }
        return result;
    }

    @Benchmark
    public int[] mArrayBinarySearchAllHitScalar() {
        int[] result = new int[hitKeys.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = Arrays.binarySearch(a, 0, a.length, hitKeys[i]);
        }
        return result;
    }
}
