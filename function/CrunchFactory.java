package function;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.TreeMap;


public class CrunchFactory {
	public static String crunchPath = "./bin/function";
	public static String experimentalCrunchPath = crunchPath + "/experimentalDataProcessing";
	public static String matlabPath = crunchPath + "/matlab";
	static HashMap<String,ExperimentalDataCrunch> listOfCrunchers = getExperimentalDataCrunchers();
	
	/**
	 * Return the experimental data cruncher of name FUNCTIONNAME
	 * @param functionName
	 * @return An experimental data cruncher
	 */
	public static ExperimentalDataCrunch getExperimentalDataCrunch(String functionName)
	{
		try
		{
//			Set<String> functionNames = listOfCrunchers.keySet();
//			for(String name : functionNames)
//			{
//				JEXStatics.logManager.log(name + " = " + functionName + " = " + functionNames.contains(functionName), 0, CrunchFactory.class.getSimpleName());
//			}
			ExperimentalDataCrunch result = listOfCrunchers.get(functionName).getClass().newInstance();
			return result;
		} 
		catch (InstantiationException e)
		{
			e.printStackTrace();
		} catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Return a map of all loadable functions
	 * @return
	 */
	public static HashMap<String,ExperimentalDataCrunch> getExperimentalDataCrunchers(){
		HashMap<String,ExperimentalDataCrunch> result = new HashMap<String,ExperimentalDataCrunch>();
		
		File root = new File(experimentalCrunchPath); //Need to remember to include windows folks and work with classes instead of 
		File[] l = root.listFiles();
		
		for(int i = 0; i < l.length; i++)
		{
			String name = l[i].getName();
			if(name.length() > 4 && name.substring(0, 4).equals("JEX_"))
			{
				String ending     = name.substring(name.length()-6);
				if (ending.equals(".class")){
					String crunchname = (String) name.subSequence(0, name.length()-6);
					ExperimentalDataCrunch c = getInstanceOfExperimentalDataCrunch(crunchname);
					if (c == null) continue;
					if (c.showInList()) result.put(c.getName(), c);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Create a new instance of ExperimentalDataCrunch class 
	 * @param name
	 * @return instance of ExperimentalDataCrunch of name NAME
	 */
	public static ExperimentalDataCrunch getInstanceOfExperimentalDataCrunch(String name)
    {
//		Class toInstantiate;
		try {
			@SuppressWarnings("rawtypes")
			Class toInstantiate = Class.forName("function.experimentalDataProcessing." + name);
			ExperimentalDataCrunch ret = (ExperimentalDataCrunch) toInstantiate.newInstance();
			return ret;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
    }

	/**
	 * Return an ordered set of all the toolboxes
	 * @return set of toolboxes
	 */
	public static LinkedHashSet<String> getToolboxes(){
		LinkedHashSet<String> result = new LinkedHashSet<String>();
		listOfCrunchers = getExperimentalDataCrunchers();
		
		for (ExperimentalDataCrunch c: listOfCrunchers.values()){
			String tb = c.getToolbox();
			result.add(tb);
		}
		
		return result;
	}
	
	/**
	 * Get a subset of the functions in toolbox TOOBOX
	 * @param toolbox
	 * @return Sub set of function matching a toolbox name
	 */
	public static TreeMap<String,ExperimentalDataCrunch> getFunctionsFromToolbox(String toolbox){
//		HashMap<String,ExperimentalDataCrunch> result = new HashMap<String,ExperimentalDataCrunch>();
		TreeMap<String,ExperimentalDataCrunch> result = new TreeMap<String,ExperimentalDataCrunch>();
		
		for (ExperimentalDataCrunch c: listOfCrunchers.values()){
			if (c.getToolbox().equals(toolbox)) {
				result.put(c.getName(), c);
			}
		}
		
		return result;
	}
	
}
