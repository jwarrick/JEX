package jex.jexTabPanel.jexFunctionPanel;

import guiObject.DialogGlassPane;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import jex.ErrorMessagePane;
import jex.statics.DisplayStatics;
import jex.statics.JEXStatics;
import signals.SSCenter;
import Database.DBObjects.JEXEntry;
import Database.DBObjects.JEXWorkflow;
import Database.Definition.TypeName;
import cruncher.JEXFunction;
import cruncher.Ticket;

public class JEXFunctionPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	// GUI
	private JSplitPane            functionAndPreviewSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	private FunctionListPanel     functionListPanel ;
	private FunctionPreviewPanel  functionPreviewPanel ;
	
	// Function list
	public static List<FunctionBlockPanel> functionList = new ArrayList<FunctionBlockPanel>(0);

	JEXFunctionPanel()
	{
		initialize();
	}
	
	/**
	 * Detach the signals
	 */
	public void deInitialize()
	{
		SSCenter.defaultCenter().disconnect(this);
	}
	
	private void initialize()
	{
		functionPreviewPanel = new FunctionPreviewPanel();
		functionListPanel    = new FunctionListPanel(this);

		functionAndPreviewSplitPane.setBackground(DisplayStatics.background);
		functionAndPreviewSplitPane.setBorder(BorderFactory.createEmptyBorder());
		functionAndPreviewSplitPane.setLeftComponent(functionListPanel.panel());
		functionAndPreviewSplitPane.setRightComponent(functionPreviewPanel);
		functionAndPreviewSplitPane.setDividerLocation(250);
		functionAndPreviewSplitPane.setDividerSize(6);
		functionAndPreviewSplitPane.setResizeWeight(0);
		
		this.setLayout(new BorderLayout());
		this.setBackground(DisplayStatics.background);
		this.add(functionAndPreviewSplitPane);
		this.repaint();
	}
	
	////// Getters and setters
	
	/**
	 * Return the function list
	 */
	public List<FunctionBlockPanel> getFunctionPanels()
	{
		return JEXFunctionPanel.functionList;
	}

	/**
	 * Move the function functionBlock up one position
	 * @param functionBlock
	 */
	public void upOne(FunctionBlockPanel functionBlock)
	{
		// Get the index of the function to move up
		int index = getIndex(functionBlock);
		if (index == -1) return;
		
		// Make the new index of the function
		int newIndex = index -1 ;
		if (newIndex < 0) newIndex = 0;
		
		// Move the function
		functionList.remove(index);
		functionList.add(newIndex, functionBlock);
		
		// Refresh display
		functionListPanel.rebuildList();
	}
	
	/**
	 * Move the function functionBlock down one position
	 * @param functionBlock
	 */
	public void downOne(FunctionBlockPanel functionBlock)
	{
		// Get the index of the function to move down
		int index = getIndex(functionBlock);
		if (index == -1) return;
		
		// Make the new index of the function
		int newIndex = index + 1 ;
		if (newIndex >= functionList.size()) newIndex = functionList.size()-2;
		
		// Move the function
		functionList.remove(index);
		functionList.add(newIndex, functionBlock);
		
		// Refresh display
		functionListPanel.rebuildList();
	}
	
	/**
	 * Delete the function
	 * @param functionBlock
	 */
	public void delete(FunctionBlockPanel functionBlock)
	{
		// Get the index of the function to delete
		int index = getIndex(functionBlock);
		if (index == -1) return;
		
		// Remove the function
		JEXFunctionPanel.functionList.remove(index);
		
		// Refresh display
		functionListPanel.rebuildList();
	}
	
	/**
	 * Return the index of function in functionblockpanel functionBlack
	 * @param functionBlack
	 * @return
	 */
	private int getIndex(FunctionBlockPanel functionBlack)
	{
		for (int i=0; i<functionList.size(); i++)
		{
			FunctionBlockPanel fb = functionList.get(i);
			if (fb == functionBlack) return i;
		}
		return -1;
	}
	
	/**
	 * Add a function to the list
	 * @param function
	 */
	public FunctionBlockPanel addFunction(JEXFunction function)
	{
		// Create the new function block
		FunctionBlockPanel newFunc = new FunctionBlockPanel(this);
		newFunc.setFunction(function);
		
		// add it to the list
		JEXFunctionPanel.functionList.add(newFunc);
		
		// rebuild the display
		functionListPanel.rebuildList();
		
		// return the functionblock
		return newFunc;
	}
	
/////// METHODS
	
//	/**
//	 * Run the function at index INDEX
//	 */
//	public void runFunction(int index, boolean runAll)
//	{
//		// Get the function at index INDEX
//		FunctionBlockPanel functionBlack = this.getFunctionPanels().get(index);
//		if (functionBlack == null) return;
//		JEXFunction function = functionBlack.getFunction();
//		
//		// Get the entries to run the function on
//		TreeSet<JEXEntry> entries = null;
//		if (runAll) entries = JEXStatics.jexManager.getSelectedEntries();
//		else {
//			entries = new TreeSet<JEXEntry>();
//			entries.add(JEXStatics.jexManager.getViewedEntry());
//		}
//		
//		// Run the function in functionBlack
//		parent.getObjectPool().runOneFunction(function, entries);
//		
//		// Send a signal of for previewers to refresh with new data
//		JEXStatics.logManager.log("Function has ended, sending signal", 0, this);
//		SSCenter.defaultCenter().emit(this, JEXFunctionPanelController.FUNCTION_ENDED, (Object[])null);
//	}
	
	/**
	 * Run the function FUNCTION
	 * @param function
	 */
	public void runOneFunction(JEXFunction function, boolean isTest)
	{
		// Get the entries to run the function on
		TreeSet<JEXEntry> entries = null;
		if (!isTest) entries = JEXStatics.jexManager.getSelectedEntries();
		else {
			entries = new TreeSet<JEXEntry>();
			entries.add(JEXStatics.jexManager.getViewedEntry());
		}
		
		// Duplicate the functions and make a run list for making a ticket
		TreeMap<JEXEntry,JEXFunction> runlist = new TreeMap<JEXEntry,JEXFunction>();
		for (JEXEntry entry: entries){
			JEXFunction func = function.duplicate();
			runlist.put(entry, func);
		}
		
		Ticket ticket = new Ticket(runlist);
		JEXStatics.cruncher.runTicket(ticket);
		
		// Send a signal of for previewers to refresh with new data
		JEXStatics.logManager.log(function.getFunctionName() + " has ended.", 0, this);
		//
	}

	/**
	 * Run all the functions in the list one by one for all entries available
	 */
	public void runAllFunctions(boolean runAll, boolean autoSave)
	{
		// Get the entries to run the function on
		TreeSet<JEXEntry> entries = null;
		if (runAll) entries = JEXStatics.jexManager.getSelectedEntries();
		else {
			entries = new TreeSet<JEXEntry>();
			entries.add(JEXStatics.jexManager.getViewedEntry());
		}
		if (entries == null || entries.size() == 0){
			JEXStatics.logManager.log("No selected entries to run the function",1,this);
			JEXStatics.statusBar.setStatusText("No selected entries");

			DialogGlassPane diagPanel = new DialogGlassPane("Warning");
			diagPanel.setSize(400, 200);

			ErrorMessagePane errorPane = new ErrorMessagePane("You need to select at least one entry in the database to run this function");
			diagPanel.setCentralPanel(errorPane);

			JEXStatics.main.displayGlassPane(diagPanel,true);
		}
		else
		{
			// Loop through the functions
			for (FunctionBlockPanel fb: functionList)
			{
				// Get the function
				JEXFunction function = fb.getFunction();
				
				// Duplicate the functions and make a run list for making a ticket
				TreeMap<JEXEntry,JEXFunction> runlist = new TreeMap<JEXEntry,JEXFunction>();
				for (JEXEntry entry: entries){
					JEXFunction func = function.duplicate();
					runlist.put(entry, func);
				}
				
				Ticket ticket = new Ticket(runlist);
				ticket.setAutoSave(autoSave);
				JEXStatics.cruncher.runTicket(ticket);
			}
		}
	}
	
	/**
	 * Select the function form functionblockpanel fb
	 * @param fb
	 */
	public void selectFunction(FunctionBlockPanel fb)
	{
		this.functionListPanel.selectFunction(fb);
	}
	
	/**
	 * Returns the selected function
	 * @return
	 */
	public FunctionBlockPanel getSelectedFunction()
	{
		return this.functionListPanel.getSelectedFunction();
	}
	
	/**
	 * Save the list of functions into an xml file
	 * @param Path
	 */
	public void saveFunctionList(File file)
	{		
		// Loop through the functions
		JEXWorkflow toSave = new JEXWorkflow();
		for (FunctionBlockPanel fb: JEXFunctionPanel.functionList)
		{
			// Get the function object
			JEXFunction function = fb.getFunction();
			toSave.add(function);
		}
		toSave.saveWorkflow(file);
	}
	
	/**
	 * Load function located at path FILE
	 * @param file
	 */
	public void loadFunctionList(File file)
	{
		JEXWorkflow workflow = JEXWorkflow.loadWorkflow(file);
		// Loop through the buds
		for (JEXFunction func : workflow)
		{
			FunctionBlockPanel fb = addFunction(func);
			fb.testFunction();
		}
	}
	
	/**
	 * Return true if the typename is set for the output of one of the existing functions
	 * @param tn
	 * @return
	 */
	public boolean isDataOutputOfFunction(TypeName tn)
	{
		for (FunctionBlockPanel fb: JEXFunctionPanel.functionList)
		{
			JEXFunction function = fb.getFunction();
			
			// Get the set names
			TreeMap<Integer,TypeName> outputs = function.getExpectedOutputs();
			
			// Get the normal names
			for (Integer index : outputs.keySet())
			{
				if (tn.equals(outputs.get(index))) return true;
			}
		}
		
		return false;
	}
	
	// ACTIONS
	
	public void actionPerformed(ActionEvent e)
	{
//		if (e.getSource() == this.resetButton)
//		{
//			pool.reset();
//		}
//		else if (e.getSource() == this.saveButton)
//		{
//			pool.saveToDatabase();
//		}
	}
	
}