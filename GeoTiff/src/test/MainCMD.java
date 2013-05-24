package test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import tiff.baseline.GrayScaleImage;

public class MainCMD {
	public static void main(String args[]) throws IOException{
		
		double x = 1.0/9, y = 1.0,z = -1.0;
		//This is Image Smoothing mask
		double[][] lowpass =  { 
								{ x, x, x}
							   ,{ x, x, x}
							   ,{ x, x, x}
							  };
		
		//This is Laplacian mask
		double[][] highpass = { 
								{ y,   y, y}
							   ,{ y, z*8, y}
							   ,{ y,   y, y}
							  };
		
		RandomAccessFile  in  = new RandomAccessFile( new File( "src/test/ceain.TIF") , "r" );
		RandomAccessFile  out = new RandomAccessFile( new File("src/test/ceaout.TIF"), "rw");
		
		GrayScaleImage gi = new GrayScaleImage();
		gi.decode( in );
		gi.readPixels( in );
		
		gi.filter(lowpass, 3);
		gi.filter(lowpass, 3);
		
		gi.filter(highpass, 3);
		
		gi.writePixels( out );
	}
}
