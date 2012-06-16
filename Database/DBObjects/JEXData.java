package Database.DBObjects;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import jex.statics.JEXStatics;
import utilities.CSVList;
import utilities.DateUtility;
import Database.DBObjects.dimension.Dim;
import Database.DBObjects.dimension.DimTable;
import Database.DBObjects.dimension.DimensionMap;
import Database.Definition.TypeName;
import Database.SingleUserDatabase.JEXDataIO;

public class JEXData {
	// JEXData types
	public static String IMAGE     = "Image";
	public static String FILE      = "File";
	public static String MOVIE     = "Movie";
	public static String SOUND     = "Sound";
	public static String VALUE     = "Value";
	public static String LABEL     = "Label";
	public static String FUNCTION_OLD  = "Function";
	public static String WORKFLOW  = "Workflow";
	public static String ROI       = "Roi";
	public static String HIERARCHY = "Hierarchy";
	public static String TRACK     = "Track";
	
	// JEXData fields
	public static String NAME         = "Name";
	public static String TYPE         = "Type";
	public static String ADMIN		  = "Adm";
	public static String INFO         = "Info";
	public static String DATE         = "Date";
	public static String MDATE        = "ModifDate";
	public static String DIMS         = "Dimensions";
	public static String DETACHED_RELATIVEPATH = "DetachedPath";
	public static String AUTHOR       = "Author";
	public static String DICTKEY      = "Dictionary Key";
	
	// variables
	public String                              type;
	public String                              name;
	public HashMap<String,String>              info = new HashMap<String,String>();;
	public TreeMap<DimensionMap,JEXDataSingle> datamap = null;
	public DimTable                            dimTable;
	public String                              eid;
	
	protected JEXEntry entry;
	private    String     detachedRelativePath = null;
	
	public JEXData(String type, String name)
	{
		this.type = type;
		this.name = name;
	}
	
	public JEXData(TypeName tn)
	{
		this.type = tn.getType();
		this.name = tn.getName();
	}
	
	public JEXData(String type, String name, String info)
	{
		this.type = type;
		this.name = name;
		this.info.put(JEXEntry.INFO, info);
	}
	
	/**
	 * Use this method to perform a deep copy of the jexdata object
	 * EXCEPT FOR LOAD NAME BECAUSE WE ARE CREATING NEW THINGS HERE
	 * THAT DON'T HAVE A LOAD LOCATION. WE ARE CREATING THEM. ALL
	 * DETACHED FILES ARE WRITTEN ANEW ANYWAYS. THE ONLY THING THAT
	 * WILL BE BAD IS FILE PATHS REFERENCED IN THE DETACHED FILE
	 * WILL NOT BE DUPLICATED. IN ORDER TO MAKE THIS A TRUE DEEP COPY
	 * WE WOULD HAVE TO COPY THE INFORMATION REFERENCED IN THE 
	 * JEXDATASINGLE AS WELL. WHICH I DON'T THINK MAKES SENSE.
	 * @param jd
	 */
	public JEXData(JEXData jd){
		this(jd.getDataObjectType(),jd.getDataObjectName(),jd.getDataObjectInfo());
		this.setAuthor(jd.getAuthor());
		this.setDataObjectDate(jd.getDataObjectDate());
		this.setDataObjectModifDate(jd.getDataObjectModifDate());
		this.setDataID(jd.getDataID());
		this.setDimTable(jd.getDimTable());
		
		TreeMap<DimensionMap,JEXDataSingle> datamap = jd.getDataMap();
		for (DimensionMap map: datamap.keySet()){
			// get the datasingle
			JEXDataSingle thisDS = datamap.get(map);
			
			// create the xml-izable datasingle
			if(thisDS == null) continue;
			JEXDataSingle ds = new JEXDataSingle();
			for (String key: thisDS.getDataMap().keySet()){
				ds.put(key, thisDS.get(key));
			}
			
			this.addData(map, ds);
		}
	}
	
	/**
	 * Return the value of key KEY in the info map
	 * @param key
	 * @return Value of Key KEY
	 */
	public String get(String key){
		return info.get(key);
	}

	/**
	 * Set the info field KEY with value VALUE
	 * @param key
	 * @param value
	 */
	public void put(String key, String value){
		this.info.put(key, value);
	}
	
	// ----------------------------------------------------
	// --------- GETTERS AND SETTERS ----------------------
	// ----------------------------------------------------
	
	/**
	 * Return the unique ID of this data object
	 * @return String ID
	 */
	public String getDataID(){
		return info.get(JEXEntry.EID);
	}
	
	/**
	 * Set the ID of this data object
	 * @param id
	 */
	public void setDataID(String id){
		info.put(JEXEntry.EID, id);
	}
	
	/**
	 * Return the name of the data object
	 * @return
	 */
	public String getDataObjectName(){
		return this.name;
	}
	
	/**
	 * Set the dataobject name
	 * @param name
	 */
	public void setDataObjectName(String name){
		this.name = name;
	}
	
	/**
	 * Return the dataobject type
	 * @return type
	 */
	public String getDataObjectType(){
		return this.type;
	}
	
	/**
	 * Set the dataobject type
	 * @param type
	 */
	public void setDataObjectType(String type){
		this.type = type;
	}
	
	/**
	 * Return the typename of the data object
	 * @return
	 */
	public TypeName getTypeName(){
		return new TypeName(type,name);
	}
	
	public String getDetachedRelativePath()
	{
		return this.detachedRelativePath;
	}
	
	public void setDetachedRelativePath(String relativePath)
	{
		this.detachedRelativePath = relativePath;
	}
	
//	/**
//	 * Set the ID of the entry containing this data object
//	 * @param eid
//	 */
//	public void setEntryID(String eid){
//		this.eid = eid;
//	}
	
	/**
	 * Return the author of this object
	 * @return
	 */
	public String getAuthor(){
		String result = info.get(JEXData.AUTHOR);
		if (result == null) setAuthor("No author");
		return info.get(JEXData.AUTHOR);
	}
	
	/**
	 * Set the author of this dat aobject
	 * @param author
	 */
	public void setAuthor(String author){
		this.info.put(JEXData.AUTHOR, author);
	}

	/**
	 * Set the object info field
	 * @param info
	 */
	public void setDataObjectInfo(String info){
		this.put(JEXData.INFO, info);
	}
	
	/**
	 * Return the object info field
	 * @return Object info
	 */
	public String getDataObjectInfo(){
		String info = this.get(JEXData.INFO);
		if (info == null || info.equals("NA") || info.equals("")) setDataObjectInfo("No info available");
		return this.get(JEXData.INFO);
	}
	
	/**
	 * Set the object date field
	 * @param date
	 */
	public void setDataObjectDate(String date){
		this.put(JEXData.DATE, date);
	}

	/**
	 * Set the object date field
	 * @param date
	 */
	public void setDataObjectModifDate(String modifdate){
		this.put(JEXData.MDATE, modifdate);
	}
	
	/**
	 * Return the object date field
	 * @return Object date
	 */
	public String getDataObjectDate(){
		String date = this.get(JEXData.DATE);
		if (date == null || date.equals("NA") || date.equals("")) setDataObjectDate(DateUtility.getDate());
		return this.get(JEXData.DATE);
	}

	/**
	 * Return the object date field
	 * @return Object date
	 */
	public String getDataObjectModifDate(){
		String modifdate = this.get(JEXData.MDATE);
		if (modifdate == null || modifdate.equals("NA") || modifdate.equals("")) setDataObjectModifDate(DateUtility.getDate());
		return this.get(JEXData.MDATE);
	}
	
//	/**
//	 * Set the locality flag of the data object
//	 * @param isLocal
//	 */
//	public void setIsLocal(boolean isLocal)
//	{
//		this.isLocal = isLocal;
//	}
	
//	/**
//	 * return the locality flag of the data object
//	 * (true if all the files associated are saved in the preset database structure
//	 * @return
//	 */
//	public boolean isLocal()
//	{
//		return this.isLocal;
//	}
	
	/**
	 * return whether this object has a dettached path
	 * This method is kept around in case we run into an old
	 * version where JEXData was saved as xml and could be either detached or not
	 */
	public boolean hasDetachedFile()
	{
		return !(this.detachedRelativePath == null || this.detachedRelativePath.equals(""));
	}
	
	/**
	 * Set the parent entry this data is contained in
	 * @param entry
	 */
	public void setParent(JEXEntry entry){
		this.entry = entry;
	}
	
	/**
	 * Return the entry this data is contained in
	 * @return parent entry
	 */
	public JEXEntry getParent(){
		return this.entry;
	}
	
	// ----------------------------------------------------
	// --------- LOADING AND GETTING THE DATAMAP ----------
	// ----------------------------------------------------

	/**
	 * Return the loading status of this object
	 * @return boolean
	 */
	public boolean isLoaded(){
		return this.datamap != null;
	}

	/**
	 * Unload object from memory
	 */
	public void unloadObject()
	{
		// Release the data from the memory
		this.datamap  = null;
	}
	
	// ----------------------------------------------------
	// --------- DATA AND DATA TABLE ----------------------
	// ----------------------------------------------------
	
	/**
	 * Put the value VALUE at the location MAP in the data object
	 * @param map
	 * @param value
	 */
	public void addData(DimensionMap map, JEXDataSingle value){
		if(value != null)
		{
			value.setParent(this);
			value.setDimensionMap(map);
			this.getDataMap().put(map, value);
		}
	}
	
	/**
	 * Return the value at location MAP
	 * @param map
	 * @return
	 */
	public JEXDataSingle getData(DimensionMap map){
		if (map == null) return null;
		return this.getDataMap().get(map);
	}

	/**
	 * Return the first datasingle of the list
	 * @return the first data single
	 */
	public JEXDataSingle getFirstSingle()
	{
		if (getDataMap().size()==0)
		{
			return null;
		}
		JEXDataSingle ds = getDataMap().firstEntry().getValue();
		return ds;
	}
	
	/**
	 * Return the datamap
	 * @return
	 */
	public TreeMap<DimensionMap,JEXDataSingle> getDataMap()
	{
		if (!this.isLoaded() && !this.hasDetachedFile())
		{
			// THEN IT IS A NEW OBJECT AND WE CAN CREATE AN EMPTY DATA MAP
			this.datamap = new TreeMap<DimensionMap,JEXDataSingle>();
		}
		else if(!this.isLoaded() && this.hasDetachedFile())
		{
			// THEN CREATE AND FILL THIS JEXDATAs DATAMAP FROM THE DETACHED FILE
			JEXDataIO.loadDimTableAndDataMap(this);
		}
		
		// OTHERWISE THIS IS A PREVIOUSLY LOADED FILE THAT HAS A DETACHED FILE
		// OR IT IS AN OLD XML FILE THAT HAS BEEN LOADED BUT DOESN'T HAVE A DETACHED FILE
		return this.datamap;
	}
	
	/**
	 * Returns the current dim table
	 * @return
	 */
	public DimTable getDimTable()
	{
//////// This is the new code.
		if (this.dimTable == null)
		{
			if(this.hasDetachedFile())
			{
				JEXDataIO.loadDimTable(this);
			}
			else // This is a new object (typically from importing) that isn't set with a dimTable
			{
				this.dimTable = new DimTable(this.getDataMap());
			}
		}
		return this.dimTable;
		
//////// This is the old version
//		if (dimTable == null){
//			// Make a new dimtable
//			DimTable table = new DimTable(this.getDataMap());
//			
//			// Set the dim table
//			this.setDimTable(table);
//		}
//		return this.dimTable.copy();
		
	}
	
	/**
	 * Sets the dimtable 
	 * @param dimTable
	 */
	public void setDimTable(DimTable dimTable){
		this.dimTable = dimTable;
	}

	/**
	 * Get the dimension size with name dimName
	 * @param dimension
	 * @return dimension Size
	 */
	public int getDimensionSize(String dimName){
		for (Dim dim: getDimTable()){
			String dimName2 = dim.name();
			if (dimName2.equals(dimName)){
				return dim.valueArray().length;
			}
		}
		return 0;
	}
	
	/**
	 * Return the list of dimension in form of a CSV list
	 * @return CSVLIst of dimensions
	 */
	public CSVList getDimensionCSV(){
		CSVList result = new CSVList();
		for (Dim dim: getDimTable()){
			result.add(dim.name());
		}
		return result;
	}

	// ----------------------------------------------------
	// --------- OUTPUTTING AND EXPORT --------------------
	// ----------------------------------------------------
	
	/**
	 * Return a string CSV version of this jex data
	 */
	public String exportToCSV(){
		String result = "";
		
		List<Dim> dim = this.getDimTable();
		if (dim.size() == 0) result = result + getCSVforDimension(-1,dim);
		else result = result + getCSVforDimension(0,dim);
		
		return result;
	}
	
	/**
	 * Make a string for exporting
	 * @param index
	 * @param dims
	 * @return
	 */
	private String getCSVforDimension(int index, List<Dim> dims){
		String result = "";
		
		if (dims.size() == 0){
			JEXStatics.logManager.log("Creating 0 dimension csv", 0, this);
			JEXDataSingle ds = this.getFirstSingle();
			result           = ds.exportToCSV();
		}
		else if (dims.size() == 1){
			JEXStatics.logManager.log("Creating 1 dimension csv", 0, this);
			
			TreeMap<DimensionMap,JEXDataSingle> datas = this.getDataMap();
			result = result + dims.get(0).name();
			for (DimensionMap map: datas.keySet()){
				JEXDataSingle ds = datas.get(map);
				result = result + "," + ds.exportToCSV();
			}
		}
		else if (dims.size() == 2){
			JEXStatics.logManager.log("Creating 2 dimension csv", 0, this);
			
			Dim dim1 = dims.get(0);
			Dim dim2 = dims.get(1);
			String[] values1 = dim1.valueArray();
			String[] values2 = dim2.valueArray();
			
			for (String value1: values1){
				result = result + dim1.name() + "-" + value1;
				
				for (String value2: values2){
					DimensionMap map = new DimensionMap();
					map.put(dim1.name(), ""+value1);
					map.put(dim2.name(), ""+value2);
					
					JEXDataSingle ds = this.getData(map);
					result = result + "," + ds.exportToCSV();
				}
				result = result + "\n";
			}
		}
		else {
			JEXStatics.logManager.log("Creating >2 dimension csv", 0, this);
			TreeMap<DimensionMap,JEXDataSingle> datas = this.getDataMap();
			for (DimensionMap map: datas.keySet()){
				JEXDataSingle ds = datas.get(map);
				result = result + "," + ds.exportToCSV();
			}
		}

		result = result + "\n";
		return result;
	}
	
	/**
	 * Set the dictionary value of this jexmldata
	 * @param dictValue
	 */
	public void setDictionaryValue(String dictValue){
		this.put(JEXData.DICTKEY, dictValue);
	}
	
	/**
	 * Return a string identifying the data object to file in a dictionary
	 * @return
	 */
	public String getDictionaryValue(){
		String result = this.get(JEXData.DICTKEY);

		// If the dictionary value is set return it
		if (result != null) return result;
		
		// Else load the object rebuild the dictionary key and set it
		else 
		{
			JEXDataSingle ds = this.getFirstSingle();
			result = ds.toString();
			this.setDictionaryValue(result);
		}
		
		return result;
	}
	
	// TEST
	public void print(){
		TreeMap<DimensionMap,JEXDataSingle> datas = this.getDataMap();
		JEXStatics.logManager.log("---- Printing ----",1,this);
		for (DimensionMap map: datas.keySet()){
			JEXDataSingle ds = datas.get(map);
			JEXStatics.logManager.log(ds.toString(),1,this);
		}
		JEXStatics.logManager.log("---- End of print ----",1,this);
	}
	
	/**
	 * Return a string desription of the object
	 */
	@Override
	public String toString(){
		String result = "";
		
		JEXDataSingle ds = this.getFirstSingle();
		if (ds == null) result = "";
		else result = ds.toString();
		
		return result;
	}
	
	// ----------------------------------------------------
	// --------- INTERACTION ------------------------------
	// ----------------------------------------------------
	
	/**
	 * Return true if jex should open all selected datas in default program when prompted
	 * @return boolean
	 */
	public boolean openMulti(){
		return false;
	}
	
	/**
	 * Open data in default program
	 */
	public void openInDefaultProgram(){
		return;
	}
}
