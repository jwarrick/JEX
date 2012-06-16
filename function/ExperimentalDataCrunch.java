package function;

import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import jex.statics.JEXStatics;
import Database.DBObjects.JEXData;
import Database.DBObjects.JEXEntry;
import Database.Definition.Parameter;
import Database.Definition.ParameterSet;
import Database.Definition.TypeName;
import cruncher.Ticket;


public abstract class ExperimentalDataCrunch {
	// Several statics
	public static int INPUTERROR = -1;
	public static int OUTPUTERROR = -2;
	public static int PARAMETERERROR = -3;
	public static int INPUTSOK = 1;
	public static int OUTPUTSOK = 2;
	public static int PARAMETERSOK = 3;
	public static int READYTORUN = 10;
	
	// Convenience data types
	public static String IMAGE     = JEXData.IMAGE;
	public static String FILE      = JEXData.FILE;
	public static String MOVIE     = JEXData.MOVIE;
	public static String SOUND     = JEXData.SOUND;
	public static String VALUE     = JEXData.VALUE;
	public static String LABEL     = JEXData.LABEL;
	public static String FUNCTION  = JEXData.FUNCTION_OLD;
	public static String ROI       = JEXData.ROI;
	public static String HIERARCHY = JEXData.HIERARCHY;
	public static String TRACK     = JEXData.TRACK;
	
	// Class variables
	protected TreeMap<String,TypeName> inputs;
	protected TypeName[] outputNames;
	protected TypeName[] defaultOutputNames;
	protected ParameterSet parameters;
	protected HashSet<JEXData> realOutputs = new HashSet<JEXData>();
	
	protected int inputStatus = INPUTERROR;
	protected int parameterStatus = PARAMETERERROR;
	protected int outputStatus = OUTPUTERROR;	
	
	// Class methods required to be written by the user in the function template
	public abstract boolean showInList() ;
	public abstract boolean isInputValidityCheckingEnabled() ;
	
	public abstract String getName() ;
	public abstract String getInfo() ;
	public abstract String getToolbox() ;
	
	public abstract TypeName[] getInputNames() ;
	public abstract ParameterSet requiredParameters() ;
	
	// run before starting
	public void prepareEntry(){
		realOutputs = new HashSet<JEXData>();
	}
	
	// run after ending
	public void finalizeEntry(){
		JEXStatics.statusBar.setProgressPercentage(0);
	}
	
	public void prepareTicket()
	{
		
	}
	
	public void finalizeTicket(Ticket ticket)
	{
		
	}
	
	/**
	 * Set and check the inputs of this function
	 * @param inputs
	 * @return an integer status of the input setting
	 */
	public int setInputs(TreeMap<String,TypeName> inputs){
		this.inputs = inputs;
		this.inputStatus = INPUTERROR;
		
		if (inputs.size() != getInputNames().length) {
			this.inputStatus = INPUTERROR;
			return INPUTERROR;
		}
		if (isInputValidityCheckingEnabled()) {
			for (TypeName tn: inputs.values()){
				if (tn == null) JEXStatics.logManager.log("This input is not set",1,this); 
				else JEXStatics.logManager.log("Set input name:"+tn.getName()+" type:"+tn.getType()+" dimension:"+tn.getDimension(),1,this);
			}
			this.inputStatus = checkInputs();
		}
		
		return inputStatus;
	}
	
	/**
	 * Set the input of a given name
	 * @param inputName
	 * @param input
	 * @return an integer status of the input setting
	 */
	public int setInput(String inputName, TypeName input){
		this.inputStatus = INPUTERROR;
		inputs.put(inputName, input);
		
		if (inputs.size() != getInputNames().length) {
			this.inputStatus = INPUTERROR;
			return INPUTERROR;
		}
		if (isInputValidityCheckingEnabled()) {
			checkInputs();
		}
		
		return inputStatus;
	}
	
	/**
	 * Return the set input list for this function
	 * @return
	 */
	public TreeMap<String,TypeName> getInputs(){
		return this.inputs;
	}
	
	/**
	 * Set and check the parameters of this function
	 * @param parameters
	 * @returnan integer status of the parameters of the function
	 */
	public int setParameters(ParameterSet parameters){
		this.parameters = parameters;
		
		ParameterSet params = requiredParameters();
		for (Parameter param: params.getParameters()){
			String p = parameters.getValueOfParameter(param.title);
			if (p == null) return PARAMETERERROR;
		}
		
		return PARAMETERSOK;
	}
	
	/**
	 * Check the inputs and runability of the function
	 * @return
	 */
	public int checkInputs(){
		
		return INPUTSOK;
	}
	
	/**
	 * Set the names of all the outputs
	 * @param outputNames
	 * @return An integer status of output name setting
	 */
	public void setOutputs(TreeMap<Integer,TypeName> outputs)
	{
		if (this.outputNames == null)
		{
			this.outputNames = this.getOutputs();
		}
		for (int i=0; i<outputNames.length; i++)
		{
			TypeName tn = this.outputNames[i];
			tn.setName(outputs.get(i).getName());
		}
	}
	
	/**
	 * Return the array of output names defined for this function
	 * @return
	 */
	public abstract TypeName[] getOutputs();
	
	/**
	 * Returns true if the user wants to allow multithreding
	 * @return
	 */
	public abstract boolean allowMultithreading();
	
	/**
	 * Set the progress indicator of this function
	 * @param progress
	 */
	public void setProgress(int progress)
	{
		
	}
	
	/**
	 * Return an array of output data objects for this function
	 * @return Outputs of this array
	 */
	public HashSet<JEXData> getRealOutputs(){
		return this.realOutputs;
	}
	
	public abstract boolean run(JEXEntry entry, HashMap<String,JEXData> inputs);
}
