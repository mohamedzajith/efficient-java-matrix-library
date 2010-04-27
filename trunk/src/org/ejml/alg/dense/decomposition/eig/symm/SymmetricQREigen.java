/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml.alg.dense.decomposition.eig.symm;

import org.ejml.UtilEjml;
import org.ejml.alg.dense.decomposition.eig.EigenvalueSmall;
import org.ejml.data.DenseMatrix64F;

import java.util.Random;


/**
 * A helper class for the symmetric matrix implicit QR algorithm for eigenvalue decomposition.
 * Performs most of the basic operations needed to extract eigenvalues and eigenvectors.
 *
 * @author Peter Abeles
 */
public class SymmetricQREigen {

    // used in exceptional shifts
    protected Random rand = new Random(0x34671e);

    // how many steps has it taken
    protected int steps;

    // how many exception shifts has it performed
    protected int numExceptional;
    // the step number of the last exception shift
    protected int lastExceptional;

    // used to compute eigenvalues directly
    protected EigenvalueSmall eigenSmall = new EigenvalueSmall();

    // orthogonal matrix used in similar transform.  optional
    protected DenseMatrix64F Q;

    // size of the matrix being processed
    protected int N;
    // diagonal elements in the matrix
    protected double diag[];
    // the off diagonal elements
    protected double off[];

    // which submatrix is being processed
    protected int x1;
    protected int x2;

    // where splits are performed
    protected int splits[];
    protected int numSplits;

    // current value of the bulge
    private double bulge;

    public SymmetricQREigen() {
        diag = new double[1];
        off = new double[1];
        splits = new int[1];
    }

    public void printMatrix() {
        System.out.print("Off Diag[ ");
        for( int j = 0; j < N-1; j++ ) {
            System.out.printf("%5.2f ",off[j]);
        }
        System.out.println();
        System.out.print("    Diag[ ");
        for( int j = 0; j < N; j++ ) {
            System.out.printf("%5.2f ",diag[j]);
        }
        System.out.println();
    }

    public void setQ(DenseMatrix64F q) {
        Q = q;
    }

    public void incrementSteps() {
        steps++;
    }

    /**
     * Sets up and declares internal data structures.
     *
     * @param T The tridiagonal matrix that is to be processed.  Not modified.
     */
    public void init( DenseMatrix64F T ) {
        if( T.numCols != T.numRows )
            throw new IllegalArgumentException("Matrix must be square.");

        reset(T.numCols);

        // copy the tridiagonal portion of the matrix
        for( int i = 0; i < N; i++ ) {
            diag[i] = T.data[i*N+i];

            if( i+1 < N ) {
                off[i] = T.data[i*N+i+1];
            }
        }
    }

    /**
     * Exchanges the internal array of the diagonal elements for the provided one.
     */
    public double[] swapDiag( double diag[] ) {
        double[] ret = this.diag;
        this.diag = diag;

        return ret;
    }

    /**
     * Exchanges the internal array of the off diagonal elements for the provided one.
     */
    public double[] swapOff( double off[] ) {
        double[] ret = this.off;
        this.off = off;

        return ret;
    }

    /**
     * Sets the size of the matrix being decomposed, declares new memory if needed,
     * and sets all helper functions to their initial value.
     */
    public void reset( int N ) {
        this.N = N;

        if( diag.length < N ) {
            diag = new double[N];
            off = new double[N-1];
            splits = new int[N];
        }

        numSplits = 0;

        x1 = 0;
        x2 = N-1;

        steps = numExceptional = lastExceptional = 0;

        this.Q = null;
    }

    public double[] copyDiag( double []ret ) {
        if( ret == null || ret.length < N ) {
            ret = new double[N];
        }

        System.arraycopy(diag,0,ret,0,N);

        return ret;
    }

    public double[] copyOff( double []ret ) {
        if( ret == null || ret.length < N-1 ) {
            ret = new double[N-1];
        }

        System.arraycopy(off,0,ret,0,N-1);

        return ret;
    }

    public double[] copyEigenvalues( double []ret ) {
        if( ret == null || ret.length < N ) {
            ret = new double[N];
        }

        System.arraycopy(diag,0,ret,0,N);

        return ret;
    }

    /**
     * Sets which submatrix is being processed.
     * @param x1 Lower bound, inclusive.
     * @param x2 Upper bound, inclusive.
     */
    public void setSubmatrix( int x1 , int x2 ) {
        this.x1 = x1;
        this.x2 = x2;
    }

    /**
     * Checks to see if the specified off diagonal element is zero using a relative metric.
     */
    protected boolean isZero( int index ) {
        double bottom = Math.abs(diag[index])+Math.abs(diag[index+1]);

        return( Math.abs(off[index]) <= 0.5*bottom*UtilEjml.EPS);
    }

    protected void performImplicitSingleStep( double lambda )
    {
        if( x2-x1 == 1  ) {
            createBulge2by2(x1,lambda);
        } else {
            createBulge(x1,lambda);

            for( int i = x1; i < x2-2 && bulge != 0.0; i++ ) {
                removeBulge(i);

            }
            removeBulgeEnd(x2-2);
        }
    }

    protected void updateQ( int m , int n , double c ,  double s )
    {
        int rowA = m*N;
        int rowB = n*N;

        for( int i = 0; i < N; i++ ) {
            double a = Q.data[rowA+i];
            double b = Q.data[rowB+i];
            Q.data[rowA+i] = c*a + s*b;
            Q.data[rowB+i] = -s*a + c*b;
        }
    }

    /**
     * Performs a similar transform on A-pI
     */
    protected void createBulge( int x1 , double p ) {
        double a11 = diag[x1];
        double a22 = diag[x1+1];
        double a12 = off[x1];
        double a23 = off[x1+1];

        // normalize to improve resistance to overflow/underflow
        double abs11 = Math.abs(a11);
        double abs12 = Math.abs(a12);

        double scale = abs11 > abs12 ? abs11 : abs12;

        double l = (a11-p)/scale;
        abs12 /= scale;

        double alpha = Math.sqrt(l*l+abs12*abs12);

        double c = l/alpha;
        double s = a12/(scale*alpha);

//        double l = a11-p;
//        double alpha = Math.sqrt(l*l+a12*a12);
//
//        double c = l/alpha;
//        double s = a12/alpha;

        double c2 = c*c;
        double s2 = s*s;
        double cs = c*s;

        // multiply the rotator on the top left.
        diag[x1] = c2*a11 + 2.0*cs*a12 + s2*a22;
        diag[x1+1] = c2*a22 - 2.0*cs*a12+s2*a11;
        off[x1] = c2*a12+cs*a22 - cs*a11 - s2*a12;
        off[x1+1] = c*a23;
        bulge = s*a23;

        if( Q != null )
            updateQ(x1,x1+1,c,s);
    }

    protected void createBulge2by2( int x1 , double p ) {
        double a11 = diag[x1];
        double a22 = diag[x1+1];
        double a12 = off[x1];

        // normalize to improve resistance to overflow/underflow
        double abs11 = Math.abs(a11);
        double abs12 = Math.abs(a12);

        double scale = abs11 > abs12 ? abs11 : abs12;
        abs12 /= scale;

        double l = (a11-p)/scale;

        double alpha = Math.sqrt(l*l+abs12*abs12);

        double c = l/alpha;
        double s = a12/(scale*alpha);

        double c2 = c*c;
        double s2 = s*s;
        double cs = c*s;

        // multiply the rotator on the top left.
        diag[x1] = c2*a11 + 2.0*cs*a12 + s2*a22;
        diag[x1+1] = c2*a22 - 2.0*cs*a12+s2*a11;
        off[x1] = c2*a12+cs*a22 - cs*a11 - s2*a12;

        if( Q != null )
            updateQ(x1,x1+1,c,s);
    }

    protected void removeBulge( int x1 ) {
        double a22 = diag[x1+1];
        double a33 = diag[x1+2];
        double a12 = off[x1];
        double a23 = off[x1+1];
        double a34 = off[x1+2];

        // normalize to improve resistance to overflow/underflow
        double absBulge = Math.abs(bulge);
        double abs12 = Math.abs(a12);

        double scale = absBulge > abs12 ? absBulge : abs12;

        abs12/=scale;
        absBulge/=scale;

        double gamma = scale*Math.sqrt(abs12*abs12+absBulge*absBulge);

        double c = a12/gamma;
        double s = bulge/gamma;

        double c2 = c*c;
        double s2 = s*s;
        double cs = c*s;

        // multiply the rotator on the top left.
        diag[x1+1] = c2*a22 + 2.0*cs*a23 + s2*a33;
        diag[x1+2] = c2*a33 - 2.0*cs*a23 + s2*a22;
        off[x1] = c*a12 + s*bulge;
        off[x1+1] = c2*a23 + cs*a33 - cs*a22 - s2*a23;
        off[x1+2] = c*a34;
        bulge = s*a34;

        if( Q != null )
            updateQ(x1+1,x1+2,c,s);
    }

    /**
     * Rotator to remove the bulge
     */
    protected void removeBulgeEnd( int x1 ) {
        double a22 = diag[x1+1];
        double a12 = off[x1];
        double a23 = off[x1+1];
        double a33 = diag[x1+2];

         // normalize to improve resistance to overflow/underflow
        double absBulge = Math.abs(bulge);
        double abs12 = Math.abs(a12);

        double scale = absBulge > abs12 ? absBulge : abs12;

        abs12/=scale;
        absBulge/=scale;

        double gamma = scale*Math.sqrt(abs12*abs12+absBulge*absBulge);

        double c = a12/gamma;
        double s = bulge/gamma;

        double c2 = c*c;
        double s2 = s*s;
        double cs = c*s;

        // multiply the rotator on the top left.
        diag[x1+1] = c2*a22 + 2.0*cs*a23 + s2*a33;
        diag[x1+2] = c2*a33 - 2.0*cs*a23 + s2*a22;
        off[x1] = c*a12+s*bulge;
        off[x1+1] = c2*a23-cs*a22+cs*a33-s2*a23;

        if( Q != null )
            updateQ(x1+1,x1+2,c,s);
    }

    /**
     * Computes the eigenvalue of the 2 by 2 matrix.
     */
    protected void eigenvalue2by2( int x1 ) {
        double a = diag[x1];
        double b = off[x1];
        double c = diag[x1+1];

        // normalize to reduce overflow
        double absA = Math.abs(a);
        double absB = Math.abs(b);
        double absC = Math.abs(c);

        double scale = absA > absB ? absA : absB;
        if( absC > scale ) scale = absC;

        // see if it is a pathological case.  the diagonal must already be zero
        // and the eigenvalues are all zero.  so just return
        if( scale == 0 )
            return;

        a /= scale;
        b /= scale;
        c /= scale;

        eigenSmall.symm2x2_fast(a,b,c);

        off[x1] = 0;
        diag[x1] = scale*eigenSmall.value0.real;
        diag[x1+1] = scale*eigenSmall.value1.real;
    }

    /**
     * Perform a shift in a random direction that is of the same magnitude as the elements in the matrix.
     */
    public void exceptionalShift() {
        // perform a random shift that is of the same magnitude as the matrix
        double val = Math.abs(diag[x2]);

        if( val == 0 )
            val = 1;

        val *= 0.95+0.2*(rand.nextDouble()-0.5);

        if( rand.nextBoolean() )
            val = -val;

        if( x2-x1==1 ) {

        } else {
            performImplicitSingleStep(val);
        }

        lastExceptional = steps;
        numExceptional++;
    }

    /**
     * Tells it to process the submatrix at the next split.  Should be called after the
     * current submatrix has been processed.
     */
    public boolean nextSplit() {
        if( numSplits == 0 )
            return false;
        x2 = splits[--numSplits];
        if( numSplits > 0 )
            x1 = splits[numSplits-1]+1;
        else
            x1 = 0;

        return true;
    }

    public int getMatrixSize() {
        return N;
    }

    public void resetSteps() {
        steps = 0;
        lastExceptional = 0;
    }
}