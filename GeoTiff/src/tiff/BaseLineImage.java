package tiff;

public class BaseLineImage{
	/**
	 PlanarConfiguration
		How the components of each pixel are stored.
			Tag = 284  (11C.H)
			Type = SHORT
			N= 1
			
		1 =  Chunky format. The component values for each pixel are stored contiguously.
		The order of the components within the pixel is specified by
		PhotometricInterpretation. For example, for RGB data, the data is stored as
		RGBRGBRGB…
		2 =  Planar  format. The components are stored in separate “component planes.”  The
		values in StripOffsets and StripByteCounts are then arranged as a 2-dimensional
		array, with SamplesPerPixel rows and StripsPerImage columns. (All of the col-umns for row 0 are stored first, followed by the columns of row 1, and so on.)
		PhotometricInterpretation describes the type of data stored in each component
		plane. For example, RGB data is stored with the Red components in one compo-nent plane, the Green in another, and the Blue in another.
		PlanarConfiguration=2 is not currently in widespread use and it is not recom-mended for general interchange. It is used as an extension and Baseline TIFF
		readers are not required to support it.
		If SamplesPerPixel is 1, PlanarConfiguration is irrelevant, and need not be in-cluded.
		If a row interleave effect is desired, a writer might write out the data as
		PlanarConfiguration=2—separate sample planes—but break up the planes into
		multiple strips (one row per strip, perhaps) and interleave the strips.
		Default is 1. See also BitsPerSample, SamplesPerPixel
	 */
	protected int planerConfiguration;
	
}
