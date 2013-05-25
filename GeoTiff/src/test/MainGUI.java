package test;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Polygon;
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

import tiff.baseline.GrayScaleImage;

public class MainGUI{
	static RandomAccessFile  in;
	static String            in_path;
	static BufferedImage     bin;
	static JFrame jf;
	static TiffCanvas canvas;
	static final JFileChooser fc  = new JFileChooser();
	static Polygon selectedBoundry;
	static boolean select;
	
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
		canvas.drawImage( bin );
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
					selectedBoundry.addPoint( me.getX() , me.getY());
				}
			}

			@Override
			public void mouseReleased(MouseEvent me) {
				// TODO Auto-generated method stub
				if(select)	canvas.getGraphics().drawPolygon( selectedBoundry );
				select = false;
				//canvas.repaint();
			}
			
		});
		canvas.addMouseMotionListener(new MouseMotionListener(){

			@Override
			public void mouseDragged(MouseEvent me) {
				// TODO Auto-generated method stub
				if(select) selectedBoundry.addPoint( me.getX() , me.getY());
			}

			@Override
			public void mouseMoved(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
		jf.add("Center" , canvas);
		
		jp.setLayout(new GridLayout(1,2));
		
		JButton openBtn = new JButton("OPEN");
		openBtn.setSize( 20, 40);
		openBtn.addActionListener( new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent ae) {
				//openImage( "src/test/input.tif" );
				int val = fc.showOpenDialog( jf );
				if( val == JFileChooser.APPROVE_OPTION ){
					openImage ( fc.getSelectedFile().getAbsolutePath() );
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
					canvas.repaint( selectedBoundry.getBounds().x,
							selectedBoundry.getBounds().y,
							selectedBoundry.getBounds().width+1,
							selectedBoundry.getBounds().height+1);
					selectedBoundry = null;
				}
			}
		});
		selectBtn.setSize( 20, 40);
		jp.add(openBtn);
		jp.add(selectBtn);
		
		jf.add(BorderLayout.NORTH, jp);
		jf.setSize( new Dimension( 200, 60 ));
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

