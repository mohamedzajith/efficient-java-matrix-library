# Introduction #


The following example is intended to show how to effectively use extraction and insertion with either operations or SimpleMatrix.    Insert and extract are ways of inserting a matrix inside of another matrix or extracting a submatrix.  These functions can often make programming much easier but are not always the most efficient way.

Performing QR decomposition is a common first step when solving the linear least squares problem.  When decomposed A=QR where Q is an M by M orthogonal matrix and R is a M by N upper triangular matrix.  Since this code is intended **for demonstration purposes only** it is naive as far as performance is concerned.

**IF YOU WANT TO DO QR-DECOMPOSITION IN PRODUCTION CODE USE DecompositionFactory.qr()**

Click to jump to a specific example:
  * [Equations](#Equations.md)
  * [SimpleMatrix](#SimpleMatrix.md)
  * [Operations](#Operations.md)

# Equations #

Implementation of QR decomposition using Equations API.

```
public class QRExampleEquation {

    // where the QR decomposition is stored
    private DenseMatrix64F QR;

    // used for computing Q
    private double gammas[];

    /**
     * Computes the QR decomposition of the provided matrix.
     *
     * @param A Matrix which is to be decomposed.  Not modified.
     */
    public void decompose( DenseMatrix64F A ) {

        Equation eq = new Equation();

        this.QR = A.copy();

        int N = Math.min(A.numCols,A.numRows);

        gammas = new double[ A.numCols ];

        for( int i = 0; i < N; i++ ) {
            // update temporary variables
            eq.alias(QR.numRows-i,"Ni",QR,"QR",i,"i");

            // Place the column that should be zeroed into v
            eq.process("v=QR(i:,i)");
            // Note that v is lazily created above.  Need direct access to it, which is done below.
            DenseMatrix64F v = eq.lookupMatrix("v");

            double maxV = CommonOps.elementMaxAbs(v);
            eq.alias(maxV,"maxV");

            if( maxV > 0 && v.getNumElements() > 1 ) {
                // normalize to reduce overflow issues
                eq.process("v=v/maxV");

                // compute the magnitude of the vector
                double tau = NormOps.normF(v);

                if( v.get(0) < 0 )
                    tau *= -1.0;

                double u_0 = v.get(0) + tau;
                double gamma = u_0/tau;

                eq.alias(gamma,"gamma",tau,"tau");
                eq.process("v=v/"+u_0);
                eq.process("v(0,0)=1");
                eq.process("QR(i:,i:) = (eye(Ni) - gamma*v*v')*QR(i:,i:)");
                eq.process("QR(i:,i) = v");
                eq.process("QR(i,i) = -1*tau*maxV");

                // save gamma for recomputing Q later on
                gammas[i] = gamma;
            }
        }
    }

    /**
     * Returns the Q matrix.
     */
    public DenseMatrix64F getQ() {
        Equation eq = new Equation();

        DenseMatrix64F Q = CommonOps.identity(QR.numRows);
        DenseMatrix64F u = new DenseMatrix64F(QR.numRows,1);

        int N = Math.min(QR.numCols,QR.numRows);

        eq.alias(u,"u",Q,"Q",QR,"QR",QR.numRows,"r");

        // compute Q by first extracting the householder vectors from the columns of QR and then applying it to Q
        for( int j = N-1; j>= 0; j-- ) {
            eq.alias(j,"j",gammas[j],"gamma");

            eq.process("u(j:,0) = [1 ; QR(j+1:,j)]");
            eq.process("Q=(eye(r)-gamma*u*u')*Q");
        }

        return Q;
    }

    /**
     * Returns the R matrix.
     */
    public DenseMatrix64F getR() {
        DenseMatrix64F R = new DenseMatrix64F(QR.numRows,QR.numCols);
        int N = Math.min(QR.numCols,QR.numRows);

        for( int i = 0; i < N; i++ ) {
            for( int j = i; j < QR.numCols; j++ ) {
                R.unsafe_set(i,j, QR.unsafe_get(i,j));
            }
        }

        return R;
    }
}
```

# SimpleMatrix #

SimpleMatrix supports simplified version of insert and extract that only works with whole matrices.  To insert a submatrix into another matrix you first need to extract the submatrix.

```
public class QRExampleSimple {

    // where the QR decomposition is stored
    private SimpleMatrix QR;

    // used for computing Q
    private double gammas[];

    /**
     * Computes the QR decomposition of the provided matrix.
     *
     * @param A Matrix which is to be decomposed.  Not modified.
     */
    public void decompose( SimpleMatrix A ) {

        this.QR = A.copy();

        int N = Math.min(A.numCols(),A.numRows());
        gammas = new double[ A.numCols() ];

        for( int i = 0; i < N; i++ ) {
            // use extract matrix to get the column that is to be zeroed
            SimpleMatrix v = QR.extractMatrix(i, END,i,i+1);
            double max = v.elementMaxAbs();

            if( max > 0 && v.getNumElements() > 1 ) {
                // normalize to reduce overflow issues
                v = v.divide(max);

                // compute the magnitude of the vector
                double tau = v.normF();

                if( v.get(0) < 0 )
                    tau *= -1.0;

                double u_0 = v.get(0) + tau;
                double gamma = u_0/tau;

                v = v.divide(u_0);
                v.set(0,1.0);

                // extract the submatrix of A which is being operated on
                SimpleMatrix A_small = QR.extractMatrix(i,END,i,END);

                // A = (I - &gamma;*u*u<sup>T</sup>)A
                A_small = A_small.plus(-gamma,v.mult(v.transpose()).mult(A_small));

                // save the results
                QR.insertIntoThis(i,i,A_small);
                QR.insertIntoThis(i+1,i,v.extractMatrix(1,END,0,1));

                // save gamma for recomputing Q later on
                gammas[i] = gamma;
            }
        }
    }

    /**
     * Returns the Q matrix.
     */
    public SimpleMatrix getQ() {
        SimpleMatrix Q = SimpleMatrix.identity(QR.numRows());

        int N = Math.min(QR.numCols(),QR.numRows());

        // compute Q by first extracting the householder vectors from the
        // columns of QR and then applying it to Q
        for( int j = N-1; j>= 0; j-- ) {
            SimpleMatrix u = new SimpleMatrix(QR.numRows(),1);
            u.insertIntoThis(j,0,QR.extractMatrix(j, END,j,j+1));
            u.set(j,1.0);

            // A = (I - &gamma;*u*u<sup>T</sup>)*A<br>
            Q = Q.plus(-gammas[j],u.mult(u.transpose()).mult(Q));
        }

        return Q;
    }

    /**
     * Returns the R matrix.
     */
    public SimpleMatrix getR() {
        SimpleMatrix R = new SimpleMatrix(QR.numRows(),QR.numCols());

        int N = Math.min(QR.numCols(),QR.numRows());

        for( int i = 0; i < N; i++ ) {
            for( int j = i; j < QR.numCols(); j++ ) {
                R.set(i,j, QR.get(i,j));
            }
        }

        return R;
    }
}
```

# Operations #

The insert and extract operations support a little bit more robustness since they allow a submatrix to be inserted or extracted without creating a temporary variable first to store intermediate results.  While more efficient it also increases the complexity.

```
public class QRExampleOps {

    // where the QR decomposition is stored
    private DenseMatrix64F QR;

    // used for computing Q
    private double gammas[];

    /**
     * Computes the QR decomposition of the provided matrix.
     *
     * @param A Matrix which is to be decomposed.  Not modified.
     */
    public void decompose( DenseMatrix64F A ) {

        this.QR = A.copy();

        int N = Math.min(A.numCols,A.numRows);

        gammas = new double[ A.numCols ];

        DenseMatrix64F A_small = new DenseMatrix64F(A.numRows,A.numCols);
        DenseMatrix64F A_mod = new DenseMatrix64F(A.numRows,A.numCols);
        DenseMatrix64F v = new DenseMatrix64F(A.numRows,1);
        DenseMatrix64F Q_k = new DenseMatrix64F(A.numRows,A.numRows);

        for( int i = 0; i < N; i++ ) {
            // reshape temporary variables
            A_small.reshape(QR.numRows-i,QR.numCols-i,false);
            A_mod.reshape(A_small.numRows,A_small.numCols,false);
            v.reshape(A_small.numRows,1,false);
            Q_k.reshape(v.getNumElements(),v.getNumElements(),false);

            // use extract matrix to get the column that is to be zeroed
            CommonOps.extract(QR,i,QR.numRows,i,i+1,v,0,0);

            double max = CommonOps.elementMaxAbs(v);

            if( max > 0 && v.getNumElements() > 1 ) {
                // normalize to reduce overflow issues
                CommonOps.divide(max,v);

                // compute the magnitude of the vector
                double tau = NormOps.normF(v);

                if( v.get(0) < 0 )
                    tau *= -1.0;

                double u_0 = v.get(0) + tau;
                double gamma = u_0/tau;

                CommonOps.divide(u_0,v);
                v.set(0,1.0);

                // extract the submatrix of A which is being operated on
                CommonOps.extract(QR,i,QR.numRows,i,QR.numCols,A_small,0,0);

                // A = (I - &gamma;*u*u<sup>T</sup>)A
                CommonOps.setIdentity(Q_k);
                CommonOps.multAddTransB(-gamma,v,v,Q_k);
                CommonOps.mult(Q_k,A_small,A_mod);

                // save the results
                CommonOps.insert(A_mod, QR, i,i);
                CommonOps.insert(v, QR, i,i);
                QR.unsafe_set(i,i,-tau*max);

                // save gamma for recomputing Q later on
                gammas[i] = gamma;
            }
        }
    }

    /**
     * Returns the Q matrix.
     */
    public DenseMatrix64F getQ() {
        DenseMatrix64F Q = CommonOps.identity(QR.numRows);
        DenseMatrix64F Q_k = new DenseMatrix64F(QR.numRows,QR.numRows);
        DenseMatrix64F u = new DenseMatrix64F(QR.numRows,1);

        DenseMatrix64F temp = new DenseMatrix64F(QR.numRows,QR.numRows);

        int N = Math.min(QR.numCols,QR.numRows);

        // compute Q by first extracting the householder vectors from the
        // columns of QR and then applying it to Q
        for( int j = N-1; j>= 0; j-- ) {
            if( j + 1 < N )
                u.set(j+1,0);

            CommonOps.extract(QR,j, QR.numRows,j,j+1,u,j,0);
            u.set(j,1.0);

            // A = (I - &gamma;*u*u<sup>T</sup>)*A<br>
            CommonOps.setIdentity(Q_k);
            CommonOps.multAddTransB(-gammas[j],u,u,Q_k);
            CommonOps.mult(Q_k,Q,temp);
            Q.set(temp);
        }

        return Q;
    }

    /**
     * Returns the R matrix.
     */
    public DenseMatrix64F getR() {
        DenseMatrix64F R = new DenseMatrix64F(QR.numRows,QR.numCols);

        int N = Math.min(QR.numCols,QR.numRows);

        for( int i = 0; i < N; i++ ) {
            for( int j = i; j < QR.numCols; j++ ) {
                R.unsafe_set(i,j, QR.unsafe_get(i,j));
            }
        }

        return R;
    }
}
```