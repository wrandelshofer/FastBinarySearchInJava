/*
 * @(#)OffsetBinarySearch.java
 * Copyright © 2022. Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.binarysearch;

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
    public static int offsetBinarySearch(int[] a, int fromIndex, int toIndex,
                                         int key) {

        int size = toIndex - fromIndex;
        if (size == 0) {
            return ~fromIndex;
        }

        int nIter = 31 - Integer.numberOfLeadingZeros(size);

        // there is at least one iteration
        int mid0 = size / 2;
        int i = (key >= a[fromIndex + mid0]) ? mid0 : 0;
        int sz = size - mid0;
        for (int n = nIter; n > 0; n--) {
            int h = sz / 2;
            int mid = i + h;
            if (key >= a[fromIndex + mid]) {
                i = mid;
            }
            sz -= h;
        }

        int low = fromIndex + i;
        int value = a[low];
        return value == key ? low : (value < key ? ~low - 1 : ~low);
    }
}
