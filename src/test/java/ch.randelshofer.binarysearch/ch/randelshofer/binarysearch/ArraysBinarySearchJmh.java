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
import static ch.randelshofer.binarysearch.ArrayUtil.rndNoDuplicates;

/**
 * <pre>
 * # JMH version: 1.34
 * # VM version: JDK 18, OpenJDK 64-Bit Server VM, 18+36-2087
 *
 * Benchmark  Mode  Cnt       Score     Error  Units
 * Search     avgt   25      36.055 ±   0.376  ns/op
 * SearchAll  avgt   25  32,587.259 ± 440.333  ns/op
 * </pre>
 */
@Fork(value = 5, jvmArgsAppend = {"-XX:+UnlockExperimentalVMOptions", "--add-modules", "jdk.incubator.vector"})
@Measurement(iterations = 5)
@Warmup(iterations = 4)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class ArraysBinarySearchJmh {
    private static final int[] hitKeys = rndNoDuplicates(1000);
    private static final int[] missKeys = rndNoDuplicates(1000, hitKeys);
    private static final int[] fiftyFiftyKeys = rndFiftyFifty(missKeys, hitKeys);
    private static final int[] a = hitKeys.clone();
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
