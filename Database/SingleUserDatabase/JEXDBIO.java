package Database.SingleUserDatabase;

import image.roi.ROIPlus;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import jex.statics.JEXStatics;

import org.apache.commons.io.FileUtils;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import preferences.XMLPreferences_XElement;
import utilities.FileUtility;
import utilities.Pair;
import utilities.StopWatch;
import utilities.XMLUtility;
import Database.DBObjects.JEXData;
import Database.DBObjects.JEXDataSingle;
import Database.DBObjects.JEXEntry;
import Database.DBObjects.JEXLabel;
import Database.DBObjects.dimension.DimTable;
import Database.DBObjects.dimension.DimensionMap;
import Database.DataWriter.HeirarchyWriter;
import Database.SingleUserDatabase.xml.ObjectFactory;
import Database.SingleUserDatabase.xml.XData;
import Database.SingleUserDatabase.xml.XDataSingle;
import Database.SingleUserDatabase.xml.XElement;
import Database.SingleUserDatabase.xml.XEntry;
import Database.SingleUserDatabase.xml.XEntrySet;

public class JEXDBIO {
	
	// Administrative statics
	public static String LOCAL_DATABASE_FILENAME = "JEX4Database.xml";
	
	// statics
	public static String VERSION = "2012-06-05";
	
	// ---------------------------------------------
	// Load / save
	// ---------------------------------------------
	
	/**
	 * Load the file as a Database
	 */
	public static JEXDB load(String xmlPath)
	{
		JEXStatics.logManager.log("Loading the local database "+xmlPath, 0, null);
		
		// Load the xml into this db object
		JEXDB db = JEXDBIO.XEntrySetToDatabaseObject(xmlPath);
		
		if(db == null)
		{
			JEXStatics.logManager.log("!!! Database loading failed !!!", 0, null);
		}
		return db;
	}
	
	/**
	 * Save the database in folder FOLDER
	 * Set consolidate flag to true to copy all raw data with it
	 * @param folder
	 * @param consolidate
	 * @return
	 * @throws IOException 
	 */
	public static boolean saveDB(JEXDB db) {
		
		// Get the saving folder
		File folder = new File(JEXWriter.getDatabaseFolder());
		
		// Archive the old database
		String dbFileName  = FileUtility.getNextName(folder.getPath(), LOCAL_DATABASE_FILENAME, "Archive");
		File dbFile        = new File(folder.getPath() + File.separator + LOCAL_DATABASE_FILENAME);
		File archiveDBFile = new File(folder.getPath() + File.separator + dbFileName);
		if (dbFile.exists()){
			FileUtility.copy(dbFile, archiveDBFile);
		}
		
		////////////////////////////////////////////////////////////////////////////////////////
		// Get the XML version of the database and any file updates that need to be performed //
		// Then perform the file updates													  //
		////////////////////////////////////////////////////////////////////////////////////////
		Pair<XEntrySet,TreeMap<String,Pair<String,String>>> results  = JEXDBIO.DatabaseObjectToXEntrySet(db);
		XEntrySet xml = results.p1;
		TreeMap<String,Pair<String,String>> fileUpdatesToPerform = results.p2;

		// Temporarily move changed files to the temp folder to avoid moving files in
		// the wrong order and deleting something that we should be keeping.
		try
		{
			File src, temp, dst;
			for(String srcPath : fileUpdatesToPerform.keySet())
			{
				Pair<String,String> fileUpdate = fileUpdatesToPerform.get(srcPath);
				src = new File(srcPath);
				temp = new File(fileUpdate.p1); // The temp file name should already be unique because it was given to use by JEXWriter.
				dst = new File(fileUpdate.p2);
				if(!src.equals(dst))
				{
					if(src.isDirectory())
					{
						FileUtility.moveFolderContents(src, temp);
					}
					else
					{
						if(temp.exists())
						{
							// We can delete this because we know we alread have a unique name from JEXWriter for this Database session
							// This is a leftover file from a previous session
							FileUtils.deleteQuietly(temp);
						}
						FileUtils.moveFile(src, temp);
					}
				}
			}
			// Now move from the temp folder to the new location in the database.
			// Now we can assume that everything in the way of the file move can be deleted.
			for(String srcPath : fileUpdatesToPerform.keySet())
			{
				Pair<String,String> fileUpdate = fileUpdatesToPerform.get(srcPath);
				src = new File(srcPath);
				temp = new File(fileUpdate.p1); // The temp file name should already be unique because it was given to use by JEXWriter.
				dst = new File(fileUpdate.p2);
				if(!src.equals(dst))
				{
					// Now we know we can delete any file that might exists at dst
					FileUtils.deleteQuietly(dst); // make room for the new file or else moveFile will error
					if(src.isDirectory())
					{
						FileUtility.moveFolderContents(temp, dst);
					}
					else
					{
						FileUtils.moveFile(temp, dst);
					}
				}
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		/////////////////////////////////////////
		// Save the real XML version of the DB //
		/////////////////////////////////////////
		String XMLDBString = XMLUtility.toHardXML(xml);
		String xmlPath     = JEXWriter.getDatabaseFolder() + File.separator + LOCAL_DATABASE_FILENAME; 
		XMLUtility.XMLsave(xmlPath, XMLDBString);
		JEXStatics.logManager.log("======================================================", 0, JEXDBIO.class.getSimpleName());
		JEXStatics.logManager.log("Saved hard XML at location "+xmlPath, 0, JEXDBIO.class.getSimpleName());
		
		// Save the pretty version of the XML
		String XMLDBPrettyString = XMLUtility.toXML(xml);
		String prettypath = xmlPath.substring(0, xmlPath.length()-4)+"_pretty.xml";
		XMLUtility.XMLsave(prettypath, XMLDBPrettyString);
		JEXStatics.logManager.log("Saved pretty XML at location "+prettypath, 0, JEXDBIO.class.getSimpleName());
		JEXStatics.logManager.log("======================================================", 0, JEXDBIO.class.getSimpleName());
		
		// Remake the tnvis because the dictionary values have been updated 
		db.makeTNVIs();
		db.makeFiltTNVI();
		db.makeStatTNVI();
		db.makeExperimentTree();
		
		// Return save success
		return true;
	}
	
	// ----------------------------
	// XEntrySet to JEXLocalDatabaseCore
	// ----------------------------
	
	public static JEXDB XEntrySetToDatabaseObject(String xmlPath)
	{
		JEXDB ret = new JEXDB();
		
		// Generate the datamap
		TreeMap<JEXEntry, TreeMap<String, TreeMap<String, JEXData>>> datamap = new TreeMap<JEXEntry, TreeMap<String, TreeMap<String, JEXData>>>(); 
		
		// Make the database path
		File xmlFile   = new File(xmlPath);
		if (!xmlFile.exists()) return null;
		
		// Parse the xml
		JEXStatics.logManager.log("Parsing the xml string "+xmlPath, 0, JEXDBIO.class.getSimpleName());
		StopWatch stopwatch = new StopWatch();
	    stopwatch.start();
		SAXBuilder sb = new SAXBuilder();
		sb.setFactory(new ObjectFactory());
		Document resturnDoc = null;
		try {
			resturnDoc = sb.build(new File(xmlPath));
		}
		catch (JDOMException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		stopwatch.stop();
		JEXStatics.logManager.log("Factory TIME: "+stopwatch.toString(), 0, JEXDBIO.class.getSimpleName());
		JEXStatics.logManager.log("======================================================", 0, JEXDBIO.class.getSimpleName());
		XEntrySet xmlElement = (XEntrySet) resturnDoc.getRootElement();

		stopwatch = new StopWatch();
	    stopwatch.start();

		// set the entry set of this class
		List<XElement> children = xmlElement.getXElements();
		int maxID = 0 ;
		boolean resetIDs = false;
		
		for (XElement elem: children){
			XEntry xentry    = (XEntry)elem;
			JEXEntry entry = JEXDBIO.XEntryToDatabaseObject(xentry);
			entry.setParent(ret);
			
			// Check that the entry is good
			String eid = entry.getEntryID();
			if (eid == null || eid.equals("") || resetIDs) {
				
				// If this is the first entry that doesn't have an ID then renumerate
				// all of the previous entries
				if (!resetIDs){
					int index = 0;
					for (JEXEntry e: datamap.keySet()){
						e.setEntryID("" + index);
						index ++;
					}
					maxID = index ;
				}
				
				// Switch the resetID flag to true and override the id setting
				resetIDs = true;
				entry.setEntryID("" + maxID);
				maxID ++;
			}
			
			// Set the max id as the maximum current ID value
			int thisID = Integer.parseInt(entry.getEntryID());
			maxID      = Math.max(maxID, thisID) + 1;
			
			// get the data list
			TreeMap<String, TreeMap<String, JEXData>> datalist = entry.getDataList();
			
			// Make the hierarchy map
			TreeMap<String, JEXData> hmap = datalist.get(JEXData.HIERARCHY);
			if (hmap == null) {
				hmap = new TreeMap<String, JEXData>();
				datalist.put(JEXData.HIERARCHY, hmap);
			}
			
			// Add the experiment, array name, and x, y locations to the datamap
			JEXData expData = HeirarchyWriter.makeHeirarchy(JEXEntry.EXPERIMENT, entry.getEntryExperiment());
			hmap.put(JEXEntry.EXPERIMENT, expData);
			
			JEXData trayData = HeirarchyWriter.makeHeirarchy(JEXEntry.TRAY, entry.getEntryTrayName());
			hmap.put(JEXEntry.TRAY, trayData);
			
			JEXData xData = HeirarchyWriter.makeHeirarchy(JEXEntry.X, ""+entry.getTrayX());
			hmap.put(JEXEntry.X, xData);
			
			JEXData yData = HeirarchyWriter.makeHeirarchy(JEXEntry.Y, ""+entry.getTrayY());
			hmap.put(JEXEntry.Y, yData);
			
			// Check that the valid label exists
			TreeMap<String, JEXData> vmap = datalist.get(JEXData.LABEL);
			if (vmap == null){
				vmap = new TreeMap<String, JEXData>();
				datalist.put(JEXData.LABEL, vmap);
			}
			JEXData vLabel = vmap.get(JEXEntry.VALID);
			if (vLabel == null){
				vLabel = new JEXLabel(JEXEntry.VALID,"true",""); //makeJEXMLDataValue(JEXData.LABEL,JEXEntry.VALID,"validity","true");
				vmap.put(JEXEntry.VALID, vLabel);
			}
			
			// add the data list
			ret.getEntries().add(entry);
			
			// Status bar
			int percentage = (int) (100 * ((double) maxID/ (double)children.size()));
			JEXStatics.statusBar.setProgressPercentage(percentage);
		}
		
		ret.setMaxID(maxID);
		ret.makeTNVIs();
		ret.makeFiltTNVI();
		ret.makeStatTNVI();
		ret.makeExperimentTree();
		return ret;
	}
	
	// ----------------------------
	// XEntry to JEXEntry
	// ----------------------------
	
	/**
	 * Make a JEXEntry out of a XEntry
	 */
	public static JEXEntry XEntryToDatabaseObject(XEntry xe){
		JEXEntry result = new JEXEntry();
		
		String ID       = xe.getAtt(JEXEntry.EID);
		ID              = (ID == null) ? "" : ID ;
		String expName  = "";
		String expInfo  = "";
		String trayName = "";
		String trayX    = "";
		String trayY    = "";
		String date     = xe.getAtt(JEXEntry.DATE);
		String modif    = xe.getAtt(JEXEntry.MODIFDATE) ;
		String author   = xe.getAtt(JEXEntry.AUTHOR) ;
		
		// Get the child elements
		List<XElement> children = xe.getXElements();
		
		// Loop through to make the JEXEntry Hierarchy
		for (XElement elem: children){
			XData xdata = (XData)elem;
			
			// if it's a hierarchy treat it differently
			if (xdata.getTypeField().equals(JEXData.HIERARCHY)){
				String level = xdata.getAtt(JEXData.NAME);
				XDataSingle ds = xdata.getFirstDataElement();
				
				if (level.equals(JEXEntry.EXPERIMENT)){
					expName = ds.getAtt("Value");
					expInfo = xdata.getAtt("Info");
				}
				if (level.equals(JEXEntry.TRAY)){
					trayName = ds.getAtt("Value");
				}
				if (level.equals("Row")){
					trayX = ds.getAtt("Value");
				}
				if (level.equals("Column")){
					trayY = ds.getAtt("Value");
				}
			}
		}
		
		// set the meta data on the entry
		result.setEntryExperiment(expName);
		result.loadTimeExperimentName = expName;
		result.setEntryExperimentInfo(expInfo);
		result.setEntryTrayName(trayName);
		result.loadTimeTrayName = trayName;
		result.setTrayX(Integer.parseInt(trayX));
		result.setTrayY(Integer.parseInt(trayY));
		result.setDate(date);
		result.setModificationDate(modif);
		result.setEntryID(ID);
		result.setAuthor(author);
		
		// Loop though to add the common data
		for (XElement elem: children){
			XData xdata = (XData)elem;
			
			// if it's a normal object
			if (!xdata.getTypeField().equals(JEXData.HIERARCHY)){
				JEXData data = JEXDBIO.XDataToDatabaseObject(xdata,result);
				if (data == null) continue;
				result.addData(data, true);
			}
		}
		
		// Update modification date if necessary
		if (modif == null){
			modif = result.findModificationDate();
		}
		
		// VERIFICATION

		// Check if the entry has a valid label
		boolean hasValidLabel = false;
		
		TreeMap<String, JEXData> labels = result.getDataList().get(JEXData.LABEL);
		if (labels != null) {
			JEXData valid = labels.get(JEXEntry.VALID);
			if (valid != null) hasValidLabel = true;
		}
		if (!hasValidLabel){
			JEXData valid = new JEXLabel(JEXEntry.VALID, "true", ""); //makeJEXMLDataLabel(JEXEntry.VALID,"Valid flag","true");
			result.addData(valid, false);
		}
		
		return result;
	}
	
	// ----------------------------
	// XData to JEXData
	// ----------------------------
	
	/**
	 * Make a JEXData out of a XData
	 * @param xData
	 * @param entry
	 * @return
	 */
	public static JEXData XDataToDatabaseObject(XData xData, JEXEntry entry)
	{
		JEXData result = null;
		
		String type        = xData.getAttributeValue(JEXData.TYPE);
		String dataName    = xData.getAtt(JEXData.NAME);
		String dataInfo    = xData.getAtt(JEXData.INFO);
		String dataDate    = xData.getAtt(JEXData.DATE);
		String dataModif   = xData.getAtt(JEXData.MDATE);
		String author      = xData.getAtt(JEXData.AUTHOR);
		String dataDims    = xData.getAtt(JEXData.DIMS);
		String objPath     = xData.getAtt(JEXData.DETACHED_RELATIVEPATH);
		String dictKey     = xData.getAtt(JEXData.DICTKEY);
		dataName           = (dataName == null) ? "No" : dataName;
		dataInfo           = (dataInfo == null) ? "No" : dataInfo;
		
		if (type == null) return null;
		result = new JEXData(type,dataName,dataInfo);
		result.setParent(entry);
		
		result.setDataObjectDate(dataDate);
		result.setDataObjectModifDate(dataModif);
		result.setDataObjectInfo(dataInfo);
		result.setDataObjectName(dataName);
		result.setAuthor(author);
		
		if (dictKey != null && !dictKey.equals(""))
		{
			// Else the next request for the dictionaryvalue witll load the object and set it
			result.setDictionaryValue(dictKey);
		}
		if(objPath == null || objPath.equals(""))
		{
			// IF WE HAVE AN OLD XML TYP JEXDATA THAT DOESN'T HAVE A DETACHED PATH
			// LOAD THE XDATASINGLES
			result.setDetachedRelativePath(null);
			result.setDimTable(new DimTable(dataDims));
			
			List<XDataSingle> datas = xData.getSingleDataElements();
			for (XDataSingle ds: datas){
				JEXDataSingle dataSingle = JEXDBIO.XDataSingleToDatabaseObject(ds,result);
				if (dataSingle == null) continue;
				DimensionMap map = dataSingle.getDimensionMap();
				result.addData(map, dataSingle);
			}
			
			JEXDataSingle ds = (JEXDataSingle)result.getFirstSingle();
			if (ds == null)
			{
				JEXStatics.logManager.log("Error loading JEXData - " + result.getTypeName().toString(), 0, JEXDBIO.class.getSimpleName());
				return null;
			}
			
			/// Set the dict key for building the tnvi
			result.setDictionaryValue(ds.toString());
			
			// try to overwrite with dictKey (won't work if null)
			result.setDictionaryValue(dictKey);
		}
		else
		{
			if(objPath.endsWith("xml")) // XML VERSIONS OF JEXDATA WERE SAVED IN THE ENTRY FOLDER NOT THE JEXDATA FOLDER
			{
				result.setDetachedRelativePath(JEXWriter.getEntryFolder(entry, true, true) + File.separator + FileUtility.getFileNameWithExtension(objPath));
				result.setDimTable(new DimTable(dataDims)); // DIM TABLE WAS SAVED IN DATABASE XML
			}
			else // CSV/ARFF SAVED IN JEXDATA FOLDER
			{
				result.setDetachedRelativePath(JEXWriter.getDataFolder(result, true) + File.separator + FileUtility.getFileNameWithExtension(objPath));
				// DIM TABLE IS CONTAINED IN CSV/ARFF SO WE DONT WANT TO LOAD IT YET
			}
		}
		
		return result;
	}

	// ----------------------------
	// XDataSingle to JEXDataSingle
	// ----------------------------
	
	/**
	 * Make a JEXDataSingle out of a XDataSingle
	 * @param parent
	 * @param dimArray
	 * @return
	 */
	public static JEXDataSingle XDataSingleToDatabaseObject(XDataSingle xds, JEXData parent)
	{
		JEXDataSingle result = new JEXDataSingle();

		// get the type field
		String type = xds.getAttributeValue(JEXData.TYPE);
		
		// Make the dimensionmap
		DimensionMap dimMap = new DimensionMap();
		for (String dimStr: parent.getDimTable().getDimensionNames()){
			String dimValueStr = xds.getAtt(dimStr);
			if (dimValueStr == null) continue;
			dimMap.put(dimStr, dimValueStr);
		}
		
		if (type.equals(JEXData.IMAGE) || type.equals(JEXData.FILE) || type.equals(JEXData.MOVIE) || type.equals(JEXData.TRACK) || type.equals(JEXData.SOUND)){
			String test = xds.getAtt("Path");
			if (test != null)
			{
				result.put(JEXDataSingle.RELATIVEPATH, JEXWriter.getEntryFolder(parent.getParent(),true,true) + File.separator + (new File(test)).getName());
			}
			else if(parent.hasDetachedFile() && FileUtility.getFileNameExtension(parent.getDetachedRelativePath()).equals(JEXDataIO.DETACHED_FILEEXTENSION))
			{
				// DOING THINGS THIS WAY CONVERTS STUFF PREVIOUSLY SAVED AS JUST A FILENAME TO A DATABASE RELATIVE PATH
				// BUT ALSO WORKS WHEN IT IS ALREADY A RELATIVE PATH
				String relativePath = xds.getAtt(JEXDataSingle.RELATIVEPATH);
				result.put(JEXDataSingle.RELATIVEPATH, JEXWriter.getDataFolder(parent,true) + File.separator + FileUtility.getFileNameWithExtension(relativePath));
			}
			else if (!parent.hasDetachedFile() || FileUtility.getFileNameExtension(parent.getDetachedRelativePath()).equals("xml"))
			{
				String fileName = FileUtility.getFileNameWithExtension(xds.getAtt(JEXDataSingle.RELATIVEPATH));
				result.put(JEXDataSingle.RELATIVEPATH, JEXWriter.getEntryFolder(parent.getParent(),true,true) + File.separator + fileName);
			}
			
			
		}
		else if (type.equals(JEXData.FUNCTION_OLD)){
			// Make the special dimensionmap
			dimMap = new DimensionMap();
			
			List<String> attNames = xds.getAttNames();
			
			for (String attName: attNames){
				if (attName.equals(cruncher.JEXFunction.OLD_DIMKEY))
				{
					String attValue = xds.getAtt(attName);
					dimMap.put(cruncher.JEXFunction.OLD_DIMKEY, attValue);
				}
				else
				{
					String attValue = xds.getAtt(attName);
					result.put(attName, attValue);
				}
			}
		}
		else if (type.equals(JEXData.ROI))
		{
			String roitype = xds.getAtt(JEXDataSingle.ROITYPE);
			String points = xds.getAtt(JEXDataSingle.POINTLIST);
			String pattern = xds.getAtt(JEXDataSingle.PATTERN);
			
			if (roitype.equals("JRectangle")) roitype = ""+ROIPlus.ROI_RECT;
			else if  (roitype.equals("JPolygon")) roitype = ""+ROIPlus.ROI_POINT;
			else if  (roitype.equals("JPolygonLine")) roitype = ""+ROIPlus.ROI_POLYGON;
			
			result.put(JEXDataSingle.ROITYPE,roitype);
			result.put(JEXDataSingle.POINTLIST,points);
			result.put(JEXDataSingle.PATTERN,pattern);
		}
		else if (type.equals(JEXData.VALUE))
		{
			String value = xds.getAtt(JEXDataSingle.VALUE);
			String unit = xds.getAtt(JEXDataSingle.UNIT); 
			result.put(JEXDataSingle.VALUE, value);
			result.put(JEXDataSingle.UNIT, unit);
		}
		else if (type.equals(JEXData.LABEL))
		{
			String value = xds.getAtt(JEXDataSingle.VALUE);
			String unit = xds.getAtt(JEXDataSingle.UNIT); 
			result.put(JEXDataSingle.VALUE, value);
			result.put(JEXDataSingle.UNIT, unit);
		}
		else
		{
			result = null;
		}
		
		if (result != null) result.setDimensionMap(dimMap);
		return result;
	}
	
	// ----------------------------
	// JEXEntrySet to XEntrySet
	// ----------------------------
	
	/**
	 * Return an XEntrySet from a JEXDatabase
	 */
	public static Pair<XEntrySet,TreeMap<String,Pair<String,String>>> DatabaseObjectToXEntrySet(JEXDB db)
	{
		XEntrySet result = new XEntrySet();
		TreeMap<String,Pair<String,String>> fileUpdatesToPerform = new TreeMap<String,Pair<String,String>>();
		for (JEXEntry entry : db)
		{
			try 
			{
				Pair<XEntry,TreeMap<String,Pair<String,String>>> results = JEXDBIO.DatabaseObjectToXEntry(entry);
				XEntry xentry = results.p1;
				fileUpdatesToPerform.putAll(results.p2);
				result.addEntry(xentry);
			} 
			catch (Exception e)
			{
				e.printStackTrace();
				JEXStatics.logManager.log("Error in saving entry number "+entry.getEntryID(), 0, JEXDBIO.class.getSimpleName());
				JEXStatics.statusBar.setStatusText("Error in saving entry number "+entry.getEntryID());
			}
		}
		
		return new Pair<XEntrySet,TreeMap<String,Pair<String,String>>>(result, fileUpdatesToPerform);
	}
	
	// ----------------------------
	// JEXEntry to XEntry
	// ----------------------------

	/**
	 * Make an Xentry out of the classy entry
	 * @param folder
	 * @param localPath
	 * @param consolidate
	 * @return
	 */
	public static Pair<XEntry,TreeMap<String,Pair<String,String>>> DatabaseObjectToXEntry(JEXEntry entry)
	{
		XEntry result = new XEntry();
		TreeMap<String,Pair<String,String>> fileUpdatesToPerform = new TreeMap<String,Pair<String,String>>();
		
		boolean expOrTrayNameChanged = didExperimentOrTrayNameChange(entry);
		
		// Make the meta bud
		result.addMeta(JEXEntry.EID, entry.getEntryID());
		result.addMeta(JEXEntry.DATE, entry.getDate());
		result.addMeta(JEXEntry.MODIFDATE, entry.getModificationDate());
		result.addMeta(JEXEntry.AUTHOR, entry.getAuthor());
		
		// Add the hierarchy elements to the data object
		XData expGroup = new XData(JEXData.HIERARCHY);
		expGroup.addMeta(JEXData.NAME, "Experiment");
		expGroup.addMeta(JEXData.TYPE, JEXData.HIERARCHY);
		expGroup.addMeta(JEXData.INFO, entry.getEntryExperimentInfo());
		expGroup.addMeta(JEXData.DATE, entry.getDate());
		expGroup.addMeta(JEXData.DIMS,"");
		XDataSingle expSingle = new XDataSingle(JEXData.VALUE);
		expSingle.addMeta(JEXDataSingle.VALUE, entry.getEntryExperiment());
		expSingle.addMeta(JEXDataSingle.UNIT, "");
		expGroup.addDataSingle(expSingle);
		result.addData(expGroup);

		XData trayGroup = new XData(JEXData.HIERARCHY);
		trayGroup.addMeta(JEXData.NAME, "Tray");
		trayGroup.addMeta(JEXData.TYPE, JEXData.HIERARCHY);
		trayGroup.addMeta(JEXData.INFO, "");
		trayGroup.addMeta(JEXData.DATE, entry.getDate());
		trayGroup.addMeta(JEXData.DIMS,"");
		XDataSingle traySingle = new XDataSingle(JEXData.VALUE);
		traySingle.addMeta(JEXDataSingle.VALUE, entry.getEntryTrayName());
		traySingle.addMeta(JEXDataSingle.UNIT, "");
		trayGroup.addDataSingle(traySingle);
		result.addData(trayGroup);

		XData rowGroup = new XData(JEXData.HIERARCHY);
		rowGroup.addMeta(JEXData.NAME, "Row");
		rowGroup.addMeta(JEXData.TYPE, JEXData.HIERARCHY);
		rowGroup.addMeta(JEXData.INFO, "");
		rowGroup.addMeta(JEXData.DATE, entry.getDate());
		rowGroup.addMeta(JEXData.DIMS,"");
		XDataSingle rowSingle = new XDataSingle(JEXData.VALUE);
		rowSingle.addMeta(JEXDataSingle.VALUE, ""+entry.getTrayX());
		rowSingle.addMeta(JEXDataSingle.UNIT, "");
		rowGroup.addDataSingle(rowSingle);
		result.addData(rowGroup);

		XData colGroup = new XData(JEXData.HIERARCHY);
		colGroup.addMeta(JEXData.NAME, "Column");
		colGroup.addMeta(JEXData.TYPE, JEXData.HIERARCHY);
		colGroup.addMeta(JEXData.INFO, "");
		colGroup.addMeta(JEXData.DATE, entry.getDate());
		colGroup.addMeta(JEXData.DIMS,"");
		XDataSingle colSingle = new XDataSingle(JEXData.VALUE);
		colSingle.addMeta(JEXDataSingle.VALUE, ""+entry.getTrayY());
		colSingle.addMeta(JEXDataSingle.UNIT, "");
		colGroup.addDataSingle(colSingle);
		result.addData(colGroup);
		
		// Make the collection bud with the data objects
		TreeMap<String, TreeMap<String, JEXData>> datas = entry.getDataList();
		for (String type:datas.keySet())
		{
			if (type.equals(JEXData.HIERARCHY))
			{
				continue;
			}
			
			TreeMap<String, JEXData> dl = datas.get(type);
			for (String name: dl.keySet())
			{
				JEXData data = dl.get(name);
				if (data.getParent() == null)
				{
					data.setParent(entry);
				}
				
				if(expOrTrayNameChanged)
				{
					// LOAD THE DATA TO MARK IT FOR UPDATING TO NEW EXPERIMENT OR TRAY OR DATA FOLDERS IF IT ISNT ALREADY
					// THIS OCCURS IN JEXMLFactory.DatabaseObjectToXData
					if(!data.isLoaded())
					{
						data.getDataMap();
					}
					if(expOrTrayNameChanged)
					{
						// THEN WE NEED TO PREPARE TO MOVE THE ATTACHED FILES AT THE EXPERIMENT AND TRAY LEVELS						
						// First get the src folders
						String src1 = JEXWriter.getAttachedFolderPath(entry, JEXEntry.EXPERIMENT, true, false);
						String src2 = JEXWriter.getAttachedFolderPath(entry, JEXEntry.TRAY, true, false);
						
						// Get the dst folders
						String dst1 = JEXWriter.getAttachedFolderPath(entry, JEXEntry.EXPERIMENT, false, false);
						String dst2 = JEXWriter.getAttachedFolderPath(entry, JEXEntry.TRAY, false, false);

						// Secure the temp folder names
						String temp1 = JEXWriter.getDatabaseFolder() + File.separator + JEXWriter.getTempFolderName() + File.separator + FileUtility.getFileNameWithoutExtension(JEXWriter.getUniqueRelativeTempPath("duh"));
						String temp2 = JEXWriter.getDatabaseFolder() + File.separator + JEXWriter.getTempFolderName() + File.separator + FileUtility.getFileNameWithoutExtension(JEXWriter.getUniqueRelativeTempPath("duh"));
						
						fileUpdatesToPerform.put(src1, new Pair<String,String>(temp1,dst1));
						fileUpdatesToPerform.put(src2, new Pair<String,String>(temp2,dst2));
					}
				}
				
				Pair<XData,TreeMap<String,Pair<String,String>>> results = JEXDBIO.DatabaseObjectToXData(data);
				XData xdata = results.p1;
				fileUpdatesToPerform.putAll(results.p2);
				result.addData(xdata);
			}
		}
		
		return new Pair<XEntry,TreeMap<String,Pair<String,String>>>(result, fileUpdatesToPerform);
	}
	
	private static boolean didExperimentOrTrayNameChange(JEXEntry entry)
	{
		if(entry.loadTimeExperimentName == null || !entry.getEntryExperiment().equals(entry.loadTimeExperimentName))
		{
			return true;
		}
		if(entry.loadTimeTrayName == null || !entry.getEntryTrayName().equals(entry.loadTimeTrayName))
		{
			return true;
		}
		return false;
	}
	
	
	// ----------------------------
	// JEXData to XData
	// ----------------------------
	
	/**
	 * Return an XML version of the dataobject
	 */
	public static Pair<XData,TreeMap<String,Pair<String,String>>> DatabaseObjectToXData(JEXData data)
	{
		
		XData xData = new XData(data.getTypeName().getType());
		TreeMap<String,Pair<String,String>> fileUpdatesToPerform = new TreeMap<String,Pair<String,String>>();
		
		// Make the meta bud
		xData.addMetaWithCategory(JEXData.NAME,   "Adm", data.getDataObjectName());
		xData.addMetaWithCategory(JEXData.TYPE,   "Adm", data.getDataObjectType());
		xData.addMetaWithCategory(JEXData.INFO,   "Adm", data.getDataObjectInfo());
		xData.addMetaWithCategory(JEXData.DATE,   "Adm", data.getDataObjectDate());
		xData.addMetaWithCategory(JEXData.MDATE,  "Adm", data.getDataObjectModifDate());
		xData.addMetaWithCategory(JEXData.AUTHOR, "Adm", data.getAuthor());
		xData.addMetaWithCategory(JEXData.DICTKEY, "Adm", data.getDictionaryValue());
		
		// Make the collection bud with the data objects
		// If the object is not loaded, no need to load it, keep it unchanged
		if (!data.isLoaded())
		{
			xData.addMetaWithCategory(JEXData.DETACHED_RELATIVEPATH, "Adm", data.getDetachedRelativePath());
		}
		else
		{	
			TreeMap<String,Pair<String,String>> results = JEXDataIO.updateData(data);
			// Change the detached path after calling JEXDataSaver because this field gets updated
			// using the now updated value in data.detachedRelativePath
			xData.addMetaWithCategory(JEXData.DETACHED_RELATIVEPATH, "Adm", data.getDetachedRelativePath());
			fileUpdatesToPerform.putAll(results);
			data.unloadObject();
		}
		
		return new Pair<XData,TreeMap<String,Pair<String,String>>>(xData,fileUpdatesToPerform);
	}
	
	public static XMLPreferences_XElement getViewingXMLElement(JEXData data)
	{
		
		XMLPreferences_XElement result = new XMLPreferences_XElement();

		// Make the meta bud
		result.setAtt(JEXData.NAME, data.getDataObjectName(), JEXData.ADMIN);
		result.setAtt(JEXData.TYPE, data.getDataObjectType(), JEXData.ADMIN);
		result.setAtt(JEXData.INFO, data.getDataObjectInfo(), JEXData.ADMIN);
		result.setAtt(JEXData.DATE, data.getDataObjectDate(), JEXData.ADMIN);
		result.setAtt(JEXData.MDATE, data.getDataObjectModifDate(), JEXData.ADMIN);
		result.setAtt(JEXData.AUTHOR, data.getAuthor(), JEXData.ADMIN);
		
		TreeMap<DimensionMap,JEXDataSingle> datas = data.getDataMap();
		for (DimensionMap map: datas.keySet()){
			JEXDataSingle ds = (JEXDataSingle)datas.get(map);
			ds.setDimensionMap(map);
			XMLPreferences_XElement datasingle = JEXDBIO.getViewingXMLElement(ds, data.getDataObjectType());
			result.addXElement(datasingle);
		}

		return result;
	}
	

	public static XMLPreferences_XElement getViewingXMLElement(JEXDataSingle ds, String type)
	{
		XMLPreferences_XElement result = new XMLPreferences_XElement();

		// Make the meta bud
		Set<String> keys = ds.getDimensionMap().keySet();
		for (String key: keys){
			String index = ds.getDimensionMap().get(key);
			result.setAtt(key,index);
		}
		
		for (String key: ds.getDataMap().keySet()){
			String value = ds.getDataMap().get(key);
			
			if (value == null){
				// try to solve the problem
				if (key.equals(JEXDataSingle.UNIT)){
					result.setAtt(key,"");
				}
				else {
					JEXStatics.logManager.log("Problem with saving data object for key = "+key.equals(JEXDataSingle.UNIT), 1, null);
				}
				continue;
			}
			
			result.setAtt(key,value);
		}

		return result;
	}
	
}
