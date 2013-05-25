package test;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import tiff.baseline.GrayScaleImage;

public class MainGUI{
	static RandomAccessFile  in;
	static String            in_path;
	static BufferedImage     bin;
	public static JFrame jf;
	public static void openImage(String path){
		in_path = path;
		try {
			in  = new RandomAccessFile( new File(in_path) , "r" );
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
		
		bin  = gi.getBufferedImage();
		jf.add( "Center", new TiffCanvas( bin ));
		jf.setSize( new Dimension( bin.getWidth(),bin.getHeight()+ 20 ) );
	}
	static class TiffCanvas extends Canvas
	{
		BufferedImage bi;
		TiffCanvas(BufferedImage bi){
			this.bi = bi;  
		}
	    public void paint(Graphics g)
	    {
	        g.drawImage( bi, 0, 0, null);
	    }
	}

	public static void main(String args[]){
		jf = new JFrame("GeoTIFF GUI");
		JPanel jp = new JPanel();
		JButton openBtn = new JButton("OPEN");
		openBtn.setSize( 20, 40);
		openBtn.addActionListener( new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent ae) {
				openImage( "src/test/input.tif" );
			}
		});
		JButton selectBtn = new JButton("SELECT");
		selectBtn.setSize( 20, 40);
		jp.setLayout(new GridLayout(1,2));
		jp.add(openBtn);
		jp.add(selectBtn);
		
		jf.add(BorderLayout.NORTH, jp);
		jf.setSize( new Dimension( 200, 60 ));
		jf.setVisible( true );
	}
}

