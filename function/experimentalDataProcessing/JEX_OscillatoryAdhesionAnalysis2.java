package function.experimentalDataProcessing;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.Measurements;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageStatistics;
import image.roi.ROIPlus;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import jex.statics.JEXStatics;
import utilities.CSVList;
import utilities.FunctionUtility;
import utilities.LSVList;
import utilities.StatisticsUtility;
import Database.DBObjects.JEXData;
import Database.DBObjects.JEXEntry;
import Database.DBObjects.dimension.Dim;
import Database.DBObjects.dimension.DimTable;
import Database.DBObjects.dimension.DimensionMap;
import Database.DataReader.ImageReader;
import Database.DataReader.LabelReader;
import Database.DataReader.RoiReader;
import Database.DataWriter.FileWriter;
import Database.DataWriter.ImageWriter;
import Database.Definition.Parameter;
import Database.Definition.ParameterSet;
import Database.Definition.TypeName;
import Database.SingleUserDatabase.JEXWriter;
import function.ExperimentalDataCrunch;
import function.imageUtility.AutoThresholder;


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
public class JEX_OscillatoryAdhesionAnalysis2 extends ExperimentalDataCrunch{
	
	public JEX_OscillatoryAdhesionAnalysis2(){}

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
		String result = "Oscillatory Adhesion Analysis (2)";
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
		String result = "Use the filtered images to obtain a baseline measurement for each frame to average and output as a value along with the value of the thresholded max stack projection.";
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
		String toolbox = "Custom Cell Analysis";
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
		TypeName[] inputNames = new TypeName[2];
		inputNames[0] = new TypeName(IMAGE,"Filtered Timelapse Images");
		inputNames[1] = new TypeName(ROI,"ROI");
		return inputNames;
	}
	
	/**
	 * Return the array of output names defined for this function
	 * @return
	 */
	@Override
	public TypeName[] getOutputs(){
		defaultOutputNames = new TypeName[2];
		defaultOutputNames[0] = new TypeName(IMAGE,"Difference Images");
		defaultOutputNames[1] = new TypeName(FILE,"Octave Summary File");
		
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
		Parameter p0 = new Parameter("Threshold Multiplier (optional)","Amount to multiply autothreshold value by for making binary difference images to analyze.","1.0");
//		Parameter p4 = new Parameter("New Max","Image Intensity Value","65535.0");
//		Parameter p5 = new Parameter("Gamma","0.1-5.0, value of 1 results in no change","1.0");
//		Parameter p6 = new Parameter("Output Bit Depth","Depth of the outputted image",FormLine.DROPDOWN,new String[] {"8","16","32"},1);
		
		// Make an array of the parameters and return it
		ParameterSet parameterArray = new ParameterSet();
		parameterArray.addParameter(p0);
//		parameterArray.addParameter(p1);
//		parameterArray.addParameter(p2);
//		parameterArray.addParameter(p3);
//		parameterArray.addParameter(p4);
//		parameterArray.addParameter(p5);
//		parameterArray.addParameter(p6);
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
	@SuppressWarnings("unchecked")
	public boolean run(JEXEntry entry, HashMap<String,JEXData> inputs)
	{
		// Collect the inputs
		JEXData imageData = inputs.get("Filtered Timelapse Images");
		if (imageData == null || !imageData.getTypeName().getType().equals(JEXData.IMAGE)) return false;
		DimTable originalDimTable = imageData.getDimTable().copy();
		TreeMap<DimensionMap,String> imagePaths = ImageReader.readObjectToImagePathTable(imageData);
		
		// Collect the inputs
		JEXData roiData = inputs.get("ROI");
		if (roiData == null || !roiData.getTypeName().getType().equals(JEXData.ROI)) return false;
		TreeMap<DimensionMap,ROIPlus> roiMap = RoiReader.readObjectToRoiMap(roiData);
		
		// Collect Parameters
		String dimName = "Z";
		double thresholdMultiplier = Double.parseDouble(parameters.getValueOfParameter("Threshold Multiplier (optional)"));
		
		// Collect fStart and fEnd Labels if present and return false if not;
		double fStart = 0;
		JEXData fStartData = JEXStatics.jexManager.getDataOfTypeNameInEntry(new TypeName(JEXData.LABEL,"fStart"), entry);
		if(fStartData != null)
		{
			fStart = Double.parseDouble(LabelReader.readLabelValue(fStartData));
		}
		else
		{
			JEXStatics.statusBar.setStatusText("Couldn't find fStart. Please apply label before running again.");
			return false;
		}
		double fEnd = 0;
		JEXData fEndData = JEXStatics.jexManager.getDataOfTypeNameInEntry(new TypeName(JEXData.LABEL,"fEnd"), entry);
		if(fEndData != null)
		{
			fEnd = Double.parseDouble(LabelReader.readLabelValue(fEndData));
		}
		else
		{
			JEXStatics.statusBar.setStatusText("Couldn't find fEnd. Please apply label before running again.");
			return false;
		}
		
		// Get the subDimTable (i.e. the dimtable without the different Z's)
		Dim dimToProject = originalDimTable.getDimWithName(dimName);
		if(dimToProject == null) return false;
		DimTable subDimTable = originalDimTable.copy();
		int originalDimIndex = subDimTable.indexOfDimWithName(dimToProject.name());
		subDimTable.remove(originalDimIndex);
		
		List<DimensionMap> timeMaps = subDimTable.getDimensionMaps();
		TreeMap<DimensionMap,String> imageMap = new TreeMap<DimensionMap,String>();
		TreeMap<DimensionMap,Double> threshMap = new TreeMap<DimensionMap,Double>();
		TreeMap<DimensionMap,Double> baselineMap = new TreeMap<DimensionMap,Double>();
		TreeMap<DimensionMap,List<Double>> signalMap = new TreeMap<DimensionMap,List<Double>>();
		int total = -1, index = 0, count = 0;
		Object[] baselineResults, differenceResults;
		DimensionMap baselineImage;
		Double threshold = 0.0;
		for(DimensionMap timeMap : timeMaps)
		{
			// Get maps for zstack at this time
			List<DimensionMap> zMaps = this.getAllStackMaps(timeMap, dimToProject);
			
			// Setup counters for status bar
			if(total == -1) total = timeMaps.size()*zMaps.size()*2; // 1 time through for baseline and 1 time for differencing
			count = index * zMaps.size() * 2;
			
			// Get baseline results... Object[] = {DimensionMap of image used for reference image, Double reference measurement, Double threshold}
			baselineResults = getBaseline(imagePaths, zMaps, roiMap, count, total);
			
			// Update counter information
			count = count + zMaps.size();
			
			// Use baseline information to getFinalData (Object[]{List<Double> signal, TreeMap<DimensionMap,String> savedDifferenceImages}
			baselineImage = (DimensionMap) baselineResults[0];
			threshold = ((Double) baselineResults[2])*thresholdMultiplier;
			differenceResults = getDifferencesFromBaseline(imagePaths, baselineImage, threshold, zMaps, roiMap, count, total);
			
			// Store results
			imageMap.putAll((TreeMap<DimensionMap,String>) differenceResults[1]);
			threshMap.put(timeMap,threshold);
			baselineMap.put(timeMap, (Double) baselineResults[1]);
			signalMap.put(timeMap, (List<Double>) differenceResults[0]);
			
			// Increment counter
			index = index + 1;
		}
		
		String filePath = this.writeMFile(timeMaps, baselineMap, signalMap, threshMap, fStart, fEnd);
		if(filePath == null) return false;
		
		JEXData output1 = ImageWriter.makeImageStackFromPaths(outputNames[0].getName(), imageMap);
		JEXData output2 = FileWriter.makeFileObject(outputNames[1].getName(), filePath, "CSV");
		output1.setDimTable(originalDimTable);
		output2.setDimTable(subDimTable);
		
		// Set the outputs
		realOutputs.add(output1);
		realOutputs.add(output2);
		
		// Return status
		return true;
	}
	
	/**
	 * Results are... Object[] = {DimensionMap of image used for reference image, Double reference measurement, Double threshold}
	 * @param paths
	 * @param stackMaps
	 * @param thresh
	 * @param regionToAnalyze
	 * @return
	 */
	private static Object[] getBaseline(TreeMap<DimensionMap,String> paths, List<DimensionMap> stackMaps, TreeMap<DimensionMap,ROIPlus> rois, int statusIndex, int statusTotal)
	{
		ImagePlus imAvg = null;
		FloatProcessor impAvgFloat;
		ByteProcessor impAvgByte;
		ImageStatistics stats;
		List<Double> thresholds = new Vector<Double>();
		List<Double> signals = new Vector<Double>();
		int[] histogram = null;
		Double thresh;
		int count = 0;
		function.imageUtility.AutoThresholder thresholder = new function.imageUtility.AutoThresholder();
		List<DimensionMap> baselineImages = new Vector<DimensionMap>();
		for(DimensionMap map : stackMaps)
		{
			String path = paths.get(map);
			if(path != null)
			{
				baselineImages.add(map);
				
				// Get data
				imAvg = new ImagePlus(paths.get(map));
				impAvgFloat = (FloatProcessor) imAvg.getProcessor().convertToFloat();
				FunctionUtility.imAdjust(impAvgFloat, 0, 4095, 0, 255, 1);
				impAvgByte = (ByteProcessor) impAvgFloat.convertToByte(false);
				//imAvg.setProcessor(impAvgByte);
				//imAvg.setRoi(new Rectangle(0,0,impAvgByte.getWidth(),impAvgByte.getHeight()));
					
				// Get the baseline measurement and threshold value
				histogram = impAvgByte.getHistogram();
				thresh = new Double(thresholder.getThreshold(AutoThresholder.OTSU, histogram));
				thresholds.add((thresh/255)*4095); // Threshold obtained using 8-bit version so multiply back up
				FunctionUtility.imThresh(impAvgFloat, thresh, false); // use 8-bit scale threshold here because float processor has been scaled to 8-bit
				imAvg.setProcessor(impAvgFloat);
				stats = imAvg.getStatistics(Measurements.MEAN);
				signals.add(stats.mean); // Signal is from black and white image so don't scale back up to 12-bit levels
				
				// Get rid of data
				histogram = null;
				impAvgFloat = null;
				impAvgByte = null;
				imAvg.flush();
			}
			count = count + 1;
			int percentage = (int) (100 * ((double) (statusIndex + count) / (double) statusTotal));
			JEXStatics.statusBar.setProgressPercentage(percentage);
		}
		
		// Choose the best image from the baseline
		double mean = (StatisticsUtility.mean(signals));
		int index = StatisticsUtility.nearestIndex(signals, mean);
		
		// Return the baseline information (Multiply baseline signal by 2 to get max potential signal for difference images)
		return new Object[]{baselineImages.get(index), signals.get(index)*2.0, thresholds.get(index)};
	}
	
	private String writeMFile(List<DimensionMap> timeMaps, TreeMap<DimensionMap,Double> baselineMap, TreeMap<DimensionMap,List<Double>> signalMap, TreeMap<DimensionMap,Double> threshMap, double fStart, double fEnd)
	{
		DecimalFormat formatD = new DecimalFormat("##0.000");
		CSVList baselines = new CSVList();
		CSVList thresholds = new CSVList();
		LSVList signals = new LSVList();
		CSVList times = new CSVList();
		CSVList signal = new CSVList();
		int counter = 1;
		for(DimensionMap timeMap : timeMaps)
		{
			// Get baseline info
			baselines.add(formatD.format(baselineMap.get(timeMap)));
			
			// Get threshold info
			thresholds.add(formatD.format(threshMap.get(timeMap)));
			
			// Get time info
			times.add(timeMap.get("T"));
			
			// Get the signal values.
			for(Double value : signalMap.get(timeMap))
			{
				signal.add(formatD.format(value));
			}
			signals.add("data(" + counter + ").diffStackRow = [" + signal.toString() + "];");
			signal.clear();
			counter = counter + 1;
		}

		String fStartString = "fStart = " + fStart + ";";
		String fEndString = "fEnd = " + fEnd + ";";
		String calibrationString = "cal = 100;";
		String timeString = "time = [" + times.toString() + "];";
		String baselineString = "baseline = [" + baselines.toString() + "];";
		String thresholdString = "threshold = [" + thresholds.toString() + "];";
		String signalString = signals.toString();
		
		LSVList out = new LSVList();
		out.add(fStartString);
		out.add(fEndString);
		out.add(calibrationString);
		out.add(timeString);
		out.add(baselineString);
		out.add(thresholdString);
		out.add(signalString);
		
		
		String fullPath = JEXWriter.saveText(out.toString(), "m");
		return fullPath;
	}
	
	/**
	 * Return the List<Double> that represents the average black and white intensity in the roi
	 * after thresholding for each image in the stack as well as the TreeMap<DimensionMap,String>
	 * that represents those saved difference images.
	 * @param imagePaths
	 * @param baselineImage
	 * @param threshold
	 * @param stackMaps
	 * @param roi
	 * @return
	 */
	private Object[] getDifferencesFromBaseline(TreeMap<DimensionMap,String> imagePaths, DimensionMap baselineImage, Double threshold, List<DimensionMap> stackMaps, TreeMap<DimensionMap,ROIPlus> roiMap, int statusIndex, int total)
	{
		String baselinePath = imagePaths.get(baselineImage);
		if(baselinePath == null) return null;
		FloatProcessor baselineP = (FloatProcessor) (new ImagePlus(baselinePath).getProcessor().convertToFloat());
		FunctionUtility.imThresh(baselineP, threshold, false);
		float[] bg = (float[]) baselineP.getPixels();
		baselineP = null;
		
		TreeMap<DimensionMap,String> dataMap = new TreeMap<DimensionMap,String>();
		ImagePlus temp;
		FloatProcessor tempP;
		ByteProcessor tempByteP;
		float[] tempPix;
		String tempPath;
		ImageStatistics stats;
		List<Double> imDiffs = new Vector<Double>(0);
		int count = 0, percentage = 0;
		for(DimensionMap stackMap : stackMaps)
		{
			String path = imagePaths.get(stackMap);
			if(path != null)
			{
				// Get Data
				temp = new ImagePlus(path);
				tempP = (FloatProcessor) temp.getProcessor().convertToFloat();
				FunctionUtility.imThresh(tempP, threshold, false);
				tempPix = (float[]) tempP.getPixels();
				for (int p=0; p<bg.length; p++)
				{
					tempPix[p] = Math.abs(tempPix[p]-bg[p]);
				}
				tempByteP = (ByteProcessor) tempP.convertToByte(false);
				
				// Save the data
				tempPath = JEXWriter.saveImage(tempByteP);
				
				if(tempPath != null)
				{
					dataMap.put(stackMap, tempPath);
				}
				
				// Get the difference signal;
				ROIPlus roip = roiMap.get(stackMap);
				Roi imageJRoi = roip.getRoi();
				tempP.setRoi(imageJRoi);
				temp.setProcessor(tempP);
				stats = temp.getStatistics(Measurements.MEAN);
				imDiffs.add(stats.mean);
				
				// Get rid of data
				tempPix = null;
				tempP = null;
				tempByteP = null;
				temp.flush();
			}
			count = count + 1;
			percentage = (int) (100 * ((double) (statusIndex + count)/ (double) total));
			JEXStatics.statusBar.setProgressPercentage(percentage);
		}
		bg = null;
		baselineP = null;
		
		return new Object[]{imDiffs, dataMap};
	}
	
//	/**
//	 * Saves the image in the database temp folder
//	 * @param imp
//	 * @param bitDepth
//	 * @return
//	 */
//	private static String saveImage(FloatProcessor imp, int bitDepth)
//	{
//		ImagePlus toSave = FunctionUtility.makeImageToSave(imp, "false", bitDepth);
//		String finalPath = JEXWriter.saveImage(toSave);
//		
//		return finalPath;
//	}
//	
//	private static FloatProcessor evaluate(ImagePlus virtualStack, String method, int stackSize)
//	{
//		//:,mean,max,min,sum,std. dev.,median"///
//		int methodInt = 5;
//		if(method.equals("mean")) methodInt = 0;
//		else if(method.equals("max")) methodInt = 1;
//		else if(method.equals("min")) methodInt = 2;
//		else if(method.equals("sum")) methodInt = 3;
//		else if(method.equals("std. dev.")) methodInt = 4;
//		else if(method.equals("median")) methodInt = 5;
//		
//		ZProjector p = new ZProjector(virtualStack);
//		p.setStartSlice(1);
//		p.setStopSlice(stackSize);
//		p.setMethod(methodInt);
//		p.doProjection();
//		return (FloatProcessor) p.getProjection().getProcessor().convertToFloat();
//	}
	
//	private static ImagePlus getVirtualStack(TreeMap<DimensionMap, String> imagePaths, List<DimensionMap> maps)
//	{
//		if(maps.size() == 0 || imagePaths.size() == 0) return null;
//		String firstPath = imagePaths.firstEntry().getValue();
//		ImagePlus im = new ImagePlus(firstPath);
//		File temp = new File(firstPath);
//		VirtualStack stack = new VirtualStack(im.getWidth(), im.getHeight(), im.getProcessor().getColorModel(), temp.getParent());
//		for(DimensionMap map : maps)
//		{
//			String path = imagePaths.get(map);
//			if(path != null)
//			{
//				temp = new File(imagePaths.get(map));
//				stack.addSlice(temp.getName());
//			}
//		}
//		ImagePlus ret = new ImagePlus(temp.getPath() + "... ", stack);
//		return ret;
//	}
	
	private List<DimensionMap> getAllStackMaps(DimensionMap map, Dim dimToProject)
	{
		List<DimensionMap> ret = new Vector<DimensionMap>();
		for(int i = 0; i < dimToProject.size(); i++)
		{
			ret.add(this.getAStackMap(map, dimToProject, i));
		}
		return ret;
	}
	
	private DimensionMap getAStackMap(DimensionMap map, Dim dimToProject, int indexOfValue)
	{
		DimensionMap ret = map.copy();
		ret.put(dimToProject.name(), dimToProject.valueAt(indexOfValue));
		return ret;
	}
}


