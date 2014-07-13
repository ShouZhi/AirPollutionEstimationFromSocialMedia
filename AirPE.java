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
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class AirPE {
	
	public Option option;
	public HashMap<String,ArrayList<Item> > city2Items;
	public AirPE() {
		option = new Option();
	}
	
	/**
	 * main method of AirPE class
	 * It is the entry of the whole air pollution estimation program
	 * @param args 
	 * input parameters, only contains args[0], which is the path of setting file
	 */
	public static void main(String[] args) {
		try {
			//String[] argv = {"-fold", "31", "-type", "0", "-norm", "1", "-config", "overwrite=0;startC=1000000;endC=1000000000;MSEOutputPath=./CrossValidation/SVR/MSE/"};
			AirPE cv = new AirPE();
			cv.parseCommand(args[0]);
			cv.city2Items = cv.transDataInSVRFormat(cv.option.dataDirPath , cv.option);
			//cv.divideFolds(cv.option , cv.city2Items);

			/*double tuneAlphaT[] = {0.0 , 0.00001 , 0.0001 , 0.001 , 0.01 , 0.1 , 0.2 , 0.5 , 1.0 , 2.0 , 4.0 , 8.0};
			double tuneAlphaS[] = {0.0 , 0.1 , 0.2 , 0.5 , 1.0 , 2.0 , 4.0 , 8.0};
			int tuneK[] = {1, 2 , 3 , 4 , 8 , 16 , 32 , 64};
			double tuneC[] = {0.00001 , 0.0001 , 0.001 , 0.01 , 0.1 , 1 , 10 , 100 , 1000 , 10000 , 100000};
			System.out.println("ok");
			int idTuneParam = 11;
			System.out.println( args[11] );
			
			if( args[idTuneParam].equalsIgnoreCase( "alphaT" ) )
			{
				for(int i = 0; i < tuneAlphaT.length; i++)
				{
					cv.option.alphaT = tuneAlphaT[i];
					cv.run();	
				}
			}
			else if( args[idTuneParam].equalsIgnoreCase("alphaS") )
			{
				for(int i = 0; i < tuneAlphaS.length; i++)
				{
					cv.option.alphaS = tuneAlphaS[i];
					System.out.println( cv.option.alphaS );
					cv.run();
				}
			}
			else if( args[idTuneParam].equalsIgnoreCase("K") )
			{
				for(int i = 0; i < tuneK.length; i++)
				{
					cv.option.K = tuneK[i];
					cv.run();	
				}
			}
			else if( args[idTuneParam].equalsIgnoreCase("C") )
			{
				for(int i = 0; i < tuneC.length; i++)
				{
					cv.option.c = tuneC[i];
					cv.run();	
				}
			}*/	
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * divideFolds method of AirPE class
	 * Divide the whole data into training data and test data randomly. 
	 * The divide is based on cities. 
	 * We divide 5 times and write the divided data pairs into disk
	 * @param option
	 * the options of the air pollution program
	 * @param city2Items
	 * map from each city name to the data points of the city
	 * @throws Exception 
	 */
	private void divideFolds(Option option,
			HashMap<String, ArrayList<Item>> city2Items) throws Exception {
		// TODO Auto-generated method stub
		
		// filter the test data
		ArrayList<String> listCities = shuffleCities(city2Items.keySet());
		for (int i = 0; i < listCities.size(); i++)
			if (i > 80) {
				city2Items.remove(listCities.get(i));
			}
		// shuffle the remain training data
		listCities = shuffleCities(city2Items.keySet());
		int foldSize = listCities.size() / 5;
		for (int fold=0; fold<5; fold++)
			splitDataset(option, city2Items, fold, true, fold*foldSize, (fold+1)*foldSize);
	}
	
	/**
	 * shuffleCities method of AirPE class
	 * randomly shuffle the cities.
	 * @param city2Items2
	 * map from each city name to the data points of the city
	 * @return
	 * the list of shuffled cities names
	 */
	private ArrayList<String> shuffleCities(
			Set<String> set) {
		// TODO Auto-generated method stub
		ArrayList<String> listCities = new ArrayList<String>();
		for(String city : set)
			listCities.add( city );
		
		// shuffle the cities
		for(int i = 1; i < listCities.size(); i++) {
			int p = option.rand.nextInt(i);
			String tmp = listCities.get(i);
			listCities.set(i, listCities.get(p));
			listCities.set(p, tmp);
		}
		
		return listCities;
	}

	/**
	 * run method of AirPE class
	 * do the estimation on training data
	 * and apply the estimate model to predict the air quality index (AQI) on (day,city) points in the test data
	 * report the mean square error (MSE)
	 * @throws Exception
	 */
	public void run() throws Exception {
		
		System.out.println(option.ANS_DIR);
		System.out.println(option.type);
		System.out.println(city2Items.size());		
		
		long startTime, endTime;
		startTime = System.currentTimeMillis();
		
		for (int i = 0; i < option.times; i ++) {
			System.out.println("RunId: " + i);

			Method baseline = null;
			if (option.type == 0) {
				// an linear regression model on only one feature: proportion of mai (haze in chinese) for air pollution estimation
				int idMai = 305;
				splitDataset(option, city2Items, i, true, option.trainNumCities, city2Items.keySet().size());
				baseline = new MaiRegBaseline();
				((MaiRegBaseline)baseline).filterData(city2Items, idMai);
			}
			else if (option.type == 1) {
				// an linear regression model on all features in social media content for air pollution estimation
				baseline = new LinearRegBaseline();
			}
			else if(option.type == 2) {
				// k nearest neighbor model for air pollution estimation
				splitDataset(option, city2Items, i, true, option.trainNumCities, city2Items.keySet().size());
				baseline = new KNNBaseline();
			}
			else if (option.type == 3) {
				// Markov Random Field (MRF) model for air pollution estimation
				splitDataset(option, city2Items, i, true, option.trainNumCities, city2Items.keySet().size());
				baseline = new MRFMethod();
			}
			else if (option.type == 4) {
				city2Items = transDataInSVRFormat(option.dataDirPath, option);
				ArrayList<String> listCities = new ArrayList<String>();
				for(String city : city2Items.keySet())
					listCities.add(city);
				printDataSet(listCities, option.TRAIN_PATH + i, option.TRAIN_PATH+"Names" + i, city2Items);

				city2Items = transDataInSVRFormat(option.PREDICT_PATH, option);
				listCities.clear();
				for(String city : city2Items.keySet())
					listCities.add(city);
				printDataSet(listCities, option.TEST_PATH + i, option.TEST_PATH+"Names" + i, city2Items);
	
				baseline = new LinearRegBaseline();
			}
			
			System.out.println("C Value: " + option.c);	
			
			baseline.run(i, option, city2Items);
		}
		endTime = System.currentTimeMillis();
		System.out.println("Time: " + (endTime - startTime) / 1000);
		
		// calc MSR
		calcMSE(option);
	}
	
	/**
	 * parseCommand method in AirPE class
	 * reading the setting file and set the parameters of the program
	 * @param settingFname
	 * setting file name
	 * @throws IOException
	 * error when reading the setting file
	 */
	public void parseCommand(String settingFname) throws IOException {
		if (settingFname == null) {
			System.err.println("Error: no parameter configuration");
		}
		
		BufferedReader fin = new BufferedReader(new FileReader(settingFname));
		ArrayList<String[]> args = new ArrayList<String[]>();
		for (int i = 0; i < 11; i++) {
			String line = fin.readLine();
			String tmp[] = line.replaceAll(" ", "").split(":");
			args.add(tmp);
			System.out.println(tmp[0] + " " + tmp[1]);
		}
		fin.close();

		option.dataDirPath = args.get(0)[1];
		option.TRAIN_PATH = args.get(1)[1] + "train";
		option.TEST_PATH = args.get(1)[1] + "test";
		option.COOR_PATH = args.get(2)[1];
		option.ANS_DIR = args.get(3)[1];

		option.type = Integer.parseInt(args.get(4)[1]);
		option.times = Integer.valueOf(args.get(5)[1]);
		option.trainNumCities = Integer.parseInt(args.get(6)[1]);
		
		option.c = Double.parseDouble(args.get(7)[1]);
		option.K = Integer.parseInt(args.get(8)[1]);
		option.alphaS = Double.parseDouble(args.get(9)[1]);
		option.alphaT = Double.parseDouble(args.get(10)[1]);
		
		// outdated parameters
		option.PREDICT_PATH = "none";
		option.imageFeatureFile = "none";
		
		try {
			File mseOutputDir = new File(option.ANS_DIR);
			if (!(mseOutputDir.exists() && mseOutputDir.isDirectory())) {
				System.err.println("In parsing config: MSEOutputPath doesn't exist");
				System.exit(0);
			}
		}
		catch(Exception e) {
				e.printStackTrace();
		}
		
		option.paramStr = "C=" + option.c + "K=" + option.K 
							+ "AlphaS=" + option.alphaS + "AlphaT=" + option.alphaT;
		//+ "tune=" + args[11] + "image=" + args[12];
		option.ANS_DIR = option.ANS_DIR + option.paramStr;
		option.rand = new Random(37);
		
		try {
			fin = new BufferedReader( new FileReader( option.COOR_PATH ) );
			String line;
			option.city2Coor = new HashMap<String, Coordinate>();
			while ((line=fin.readLine()) != null) {
				String str[] = line.split(" ");
				option.city2Coor.put(str[0], new Coordinate( Double.parseDouble(str[1]) 
														, Double.parseDouble(str[2])));
			}
			fin.close();
		}
		catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	/**
	 * transDataInSVRFormat method in AirPE class
	 * transform the social media AQI information into the format of LibLinear (a package of linear model)
	 * @see http://www.csie.ntu.edu.tw/~cjlin/liblinear/
	 * @param inDirPath
	 * the path of the directory of the input weibo and AQI information
	 * @param option
	 * the option of the air pollution program
	 * @return
	 * map from each city name to the associated (day,city) Social media (as features in the linear model) AQI (as response in the linear model) 
	 * data points.
	 * @throws Exception
	 */
	public HashMap<String,ArrayList<Item> > transDataInSVRFormat(String inDirPath, Option option) throws Exception {			
		File oriDir = new File(inDirPath);
		HashMap<String,ArrayList<Item> > city2Items = new HashMap<String,ArrayList<Item> >();
		
		System.out.println("read" + inDirPath);
		
		int numFolds[] = new int[5];
		for (int i = 0; i < 5; i++) numFolds[i] = 0;
		
		if (oriDir.isDirectory()) {
			String[] files = oriDir.list();
			for (String fileName : files) {

				File node = new File(oriDir.getAbsolutePath()+"/"+fileName);
				if (!node.isDirectory())
					continue;
				File realNode = new File(node.getAbsolutePath() + "/BOW");
				if (!realNode.exists()) {
					System.out.println("notexist:"+node.getAbsolutePath());
					continue;	
				}
				// first read the social media content in bag-of-words format (raw counts of words)
				
				BufferedReader br = new BufferedReader(new FileReader(realNode.getAbsolutePath()));
				String str[] = fileName.split("_");
				String cityName = str[0];
				String date = str[1];
				
				Item dataPoint = new Item(cityName, date);
				
				String line = "";
				while ((line = br.readLine()) != null) {
					String[] split = line.split(":");
					dataPoint.indices.add( Integer.parseInt(split[0]) );
					dataPoint.weights.add( Double.parseDouble(split[1]) );
				}
				
				br.close();				
				rawcounts2normalizedTotal(dataPoint);
				
				if (!option.imageFeatureFile.equals("none")) {
					File imageNode = new File(node.getAbsolutePath() + "/" + option.imageFeatureFile);
					if (!imageNode.exists()) {
						System.out.println("notexist:" + imageNode.getAbsolutePath());
						continue;	
					}
					
					br = new BufferedReader(new FileReader(imageNode.getAbsolutePath()));
							
					while ((line = br.readLine()) != null) {
						String[] split = line.split(" ");
						for (int i = split.length/2; i < split.length; i++) {
							String[] tmp = split[i].split(":");
							dataPoint.indices.add(Integer.parseInt(tmp[0])+110000);				
							if (Double.isNaN(Double.parseDouble(tmp[1])))  // "Nan" is due to zero collected images
								dataPoint.weights.add(0.0);
							else
								dataPoint.weights.add(20*Double.parseDouble(tmp[1]));
						}
					}
					br.close();				
				}
				
				// then read the AQI information
				try {
					br = new BufferedReader(new FileReader(node.getAbsolutePath() + "/AQI"));
					line = br.readLine();
					dataPoint.response = Double.parseDouble(line);
					numFolds[getTheClass(dataPoint.response)] += 1;
					br.close();
				}
				catch(Exception o) {
					dataPoint.response = 0.0;
				}
				
				// add the data points into the data set seperated by city names
				if (city2Items.containsKey(cityName))
					city2Items.get(cityName).add(dataPoint);
				else
				{
					city2Items.put(cityName, new ArrayList<Item>());
					city2Items.get(cityName).add(dataPoint);
				}
			}
		}
		
		for (String cityName : city2Items.keySet())
			Collections.sort(city2Items.get(cityName), new Comparator<Item>() {
				public int compare(Item a, Item b) {
					// TODO Auto-generated method stub
					String[] tmpa = a.date.split("-");
					String[] tmpb = b.date.split("-");
					int numA = Integer.parseInt(tmpa[0]) * 10000 + Integer.parseInt(tmpa[1]) * 100 + Integer.parseInt(tmpa[2]);
					int numB = Integer.parseInt(tmpb[0]) * 10000 + Integer.parseInt(tmpb[1]) * 100 + Integer.parseInt(tmpb[2]);
					if (numA < numB)
						return -1;
					else if(numA > numB)
						return 1;
					else
						return 0;
				}
		    });
			
		
		int sumFolds = 0;
		for(int i = 0; i < 5; i++)
			sumFolds += numFolds[i];
		for(int i = 0; i < 5; i++)
			System.out.println( numFolds[i] + " " + ((double)numFolds[i])/sumFolds);
		return city2Items;
	}
	
	/**
	 * rawcounts2normalizedTotal method of AirPE class
	 * normalize the raw word counts vector
	 * @param dataPoint
	 * the raw word counts vector
	 */
	private void rawcounts2normalizedTotal(Item dataPoint) {
		// TODO Auto-generated method stub
		double total = 0.0;
		for (int i = 0; i < dataPoint.weights.size(); i++)
			total += dataPoint.weights.get(i);
		for (int i = 0; i < dataPoint.weights.size(); i++)
			dataPoint.weights.set(i, dataPoint.weights.get(i)/total);
	}
	
	/**
	 * calcMSE method of AirPE class
	 * calculate the mean square error (MSE) of the prediction on test data
	 * also calculate the MSE for test data with AQI in different ranges (e.g. 1 to 100, 100 to 200, and etc.)
	 * @param option
	 * the parameter settings of air pollution program
	 * @throws Exception
	 * return error when reading the prediction and true AQI information
	 */
	public void calcMSE(Option option) throws Exception {
		FileWriter fw = new FileWriter(option.ANS_DIR , true);
		int totalLineCount = 0;
		int[] totalCount = new int[5];
		double[] totalError = new double[5];
		
		double[] partitionError = new double[5];
		for (int i = 0; i < option.times; i ++) {
			//fw.write("Fold: " + i + "\n");
			FileReader frPre = new FileReader(option.ANS_DIR + "Predict_" + i);
			BufferedReader brPre = new BufferedReader(frPre);
			FileReader frAns = new FileReader(option.TEST_PATH + i);
			BufferedReader brAns = new BufferedReader(frAns);
			String preLine = "", ansLine = "";
			int lineCount = 0;
			int[] foldCount = new int[5];
			double[] foldError = new double[5];
			while ((preLine = brPre.readLine()) != null) {
				lineCount ++;
				totalLineCount ++;
				ansLine = brAns.readLine();
				String[] preSplit = preLine.split(" ");
				String[] ansSplit = ansLine.split(" ");
				int classNum = getTheClass(Double.parseDouble(ansSplit[0]));
				foldCount[classNum] ++;
				
				double preValue = Double.parseDouble(preSplit[0]), ansValue = Double.parseDouble(ansSplit[0]);
				foldError[classNum] += (preValue - ansValue) * (preValue - ansValue);
			}
			
			int foldCountSum = 0;
			double foldErrorSum = 0;
			for (int j = 0; j < foldCount.length; j ++) {
				if (foldCount[j] == 0)
					fw.write(" 0.0");
				else
					fw.write( " "+String.format("%.5f", foldError[j] / foldCount[j]) );
				
				foldCountSum += foldCount[j];
				foldErrorSum += foldError[j];
				totalCount[j] += foldCount[j];
				totalError[j] += foldError[j];
			}
			if (foldCountSum != lineCount) {
				System.out.println("Error");
				System.exit(-1);
			}
			fw.write(" Ave: " + String.format("%.5f", foldErrorSum / foldCountSum) + "\n");
			partitionError[i] = foldErrorSum / foldCountSum;
		}
		
		int checkTotal = 0;
		for (int i = 0; i < 5; i ++)
			checkTotal += totalCount[i];
		if (checkTotal != totalLineCount) {
			System.err.println("Error");
			System.exit(-1);
		}
		
		fw.write("" + String.format("%d %.5f %.5f %.5f", option.K , option.c , option.alphaS , option.alphaT) + " ");
		for (int i = 0; i < 5; i++)
			fw.write("" + String.format("%.5f", partitionError[i]) + " ");		
		fw.write("\n");
		fw.close();
	}
	
	/**
	  * get the range label of the AQI information 
	  * e.g. 0<=AQI<=99 is 0, 100<=AQI<=199 is 1, 200<=AQI<=299 is 2, AQI>=300 is 3
	  * @param d
	  * the AQI value
	  * @return
	  * the range label of the AQI value 
	  */
	public int getTheClass(double d) {
		if(d >= 300)
			return 3;
		else
			return (int)(d / 100);
	}
	
	
	/**
	 * split the files as training and test data and output them
	 * @param option
	 * parameter setting for the air pollution program
	 * @param city2Items
	 * map from city names to the (day,city) data points
	 * @param fold
	 * the fold of the training and test data (only useful in cross validation)
	 * @param printToFile
	 * a boolean to indicate whether print the training and test file to disk
	 * @param testLowerBound
	 * the lower bound (inclusive) of the id (of cities) for test file
	 * @param testUpperBound
	 * the upper bound (exclusive) of the id (of cities) for test file
	 * @throws Exception
	 */
	public void splitDataset(Option option, HashMap<String, ArrayList<Item>> city2Items, int fold, boolean printToFile, 
							int testLowerBound, int testUpperBound) throws Exception {
		
		ArrayList<String> listCities = shuffleCities(city2Items.keySet());
		
		option.trainCities = new ArrayList<String>();
		option.testCities = new ArrayList<String>();
			
		for (int i = 0; i < listCities.size(); i++)
		if (i>=testLowerBound && i<testUpperBound) {
			option.trainCities.add(listCities.get(i));
		}
		else {
			option.testCities.add(listCities.get(i));
		}
		
		if(printToFile) {
			printDataSet(listCities.subList(0, option.trainNumCities), option.TRAIN_PATH + fold, option.TRAIN_PATH+"Names" + fold, city2Items);
			printDataSet(listCities.subList(option.trainNumCities, listCities.size()), option.TEST_PATH + fold, option.TEST_PATH+"Names" + fold, city2Items);
		}
	}


	/**
	 * print the data points for a set of cities into disk
	 * @param listCities
	 * the set of cities
	 * @param fname
	 * the file name to output the social media and AQI information
	 * @param fnameName
	 * the file name to output the city name
	 * @param city2Items
	 * the map from each city name to the associated data points (the social media and AQI information)
	 * @throws IOException
	 */
	private void printDataSet(List<String> listCities, String fname,
			String fnameName, HashMap<String, ArrayList<Item>> city2Items) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("print to:" + fname);
		
		BufferedWriter foutTrain = new BufferedWriter(new FileWriter(fname));
		BufferedWriter foutNamesTrain = new BufferedWriter(new FileWriter(fnameName));
	
		for (int i = 0; i < listCities.size(); i++) {
			for (Item dataPoint : city2Items.get(listCities.get(i)))
					dataPoint.print(foutTrain, foutNamesTrain);
		}
		
		foutTrain.close();
		foutNamesTrain.close();
	}
}