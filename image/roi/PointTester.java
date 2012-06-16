package image.roi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.io.FileUtils;

import jex.statics.JEXStatics;
import jex.statics.LogManager;
import utilities.FileUtility;

public class PointTester extends URLClassLoader {
	
//	public PointTester(String path) throws MalformedURLException
//	{
//		super();
//	}
	
	public PointTester(URL[] urls)
	{
		super(urls);
		// TODO Auto-generated constructor stub
	}



	public static void main(String[] args) throws IOException
	{		
		
		JEXStatics.logManager = new LogManager();
		
		File f = new File("/Users/warrick/tmp2");
		File f2 = new File(FileUtility.removeWhiteSpaceOnEnds("  /Users/warrick/tmp2      "));
		System.out.println(f.equals(f2));
		FileUtils.moveDirectory(f, f2);
		
		
//		Dim a = new Dim("A,1,2,3,4,5,6,7");
//		Dim b = new Dim("B,1,2,3");
//		Dim c = new Dim("C,1,2,3,4,5");
//		
//		DimTable table = new DimTable();
//		table.add(a);
//		table.add(b);
//		table.add(c);
//		
//		DimTableMapIterator mapItr = table.getIterator().iterator();
//		while(mapItr.hasNext())
//		{
//			DimensionMap map = mapItr.next();
//			System.out.println(map.toString() + "  :  " + mapItr.currentRow());
//		}
//		
//		TreeMap<DimensionMap,Double> data = new TreeMap<DimensionMap,Double>();
//		for(DimensionMap map : table.getIterator())
//		{
//			double total = 0;
//			for(String key : map.keySet())
//			{
//				total = total + Double.parseDouble(map.get(key));
//			}
//			//System.out.println(total);
//			data.put(map, total);
//		}
//		
//		String path = JEXTableWriter.writeTable("My Data", table, data, JEXTableWriter.TXT_FILE);
//		Table<Double> table1 = JEXTableReader.getNumericTable(path);
//		Table<Double> table2 = JEXTableReader.getNumericTable(path, new DimensionMap("A=1"));
//		Table<Double> table3 = JEXTableReader.getNumericTable(path, new DimensionMap("A=2,B=3"));
//		System.out.println(table1.data);
//		System.out.println(table1.dimTable);
//		System.out.println(table2.data);
//		System.out.println(table2.dimTable);
//		System.out.println(table3.data);
//		System.out.println(table3.dimTable);
		
//		try
//    	{
////			String wd = "\"/Users/warrick/Desktop/R Scripts/CellTracking\"";
////			File f = new File(wd);
////			String inPath = "\"CellTracking.arff\"";
//////			String inPath = "CellTracking.arff";
////			String outPath = "\"CellTracking_JAVA2.png\"";
//			
//			Rsession s = Rsession.newInstanceTry(System.out,null);
//			System.out.println("%%%%%%%% Set Working Directory %%%%%%%%");
//			s.voidEval("setwd(\"/Users/warrick/Desktop/CellTracking\")");
//			System.out.println("%%%%%%%% Show Working Directory %%%%%%%%");
//			REXP x = s.eval("getwd()");
//			System.out.println(x.asString());
//			System.out.println("%%%%%%%% Create Vectors %%%%%%%%");
//			s.voidEval("x <- 1:10");
//			s.voidEval("y <- 1:10");
//			System.out.println("%%%%%%%% Attempt to Plot %%%%%%%%");
//			s.loadPackage("RWeka");
////			s.toPNG(new File("/Users/warrick/Desktop/CellTracking/AAA.png"), 1000, 1000, "plot(x,y)");
////			System.out.println("%%%%%%%% Check Version %%%%%%%%");
////			RList resultsList = s.eval("version").asList();
////			for(Object result : resultsList)
////			{
////				System.out.println(((REXP)result).asString());
////			}
////			System.out.println("%%%%%%%% Show Working Directory %%%%%%%%");
////			REXP x = s.eval("getwd()");
////			System.out.println(x.asString());
////			System.out.println("%%%%%%%% Set Working Directory %%%%%%%%");
////			s.voidEval("setwd(\"/Users/warrick/Desktop/CellTracking\")");
////			System.out.println("%%%%%%%% Show Working Directory %%%%%%%%");
////			x = s.eval("getwd()");
////			System.out.println(x.asString());
////			System.out.println("%%%%%%%% Show Available Packages %%%%%%%%");
////			String[] results = s.eval(".packages(all=TRUE)").asStrings();
////			for(String result : results)
////			{
////				System.out.println(result);
////			}
////			System.out.println("%%%%%%%% Show Loaded Packages %%%%%%%%%");
////			results = s.eval(".packages()").asStrings();
////			for(String result : results)
////			{
////				System.out.println(result);
////			}
////			System.out.println("%%%%%%%% Load Already Loaded Package %%%%%%%%%");
////			s.loadPackage("datasets");
////			System.out.println("%%%%%%%% Load Available Package %%%%%%%%%");
////			s.loadPackage("Rserve");
////			System.out.println("%%%%%%%% Load Available Package %%%%%%%%%");
////			s.loadPackage("RWeka");
//			
////			s.voidEval("testScript.R(\"/Users/warrick/Desktop/CellTracking/JEXData0000270.arff\",\"/Users/warrick/Desktop/CellTracking/BBB.png\")",true);;
//			s.end();
//
//			
////			Rengine s1=new Rengine(new String[]{"--no-save", "--vanilla"}, false, new TextConsole());
////	        System.out.println("Rengine created, waiting for R");
////			// the engine creates R is a new thread, so we should wait until it's ready
////	        if (!s1.waitForR()) {
////	            System.out.println("Cannot load R");
////	            System.out.println("ABORTING");
////	            return;
////	        }
////	        
////	        REXP x = s1.eval("R.version.string");
////	        System.out.println(x.asString());
//////	        x = s1.eval(new String[]{"x <- 1:100","y <- (1:100)/100"});
//////	        System.out.println(x.asString());
//////    		x = s1.eval("y <- sin(x)");
//////    		System.out.println(x.asString());
////    		x = s1.eval("setwd(\"/Users/warrick/Desktop/R Scripts/CellTracking\")");
////    		System.out.println(x.asString());
////    		x = s1.eval("getwd()");
////    		System.out.println(x.asString());
////    		x = s1.eval("source(\"makeFACSPlot.R\")");
////    		System.out.println(x.asString());
////    		x = s1.eval("makeFACSPlot(" + inPath + "," + outPath + ")");
////    		System.out.println(x.asString());
////    		x = s1.eval("getwd()");
////    		System.out.println(x.asString());
//////    		x = s1.eval("dev.off()");
//////    		System.out.println(x.asString());
////    		s1.end();
////	        
//////	        s1.eval(arg0, arg1)
//			
//			
//			
//			
////			ArrayList<String> Rargs = new ArrayList<String>();
////			Rargs.add("R");
////			Rargs.add("CMD");
////			Rargs.add("BATCH");
////			Rargs.add("\"/Users/warrick/Desktop/R Scripts/CellTracking/runMakeFACSPlot.R\"");
////			FileUtility.runSysCommands(Rargs);
////			Runtime runtime = Runtime.getRuntime();
////			Process step = runtime.exec(new String[]{"/bin/bash", "-c", "cd \"/Users/warrick/Desktop/R Scripts/CellTracking\" ; R CMD BATCH runMakeFACSPlot.R log.txt ; open log.txt"});
////			int results = step.waitFor();
////			System.out.println(results);
//			
////			int results = FileUtility.runMultipleStringCommands(new String[]{"cd " + wd, "R CMD BATCH " + inPath + " log.txt", "open log.txt"});
////			System.out.println(results);
//			
////			PrintWriter stdIn = new PrintWriter(step.getOutputStream());
////			InputStream stdOut = step.getInputStream();
////			stdIn.flush();
////			stdOut.
////			stdIn.println("cd " + wd);
////			results = step.waitFor();
////			System.out.println(results);
////
////			stdIn.println("R CMD BATCH runMakeFACSPlot.R log.txt");
////			stdIn.flush();
////			results = step.waitFor();
////			System.out.println(results);
////			
////			stdIn.println("open log.txt");
////			stdIn.flush();
////			results = step.waitFor();
////			System.out.println(results);
////			step.destroy();
////    		Rsession s = new Rsession(System.out,null,true);
////    		RConnection s1 = s.connection;
////    		s1.eval("setwd(\"/Users/warrick/Desktop/R Scripts/CellTracking\")");
////    		s1.eval("library(\"datasets\")");
////    		s1.eval("library(\"graphics\")");
////    		s1.eval("library(\"grDevices\")");
////    		s1.eval("library(\"methods\")");
////    		s1.eval("library(\"stats\")");
////    		s1.eval("library(\"utils\")");
////    		s1.eval("x <- 1:100");
////    		s1.eval("y <- sin(x)");
////    		s1.eval("png(\"CellTrackingSine.png\", width = 700, height = 500);");
////    		s1.eval("plot(x,y)");
////    		s1.eval("dev.off()");
////    		s1.end();
////    		//s1.toPNG(new File("/Users/warrick/Desktop/R Scripts/CellTracking/CellTrackingSin.png"),700,500,"plot(x,y)");
//////			s1.source(new File("/Users/warrick/Desktop/R Scripts/CellTracking/DataVisTest.R"));
////            s.end();
//    	}
//    	catch(Exception e)
//    	{
//    		e.printStackTrace();
//    	}
		
		
		
//		String wd = "/Users/warrick/Desktop/Octave Scripts/CellTracking"; ACTUALLY HARDCODED IN OCTAVECONTROLLER
//		String inPath = "/Users/warrick/Desktop/Octave Scripts/CellTracking/All.txt";
//		String outPath = "/Users/warrick/Desktop/Octave Scripts/CellTracking/Filtered.arff"; //ACTUALLY HARDCODED IN JEXTABLEWRITER
//
//		try {
//			ArffViewer 
//			ArffViewer.main(new String[]{outPath});
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		//		JEXTableReader reader = new JEXTableReader(inPath);
//		DimTable masterDimTable = reader.getDimTable();
//		DimTable blueDimTable = masterDimTable.getSubTable(new DimensionMap("Color=0,Measurement=1"));
//		DimTable redDimTable = masterDimTable.getSubTable(new DimensionMap("Color=2,Measurement=1"));
//		blueDimTable.removeDimWithName("Track");
//		redDimTable.removeDimWithName("Track");
//		Iterator<DimensionMap> blueItr = blueDimTable.getIterator().iterator();
//		Iterator<DimensionMap> redItr = redDimTable.getIterator().iterator();
//		
//		OctaveController octave = new OctaveController();
//		octave.makeEngine();
//		int count = 0;
//		while(blueItr.hasNext())// && count < 250)
//		{
//			DimensionMap blueMap = blueItr.next();
//			DimensionMap redMap = redItr.next();
//			
////			if(count < 200)
////			{
////				count++;
////				continue;
////			}
//			
//			Table blueTable = reader.getTable(blueMap);
//			Table redTable = reader.getTable(redMap);
//			blueTable.dimTable = DimTable.union(blueTable.dimTable, redTable.dimTable);
//			blueTable.table.putAll(redTable.table);
//			reader.close();
//			JEXTableWriter writer = new JEXTableWriter("X blue vs Y red", blueTable.dimTable);
//			writer.writeData(blueTable.table);
//			writer.close();
//			octave.runCommand("testDataViewing");
//			
//			count++;
//		}
//		octave.close();
	}
	
	static byte[] readStream(InputStream input) throws IOException {
		byte[] buffer = new byte[1024];
		int offset = 0, len = 0;
		for (;;) {
			if (offset == buffer.length)
				buffer = realloc(buffer,
						2 * buffer.length);
			len = input.read(buffer, offset,
					buffer.length - offset);
			if (len < 0)
				return realloc(buffer, offset);
			offset += len;
		}
	}

	static byte[] realloc(byte[] buffer, int newLength) {
		if (newLength == buffer.length)
			return buffer;
		byte[] newBuffer = new byte[newLength];
		System.arraycopy(buffer, 0, newBuffer, 0,
				Math.min(newLength, buffer.length));
		return newBuffer;
	}


	@Override
	@SuppressWarnings("unchecked")
	public Class<Object> loadClass(String name)
	{
		Class<Object> result = null;
		try
		{
			InputStream input = this.getResourceAsStream(FileUtility.getFileNameWithExtension(name));

			if (input != null)
			{
				byte[] buffer = readStream(input);
				input.close();
				result = (Class<Object>) this.defineClass("TestLoadable", buffer, 0, buffer.length);
				return result;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return result;
	}

	
}
