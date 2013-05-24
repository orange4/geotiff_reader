package test;

import java.applet.Applet;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.imageio.ImageIO;

import tiff.baseline.GrayScaleImage;

public class Main extends Applet{
	
	RandomAccessFile  in;
	RandomAccessFile  out;
	String            in_path,out_path;
	BufferedImage     bin;
	BufferedImage     bout;
	
	public void init(){
		in_path  = "C:\\Documents and Settings\\Administrator\\workspace\\GeoTiff\\src\\test\\input.tif";
		out_path = "C:\\Documents and Settings\\Administrator\\workspace\\GeoTiff\\src\\test\\output.tif";
		
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
		
		try {
			in  = new RandomAccessFile( new File(in_path) , "r" );
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			out = new RandomAccessFile( new File("out_path"), "rw");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		GrayScaleImage gi = new GrayScaleImage();
		try {
			gi.decode( in );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		gi.readPixels( in );
		
		try {
			bin  = ImageIO.read( new File(  in_path  ) );
			bout = ImageIO.read( new File( out_path ) );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void paint(Graphics g){
	}
}
