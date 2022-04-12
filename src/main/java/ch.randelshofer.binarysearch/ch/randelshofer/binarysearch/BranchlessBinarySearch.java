/*
 * @(#)OffsetBinarySearch.java
 * Copyright © 2022. Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.binarysearch;

import jdk.incubator.vector.IntVector;

import java.util.Arrays;

import static java.lang.Integer.numberOfLeadingZeros;
import static jdk.incubator.vector.VectorOperators.GE;
import static jdk.incubator.vector.VectorOperators.GT;
import static jdk.incubator.vector.VectorOperators.IS_DEFAULT;
import static jdk.incubator.vector.VectorOperators.IS_NEGATIVE;
import static jdk.incubator.vector.VectorOperators.LT;

/**
 * Implements offset binary search.
 * <p>
 * References:
 * <dl>
 *     <dt>
 *         Fabio Cannizzo. (2017). "A Fast and Vectorizable Alternative to Binary
 *         Search in O(1) with Wide Applicability to Arrays of Floating Point
 *         Numbers."
 *     </dt>
 *     <dd><a href="https://arxiv.org/abs/1506.08620">arxiv</a>
 *     <a href="https://github.com/fabiocannizzo/FastBinarySearch">github</a>,
 *     MIT License</dd>
 * </dl>
 */
public class BranchlessBinarySearch {

    /**
     * Searches for the provided key in the given array.
     * <p>
     * The array must be sorted in ascending order.
     * <p>
     * If the array has no duplicates then the result is identical to
     * {@link Arrays#binarySearch}. If the array has duplicates, then
     * the algorithm will pick different duplicate values than
     * {@link Arrays#binarySearch}.
     *
     * @param a         the array
     * @param fromIndex from inclusive
     * @param toIndex   to exclusive
     * @param key       the key
     * @return index of key in {@code a}, if the key is present in {@code a}.
     * {@code ~(insertionPoint)} if they key is absent in {@code a}.
     */
    public static int binarySearch(int[] a, int fromIndex, int toIndex,
                                   int key) {
        int size = toIndex - fromIndex;
        if (size <= 0) {
            return ~fromIndex;
        }

        int index = fromIndex;
        int iterations = 32 - numberOfLeadingZeros(size);
        for (int n = iterations; n > 0; n--) {
            int half = size >>> 1;
            int mid = index + half;
            if (key >= a[mid]) {
                index = mid;
            }
            size -= half;
        }

        int sign = a[index] - key;
        return sign == 0 ? index : ~index + (sign >> -1);
    }

    /**
     * Searches for the provided keys in the given array.
     * <p>
     * The array must be sorted in ascending order.
     * <p>
     * If the array has no duplicates then the result is identical to
     * {@link Arrays#binarySearch}. If the array has duplicates, then
     * the algorithm will pick different duplicate values than
     * {@link Arrays#binarySearch}.
     *
     * @param a         the array
     * @param fromIndex from inclusive
     * @param toIndex   to exclusive
     * @param keys      the keys
     * @param results   indices of keys in {@code a}, if the keys are present in {@code a}.
     *                  {@code ~(insertionPoint)} if they keys are absent in {@code a}.
     */
    public static void binarySearchUnrolled(int[] a, int fromIndex, int toIndex,
                                            int[] keys, int keysFromIndex, int keysToIndex,
                                            int[] results) {

        int size = toIndex - fromIndex;
        if (size <= 0) {
            Arrays.fill(results, ~fromIndex);
            return;
        }

        int offset = keysFromIndex;
        int half;
        int upperBound = fromIndex + ((toIndex - fromIndex) & -4);
        int iterations = 32 - numberOfLeadingZeros(size);
        for (; offset < upperBound; offset += 4) {
            int index0 = fromIndex;
            int index1 = fromIndex;
            int index2 = fromIndex;
            int index3 = fromIndex;
            int key0 = keys[offset];
            int key1 = keys[offset + 1];
            int key2 = keys[offset + 2];
            int key3 = keys[offset + 3];
            size = toIndex - fromIndex;
            for (int n = iterations; n > 0; n--) {
                half = size >>> 1;
                int mid0 = index0 + half;
                if (key0 >= a[mid0]) {
                    index0 = mid0;
                }
                int mid1 = index1 + half;
                if (key1 >= a[mid1]) {
                    index1 = mid1;
                }
                int mid2 = index2 + half;
                if (key2 >= a[mid2]) {
                    index2 = mid2;
                }
                int mid3 = index3 + half;
                if (key3 >= a[mid3]) {
                    index3 = mid3;
                }
                size -= half;
            }
            int sign0 = a[index0] - key0;
            int sign1 = a[index1] - key1;
            int sign2 = a[index2] - key2;
            int sign3 = a[index3] - key3;
            results[offset - fromIndex] = sign0 == 0 ? index0 : ~index0 + (sign0 >> -1);
            results[offset + 1 - fromIndex] = sign1 == 0 ? index1 : ~index1 + (sign1 >> -1);
            results[offset + 2 - fromIndex] = sign2 == 0 ? index2 : ~index2 + (sign2 >> -1);
            results[offset + 3 - fromIndex] = sign3 == 0 ? index3 : ~index3 + (sign3 >> -1);
        }


        for (; offset < keysToIndex; offset++) {
            results[offset - keysFromIndex] = binarySearch(a, fromIndex, toIndex, keys[offset]);
        }
    }

    /**
     * Searches for the provided keys in the given array.
     * <p>
     * The array must be sorted in ascending order.
     * <p>
     * If the array has no duplicates then the result is identical to
     * {@link Arrays#binarySearch}. If the array has duplicates, then
     * the algorithm will pick different duplicate values than
     * {@link Arrays#binarySearch}.
     *
     * @param a         the array
     * @param fromIndex from inclusive
     * @param toIndex   to exclusive
     * @param keys      the keys
     * @param results   indices of keys in {@code a}, if the keys are present in {@code a}.
     *                  {@code ~(insertionPoint)} if they keys are absent in {@code a}.
     */
    public static void binarySearchVectorized(int[] a, int fromIndex, int toIndex,
                                              int[] keys, int keysFromIndex, int keysToIndex,
                                              int[] results) {

        int size = toIndex - fromIndex;
        if (size <= 0) {
            Arrays.fill(results, ~fromIndex);
            return;
        }

        int iterations = 32 - numberOfLeadingZeros(size);

        final var SPECIES = IntVector.SPECIES_PREFERRED;
        int upperBound = SPECIES.loopBound(keysToIndex);
        int offset = keysFromIndex;
        for (; offset < upperBound; offset += SPECIES.length()) {
            int resultOffset = offset - keysFromIndex;
            size = toIndex - fromIndex;

            var key = IntVector.fromArray(SPECIES, keys, offset);
            var index = IntVector.broadcast(SPECIES, fromIndex);
            for (int n = iterations; n > 0; n--) {
                int half = size >>> 1;
                var mid = index.add(half);
                mid.intoArray(results, resultOffset);
                var value = IntVector.fromArray(SPECIES, a, 0, results, resultOffset);
                index = index.blend(mid, key.compare(GE, value));
                size -= half;
            }

            index.intoArray(results, resultOffset);
            var sign = IntVector.fromArray(SPECIES, a, 0, results, resultOffset)
                    .sub(key);
            var oneComplement = index.not();
            index.blend(oneComplement, sign.test(IS_DEFAULT).not())
                    .blend(oneComplement.sub(1), sign.test(IS_NEGATIVE))
                    .intoArray(results, resultOffset);
        }

        for (; offset < keysToIndex; offset++) {
            results[offset - keysFromIndex] = binarySearch(a, fromIndex, toIndex, keys[offset]);
        }
    }

    /**
     * See {@link #binarySearchVectorized(int[], int, int, int[], int, int, int[])}.
     * <p>
     * The implementation in this method is optimised for platforms that support
     * a predicate register.
     */
    public static void binarySearchVectorizedPredicate(int[] a, int fromIndex, int toIndex,
                                                       int[] keys, int keysFromIndex, int keysToIndex,
                                                       int[] results) {

        int size = toIndex - fromIndex;
        if (size <= 0) {
            Arrays.fill(results, ~fromIndex);
            return;
        }

        int iterations = 32 - numberOfLeadingZeros(size);

        final var SPECIES = IntVector.SPECIES_PREFERRED;
        int[] indexArray = new int[SPECIES.length()];
        for (int offset = keysFromIndex; offset < keysToIndex; offset += SPECIES.length()) {
            size = toIndex - fromIndex;

            var mask = SPECIES.indexInRange(offset, keysToIndex);
            var key = IntVector.fromArray(SPECIES, keys, offset, mask);

            var index = IntVector.broadcast(SPECIES, fromIndex);
            for (int n = iterations; n > 0; n--) {
                int half = size >>> 1;
                var mid = index.add(half);
                mid.intoArray(indexArray, 0);
                var value = IntVector.fromArray(SPECIES, a, 0, indexArray, 0);
                index = index.blend(mid, key.compare(GE, value));
                size -= half;
            }

            index.intoArray(indexArray, 0);
            var value = IntVector.fromArray(SPECIES, a, 0, indexArray, 0);
            var oneComplement = index.not();
            index.blend(oneComplement, key.compare(GT, value))
                    .blend(oneComplement.sub(1), key.compare(LT, value))
                    .intoArray(results, offset - keysFromIndex, mask);
        }
    }
}
