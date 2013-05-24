package tiff.baseline;

public class PaletteColorImage extends GrayScaleImage{
	//	ColorMap
	//	Tag = 320 (140.H)
	//	Type = SHORT
	//	N = 3 * (2**BitsPerSample)
	//	This field defines a Red-Green-Blue color map (often called a lookup table) for
	//	palette color images. In a palette-color image, a pixel value is used to index into an
	//	RGB-lookup table. For example, a palette-color pixel having a value of 0 would
	//	be displayed according to the 0th Red, Green, Blue triplet.
	//	In a TIFF ColorMap, all the Red values come first, followed by the Green values,
	//	then the Blue values. In the ColorMap, black is represented by 0,0,0 and white is
	//	represented by 65535, 65535, 65535.
	private int colorMap;
}
