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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class KNNBaseline implements Method {

	/**
	 * the estimation and prediction of the K-nearest neighbor (KNN) method
	 * it predict the AQI of each test city by taking the average of the AQI of the k nearest neighbor cities 
	 * in the training data
	 * @param fold
	 * the fold number of the training and test file
	 * @param option
	 * the parameter setting of the air pollution program
	 * @param city2Items
	 * the map from each city names to the associated data points (social media content and AQI information)
	 */
	public void run(int fold, Option option, HashMap<String, ArrayList<Item> > city2Items) throws Exception {
		// TODO Auto-generated method stub
		readCitySet( option.TEST_PATH + "Names" +fold , option.testCities );
		readCitySet( option.TRAIN_PATH + "Names" +fold , option.trainCities );		
		
		Writer fw = new FileWriter(option.ANS_DIR +  "Predict_" + fold);
		for(int i = 0; i < option.testCities.size(); i++)
		{
			String testCt = option.testCities.get(i);
			
			HashSet<Integer> cityIdUsed = new HashSet<Integer>();
			ArrayList<Double> preds = new ArrayList<Double>();
			for(int j = 0; j < city2Items.get(testCt).size(); j++)
				preds.add( 0.0 );
			
			System.out.println( testCt );
			for(int k = 0; k < option.K; k++)
			{
				double bestDis = 1e9;
				int bestId = -1;
				for(int j = 0; j < option.trainCities.size(); j++)
				{
					String trainCt = option.trainCities.get(j);
					if( !cityIdUsed.contains(j) )
						if( option.city2Coor.get(trainCt).distance(option.city2Coor.get(testCt)) < bestDis )
						{
							bestDis = option.city2Coor.get(trainCt).distance(option.city2Coor.get(testCt));
							bestId = j;
						}
				}
				
				System.out.print( option.trainCities.get(bestId) + " " );

				if( bestId != -1 )
				{
					cityIdUsed.add( bestId );
					String trainCity = option.trainCities.get(bestId);
					// add the response to the correspond date data points
					for(Item it : city2Items.get(trainCity))
						for(int j = 0; j < city2Items.get(testCt).size(); j++)
							if( it.date.equalsIgnoreCase( city2Items.get(testCt).get(j).date ) )
								preds.set( j , preds.get(j) + it.response );
				}
			}
			System.out.println();
			for(int j = 0; j < preds.size(); j++)
				preds.set( j , preds.get(j) / option.K);
			for(int j = 0; j < preds.size(); j++)
				fw.write( ""+preds.get(j) + "\n" );
		}
		fw.close();
	}

	private void readCitySet(String fName, ArrayList<String> listCities) throws IOException {
		// TODO Auto-generated method stub
		BufferedReader fin = new BufferedReader( new FileReader( fName) );
		String line;
		listCities.clear();
		
		while( (line=fin.readLine()) != null )
		{
			String str[] = line.split(" ");
			boolean exist = false;
			for(int i = 0; i < listCities.size(); i++)
			{
				if( listCities.get(i).equals(str[0]) )
					exist = true;
			}
			if( !exist )
			{
				listCities.add( str[0] );
			}
		}
		
		fin.close();
	}
}
