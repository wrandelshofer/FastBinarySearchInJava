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
 *
 * Benchmark                         Mode  Cnt       Score    Error  Units
 * Search                        avgt   25     13.782 ±  0.105  ns/op
 * SearchAllScalar               avgt   25  11491.885 ± 46.023  ns/op
 * SearchAllUnrolled             avgt   25   9833.562 ± 58.176  ns/op
 * SearchAllVectorized           avgt   25   7824.237 ± 17.661  ns/op
 * SearchAllVectorizedPredicate  avgt   25   8076.007 ± 22.956  ns/op
 *
 *
 * 4 elements:
 * Benchmark                     Mode  Cnt   Score   Error  Units
 * Search                        avgt    2     5.473          ns/op
 * SearchAllScalar               avgt    2  1921.309          ns/op
 * SearchAllUnrolled             avgt    2  6371.929          ns/op
 * SearchAllVectorized           avgt    2  2743.248          ns/op
 * SearchAllVectorizedPredicate  avgt    2  2536.118          ns/op
 *
 * 1 element:
 * Benchmark                     Mode  Cnt     Score   Error  Units
 * Search                        avgt    2     3.696          ns/op
 * SearchAllScalar               avgt    2   985.219          ns/op
 * SearchAllUnrolled             avgt    2  3142.138          ns/op
 * SearchAllVectorized           avgt    2  1143.609          ns/op
 * SearchAllVectorizedPredicate  avgt    2  1056.743          ns/op
 * </pre>
 */
@Fork(value = 1, jvmArgsAppend = {"-XX:+UnlockExperimentalVMOptions", "--add-modules", "jdk.incubator.vector"
        //      ,"-XX:+UnlockDiagnosticVMOptions", "-XX:PrintAssemblyOptions=intel", "-XX:CompileCommand=print,ch/randelshofer/binarysearch/OffsetBinarySearch.*"
})
@Measurement(iterations = 2)
@Warmup(iterations = 2)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class OffsetBinarySearchJmh {
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
        return OffsetBinarySearch.binarySearch(a, 0, a.length, fiftyFiftyKeys[index]);
    }

    @Benchmark
    public int[] m03SearchAllScalar() {
        int[] result = new int[fiftyFiftyKeys.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = OffsetBinarySearch.binarySearch(a, 0, a.length, fiftyFiftyKeys[i]);
        }
        return result;
    }
/*
    @Benchmark
    public int[] m06SearchAllUnrolled() {
        int[] result = new int[fiftyFiftyKeys.length];
        OffsetBinarySearch.binarySearchUnrolled(a, 0, a.length, fiftyFiftyKeys, 0, missKeys.length, result);
        return result;
    }

    @Benchmark
    public int[] m08SearchAllVectorized() {
        int[] result = new int[fiftyFiftyKeys.length];
        OffsetBinarySearch.binarySearchVectorized(a, 0, a.length, fiftyFiftyKeys, 0, missKeys.length, result);
        return result;
    }

    @Benchmark
    public int[] m10SearchAllVectorizedPredicate() {
        int[] result = new int[fiftyFiftyKeys.length];
        OffsetBinarySearch.binarySearchVectorizedPredicate(a, 0, a.length, fiftyFiftyKeys, 0, missKeys.length, result);
        return result;
    }

 */
}
