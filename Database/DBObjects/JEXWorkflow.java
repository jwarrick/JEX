package Database.DBObjects;

import java.io.File;
import java.util.TreeMap;
import java.util.Vector;

import jex.statics.JEXStatics;
import utilities.DateUtility;
import utilities.Pair;
import Database.DBObjects.dimension.DimTable;
import Database.DBObjects.dimension.DimensionMap;
import Database.SingleUserDatabase.JEXDataIO;
import cruncher.JEXFunction;

public class JEXWorkflow extends Vector<JEXFunction> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static String WORKFLOW_INDEX = "Workflow Index";
	
	
	public JEXData toJEXData(String workflowName)
	{
		JEXData ret = new JEXData(JEXData.WORKFLOW, workflowName);
		ret.setAuthor(JEXStatics.jexManager.getUserName());
		ret.setDataObjectDate(DateUtility.getDate());
		ret.setDataObjectModifDate(DateUtility.getDate());
		int count = 0;
		for(JEXFunction func : this)
		{
			Vector<JEXDataSingle> singles = func.getSingles();
			for(JEXDataSingle single : singles)
			{
				DimensionMap map = single.getDimensionMap();
				map.put(JEXWorkflow.WORKFLOW_INDEX, ""+count);
				ret.addData(map, single);
			}
			count = count + 1;
		}
		ret.setDimTable(new DimTable(ret.getDataMap()));
		return ret;
	}
	
	/**
	 * Save the list of functions into an xml file
	 * @param Path
	 */
	public void saveWorkflow(File file)
	{		
		JEXDataIO.saveDetachedFileToLocation(this.toJEXData("JEX Workflow Name"), file.getAbsolutePath());
	}
	
	/**
	 * Load function located at path FILE
	 * @param file
	 */
	public static JEXWorkflow loadWorkflow(File file)
	{
		JEXData data = new JEXData(JEXData.WORKFLOW, "TempName");
		JEXDataIO.loadDetachedARFFJEXData(data, file.getAbsolutePath());
		TreeMap<Integer,Pair<String,Vector<JEXDataSingle>>> functions = new TreeMap<Integer,Pair<String,Vector<JEXDataSingle>>>(); 
		
		for(DimensionMap map : data.getDataMap().keySet())
		{
			String functionIndexString = map.get(JEXWorkflow.WORKFLOW_INDEX);
			Integer functionIndex = Integer.parseInt(functionIndexString);
			Pair<String,Vector<JEXDataSingle>> temp = functions.get(functionIndex);
			if(temp == null) // create the list for singles because it doesn't already exist
			{
				temp = new Pair<String,Vector<JEXDataSingle>>(map.get(JEXFunction.FUNCTION_NAME), new Vector<JEXDataSingle>());
			}
			temp.p2.add(data.getDataMap().get(map));
			functions.put(functionIndex, temp);
		}
		
		JEXWorkflow ret = new JEXWorkflow();
		for(Integer index : functions.keySet())
		{
			JEXFunction func = new JEXFunction(functions.get(index).p1, functions.get(index).p2);
			ret.add(func);
		}
		return ret;
	}
}
