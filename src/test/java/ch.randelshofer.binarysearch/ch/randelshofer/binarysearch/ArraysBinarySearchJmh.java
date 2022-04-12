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
import java.util.concurrent.TimeUnit;

import static ch.randelshofer.binarysearch.ArrayUtil.rndFiftyFifty;
import static ch.randelshofer.binarysearch.ArrayUtil.rndHitKeys;
import static ch.randelshofer.binarysearch.ArrayUtil.rndMissKeys;
import static ch.randelshofer.binarysearch.ArrayUtil.rndNoDuplicates;

/**
 * <pre>
 * # JMH version: 1.34
 * # VM version: JDK 18, OpenJDK 64-Bit Server VM, 18+36-2087
 *
 * 1000 elements:
 * Benchmark                           Mode  Cnt       Score     Error  Units
 * Search                              avgt   25      36.055 ±   0.376  ns/op
 * SearchAll                           avgt   25  32,587.259 ± 440.333  ns/op
 *
 * 4 elements:
 * Benchmark                           Mode  Cnt     Score   Error  Units
 * ArraysBinarySearchJmh.m01Search     avgt    2     5.013          ns/op
 * ArraysBinarySearchJmh.m03SearchAll  avgt    2  3807.982          ns/op
 *
 * 1 element:
 * Benchmark                           Mode  Cnt     Score   Error  Units
 * ArraysBinarySearchJmh.m01Search     avgt    2     3.666          ns/op
 * ArraysBinarySearchJmh.m03SearchAll  avgt    2  1251.846          ns/op
 * </pre>
 */
@Fork(value = 1, jvmArgsAppend = {"-XX:+UnlockExperimentalVMOptions", "--add-modules", "jdk.incubator.vector"})
@Measurement(iterations = 2)
@Warmup(iterations = 2)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class ArraysBinarySearchJmh {
    private static final int[] a = rndNoDuplicates(1000, 1);
    private static final int[] hitKeys = rndHitKeys(a, 1000);
    private static final int[] missKeys = rndMissKeys(a, 1000, 1000);
    private static final int[] fiftyFiftyKeys = rndFiftyFifty(hitKeys, missKeys);
    private static int index;

    static {
        Arrays.sort(a);
    }

    @Benchmark
    public int m01Search() {
        index = (index + 1) % fiftyFiftyKeys.length;
        return Arrays.binarySearch(a, 0, a.length, fiftyFiftyKeys[index]);
    }

    @Benchmark
    public int[] m03SearchAll() {
        int[] result = new int[fiftyFiftyKeys.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = Arrays.binarySearch(a, 0, a.length, fiftyFiftyKeys[i]);
        }
        return result;
    }
}
