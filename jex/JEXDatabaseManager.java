package jex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import jex.jexTabPanel.jexLabelPanel.LabelsPanel;
import jex.objectAndEntryPanels.JEXDataPanel;
import jex.statics.JEXStatics;
import signals.SSCenter;
import Database.DBObjects.JEXData;
import Database.DBObjects.JEXEntry;
import Database.Definition.Experiment;
import Database.Definition.TypeName;
import Database.SingleUserDatabase.JEXDB;
import Database.SingleUserDatabase.tnvi;

public class JEXDatabaseManager {
	
	public static String TEMP_FOLDER_PATH = "temp";
	public static String CORE_TEMP_NAME   = "JEXData";
	
	public JEXDatabaseManager()
	{
		
	}
	
	// ---------------------------------------------
	// Data Editing
	// ---------------------------------------------
	
	/**
	 * Add data object to entry
	 * @param entry
	 * @param data
	 * @return boolean
	 */
	public synchronized boolean saveDataInEntry(JEXEntry entry, JEXData data, boolean overwrite){
		// Get the current database, return null if no database is open
		JEXDB db = JEXStatics.jexManager.getCurrentDatabase();
		if (db == null) return false;
		
		boolean result = db.addData(entry, data, overwrite);
		if (result) {
			JEXStatics.logManager.log("Data added to database successfully",1,this);
			JEXStatics.statusBar.setStatusText("Data added successfully");
		}
		else {
			JEXStatics.logManager.log("Data not added to database",1,this);
			JEXStatics.statusBar.setStatusText("Data not added");
		}
		
		// -------------------------
		// Update the display
		updateDatabaseView();
		
		// Emit signal of experiment tree change
		JEXStatics.logManager.log("Send signal of experiment tree change", 1, this);
		SSCenter.defaultCenter().emit(this, JEXManager.EXPERIMENTTREE_UPDATE, (Object[])null);
				
		return result;
	}
	
	/**
	 * Add a single data object to a whole list of database entries
	 * @param dataArray
	 * @return boolean
	 */
	public synchronized boolean saveDataInEntries(TreeMap<JEXEntry,JEXData> dataArray){
		// Get the current database, return null if no database is open
		JEXDB db = JEXStatics.jexManager.getCurrentDatabase();
		if (db == null) return false;
		
		HashMap<JEXEntry,Set<JEXData>> dataArray2 = new HashMap<JEXEntry,Set<JEXData>>();
		for (JEXEntry entry: dataArray.keySet()){
			JEXData data = dataArray.get(entry);
			Set<JEXData> datas = new HashSet<JEXData>();
			datas.add(data);
			dataArray2.put(entry, datas);
		}
		boolean result = db.addDatas(dataArray2, true);
		if (result) {
			JEXStatics.logManager.log("Data added to database successfully",1,this);
			JEXStatics.statusBar.setStatusText("Data added successfully");
		}
		else {
			JEXStatics.logManager.log("Data not added to database",1,this);
			JEXStatics.statusBar.setStatusText("Data not added");
		}
		
		// -------------------------
		// Update the display
		updateDatabaseView();
		
		// Set the label list
		String scope = JEXStatics.jexManager.getScope(LabelsPanel.class.toString());
		TreeMap<String,TreeMap<String,Set<JEXEntry>>> thelabels = null;
		if (scope.equals(JEXManager.ALL_DATABASE)) thelabels = JEXStatics.jexManager.getTNVI().get(JEXData.LABEL);
		else thelabels = JEXStatics.jexManager.getFilteredTNVI().get(JEXData.LABEL);
		JEXStatics.jexManager.setLabels(thelabels);
		
		// Set the objects list
		String scope2 = JEXStatics.jexManager.getScope(JEXDataPanel.class.toString());
		tnvi theobjects = null;
		if (scope2.equals(JEXManager.ALL_DATABASE)) theobjects = JEXStatics.jexManager.getTNVI();
		else theobjects = JEXStatics.jexManager.getFilteredTNVI();
		JEXStatics.jexManager.setObjects(theobjects);
		
		// Emit signal of experiment tree change
		JEXStatics.logManager.log("Send signal of experiment tree change", 1, this);
		SSCenter.defaultCenter().emit(this, JEXManager.EXPERIMENTTREE_UPDATE, (Object[])null);
		
		return result;
	}
	
	/**
	 * Add a list of dataobjects to a list of database entries
	 * @param dataArray
	 * @return boolean
	 */
	public synchronized boolean saveDataListInEntries(TreeMap<JEXEntry,Set<JEXData>> dataArray, boolean overwrite){
		// Get the current database, return null if no database is open
		JEXDB db = JEXStatics.jexManager.getCurrentDatabase();
		if (db == null) return false;
		
		boolean result = db.addDatas(dataArray, overwrite);
		if (result) {
			JEXStatics.logManager.log("Data added to database successfully",1,this);
			JEXStatics.statusBar.setStatusText("Data added successfully");
		}
		else {
			JEXStatics.logManager.log("Data not added to database",1,this);
			JEXStatics.statusBar.setStatusText("Data not added");
		}
		
		// -------------------------
		// Update the display
		updateDatabaseView();
		
		// Emit signal of experiment tree change
		JEXStatics.logManager.log("Send signal of experiment tree change", 1, this);
		SSCenter.defaultCenter().emit(this, JEXManager.EXPERIMENTTREE_UPDATE, (Object[])null);
		
		return result;
	}

	/**
	 * Add data object to entry
	 * @param entry
	 * @param data
	 * @return boolean
	 */
	public synchronized boolean removeDataFromEntry(JEXEntry entry, JEXData data)
	{
		Set<JEXData> datas = new HashSet<JEXData>();
		datas.add(data);

		TreeMap<JEXEntry,Set<JEXData>> dataArray = new TreeMap<JEXEntry,Set<JEXData>>();
		dataArray.put(entry, datas);
		
		boolean result = removeDataListFromEntry(dataArray);
		if (result)
		{
			JEXStatics.logManager.log("Data removed from database successfully",1,this);
			JEXStatics.statusBar.setStatusText("Removed data");
		}
		else
		{
			JEXStatics.logManager.log("Data not removed from database",1,this);
			JEXStatics.statusBar.setStatusText("Data not removed");
		}
		
		// -------------------------
		// Update the display
		updateDatabaseView();
		
		// Emit signal of experiment tree change
		JEXStatics.logManager.log("Send signal of experiment tree change", 1, this);
		SSCenter.defaultCenter().emit(this, JEXManager.EXPERIMENTTREE_UPDATE, (Object[])null);
		
		return result;
	}

	/**
	 * Add data object to entry
	 * @param entry
	 * @param data
	 * @return boolean
	 */
	public synchronized boolean removeDataListFromEntry(TreeMap<JEXEntry,Set<JEXData>> dataArray){
		// Get the current database, return null if no database is open
		JEXDB db = JEXStatics.jexManager.getCurrentDatabase();
		if (db == null) return false;
		
		boolean result = db.removeObjectArray(dataArray);
		if (result) {
			JEXStatics.logManager.log("Data removed from database successfully",1,this);
			JEXStatics.statusBar.setStatusText("Removed data");
		}
		else {
			JEXStatics.logManager.log("Data not removed from database",1,this);
			JEXStatics.statusBar.setStatusText("Data not removed");
		}
		
		// -------------------------
		// Update the display
		updateDatabaseView();
		
		// Emit signal of experiment tree change
		JEXStatics.logManager.log("Send signal of experiment tree change", 1, this);
		SSCenter.defaultCenter().emit(this, JEXManager.EXPERIMENTTREE_UPDATE, (Object[])null);
		
		return true;
	}
	
	/**
	 * Remove the entries
	 * @param entriesToRemove
	 */
	public synchronized void removeEntries(Set<JEXEntry> entriesToRemove){
		JEXDB db = JEXStatics.jexManager.getCurrentDatabase();
		db.removeEntries(entriesToRemove);
		JEXStatics.statusBar.setStatusText("Removed entries");
		
		// Remove the entries from the selection, so that they don't stay in memory
		JEXStatics.jexManager.removeEntriesFromSelection(entriesToRemove);
		
		// -------------------------
		// Update the display
		updateDatabaseView();
		
		// Emit signal of experiment tree change
		JEXStatics.logManager.log("Send signal of experiment tree change", 1, this);
		SSCenter.defaultCenter().emit(this, JEXManager.EXPERIMENTTREE_CHANGE, (Object[])null);
		
		// Set the label list
		TreeMap<String,TreeMap<String,Set<JEXEntry>>> thelabels = JEXStatics.jexManager.getTNVI().get(JEXData.LABEL);
		JEXStatics.jexManager.setLabels(thelabels);
		
		// Set the objects list
		tnvi theobjects = JEXStatics.jexManager.getTNVI();
		JEXStatics.jexManager.setObjects(theobjects);
	}
	
	/**
	 * Remove the entries
	 * @param entriesToRemove
	 */
	public synchronized void editHeirarchyForEntries(TreeSet<JEXEntry> entries, String experiment, String tray, String info, String date)
	{
		JEXDB db = JEXStatics.jexManager.getCurrentDatabase();
		db.editHeirarchyForEntries(entries, experiment, tray, info, date);
		JEXStatics.statusBar.setStatusText("Edited entries");
		
		// -------------------------
		// Update the display
		updateDatabaseView();
		
		// Emit signal of experiment tree change
		JEXStatics.logManager.log("Send signal of experiment tree change", 1, this);
		SSCenter.defaultCenter().emit(this, JEXManager.EXPERIMENTTREE_CHANGE, (Object[])null);
		
		// Set the label list
		TreeMap<String,TreeMap<String,Set<JEXEntry>>> thelabels = JEXStatics.jexManager.getTNVI().get(JEXData.LABEL);
		JEXStatics.jexManager.setLabels(thelabels);
		
		// Set the objects list
		tnvi theobjects = JEXStatics.jexManager.getTNVI();
		JEXStatics.jexManager.setObjects(theobjects);
	}
	
	/**
	 * Return a unique object name for objects of type TYPE
	 * with a base name BASENAME for all entries amongst ENTRIES
	 * 
	 * First the basename will be tested, if none of the entries contains 
	 * an object of that name and type it is returned
	 * 
	 * If the baseName exists, a new name will be constructed as follows: "basename #"
	 * the method returns the new name with the lowest index
	 * 
	 * @param entries
	 * @param type
	 * @param baseName
	 * @return base name
	 */
	public synchronized String getUniqueObjectName(Set<JEXEntry> entries, String type, String baseName){
		String  result = baseName;
		int     index  = 1;
		boolean exists = true;
		
		while (exists)
		{
			// set the exists to false and loop through the entries
			exists = false;
			
			// if one of the entries has a data of type name TYPE,RESULT set exists to true
			for (JEXEntry entry: entries)
			{
				TypeName newTN = new TypeName(type,result);
				JEXData data = JEXStatics.jexManager.getDataOfTypeNameInEntry(newTN, entry);
				if (data != null) exists = true;
			}
			
			// if exists is true then generate a new name and repeat the process
			if (exists)
			{
				index ++;
				result = baseName + " " + index;
			}
		}
		
		return result;
	}
	
	/**
	 * Return a unique object name for objects of type TYPE
	 * with a base name BASENAME for all entries amongst ENTRIES
	 * 
	 * First the basename will be tested, if none of the entries contains 
	 * an object of that name and type it is returned
	 * 
	 * If the baseName exists, a new name will be constructed as follows: "basename #"
	 * the method returns the new name with the lowest index
	 * 
	 * @param entries
	 * @param type
	 * @param baseName
	 * @return base name
	 */
	public synchronized String getUniqueArrayName(String inExperiment){
		
		// Get the experiment currently opened
		TreeMap<String,Experiment> experiments = JEXStatics.jexManager.getExperimentTree();
		Experiment experiment = experiments.get(inExperiment);
		
		// Make the array map
		TreeSet<JEXEntry> entries = experiment.getEntries();
		TreeMap<String,Set<JEXEntry>> arrayMap = new TreeMap<String,Set<JEXEntry>>();
		for (JEXEntry entry: entries)
		{
			String arrayName = entry.getEntryTrayName();
			
			Set<JEXEntry> array = arrayMap.get(arrayName);
			if (array == null)
			{
				array = new TreeSet<JEXEntry>();
				arrayMap.put(arrayName, array);
			}
			
			array.add(entry);
		}
		
		// Make a new array name
		String  baseName = "Array";
		String  result   = baseName;
		int     index    = 1;
		boolean exists   = true;
		
		while (exists)
		{
			// set the exists to false and loop through the entries
			exists = false;
			
			for (String arrayName: arrayMap.keySet())
			{
				if (arrayName.equals(result)) exists = true;
			}
			
			// if exists is true then generate a new name and repeat the process
			if (exists)
			{
				index ++;
				result = baseName + " " + index;
			}
		}
		
		return result;
	}

	private void updateDatabaseView()
	{
		// Change the available objects and labels if scope is not the whole database
		String labScope = JEXStatics.jexManager.getScope(LabelsPanel.class.toString());
		if (!labScope.equals(JEXManager.ALL_DATABASE)) 
		{
			JEXStatics.jexManager.setLabels(JEXStatics.jexManager.getFilteredTNVI().get(JEXData.LABEL));
		}
		else JEXStatics.jexManager.setLabels(JEXStatics.jexManager.getTNVI().get(JEXData.LABEL));
		
		String objScope = JEXStatics.jexManager.getScope(JEXDataPanel.class.toString());
		if (!objScope.equals(JEXManager.ALL_DATABASE)) 
		{
			JEXStatics.jexManager.setObjects(JEXStatics.jexManager.getFilteredTNVI());
		}
		else JEXStatics.jexManager.setObjects(JEXStatics.jexManager.getTNVI());
	}
	
	
}
