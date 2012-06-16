package function.experimentalDataProcessing;

import java.util.HashMap;
import java.util.TreeMap;

import jex.statics.JEXStatics;
import weka.core.converters.JEXTableReader2;
import Database.DBObjects.JEXData;
import Database.DBObjects.JEXEntry;
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
public class JEX_SplitARFFTable extends ExperimentalDataCrunch{
	
	public JEX_SplitARFFTable(){}

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
		String result = "Split ARFF Table";
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
		String result = "Split ARFF Tables along a particular dimension.";
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
		
		Parameter p0 = new Parameter("Dimension To Split","Name of the dimension to split.","Time");
		Parameter p1 = new Parameter("File Extension", "Extension of the filename", "arff");
//		Parameter p2 = new Parameter("Data Type","Is it numeric or string data in the table?",FormLine.DROPDOWN,new String[]{"Numeric","String"},0);
//		Parameter p3 = new Parameter("Copy Locally First?","Should each file be copied locally first to avoid file transfer times?",FormLine.DROPDOWN,new String[]{"True","False"},0);
		
		// Make an array of the parameters and return it
		ParameterSet parameterArray = new ParameterSet();
		parameterArray.addParameter(p0);
		parameterArray.addParameter(p1);
//		parameterArray.addParameter(p2);
//		parameterArray.addParameter(p3);
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
		try
		{
			// Collect the inputs
			JEXData fileData = inputs.get("ARFF Files");
			if (fileData == null || !fileData.getTypeName().getType().equals(JEXData.FILE)) return false;
			
			// Gather parameters
			String splitDimName = parameters.getValueOfParameter("Dimension To Split");
			String fileExtension = parameters.getValueOfParameter("File Extension");
//			Boolean copyLocally = Boolean.parseBoolean(parameters.getValueOfParameter("Copy Locally First?"));
			DimTable fileTable = fileData.getDimTable();
			
			// Run the function
			TreeMap<DimensionMap,String> fileMap = FileReader.readObjectToFilePathTable(fileData);
			TreeMap<DimensionMap,String> outputFileData = new TreeMap<DimensionMap,String>();
			int count = 0, percentage = 0;
			double total = fileTable.mapCount();
			JEXStatics.statusBar.setProgressPercentage(0);
			for(DimensionMap map1 : fileTable.getIterator())
			{
				Table<String> tempFileData = JEXTableReader2.splitTable(fileMap.get(map1), splitDimName, "SplitTable", fileExtension);
				if(tempFileData == null)
				{
					return false;
				}
				for(DimensionMap splitDimMap : tempFileData.dimTable.getIterator())
				{
					DimensionMap newMap = map1.copy();
					newMap.put(splitDimName, splitDimMap.get(splitDimName));
					outputFileData.put(newMap, tempFileData.getData(splitDimMap));
				}
				count = count + 1;
				percentage = (int) (100 * ((double) (count)/ ((double) total)));
				JEXStatics.statusBar.setProgressPercentage(percentage);
				
//				count = 0;
//				String masterFilePath = fileMap.get(map1);
////				if(copyLocally)
////				{
////					String temp = JEXWriter.getUniqueTempPath("arff");
////					FileUtility.copy(new File(masterFilePath), new File(temp));
////					masterFilePath = temp;
////				}
//				DimTable arffDimTable = JEXTableReader.getDimTable(masterFilePath);
//				Dim splitDim = arffDimTable.getDimWithName(splitDimName);
//				DimTable splitTable = new DimTable();
//				splitTable.add(splitDim);
//				TreeMap<String,JEXTableWriter> writers = new TreeMap<String,JEXTableWriter>();
//				for(String newDim : splitDim.dimValues)
//				{
//					writers.put(newDim, new JEXTableWriter(outputNames[0].getName()));
//				}
//				total = splitTable.mapCount();
//				for(DimensionMap map2 : splitTable.getIterator())
//				{
//					String path = null;
//					if(dataType.equals("Numeric"))
//					{
//						Table<Double> table = JEXTableReader.getNumericTable(masterFilePath, map2);
//						path = JEXTableWriter.writeTable(outputNames[0].getName(), table, fileExtension);
//					}
//					else
//					{
//						Table<String> table = JEXTableReader.getStringTable(masterFilePath, map2);
//						path = JEXTableWriter.writeTable(outputNames[0].getName(), table, fileExtension);
//					}
//					DimensionMap newDim = new DimensionMap();
//					newDim.putAll(map1);
//					newDim.putAll(map2);
//					outputFileData.put(newDim, path);
//					
//					count = count + 1;
//					percentage = (int) (100 * ((double) (count)/ ((double) total)));
//					JEXStatics.statusBar.setProgressPercentage(percentage);
//				}
			}
			if(outputFileData.size() == 0)
			{
				return false;
			}
			JEXData output1 = FileWriter.makeFileTable(outputNames[0].getName(), outputFileData);
			
			// Set the outputs
			realOutputs.add(output1);
			
			// Return status
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}	

}




