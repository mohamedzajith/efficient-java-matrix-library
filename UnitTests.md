# Unit Testing using EJML #

Unit tests are an essential component in many projects that help ensure correctness by providing automatic tests.  EJML itself makes extensive use of unit tests as well as system level tests.  To aid in development with EJML, EJML provides several function specifically designed for creating unit tests.

Functions useful for unit tests are primarily contained in EjmlUnitTests and MatrixFeatures.  EjmlUnitTests provides a similar interface to how JUnitTest operates.  MatrixFeatures is primarily intended for extracting high level information about a matrix, but also contains several functions for testing if two matrices are equal or have specific characteristics.

The following is a brief introduction to unit testing with EJML.  See the JavaDoc for a more detailed list of functions available in EjmlUnitTests and MatrixFeatures.

# Example with EjmlUnitTests #

EjmlUnitTests provides various functions for testing equality and matrix shape. Below is an example taken from an internal EJML unit test that compares the output from two different matrix decompositions with different matrix types:
```
    DenseMatrix64F Q = decomp.getQ(null);
    BlockMatrix64F Qb = decompB.getQ(null,false);

    EjmlUnitTests.assertEquals(Q,Qb,1e-8);
```
In this example it checks to see if each element of the two matrices are within 1e-8 of each other.  The reference EjmlUnitTests to can be avoided by invoking a "static import".  If an error is found and the test fails the exact element it failed at is printed.

To maintain compatibility with different unit test libraries a generic runtime exception is thrown if a test fails.

# Example using MatrixFeatures #

MatrixFeatures is not designed with unit testing in mind, but provides many useful functions for unit tests.  For example, to test for equality between two matrices:
```
   assertTrue(MatrixFeatures.isEquals(Q,Qb,1e-8));
```
Here the JUnitTest function assertTrue() has been used.  MatrixFeatures.isEquals() returns true of the two matrices are within tolerance of each other.  If the test fails it doesn't print any additional information, such as which element it failed at.

One advantage of MatrixFeatures is it provides support for many more specialized tests. For example if you want to know if a matrix is orthogonal call MatrixFeatures.isOrthogonal() or to test for symmetry call MatrixFeatures.isSymmetric().