package function.experimentalDataProcessing;

import java.util.HashMap;

import Database.DBObjects.JEXData;
import Database.DBObjects.JEXEntry;
import Database.DataReader.ValueReader;
import Database.DataWriter.ValueWriter;
import Database.Definition.Parameter;
import Database.Definition.ParameterSet;
import Database.Definition.TypeName;
import cruncher.Ticket;
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
public class JEX_CTCsPerML extends ExperimentalDataCrunch{
	
	//public static TreeMap<DimensionMap,String> valueMap = null;
	
	public JEX_CTCsPerML(){}

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
		String result = "CTCs Per mL Calculator";
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
		String result = "Function that takes cell counts and converts them into cells per mL";
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
		String toolbox = "CTC Tools";
		return toolbox;
	}

	/**
	 * This method defines if the function appears in the list in JEX
	 * It should be set to true except if you have good reason for it
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
		TypeName[] inputNames = new TypeName[4];
		inputNames[0] = new TypeName(VALUE,"EpCAM Count");
		inputNames[1] = new TypeName(VALUE,"Spiked Cell Count");
		inputNames[2] = new TypeName(VALUE,"mL equivalent seeded");
		inputNames[3] = new TypeName(VALUE,"mL whole blood to start");
		return inputNames;
	}
	
	/**
	 * Return the array of output names defined for this function
	 * @return
	 */
	@Override
	public TypeName[] getOutputs(){
		defaultOutputNames = new TypeName[2];
		defaultOutputNames[0] = new TypeName(VALUE,"CTCs per mL");
		defaultOutputNames[1] = new TypeName(VALUE,"Spiked Efficiency Percent");
		
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
		Parameter p0 = new Parameter("dummy paramater","dummy", FormLine.DROPDOWN,new String[]{"Yup","Duh"});
//		Parameter p1 = new Parameter("uL for mRNA","Separate values with a comma","");
//		Parameter p2 = new Parameter("uL for EPISpot","Separate values with a comma","");
//		Parameter p3 = new Parameter("mL whole blood to start","Separate values with a comma","");
//		Parameter p4 = new Parameter("Rows","Number of rows.","2");
//		Parameter p5 = new Parameter("Cols","Number of columns.","1");
//		Parameter p6 = new Parameter("Order","Fill row then go to next column (Row) or fill column and then go to next row (Column)",FormLine.DROPDOWN,new String[]{"Row","Column"});
//		Parameter p7 = new Parameter("Came from which tube?","Separate values with a comma","1,2");
		
		// Make an array of the parameters and return it
		ParameterSet parameterArray = new ParameterSet();
		parameterArray.addParameter(p0);
//		parameterArray.addParameter(p1);
//		parameterArray.addParameter(p2);
//		parameterArray.addParameter(p3);
//		parameterArray.addParameter(p4);
//		parameterArray.addParameter(p5);
//		parameterArray.addParameter(p6);
//		parameterArray.addParameter(p7);
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
	public boolean run(JEXEntry entry, HashMap<String,JEXData> inputs)
	{
		// Run the function
		//EpCAM Counts
		JEXData epcam = inputs.get("EpCAM Count");
		if(!epcam.getTypeName().getType().equals(JEXData.VALUE) ) return false;

		//GFP Counts
		JEXData gfp = inputs.get("Spiked Cell Count");
		if(!gfp.getTypeName().getType().equals(JEXData.VALUE) ) return false;
		
		//mL Whole Blood Equivalents
		JEXData blood = inputs.get("mL equivalent seeded");
		if(blood == null || !blood.getTypeName().getType().equals(JEXData.VALUE) ) return false;
		
		//mL whole blood to start
		JEXData start = inputs.get("mL whole blood to start");
		if(start == null || !start.getTypeName().getType().equals(JEXData.VALUE) ) return false;
		
			
		//This is the meat
		
		//dummy variables
		double ctc = 0;
		double spiked = 0;
		double added = 1500;
		String whole = ValueReader.readValueObject(blood);
		String starts = ValueReader.readValueObject(start);
		double mls = Double.parseDouble(whole);
		double bloodvol = Double.parseDouble(starts);
		
		//CTCs first
		if(epcam != null)
		{
			ctc = Double.parseDouble(ValueReader.readValueObject(epcam))/mls;			
		}
		if(gfp != null)
		{
			spiked = (Double.parseDouble(ValueReader.readValueObject(gfp))/mls)/(added/bloodvol)*100;
		}

		
		
		// devices holds all
		JEXData output1 = ValueWriter.makeValueObject(outputNames[0].getName(), ""+ctc);
		JEXData output2 = ValueWriter.makeValueObject(outputNames[1].getName(), ""+spiked);
		
		// Set the outputs
		realOutputs.add(output1);
		realOutputs.add(output2);
		
		// Return status
		return true;
	}
	@Override
	public void finalizeTicket(Ticket t)
	{
		//valueMap = null;
	}
}


