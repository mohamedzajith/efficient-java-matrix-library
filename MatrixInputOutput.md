# Introduction #

EJML provides several different methods for creating, saving, and displaying a matrix. A matrix can be saved and loaded from a file, displayed visually in a window, printed to the console, created from raw arrays or strings.  The following describes different ways in which input or output a matrix.

  * [Creating a matrix](#Creating_a_matrix.md)
  * [Text output](#Text_Output.md)
  * [CSV output](#CSV_Input/Outut.md)
  * [Binary Input/Output](#Serialized_Binary_Input/Output.md)
  * [Visually Displaying a Matrix](#Visual_Display.md)

# Creating a Matrix #

Several different constructors are provided in SimpleMatrix and DenseMatrix64F for creating a matrix.  This allows for easy input from different libraries and formats.  The following example code shows four different ways in which the same matrix can be defined in a constructor.  Equivalent constructors are provided in SimpleMatrix and DenseMatrix64F.

Sample Code:
```
public static void main( String []args ) {

    double []rowMajor = new double[]{1,2,3,4,5,6};
    double []columnMajor = new double[]{1,4,2,5,3,6};
    double [][]doubleArray = new double[][]{{1,2,3},{4,5,6}};

    DenseMatrix64F A = new DenseMatrix64F(2,3,true,1,2,3,4,5,6);
    DenseMatrix64F B = new DenseMatrix64F(2,3,true,rowMajor);
    DenseMatrix64F C = new DenseMatrix64F(2,3,false,columnMajor);
    DenseMatrix64F D = new DenseMatrix64F(doubleArray);

    A.print();
    B.print();
    C.print();
    D.print();
}
```

Output:
```
Type = dense , numRows = 2 , numCols = 3
 1.000   2.000   3.000  
 4.000   5.000   6.000  
Type = dense , numRows = 2 , numCols = 3
 1.000   2.000   3.000  
 4.000   5.000   6.000  
Type = dense , numRows = 2 , numCols = 3
 1.000   2.000   3.000  
 4.000   5.000   6.000  
Type = dense , numRows = 2 , numCols = 3
 1.000   2.000   3.000  
 4.000   5.000   6.000  
```

# Text Output #

Most common way to output the state of a matrix is by printing its state to the console.  Both SimpleMatrix and DenseMatrix64F provide various print() write the matrix's state to standard out.  These are wrappers around print() functions contained inside of MatrixIO.

Output is done in a row-major floating point format.  The format can be modified by providing a text formatting string that is compatible with printf().

Sample Code:
```
public static void main( String []args ) {
    DenseMatrix64F A = new DenseMatrix64F(2,3,true,1.1,2.34,3.35436,4345,59505,0.00001234);

    A.print();
    System.out.println();
    A.print("%e");
    System.out.println();
    A.print("%10.2f");
}
```

Output:
```
Type = dense , numRows = 2 , numCols = 3
 1.100   2.340   3.354  
4345.000  59505.000   0.000  

Type = dense , numRows = 2 , numCols = 3
1.100000e+00 2.340000e+00 3.354360e+00 
4.345000e+03 5.950500e+04 1.234000e-05 

Type = dense , numRows = 2 , numCols = 3
      1.10       2.34       3.35 
   4345.00   59505.00       0.00 
```

# CSV Input/Outut #

Column Space Value (CSV) reader and writer is provided by EJML.  The advantage of this file format is that it's human readable, the disadvantage is that its large and slow.  Two CSV formats are supported, one where the first line specifies the matrix dimension and the other the user specifies it programmatically.

In the example below, the matrix size is specified in the first line, row then column.  The remainder of the file contains the value of each row in the matrix.  A file containing
```
2 3
2.4 6.7 9
-2 3 5
```
would describe a matrix with 2 rows and 3 columns.


MatrixIO Example:
```
    public static void main( String args[] ) {
        DenseMatrix64F A = new DenseMatrix64F(2,3,true,new double[]{1,2,3,4,5,6});

        try {
            MatrixIO.saveCSV(A, "matrix_file.csv");
            DenseMatrix64F B = MatrixIO.loadCSV("matrix_file.csv");
            B.print();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
```

SimpleMatrix Example:
```
    public static void main( String args[] ) {
        SimpleMatrix A = new SimpleMatrix(2,3,true,new double[]{1,2,3,4,5,6});

        try {
            A.saveToFileCSV("matrix_file.csv");
            SimpleMatrix B = SimpleMatrix.loadCSV("matrix_file.csv");
            B.print();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
```

# Serialized Binary Input/Output #

DenseMatrix64F is a serializable object and is fully compatible with any Java serialization routine.  MatrixIO provides save() and load() functions for saving to and reading from a file.  The matrix is saved as a Java binary serialized object.  SimpleMatrix provides its own function (that are wrappers around MatrixIO) for saving and loading from files.

MatrixIO Example:
```
    public static void main( String args[] ) {
        DenseMatrix64F A = new DenseMatrix64F(2,3,true,new double[]{1,2,3,4,5,6});

        try {
            MatrixIO.saveBin(A,"matrix_file.data");
            DenseMatrix64F B = MatrixIO.loadBin("matrix_file.data");
            B.print();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
```

**NOTE** in v0.18 saveBin/loadBin is actually saveXML/loadXML, which is a mistake since its not in an xml format.

SimpleMatrix Example:
```
    public static void main( String args[] ) {
        SimpleMatrix A = new SimpleMatrix(2,3,true,new double[]{1,2,3,4,5,6});

        try {
            A.saveToFileBinary("matrix_file.data");
            SimpleMatrix B = SimpleMatrix.loadBinary("matrix_file.data");
            B.print();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
```

# Visual Display #

Understanding the state of a matrix from text output can be difficult, especially for large matrices.  To help in these situations a visual way of viewing a matrix is provided in MatrixVisualization.  By calling MatrixVisualization.show() a window will be created that shows the matrix.  Positive elements will appear as a shade of red, negative ones as a shade of blue, and zeros as black.  How red or blue an element is depends on its magnitude.

Example Code:
```
    public static void main( String args[] ) {
        DenseMatrix64F A = new DenseMatrix64F(4,4,true,
                0,2,3,4,-2,0,2,3,-3,-2,0,2,-4,-3,-2,0);

        MatrixIO.show(A,"Small Matrix");

        DenseMatrix64F B = new DenseMatrix64F(25,50);
        for( int i = 0; i < 25; i++ )
            B.set(i,i,i+1);

        MatrixIO.show(B,"Larger Diagonal Matrix");
    }
```

Output:
| ![http://efficient-java-matrix-library.googlecode.com/svn/wiki/MatrixInputOutput.attach/small_matrix.gif](http://efficient-java-matrix-library.googlecode.com/svn/wiki/MatrixInputOutput.attach/small_matrix.gif) | ![http://efficient-java-matrix-library.googlecode.com/svn/wiki/MatrixInputOutput.attach/larger_matrix.gif](http://efficient-java-matrix-library.googlecode.com/svn/wiki/MatrixInputOutput.attach/larger_matrix.gif) |
|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|