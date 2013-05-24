package tiff.baseline;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import tiff.BaseLineImage;
import utils.LogCat;
import utils.Types;

public class BiLevelImage extends BaseLineImage{
	LogCat keep = new LogCat();
	//Pixel Data
	short[][] pixel;
	//Fields Required in BiLevel Image are as Follows 
	
	//Color
		//	PhotometricInterpretation
		//	Tag = 262  (106.H)
		//	Type = SHORT
		protected int photometricInterpretation;

	//Compression
		//	Compression
		//	Tag = 259  (103.H)
		//	Type = SHORT
		protected int compression;
		
	//Rows and Columns
		//	ImageLength
		//	Tag = 257  (101.H)
		//	Type = SHORT or LONG
		//	The number of rows (sometimes described as  scanlines ) in the image.
		//	ImageWidth
		//	Tag = 256  (100.H)
		//	Type = SHORT or LONG
		//	The number of columns in the image, i.e., the number of pixels per scanline.
		protected long imageLength,imageWidth;

	//Physical Dimensions
		//	ResolutionUnit
		//	Tag = 296 (128.H)
		//	Type = SHORT
		protected long resolutionUnit;
		
		//	XResolution
		//	Tag = 282  (11A.H)
		//	Type = RATIONAL
		//	YResolution
		//	Tag = 283  (11B.H)
		//	Type = RATIONAL
		protected double xResolution;
		protected double yResolution;
		
	//Location of Data
		//	RowsPerStrip
		//	Tag = 278  (116.H)
		//	Type = SHORT or LONG
		//	The number of rows in each strip (except possibly the last strip.)
		protected long rowsPerStrip;
		
		//	StripOffsets
		//	Tag = 273  (111.H)
		//	Type = SHORT or LONG
		//	For each strip, the byte offset of that strip.
		protected long stripOffsets[];
		
		//	StripByteCounts
		//	Tag = 279  (117.H)
		//	Type = SHORT or LONG
		//	For each strip, the number of bytes in that strip after any compression .
		protected long stripByteCounts[];	
		
		//	decode Method Read Tiff Image from File
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
		public boolean readPixels(RandomAccessFile fstream) throws IOException{			
			keep.log("Reading Pixel Data");
			int buffersize = 0;
			
			int row = 0, col = 0;
			
			if(buffersize == 0) buffersize = 1; //buffer size is minimum one byte
			pixel = new short[(int) imageWidth][(int)imageLength]; //allocate the 2d pixel array
			
			for(int offset = 0; offset < stripOffsets.length; offset++){
				keep.log("Offset : "+offset);
				
				//move file pointer to address of current strip
				fstream.seek((int)stripOffsets[offset]);
				
				byte[] buffer = new byte[buffersize];//create buffer
				
				int count_max = (int) stripByteCounts[offset];//how much bytes we have to read from above offset i.e size of strip
				
				if(offset+1 < stripOffsets.length){
					count_max = (int) (imageLength % rowsPerStrip);
				}
				for(int count = 0 ; count < count_max ; count++){
					fstream.read(buffer);
					row = (int) (count / imageLength);
					col = (int) (count % imageLength);
					pixel [row][col] = (short) (buffer[0] & 0xff);
					System.out.print(pixel[row][col]+" ");
				}
				System.out.println();
			}
			return false;
		}
		//	encode Method Write Tiff Image to File
		public boolean encode(File output){
			return false;
		}
			
}
