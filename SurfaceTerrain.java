//************************** Surface Terrain Data Of Mars **************************
// Last Modified: 2/20/00

// The SurfaceTerrain class represents the surface terrain of the virtual Mars.
// It can provide information about elevation and terrain ruggedness at any location
// on the surface of virtual Mars.

import java.awt.*;
import java.io.*;

public class SurfaceTerrain {
	
	// Data members
	
	RandomAccessFile map;    // File containing elevation data for virtual Mars.
	int[] index;             // An cached array for row count indexing of the elevation data.
	long[] sum;              // An cached array to help find rows of the elevation data.
	
	// Constructor
	
	public SurfaceTerrain() {
		try {
			map = new RandomAccessFile("TopoMarsMap.dat", "r");
			
			BufferedInputStream indexBuff = new BufferedInputStream(new FileInputStream("TopoMarsMap.index"));
			DataInputStream indexReader = new DataInputStream(indexBuff);
			index = new int[1440];
			for (int x=0; x < 1440; x++) index[x] = indexReader.readInt();
			indexReader.close();
			indexBuff.close();
			
			BufferedInputStream sumBuff = new BufferedInputStream(new FileInputStream("TopoMarsMap.sum"));
			DataInputStream sumReader = new DataInputStream(sumBuff);
			sum = new long[1440];
			for (int x=0; x < 1440; x++) sum[x] = sumReader.readLong();
			sumReader.close();
			sumBuff.close();
		}
		catch(IOException e) { System.out.println(e.toString()); }
	}
	
	// Destroyer
	
	protected void finalize() throws Throwable {
		
		// Close all IO streams
		
		map.close();
	}
	
	// Returns terrain steepness angle from location by sampling 11.1 km in given direction

	public double determineTerrainDifficulty(Coordinates currentLocation, double currentDirection) {

		double newY = -1.5D * Math.cos(currentDirection);
		double newX = 1.5D * Math.sin(currentDirection);
		Coordinates sampleLocation = currentLocation.convertRectToSpherical(newX, newY);
		double elevationChange = getElevation(sampleLocation) - getElevation(currentLocation);
		double result = Math.atan(elevationChange / 11.1D);
		
		return result;	
	}
	
	// Returns elevation in km at the given location

	public double getElevation(Coordinates location) {

		int red = 0;
		int green = 0;
		int blue = 0;
		double tempPhi = location.getPhi();
		double tempTheta = location.getTheta();

		try {
			int row = (int)Math.round((tempPhi / Math.PI) * 1439D);
			int rowLength = index[row];
			long summer = sum[row];

			tempTheta += Math.PI;
			if (tempTheta >= (2D * Math.PI)) tempTheta -= (2D * Math.PI);
			int col = (int)Math.round((tempTheta / (2D * Math.PI)) * rowLength);

			map.seek((long)((summer + col) * 3));

			red = (int) map.readByte();
			red &= 0x000000FF;
			green = (int) map.readByte();
			green &= 0x000000FF;
			blue = (int) map.readByte();
			blue &= 0x000000FF;
		}
		catch(IOException e) { System.out.println(e.toString()); }

		float[] hsb = new float[3];
		hsb = Color.RGBtoHSB(red, green, blue, null);
		float hue = hsb[0];
		float saturation = hsb[1];

		double elevation = 0D;
		if ((hue < .792F) && (hue > .033F)) elevation = (-13801.99D * hue) + 2500D;
		else elevation = (-21527.78D * saturation) + 19375D + 2500D;
		elevation = elevation / 1000D;

		return elevation;
	}
}

// Mars Simulation Project
// Copyright (C) 1999 Scott Davis
//
// For questions or comments on this project, contact:
//
// Scott Davis
// 1725 W. Timber Ridge Ln. #6206
// Oak Creek, WI  53154
// scud1@execpc.com
// http://www.execpc.com/~scud1/
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA