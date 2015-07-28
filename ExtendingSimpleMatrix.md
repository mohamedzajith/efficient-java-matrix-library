[SimpleMatrix](SimpleMatrix.md) provides an easy to use object oriented way of doing linear algebra.  There are many other problems which use matrices and could use SimpleMatrix's functionality.  In those situations it is desirable to simply extend SimpleMatrix and add additional functions as needed.

Naively extending SimpleMatrix is problematic because internally SimpleMatrix creates new matrices and its functions returned objects of the wrong type.  To get around these problems, instead SimpleBase should be extended and its abstract functions implemented.  SimpleBase provides all the core functionality of SimpleMatrix, with the exception of its static functions.  Using generics strong typing is maintained and by overloading "createMatrix( int numRows , int numCols )" the returned matrices will be the correct type.

An example is provided below where a new class called StatisticsMatrix is created that adds statistical functions to SimpleMatrix.  Usage examples are provided in its main() function.

```
/**
 * Example of how to extend "SimpleMatrix" and add your own functionality.  In this case
 * two basic statistic operations are added.  Since SimpleBase is extended and StatisticsMatrix
 * is specified as the generics type, all "SimpleMatrix" operations return a matrix of
 * type StatisticsMatrix, ensuring strong typing.
 *
 * @author Peter Abeles
 */
public class StatisticsMatrix extends SimpleBase<StatisticsMatrix> {

    public StatisticsMatrix( int numRows , int numCols ) {
        super(numRows,numCols);
    }

    protected StatisticsMatrix(){}

    /**
     * Wraps a StatisticsMatrix around 'm'.  Does NOT create a copy of 'm' but saves a reference
     * to it.
     */
    public static StatisticsMatrix wrap( DenseMatrix64F m ) {
        StatisticsMatrix ret = new StatisticsMatrix();
        ret.mat = m;

        return ret;
    }

    /**
     * Computes the mean or average of all the elements.
     *
     * @return mean
     */
    public double mean() {
        double total = 0;

        final int N = getNumElements();
        for( int i = 0; i < N; i++ ) {
            total += get(i);
        }

        return total/N;
    }

    /**
     * Computes the unbiased standard deviation of all the elements.
     *
     * @return standard deviation
     */
    public double stdev() {
        double m = mean();

        double total = 0;

        final int N = getNumElements();
        if( N <= 1 )
            throw new IllegalArgumentException("There must be more than one element to compute stdev");


        for( int i = 0; i < N; i++ ) {
            double x = get(i);

            total += (x - m)*(x - m);
        }

        total /= (N-1);

        return Math.sqrt(total);
    }

    /**
     * Returns a matrix of StatisticsMatrix type so that SimpleMatrix functions create matrices
     * of the correct type.
     */
    @Override
    protected StatisticsMatrix createMatrix(int numRows, int numCols) {
        return new StatisticsMatrix(numRows,numCols);
    }

    public static void main( String args[] ) {
        Random rand = new Random(24234);

        int N = 500;

        // create two vectors whose elements are drawn from uniform distributions
        StatisticsMatrix A = StatisticsMatrix.wrap(RandomMatrices.createRandom(N,1,0,1,rand));
        StatisticsMatrix B = StatisticsMatrix.wrap(RandomMatrices.createRandom(N,1,1,2,rand));

        // the mean should be about 0.5
        System.out.println("Mean of A is               "+A.mean());
        // the mean should be about 1.5
        System.out.println("Mean of B is               "+B.mean());

        StatisticsMatrix C = A.plus(B);

        // the mean should be about 2.0
        System.out.println("Mean of C = A + B is       "+C.mean());

        System.out.println("Standard deviation of A is "+A.stdev());
        System.out.println("Standard deviation of B is "+B.stdev());
        System.out.println("Standard deviation of C is "+C.stdev());
    }
}
```