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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Option {
	
	/**
	 * times to run on random split
	 */
	public int times;
	
	/**
	 * method type value
	 * 		SVR: 0
	 */
	public int type;
		
	// special for SVR, tuned in the parameter "config"
	public double c;

	public String imageFeatureFile;
	/**
	 * Don't need to be specified, determined by the parameter "norm"
	 * 		norm = 0 -> dataDirPath = Global.ORI_DATA_PATH
	 * 		norm = 1 -> dataDirPath = Global.NORM_DATA_PATH
	 */
	public String dataDirPath;
	
	public String ANS_DIR;
	
	public String TRAIN_PATH;
	public String TEST_PATH;
	// the path of file which records the coordinates of cities.
	public String COOR_PATH;
	public String paramStr;
	
	public int trainNumCities;
	
	Random rand;
	
	// for KNN
	public int K;
	public ArrayList<String> trainCities;
	public ArrayList<String> testCities;
	public HashMap<String,Coordinate> city2Coor;

	// for combination of KNN and SVR in MRF
	public double alphaS;
	public double alphaT;

	public String PREDICT_PATH;
}