package Database.DBObjects;

import java.io.File;
import java.util.Set;
import java.util.TreeMap;

import Database.DBObjects.dimension.DimensionMap;
import Database.SingleUserDatabase.JEXWriter;

public class JEXDataSingle {
	
	public static String NAME        = "Name";
	public static String RELATIVEPATH    = "FileName";
//	public static String FOLDERNAME  = "FolderName";
//	public static String ISFILED     = "IsFiled";
	public static String VALUE       = "Value";
	public static String UNIT        = "Unit";
	public static String POINTLIST   = "polygonPts";
	public static String PATTERN	 = "patternPts";
	public static String ORIGINPOINT = "Origin";
	public static String ROITYPE     = "Type";
	public static String ROISUBTYPE  = "SubType";
	
	TreeMap<String,String> data;
	DimensionMap           map ;
	JEXData                parent;
	
	public JEXDataSingle(){
		data = new TreeMap<String,String>();
	}
	
	public JEXDataSingle(String value){
		data = new TreeMap<String,String>();
		this.setValue(value);
	}
	
//	public JEXDataSingle(JEXData parent){
//		this();
//		this.parent = parent;
//	}
//	
//	public JEXDataSingle(JEXData parent, String value){
//		this(value);
//		this.parent = parent;
//	}
	
	/**
	 * Return the key set of this data single
	 * @return key set
	 */
	public Set<String> getKeys(){
		return data.keySet();
	}
	
	/**
	 * Return the information for string KEY
	 * @param key
	 * @return
	 */
	public String get(String key){
		return data.get(key);
	}
	
	/**
	 * Set the string 
	 * @param key
	 * @param value
	 */
	public void put(String key, String value){
		data.put(key, value);
	}
	
	/**
	 * Return the value of this JEXDataSingle
	 * @return
	 */
	public String getValue(){
		return get(VALUE);
	}
	
	/**
	 * Set the value of this JEXDataSingle
	 * @param value
	 */
	public void setValue(String value){
		put(VALUE,value);
	}
	
	/**
	 * Return the data
	 * @return
	 */
	public TreeMap<String,String> getDataMap(){
		return this.data;
	}
	

	/**
	 * Set the dimension map key to the datasingle
	 * @param map
	 */
	public void setDimensionMap(DimensionMap map){
		this.map = map;
	}
	
	/**
	 * return the dimension map of this datasingle
	 * @return
	 */
	public DimensionMap getDimensionMap(){
		return this.map;
	}
	
	/**
	 * Sets the parent dataobject of this datasingle
	 * @param parent
	 */
	public void setParent(JEXData parent){
		this.parent = parent;
	}
	
	/**
	 * Returns the parent data object of this data single
	 * @return
	 */
	public JEXData getParent(){
		return this.parent;
	}

	// ----------------------------------------------------
	// --------- OUTPUTTING AND EXPORT --------------------
	// ----------------------------------------------------
	
	/**
	 * Return a CSV formated string of the jexDatasingle
	 */
	public String exportToCSV(){
		String result = ""+getValue();
		return result;
	}

	public String toString(){
		String type = (parent==null)? JEXData.VALUE : parent.getDataObjectType();
		String result = "";
		if (type.equals(JEXData.VALUE)){
			result = this.get(VALUE);
		}
		else if (type.equals(JEXData.FILE)){
			result = JEXWriter.getDatabaseFolder() + File.separator + this.get(RELATIVEPATH);
		}
		else if (type.equals(JEXData.IMAGE)){
			result = JEXWriter.getDatabaseFolder() + File.separator + this.get(RELATIVEPATH);
		}
		else if (type.equals(JEXData.SOUND)){
			result = JEXWriter.getDatabaseFolder() + File.separator + this.get(RELATIVEPATH);
		}
		else if (type.equals(JEXData.MOVIE)){
			result = JEXWriter.getDatabaseFolder() + File.separator + this.get(RELATIVEPATH);
		}
		else if (type.equals(JEXData.HIERARCHY)){
			result = this.get(VALUE);
		}
		else if (type.equals(JEXData.LABEL)){
			result = this.get(VALUE);
		}
		else if (type.equals(JEXData.TRACK)){
			result = JEXWriter.getDatabaseFolder() + File.separator + this.get(RELATIVEPATH);
		}
		else {
			for (String key: this.getDataMap().keySet()){
				result = result + "[" + key + "=" + this.getDataMap().get(key) + "]";
			}
		}
		
		return result;
	}
}
