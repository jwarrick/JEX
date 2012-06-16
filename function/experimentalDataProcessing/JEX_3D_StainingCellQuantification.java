package function.experimentalDataProcessing;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.filter.BackgroundSubtracter;
import ij.plugin.filter.MaximumFinder;
import ij.plugin.filter.ParticleAnalyzer;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.ShortProcessor;
import image.roi.PointList;
import image.roi.ROIPlus;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jex.statics.JEXStatics;
import utilities.ArrayUtility;
import utilities.FunctionUtility;
import Database.DBObjects.JEXData;
import Database.DBObjects.JEXEntry;
import Database.DataReader.ImageReader;
import Database.DataReader.RoiReader;
import Database.DataReader.ValueReader;
import Database.DataWriter.ImageWriter;
import Database.DataWriter.ValueWriter;
import Database.Definition.Parameter;
import Database.Definition.ParameterSet;
import Database.Definition.TypeName;
import Database.SingleUserDatabase.JEXWriter;
import function.ExperimentalDataCrunch;
import function.GraphicalCrunchingEnabling;
import function.GraphicalFunctionWrap;
import function.ImagePanel;
import function.ImagePanelInteractor;
import function.imageUtility.Thresholder;
import function.tracker.HistogramFactory;
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
/**
 * @author edmyoung
 *
 */
public class JEX_3D_StainingCellQuantification extends ExperimentalDataCrunch{

	// ----------------------------------------------------
	// --------- INFORMATION ABOUT THE FUNCTION -----------
	// ----------------------------------------------------
	
	/**
	 * Returns the name of the function
	 * 
	 * @return Name string
	 */
	public String getName() {
		String result = "3D count with fluo. staining.";
		return result;
	}
	
	/**
	 * This method returns a string explaining what this method does
	 * This is purely informational and will display in JEX
	 * 
	 * @return Information string
	 */
	public String getInfo() {
		String result = "Find locations of clusters of cells using a fluorescent stain and find number";
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
	public String getToolbox() {
		String toolbox = "Custom Cell Analysis";
		return toolbox;
	}

	/**
	 * This method defines if the function appears in the list in JEX
	 * It should be set to true expect if you have good reason for it
	 * 
	 * @return true if function shows in JEX
	 */
	public boolean showInList() {
		return true;
	}
	
	/**
	 * Returns true if the user wants to allow multithreding
	 * @return
	 */
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
	public TypeName[] getInputNames(){
		TypeName[] inputNames = new TypeName[3];
		
		inputNames[0] = new TypeName(IMAGE,"Fluorescent Image");
		inputNames[1] = new TypeName(ROI,"Optional ROI");
		inputNames[2] = new TypeName(VALUE,"Optional Normalization value");
		
		return inputNames;
	}
	
	/**
	 * Return the number of outputs returned by this function
	 * 
	 * @return number of outputs
	 */
	public TypeName[] getOutputs(){
		defaultOutputNames = new TypeName[6];
		defaultOutputNames[0] = new TypeName(VALUE,"Total cell number");
		defaultOutputNames[1] = new TypeName(VALUE,"Total cell area");
		defaultOutputNames[2] = new TypeName(VALUE,"Mean cluster size");
		defaultOutputNames[3] = new TypeName(VALUE,"Histogram");
		defaultOutputNames[4] = new TypeName(VALUE,"More Info");
		defaultOutputNames[5] = new TypeName(IMAGE,"Cell mask Image");
		
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
	public ParameterSet requiredParameters() {
		Parameter p0 = new Parameter("Automatic","Enable visual interface",FormLine.DROPDOWN,new String[] {"true","false"},1);
		
		Parameter p1 = new Parameter("RollingBall","Rolling ball radius for removing background","50.0");
		Parameter p2 = new Parameter("Use thresh. method","Use a thresholding method to find the cells... better for out-of-focus clusters",FormLine.DROPDOWN,new String[] {"true","false"},0);
		Parameter p3 = new Parameter("Erode-Dilate","Erode dilate to remove small clusters",FormLine.DROPDOWN,new String[] {"true","false"},0);
		Parameter p4 = new Parameter("Tolerance","Threshold for identifying live cell locations","8.0");
		Parameter p5 = new Parameter("Min. value.","Lowest value for a live cell minimum","1.0");
		Parameter p6 = new Parameter("Min. cell radius","Radius of cell in pixels (e.g. 3 to 30)","5");
		Parameter p7 = new Parameter("Max. cell radius","Radius of cell in pixels (e.g. 3 to 30)","1000");
		Parameter p8 = new Parameter("Cell Area","Number of pixels covered by one cell. Set to -1 for disabling area normalization","-1");
		
		// Make an array of the parameters and return it
		ParameterSet parameterArray = new ParameterSet();
		parameterArray.addParameter(p0);
		parameterArray.addParameter(p1);
		parameterArray.addParameter(p2);
		parameterArray.addParameter(p3);
		parameterArray.addParameter(p4);
		parameterArray.addParameter(p5);
		parameterArray.addParameter(p6);
		parameterArray.addParameter(p7);
		parameterArray.addParameter(p8);
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
	public boolean run(JEXEntry entry, HashMap<String,JEXData> inputs){
		// Collect the inputs
		JEXData data1 = inputs.get("Fluorescent Image");
		if (!data1.getTypeName().getType().equals(JEXData.IMAGE)) return false;
		
		JEXData data2 = inputs.get("Optional Normalization Image");
		JEXData data3 = inputs.get("Optional ROI");
		
		// Run the function
		FluorescentCellFinder graphFunc = new FluorescentCellFinder(entry,data1,data2,data3,outputNames,parameters);
		graphFunc.doit();
		JEXData output1 = graphFunc.totalNumber;
		JEXData output2 = graphFunc.day1_clusterSize;
		JEXData output3 = graphFunc.moreInfo;
		JEXData output4 = graphFunc.cellImage;
		JEXData output5 = graphFunc.totalArea;
		JEXData output6 = graphFunc.histogram;
		
		// Set the outputs
		realOutputs.add(output1);
		realOutputs.add(output2);
		realOutputs.add(output3);
		realOutputs.add(output4);
		realOutputs.add(output5);
		realOutputs.add(output6);
		
		// Return status
		return true;
	}
}



class FluorescentCellFinder implements GraphicalCrunchingEnabling, ImagePanelInteractor{
	
	// Utilities
	ImagePanel imagepanel ;
	GraphicalFunctionWrap wrap ;
	int index    = 0      ;
	int atStep   = 0      ;
	int frame    = 0      ;
	
	// Roi interaction
	boolean interactionMode = false;
	Point first         = null;
	Point second        = null;
	Rectangle rectroi   = null;
	
	// Outputs
	public JEXData totalNumber;
	public JEXData day1_clusterSize;
	public JEXData moreInfo ;
	public JEXData cellImage;
	public JEXData totalArea;
	public JEXData histogram;
	
	// Parameters
	ParameterSet params  ;
	boolean auto             = false ;
	boolean useThreshold     = false ;
	int     rollingBall      = 30 ;
	int     thresh           = 40 ;
	double  mean             = 1 ;
	float   singleCellArea   = -1;
	boolean erodedilate      = true;
	
	double  radius1          = 15.0;
	double  radius2          = 20.0;
	boolean createBackground = false; 
	boolean lightBackground  = false; 
	boolean useParaboloid    = false; 
	boolean doPresmooth      = false;
	boolean correctCorners   = false;
	
	// Variables used during the function steps
	private ImagePlus     im  ;
	private ImagePlus     cellIm ;
	private ImagePlus     cellImPreprocessed ;
	private Double        norm ;
	
	// Input
	JEXData    day1_cellImset      ;
	JEXData    day1_normalization  ;
	JEXData    rectangle          ;
	JEXEntry   entry          ;
	TypeName[]   outputNames    ;
	
	PointList      day1_Points  ;
   	float[]        clusterAreas     ;
	
   	FluorescentCellFinder(
			JEXEntry entry, 
			JEXData day1_cellImset, 
			JEXData day1_normalization, 
			JEXData rectangle, 
			TypeName[] outputNames, 
			ParameterSet parameters){
		 
		// Pass the variables
		this.day1_cellImset     = day1_cellImset;
		this.day1_normalization = day1_normalization;
		this.rectangle   = rectangle;
		this.params      = parameters;
		this.entry       = entry;
		this.outputNames = outputNames;
		
		////// Get params
		getParams();
		
		// Prepare images
		cellIm = ImageReader.readObjectToImagePlus(day1_cellImset);
		if (day1_normalization != null){
			norm = ValueReader.readObjectToDouble(day1_normalization);
		}
		if (rectangle != null){
			ROIPlus roip = RoiReader.readObjectToRoi(rectangle);
			rectroi      = (roip!=null && roip.roi!=null) ? roip.roi.getBounds() : null;
		}
		
		// Prepare the graphics
		imagepanel = new ImagePanel(this,"Locate live dead cells and determine proliferation");
		imagepanel.setImage(cellIm);
		
		//displayImage(index);
		wrap = new GraphicalFunctionWrap(this,params);
		wrap.addStep(0, "LIVE - Preprocess image", new String[] {"RollingBall","Use thresh. method","Erode-Dilate","Tolerance","Min. value."});
		wrap.addStep(1, "LIVE - locate clusters", new String[] {"Min. cell radius","Max. cell radius"});
		wrap.addStep(2, "Analyze", new String[] {"Cell Area","Automatic"} );
		
		String title = "Analyzing entry "+entry.getEntryExperiment()+" - "+entry.getEntryTrayName()+" - "+entry.getTrayX()+"."+entry.getTrayY();
		wrap.setTitle(title);
		wrap.setInCentralPanel(imagepanel);
		wrap.setDisplayLoopPanel(true);
	}
	
	/**
	 * Retrieve the parameters of the function
	 */
	private void getParams(){
		////// Get params
		auto           = Boolean.parseBoolean(params.getValueOfParameter("Automatic"));
		useThreshold   = Boolean.parseBoolean(params.getValueOfParameter("Use thresh. method"));
		rollingBall    = (int) Double.parseDouble(params.getValueOfParameter("RollingBall"));
		thresh         = (int) Double.parseDouble(params.getValueOfParameter("Tolerance"));
		mean           = Double.parseDouble(params.getValueOfParameter("Min. value."));
		singleCellArea = Float.parseFloat(params.getValueOfParameter("Cell Area"));
		
		radius1   = (int) Double.parseDouble(params.getValueOfParameter("Min. cell radius"));
		radius2   = (int) Double.parseDouble(params.getValueOfParameter("Max. cell radius"));
		
		//removeOutliers = Float.parseFloat(params.getValueOfParameter("Outliers"));
		erodedilate = Boolean.parseBoolean(params.getValueOfParameter("Erode-Dilate"));
	}

	@SuppressWarnings("unused")
	private void displayImage(int index){
		imagepanel.setImage(im);
	}
	
	/**
	 * Run the function and open the graphical interface
	 * @return the ROI data
	 */
	public void doit(){
		////// Get params
		getParams();
		
		if (auto){
			preProcessLive();
			locateLive();
			analyze();
		}
		else {
			wrap.start();
		}
		return;
	}

	public void runStep(int step) {
		atStep = step;
		
		////// Get params
		getParams();
		
		///// Run step index
		JEXStatics.logManager.log("Running step "+atStep,1,this);
		imagepanel.setPointListArray(null,null);
		imagepanel.setRoi(null);
		imagepanel.setTracks(null);
		
		if (atStep == 0){
			preProcessLive();
			imagepanel.setImage(cellImPreprocessed);
			interactionMode = true;
			if (auto) {
				atStep = atStep + 1;
				runStep(atStep);
			}
		}
		else if (atStep == 1){
			locateLive();
			imagepanel.setPointList(day1_Points);
			imagepanel.setImage(cellIm);
			interactionMode = true;
			
			if (auto) {
				atStep = atStep + 1;
				runStep(atStep);
			}
		}
		else if (atStep == 2){
			analyze();
			
			interactionMode = false;
			if (auto) {
				atStep = atStep + 1;
				runStep(atStep);
			}
		}
		
		String title = "Analyzing entry "+entry.getEntryExperiment()+" - "+entry.getEntryTrayName()+" - "+entry.getTrayX()+"."+entry.getTrayY();
		wrap.setTitle(title);
	}
	public void runNext(){
		atStep = atStep+1;
		if (atStep > 2) atStep = 2;
	}
	public void runPrevious(){
		atStep = atStep-1;
		if (atStep < 0) atStep = 0;
	}
	public int getStep(){ return atStep;}
	
	public void loopNext(){
		index = 0;
		runStep(atStep);
	}
	public void loopPrevious(){
		index = 0;
		runStep(atStep);
	}
	public void recalculate(){}

	public void startIT() {
		wrap.displayUntilStep();
	}
	
	/**
	 * Apply the roi to all other images
	 */
	public void finishIT() {
		
	}
	
	/**
	 * Pre process and watershed the live image
	 */
	private void preProcessLive(){
		// if  the threshold method is used call the threshold method else use the find max method
		if (this.useThreshold)
		{
			processUsingThreshold();
		}
		else
		{
			processUsingFindMax();
		}
	}
	
	private void processUsingFindMax()
	{
		// Get the processor in the right kind
		ShortProcessor shortLive = (ShortProcessor) cellIm.getProcessor().convertToShort(true);
		JEXStatics.logManager.log("Live cell processor reached",1,this);
		
		// Background subtracter
		BackgroundSubtracter bgs = new BackgroundSubtracter();
		
		// Background subtracter parameters
		double radius1           = this.rollingBall;
		boolean createBackground = false; 
		boolean lightBackground  = false; 
		boolean useParaboloid    = false; 
		boolean doPresmooth      = false;
		boolean correctCorners   = false;

		// Perform background subtraction for both Hoechst and p65 images
		bgs.rollingBallBackground(shortLive, radius1, createBackground, lightBackground, useParaboloid, doPresmooth, correctCorners);
		JEXStatics.logManager.log("Live image rolling ball performed",1,this);
		
		// Convert ShortProcessor to ByteProcessor for watershedding
		ByteProcessor byteImPlus1 = (ByteProcessor) shortLive.convertToByte(true);
		ImagePlus tempImage       = new ImagePlus("",byteImPlus1);
		ImageStatistics stats     = tempImage.getStatistics();
		double mean = stats.mean;
		
		// Find maxima
		JEXStatics.logManager.log("Live image Finding particles",1,this);
		MaximumFinder finder = new MaximumFinder();
		ByteProcessor out = finder.findMaxima(byteImPlus1, thresh, this.mean*mean, MaximumFinder.SEGMENTED, true, false);

		if (erodedilate)
		{
			out = (ByteProcessor) out.duplicate().convertToByte(true);
			int[] kern2 = new int[] {-1,-1,-1,-1,-1,-1,-1,-1,-1};
			out.convolve3x3(kern2);
			out.convolve3x3(kern2);
			int[] kern = new int[] {1,1,1,1,1,1,1,1,1};
			out.convolve3x3(kern);
			out.convolve3x3(kern);
			out.invertLut();
			out.threshold(100);
		}
		
		// Convert ByteProcessor back to ImagePlus after watershedding is done
		cellImPreprocessed = new ImagePlus("Watershed", out.duplicate());
	}
	
	private void processUsingThreshold()
	{
		// Normalize the image
		ImagePlus im2run   = new ImagePlus("",cellIm.getProcessor().duplicate());
		FloatProcessor imp = (FloatProcessor) im2run.getProcessor().convertToFloat();
		FunctionUtility.imAdjust(imp, (int)imp.getMin(), (int)imp.getMax(), 0, 4095, 1);
		
		// Get the processor in the right kind
		ShortProcessor shortLive = (ShortProcessor) imp.convertToShort(true);
		JEXStatics.logManager.log("Live cell processor reached",1,this);
		
		// Background subtracter
		BackgroundSubtracter bgs = new BackgroundSubtracter();
		
		// Background subtracter parameters
		double radius1           = this.rollingBall;
		boolean createBackground = false; 
		boolean lightBackground  = false; 
		boolean useParaboloid    = false; 
		boolean doPresmooth      = false;
		boolean correctCorners   = false;

		// Perform background subtraction for both Hoechst and p65 images
		bgs.rollingBallBackground(shortLive, radius1, createBackground, lightBackground, useParaboloid, doPresmooth, correctCorners);
		JEXStatics.logManager.log("Live image rolling ball performed",1,this);
		
		// Convert ShortProcessor to ByteProcessor
		ByteProcessor byteImPlus1 = (ByteProcessor) shortLive.convertToByte(true);
		
		// Do the threshold
		Thresholder thresholder = new Thresholder(new ImagePlus("",byteImPlus1));
		thresholder.run(null);
		ImagePlus result = thresholder.im2run;
		ImageProcessor out = result.getProcessor();
		
		// Erode dilate to remove the small objects
		if (erodedilate){
			out.erode();
			out.erode();
			out.dilate();
			out.dilate();
		}
		
		cellImPreprocessed = new ImagePlus("Thresholded", out.duplicate());
	}
	
	/**
	 * Locate cells in the live image
	 */
	private void locateLive(){
		// Use ImageJ Particle Analyzer on data1
		int options = 0;
		int measure = ParticleAnalyzer.AREA | ParticleAnalyzer.CIRCULARITY  | ParticleAnalyzer.INTEGRATED_DENSITY
		| ParticleAnalyzer.CENTROID | ParticleAnalyzer.ELLIPSE;
		
		// Make the particle analyzer
		ResultsTable rt = new ResultsTable();
		double minSize = radius1;
		double maxSize = radius2;
		double minCirc = 0.0;
		double maxCirc = 1.0;
		ParticleAnalyzer analyzer = new ParticleAnalyzer(options, measure, rt, minSize, maxSize, minCirc, maxCirc);
		boolean done = analyzer.analyze(cellImPreprocessed);
		JEXStatics.logManager.log("Live image particle analyzer performed returned "+done,1,this);

		// Acquire the ROIs from the particle analysis and apply to the p65 image	
		List<Roi> foundRois = analyzer.foundRois;
		JEXStatics.logManager.log("Total number of rois is " + foundRois.size(),1,this);
		
		// Get the results out
		int lastColumn = rt.getLastColumn();
		float[] xPos      = new float[0] ;
		float[] yPos      = new float[0] ;
		float[] areas     = new float[0] ;
		
		for (int i=0;i<lastColumn;i++){
			String cName = rt.getColumnHeading(i);
			if (cName.equals("X")){
				xPos = rt.getColumn(i);
			}
			if (cName.equals("Y")){
				yPos = rt.getColumn(i);
			}
			if (cName.equals("Area")){
				areas = rt.getColumn(i);
			}
		}
		
		day1_Points = new PointList();
		clusterAreas    = new float[xPos.length];
		for (int i=0; i<xPos.length; i++){
			int   px = (int)xPos[i];
			int   py = (int)yPos[i];
			Point p  = new Point(px,py);
			
			if (rectroi!= null){
				boolean isInX = (px > rectroi.getX() && px < rectroi.getX() + rectroi.getWidth());
				boolean isInY = (py > rectroi.getY() && py < rectroi.getY() + rectroi.getHeight());
				if (!isInX || !isInY) continue;
			}
			
			day1_Points.add(p);
			clusterAreas[i] = areas[i];
		}
		
		// Make the live image output
		BufferedImage bim = this.plotImage(cellIm, day1_Points);
		ImagePlus lim     = new ImagePlus("",bim);
		String    limPath =JEXWriter.saveImage(lim);
		cellImage = ImageWriter.makeImageObject(outputNames[6].getName(), limPath);
	}
			
	/**
	 * Analyze proliferation and fill the variables to output
	 */
	private void analyze(){
		int totalCellNumber = 0;
		float sizeNumber    = 0;
		float totalAreaNb   = 0; 
		List<Double> histArray = new ArrayList<Double>(0);
		
		if (norm != null || singleCellArea > 0){
			for (float f: clusterAreas){
				totalAreaNb = totalAreaNb + f;
				histArray.add((double)f);
			}
			
			if (norm != null) totalCellNumber = (int) (totalAreaNb / norm);
			else              totalCellNumber = (int) (totalAreaNb / singleCellArea);
		}
		else {
			totalCellNumber = day1_Points.size(); ;
		}
		  
		sizeNumber = ArrayUtility.mean(clusterAreas);
		sizeNumber = ArrayUtility.mean(clusterAreas,(float)0.05);
		
		totalNumber = ValueWriter.makeValueObject(outputNames[0].getName(), ""+totalCellNumber);
		totalNumber.setDataObjectInfo("Total cell number found with function: 3D live dead cell location for clusters");
		
		totalArea = ValueWriter.makeValueObject(outputNames[1].getName(), ""+totalArea);
		totalArea.setDataObjectInfo("Total cell area found with function: 3D live dead cell location for clusters");
		
		day1_clusterSize = ValueWriter.makeValueObject(outputNames[2].getName(), ""+sizeNumber);
		day1_clusterSize.setDataObjectInfo("Mean cluster size found with function: 3D live dead cell location for clusters");

		// Histogram
		List<Double> linHist = HistogramFactory.makeAbscHistorgram(10, 0, (double)(3*sizeNumber));
		List<Double> hist    = HistogramFactory.getIncreasingHistogram(histArray,linHist);
		double[]     histDouble = new double[hist.size()];
		for (int i=0; i<hist.size(); i++)
		{
			histDouble[i] = hist.get(i);
		}
		histogram = ValueWriter.makeValueStack(outputNames[3].getName(), histDouble, "Size");
		
		String[] columnNames = new String[] {"Start X", "Start Y", "Area"};
		HashMap<String,String[]> columns = new HashMap<String,String[]>();
		String[] stX = new String[day1_Points.size()];
		String[] stY = new String[day1_Points.size()];
		String[] are = new String[clusterAreas.length];
		for (int i=0; i<day1_Points.size(); i++){
			Point p = day1_Points.get(i);
			stX[i]  = ""+p.x;
			stY[i]  = ""+p.y;
		}
		for (int i=0; i<clusterAreas.length; i++){
			are[i]  = ""+clusterAreas[i];
		}
		columns.put("Start X",stX);
		columns.put("Start Y",stY);
		columns.put("Area",are);
		moreInfo = ValueWriter.makeValueTable(outputNames[4].getName(), columnNames, columns);
		moreInfo.setDataObjectInfo("Additional info from function: 3D live dead cell location for clusters");
	}
	
	/**
	 * Return a buffered image with the tracks printed on the image IMPLUS
	 * in a pretty manner
	 * @param i
	 * @param implus
	 * @return
	 */
	private BufferedImage plotImage(ImagePlus implus, PointList points){
		ImageProcessor imp = implus.getProcessor();
		imp = imp.resize((int)(implus.getWidth()));

		// Create a buffered image using the default color model
		int type = BufferedImage.TYPE_INT_RGB;
		BufferedImage bimage = new BufferedImage(imp.getWidth(), imp.getHeight(), type);

		// Copy image to buffered image
		Graphics g = bimage.createGraphics();

		// Paint the image onto the buffered image
		g.drawImage(imp.getBufferedImage(), 0, 0, null);

		// plot current points
		for (int k=0, len=points.size(); k<len; k++)
		{	
			// Get point
			Point p = points.get(k);
			
			// Draw point
			g.setColor(Color.red);
			g.drawRect(p.x- (int) radius1, p.y- (int) radius1, (int) radius1*2+1, (int) radius1*2+1);
			g.drawRect(p.x- (int) radius1+1, p.y- (int) radius1-3, (int) radius1*2-1, (int) radius1*2-1);
		}
		
		g.dispose();

		return bimage;
	} 
	
	
	public void clickedPoint(Point p) {}
	public void pressedPoint(Point p) {}
	public void mouseMoved(Point p){}
	
}






