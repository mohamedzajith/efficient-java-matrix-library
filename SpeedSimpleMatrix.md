# Introduction #
To provide a better understanding of the performance difference between SimpleMatrix and using the operations interface a study was performed.  The study was composed of running several benchmarks across different operations and matrix sizes using [Java Matrix Benchmark](http://code.google.com/p/java-matrix-benchm).  Only operations were tested that had a direct equivalent in the two interfaces.

In general the more computationally intensive an operation is smaller the difference in performance.  EJML is significantly slower for element-wise operations such as add and scale.  There is an insignificant performance difference for more expensive operations such as multiplication and SVD on larger matrices and only a small performance hit on small matrices.  Note that this is true even though the SimpleMatrix implementation of SVD does additional work to order singular values.

As one might expect, the bottleneck in most applications will be more expensive operations.  Thus it is likely that there is only an insignificant performance difference between the two approaches in many applications.

# Relative Runtime Plots #

Results are presented using relative runtime plots.  These plots show how fast each interface is relative to the other.  The fastest interface at each matrix size always has a value of one since it can perform the most operations per second.  For more information see the Java Matrix Benchmark [manual here](http://code.google.com/p/java-matrix-benchmark/wiki/RuntimePerformanceBenchmark).

Looking at the addition plot, SimpleMatrix runs at about 0.25 times the speed as using DenseMatrix64F for smaller matrices.  When it processes larger matrices it runs at about 0.6 times the speed of the operations interface. This means that for larger matrices it runs relative faster.  For more expensive operations (SVD, solve, matrix multiplication, etc ) it is clear that the difference in performance is not significant for matrices that are 100 by 100 or larger.

EJML is EJML using the operations interface and SEJML is EJML using SimpleMatrix.

# Test Environment #

Test Environment:
| Date | 2010.07.24 |
|:-----|:-----------|
| OS   | Vista 64bit |
| CPU  | Q9400 - 2.66 Ghz - 4 cores |
| JVM  | Java HotSpot(TM) 64-Bit Server VM 1.6.0\_16 |
| Benchmark | 0.7pre     |
| EJML |  0.14pre        |

# Plots #

## Java: Basic Operation Results ##

| ![http://efficient-java-matrix-library.googlecode.com/svn/wiki/SpeedSimpleMatrix.attach/add.png](http://efficient-java-matrix-library.googlecode.com/svn/wiki/SpeedSimpleMatrix.attach/add.png) | ![http://efficient-java-matrix-library.googlecode.com/svn/wiki/SpeedSimpleMatrix.attach/scale.png](http://efficient-java-matrix-library.googlecode.com/svn/wiki/SpeedSimpleMatrix.attach/scale.png) |
|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ![http://efficient-java-matrix-library.googlecode.com/svn/wiki/SpeedSimpleMatrix.attach/mult.png](http://efficient-java-matrix-library.googlecode.com/svn/wiki/SpeedSimpleMatrix.attach/mult.png) | ![http://efficient-java-matrix-library.googlecode.com/svn/wiki/SpeedSimpleMatrix.attach/inv.png](http://efficient-java-matrix-library.googlecode.com/svn/wiki/SpeedSimpleMatrix.attach/inv.png)     |
| ![http://efficient-java-matrix-library.googlecode.com/svn/wiki/SpeedSimpleMatrix.attach/det.png](http://efficient-java-matrix-library.googlecode.com/svn/wiki/SpeedSimpleMatrix.attach/det.png) | ![http://efficient-java-matrix-library.googlecode.com/svn/wiki/SpeedSimpleMatrix.attach/tran.png](http://efficient-java-matrix-library.googlecode.com/svn/wiki/SpeedSimpleMatrix.attach/tran.png)   |


## Java: Solving Linear Systems ##

| ![http://efficient-java-matrix-library.googlecode.com/svn/wiki/SpeedSimpleMatrix.attach/solveEq.png](http://efficient-java-matrix-library.googlecode.com/svn/wiki/SpeedSimpleMatrix.attach/solveEq.png) |![http://efficient-java-matrix-library.googlecode.com/svn/wiki/SpeedSimpleMatrix.attach/solveOver.png](http://efficient-java-matrix-library.googlecode.com/svn/wiki/SpeedSimpleMatrix.attach/solveOver.png)|
|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

## Java: Matrix Decompositions ##

| ![http://efficient-java-matrix-library.googlecode.com/svn/wiki/SpeedSimpleMatrix.attach/svd.png](http://efficient-java-matrix-library.googlecode.com/svn/wiki/SpeedSimpleMatrix.attach/svd.png) | ![http://efficient-java-matrix-library.googlecode.com/svn/wiki/SpeedSimpleMatrix.attach/EigSymm.png](http://efficient-java-matrix-library.googlecode.com/svn/wiki/SpeedSimpleMatrix.attach/EigSymm.png) |
|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|