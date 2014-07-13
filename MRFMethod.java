// (C) Copyright 2014, Shike Mei, Han Li and Jing Fan

// written by Shike Mei, mei@cs.wisc.edu

// This file is part of air pollution estimator (AirPE).

// AirPE is free software; you can redistribute it and/or modify it under
// the terms of the GNU General Public License as published by the Free
// Software Foundation; either version 2 of the License, or (at your
// option) any later version.

// AirPE is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
// for more details.

// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
// USA

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import Jama.LUDecomposition;
import Jama.Matrix;

public class MRFMethod implements Method {

	/**
	 * the estimation and prediction of the Markov Random Field method
	 * It is actually a combination of the linear regression model and the k nearest neighbor (KNN) model.
	 * it first training the linear regression model on the training data by calling Liblinear package
	 * It then combines the AQI of k nearest neighbor cities in the training data
	 * then it applies the model to predict the AQI for test data points (by solving an equation)
	 * @see http://pages.cs.wisc.edu/~jerryzhu/pub/airPollution.pdf
	 * @param fold
	 * the fold number of the training and test file
	 * @param option
	 * the parameter setting of the air pollution program
	 * @param city2Items
	 * the map from each city names to the associated data points (social media content and AQI information)
	 */
	public void run(int fold, Option option,
			HashMap<String, ArrayList<Item>> city2Items) throws Exception {
		// TODO Auto-generated method stub
		ArrayList<Double> combinePred = new ArrayList<Double>();
		
		Method knnBaseline = new KNNBaseline();
		knnBaseline.run(fold , option, city2Items);
		BufferedReader br = new BufferedReader( new FileReader( option.ANS_DIR + "Predict_" + fold ) );
		String line;
		while( (line = br.readLine()) != null )
		{
			combinePred.add( Double.parseDouble(line) );
		}
		br.close();
		
		Method svrBaseline = new LinearRegBaseline();
		svrBaseline.run(fold , option, city2Items);
		br = new BufferedReader( new FileReader( option.ANS_DIR + "Predict_" + fold ) );
		int iter = 0;
		while( (line = br.readLine()) != null )
		{
			combinePred.set( iter, option.alphaS/(option.alphaS+1.0/option.K)*combinePred.get(iter) + (1.0/option.K)/(option.alphaS+1.0/option.K) * Double.parseDouble(line) );
			iter += 1;
		}
		br.close();
		
		int size = 32;
		for(int i = 0; i < iter/size; i++)
		{
			double value[][] = new double[size][size];
			for(int j = 0; j < size; j++)
			{
				value[j][j] = 1.0;
				if( j-1 >=0 ) 
				{
					value[j][j-1] -= option.alphaT;
					value[j][j] += option.alphaT;
				}
				if( j+1 < size ) 
				{
					value[j][j] += option.alphaT;
					value[j][j+1] -= option.alphaT;
				}
			}
			Matrix A = new Matrix(value);
			
			double b[] = new double[size];
			for(int j = 0; j<size; j++)
				b[j] = combinePred.get(j+i*size);
			Matrix bb = new Matrix( b , size );
			
			LUDecomposition luDecomposition = new LUDecomposition(A);
			Matrix x = luDecomposition.solve(bb);
			
			for(int j = 0; j < x.getArray().length; j++)
				combinePred.set( j + i*size , x.getArray()[j][0] ); 
			bb.print(size, 1);
			x.print(size, 1);
			
		}
		
		
		BufferedWriter bw = new BufferedWriter( new FileWriter( option.ANS_DIR + "Predict_" + fold ) );
		for(int i = 0; i < combinePred.size(); i++)
			bw.write( ""+combinePred.get(i) + "\n" );
		bw.close();
	}

}
