## Random Matrices ##

Random matrices and vectors are used extensively in Monti Carlo methods, simulations, and testing.  There are many different types of ways in which an matrix can be randomized.  For example, each element can be independent variables or the rows/columns are independent orthogonal vectors. EJML provides built in methods for creating a variety types of random matrices.

Functions for creating random matrices are contained inside of the RandomMatrices class.  A partial list of types of random matrices it can create includes:
  * Uniform distribution in each element.
  * Uniform distribution along diagonal elements.
  * Uniform distribution triangular.
  * Symmetric from a uniform distribution.
  * Random with fixed singular values.
  * Random with fixed eigen values.
  * Random orthogonal.

Creating a random matrix is very simple as the code sample below shows:
```
Random rand = new Random();
DenseMatrix A = RandomMatrices.createSymmetric(20,-2,3,rand);
```
This will create a random 20 by 20 matrix 'A' which is symmetric and has elements whose values range from -2 to 3.

## Matrix Features ##

It is common to describe a matrix based on different features it might posses.  A common example is a symmetric matrix whose elements have the following properties: a<sub>i,j</sub> == a<sub>j,i</sub>.  Testing for certain features is often required at runtime to detect computational errors caused by bad inputs or round off errors.

MatrixFeatures contains a list of commonly used matrix features.  In practice a matrix in a compute will almost never exactly match a feature's definition due to small round off errors.  For this reason a tolerance parameter is almost always provided to test if a matrix has a feature or not.  What a reasonable tolerance is is dependent on the applications.

Functions include:
  * If two matrices are identical.
  * If a matrix contains NaN or other uncountable numbers.
  * If a matrix is symmetrix.
  * If a matrix is positive definite.
  * If a matrix is orthogonal.
  * If a matrix is an identity matrix.
  * If a matrix is the negative of another one.
  * If a matrix is triangular.
  * A matrix's rank and nullity.
  * And several others...

Code Example:
```
DenseMatrix A = new DenseMatrix(2,2);
A.set(0,1,2);
A.set(1,0,-2.0000000001);

if( MatrixFeatures.isSkewSymmetric(A,1e-8) )
  System.out.println("Is skew symmetric!");
else
  System.out.println("Should be skew symmetric!");
```
Note that even through it is not exactly skew symmetric it will be within tolerance.

## Matrix Norms ##

Norms are a measure of the size of a vector or a matrix.  One typical application is in error analysis.

Vector norms have the following properties:

  1. |x| > 0 if x != 0 and |0|= 0
  1. |a\*x| = |a| |x|
  1. |x+y| <= |x| + |y|

Matrix norms have the following properties:

  1. |A| > 0 if A != 0
  1. | a A | = |a| |A|
  1. |A+B| <= |A| + |B|
  1. |AB| <= |A| |B|

where A and B are m by n matrices.  Note that the last item in the list only applies to square matrices.

In EJML norms are computed inside the NormOps class.  For some norms it will provide a fast method of computing the norm.  Typically this means that it is skipping some steps that ensure numerical stability over a wider range of inputs. In applications where the input matrices or vectors are known to be well behaved the fast functions can be used.

Code Example:
```
double v = NormOps.normF(A);
```
which computes the Frobenius norm of 'A'.