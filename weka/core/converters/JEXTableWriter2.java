package weka.core.converters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import jex.statics.JEXStatics;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.Utils;
import Database.DBObjects.dimension.Dim;
import Database.DBObjects.dimension.DimTable;
import Database.DBObjects.dimension.DimensionMap;
import Database.DBObjects.dimension.Table;
import Database.SingleUserDatabase.JEXWriter;

public class JEXTableWriter2 {
	
	public final static String VALUE = "Value";
	public final static String METADATA = "Metadata";
	public final static String ARFF_FILE = "arff", CSV_FILE = "csv", TXT_FILE = "txt";
	
	public PrintWriter writer;
	public String filePath, tableName;
	public DimTable dimTable;
	public Instances instances;
	public String valueHeaderName = VALUE;
	public static int precisionAfterDecPoint = 6;
	
	private int rowCounter = 0;
	
	public static <E> String writeTable(String tableName, Table<E> table)
	{
		return JEXTableWriter2.writeTable(tableName, table, ARFF_FILE);
	}
	
	public static <E> String writeTable(String tableName, Table<E> table, String fileExtension)
	{
		JEXTableWriter2 writer = new JEXTableWriter2(tableName, fileExtension);
		writer.writeTable(table);
		return writer.getPath();
	}
	
	public static <E> String writeTable(String tableName, TreeMap<DimensionMap,E> data)
	{
		DimTable dimTable = new DimTable(data);
		return JEXTableWriter2.writeTable(tableName, dimTable, data, ARFF_FILE);
	}
	
	public static <E> String writeTable(String tableName, DimTable dimTable, TreeMap<DimensionMap,E> data)
	{
		return JEXTableWriter2.writeTable(tableName, dimTable, data, ARFF_FILE);
	}
	
	public static <E> String writeTable(String tableName, DimTable dimTable, TreeMap<DimensionMap,E> data, String fileExtension)
	{
		return JEXTableWriter2.writeTable(tableName, new Table<E>(dimTable,data), fileExtension);
	}
	
	public JEXTableWriter2(String tableName)
	{
		this(tableName, ARFF_FILE);
	}
	
	public JEXTableWriter2(String tableName, String fileExtension)
	{
		this.tableName = tableName;
		if(JEXStatics.logManager == null)
		{
			this.filePath = "/Users/warrick/Desktop/Octave Scripts/CellTracking/LatterTest.arff";
		}
		else
		{
			this.filePath = JEXWriter.getDatabaseFolder() + File.separator + JEXWriter.getUniqueRelativeTempPath(fileExtension);
		}
	}	

	/**
	 * Every possible dimension map in the the dimTable must have a row even if the value is blank (i.e. "")
	 * In other words, be sure to completely fill the table otherwise rows and dimensionMaps won't match up
	 * This matching is used to make reading MUCH faster
	 * @param filter
	 * @param data
	 */
	public <E> void writeTable(Table<E> table)
	{
		if(table.data.firstEntry().getValue() instanceof String)
		{
			this.writeStringTableHeader(table.dimTable);
			this.writeData(table.data);
		}
		else
		{
			this.writeNumericTableHeader(table.dimTable);
			this.writeData(table.data);
		}
		this.close();
	}
	
	/**
	 * Every possible dimension map must have a row even if the value is blank (i.e. "")
	 * @param filter
	 * @param data
	 */
	public <E> void writeTable(DimTable dimTable, TreeMap<DimensionMap,E> data)
	{
		this.writeTable(new Table<E>(dimTable,data));
	}
	
	public void setAlternateFileOutputPath(String newPath)
	{
		this.filePath = newPath;
	}
	
	public void setAlternateValueHeaderName(String valueHeaderName)
	{
		this.valueHeaderName = valueHeaderName;
	}
	
	/**
	 * This also flushes the buffer used to write the file so if this isn't called at the end
	 * there will likely be missing data. Be sure to call this when done writing.
	 */
	public void close()
	{
		if(this.writer != null)
		{
			this.writer.flush();
			this.writer.close();
		}
	}
	
	public String getPath()
	{
		return this.filePath;
	}
	
	public void writeStringTableHeader(DimTable dimTable)
	{
		writeHeader(dimTable, false);
	}
	
	public void writeNumericTableHeader(DimTable dimTable)
	{
		writeHeader(dimTable, true);
	}
	
	protected void writeHeader(DimTable dimTable, boolean isNumeric)
	{
		try
		{
			this.dimTable = dimTable;
			//this.filePath = JEXWriter.getUniqueTempPath(fileExtension);
			
			ArrayList<Attribute> atts = this.dimTable.getArffAttributeList();
			if(isNumeric)
			{
				atts.add(new Attribute(this.valueHeaderName));
			}
			else
			{
				atts.add(new Attribute(this.valueHeaderName,(List<String>)null));
			}
			this.instances = new Instances(tableName, atts, this.dimTable.mapCount());
			
			// Initialize PrintWriter and write header
			this.writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filePath)))));
			writer.print(instances.toString());
			writer.flush();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			if(writer != null)
			{
				writer.flush();
				writer.close();
			}
		}
	}
	
	public <E> void writeData(DimensionMap map, E value)
	{
		this.writer.println(this.makeRow(map, value));
		rowCounter++;
		if(rowCounter > 100)
		{
			this.writer.flush();
			rowCounter = 0;
		}
	}
	
	protected <E> void writeData(TreeMap<DimensionMap,E> data)
	{
		int count = 0, percentage = 0, total = data.size(), newPercentage = 0;
		JEXStatics.statusBar.setStatusText("Writing ARFF: 0%");
		for(Entry<DimensionMap,E> e : data.entrySet())
		{
			count = count + 1;
			newPercentage = (int) (100*((double) count)/((double) total));
			E value = e.getValue();
			this.writeData(e.getKey(), value);
			
			if(newPercentage != percentage)
			{
				percentage = newPercentage;
				JEXStatics.statusBar.setStatusText("Writing ARFF: " + percentage + "%");
			}
		}
		JEXStatics.statusBar.setStatusText("Writing ARFF Done.");
	}
	
	private <E> String makeRow(DimensionMap map, E value)
	{
		StringBuffer text = new StringBuffer();
		int i = 0;
		for(Dim dim : this.dimTable)
		{
			if (i > 0) text.append(",");
			text.append(Utils.quote(map.get(dim.name())));
			i++;
		}
		if (i > 0) text.append(",");
		if(value == null)
		{
			text.append("?");
		}
		else if(value instanceof String)
		{
			text.append(Utils.quote(value.toString()));
		}
		else
		{
			Double temp = ((Number)value).doubleValue();
			if(temp.equals(Double.NaN))
			{
				text.append("?");
			}
			else
			{
				text.append(doubleToString(((Number)value).doubleValue(), precisionAfterDecPoint));
			}
		}
		return text.toString();
	}

	public static String doubleToString(double value, int precisionAfterDecPoint)
	{
		return Utils.doubleToString(value, precisionAfterDecPoint);
	}
}
