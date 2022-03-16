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
 * Benchmark                         Mode  Cnt      Score       Error  Units
 * SearchHit                         avgt    4      13.740 ±     0.978  ns/op
 * SearchMiss                        avgt    4      13.741 ±     1.044  ns/op
 * SearchAllHitScalar                avgt    4  12,422.142 ±   447.543  ns/op
 * SearchAllMissScalar               avgt    4  12,400.855 ±   544.529  ns/op
 * SearchAllMissVectorized           avgt   20   8,369.477 ±    96.603  ns/op
 * SearchAllHitVectorized            avgt   20   8,304.012 ±    28.216  ns/op
 * SearchAllHitVectorizedPredicate   avgt    4  10,880.944 ±   324.991  ns/op
 * SearchAllMissVectorizedPredicate  avgt    4  10,824.889 ±    60.735  ns/op
 * </pre>
 * <pre>
 * # JMH version: 1.28
 * # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 *
 * Benchmark                         Mode  Cnt      Score      Error  Units
 * SearchHit                         avgt   25      14.007 ±   0.316  ns/op
 * SearchMiss                        avgt   25      13.647 ±   0.027  ns/op
 *
 * SearchAllHitScalar                avgt   25  12,480.853 ± 100.051  ns/op
 * SearchAllMissScalar               avgt   25  12,505.338 ±  45.492  ns/op
 * SearchAllHitVectorizedPredicate   avgt   25  10,241.447 ± 249.028  ns/op
 * SearchAllMissVectorizedPredicate  avgt   25   9,930.400 ± 173.538  ns/op
 * </pre>
 * <pre>
 * # JMH version: 1.34
 * # VM version: JDK 1.8.0_261, Java HotSpot(TM) 64-Bit Server VM, 25.261-b12
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 *
 * Benchmark                           Mode  Cnt        Score   Error  Units
 * SearchHit                           avgt   25       16.229 ± 0.057  ns/op
 * SearchMiss                          avgt   25       16.272 ± 0.065  ns/op
 * </pre>
 */
@Fork(value = 4, jvmArgsAppend = {"-XX:+UnlockExperimentalVMOptions", "--add-modules", "jdk.incubator.vector"
        //      ,"-XX:+UnlockDiagnosticVMOptions", "-XX:PrintAssemblyOptions=intel", "-XX:CompileCommand=print,ch/randelshofer/binarysearch/OffsetBinarySearch.*"
})
@Measurement(iterations = 5)
@Warmup(iterations = 4)
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

    /*
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
*/
    @Benchmark
    public int[] m05SearchAllMissVectorized() {
        int[] result = new int[missKeys.length];
        OffsetBinarySearch.binarySearch(a, 0, a.length, missKeys, 0, missKeys.length, result);
        return result;
    }

    @Benchmark
    public int[] m06SearchAllHitVectorized() {
        int[] result = new int[hitKeys.length];
        OffsetBinarySearch.binarySearch(a, 0, a.length, hitKeys, 0, hitKeys.length, result);
        return result;
    }
/*
    @Benchmark
    public int[] m05SearchAllMissVectorizedPredicate() {
        int[] result = new int[missKeys.length];
        OffsetBinarySearch.binarySearchWithPredicateRegisters(a, 0, a.length, missKeys, 0, missKeys.length, result);
        return result;
    }

    @Benchmark
    public int[] m06SearchAllHitVectorizedPredicate() {
        int[] result = new int[hitKeys.length];
        OffsetBinarySearch.binarySearchWithPredicateRegisters(a, 0, a.length, hitKeys, 0, hitKeys.length, result);
        return result;
    }
*/
}
