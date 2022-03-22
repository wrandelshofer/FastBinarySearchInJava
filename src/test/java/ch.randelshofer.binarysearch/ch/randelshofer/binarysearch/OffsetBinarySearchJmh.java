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
 * # JMH version: 1.34
 * # VM version: JDK 18, OpenJDK 64-Bit Server VM, 18+36-2087
 *
 * Benchmark                         Mode  Cnt       Score    Error  Units
 * SearchHit                         avgt   25      13.573 ±  0.030  ns/op
 * SearchMiss                        avgt   25      13.528 ±  0.023  ns/op
 * SearchAllHitScalar                avgt   25  11,814.770 ± 26.620  ns/op
 * SearchAllMissScalar               avgt   25  11,764.145 ± 38.127  ns/op
 * SearchAllHitUnrolled              avgt   25   9,959.253 ± 40.935  ns/op
 * SearchAllMissUnrolled             avgt   25   9,972.491 ± 45.669  ns/op
 * SearchAllHitVectorized            avgt   25   8,071.904 ± 13.599  ns/op
 * SearchAllMissVectorized           avgt   25   8,092.165 ± 49.610  ns/op
 * SearchAllMissVectorizedPredicate  avgt   25  10,079.808 ± 30.428  ns/op
 * SearchAllHitVectorizedPredicate   avgt   25  10,221.243 ± 24.244  ns/op
 * </pre>
 */
@Fork(value = 5, jvmArgsAppend = {"-XX:+UnlockExperimentalVMOptions", "--add-modules", "jdk.incubator.vector"
        //      ,"-XX:+UnlockDiagnosticVMOptions", "-XX:PrintAssemblyOptions=intel", "-XX:CompileCommand=print,ch/randelshofer/binarysearch/OffsetBinarySearch.*"
})
@Measurement(iterations = 5)
@Warmup(iterations = 3)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class OffsetBinarySearchJmh {
    private static final int[] hitKeys = rndNoDuplicates(1023);
    private static final int[] missKeys = rndNoDuplicates(1023, hitKeys);
    private static final int[] a = hitKeys.clone();
    private static int index;

    static {
        Arrays.sort(a);
    }

    private static int[] rndNoDuplicates(int n) {
        int[] a = new int[n];
        Set<Integer> set = new HashSet<>();
        Random rng = new Random(0);
        for (int i = 0; i < n; i++) {
            do {
                a[i] = rng.nextInt(n * 3);
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
                a[i] = rng.nextInt(n * 3);
            } while (!set.contains(a[i]));
        }
        return a;
    }

    @Benchmark
    public int m01SearchHit() {
        index = (index + 1) % hitKeys.length;
        return OffsetBinarySearch.binarySearch(a, 0, a.length, hitKeys[index]);
    }


    @Benchmark
    public int m02SearchMiss() {
        index = (index + 1) % missKeys.length;
        return OffsetBinarySearch.binarySearch(a, 0, a.length, missKeys[index]);
    }

    @Benchmark
    public int[] m03SearchAllHitScalar() {
        int[] result = new int[hitKeys.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = OffsetBinarySearch.binarySearch(a, 0, a.length, hitKeys[i]);
        }
        return result;
    }

    @Benchmark
    public int[] m04SearchAllMissScalar() {
        int[] result = new int[missKeys.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = OffsetBinarySearch.binarySearch(a, 0, a.length, missKeys[i]);
        }
        return result;
    }

    @Benchmark
    public int[] m06SearchAllMissUnrolled() {
        int[] result = new int[missKeys.length];
        OffsetBinarySearch.binarySearchUnrolled(a, 0, a.length, missKeys, 0, missKeys.length, result);
        return result;
    }

    @Benchmark
    public int[] m05SearchAllHitUnrolled() {
        int[] result = new int[hitKeys.length];
        OffsetBinarySearch.binarySearchUnrolled(a, 0, a.length, hitKeys, 0, hitKeys.length, result);
        return result;
    }
/*
    @Benchmark
    public int[] m08SearchAllMissVectorized() {
        int[] result = new int[missKeys.length];
        OffsetBinarySearch.binarySearchVectorized(a, 0, a.length, missKeys, 0, missKeys.length, result);
        return result;
    }

    @Benchmark
    public int[] m07SearchAllHitVectorized() {
        int[] result = new int[hitKeys.length];
        OffsetBinarySearch.binarySearchVectorized(a, 0, a.length, hitKeys, 0, hitKeys.length, result);
        return result;
    }

    @Benchmark
    public int[] m10SearchAllMissVectorizedPredicate() {
        int[] result = new int[missKeys.length];
        OffsetBinarySearch.binarySearchVectorizedPredicate(a, 0, a.length, missKeys, 0, missKeys.length, result);
        return result;
    }

    @Benchmark
    public int[] m09SearchAllHitVectorizedPredicate() {
        int[] result = new int[hitKeys.length];
        OffsetBinarySearch.binarySearchVectorizedPredicate(a, 0, a.length, hitKeys, 0, hitKeys.length, result);
        return result;
    }
    */
}
