# FastBinarySearchInJava
This is a straight-forward C++ to Java port of Fabio Cannizzo's FastBinarySearch.

https://github.com/fabiocannizzo/FastBinarySearch

Fabio Cannizzo. (2017). "A Fast and Vectorizable Alternative to Binary Search in O(1) with Wide Applicability to Arrays
of Floating Point Numbers". [arxiv](https://arxiv.org/abs/1506.08620)

I have currently only ported the "ClassicOffset" algorithm in class OffseBinarySearch. The algorithm is about 3 times
faster than java.util.Arrays.binarySearch().
