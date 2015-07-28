#Overview of FixedMatrix

# Introduction #

FixedMatrix are matrices which have a fixed size.  Instead of storing data in an array it is stored as class parameters.  See the example below:
```
public class FixedMatrix3x3_64F {
    public double a11,a12,a13;
    public double a21,a22,a23;
    public double a31,a32,a33;
```
This avoids array access overhead, allowing it to run many times faster.  Matrix multiplication for a 3x3 matrix is about 8 times faster.

Most standard basic linear algebra operations are supported for FixedMatrix.  The quest way to work with a fixed matrix is to simply access the element directly, but the usual setters and getters are also provided.

# Accessors #

  * get( row , col )
  * set( row , col , value )
    * Returns or sets the value of an element at the specified row and column.
  * unsafe\_get( row , col )
  * unsafe\_set( row , col , value )
    * Faster version of get() or set() that does not perform bounds checking.

# Operations #

Several "Ops" classes provide functions for manipulating DenseMatrix64F and they are contained inside of the org.ejml.ops package.

  * FixedOpsN (e.g. FixedOps2 to FixedOps6)
    * Equivalent to CommonOps for DenseMatrix64F.
    * Supports the most common operations
  * ConvertMatrixType
    * Provides functions to convert into different matrix types.  Useful when you need to do SVD or some other operation not supported by FixedOpsN