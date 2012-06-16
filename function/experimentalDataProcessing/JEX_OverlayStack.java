package function.experimentalDataProcessing;

import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import jex.statics.JEXStatics;
import utilities.FunctionUtility;
import Database.DBObjects.JEXData;
import Database.DBObjects.JEXEntry;
import Database.DBObjects.dimension.Dim;
import Database.DBObjects.dimension.DimTable;
import Database.DBObjects.dimension.DimensionMap;
import Database.DataReader.ImageReader;
import Database.DataWriter.ImageWriter;
import Database.Definition.Parameter;
import Database.Definition.ParameterSet;
import Database.Definition.TypeName;
import Database.SingleUserDatabase.JEXWriter;
import function.ExperimentalDataCrunch;


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
public class JEX_OverlayStack extends ExperimentalDataCrunch{

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
		String result = "Image Stack Overlay";
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
		String result = "Overlay images along a single dimension assign them a color channel";
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
		String toolbox = "Stack processing";
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
		return false;
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
		TypeName[] inputNames = new TypeName[1];
		inputNames[0] = new TypeName(IMAGE,"Image Set");
		return inputNames;
	}
	
	/**
	 * Return the number of outputs returned by this function
	 * 
	 * @return number of outputs
	 */
	@Override
	public TypeName[] getOutputs() {
		defaultOutputNames = new TypeName[1];
		defaultOutputNames[0] = new TypeName(IMAGE,"Overlay Set");
		
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
		Parameter p1 = new Parameter("Dim Name", "Name of dim that contains the images to overlay.", "Wavelength");
		Parameter p2 = new Parameter("RED Dim Value", "Value of dim containing the RED image.", "");
		Parameter p3 = new Parameter("RED Min", "Value in the RED image to map to 0 intensity.","0");
		Parameter p4 = new Parameter("RED Max", "Value in the RED image to map to 255 intensity.", "65535");
		Parameter p5 = new Parameter("GREEN Dim Value", "Value of dim containing the RED image.", "");
		Parameter p6 = new Parameter("GREEN Min", "Value in the RED image to map to 0 intensity.","0");
		Parameter p7 = new Parameter("GREEN Max", "Value in the RED image to map to 255 intensity.", "65535");
		Parameter p8 = new Parameter("BLUE Dim Value", "Value of dim containing the RED image.", "");
		Parameter p9 = new Parameter("BLUE Min", "Value in the RED image to map to 0 intensity.","0");
		Parameter p10 = new Parameter("BLUE Max", "Value in the RED image to map to 255 intensity.", "65535");
		
		// Make an array of the parameters and return it
		ParameterSet parameterArray = new ParameterSet();
		parameterArray.addParameter(p1);
		parameterArray.addParameter(p2);
		parameterArray.addParameter(p3);
		parameterArray.addParameter(p4);
		parameterArray.addParameter(p5);
		parameterArray.addParameter(p6);
		parameterArray.addParameter(p7);
		parameterArray.addParameter(p8);
		parameterArray.addParameter(p9);
		parameterArray.addParameter(p10);
		return parameterArray;
	}
	
	
	// ----------------------------------------------------
	// --------- ERROR CHECKING METHODS -------------------
	// ----------------------------------------------------
	
	/**
	 * Returns the status of the input validity checking
	 * It is HIGHLY recommended to implement input checking
	 * however this can be over-rided by returning false
	 * If over-ridden ANY batch function using this function 
	 * will not be able perform error checking... 
	 * 
	 * @return true if input checking is on
	 */
	@Override
	public boolean isInputValidityCheckingEnabled(){
		return true;
	}
	
	
	
	// ----------------------------------------------------
	// --------- THE ACTUAL MEAT OF THIS FUNCTION ---------
	// ----------------------------------------------------

	/**
	 * Perform the algorithm here
	 * 
	 */
	@Override
	public boolean run(JEXEntry entry, HashMap<String,JEXData> inputs){
		// Collect the inputs
		JEXData data1 = inputs.get("Image Set");
		if (!data1.getTypeName().getType().equals(JEXData.IMAGE)) return false;
		
		////// Get params
		String dimName = parameters.getValueOfParameter("Dim Name");
		String rDim = parameters.getValueOfParameter("RED Dim Value");
		double rMin = Double.parseDouble(parameters.getValueOfParameter("RED Min"));
		double rMax = Double.parseDouble(parameters.getValueOfParameter("RED Max"));
		String gDim = parameters.getValueOfParameter("GREEN Dim Value");
		double gMin = Double.parseDouble(parameters.getValueOfParameter("GREEN Min"));
		double gMax = Double.parseDouble(parameters.getValueOfParameter("GREEN Max"));
		String bDim = parameters.getValueOfParameter("BLUE Dim Value");
		double bMin = Double.parseDouble(parameters.getValueOfParameter("BLUE Min"));
		double bMax = Double.parseDouble(parameters.getValueOfParameter("BLUE Max"));
		
		// Run the function
		TreeMap<DimensionMap,String> images1 = ImageReader.readObjectToImagePathTable(data1);
		TreeMap<DimensionMap,String> outputMap = new TreeMap<DimensionMap,String>();
		
		DimTable reducedTable = data1.getDimTable();
		Dim colorDim = reducedTable.getDimWithName(dimName);
		reducedTable.remove(colorDim);
		List<DimensionMap> maps = reducedTable.getDimensionMaps();
		
		int count = 0;
		int total = maps.size();

		for (DimensionMap map : maps)
		{
			// Make dims to get
			DimensionMap rMap = map.copy();
			rMap.put(dimName, rDim);
			DimensionMap gMap = map.copy();
			gMap.put(dimName, gDim);
			DimensionMap bMap = map.copy();
			bMap.put(dimName, bDim);
			
			// get the paths
			String rPath = null;
			if(rMap != null) rPath = images1.get(rMap);
			String gPath = null;
			if(gMap != null) gPath = images1.get(gMap);
			String bPath = null;
			if(bMap != null) bPath = images1.get(bMap);
			
			// get the images and image processors
			ImagePlus im = null, rIm = null, gIm = null, bIm = null;
			ByteProcessor rImp = null, gImp = null, bImp = null;
			FloatProcessor imp = null;
			int w=0, h=0;
			if(rPath != null)
			{
				im = new ImagePlus(rPath);
				imp = (FloatProcessor) im.getProcessor().convertToFloat(); // should be a float processor
				FunctionUtility.imAdjust(imp, rMin, rMax, 0, 255, 1);
				rIm = FunctionUtility.makeImageToSave(imp, "false", 8);
				rImp = (ByteProcessor) rIm.getProcessor();
				//rIm.show();
				if(w == 0 || h == 0)
				{
					w = rImp.getWidth();
					h = rImp.getHeight();
				}
			}
			if(gPath != null)
			{
				im = new ImagePlus(gPath);
				imp = (FloatProcessor) im.getProcessor().convertToFloat(); // should be a float processor
				FunctionUtility.imAdjust(imp, gMin, gMax, 0, 255, 1);
				gIm = FunctionUtility.makeImageToSave(imp, "false", 8);
				gImp = (ByteProcessor) gIm.getProcessor();
				//gIm.show();
				if(w == 0 || h == 0)
				{
					w = gImp.getWidth();
					h = gImp.getHeight();
				}
			}
			if(bPath != null)
			{
				im = new ImagePlus(bPath);
				imp = (FloatProcessor) im.getProcessor().convertToFloat(); // should be a float processor
				FunctionUtility.imAdjust(imp, bMin, bMax, 0, 255, 1);
				bIm = FunctionUtility.makeImageToSave(imp, "false", 8);
				bImp = (ByteProcessor) bIm.getProcessor();
				//bIm.show();
				if(w == 0 || h == 0)
				{
					w = bImp.getWidth();
					h = bImp.getHeight();
				}
			}
			
			if(w == 0 || h == 0) return false;
			
			////// Begin Actual Function
			byte[] r=null, g=null, b=null;
			ColorProcessor cp = new ColorProcessor(w, h);
			if(rImp != null) r = (byte[]) rImp.getPixels();
			if(gImp != null) g = (byte[]) gImp.getPixels();
			if(bImp != null) b = (byte[]) bImp.getPixels();
			if(rImp == null) r = new byte[w*h];
			if(gImp == null) g = new byte[w*h];
			if(bImp == null) b = new byte[w*h];
			cp.setRGB(r, g, b);
			////// End Actual Function
			
			////// Save the results
			String finalPath = JEXWriter.saveImage(cp);
			outputMap.put(map.copy(), finalPath);
			JEXStatics.logManager.log("Finished processing " + (count+1) + " of " + total + ".",1,this);
			count++;

			// Status bar
			int percentage = (int) (100 * ((double) count/ (double)maps.size()));
			JEXStatics.statusBar.setProgressPercentage(percentage);
		}
		
		// Set the outputs
		JEXData output = ImageWriter.makeImageStackFromPaths(outputNames[0].getName(), outputMap);
		output.setDataObjectInfo("Overaly performed using Image Overlay Along a Dimension Function");
		realOutputs.add(output);
		
		// Return status
		return true;
	}
}
