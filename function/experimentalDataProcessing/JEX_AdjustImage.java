package function.experimentalDataProcessing;

import ij.ImagePlus;
import ij.process.FloatProcessor;

import java.awt.Point;
import java.util.HashMap;
import java.util.TreeMap;

import jex.statics.JEXStatics;
import utilities.FunctionUtility;
import Database.DBObjects.JEXData;
import Database.DBObjects.JEXDataSingle;
import Database.DBObjects.JEXEntry;
import Database.DBObjects.dimension.DimensionMap;
import Database.DataReader.ImageReader;
import Database.DataWriter.ImageWriter;
import Database.Definition.Parameter;
import Database.Definition.ParameterSet;
import Database.Definition.TypeName;
import Database.SingleUserDatabase.JEXWriter;
import function.ExperimentalDataCrunch;
import function.GraphicalCrunchingEnabling;
import function.GraphicalFunctionWrap;
import function.ImagePanel;
import function.ImagePanelInteractor;
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
public class JEX_AdjustImage extends ExperimentalDataCrunch{
	
	public JEX_AdjustImage(){}

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
		String result = "Adjust levels";
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
		String result = "Function that allows you to offset, scale, gamma adjust, and threshold image intensities, as well as save as a different bitdepth after adjustment.";
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
		inputNames[0] = new TypeName(IMAGE,"Image to adjust");
		return inputNames;
	}
	
	/**
	 * Return the array of output names defined for this function
	 * @return
	 */
	@Override
	public TypeName[] getOutputs(){
		defaultOutputNames = new TypeName[1];
		defaultOutputNames[0] = new TypeName(IMAGE,"Adjusted Image");
		
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
		Parameter p0 = new Parameter("Automatic","Enable visual interface",FormLine.DROPDOWN,new String[] {"true","false"},0);
		Parameter p1 = new Parameter("Old Min","Image Intensity Value","0.0");
		Parameter p2 = new Parameter("Old Max","Image Intensity Value","4095.0");
		Parameter p3 = new Parameter("New Min","Image Intensity Value","0.0");
		Parameter p4 = new Parameter("New Max","Image Intensity Value","65535.0");
		Parameter p5 = new Parameter("Gamma","0.1-5.0, value of 1 results in no change","1.0");
		Parameter p6 = new Parameter("Output Bit Depth","Depth of the outputted image",FormLine.DROPDOWN,new String[] {"8","16","32"},1);
		
		// Make an array of the parameters and return it
		ParameterSet parameterArray = new ParameterSet();
		parameterArray.addParameter(p0);
		parameterArray.addParameter(p1);
		parameterArray.addParameter(p2);
		parameterArray.addParameter(p3);
		parameterArray.addParameter(p4);
		parameterArray.addParameter(p5);
		parameterArray.addParameter(p6);
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
		JEXData data = inputs.get("Image to adjust");
		if (!data.getTypeName().getType().equals(JEXData.IMAGE)) return false;
		
		// Run the function
		AdjustImageHelperFunction graphFunc = new AdjustImageHelperFunction(data,entry,outputNames,parameters);
		graphFunc.doit();
		JEXData output1 = graphFunc.output;
		
		// Set the outputs
		realOutputs.add(output1);
		
		// Return status
		return true;
	}
}


class AdjustImageHelperFunction implements GraphicalCrunchingEnabling, ImagePanelInteractor{
	ImagePanel     imagepanel   ;
	GraphicalFunctionWrap wrap  ;
	DimensionMap[]   dimensions ;
	String[]         images     ;
	ImagePlus      im     ;
	FloatProcessor imp    ;
	int index    = 0      ;
	int atStep   = 0      ;
	
	boolean auto  = false;
	double oldMin = 0;
	double oldMax = 1000;
	double newMin = 0;
	double newMax = 100;
	double gamma  = 0.5;
	int depth     = 16;
	
	ParameterSet params;
	JEXData      imset;
	JEXEntry     entry;
	TypeName[]     outputNames;
	public JEXData output;
	
	AdjustImageHelperFunction(JEXData imset, JEXEntry entry, TypeName[] outputNames, ParameterSet parameters){
		// Pass the variables
		this.imset  = imset;
		this.params = parameters;
		this.entry  = entry;
		this.outputNames = outputNames;
		
		////// Get params
		auto  = Boolean.parseBoolean(params.getValueOfParameter("Automatic"));
		oldMin = Double.parseDouble(params.getValueOfParameter("Old Min"));
		oldMax = Double.parseDouble(params.getValueOfParameter("Old Max"));
		newMin = Double.parseDouble(params.getValueOfParameter("New Min"));
		newMax = Double.parseDouble(params.getValueOfParameter("New Max"));
		gamma  = Double.parseDouble(params.getValueOfParameter("Gamma"));
		depth     = Integer.parseInt(params.getValueOfParameter("Output Bit Depth"));
		
		TreeMap<DimensionMap,JEXDataSingle> map = imset.getDataMap();
		int length = map.size();
		
		images     = new String[length];
		dimensions = new DimensionMap[length];
		int i  = 0;
		TreeMap<DimensionMap,String> pathMap = ImageReader.readObjectToImagePathTable(imset);
		for (DimensionMap dim: pathMap.keySet()){
			String path   = pathMap.get(dim);
			dimensions[i] = dim;
			images[i]     = path;
			i ++;
		}
		
		// Prepare the graphics
		imagepanel = new ImagePanel(this,"Adjust image");
		displayImage(index);
		wrap = new GraphicalFunctionWrap(this,params);
		wrap.addStep(0, "Select roi", new String[] {"Automatic","Old Min","Old Max","New Min","New Max","Output Bit Depth"});
		wrap.setInCentralPanel(imagepanel);
		wrap.setDisplayLoopPanel(true);
	}

	private void displayImage(int index){
		ImagePlus im = new ImagePlus(images[index]);
		imagepanel.setImage(im);
	}
	
	/**
	 * Run the function and open the graphical interface
	 * @return the ROI data
	 */
	public JEXData doit(){
		if (!auto){
			wrap.start();
		}
		else {
			finishIT();
		}
		
		return output;
	}

	public void runStep(int index) {
		// Get the new parameters
		auto  = Boolean.parseBoolean(params.getValueOfParameter("Automatic"));
		oldMin = Double.parseDouble(params.getValueOfParameter("Old Min"));
		oldMax = Double.parseDouble(params.getValueOfParameter("Old Max"));
		newMin = Double.parseDouble(params.getValueOfParameter("New Min"));
		newMax = Double.parseDouble(params.getValueOfParameter("New Max"));
		gamma  = Double.parseDouble(params.getValueOfParameter("Gamma"));
		depth     = Integer.parseInt(params.getValueOfParameter("Output Bit Depth"));
		
		// prepare the images for calculation
		ImagePlus im = new ImagePlus(images[index]);
		imagepanel.setImage(im);
		imp = (FloatProcessor) im.getProcessor().convertToFloat(); // should be a float processor
		
		adjustImage();
		
		imagepanel.setImage(new ImagePlus("",imp));
	}
	public void runNext(){
		atStep = atStep+1;
		if (atStep > 0) atStep = 0;
	}
	public void runPrevious(){
		atStep = atStep-1;
		if (atStep < 0) atStep = 0;
	}
	public int getStep(){ return atStep;}
	
	public void loopNext(){
		index = index + 1;
		
		if (index >= images.length-1) index = images.length-1;
		if (index < 0) index = 0;
		
		runStep(index);
	}
	public void loopPrevious(){
		index = index - 1;
		
		if (index >= images.length-1) index = images.length-1;
		if (index < 0) index = 0;
		
		runStep(index);
	}
	public void recalculate(){}

	public void startIT() {
		wrap.displayUntilStep();
	}
	/**
	 * Apply the roi to all other images
	 */
	public void finishIT() {
		auto  = Boolean.parseBoolean(params.getValueOfParameter("Automatic"));
		oldMin = Double.parseDouble(params.getValueOfParameter("Old Min"));
		oldMax = Double.parseDouble(params.getValueOfParameter("Old Max"));
		newMin = Double.parseDouble(params.getValueOfParameter("New Min"));
		newMax = Double.parseDouble(params.getValueOfParameter("New Max"));
		gamma  = Double.parseDouble(params.getValueOfParameter("Gamma"));
		depth     = Integer.parseInt(params.getValueOfParameter("Output Bit Depth"));

		output = new JEXData(JEXData.IMAGE,outputNames[0].getName(),"Adjusted image");
		
		// Run the function
//		TreeMap<DimensionMap,JEXDataSingle> map = imset.getDataMap();
		TreeMap<DimensionMap,String> pathMap = ImageReader.readObjectToImagePathTable(imset);
		int count = 0;
		int total = pathMap.size();
		JEXStatics.statusBar.setProgressPercentage(0);
		
		TreeMap<DimensionMap,String> imMap = new TreeMap<DimensionMap,String>();
		for (DimensionMap dim: pathMap.keySet()){
//			JEXDataSingle ds = map.get(dim);
//			String imagePath = ds.get(JEXDataSingle.FOLDERNAME) + File.separator + ds.get(JEXDataSingle.FILENAME);
			String imagePath = pathMap.get(dim);
//			File   imageFile = new File(imagePath);
//			String imageName = imageFile.getName();
			
			// get the image
			im  = new ImagePlus(imagePath);
			imp = (FloatProcessor) im.getProcessor().convertToFloat(); // should be a float processor
			
			////// Begin Actual Function
			adjustImage();
			////// End Actual Function
			
			////// Save the results
			ImagePlus toSave = FunctionUtility.makeImageToSave(imp, "false", depth);
			String imPath    = JEXWriter.saveImage(toSave);

			// Put into imMap
			imMap.put(dim, imPath) ;
			
//			String localDir     = JEXWriter.getEntryFolder(entry);
//			String newFileName  = FunctionUtility.getNextName(localDir, imageName, "A");
//			String newImagePath = localDir + File.separator + newFileName;
//			FunctionUtility.imSave(imp, "false", depth, newImagePath);
//			
//			// Put into imMap
//			imMap.put(dim, localDir + File.separator + newFileName) ;
			
//			JEXDataSingle outputds = new DefaultJEXDataSingle();
//			outputds.put(JEXDataSingle.FOLDERNAME, localDir);
//			outputds.put(JEXDataSingle.FILENAME, newFileName);
//			output.addData(dim,outputds);
			
			// Increment count
			JEXStatics.logManager.log("Finished processing " + count + " of " + total + ".",1,this);
			count++;
			
			// Status bar
			int percentage = (int) (100 * ((double) count/ (double)pathMap.size()));
			JEXStatics.statusBar.setProgressPercentage(percentage);
		}

		output = ImageWriter.makeImageStackFromPaths(outputNames[0].getName(), imMap);
	}
	
	private void adjustImage(){
		FunctionUtility.imAdjust(imp, oldMin, oldMax, newMin, newMax, gamma);
	}

	
	public void clickedPoint(Point p) {}
	public void pressedPoint(Point p) {}
	public void mouseMoved(Point p){}
	
}


