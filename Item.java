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

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;


public class Item {
	public Item(String cityName, String date2) {
		// TODO Auto-generated constructor stub
		city = cityName;
		date = date2;
		
		indices = new ArrayList<Integer>();
		weights = new ArrayList<Double>();
	}
	String city;
	String date;
	double response;
	
	ArrayList<Integer> indices;
	ArrayList<Double> weights;
	
	/**
	 * print the social media content and the AQI information to the disk
	 * @param foutTrain
	 * file name to output the social media content 
	 * @param foutNames
	 * file name to output the city name, the date and the AQI information
	 */
	public void print(BufferedWriter foutTrain, BufferedWriter foutNames) {
		// TODO Auto-generated method stub
		try {
			foutTrain.write(""+response);
			for(int i = 0; i < indices.size(); i++)
				foutTrain.write( " " + indices.get(i) + ":" + weights.get(i) );
			foutTrain.write("\n");
			
			foutNames.write( city + " " + date + " " + response +  "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
