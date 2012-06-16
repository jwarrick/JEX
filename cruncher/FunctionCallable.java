package cruncher;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import jex.statics.JEXStatics;
import Database.DBObjects.JEXData;
import Database.DBObjects.JEXEntry;
import function.ExperimentalDataCrunch;

public class FunctionCallable implements Callable<Integer> {
	JEXFunction             function;
	JEXEntry                entry;
	HashMap<String,JEXData> inputs;
	Set<JEXData>            outputData;

	public FunctionCallable(JEXFunction function, JEXEntry entry, HashMap<String,JEXData> inputs) { 
		this.function = function;
		this.entry    = entry;
		this.inputs   = inputs;
	}

	public Integer call() throws Exception {  
		
		// Run the function for this FunctionCallable's entry.
		outputData = new HashSet<JEXData>();
		
		try
		{
			// Function run
			ExperimentalDataCrunch cr = function.getCrunch();
			
			// Run the JEXFunction
			outputData = run(entry,function);
			
			// Finalize entry
			cr.finalizeEntry();
		}
		catch(Exception e)
		{
			JEXStatics.statusBar.setStatusText("Error running " + this.function.getFunctionName() + " on entry " + this.entry.getEntryTrayName() + ": " + this.entry.getTrayX() + "," + this.entry.getTrayY());
			e.printStackTrace();
			return 0;
		}
		
		return 1;
	}
	
	/**
	 * Return the data objects outputted by the function
	 * @return
	 */
	public Set<JEXData> getOutputtedData()
	{
		return this.outputData;
	}
	
	/**
	 * Returns the function object used to run this function
	 * @return
	 */
	public JEXFunction getFunctionObject()
	{
		return this.function;
	}
	
	/**
	 * Returns the entry on which this function was run
	 * @return
	 */
	public JEXEntry getEntry()
	{
		return this.entry;
	}
	
	private Set<JEXData> run(JEXEntry entry, JEXFunction func)
	{
		JEXStatics.logManager.log("Running the function",1,this);
		HashSet<JEXData> result = func.run(entry, inputs);
		return result;
	}
}
