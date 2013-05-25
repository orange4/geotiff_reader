package tiff.baseline;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.RandomAccessFile;

import utils.Types;

public class GrayScaleImage extends BiLevelImage{
	
	//	BitsPerSample
	//	Tag = 258  (102.H)
	//	Type = SHORT
	//	The number of bits per component.
	//	Allowable values for Baseline TIFF grayscale images are 4  and 8 , allowing either
	//	16 or 256 distinct shades of gray.
	
	private int bitsPerSample;
	public boolean decode(RandomAccessFile fstream) throws IOException{
		boolean byteOrder = false;
		int 	version;
		long 	IFDOffset;
		
// Read Header from FileInputStream
		byte[] buffer = new byte[8];
		fstream.read( buffer);

		//read Byte Order
		if(buffer[0]==0x49&&buffer[0]==buffer[1]){
			byteOrder = true;
		}
		else if(buffer[0]==0x4D&&buffer[0]==buffer[1]){
			byteOrder = false;
		}
		
		//read Version
		version  = Types.getShort( buffer, 2, byteOrder);
		
		//read IFD Offset
		IFDOffset = Types.getLong( buffer, 4, byteOrder);
		

// Read IFD Contents
		// count stores no. of fields in directory	
		int fieldCount = 0;
		
		fstream.seek(IFDOffset);
		buffer = new byte[2];
		fstream.read(buffer);
		
		fieldCount = Types.getShort( buffer, 0, byteOrder);
		
		long filePointer = fstream.getFilePointer();
// Read Field Content
		int		     tag,
		        datatype;
		long  valueCount,
		     valueOffset;
		
		for( int index = 0; index < fieldCount; index++){
			buffer = new byte[12];
			
			fstream.seek(filePointer);
			fstream.read(buffer);
			filePointer = fstream.getFilePointer();
			
			tag 		= Types.getShort(buffer, 0, byteOrder);
	    	datatype 	= Types.getShort(buffer, 2, byteOrder);
	    	valueCount 	= Types.getLong (buffer, 4, byteOrder);
	    	valueOffset = Types.getLong	(buffer, 8, byteOrder);
	    
	    	switch( tag ){
	    
	    	case 256:
	    		imageWidth = valueOffset; 
	    		keep.log("Width : "+valueOffset);
	    		break;
	    		
	    	case 257:
	    		imageLength = valueOffset;
	    		keep.log("Length : "+valueOffset);
	    		break;
	    	
	    	case 258:
	    		bitsPerSample = (int) valueOffset;
	    		keep.log("Bits Per Sample : "+valueOffset);
	    		break;
	    	case 259:
	    		compression = (int) valueOffset;
	    		keep.log("Compression : "+valueOffset);
	    		break;
	    	
	    	case 262:
	    		photometricInterpretation = (int) valueOffset;
	    		keep.log("Photometric Interpretation : "+valueOffset);
	    		break;
	    	
	    	case 273:
	    		stripOffsets = new long[ (int) valueCount ];
	    		buffer = new byte[Types.DATATYPE[datatype]];
	    		fstream.seek(valueOffset);
	    		keep.log("Strip Offsets Count :  "+valueCount);
	    	    for(int i = 0; i < valueCount; i++ ){
	    	    	fstream.read(buffer);
	    	    	stripOffsets[i] = (long) Types.getObject(buffer, 0, datatype, byteOrder);
	    	    	keep.log("Offset "+i+" : "+stripOffsets[i]);
	    	    }
	    		break;
	    	case 278:
	    		rowsPerStrip = (long) valueOffset;
	    		keep.log("Rows Per Strip : "+valueOffset);
	    		break;
	    	case 279:
	    		stripByteCounts = new long[ (int) valueCount ];
	    		keep.log("Strip Byte Count : "+valueCount);
	    		
	    		buffer = new byte[Types.DATATYPE[datatype]];
	    		fstream.seek(valueOffset);
	    		
	    	    for(int i = 0; i < valueCount; i++ ){
	    	    	fstream.read(buffer);
	    	    	stripByteCounts[i] = (long) Types.getObject(buffer, 0, datatype, byteOrder);
	    	    	keep.log("Strip Byte Count "+i+" : "+stripByteCounts[i]);
	    	    }
	    		break;
	    	case 282:
	    		buffer = new byte[Types.DATATYPE[datatype]];
	    		fstream.seek(valueOffset);
	    		fstream.read(buffer);
	    		xResolution =  Types.getRational(buffer, 0, byteOrder);
	    		keep.log("X Resolution : "+xResolution);
	    		break;
	    	case 283:
	    		buffer = new byte[Types.DATATYPE[datatype]];
	    		fstream.seek(valueOffset);
	    		fstream.read(buffer);
	    		yResolution =  Types.getRational(buffer, 0, byteOrder);
	    		keep.log("Y Resolution : "+yResolution);
	    		break;
	    	case 296:
	    		resolutionUnit = valueOffset;
	    		keep.log("Resolution Unit : "+valueOffset);
	    		break;
	    	default:
	    		keep.log("Unknown Tag Found : "+tag);
	    	}
		}
		return true;
	}
	public boolean writePixels(RandomAccessFile fstream){
		int row = 0, col = 0, c = 0;
		byte[] buffer = new byte[1];
		try{
		for( int offset = 0; offset < stripByteCounts.length; offset++){
			fstream.seek(stripOffsets[offset]);
			for(int count = 0; count < stripByteCounts[offset];count++,c++){
				if( c == imageLength*imageWidth) break;
				row = (int) (c / imageWidth);
				col = (int) (c % imageWidth);
				buffer = Types.getByteAsBytes( (byte)pixel[row][col], true);
				fstream.write(buffer);
			}
		}
		}catch(Exception e){
			System.out.println("imageWidth : "+imageWidth+" imageLength :"+imageLength);
			System.out.println("row : "+row+" col : "+col+" count : "+c);
			e.printStackTrace();
		}
		return true;
	}
	public boolean readPixels(RandomAccessFile fstream){			
		keep.log("Reading Pixel Data");
		int buffersize = 0;
		
		int row = 0, col = 0,c = 1;
		
		if(buffersize == 0) buffersize = 1; //buffer size is minimum one byte
		pixel = new short[(int) imageLength][(int)imageWidth]; //allocate the 2d pixel array
		try{
		for(int offset = 0; offset < stripOffsets.length; offset++){
			keep.log("Offset : "+offset);
			
			//move file pointer to address of current strip
			fstream.seek((int)stripOffsets[offset]);
			
			byte[] buffer = new byte[buffersize];//create buffer
			
			for(int count = 0 ; count < stripByteCounts[offset] ; count++,c++){
				fstream.read(buffer);
				//if( c % imageWidth == 0) System.out.println();
				if( c == imageLength*imageWidth) break;
				row = (int) (c / imageWidth);
				col = (int) (c % imageWidth);
				pixel [row][col] = (short) (buffer[0] & 0xff);
				//System.out.print(pixel[row][col]+" ");
			}
		}
		}catch(Exception e){
			System.out.println("imageWidth : "+imageWidth+" imageLength :"+imageLength);
			System.out.println("row : "+row+" col : "+col+" count : "+c);
			e.printStackTrace();
		}
		return false;
	}
	public BufferedImage getBufferedImage(){
		BufferedImage bi = new BufferedImage( (int)imageWidth, (int)imageLength, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster wr = bi.getRaster();
		int[] data = new int[3];
		
		for( int row = 0; row < imageLength; row++)
			for( int  col = 0; col < imageWidth; col++ ){
				data[0] = pixel[row][col];
				wr.setPixel(col, row, data);
			}
		return bi;
	}
	public void filter( double[][] mask, int size){
		short[][] output = new short[(int) imageLength][(int) imageWidth];
		int offset = (size-1)/2;
		for( int row = offset; row < imageLength - ( offset  ); row++){
			//System.out.println();
			for( int  col = offset; col < imageWidth - ( offset ); col++ ){
				//System.out.println("Column : "+col);
				//System.out.println("Row : "+pixel[row  ][col  ]);
				 
				double value =  (
			 					( pixel[row-1][col-1] * mask[0][0] ) +
			 					( pixel[row-1][col  ] * mask[0][1] ) +
			 					( pixel[row-1][col+1] * mask[0][2] ) +
			 					( pixel[row	 ][col-1] * mask[1][0] ) +
			 					( pixel[row  ][col  ] * mask[1][1] ) +
			 					( pixel[row  ][col+1] * mask[1][2] ) +
			 					( pixel[row+1][col-1] * mask[2][0] ) +
			 					( pixel[row+1][col  ] * mask[2][1] ) +
			 					( pixel[row+1][col+1] * mask[2][2] ) 
			 					);
				//System.out.print(value);
				output[row][col] = (short) value;
				//System.out.print(" <<"+output[row][col]+" ");
			}
		}
		pixel = output;
	}
}
