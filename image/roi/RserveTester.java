package image.roi;

import java.io.IOException;

import jex.statics.JEXStatics;
import jex.statics.LogManager;
import jex.statics.R;

import org.math.R.StartRserve;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;


public class RserveTester {
	
	public static void main(String[] args) throws IOException, RserveException
	{		
		JEXStatics.logManager = new LogManager();
		
		
//		Dim a = new Dim("a",new String[]{"1","2","3","4","5"});
//		Dim b = new Dim("b",new String[]{"1","2","3","4","5"});
//		Dim c = new Dim("c",new String[]{"1","2","3","4","5"});
//		
//		DimensionMap map1 = new DimensionMap("a=1,b=2,c=3");
//		DimensionMap map2 = new DimensionMap("a=1,b=2");
//		
//		JEXStatics.logManager.log("1.2="+map1.compareTo(map2), 0, "RserveTester");
//		JEXStatics.logManager.log("2.1="+map2.compareTo(map1), 0, "RserveTester");
//		
//		DimTable dTable = new DimTable();
//		dTable.add(a);
//		dTable.add(b);
//		dTable.add(c);
//		
//		for(DimensionMap map : dTable.getIterator(new DimensionMap("b=3,c=1"), 10000))
//		{
//			JEXStatics.logManager.log(map.toString(), 0, "RserveTester");
//		}
		
		
		@SuppressWarnings("unused")
		double[] y = new double[]{Double.NaN,Double.NaN,3.0,4.00,5.0};
		@SuppressWarnings("unused")
		double[] t = new double[]{1.0,2.0,3.0,4.0,5.0};
//		REXPDouble y_R = new REXPDouble(y);
//		REXPDouble t_R = new REXPDouble(t);
		StartRserve.launchRserve("/Library/Frameworks/R.framework/Resources/bin/R");
		R.rConnection = new RConnection();
//		try {
//			R.rConnection.assign("y", y);
//			R.rConnection.assign("t", t);
//			File plot = new File("/Users/warrick/Desktop/CellTracking/123.svg");
//			R._startPlot(plot, 4, 3, 300, 10, "Arial", "lzw");
			R.setwd("/Volumes/shared/JEX Databases/Adam/CellTracking");
			R.source("makeData.R");
			@SuppressWarnings("unused")
			REXP result = R.eval("makeData('hello')");
			JEXStatics.logManager.log("Yo", 0, "RServeTester");
//			R.endPlot();
//			Desktop.getDesktop().open(plot);
//		} catch (REngineException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}	
}
