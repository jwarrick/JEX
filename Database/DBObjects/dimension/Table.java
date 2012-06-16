package Database.DBObjects.dimension;

import java.util.Iterator;
import java.util.TreeMap;

import weka.core.converters.JEXTableReader2;


public class Table<E> implements Iterable<DimensionMap>{
	
	public DimTable dimTable;
	public TreeMap<DimensionMap,E> data;
	
	public Table(DimTable dimTable, TreeMap<DimensionMap,E> table)
	{
		this.dimTable = dimTable;
		this.data = table;
	}
	
	public Iterator<DimensionMap> iterator()
	{
		return this.dimTable.getIterator().iterator();
	}
	
	public DimTableMapIterable getIterator(DimensionMap filter)
	{
		return this.dimTable.getIterator(filter);
	}
	
	public E getData(DimensionMap key)
	{
		return this.data.get(key);
	}
	
	public TreeMap<DimensionMap,E> getFilteredData(DimensionMap filter)
	{
		return JEXTableReader2.filter(this.data, filter);
	}

}
