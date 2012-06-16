package function.experimentalDataProcessing;

import ij.ImagePlus;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import jex.statics.JEXStatics;
import utilities.FunctionUtility;
import Database.DBObjects.JEXData;
import Database.DBObjects.JEXEntry;
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
public class JEX_MakeTemplateCell extends ExperimentalDataCrunch {

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
		String result = "Make template cell";
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
		String result = "Make a template cell of a given radius";
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
		String toolbox = "Cell tracking";
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
		TypeName[] inputNames = new TypeName[0];
//		inputNames[0] = "Timelapse";
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
		defaultOutputNames[0] = new TypeName(IMAGE,"Cell");

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
		Parameter p1 = new Parameter("Image with","With of the template image","20");
		Parameter p2 = new Parameter("Image height","Height of the template image","20");
		Parameter p3 = new Parameter("Cell radius","Radius of the cell","7");
		
		// Make an array of the parameters and return it
		ParameterSet parameterArray = new ParameterSet();
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
		// Collect the parameters
		int radius   = (int) Double.parseDouble(parameters.getValueOfParameter("Cell radius"));
		int imWidth  = (int) Double.parseDouble(parameters.getValueOfParameter("Image with"));
		int imHeight = (int) Double.parseDouble(parameters.getValueOfParameter("Image height"));
	
		// Run the function
		JEXStatics.logManager.log("Running the function", 1, this);
		ImagePlus im = this.makeCellOfRadius(imWidth, imHeight, radius);
		FloatProcessor fimp = (FloatProcessor)im.getProcessor().convertToFloat();
		
		// Save the cell image
//		String localDir    = JEXWriter.getEntryFolder(entry);
//		String newFileName = FunctionUtility.getNextName(localDir, "Cell.tif", "Im");
//		String vhPath      = localDir+ File.separator + newFileName;
//		FunctionUtility.imSave(fimp, "false", 8, vhPath);
		
		ImagePlus toSave = FunctionUtility.makeImageToSave(fimp, "false", 8);
		String vhPath    = JEXWriter.saveImage(toSave);
		
		// Collect the outputs
		JEXStatics.logManager.log("Collecting outputs", 1, this);
		JEXData cellIM = ImageWriter.makeImageObject(outputNames[0].getName(), vhPath);
		realOutputs.add(cellIM);
		
		// Return status
		return true;
	}
	
	/**
	 * Return an image of width and hight W and H of a disk of diameter PARTICLESIZE
	 * @param w
	 * @param h
	 * @param particleSize
	 * @return
	 */
	private ImagePlus makeCellOfRadius(int w, int h, int particleSize){
		// Create a buffered image using the B&W model
		int type = BufferedImage.TYPE_BYTE_BINARY;
		BufferedImage bimage = new BufferedImage(w, h, type);
		
		// Copy image to buffered image
		Graphics g = bimage.createGraphics();

		// Paint a circle in the center
		g.setColor(Color.white);
		g.fillOval(w/2-particleSize, h/2-particleSize, 2*particleSize, 2*particleSize);
		g.dispose();
		
		// Make an imageplus
		ImagePlus result = new ImagePlus("",bimage);
		ImageProcessor imp = result.getProcessor().convertToByte(true);
		result = new ImagePlus("",imp);
		
		return result;
	}
	
}

