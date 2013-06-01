package test;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

import tiff.BaseLineImage;
import tiff.baseline.GrayScaleImage;
import tiff.baseline.RGBImage;
import utils.LogCat;

public class MainGUI{
	static LogCat log = new LogCat();
	static File  in;
	static String            in_path;
	static BufferedImage     bin;
	static BaseLineImage img;
	static JFrame jf;
	static TiffCanvas canvas;
	static final JFileChooser fc  = new JFileChooser();
	static Object selectedBoundry;
	static boolean select;
	static double[][] highpass = { 
			{ 1,  1, 1}
		   ,{ 1, -8, 1}
		   ,{ 1,  1, 1}
		  };
	static double[][] lowpass = { 
		{ 1.0/9,  1.0/9, 1.0/9}
	   ,{ 1.0/9,  1.0/9, 1.0/9}
	   ,{ 1.0/9,  1.0/9, 1.0/9}
	  };
	static double[][] identity = { 
			{ 0,  0, 0}
		   ,{ 0,  1, 0}
		   ,{ 0,  0, 0}
		};
	public static void openImage(String path) throws FileNotFoundException, IOException{
		in_path = path;
		in  = new File(in_path);		
//		GrayScaleImage gi = new GrayScaleImage( in );//to open GrayScale Image
		RGBImage gi = new RGBImage( in ) ; // top open RGBImage 
		img = gi;
		bin  = gi.getImage();
		canvas.drawImage( bin );
		selectedBoundry = new Rectangle(0, 0, bin.getWidth(),bin.getHeight()); 
		jf.setSize( new Dimension( bin.getWidth(),bin.getHeight()+ 20 ) );
	}
	public static void main(String args[]){
		jf = new JFrame("GeoTIFF GUI");
		JPanel jp = new JPanel();
		canvas = new TiffCanvas();
		canvas.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent me) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseEntered(MouseEvent me) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent me) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent me) {
				// TODO Auto-generated method stub
				if(select){		
					selectedBoundry = new Polygon();
					((Polygon) selectedBoundry).addPoint( me.getX() , me.getY());
				}
			}

			@Override
			public void mouseReleased(MouseEvent me) {
				// TODO Auto-generated method stub
				if(select)	canvas.getGraphics().drawPolygon( (Polygon) selectedBoundry );
				select = false;
				//canvas.repaint();
			}
			
		});
		canvas.addMouseMotionListener(new MouseMotionListener(){

			@Override
			public void mouseDragged(MouseEvent me) {
				// TODO Auto-generated method stub
				if(select) ((Polygon) selectedBoundry).addPoint( me.getX() , me.getY());
			}

			@Override
			public void mouseMoved(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
		jf.add("Center" , canvas);
		
		jp.setLayout(new GridLayout(1,5));
		
		JButton openBtn = new JButton("OPEN");
		openBtn.setSize( 20, 40);
		openBtn.addActionListener( new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent ae) {
				//openImage( "src/test/input.tif" );
				int val = fc.showOpenDialog( jf );
				if( val == JFileChooser.APPROVE_OPTION ){
					try {
						openImage ( fc.getSelectedFile().getAbsolutePath() );
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else{
					
				}
			}
		});
		
		JButton selectBtn = new JButton("SELECT");
		selectBtn.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent ae) {
				// TODO Auto-generated method stub
				
				select = true;
				
				if( selectedBoundry != null ){
					if( selectedBoundry.getClass() == Polygon.class){
						canvas.repaint(
							((Polygon)selectedBoundry).getBounds().x,
							((Polygon)selectedBoundry).getBounds().y,
							((Polygon)selectedBoundry).getBounds().width+1,
							((Polygon)selectedBoundry).getBounds().height+1
							);
					}
					if( selectedBoundry.getClass() == Rectangle.class ){
						canvas.repaint( 
							((Rectangle)selectedBoundry).getBounds().x,
							((Rectangle)selectedBoundry).getBounds().y,
							((Rectangle)selectedBoundry).getBounds().width+1,
							((Rectangle)selectedBoundry).getBounds().height+1
							);
					}
					selectedBoundry = new Rectangle( 0, 0, bin.getWidth(),bin.getHeight());
				}
			}
		});
		selectBtn.setSize( 20, 40);
		
		JButton highpassBtn = new JButton("HighPass");
		highpassBtn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				log.append( ""+img.getClass() );
				if( img.getClass() == RGBImage.class ){
					RGBImage rgbImage = (RGBImage)img;
					if( selectedBoundry.getClass() == Rectangle.class ){
						rgbImage.filter( (Rectangle) selectedBoundry, highpass, false);
					}
					if( selectedBoundry.getClass() == Polygon.class ){
						rgbImage.filter( (Polygon) selectedBoundry, highpass, false);
					}
					canvas.repaint();
				}else if( img.getClass() == GrayScaleImage.class){
					GrayScaleImage grayImage = (GrayScaleImage)img;
					if( selectedBoundry.getClass() == Rectangle.class ){
						grayImage.filter( (Rectangle) selectedBoundry, highpass, false);
					}
					if( selectedBoundry.getClass() == Polygon.class ){
						grayImage.filter( (Polygon) selectedBoundry, highpass, false);
					}
					canvas.repaint();
				}
			}
		});
		highpassBtn.setSize( 20, 40);
		
		JButton identityBtn = new JButton("Identity");
		identityBtn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				log.append( ""+img.getClass() );
				if( img.getClass() == RGBImage.class ){
					RGBImage rgbImage = (RGBImage)img;
					rgbImage.filter( (Rectangle) selectedBoundry,identity, false);
					canvas.repaint();
				}else if( img.getClass() == GrayScaleImage.class){
					GrayScaleImage grayImage = (GrayScaleImage)img;
					grayImage.filter( (Rectangle) selectedBoundry,identity, false);
					canvas.repaint();
				}
			}
		});
		identityBtn.setSize( 20, 40);
		
		JButton lowpassBtn = new JButton("LowPass");
		lowpassBtn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				log.append( ""+img.getClass() );
				if( img.getClass() == RGBImage.class ){
					RGBImage rgbImage = (RGBImage)img;
					rgbImage.filter( (Rectangle) selectedBoundry,lowpass, false);
					canvas.repaint();
				}else if( img.getClass() == GrayScaleImage.class){
					GrayScaleImage grayImage = (GrayScaleImage)img;
					grayImage.filter( (Rectangle) selectedBoundry,lowpass, false);
					canvas.repaint();
				}
			}
		});
		lowpassBtn.setSize( 20, 40);
		
		jp.add(openBtn);
		jp.add(selectBtn);
		jp.add(identityBtn);
		jp.add(highpassBtn);
		jp.add(lowpassBtn);
		
		jf.add(BorderLayout.NORTH, jp);
		jf.setSize( new Dimension( 200, 200 ));
		jf.setVisible( true );
	}
}
class TiffCanvas extends Canvas
{
	BufferedImage bi;
	public void drawImage( BufferedImage bi ){
		this.bi = bi;
		this.repaint();
	}
    public void paint(Graphics g)
    {
        g.drawImage( bi, 0, 0, null);
    }
}

