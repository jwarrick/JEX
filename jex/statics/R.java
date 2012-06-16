package jex.statics;

import java.io.File;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import org.math.R.StartRserve;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import utilities.CSVList;
import utilities.FileUtility;
import Database.DBObjects.dimension.DimensionMap;
import Database.DBObjects.dimension.Table;
import Database.SingleUserDatabase.JEXWriter;

public class R {
	
	public static final String FONT_AVANTGARDE = "AvantGarde", FONT_BOOKMAN = "Bookman", FONT_COURIER = "Courier", FONT_HELVETICA = "Helvetica", FONT_HELVETICA_NARROW = "Helvetica-Narrow", FONT_NEWCENTURYSCHOOLBOOK = "NewCenturySchoolbook", FONT_PALATINO = "Palatino", FONT_TIMES = "Times";
	private static final String[] fonts = new String[]{FONT_AVANTGARDE, FONT_BOOKMAN, FONT_COURIER, FONT_HELVETICA, FONT_HELVETICA_NARROW, FONT_NEWCENTURYSCHOOLBOOK, FONT_PALATINO, FONT_TIMES};
	
	// R Statistical Analysis Software Package Server Connection
	public static RConnection rConnection = null;
	public static int numRetries = 0;
	public static int numRetriesLimit = 3;
	
	private static boolean startRserve()
	{
		try
		{
			if(!R.isConnected())
			{
				// Try again just in case it took too long to start last time.
				try
				{
					rConnection = new RConnection();
					if(R.isConnected())
					{
						return true;
					}
				}
				catch (Exception e) // else failed to start connection, continue with starting server
				{
					JEXStatics.logManager.log("Couldn't find existing connection", 0, "R");
				}
				
				// Else start it in a platform specific way
				if(OsVersion.IS_WINDOWS)
				{
					StartRserve.launchRserve(PrefsUtility.getRBinary());
					//StartRserve.launchRserve("C:\\Program Files\\R\\R-2.14.2\\bin\\x64\\R");
				}
				else
				{
					Runtime.getRuntime().exec(new String[]{"/bin/bash","-c","open " + PrefsUtility.getRServeBinary()});
					JEXStatics.statusBar.setStatusText("Starting R server");
					Thread.sleep(PrefsUtility.getRServeStartDelay());
					//StartRserve.launchRserve(PrefsUtility.getRBinary()); Eventually want to use this when it actually works
				}
				rConnection = new RConnection();
				return (R.isConnected());
			}
			else
			{
				return true;
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
		
		// Keep JRI version of things around just in case
//		Rengine rEngine = new Rengine(new String[]{"--no-save", "--vanilla"}, false, new TextConsole());
//        System.out.println("Rengine created, waiting for R");
//		// the engine creates R is a new thread, so we should wait until it's ready
//        if (!rEngine.waitForR()) {
//            logManager.log("Cannot load R",0,"JEXStatics");
//            logManager.log("ABORTING",0,"JEXStatics");
//            return null;
//        }
//        return rEngine;
	}
	
	public static boolean isConnected()
	{
		return (R.rConnection != null && R.rConnection.isConnected());
	}
	
	public static void close()
	{
		R.rConnection.close();
	}
	
	public static REXP eval(String command)
	{
		JEXStatics.logManager.log("Attemping command: " + command, 0, "R");
		if(!R.isConnected()) // If not connected start the server and connect
		{
			if(!R.startRserve()) // If starting the server doesn't work return null
			{
				JEXStatics.logManager.log("Couldn't evaluate R command because either couldn't start server or connect to server!", 0, "R");
				return null;
			}
		}
		
		REXP ret = null;
		try
		{
			ret = rConnection.eval(command);
		} 
		catch (RserveException e)
		{
			if(numRetries < numRetriesLimit)
			{
//				e.printStackTrace();
				JEXStatics.logManager.log("Handling R Exception. Starting server and revaluating.", 0, "R");
				if(rConnection != null)
				{
					R.rConnection.close();
					R.rConnection = null;
					numRetries++;
					ret = R.eval(command);
				}
				
				return ret;
			}
			else
			{
				e.printStackTrace();
				JEXStatics.logManager.log("Couldn't resolve issue with R evaluation of command '" + command + "'. Giving up now.", 0, "R");
				numRetries = 0;
				return ret;
			}		
		}
		return ret;
	}
	
	public static REXP setwd(String path)
	{
		return R.eval("setwd(" + R.quotedPath(path) + ")");
	}
	
	/**
	 * Use this to source a .R file
	 * @param path
	 * @return
	 */
	public static REXP source(String path)
	{
		return R.eval("source(" + R.quotedPath(path) + ")");
	}
	
	/**
	 * Use this to load a library
	 * @param library
	 * @return
	 */
	public static REXP load(String library)
	{
		return R.eval("library(" + R.sQuote(library) + ")");
	}
	
	/**
	 * Returns the full path where this is being plotted
	 * @param extension (file extension, e.g. png, tiff, bmp, pdf, or svg)
	 * @param width_inches
	 * @param height_inches
	 * @param res_ppi (not used for svg or pdf)
	 * @param fontsize_pts
	 * @param optionalFont
	 * @param optionalTifCompression (only used for tif and takes values of none, lzw, rle, jpeg, or zip)
	 * @return
	 */
	public static String startPlot(String extension, double width_inches, double height_inches, double res_ppi, double fontsize_pts, String optionalFont, String optionalTifCompression)
	{
		extension = extension.toLowerCase();
		String filePath = JEXWriter.getDatabaseFolder() + File.separator + JEXWriter.getDatabaseFolder() + File.separator + JEXWriter.getUniqueRelativeTempPath(extension);
		if(R._startPlot(new File(filePath), width_inches, height_inches, res_ppi, fontsize_pts, optionalFont, optionalTifCompression))
		{
			return filePath;
		}
		return null;
	}
	
	/**
	 * Returns the full path where this is being plotted
	 * @param extension (file extension, e.g. png, tiff, bmp, pdf, or svg)
	 * @param width_inches
	 * @param height_inches
	 * @param res_ppi (not used for svg or pdf)
	 * @param fontsize_pts
	 * @param optionalFont
	 * @param optionalTifCompression (only used for tif and takes values of none, lzw, rle, jpeg, or zip)
	 * @return
	 */
	public static boolean _startPlot(File file, double width_inches, double height_inches, double res_ppi, double fontsize_pts, String optionalFont, String optionalTifCompression)
	{
		String extension = FileUtility.getFileNameExtension(file.getAbsolutePath());
		extension = extension.toLowerCase();
		String commandStart = extension;
		if(extension.equals("tif"))
		{
			commandStart = "tiff";
		}
		if(extension.equals("tiff"))
		{
			extension = "tif";
		}
//		if(R.load("Cairo") == null)
//		{
//			JEXStatics.logManager.log("Install Cairo Package for R to enable plotting (best package for plotting using server version of R).", 0, "R");
//			return false;
//		}
		CSVList args = new CSVList();
		args.add(R.quotedPath(file.getAbsolutePath()));
		args.add("height=" + height_inches);
		args.add("width=" + width_inches);
		if(!extension.equals("svg") && !extension.equals("pdf"))
		{
			args.add("res=" + res_ppi);
			args.add("units='in'");
			args.add("type='cairo'");
		}
		args.add("pointsize="+ fontsize_pts);
		if(optionalFont != null)
		{
			if(extension.equals("pdf"))
			{
				if(R.isPostScriptFont(optionalFont))
				{
					args.add("family=" + R.sQuote(optionalFont));
				}
				else
				{
					args.add("family=" + R.sQuote(FONT_HELVETICA));
				}
			}
			
		}
		if(extension.equals("tif") && optionalTifCompression != null)
		{
			args.add("compression=" + R.sQuote(optionalTifCompression));
		}
		String command = commandStart + "(" + args.toString() + ")";
		if(R.eval(command) == null)
		{
			JEXStatics.logManager.log("Couldn't start the " + extension + " plot in R", 0, "R");
			return false;
		}
		return true;
	}
	
	/**
	 * Returns the full path where this is being plotted
	 * @param extension (file extension, e.g. "png")
	 * @param width
	 * @param height
	 * @return
	 */
	public static REXP endPlot()
	{
		return R.eval("dev.off()");
	}
	
	/**
	 * Put parentheses around the String
	 * @param s
	 * @return
	 */
	public static String parentheses(String s)
	{
		return "(" + s + ")";
	}
	
	/**
	 * Put single quotes around a string
	 * @param s
	 * @return
	 */
	public static String sQuote(String s)
	{
		return "'" + s + "'";
	}
	
	/**
	 * Put single quotes around a string
	 * @param s
	 * @return
	 */
	public static String pathString(String path)
	{
		return Pattern.quote(path);
	}
	
	/**
	 * Put single quotes around a string
	 * @param s
	 * @return
	 */
	public static String quotedPath(String path)
	{
		return "'" + path.replaceAll(Pattern.quote(File.separator), "/") + "'";
	}
	
	/**
	 * Put double quotes around a string
	 * @param s
	 * @return
	 */
	public static String dQuote(String s)
	{
		return "\"" + s + "\"";
	}
	
	private static boolean isPostScriptFont(String font)
	{
		for(String fontName : fonts)
		{
			if(font.equals(fontName))
			{
				return true;
			}
		}
		return false;
	}
	
	public static <E> boolean makeVector(String vectorName, DimensionMap filter, Table<E> data)
	{
		if(data == null || data.data.size() == 0) return false;
		E temp = data.data.get(0);
		if(temp instanceof Double)
		{
			Vector<Double> vector = new Vector<Double>();
			for(DimensionMap map : data.dimTable.getIterator(filter))
			{
				Double temp2 = (Double) data.getData(map);
				if(temp == null)
				{
					temp2 = Double.NaN;
				}
				vector.add(temp2);
			}
			return makeVector(vectorName, vector);
		}
		else if(temp instanceof String)
		{
			Vector<String> vector = new Vector<String>();
			for(DimensionMap map : data.dimTable.getIterator(filter))
			{
				String temp2 = (String) data.getData(map);
				if(temp == null)
				{
					temp2 = "";
				}
				vector.add(temp2);
			}
			return makeVector(vectorName, vector);
		}
		else return false;
	}
	
	public static <E> boolean makeVector(String vectorName, List<E> data)
	{
		if(data == null || data.size() == 0) return false;
		E temp = data.get(0);
		if(temp instanceof Double)
		{
			double[] dNumbers = new double[data.size()];
			for(int i = 0; i < data.size(); i++)
			{
				dNumbers[i] = (Double)data.get(i);
			}
			return makeVector(vectorName, dNumbers);
		}
		else if(temp instanceof String)
		{
			String[] aStrings = data.toArray(new String[data.size()]);
			return makeVector(vectorName, aStrings);
		}
		else return false;
	}
	
	public static boolean makeVector(String vectorName, double[] numbers)
	{
		try 
		{
			rConnection.assign(vectorName, numbers);
			return true;
		} 
		catch (REngineException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean makeVector(String vectorName, String[] strings)
	{
		try 
		{
			rConnection.assign(vectorName, strings);
			return true;
		} 
		catch (REngineException e)
		{
			e.printStackTrace();
			return false;
		}
	}

}
