package tiff.baseline;

public class RGBImage extends GrayScaleImage{
	//	SamplesPerPixel
	//	Tag = 277  (115.H)
	//	Type = SHORT
	//	The number of components per pixel. This number is 3 for RGB images, unless
	//	extra samples are present. See the ExtraSamples field for further information.
	private int samplesPerPixel;
}
