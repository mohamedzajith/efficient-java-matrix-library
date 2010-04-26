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

package org.ejml.alg.dense.decomposition.eig;

import org.ejml.alg.dense.decomposition.EigenDecomposition;
import org.ejml.alg.dense.decomposition.eig.watched.WatchedDoubleStepQREigenvalue;
import org.ejml.alg.dense.decomposition.eig.watched.WatchedDoubleStepQREigenvector;
import org.ejml.alg.dense.decomposition.hessenberg.HessenbergSimilarDecomposition;
import org.ejml.data.Complex64F;
import org.ejml.data.DenseMatrix64F;


/**
 * <p>
 * Finds the eigenvalue decomposition of an arbitrary square matrix using the implicit double-step QR algorithm.
 * Watched is included in its name because it is designed to print out internal debugging information.  This
 * class is still underdevelopment and has yet to be optimized.
 * </p>
 *
 * <p>
 * Based off the description found in:<br>
 * David S. Watkins, "Fundamentals of Matrix Computations." Second Edition.
 * </p>
 *
 * @author Peter Abeles
 */
//TODO looks like there might be some pointless copying of arrays going on
public class WatchedDoubleStepQRDecomposition implements EigenDecomposition {

    HessenbergSimilarDecomposition hessenberg;
    WatchedDoubleStepQREigenvalue algValue;
    WatchedDoubleStepQREigenvector algVector;

    DenseMatrix64F H;

    public WatchedDoubleStepQRDecomposition() {
        hessenberg = new HessenbergSimilarDecomposition(10);
        algValue = new WatchedDoubleStepQREigenvalue();
        algVector = new WatchedDoubleStepQREigenvector();
    }

    @Override
    public boolean decompose(DenseMatrix64F A) {

        hessenberg.decompose(A);

        H = hessenberg.getH(null);

        algValue.getImplicitQR().createR = false;
//        algValue.getImplicitQR().setChecks(true,true,true);

        if( !algValue.process(H) )
            return false;

//        for( int i = 0; i < A.numRows; i++ ) {
//            System.out.println(algValue.getEigenvalues()[i]);
//        }

        algValue.getImplicitQR().createR = true;
        return algVector.process(algValue.getImplicitQR(), H, hessenberg.getQ(null));

    }

    @Override
    public void setExpectedMaxSize(int numRows, int numCols) {
    }

    @Override
    public int getNumberOfEigenvalues() {
        return algVector.getEigenvalues().length;
    }

    @Override
    public Complex64F getEigenvalue(int index) {
        return algVector.getEigenvalues()[index];
    }

    @Override
    public DenseMatrix64F getEigenVector(int index) {
        return algVector.getEigenvectors()[index];
    }
}