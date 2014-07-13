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


public class Coordinate {
	public double latitude;
	public double longitude;
	
	Coordinate(double x , double y)
	{
		latitude = x;
		longitude = y;
	}
	
	double distance(Coordinate a)
	{
		return Math.sqrt(Math.pow(latitude-a.latitude, 2.0)
				 + Math.pow(longitude-a.longitude , 2.0));
	}
}
