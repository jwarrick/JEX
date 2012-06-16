package Database.DBObjects.dimension;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import utilities.Copiable;
import utilities.SSVList;
import utilities.StringUtility;
import weka.core.Attribute;

public class DimTable extends ArrayList<Dim> implements Copiable<DimTable> {
	private static final long serialVersionUID = 1L;

	public DimTable(){
		super();
	}

	/**
	 * Load from a Dim list, this makes a deep copy
	 * @param dims
	 */
	public DimTable(List<Dim> dims){
		this();
		for(Dim d : dims)
		{
			this.add(d.copy());
		}
	}
	
	/**
	 * Load from a SSV-CSV string
	 * @param csvString
	 */
	public DimTable(String csvString)
	{
		this();
		if (csvString != null && !csvString.equals("")){
			SSVList   ssvDim   = new SSVList(csvString);
			for (String dimStr: ssvDim){
				Dim dim = new Dim(dimStr);
				this.add(dim);
			}
		}
	}
	
	/**
	 * Make a dimension table from the datamap of a JEXData
	 * @param datamap
	 */
	public DimTable(TreeMap<DimensionMap,?> datamap){
		this();
		if (datamap == null || datamap.size() == 0) return;
		
		// Make a set version of the dimension map, as it is easy to fill
		TreeMap<String,TreeSet<String>> dimset = new TreeMap<String,TreeSet<String>>(new StringUtility());
		for (DimensionMap dim: datamap.keySet()){
			for (String dimName: dim.keySet()){
				String dimValue = dim.get(dimName);
				TreeSet<String> dimValues = dimset.get(dimName);
				
				// if the dim doesn't exist, create it
				if (dimValues == null){
					dimValues = new TreeSet<String>(new StringUtility());
					dimset.put(dimName, dimValues);
				}
				
				dimValues.add(dimValue);
			}
		}
		
		// Convert it to the proper list of dims
		for (String dimName: dimset.keySet())
		{
			TreeSet<String> dimValues = dimset.get(dimName);
			String[] dimValueArray = dimValues.toArray(new String[0]);
			Dim dim = new Dim(dimName,dimValueArray);
			this.add(dim);
		}
	}
	
	// Getters ans setters	
	/**
	 * Return a parsable string representation of the dimtable
	 */
	public String toCSVString(){
		SSVList simSSV = new SSVList();
		for (Dim dim: this){
			simSSV.add(dim.toCSVString());
		}
		return simSSV.toString();
	}
	
	/**
	 * Return a list of the dimension names in the dimtable
	 * @return
	 */
	public List<String> getDimensionNames(){
		List<String> result = new ArrayList<String>(0);
		for (Dim dim: this){
			result.add(dim.name());
		}
		return result;
	}
	
	public int indexOfDimWithName(String dimName)
	{
		return this.getDimensionNames().indexOf(dimName);
	}
	
	/**
	 * Return the dimension with name NAME
	 * @param name
	 * @return
	 */
	public Dim getDimWithName(String name){
		for (Dim dim: this){
			if (dim.name().equals(name)) return dim;
		}
		return null;
	}
	
	/**
	 * Remove the dimension with name NAME
	 * @param name
	 * @return
	 */
	public Dim removeDimWithName(String name)
	{
		Dim toRemove = null;
		for (Dim dim: this){
			if (dim.name().equals(name))
			{
				toRemove = dim;
				break;
			}
		}
		if(toRemove == null)
		{
			return null;
		}
		this.remove(toRemove);
		return toRemove;
	}
	
	/**
	 * Return the values for dimension NAME in an array form
	 * @param name
	 * @return
	 */
	public String[] getValueArrayOfDimension(String name){
		Dim dim = this.getDimWithName(name);
		String[] result = dim.valueArray();
		return result;
	}
	
	/**
	 * Return the values for dimension NAME 
	 * @param name
	 * @return
	 */
	public List<String> getValuesOfDimension(String name){
		Dim dim = this.getDimWithName(name);
		List<String> result = dim.values();
		return result;
	}
	
	/**
	 * Return all possible DimensionMaps for this DimTable in order
	 */
	public List<DimensionMap> getDimensionMaps()
	{
		List<DimensionMap> ret = new ArrayList<DimensionMap>();
		for(DimensionMap map : this.getIterator())
		{
			ret.add(map);
		}
		return ret;
	}
	
	/**
	 * Return all possible DimensionMaps for this DimTable in order
	 */
	public List<DimensionMap> getDimensionMaps(DimensionMap filter)
	{
		List<DimensionMap> ret = new ArrayList<DimensionMap>();
		for(DimensionMap map : this.getIterator(filter))
		{
			ret.add(map);
		}
		return ret;
	}
	
	public DimTable getSubTable(DimensionMap filter)
	{
		if(!this.hasDimensionMap(filter)) return null;
		
		DimTable ret = new DimTable();
		for(Dim d : this)
		{
			if(filter.get(d.name()) != null)
			{
				Dim toAdd = new Dim(d.name(),filter.get(d.name()));
				ret.add(toAdd);
			}
			else
			{
				ret.add(d.copy());
			}
		}
		return ret;
	}
	
	public ArrayList<Attribute> getArffAttributeList()
	{
		ArrayList<Attribute> ret = new ArrayList<Attribute>();
		for(Dim dim : this)
		{
			ret.add(dim.toArffAttribute());
		}
		return ret;
	}
	
	/**
	 * Return the number of dimensions in the dim table
	 */
	@Override
	public int size()
	{
		return super.size();
	}
	
	public int mapCount()
	{
		if(this.size() == 0)
		{
			return 0;
		}
		int total = this.get(0).size();
		for(int i = 1; i< this.size(); i++)
		{
			total = total * this.get(i).size();
		}
		return total;
	}
	
	/**
	 * DimTables just need to have the same dims (not necessarily in the
	 * same order) and each matching dim must have the same values (in 
	 * the same order).
	 * @param table2
	 * @return
	 */
	public boolean matches(DimTable table2)
	{
		if(this.size() != table2.size()) return false;
		Dim dim2;
		for(Dim dim1 : this)
		{
			dim2 = table2.getDimWithName(dim1.name());
			if(dim2 == null || !dim2.values().equals(dim1.values())) return false;
		}
		return true;
	}
	
	public boolean hasDimensionMap(DimensionMap map)
	{
		// WE DO THINGS THIS WAY SO WE DONT HAVE TO SEARCH FOR THE DIM WITH THE MATCHING NAME OF THE DIMENSIONMAP KEY OVER AND OVER
		// INSTEAD LOOP THROUGH THE DIMS ONLY ONCE AND KEEP TRACK OF THE NUMBER OF MATCHES RETURNING FALSE WHERE THE DIM DOESNT HAVE THE CORRESPONDING DIMENSIONMAP VALUE
		Set<String> mapKeys = map.keySet();
		int n = mapKeys.size();
		int count = 0;
		for(Dim d : this)
		{
			if(mapKeys.contains(d.dimName))
			{
				if(d.containsValue(map.get(d.dimName)))
				{
					count = count + 1;
				}
				else
				{
					return false;
				}
			} // OTHERWISE SKIP
		}
		return count == n; // I.E. WE FOUND A DIM THAT CONTAINS THE DIMENSIONMAP VALUE FOR EACH VALUE IN THE DIMENSION MAP
	}
	
	// Methods
	/**
	 * returned DimTable contains no references to any of the DimTables in tables or their values. (deep copy)
	 * @param table1
	 * @param table2
	 * @return
	 */
	public static DimTable union(List<DimTable> tables){
		DimTable result = new DimTable();
		for (DimTable t: tables){
			result = union(result,t);
		}
		return result;
	}

	/**
	 * returned DimTable contains no references to table1 and table2 or their values. (deep copy)
	 * @param table1
	 * @param table2
	 * @return
	 */
	public static DimTable union(DimTable table1, DimTable table2)
	{
		DimTable result = new DimTable();
		
		// add all dimensions from table 1 and combine with any from table2 if needed
		Dim temp;
		for (Dim dim1: table1){
			temp = table2.getDimWithName(dim1.name());
			if(temp != null)
			{
				temp = Dim.union(dim1, temp); // union returns a totally new dim (deep copy)
				result.add(temp);
			}
			else
			{
				result.add(dim1.copy());
			}
		}
		
		// add all dimensions from table2 that weren't found in table1
		for (Dim dim2: table2){
			if (result.getDimWithName(dim2.name()) != null) continue;
			// else we know that table1 doesn't contain this dim and it can be added as is
			result.add(dim2.copy());
		}
		
		return result;
	}

	/**
	 * returned DimTable contains no references to table1 and table2 or their values. (deep copy)
	 * @param table1
	 * @param table2
	 * @return
	 */
	public static DimTable intersect(List<DimTable> tables){
		if (tables == null || tables.size() == 0) return new DimTable();
		DimTable result = tables.get(0);
		for (DimTable t: tables){
			result = intersect(result,t);
		}
		return result;
	}

	/**
	 * returned DimTable contains no references to table1 and table2 or their values. (deep copy)
	 * @param table1
	 * @param table2
	 * @return
	 */
	public static DimTable intersect(DimTable table1, DimTable table2)
	{
		// first intersect dimNames and then dimValues of matches
		DimTable ret = new DimTable();
		for(Dim d : table1)
		{
			if(table2.getDimWithName(d.name()) != null)
			{
				ret.add(d); // will copy dim during next intersection step.
			}
		}
		// then intersect those dims with matching names
		for(Dim d : ret)
		{
			d = Dim.intersect(d, table2.getDimWithName(d.name())); // d is now replaced by a standalone fresh Dim (deep copy)
		}
		return ret;
	}
	
//	class NumString implements Comparable<NumString>{
//		private String str;
//		
//		NumString(String str){
//			this.str = str;
//		}
//		
//		public String string(){
//			return str;
//		}
//
//		public int compareTo(NumString o) {
//			try {
//				// try to cast these to numbers
//				Integer i1 = new Integer(str);
//				Integer i2 = new Integer(o.string());
//				int compare = i1.compareTo(i2);
//				if (compare > 0) return 1;
//				if (compare < 0) return -1;
//				
//			} catch (Exception e){
//				int compare = str.compareTo(o.string());
//				if (compare > 0) return 1;
//				if (compare < 0) return -1;
//			}
//			return 0;
//		}
//	}

	public DimTableMapIterable getIterator()
	{
		return new DimTableMapIterable(this);
	}
	
	public DimTableMapIterable getIterator(DimensionMap filter)
	{
		return new DimTableMapIterable(this, filter, 0);
	}
	
	public DimTableMapIterable getIterator(DimensionMap filter, int skipN)
	{
		return new DimTableMapIterable(this, filter, skipN);
	}
	
	public DimTable copy()
	{
		DimTable ret = new DimTable();
		for(Dim d : this)
		{
			ret.add(d.copy());
		}
		return ret;
	}

}
