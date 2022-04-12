/*
 * @(#)OffsetBinarySearchTest.java
 * Copyright © 2022. Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.binarysearch;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class BranchlessBinarySearchTest {
    @TestFactory
    public List<DynamicTest> testsWithDistinctValues() {
        return Arrays.asList(
                dynamicTest("size 0", () -> testDistinctValues(new int[]{}, 0, 0)),
                dynamicTest("size 1", () -> testDistinctValues(new int[]{1}, 0, 1)),
                dynamicTest("1", () -> testDistinctValues(new int[]{1, 2, 3, 4}, 0, 4)),
                dynamicTest("2", () -> testDistinctValues(new int[]{1, 3, 5, 7}, 0, 4)),
                dynamicTest("3", () -> testDistinctValues(new int[]{2, 4, 8, 10}, 0, 4)),
                dynamicTest("4", () -> testDistinctValues(new int[]{1, 2, 3, 4, 5}, 0, 5)),
                dynamicTest("5", () -> testDistinctValues(new int[]{1, 3, 5, 7, 9}, 0, 5)),
                dynamicTest("6", () -> testDistinctValues(new int[]{2, 4, 8, 10, 12}, 0, 5)),
                dynamicTest("7", () -> testDistinctValues(new int[]{1, 2, 3, 40, 50}, 0, 5)),
                dynamicTest("8", () -> testDistinctValues(new int[]{0, 1, 2, 3, 40, 50, 60}, 2, 5)),
                dynamicTest("9 empty!", () -> testDistinctValues(new int[]{0, 1, 2, 3, 40, 50, 60}, 2, 2)),
                dynamicTest("10", () -> testDistinctValues(rndNoDuplicates(15), 0, 15)),
                dynamicTest("11 large", () -> testDistinctValues(rndNoDuplicates(1023), 0, 1023))
        );
    }

    @TestFactory
    public List<DynamicTest> testsSearchAll() {
        return Arrays.asList(
                dynamicTest("1", () -> testAllValues(new int[]{1, 2, 3, 4}, 0, 4)),
                dynamicTest("2", () -> testAllValues(new int[]{1, 3, 5, 7}, 0, 4)),
                dynamicTest("3", () -> testAllValues(new int[]{2, 4, 8, 10}, 0, 4)),
                dynamicTest("4", () -> testAllValues(new int[]{1, 2, 3, 4, 5}, 0, 5)),
                dynamicTest("5", () -> testAllValues(new int[]{1, 3, 5, 7, 9}, 0, 5)),
                dynamicTest("6", () -> testAllValues(new int[]{2, 4, 8, 10, 12}, 0, 5)),
                dynamicTest("7", () -> testAllValues(new int[]{1, 2, 3, 40, 50}, 0, 5)),
                dynamicTest("8", () -> testAllValues(new int[]{0, 1, 2, 3, 40, 50, 60}, 2, 5)),
                dynamicTest("9 empty!", () -> testAllValues(new int[]{0, 1, 2, 3, 40, 50, 60}, 2, 2)),
                dynamicTest("10", () -> testAllValues(rndNoDuplicates(15), 0, 15)),
                dynamicTest("11 large", () -> testAllValues(rndNoDuplicates(1023), 0, 1023))
        );
    }

    @TestFactory
    public List<DynamicTest> testsWithDuplicates() {
        return Arrays.asList(
                dynamicTest("1", () -> testDuplicateValues(new int[]{1, 2, 2, 3, 3, 4, 4, 4, 4}, 0, 9)),
                dynamicTest("2", () -> testDuplicateValues(new int[]{1, 1, 2, 2, 3, 4, 4, 5, 5}, 0, 9)),
                dynamicTest("3", () -> testDuplicateValues(new int[]{8, 8, 1, 1, 2, 2, 3, 4, 4, 5, 5, -1, -1}, 2, 11))
        );
    }

    private int[] rndNoDuplicates(int n) {
        int[] a = new int[n];
        Set<Integer> set = new HashSet<>();
        Random rng = new Random(0);
        for (int i = 0; i < n; i++) {
            do {
                a[i] = rng.nextInt();
            } while (!set.add(a[i]));
        }
        Arrays.sort(a);
        return a;
    }

    private void testDistinctValues(int[] a, int fromIndex, int toIndex) {
        Set<Integer> distinct = new HashSet<>();
        for (int j : a) {
            for (int key = j - 1; key <= j + 1; key++) {
                if (!distinct.add(key)) {
                    continue;
                }
                int actual = BranchlessBinarySearch.binarySearch(a, fromIndex, toIndex, key);
                int expected = Arrays.binarySearch(a, fromIndex, toIndex, key);
                assertEquals(expected, actual, "key=" + key);
            }
        }
    }

    private void testAllValues(int[] a, int fromIndex, int toIndex) {
        int[] actual = new int[toIndex - fromIndex];
        int[] actual2 = new int[toIndex - fromIndex];
        int[] actual3 = new int[toIndex - fromIndex];
        BranchlessBinarySearch.binarySearchVectorized(a, fromIndex, toIndex, a, fromIndex, toIndex, actual);
        BranchlessBinarySearch.binarySearchVectorizedPredicate(a, fromIndex, toIndex, a, fromIndex, toIndex, actual2);
        BranchlessBinarySearch.binarySearchUnrolled(a, fromIndex, toIndex, a, fromIndex, toIndex, actual3);
        for (int i = fromIndex; i < toIndex; i++) {
            int expected = Arrays.binarySearch(a, fromIndex, toIndex, a[i]);
            assertEquals(expected, actual[i - fromIndex]);
            assertEquals(expected, actual2[i - fromIndex]);
            assertEquals(expected, actual3[i - fromIndex]);
        }
    }

    private void testDuplicateValues(int[] a, int fromIndex, int toIndex) {

        List<Integer> list = new ArrayList<>();
        for (int v : a) list.add(v);
        list = list.subList(fromIndex, toIndex);
        Set<Integer> distinct = new HashSet<>();

        for (int j : a) {
            for (int key = j - 1; key <= j + 1; key++) {
                if (!distinct.add(key)) {
                    continue;
                }

                int actual = BranchlessBinarySearch.binarySearch(a, fromIndex, toIndex, key);
                int firstIndex = list.indexOf(key);
                if (firstIndex >= 0) {
                    firstIndex += fromIndex;
                }
                int lastIndex = list.lastIndexOf(key);
                if (lastIndex >= 0) {
                    lastIndex += fromIndex;
                }
                int expected = Arrays.binarySearch(a, fromIndex, toIndex, key);
                System.out.println(Arrays.toString(a));
                System.out.println(" key=" + key + " expected=" + expected + " actual=" + actual);
                System.out.println("   firstIndex " + firstIndex + " lastIndex=" + lastIndex);
                if (expected < 0) {
                    assertEquals(expected, actual, "key=" + key);
                } else {
                    assertTrue(firstIndex <= actual && actual <= lastIndex,
                            "key=" + key + " firstIndex=" + firstIndex + " lastIndex=" + lastIndex + " actual=" + actual);
                }
            }
        }
    }
}