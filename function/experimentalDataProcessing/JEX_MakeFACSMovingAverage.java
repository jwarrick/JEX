package function.experimentalDataProcessing;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import jex.statics.JEXStatics;
import utilities.DataWindow;
import utilities.DataWindows;
import utilities.Pair;
import weka.core.converters.JEXTableReader2;
import weka.core.converters.JEXTableWriter2;
import Database.DBObjects.JEXData;
import Database.DBObjects.JEXEntry;
import Database.DBObjects.dimension.Dim;
import Database.DBObjects.dimension.DimTable;
import Database.DBObjects.dimension.DimensionMap;
import Database.DBObjects.dimension.Table;
import Database.DataReader.FileReader;
import Database.DataWriter.FileWriter;
import Database.Definition.Parameter;
import Database.Definition.ParameterSet;
import Database.Definition.TypeName;
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
public class JEX_MakeFACSMovingAverage extends ExperimentalDataCrunch{
	
	public JEX_MakeFACSMovingAverage(){}

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
		String result = "Make FACS Moving Average";
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
		String result = "Make an ARFF file of the moving average of tracked expression information.";
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
		String toolbox = "Table Tools";
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
		inputNames[0] = new TypeName(FILE,"ARFF Files");
		return inputNames;
	}
	
	/**
	 * Return the array of output names defined for this function
	 * @return
	 */
	@Override
	public TypeName[] getOutputs(){
		defaultOutputNames = new TypeName[1];
		defaultOutputNames[0] = new TypeName(FILE,"ARFF Files");
		
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
		// Octave file is the following
		// [outPath] = makeFACSPlot(inPath, outPath, colorName, colorx, xmin, xmax, xbins, colory, ymin, ymax, ybins)
		
//		Parameter p0 = new Parameter("Working Directory","Desired R working directory","/Users/warrick/Desktop/R Scripts/CellTracking");
//		Parameter p1 = new Parameter("Octave Binary File","Location of the Octave binary file","/Applications/Octave.app/Contents/Resources/bin/octave");
		Parameter p2 = new Parameter("'Track' Dim Name","Name of 'Track' dim","Track");
		Parameter p4 = new Parameter("'Color' Dim Name","Name of 'Color' dim","Color");
		Parameter p5 = new Parameter("'X Color' Value","Color to plot on x axis","1");
		Parameter p6 = new Parameter("'Y Color' Value","Color to plot on y axis","2");
		Parameter p7 = new Parameter("Number of Frames","Size of the rolling window in number of frames","10");
//		Parameter p8 = new Parameter("Number of Frames, Rates","Size of the rolling window in number of frames for calculating rates from running averages","3");
//		Parameter p9 = new Parameter("Minutes Between Frames","Number of minutes between each frame","5");
//		Parameter p9 = new Parameter("Plot Height","Height of plot in pixels","1200");
//		Parameter p4 = new Parameter("X Ticks","Values at which to place tick marks","1,10,100,1000");
//		Parameter p5 = new Parameter("X Label","Label of the X axis","Antiviral Defense Activity [au]");
//		Parameter p6 = new Parameter("Y Color","Color to be plotted on Y axis","2");
//		Parameter p7 = new Parameter("Y Ticks","Values at which to place tick marks","1,10,100,1000,10000");
//		Parameter p8 = new Parameter("Y Label","Label of the Y axis","Viral Activity [au]");
//		Parameter p9 = new Parameter("Width","Pixel width of plot","1500");
//		Parameter p10 = new Parameter("Height","Pixel height of plot","1200");
//		Parameter p13 = new Parameter("Measurement Name", "Name of the measurement attribute","Measurement");
//		Parameter p14 = new Parameter("Measurement Value", "The measurement to plot (Measure Points Roi - 1:Mean, 2:Area, 3:Min, 4:Max, 5:StdDev, 6:Median)", "1");
		
		// Make an array of the parameters and return it
		ParameterSet parameterArray = new ParameterSet();
//		parameterArray.addParameter(p0);
//		parameterArray.addParameter(p1);
		parameterArray.addParameter(p2);
//		parameterArray.addParameter(p3);
		parameterArray.addParameter(p4);
		parameterArray.addParameter(p5);
		parameterArray.addParameter(p6);
		parameterArray.addParameter(p7);
//		parameterArray.addParameter(p8);
//		parameterArray.addParameter(p9);
//		parameterArray.addParameter(p10);
//		parameterArray.addParameter(p11);
//		parameterArray.addParameter(p12);
//		parameterArray.addParameter(p13);
//		parameterArray.addParameter(p14);
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

	public LinkedList<Pair<String,DimTable>> dimTables = new LinkedList<Pair<String,DimTable>>();
	public TreeMap<DimensionMap,Double> data = new TreeMap<DimensionMap,Double>();
	
	/**
	 * Perform the algorithm here
	 * 
	 */
	@Override
	public boolean run(JEXEntry entry, HashMap<String,JEXData> inputs)
	{
		// Collect the inputs
		JEXData fileData = inputs.get("ARFF Files");
		if (fileData == null || !fileData.getTypeName().getType().equals(JEXData.FILE))
		{
			return false;
		}
		
		// Gather parameters
		String trackDimName = parameters.getValueOfParameter("'Track' Dim Name");
		String colorDimName = parameters.getValueOfParameter("'Color' Dim Name");
		String xColor = parameters.getValueOfParameter("'X Color' Value");
		String yColor = parameters.getValueOfParameter("'Y Color' Value");
		Integer avgFrames = Integer.parseInt(parameters.getValueOfParameter("Number of Frames"));
		
		// Run the function
		TreeMap<DimensionMap,String> outputFileTreeMap = new TreeMap<DimensionMap,String>();
		TreeMap<DimensionMap,String> fileTreeMap = FileReader.readObjectToFilePathTable(fileData);
		DimTable fileTable = fileData.getDimTable();
		Dim timeDim = fileTable.get(0);
		DataWindows tracks = new DataWindows(avgFrames);
		Dim trackDim = null;
		int count = 0, percentage = 0;
		JEXStatics.statusBar.setProgressPercentage(0);
		for(DimensionMap fileMap : fileTable.getIterator())
		{
			Table<Double> timePoint = JEXTableReader2.getNumericTable(fileTreeMap.get(fileMap));
			if(trackDim == null)
			{
				trackDim = timePoint.dimTable.getDimWithName(trackDimName).copy();
			}
			List<DimensionMap> xMaps = timePoint.dimTable.getDimensionMaps(new DimensionMap("Measurement=1," + colorDimName + "=" + xColor));
			List<DimensionMap> yMaps = timePoint.dimTable.getDimensionMaps(new DimensionMap("Measurement=1," + colorDimName + "=" + yColor));
			tracks.increment();
			int count2 = 0, percentage2 = 0;
			JEXStatics.statusBar.setStatusText("Indexing Data: 0%");
			for(int t = 0; t < xMaps.size(); t++)
			{
				DimensionMap xMap = xMaps.get(t);
				DimensionMap yMap = yMaps.get(t);
				Integer xTrack = Integer.parseInt(xMap.get(trackDimName));
				Integer yTrack = Integer.parseInt(yMap.get(trackDimName));
				if(!xTrack.equals(yTrack))
				{
					JEXStatics.logManager.log("Whoa there. Track numbers aren't matching.", 0, this);
					return false;
				}
				Double x = timePoint.getData(xMap);
				Double y = timePoint.getData(yMap);
				if(x == null || y == null || x.isNaN() || y.isNaN())
				{
					tracks.removeWindow(xTrack);
				}
				else
				{
					tracks.addPoint(xTrack, x, y);
				}
				count2 = count2 + 1;
				percentage2 = (int)(100*((double)count2)/((double)xMaps.size()));
				JEXStatics.statusBar.setStatusText("Indexing Data: " + percentage2 + "%");
			} // Note that all tracks consist of consecutive timepoints
			
			// Write the avg data, save it as a table, and record the path and timestamp in the outputFileTreeMap
			Pair<DimensionMap,String> resultsTable = writeAvgTimePoint(tracks, trackDim, timeDim, "##0.00");
			if(resultsTable != null)
			{
				outputFileTreeMap.put(resultsTable.p1, resultsTable.p2);
			}
			
			count = count + 1;
			percentage = (int) (100 * ((double) (count)/ ((double) fileTable.mapCount())));
			JEXStatics.statusBar.setProgressPercentage(percentage);
		}
		if(outputFileTreeMap.size() == 0)
		{
			return false;
		}
		
		// Save the output images in the database
		JEXData output1 = FileWriter.makeFileTable(outputNames[0].getName(), outputFileTreeMap);
		realOutputs.add(output1);
		
		// Return status
		return true;
	}
	
	public Pair<DimensionMap,String> writeAvgTimePoint(DataWindows tracks, Dim trackDim, Dim timeDim, String timeFormat)
	{
		// "##0.00" time format
		TreeMap<DimensionMap,Double> avgData = new TreeMap<DimensionMap,Double>();
		DimensionMap dataPointMap = new DimensionMap();
		DecimalFormat formatD = new DecimalFormat(timeFormat);
		int count = 0, percentage = 0, total = trackDim.size();
		JEXStatics.statusBar.setStatusText("Calculating: 0%");
		for(String trackNumString : trackDim.dimValues)
		{
			Integer trackNum = Integer.parseInt(trackNumString);
			DataWindow track = tracks.getWindow(trackNum);
			if(track != null && track.isFilled())
			{
				dataPointMap.put(trackDim.name(), trackNumString);
				dataPointMap.put("Measurement", "x");
				avgData.put(dataPointMap.copy(), track.avgX());
				dataPointMap.put("Measurement", "y");
				avgData.put(dataPointMap.copy(), track.avgY());
			}
			count = count + 1;
			percentage = (int)(100*((double)count)/((double)total));
			JEXStatics.statusBar.setStatusText("Calculating: " + percentage + "%");
		}
		if(avgData.size() == 0)
		{
			return null;
		}

		// create the data table for writing
		DimTable newDimTable = new DimTable();
		newDimTable.add(trackDim.copy());
		Dim measurementDim = new Dim("Measurement",new String[]{"x","y"});
		newDimTable.add(measurementDim);
		Table<Double> table = new Table<Double>(newDimTable,avgData);

		// write the data and store the path and time stamp
		String tablePath = JEXTableWriter2.writeTable("AvgData", table);
		DimensionMap retDim = new DimensionMap();
		Double startFrame = Double.parseDouble(timeDim.dimValues.get(0));
		retDim.put(timeDim.name(), formatD.format(startFrame + tracks.getAvgIndex()));
		return new Pair<DimensionMap,String>(retDim,tablePath);
	}
}