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
 * Benchmark            Mode  Cnt       Score      Error  Units
 * SearchHit            avgt   25      30.186 ±    0.511  ns/op
 * SearchMiss           avgt   25      28.330 ±    1.558  ns/op
 * SearchAllMissScalar  avgt   25  33,440.215 ± 1219.558  ns/op
 * SearchAllHitScalar   avgt   25  33,888.931 ± 1527.506  ns/op
 * </pre>
 * <pre>
 * # JMH version: 1.28
 * # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 *
 * Benchmark            Mode  Cnt       Score     Error  Units
 * SearchHit            avgt   25      32.194 ±   1.065  ns/op
 * SearchMiss           avgt   25      31.425 ±   1.161  ns/op
 * SearchAllHitScalar   avgt   25  34,655.962 ± 300.325  ns/op
 * SearchAllMissScalar  avgt   25  34,751.219 ± 683.322  ns/op
 * </pre>
 */
@Fork(value = 5, jvmArgsAppend = {"-XX:+UnlockExperimentalVMOptions", "--add-modules", "jdk.incubator.vector"})
@Measurement(iterations = 5)
@Warmup(iterations = 4)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class ArraysBinarySearchJmh {
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
    public int m01SearchHit() {
        index = (index + 1) % hitKeys.length;
        return Arrays.binarySearch(a, 0, a.length, hitKeys[index]);
    }

    @Benchmark
    public int m02SearchMiss() {
        index = (index + 1) % missKeys.length;
        return Arrays.binarySearch(a, 0, a.length, missKeys[index]);
    }

    @Benchmark
    public int[] m03SearchAllMissScalar() {
        int[] result = new int[missKeys.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = Arrays.binarySearch(a, 0, a.length, missKeys[i]);
        }
        return result;
    }

    @Benchmark
    public int[] m04SearchAllHitScalar() {
        int[] result = new int[hitKeys.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = Arrays.binarySearch(a, 0, a.length, hitKeys[i]);
        }
        return result;
    }
}
