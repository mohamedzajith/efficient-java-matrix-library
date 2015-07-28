# Introduction #

DenseMatrix64F is the most common basic matrix format in EJML.  It provides basic operators for accessing elements within a matrix and well as its size and shape.  More complex functions for manipulating DenseMatrix64F are available in Ops classes, described below.  This was done to simplify the design and avoid having a single monolithic difficult to understand class.  Internally it stores the matrix in a single array using a row-major format.

The operator interface in EJML works with DenseMatrix64F and is designed to enable highly efficient code to be written.  An easier to use but more restrictive interface is provided by SimpleMatrix.

# Accessors #

  * get( row , col )
  * set( row , col , value )
    * Returns or sets the value of an element at the specified row and column.
  * unsafe\_get( row , col )
  * unsafe\_set( row , col , value )
    * Faster version of get() or set() that does not perform bounds checking.
  * get( index )
  * set( index )
    * Returns or sets the value of an element at the specified index.  Useful for vectors and element-wise operations.
  * iterator( boolean rowMajor, int minRow, int minCol, int maxRow, int maxCol )
    * An iterator that iterates through the sub-matrix by row or by column.

# Operations #

Several "Ops" classes provide functions for manipulating DenseMatrix64F and they are contained inside of the org.ejml.ops package.

  * CommonOps
    * Provides the most common matrix operations.
  * EigenOps
    * Provides operations related to eigenvalues and eigenvectors.
  * MatrixFeatures
    * Used to compute various features related to a matrix.
  * NormOps
    * Operations for computing different matrix norms.
  * SingularOps
    * Operations related to singular value decompositions.
  * SpecializedOps
    * Grab bag for operations which do not fit in anywhere else.

Different types of random matrices can be generated using RandomMatrices.