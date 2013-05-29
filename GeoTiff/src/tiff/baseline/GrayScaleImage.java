package tiff.baseline;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
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
	public GrayScaleImage(){}
	public GrayScaleImage(File input) throws FileNotFoundException, IOException {
		RandomAccessFile fstream = new RandomAccessFile( input, "rw" );
		decode( fstream );
		image = new BufferedImage(	(int) imageWidth,
									(int) imageLength,
									 BufferedImage.TYPE_BYTE_GRAY
				);
		if( stripOffsets != null ) readStrips( fstream );
		if( tileOffsets	 != null ) readTiles ( fstream );
	}
	protected int[] bitsPerSample;
	
	//	TileWidth
	//	Tag = 322  (142.H)
	//	Type = SHORT or LONG
	//	N= 1
	//	The tile width in pixels.  This is the number of columns in each tile.
	//	Assuming integer arithmetic, three computed values that are useful in the follow-ing field descriptions are:
	//	TilesAcross = (ImageWidth + TileWidth - 1) / TileWidth
	//	TilesDown = (ImageLength + TileLength - 1) / TileLength
	//	TilesPerImage = TilesAcross * TilesDown
	
	//	TileLength
	//	Tag = 323  (143.H)
	//	Type = SHORT or LONG
	//	N= 1
	//	The tile length (height) in pixels. This is the number of rows in each tile.
	
	protected long tileWidth;

	protected long tileLength;
	
	//	TileOffsets
	//	Tag = 324  (144.H)
	//	Type = LONG
	//	N = TilesPerImage for PlanarConfiguration = 1
	//	= SamplesPerPixel * TilesPerImage for PlanarConfiguration = 2
	protected long[] tileOffsets;
	
	//	TileByteCounts
	//	Tag = 325  (145.H)
	//	Type = SHORT or LONG
	//	N = TilesPerImage for PlanarConfiguration = 1
	//	= SamplesPerPixel * TilesPerImage for PlanarConfiguration = 2
	protected long[] tileByteCounts;
	
	
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
		int		tag;
		int		datatype;
		long	valueCount;
		long	valueOffset;
		boolean isValue = false; 
		int start = 0;
		for( int index = 0; index < fieldCount; index++){
			buffer = new byte[12];
			
			fstream.seek(filePointer);
			fstream.read(buffer);
			filePointer = fstream.getFilePointer();
			
			tag 		= Types.getShort(buffer, 0, byteOrder);
	    	datatype 	= Types.getShort(buffer, 2, byteOrder);
	    	valueCount 	= Types.getLong (buffer, 4, byteOrder);
	    	valueOffset = Types.getLong	(buffer, 8, byteOrder);
	    	
	    	if( ( valueCount * Types.DATATYPE[datatype] ) > 4){
	    		fstream.seek( valueOffset );
	    		buffer = new byte[(int) (valueCount * Types.DATATYPE[datatype])];
	    		fstream.read( buffer );
	    		start	= 0;
	    		isValue = false;
	    		
	    	}else{
	    		isValue = true;
	    	}
	    	
	    	switch( tag ){
	    
	    	case 256:
	    		imageWidth = valueOffset; 
	    		log.append("Width : "+valueOffset);
	    		break;
	    		
	    	case 257:
	    		imageLength = valueOffset;
	    		log.append("Length : "+valueOffset);
	    		break;
	    	
	    	case 258:
	    		log.append( "IsValue : "+isValue);
	    		log.append( "Count : "+valueCount);
	    		log.append( "Datatype : "+datatype);
	    		bitsPerSample =new int[3];
	    		if( !isValue ){
	    			bitsPerSample[0] = (int) Types.getObject(buffer, 0, datatype, byteOrder);
	    		}
	    		else bitsPerSample[0] = (int) valueOffset;
//	    		bitsPerSample = ( Types.getObject(buffer, datatype, 8, byteOrder) ) ;
	    		log.append("Bits Per Sample : "+bitsPerSample);
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
	    		log.append("Strip Offsets Count :  "+valueCount);
	    	    for(int i = 0; i < valueCount; i++ ){
	    	    	stripOffsets[i] = (long) Types.getObject(buffer, start, datatype, byteOrder);
	    	    	log.append("Offset "+i+" : "+stripOffsets[i]);
	    	    	start += Types.DATATYPE[datatype];
	    	    }
	    		break;
	    	case 278:
	    		rowsPerStrip = (long) valueOffset;
	    		log.append("Rows Per Strip : "+valueOffset);
	    		break;
	    	case 279:
	    		stripByteCounts = new long[ (int) valueCount ];
	    		log.append("Strip Byte Count : "+valueCount);	    		
	    	    for(int i = 0; i < valueCount; i++ ){
	    	    	stripByteCounts[i] = (long) Types.getObject(buffer, start, datatype, byteOrder);
	    	    	log.append("Strip Byte Count "+i+" : "+stripByteCounts[i]);
	    	    	start += Types.DATATYPE[datatype];
	    	    }
	    		break;
	    	case 282:
	    		xResolution =  Types.getRational(buffer, 0, byteOrder);
	    		log.append("X Resolution : "+xResolution);
	    		break;
	    	case 283:
	    		yResolution =  Types.getRational(buffer, 0, byteOrder);
	    		log.append("Y Resolution : "+yResolution);
	    		break;
	    	case 296:
	    		resolutionUnit = valueOffset;
	    		log.append("Resolution Unit : "+valueOffset);
	    		break;
	    	case 322:
	    		tileWidth = valueOffset;
	    		log.append("Tile Width : "+tileWidth);
	    		break;
	    	case 323:
	    		tileLength = valueOffset;
	    		log.append("Tile Length : "+tileLength);
	    		break;
	    	case 324:
	    		tileOffsets = new long[ (int) valueCount ];
	    	    for(int i = 0; i < valueCount; i++ ){
	    	    	tileOffsets[i] = (long) Types.getObject(buffer, start , datatype, byteOrder);
	    	    	log.append("Offset "+i+" : "+tileOffsets[i]);
	    	    	start += Types.DATATYPE[datatype];
	    	    }
	    	    break;
	    	case 325:
	    		tileByteCounts = new long[ (int) valueCount ];	    			    		
	    	    for(int i = 0; i < valueCount; i++ ){
	    	    	tileByteCounts[i] = (long) Types.getObject(buffer, start, datatype, byteOrder);
	    	    	log.append("tile Byte Count "+i+" : "+tileByteCounts[i]);
	    	    	start += Types.DATATYPE[datatype];
	    	    }
	    	    break;
	    	default:
	    		log.append("Unknown Tag Found : "+tag+" ValueOFFset : "+valueOffset);
	    		break;
	    	}
		}
		return true;
	}
	public boolean writePixels(RandomAccessFile fstream){
		int row = 0, col = 0, c = 0;
		WritableRaster wr = image.getRaster();
		byte[] buffer = new byte[1];
		try{
		for( int offset = 0; offset < stripByteCounts.length; offset++){
			fstream.seek(stripOffsets[offset]);
			for(int count = 0; count < stripByteCounts[offset];count++,c++){
				if( c == imageLength*imageWidth) break;
				row = (int) (c / imageWidth);
				col = (int) (c % imageWidth);
				buffer = Types.getByteAsBytes( (byte)wr.getSample(row, col, 0), true);
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
	public boolean readStrips(RandomAccessFile fstream){			
		log.append("Reading Pixel Data From Strips");
		int buffersize = 1;
		int row = 0, col = 0,c = 1;
//		short[][] pixel = new short[(int) imageLength][(int)imageWidth]; //allocate the 2d pixel array
		WritableRaster wr = image.getRaster();
		try{
		for(int offset = 0; offset < stripOffsets.length; offset++){
			log.append("Offset : "+offset);
			
			//move file pointer to address of current strip
			fstream.seek((int)stripOffsets[offset]);
			
			byte[] buffer = new byte[buffersize];//create buffer
			
			for(int count = 0 ; count < stripByteCounts[offset] ; count++,c++){
				fstream.read(buffer);
				//if( c % imageWidth == 0) System.out.println();
				if( c == imageLength*imageWidth) break;
				row = (int) (c / imageWidth);
				col = (int) (c % imageWidth);
				wr.setSample( col, row, 0,(buffer[0] & 0xff));
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
	public boolean readTiles( RandomAccessFile fstream){
		
		log.append( "Reading Pixels Data From Tiles " );
		boolean flag1 = true;
		boolean flag2 = true;
		boolean flag3 = true;
		long	tilesAcross;
		long	tilesDown;
		long	tilesPerImage;
		long	pixelsPerTile;
		short[][] pixel = new short[(int) imageLength][(int)imageWidth];
		WritableRaster wr = image.getRaster();
		
		tilesAcross 	= (imageWidth  + tileWidth  - 1 ) / tileWidth ;
		tilesDown 		= (imageLength + tileLength - 1 ) / tileLength;
		tilesPerImage 	=  tilesAcross * tilesDown;
		pixelsPerTile	=  tileWidth   * tileLength;
		
		log.append( "TA"+tilesAcross );
		log.append( "TD"+tilesDown );
		log.append( "TPI"+tilesPerImage );
		log.append( "PPT"+pixelsPerTile );
		
		long	offset = 0;
		int		ROW = 0;
		int		COL = 0;
		try{
		byte[] buffer = new byte[(int) pixelsPerTile];
		
		for( long row = 0; row < tilesDown && flag1 ; row++ ){
			for( long col = 0; col < tilesAcross && flag1 ; col++ ){
				fstream.seek( tileOffsets[ (int) offset ]);
				fstream.read( buffer );
//				flag2 = true;
//				flag3 = true;
				for( int y = 0; y < tileLength ; y++ ){
					for( int x = 0; x < tileWidth; x++){
						COL = (int) (x + (col * tileWidth ));
						ROW = (int) (y + (row * tileLength));
						if( COL >= imageWidth ){
							continue;
						}
						if( ROW >= imageLength ){
							continue;
						}
						wr.setSample( COL, ROW, 0, buffer[  (int) ( ( x * tileWidth) + y )]);
//						pixel[ROW][COL] = buffer[ (int) (( x * tileWidth) + y)]; 
//						System.out.println ( buffer[  x + ( tileLength * y)] );
					}
				}
				offset++;
			}
		}
		}catch(Exception e){
			log.append("Pixels Per Tile : "+pixelsPerTile);
			System.out.println("tileWidth : "+tileWidth+" tileLength :"+tileLength);
			System.out.println("row : "+ROW+" col : "+COL+" count : "+offset);
			e.printStackTrace();
		}
		
		return false;
	}
//	public void filter( double[][] mask, int size){
//		short[][] output = new short[(int) imageLength][(int) imageWidth];
//		int offset = (size-1)/2;
//		for( int row = offset; row < imageLength - ( offset  ); row++){
//			//System.out.println();
//			for( int  col = offset; col < imageWidth - ( offset ); col++ ){
//				//System.out.println("Column : "+col);
//				//System.out.println("Row : "+pixel[row  ][col  ]);
//				 
//				double value =  (
//			 					( pixel[row-1][col-1] * mask[0][0] ) +
//			 					( pixel[row-1][col  ] * mask[0][1] ) +
//			 					( pixel[row-1][col+1] * mask[0][2] ) +
//			 					( pixel[row	 ][col-1] * mask[1][0] ) +
//			 					( pixel[row  ][col  ] * mask[1][1] ) +
//			 					( pixel[row  ][col+1] * mask[1][2] ) +
//			 					( pixel[row+1][col-1] * mask[2][0] ) +
//			 					( pixel[row+1][col  ] * mask[2][1] ) +
//			 					( pixel[row+1][col+1] * mask[2][2] ) 
//			 					);
//				//System.out.print(value);
//				output[row][col] = (short) value;
//				//System.out.print(" <<"+output[row][col]+" ");
//			}
//		}
//		pixel = output;
//	}
}
