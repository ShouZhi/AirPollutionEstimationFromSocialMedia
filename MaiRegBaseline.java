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
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

import bwaldvogel.liblinear.Linear;
import bwaldvogel.liblinear.Model;
import bwaldvogel.liblinear.Parameter;
import bwaldvogel.liblinear.Predict;
import bwaldvogel.liblinear.Problem;
import bwaldvogel.liblinear.SolverType;
import bwaldvogel.liblinear.Train;


public class MaiRegBaseline implements Method{

	/**
	 * the estimation and prediction of the MAI linear model method
	 * it first traing a linear regression model on the reduced training data by calling Liblinear package
	 * the reduced training data contains only one feature: proportion of mai (haze in chinese) in the social media
	 * @see http://pages.cs.wisc.edu/~jerryzhu/pub/airPollution.pdf
	 * then it applies the model to predict the AQI for test data points 
	 * @param fold
	 * the fold number of the training and test file
	 * @param option
	 * the parameter setting of the air pollution program
	 * @param city2Items
	 * the map from each city names to the associated data points (social media content and AQI information)
	 */
	public void run(int fold, Option option, HashMap<String, ArrayList<Item> > city2Items) throws Exception {
		// TODO Auto-generated method stub
		Problem problem = Train.readProblem(new File(option.TRAIN_PATH + fold), 0);
		SolverType solver = SolverType.L2R_L2LOSS_SVR_DUAL;
		//SolverType solver = SolverType.L2R_L2LOSS_SVR;
		double C = option.c, eps = 0.01;
		Parameter parameter = new Parameter(solver, C, eps);
		//double[] weights = {1, 2, 10, 20, 50};
		//int[] weightLabels = {0, 1, 2, 3, 4};
		//parameter.setWeights(weights, weightLabels);
		Model model = Linear.train(problem, parameter);

		FileReader fr = new FileReader(option.TEST_PATH + fold);
		BufferedReader br = new BufferedReader(fr);
		Writer fw = new FileWriter(option.ANS_DIR + "Predict_" + fold);
		Predict.doPredict(br, fw, model);
		fw.close();
		br.close();
		fr.close();
	}

	public void filterData(HashMap<String, ArrayList<Item>> city2Items,
			int idMai) {
		// TODO Auto-generated method stub
		for(String city : city2Items.keySet())
			for(Item item : city2Items.get(city))
			{
				double weight = 0.0;
				for(int i = 0; i < item.indices.size(); i++)
					if( item.indices.get(i) == idMai )
						weight = item.weights.get(i);
				
				item.indices.clear();
				item.weights.clear();
				
				item.indices.add( idMai );
				item.weights.add( weight );
			}
	}
}
