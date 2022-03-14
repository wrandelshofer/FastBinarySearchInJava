/*
 * @(#)OffsetBinarySearch.java
 * Copyright © 2022. Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.binarysearch;

import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

import java.util.Arrays;

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
public class OffsetBinarySearch {

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
        if (size == 0) {
            return ~fromIndex;
        }

        int iterations = 31 - Integer.numberOfLeadingZeros(size);

        int half = size / 2;
        int index = (key >= a[fromIndex + half]) ? half : 0;
        size = size - half;

        for (int n = iterations; n > 0; n--) {
            half = size / 2;
            int mid = index + half;
            if (key >= a[fromIndex + mid]) {
                index = mid;
            }
            size -= half;
        }

        int low = fromIndex + index;
        int value = a[low];
        return value == key ? low : (value < key ? ~low - 1 : ~low);
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
    public static void binarySearch(int[] a, int fromIndex, int toIndex,
                                    int[] keys, int keysFromIndex, int keysToIndex,
                                    int[] results) {

        int size = toIndex - fromIndex;
        if (size == 0) {
            Arrays.fill(results, ~fromIndex);
            return;
        }

        int iterations = 31 - Integer.numberOfLeadingZeros(size);


        final VectorSpecies<Integer> SPECIES = IntVector.SPECIES_PREFERRED;
        int[] indexArray = new int[SPECIES.length()];
        for (int offset = keysFromIndex; offset < keys.length; offset += SPECIES.length()) {
            size = toIndex - fromIndex;
            VectorMask<Integer> arrayMask = SPECIES.indexInRange(offset, keysToIndex);
            IntVector keyVec = IntVector.fromArray(SPECIES, keys, offset, arrayMask);

            int half = size / 2;
            IntVector value = IntVector.broadcast(SPECIES, a[fromIndex + half]);
            VectorMask<Integer> greaterEqualMask = keyVec.compare(VectorOperators.GE, value);

            IntVector indexVec = IntVector.zero(SPECIES).blend(half, greaterEqualMask);
            size -= half;

            for (int n = iterations; n > 0; n--) {
                half = size / 2;
                IntVector midVec = indexVec.add(half);
                midVec.intoArray(indexArray, 0);
                value = IntVector.fromArray(SPECIES, a, fromIndex, indexArray, 0);
                greaterEqualMask = keyVec.compare(VectorOperators.GE, value);
                indexVec = indexVec.blend(midVec, greaterEqualMask);
                size -= half;
            }

            indexVec = indexVec.add(fromIndex);
            indexVec.intoArray(indexArray, 0);
            value = IntVector.fromArray(SPECIES, a, 0, indexArray, 0);
            VectorMask<Integer> greaterMask = keyVec.compare(VectorOperators.GT, value);
            VectorMask<Integer> lesserMask = keyVec.compare(VectorOperators.LT, value);

            IntVector oneComplement = indexVec.not();
            indexVec = indexVec.blend(oneComplement, greaterMask);
            indexVec = indexVec.blend(oneComplement.sub(1), lesserMask);
            indexVec.intoArray(results, offset - keysFromIndex, arrayMask);
        }
    }
}
