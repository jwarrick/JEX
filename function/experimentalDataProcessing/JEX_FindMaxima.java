package function.experimentalDataProcessing;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import image.roi.ROIPlus;

import java.util.HashMap;
import java.util.TreeMap;

import jex.statics.JEXStatics;
import Database.DBObjects.JEXData;
import Database.DBObjects.JEXEntry;
import Database.DBObjects.dimension.DimensionMap;
import Database.DataReader.ImageReader;
import Database.DataReader.RoiReader;
import Database.DataWriter.RoiWriter;
import Database.Definition.Parameter;
import Database.Definition.ParameterSet;
import Database.Definition.TypeName;
import function.ExperimentalDataCrunch;
import function.imageUtility.MaximumFinder;
import guiObject.FormLine;


/**
 * This is a JEXperiment function template
 * To use it follow the following instructions
 * 
 * 1. Fill in all the required methods according to their specific instructions
 * 2. Place the file in the Functions/SingleDataPointFunctions folder
 * 3. Compile and run JEX!
 * 
 * JEX enables the use of several data object types
 * The specific API for these can be found in the main JEXperiment folder.
 * These API provide methods to retrieve data from these objects,
 * create new objects and handle the data they contain.
 * 
 * @author erwinberthier
 *
 */
public class JEX_FindMaxima extends ExperimentalDataCrunch{
	
	public JEX_FindMaxima(){}

	// ----------------------------------------------------
	// --------- INFORMATION ABOUT THE FUNCTION -----------
	// ----------------------------------------------------
	
	/**
	 * Returns the name of the function
	 * 
	 * @return Name string
	 */
	@Override
	public String getName() {
		String result = "Find Maxima";
		return result;
	}
	
	/**
	 * This method returns a string explaining what this method does
	 * This is purely informational and will display in JEX
	 * 
	 * @return Information string
	 */
	@Override
	public String getInfo() {
		String result = "Find maxima in a grayscale image.";
		return result;
	}

	/**
	 * This method defines in which group of function this function 
	 * will be shown in... 
	 * Toolboxes (choose one, caps matter):
	 * Visualization, Image processing, Custom Cell Analysis, Cell tracking, Image tools
	 * Stack processing, Data Importing, Custom image analysis, Matlab/Octave
	 * 
	 */
	@Override
	public String getToolbox() {
		String toolbox = "Image processing";
		return toolbox;
	}

	/**
	 * This method defines if the function appears in the list in JEX
	 * It should be set to true expect if you have good reason for it
	 * 
	 * @return true if function shows in JEX
	 */
	@Override
	public boolean showInList() {
		return true;
	}
	
	/**
	 * Returns true if the user wants to allow multithreding
	 * @return
	 */
	@Override
	public boolean allowMultithreading()
	{
		return true;
	}
	
	
	
	// ----------------------------------------------------
	// --------- INPUT OUTPUT DEFINITIONS -----------------
	// ----------------------------------------------------
	
	/**
	 * Return the array of input names
	 * 
	 * @return array of input names
	 */
	@Override
	public TypeName[] getInputNames(){
		TypeName[] inputNames = new TypeName[2];
		inputNames[0] = new TypeName(IMAGE,"Image");
		inputNames[1] = new TypeName(ROI,"ROI (optional)");
		return inputNames;
	}
	
	/**
	 * Return the array of output names defined for this function
	 * @return
	 */
	@Override
	public TypeName[] getOutputs(){
		defaultOutputNames = new TypeName[1];
		defaultOutputNames[0] = new TypeName(ROI,"Maxima");
		
		if (outputNames == null) return defaultOutputNames;
		return outputNames;
	}
	
	/**
	 * Returns a list of parameters necessary for this function 
	 * to run...
	 * Every parameter is defined as a line in a form that provides 
	 * the ability to set how it will be displayed to the user and 
	 * what options are available to choose from
	 * The simplest FormLine can be written as:
	 * FormLine p = new FormLine(parameterName);
	 * This will provide a text field for the user to input the value
	 * of the parameter named parameterName
	 * More complex displaying options can be set by consulting the 
	 * FormLine API
	 * 
	 * @return list of FormLine to create a parameter panel
	 */
	@Override
	public ParameterSet requiredParameters() {
		//(ImageProcessor ip, double tolerance, double threshold, int outputType, boolean excludeOnEdges, boolean isEDM, Roi roiArg, boolean lightBackground)
//		Parameter p0 = new Parameter("Dummy Parameter","Lets user know that the function has been selected.",FormLine.DROPDOWN,new String[] {"true"},0);
		Parameter p1 = new Parameter("Tolerance","Local intensity increase threshold.","20");
		Parameter p2 = new Parameter("Threshold","Minimum hieght of a maximum.","0");
		Parameter p3 = new Parameter("Exclude on Edges","Exclude particles on the edge of the image?",FormLine.DROPDOWN, new String[] {"True", "False"}, 0);
		Parameter p4 = new Parameter("Is EDM?","Is the image being analyzed already a Euclidean Distance Measurement?",FormLine.DROPDOWN, new String[] {"True", "False"}, 1);
		Parameter p5 = new Parameter("Particles Are White?","Are the particles displayed as white on a black background?",FormLine.DROPDOWN, new String[] {"True", "False"}, 0);
		
		// Make an array of the parameters and return it
		ParameterSet parameterArray = new ParameterSet();
//		parameterArray.addParameter(p0);
		parameterArray.addParameter(p1);
		parameterArray.addParameter(p2);
		parameterArray.addParameter(p3);
		parameterArray.addParameter(p4);
		parameterArray.addParameter(p5);
		return parameterArray;
	}
	
	
	// ----------------------------------------------------
	// --------- ERROR CHECKING METHODS -------------------
	// ----------------------------------------------------
	
	/**
	 * Returns the status of the input validity checking
	 * It is HIGHLY recommended to implement input checking
	 * however this can be over-ridden by returning false
	 * If over-ridden ANY batch function using this function 
	 * will not be able perform error checking... 
	 * 
	 * @return true if input checking is on
	 */
	@Override
	public boolean isInputValidityCheckingEnabled(){
		return false;
	}
	
	
	// ----------------------------------------------------
	// --------- THE ACTUAL MEAT OF THIS FUNCTION ---------
	// ----------------------------------------------------

	/**
	 * Perform the algorithm here
	 * 
	 */
	@Override
	public boolean run(JEXEntry entry, HashMap<String,JEXData> inputs)
	{
		// Collect the inputs
		boolean roiProvided = true;
		JEXData imageData = inputs.get("Image");
		if (imageData == null || !imageData.getTypeName().getType().equals(JEXData.IMAGE)) return false;
		JEXData roiData = inputs.get("ROI (optional)");
		if (roiData != null && !roiData.getTypeName().getType().equals(JEXData.ROI)) roiProvided = false;
		
		// Gather parameters
		double tolerance = Double.parseDouble(parameters.getValueOfParameter("Tolerance"));
		double threshold = Double.parseDouble(parameters.getValueOfParameter("Threshold"));
		boolean excludeOnEdges = Boolean.parseBoolean(parameters.getValueOfParameter("Exclude on Edges?"));
		boolean isEDM = Boolean.parseBoolean(parameters.getValueOfParameter("Is EDM?"));
		boolean lightBackground = !Boolean.parseBoolean(parameters.getValueOfParameter("Particles Are White?"));
		
		TreeMap<DimensionMap,ROIPlus> roiMap;
		// Run the function
		if(roiProvided)
		{
			roiMap = RoiReader.readObjectToRoiMap(roiData);
		}
		else
		{
			roiMap = new TreeMap<DimensionMap,ROIPlus>();
		}
		TreeMap<DimensionMap,String> imageMap = ImageReader.readObjectToImagePathTable(imageData);
		TreeMap<DimensionMap,ROIPlus> outputRoiMap = new TreeMap<DimensionMap,ROIPlus>();
		int count = 0, percentage = 0;
		MaximumFinder mf = new MaximumFinder();
		Roi roi;
		ROIPlus roip;
		for(DimensionMap map : imageMap.keySet())
		{
			ImageProcessor ip = new ImagePlus(imageMap.get(map)).getProcessor();
			
			roi = null;
			roip = roiMap.get(map);
			if (roip != null)
			{
				boolean isLine = roip.isLine();
				if (isLine) return false;
				roi = roip.getRoi();
				ip.setRoi(roi);
			}
			
			ROIPlus points = (ROIPlus) mf.findMaxima(ip, tolerance, threshold, MaximumFinder.ROI, excludeOnEdges, isEDM, roi, lightBackground);
			outputRoiMap.put(map, points);
			count = count + 1;
			percentage = (int) (100 * ((double) (count)/ ((double) imageMap.size())));
			JEXStatics.statusBar.setProgressPercentage(percentage);
		}
		if(outputRoiMap.size() == 0)
		{
			return false;
		}
		
		JEXData output1 = RoiWriter.makeRoiObject(outputNames[0].getName(), outputRoiMap);
		
		// Set the outputs
		realOutputs.add(output1);
		
		// Return status
		return true;
	}
	
//	private String saveAdjustedImage(String imagePath, double oldMin, double oldMax, double newMin, double newMax, double gamma, int bitDepth)
//	{
//		// Get image data
//		File f = new File(imagePath);
//		if(!f.exists()) return null;
//		ImagePlus im = new ImagePlus(imagePath);
//		FloatProcessor imp = (FloatProcessor) im.getProcessor().convertToFloat(); // should be a float processor
//		
//		// Adjust the image
//		FunctionUtility.imAdjust(imp, oldMin, oldMax, newMin, newMax, gamma);
//
//		// Save the results
//		ImagePlus toSave = FunctionUtility.makeImageToSave(imp, "false", bitDepth);
//		String imPath    = JEXWriter.saveImage(toSave);
//		im.flush();
//
//		// return temp filePath
//		return imPath;
//	}
}