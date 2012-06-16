package cruncher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import jex.statics.JEXStatics;
import Database.DBObjects.JEXData;
import Database.DBObjects.JEXEntry;
import Database.Definition.ParameterSet;
import Database.Definition.TypeName;
import function.ExperimentalDataCrunch;

public class Ticket implements Callable<Integer> {
	
	TreeMap<JEXEntry,JEXFunction> runList;
	List<Future<Integer>> futures;
	ExperimentalDataCrunch cr;
	ParameterSet firstParamSet;
	TreeMap<JEXEntry,Set<JEXData>> outputList;
	
	public boolean autoSave;
	
	public Ticket(TreeMap<JEXEntry, JEXFunction> runList){
		
		this.runList = runList;
		
		/////////////////////////////////////////////
		// Initialize Ticket with function information
		// So later edits in gui don't affect ticket
		/////////////////////////////////////////////
		
		// Get the experimental data crunch
		JEXFunction func = runList.firstEntry().getValue();
		cr = func.getCrunch();
		
		// Gather the paramset for sharing if necessary
		if(firstParamSet == null)
		{
			firstParamSet = func.getParameters();
		}
		
		// Initialize output list
		outputList   = new TreeMap<JEXEntry,Set<JEXData>>();
		
	}
	
	public void setAutoSave(boolean autoSave)
	{
		this.autoSave = autoSave;
	}
	
	public boolean getAutoSave()
	{
		return this.autoSave;
	}
	
	public TreeMap<JEXEntry,Set<JEXData>> getOutputList()
	{
		return this.outputList;
	}
	
	private void setOutputtedData(TreeMap<JEXEntry,Set<JEXData>> outputList)
	{
		this.outputList = outputList;	
	}
	
	public Integer call() throws Exception {
		
		JEXStatics.logManager.log("Running new ticket ",1,this);
		
		// If the experimental data crunch is null then stop right here
		if (cr == null) return 0;
		
		// Gather inputs and submit the functioncallables to the executor service
		futures = new ArrayList<Future<Integer>>(0);
		TreeMap<JEXEntry,FunctionCallable> fcs = new TreeMap<JEXEntry,FunctionCallable>();
		for (JEXEntry entry: runList.keySet())
		{
			if(JEXStatics.cruncher.stopCrunch == true)
			{
				JEXStatics.cruncher.stopCrunch = false;
				JEXStatics.cruncher.finishTicket(null);
				return 0;
			}
			JEXStatics.logManager.log("Submitting new function to the cruncher",1,this);
			JEXStatics.statusBar.setStatusText("Submitting new function to the cruncher");
			
			JEXFunction func = runList.get(entry);
			
			// Allow functions to share a paramset if they are not multithreaded
			// Needed for semiManual actions.
			if(!cr.allowMultithreading())
			{
				func.setParameters(firstParamSet);
			}
			
			// Gather the inputs and run
			// We gather inputs in the ticket because upon running we want to
			// grab inputs that may have been created with a ticket that was
			// submitted at the "same time" as this ticket
			FunctionCallable fc = this.getFunctionCallable(func, entry);
			JEXStatics.logManager.log("Running entry: " + entry.toString(), 0, this);
			if(fc != null)
			{
				fcs.put(entry, fc);
				Future<Integer> future = JEXStatics.cruncher.runFunction(fc,cr.allowMultithreading());
				futures.add(future);
			}
		}
		
		// Collect outputs and wait for them to finish
		int done = 0;  
		for (Future<Integer> future: futures)
		{
			if(JEXStatics.cruncher.stopCrunch == true)
			{
				JEXStatics.cruncher.stopCrunch = false;
				JEXStatics.cruncher.finishTicket(null);
				return 0;
			}
			done += future.get();
		}
		
		// Collect outputted data
		TreeMap<JEXEntry,Set<JEXData>> output = new TreeMap<JEXEntry,Set<JEXData>>();
		for (JEXEntry entry: fcs.keySet())
		{
			if(JEXStatics.cruncher.stopCrunch == true)
			{
				JEXStatics.cruncher.stopCrunch = false;
				JEXStatics.cruncher.finishTicket(null);
				return 0;
			}
			// Collect the data objects
			Set<JEXData> datas = fcs.get(entry).getOutputtedData();
			
			// Get the JEXEntry and the JEXFunction and the ExperimentalDataCrunch
//			JEXFunction func   = fcs.get(entry).getFunctionObject();
			
			// Add them to the ticket dictionary
//			datas.add(func.toJEXData());
			
			output.put(entry, datas);
		}
		
		// Finalize the ticket
		setOutputtedData(output);
		if (cr != null) cr.finalizeTicket(this);
		
		// Interact with JEX through a single synchronized function within cruncher
		// to ensure thread-safe behavior
		JEXStatics.cruncher.finishTicket(this);
		
		return 1;
	}


	/**
	 * Run the JEXfunction function on the pre-set entry entry
	 * @param function
	 */
	public synchronized FunctionCallable getFunctionCallable(JEXFunction function, JEXEntry entry)
	{
		// Collect and verify the existence of the inputs
		TreeMap<String,TypeName> inputs = function.getInputs();
		
		// Collect the inputs
		HashMap<String,JEXData> collectedInputs = new HashMap<String,JEXData>();
		for (String inputName: inputs.keySet())
		{
			// Get the input TypeName
			TypeName tn = inputs.get(inputName);
			
			// Prepare the JEXData for the input to the function
			JEXData data = JEXStatics.jexManager.getDataOfTypeNameInEntry(tn, entry);
			
			// If the data is  null yet someone defined a typeName, print error and return
			if (tn != null && data == null)
			{
				JEXStatics.logManager.log("Missing input data "+tn.toString(), 0, this);
				JEXStatics.statusBar.setStatusText("Missing input "+tn.toString());
				return null;
			}
			
			// Set the data as input to the function (a null tn indicates the input is supposed to be optional)
			collectedInputs.put(inputName, data);
		}
		
		// Run the function
		FunctionCallable fc = new FunctionCallable(function.duplicate(), entry, collectedInputs);
		return fc;	
		
	}
}
