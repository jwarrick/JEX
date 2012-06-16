package function.experimentalDataProcessing;

import ij.ImagePlus;
import ij.plugin.filter.RankFilters;
import ij.process.ImageProcessor;

import java.util.HashMap;
import java.util.TreeMap;

import jex.statics.JEXStatics;
import Database.DBObjects.JEXData;
import Database.DBObjects.JEXEntry;
import Database.DBObjects.dimension.DimensionMap;
import Database.DataReader.ImageReader;
import Database.DataWriter.ImageWriter;
import Database.Definition.Parameter;
import Database.Definition.ParameterSet;
import Database.Definition.TypeName;
import Database.SingleUserDatabase.JEXWriter;
import function.ExperimentalDataCrunch;
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
public class JEX_Filters extends ExperimentalDataCrunch{
	
	public JEX_Filters(){}

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
		String result = "Image Filters";
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
		String result = "Use a predefined image filter and specify the filter radius.";
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
		TypeName[] inputNames = new TypeName[1];
		inputNames[0] = new TypeName(IMAGE,"Image");
		return inputNames;
	}
	
	/**
	 * Return the array of output names defined for this function
	 * @return
	 */
	@Override
	public TypeName[] getOutputs(){
		defaultOutputNames = new TypeName[1];
		defaultOutputNames[0] = new TypeName(IMAGE,"Filtered Image");
		
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
//		Parameter p0 = new Parameter("Dummy Parameter","Lets user know that the function has been selected.",FormLine.DROPDOWN,new String[] {"true"},0);
		Parameter p1 = new Parameter("Filter Type","Type of filter to apply.",FormLine.DROPDOWN, new String[]{"mean","min","max","median","variance"},0);
		Parameter p2 = new Parameter("Radius","Radius of filter in pixels.","2.0");
		Parameter p3 = new Parameter("Output Bit-Depth","Bit-Depth of the output image",FormLine.DROPDOWN,new String[]{"8","16","32"},2);
		
		// Make an array of the parameters and return it
		ParameterSet parameterArray = new ParameterSet();
//		parameterArray.addParameter(p0);
		parameterArray.addParameter(p1);
		parameterArray.addParameter(p2);
		parameterArray.addParameter(p3);

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
		JEXData imageData = inputs.get("Image");
		if (imageData == null || !imageData.getTypeName().getType().equals(JEXData.IMAGE)) return false;

		// Gather parameters
		double radius = Double.parseDouble(parameters.getValueOfParameter("Radius"));
		String method = parameters.getValueOfParameter("Filter Type");
		int bitDepth = Integer.parseInt(parameters.getValueOfParameter("Output Bit-Depth"));
		
		// Run the function
		TreeMap<DimensionMap,String> imageMap = ImageReader.readObjectToImagePathTable(imageData);
		TreeMap<DimensionMap,String> outputImageMap = new TreeMap<DimensionMap,String>();
		int count = 0, percentage = 0;
		for(DimensionMap map : imageMap.keySet())
		{
			ImagePlus im = new ImagePlus(imageMap.get(map));
			ImageProcessor ip = im.getProcessor().convertToFloat();
			
			////// Begin Actual Function
			RankFilters rF = new RankFilters();
			rF.setup(method, im);
			rF.makeKernel(radius);
			rF.run(ip);
			////// End Actual Function
			
			ImageProcessor toSave = ip;
			if(bitDepth == 8)
			{
				toSave = ip.convertToByte(false);
			}
			else if(bitDepth == 16)
			{
				toSave = ip.convertToShort(false);
			}
						
			String path = JEXWriter.saveImage(toSave);
			
			if(path != null)
			{
				outputImageMap.put(map, path);
			}
			
			count = count + 1;
			percentage = (int) (100 * ((double) (count)/ ((double) imageMap.size())));
			JEXStatics.statusBar.setProgressPercentage(percentage);
		}
		if(outputImageMap.size() == 0)
		{
			return false;
		}
		
		JEXData output1 = ImageWriter.makeImageStackFromPaths(outputNames[0].getName(), outputImageMap);
		
		// Set the outputs
		realOutputs.add(output1);
		
		// Return status
		return true;
	}

}




