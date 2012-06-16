package Database.SingleUserDatabase;

import java.io.File;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import jex.statics.JEXStatics;
import utilities.FileUtility;
import utilities.Pair;
import utilities.XMLUtility;
import weka.core.converters.JEXTableReader2;
import weka.core.converters.JEXTableWriter2;
import Database.DBObjects.JEXData;
import Database.DBObjects.JEXDataSingle;
import Database.DBObjects.JEXEntry;
import Database.DBObjects.JEXWorkflow;
import Database.DBObjects.dimension.Dim;
import Database.DBObjects.dimension.DimTable;
import Database.DBObjects.dimension.DimensionMap;
import Database.DBObjects.dimension.Table;
import Database.DataReader.FileReader;
import Database.SingleUserDatabase.xml.ObjectFactory;
import Database.SingleUserDatabase.xml.XData;
import Database.SingleUserDatabase.xml.XDataSingle;
import cruncher.JEXFunction;

public class JEXDataIO {
	
	public static final String DETACHED_FILEEXTENSION = "csv";
	
	/**
	 * Update the location of this JEXData
	 * 
	 * This involves to steps 
	 * 	1) move any referenced files to the appropriate folder
	 * 	2) save/update a new detached object to the appropriate folder
	 * 
	 * Use the relative path currently saved in each JEXDataSingle to find the old location of the
	 * referenced files, then ask JEXWriter for the new location it should be based on the 
	 * experiment names etc... and move the file if the src and dst are different.
	 * @param data
	 * @return success flag
	 */
	public static TreeMap<String,Pair<String,String>> updateData(JEXData data)
	{
		// THIS ONLY GETS CALLED IF THIS DATA OBJECT WAS LOADED (NEW OBJECTS AND VIEWED OBJECTS)	(updateData IS CALLED FROM DatabaseObjectToXData which is called upon call to saveDB)
		// OR IF THE ENTRY THIS BELONGS TO NOW BELONGS TO AN EXPERIMENT OR TRAY WITH A DIFFERENT NAME
		// IN WHICH CASE WE NEED TO LOAD THE JEXDATA dataMap
		if(!data.isLoaded())// THIS IS JUST IN CASE
		{
			data.getDataMap();
		}
		
		TreeMap<String,Pair<String,String>> fileUpdatesToPerform = new TreeMap<String,Pair<String,String>>();
		
		// Determine if the data has referenced files
		String src = null, dst = null, temp = null, dstRelativePath = null;
		String type = data.getTypeName().getType();
		if (type.equals(JEXData.FILE) || type.equals(JEXData.IMAGE) || type.equals(JEXData.SOUND) || type.equals(JEXData.MOVIE) || type.equals(JEXData.TRACK))
		{
			// It has referenced files so those need to be moved along with the detached csv/arff
			// Get the datamap of JEXDataSingles for moving the data
			TreeMap<DimensionMap,JEXDataSingle> datamap = data.getDataMap();
			for(DimensionMap map : datamap.keySet())
			{
				// For each JEXDataSingle, move the referenced file
				JEXDataSingle ds = datamap.get(map);
				src = FileReader.readToPath(ds);
				temp = JEXWriter.getDatabaseFolder() + File.separator + JEXWriter.getUniqueRelativeTempPath(FileUtility.getFileNameExtension(src));
				dstRelativePath = JEXWriter.getDataFolder(data, true) + File.separator + JEXDataIO.createReferencedFileName(ds);
				dst = JEXWriter.getDatabaseFolder() + File.separator + dstRelativePath;
				fileUpdatesToPerform.put(src, new Pair<String,String>(temp,dst));
				// update the datasingle with the new path that the file will be saved at
				ds.put(JEXDataSingle.RELATIVEPATH, dstRelativePath);
			}
		}
		
		// Save a new version of the detached file in the current detached file location
		if(data.hasDetachedFile())
		{
			src = JEXWriter.getDatabaseFolder() + File.separator + data.getDetachedRelativePath();
		}
		else
		{
			src = JEXWriter.getDatabaseFolder() + File.separator + JEXWriter.getUniqueRelativeTempPath(JEXDataIO.DETACHED_FILEEXTENSION);
		}
		saveDetachedFileToLocation(data, src);
		// Get file paths for moving the detached file into the correct location
		temp = JEXWriter.getDatabaseFolder() + File.separator + JEXWriter.getUniqueRelativeTempPath(FileUtility.getFileNameExtension(src));
		dstRelativePath = JEXWriter.getDataFolder(data, true) + File.separator + JEXDataIO.createDetachedFileName(data);
		dst = JEXWriter.getDatabaseFolder() + File.separator + dstRelativePath;
		
		// update the data with the new detached path that the detached file will be saved at
		data.setDetachedRelativePath(dstRelativePath);
		fileUpdatesToPerform.put(src, new Pair<String,String>(temp,dst));
		data.unloadObject();
		return fileUpdatesToPerform;
	}
	
	public static boolean saveDetachedFileToLocation(JEXData data, String fullPath)
	{
		File dst = new File(fullPath);
		
		TreeMap<DimensionMap,JEXDataSingle> dataMap = data.getDataMap();
		TreeMap<DimensionMap,String> dataTable = new TreeMap<DimensionMap,String>();
		Dim metaDim = new Dim(JEXTableWriter2.METADATA);
		boolean firstSingle = true;
		for(DimensionMap map : dataMap.keySet())
		{
			DimensionMap newMap = map.copy();
			TreeMap<String,String> singleDataMap = dataMap.get(map).getDataMap();
			for(String singleKey : singleDataMap.keySet())
			{
				newMap.put(JEXTableWriter2.METADATA, singleKey);
				if(firstSingle)
				{
					metaDim.dimValues.add(singleKey);
				}
				dataTable.put(newMap, singleDataMap.get(singleKey));
			}
			firstSingle = false;
		}
		DimTable dimTable = data.getDimTable().copy();
		dimTable.add(metaDim);
		Table<String> tableToSave = new Table<String>(dimTable,dataTable);
		String sourcePath = JEXTableWriter2.writeTable(data.getTypeName().toString(), tableToSave, DETACHED_FILEEXTENSION);
		File src = new File(sourcePath);
		return FileUtility.copy(src, dst);
	}
	
	/**
	 * Returns the heirarchically and TypeName defined file name to save data
	 * in entry ENTRY
	 * @param type
	 * @param name
	 * @param entry
	 * @param f
	 * @return
	 */
	public static String createReferencedFileName(JEXDataSingle ds)
	{
		// Get the directory to save in
		JEXEntry entry = ds.getParent().getParent();
		
		// Get the row and column of the entry
		String row      = "" + entry.getTrayX();
		String column   = "" + entry.getTrayY();
		
		// Make the dimension string
		String dimStr   = "";
		DimensionMap dim = ds.getDimensionMap();
		if(dim != null)
		{
			for(String dimName: dim.keySet())
			{
				dimStr = dimStr + "_" + dimName + dim.get(dimName);
			}
		}
		
		// Get the extension
		String extension = FileUtility.getFileNameExtension(FileReader.readToPath(ds));
		
		// Construct a file name
		String fileName;
		if(dimStr.equals(""))
		{
			fileName = FileUtility.removeWhiteSpaceOnEnds(dimStr) + "x" + row + "_y" + column + "." + extension;
		}
		else
		{
			fileName = FileUtility.removeWhiteSpaceOnEnds(dimStr) + "x" + row + "_y" + column + "_" + dimStr + "." + extension;
		}
		
		
		return fileName;
	}
	
	/**
	 * Returns the heirarchically and TypeName defined file name to save data
	 * in entry ENTRY
	 * @param type
	 * @param name
	 * @param entry
	 * @param f
	 * @return
	 */
	public static String createDetachedFileName(JEXData data)
	{
		// Get the directory to save in
		JEXEntry entry = data.getParent();
		
		// Get the row and column of the entry
		String row      = "" + entry.getTrayX();
		String column   = "" + entry.getTrayY();
		
		// Get the extension
		String extension = "csv";
		
		// Construct a file name
		String fileName = "x" + row + "_y" + column + "." + extension;
		
		return fileName;
	}
	
	/**
	 * Just load the DimTable for this JEXData.
	 * @param data
	 */
	public static void loadDimTable(JEXData data)
	{
		if(data.hasDetachedFile())
		{
			// Then we could be dealing with either and old xml object or a new arff/csv object
			if(FileUtility.getFileNameExtension(data.getDetachedRelativePath()).equals("xml"))
			{
				// OLD XML VERSION OF JEXDATA FOUND
				JEXDataIO.loadDetachedXMLJEXDataDimTable(data);
			}
			else // THEN CSV/ARFF
			{
				DimTable dimTable = JEXTableReader2.getDimTable(JEXWriter.getDatabaseFolder() + File.separator + data.getDetachedRelativePath());
				dimTable.removeDimWithName(JEXTableWriter2.METADATA);
				data.setDimTable(dimTable);
			}
		}
		else
		{
			// Then we are dealing with an old xml object and all the info is already loaded
			// DON'T DO ANYTHING
		}
	}
	
	/**
	 * Load the DimTable and data map for this JEXData.
	 * @param data
	 */
	public static void loadDimTableAndDataMap(JEXData data)
	{
		if(data.hasDetachedFile())
		{
			// Then we could be dealing with either and old xml object or a new arff/csv object
			// We also need to load all the information
			if(FileUtility.getFileNameExtension(data.getDetachedRelativePath()).equals("xml"))
			{
				// OLD XML VERSION OF JEXDATA FOUND
				JEXDataIO.loadDetachedXMLJEXData(data);
			}
			else // THEN CSV/ARFF
			{
				JEXDataIO.loadDetachedARFFJEXData(data, JEXWriter.getDatabaseFolder() + File.separator + data.getDetachedRelativePath());
			}
		}
		else
		{
			// Then we are either dealing with an old old xml object and all the data was already loaded by JEXDBIO
		}
	}
	
	/**
	 * Build a JEXDataSingle from the information in the table. RETURNING NULL IF ANY METADATA IS MISSING
	 * @param data
	 * @param table
	 * @param map
	 * @param metaDim
	 * @return
	 */
	private static JEXDataSingle makeJEXDataSingle(Table<String> table, DimensionMap map, Dim metaDim)
	{
		JEXDataSingle ret = new JEXDataSingle();
		DimensionMap temp = map.copy();
		for(String metaKey : metaDim.dimValues)
		{
			temp.put(JEXTableWriter2.METADATA, metaKey);
//			JEXStatics.logManager.log("adding value to single", 0, JEXDataIO.class.getSimpleName());
			String metaValue = table.getData(temp);
			if(metaValue != null)
			{
//				JEXStatics.logManager.log("adding value to single", 0, JEXDataIO.class.getSimpleName());
				ret.put(metaKey,metaValue);
			}
		}
		if(ret.getDataMap().size() == 0)
		{
			return null;
		}
		return ret;
	}
	
	/**
	 * Load the detached XML version of the JEXData
	 */
	private static void loadDetachedXMLJEXData(JEXData data)
	{
		// Parse the xml object file
		JEXStatics.logManager.log("Parsing the object xml string "+data.getDetachedRelativePath(), 1, data);
		String loadPath = JEXWriter.getDatabaseFolder() + File.separator + data.getDetachedRelativePath();
		
		// Get the Xdata object
		XData             temp    = (XData) XMLUtility.XMLload(loadPath, new ObjectFactory());
		List<XDataSingle> singles = temp.getSingleDataElements();
		
		// Make the dimTable again
		String dataDims = temp.getAtt(JEXData.DIMS);
		if (dataDims != null){
			DimTable table = new DimTable(dataDims);
			data.setDimTable(table);
		}
		
		// Fill the datamap
		data.datamap  = new TreeMap<DimensionMap,JEXDataSingle>();
		for (XDataSingle ds: singles){
			JEXDataSingle dataSingle = JEXDBIO.XDataSingleToDatabaseObject(ds,data);
			if (dataSingle == null) continue;
			DimensionMap map = dataSingle.getDimensionMap();
			data.addData(map, dataSingle);
		}
		
		if(data.getTypeName().getType().equals(JEXData.FUNCTION_OLD))
		{
			JEXFunction func = JEXFunction.fromOldJEXData(data);
			JEXWorkflow workflow = new JEXWorkflow();
			workflow.add(func);
			JEXData newData = workflow.toJEXData(data.getDataObjectName());
			
			data.type = newData.type;
			data.info.clear();
			data.info.putAll(newData.info);
			data.setAuthor(newData.getAuthor());
			data.setDataObjectDate(newData.getDataObjectDate());
			data.setDataObjectModifDate(newData.getDataObjectModifDate());
			data.setDataID(newData.getDataID());
			
			data.setDimTable(newData.getDimTable());
			data.datamap.clear();
			data.datamap.putAll(newData.datamap);
		}
	}
	
	/**
	 * Load the detached XML version of the JEXData
	 */
	private static void loadDetachedXMLJEXDataDimTable(JEXData data)
	{
		// Parse the xml object file
		JEXStatics.logManager.log("Parsing the object xml string "+data.getDetachedRelativePath(), 1, data);
		String loadPath = JEXWriter.getDatabaseFolder() + File.separator + data.getDetachedRelativePath();
		
		// Get the Xdata object
		XData             temp    = (XData) XMLUtility.XMLload(loadPath, new ObjectFactory());
		
		// Make the dimTable again
		String dataDims = temp.getAtt(JEXData.DIMS);
		if (dataDims != null){
			DimTable table = new DimTable(dataDims);
			data.setDimTable(table);
		}
	}
	
	public static void loadDetachedARFFJEXData(JEXData data, String fullpath)
	{
		Table<String> table = JEXTableReader2.getStringTable(fullpath);
		DimTable temp = table.dimTable.copy();
		Dim metaDim = temp.removeDimWithName(JEXTableWriter2.METADATA);
		data.setDimTable(temp);
		data.datamap = new TreeMap<DimensionMap,JEXDataSingle>();
		if(temp.size() == 0)
		{
			DimensionMap map = new DimensionMap();
			JEXDataSingle ds = JEXDataIO.makeJEXDataSingle(table, map, metaDim);
			if(ds != null)
			{
				data.addData(map, ds);
			}
		}
		else
		{
			for(Entry<DimensionMap,String> entry : table.data.entrySet())
			{
				JEXDataSingle ds = JEXDataIO.makeJEXDataSingle(table, entry.getKey(), metaDim);
				if(ds != null)
				{
					//JEXStatics.logManager.log(ds.toString(), 0, JEXDataIO.class.getSimpleName());
					data.addData(entry.getKey(), ds);
				}
			}
		}
		return;
	}
}
