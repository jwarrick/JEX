package Database.Definition;

import java.util.TreeMap;
import java.util.TreeSet;

import utilities.CSVList;
import utilities.StringUtility;
import Database.DBObjects.JEXData;
import Database.DBObjects.JEXEntry;
import Database.SingleUserDatabase.tnvi;

public class Experiment extends TreeMap<String,Tray> implements Comparable<Experiment>, HierarchyLevel{
	private static final long serialVersionUID = 1L;
	
	public static String NUMBER = "Size";
	
	public String expName   ;
	public String expInfo   ;
	public String expDate   ;
	public String expMDate  ;
	public String expAuthor ;
	public String expNumber ;
	public TreeSet<JEXEntry> entries ;
	public tnvi TNVI ;
	
	public Experiment(
			String expName, 
			String expInfo, 
			String expDate, 
			String expMDate, 
			String expAuthor, 
			String expNumber)
	{
		super();
		this.expName   = expName;
		this.expInfo   = expInfo;
		this.expDate   = expDate;
		this.expMDate  = expMDate;
		this.expAuthor = expAuthor;
		this.expNumber = expNumber;
		this.TNVI      = new tnvi();
	}
	
//	public Experiment(	
//			String expName, 
//			String expInfo, 
//			String expDate, 
//			String expMDate, 
//			String expAuthor, 
//			String expNumber,
//			String arrayCSV)
//	{
//		this(expName,expInfo,expDate,expMDate,expAuthor,expNumber);
//		
//		CSVList arrayCSVList = new CSVList(arrayCSV);
//		for (String arrayName: arrayCSVList)
//		{
//			this.put(arrayName,new Tray(arrayName));
//		}
//	}
	
	/**
	 * Add an entry to the list of entries in the experiment
	 * @param entry
	 */
	public void addEntry(JEXEntry entry)
	{
		// Add the entry to the list
		if (entries == null) entries = new TreeSet<JEXEntry>();
		entries.add(entry);
		
		// Add to the dictionaries
		this.TNVI.addEntryForAllDataInEntry(entry);
		
		// Create a new array if one exist yet
		String arrayName = entry.getEntryTrayName();
		Tray a = this.get(arrayName);
		
		// if the tray doesn't exist create it
		if (a == null)
		{
			a = new Tray(arrayName);
			this.put(arrayName, a);
		}
		
		// Add the entry in the tray
		a.addEntry(entry);
	}
	
	/**
	 * Update the experiment by adding data 
	 * @param entry
	 * @param data
	 */
	public void addEntryForData(JEXEntry entry, JEXData data)
	{
		// if data is null pass
		if (data == null) return;
		
		// add data to tnvi
		this.TNVI.addEntryForData(entry, data);
		
		// Loop through the arrays and add the data to them
		for (Tray tray: this.values())
		{
			// If the array doesn't contain the entry skip it
			if (!tray.containsEntry(entry)) continue;
			
			// Else add the data to it
			tray.addEntryForData(entry, data);
		}
	}

	/**
	 * Update the experiment by adding data 
	 * @param entry
	 * @param data
	 */
	public void removeEntryForData(JEXEntry entry, JEXData data)
	{
		// if data is null pass
		if (data == null) return;
		
		// Remove data
		this.TNVI.removeEntryForData(entry, data);
		
		// Loop through the arrays and add the data to them
		for (Tray tray: this.values())
		{
			// If the array doesn't contain the entry skip it
			if (!tray.containsEntry(entry)) continue;
			
			// Else add the data to it
			tray.removeEntryForData(entry, data);
		}
	}
	
	public tnvi tnvi()
	{
		return this.TNVI;
	}
	
	/**
	 * Return the list of entries in the experiment
	 * @return
	 */
	public TreeSet<JEXEntry> getEntries()
	{
		return entries;
	}
	
	/**
	 * Returns a representative entry, or NULL if no entry is there
	 */
	public JEXEntry getRepresentativeEntry()
	{
		// Get a representative entry from this cell (usually the first)
		JEXEntry      entry   = null;
		if (entries != null && entries.size() > 0) entry = entries.iterator().next();
		return entry;
	}
	
	/**
	 * Is the entry ENTRY part of the group?
	 */
	public boolean containsEntry(JEXEntry entry)
	{
		boolean result = this.getEntries().contains(entry);
		return result;
	}
		
	/**
	 * Return the list of arrays as a CSV string
	 * @return
	 */
	public String getArrayNameString(){
		CSVList csvString = new CSVList();
		for (Tray array: this.values())
		{
			csvString.add(array.arrayName);
		}
		return csvString.toString();
	}
	
	public String getName()
	{
		return this.expName;
	}
	
	public String getType()
	{
		return JEXEntry.EXPERIMENT;
	}

	/**
	 * Return the list of all trays in this experiment
	 */
	public TreeMap<String,HierarchyLevel> getSublevelList()
	{
		TreeMap<String,HierarchyLevel> result = new TreeMap<String,HierarchyLevel>(new StringUtility());
		
		for (String arrayName: this.keySet())
		{
			result.put(arrayName,this.get(arrayName));
		}
		
		return result;
	}
	
	/**
	 * Return the list of all trays in an array form, i.e. a matrix column
	 */
	public TreeMap<String,TreeMap<String,HierarchyLevel>> getSublevelArray()
	{
		TreeMap<String,TreeMap<String,HierarchyLevel>> result = new TreeMap<String,TreeMap<String,HierarchyLevel>>(new StringUtility());
		
		TreeMap<String,HierarchyLevel> column = new TreeMap<String,HierarchyLevel>(new StringUtility());
		for (String arrayName: this.keySet())
		{
			column.put(arrayName, this.get(arrayName));
		}

		result.put("",column);
		return result;
	}
	
	@Override
	public int hashCode(){
		return super.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		if (!(o instanceof Experiment)) return false;
		Experiment e = (Experiment)o;
		return expName.equals(e.expName);
	}
	
	public int compareTo(Experiment o) {
		return expName.compareTo(o.expName);
	}
	
}

