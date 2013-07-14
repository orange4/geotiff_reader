package tiff.baseline;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import tiff.BaseLineImage;
import utils.LogCat;
import utils.Types;

public class BiLevelImage extends BaseLineImage{
	LogCat log = new LogCat();
	BiLevelImage(){}
	BiLevelImage(File input) throws FileNotFoundException, IOException{
		RandomAccessFile fstream = new RandomAccessFile( input, "rw" );
		decode( fstream );
		
		image = new BufferedImage( 	BufferedImage.TYPE_BYTE_BINARY,
				(int) imageLength,
				(int) imageWidth,
				null
				);
		
		readPixels( fstream );
	}
	public BufferedImage getImage() {
		return image;
	}
	//Pixel Data
	BufferedImage image;
	
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
		
	//	decode Method Reads Tiff Image from File
	public boolean decode(RandomAccessFile fstream) throws IOException{
			boolean	byteOrder  = false;
			int		version;
			long	IFDOffset;
			byte[]	buffer = new byte[8];
			int		fieldCount = 0; // count stores no. of fields in directory
			
			fstream.read( buffer);	// Read Header from FileInputStream

			//read Byte Order
			if(buffer[0]==0x49&&buffer[0]==buffer[1]){
				byteOrder = true;
			}
			else if(buffer[0]==0x4D&&buffer[0]==buffer[1]){
				byteOrder = false;
			}
			
			//read Version
			version   = Types.getShort( buffer, 2, byteOrder);
			
			//read IFD Offset
			IFDOffset = Types.getLong( buffer, 4, byteOrder);
			

	// Read IFD Contents		
			
			fstream.seek(IFDOffset); // Move File Pointer to Address of first IFD
			
			buffer = new byte[2];
			fstream.read(buffer);	// Read the count of fields in IFD
			
			fieldCount = Types.getShort( buffer, 0, byteOrder); // decode the buffer
			
			long filePointer = fstream.getFilePointer(); // Store the File Pointer
	// Read Field Content
			
			int		tag;
			int		datatype;
			long	valueCount;
			long	valueOffset;
			
			for( int index = 0; index < fieldCount; index++){
				buffer = new byte[12];
				
				fstream.seek(filePointer); // seek filePointer to current Field
				fstream.read(buffer);		// Read the Field
				filePointer = fstream.getFilePointer(); // Store the FilePointer
				
				tag 		= Types.getShort(buffer, 0, byteOrder);//Read Tag
		    	datatype 	= Types.getShort(buffer, 2, byteOrder);//Read Datatype
		    	valueCount 	= Types.getLong (buffer, 4, byteOrder);//Read Count
		    	valueOffset = Types.getLong	(buffer, 8, byteOrder);//Read ValueOffset
		    
		    	
		    	switch( tag ){ // store the corresponding field of BiLevelImage depending on tag
		    
		    	case 256:
		    		imageWidth = valueOffset; 
		    		log.append("Width : "+valueOffset);
		    		break;
		    		
		    	case 257:
		    		imageLength = valueOffset;
		    		log.append("Length : "+valueOffset);
		    		break;
		    	
		    	case 259:
		    		compression = (int) valueOffset;
		    		log.append("Compression : "+valueOffset);
		    		break;
		    	
		    	case 262:
		    		photometricInterpretation = (int) valueOffset;
		    		log.append("Photometric Interpretation : "+valueOffset);
		    		break;
		    	
		    	case 273:
		    		stripOffsets = new long[ (int) valueCount ];
		    		buffer = new byte[Types.DATATYPE[datatype]];
		    		fstream.seek(valueOffset);
		    		log.append("Strip Offsets Count :  "+valueCount);
		    	    for(int i = 0; i < valueCount; i++ ){
		    	    	fstream.read(buffer);
		    	    	stripOffsets[i] = (long) Types.getObject(buffer, 0, datatype, byteOrder);
		    	    	log.append("Offset "+i+" : "+stripOffsets[i]);
		    	    }
		    		break;
		    	case 278:
		    		rowsPerStrip = (long) valueOffset;
		    		log.append("Rows Per Strip : "+valueOffset);
		    		break;
		    	case 279:
		    		stripByteCounts = new long[ (int) valueCount ];
		    		log.append("Strip Byte Count : "+valueCount);
		    		
		    		buffer = new byte[Types.DATATYPE[datatype]];
		    		fstream.seek(valueOffset);
		    		
		    	    for(int i = 0; i < valueCount; i++ ){
		    	    	fstream.read(buffer);
		    	    	stripByteCounts[i] = (long) Types.getObject(buffer, 0, datatype, byteOrder);
		    	    	log.append("Strip Byte Count "+i+" : "+stripByteCounts[i]);
		    	    }
		    		break;
		    	case 282:
		    		buffer = new byte[Types.DATATYPE[datatype]];
		    		fstream.seek(valueOffset);
		    		fstream.read(buffer);
		    		xResolution =  Types.getRational(buffer, 0, byteOrder);
		    		log.append("X Resolution : "+xResolution);
		    		break;
		    	case 283:
		    		buffer = new byte[Types.DATATYPE[datatype]];
		    		fstream.seek(valueOffset);
		    		fstream.read(buffer);
		    		yResolution =  Types.getRational(buffer, 0, byteOrder);
		    		log.append("Y Resolution : "+yResolution);
		    		break;
		    	case 296:
		    		resolutionUnit = valueOffset;
		    		log.append("Resolution Unit : "+valueOffset);
		    		break;
		    	default:
		    		log.append("Unknown Tag Found : "+tag);
		    	}
			}
			return true;
		}
		public boolean readPixels(RandomAccessFile fstream) throws IOException{			
			log.append("Reading Pixel Data");
			int buffersize = 1;//buffer size is minimum one byte
			int row = 0;
			int	col = 0;
//			short[][]	pixel = new short[(int) imageWidth][(int)imageLength]; //allocate the 2d pixel array; 
			WritableRaster wr = image.getRaster();
			
			for(int offset = 0; offset < stripOffsets.length; offset++){
				log.append("Offset : "+offset);
				
				//move file pointer to address of current strip
				fstream.seek((int)stripOffsets[offset]);
				
				byte[] buffer = new byte[buffersize];//create buffer
				
				int count_max = (int) stripByteCounts[offset];//how much bytes we have to read from above offset i.e size of strip
				
				if(offset+1 < stripOffsets.length){
					count_max = (int) (imageLength % rowsPerStrip);//if last strip
				}
				
				for(int count = 0 ; count < count_max ; count++){
					fstream.read(buffer);	// Read Each Pixel Data From file into Buffer
					row = (int) (count / imageWidth); // compute the x co-ordinate of pixel
					col = (int) (count % imageWidth); // compute the y co-ordinate of pixel
					wr.setSample(col, row, 0, (buffer[0] & 0xff)); //set the Sample/Pixel as read Value
//					pixel [row][col] = (short) (buffer[0] & 0xff);
//					log.append(pixel[row][col]+" ");
				}
				log.append("");
			}
			return false;
		}
		//	encode Method Write Tiff Image to File
		public boolean encode(File output){
			return false;
		}
		public Rectangle getBounds(){
			log.append(imageLength+" "+imageWidth);
			return new Rectangle((int)imageLength,(int)imageWidth);
		}
			
}
