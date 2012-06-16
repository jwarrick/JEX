package function.experimentalDataProcessing;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import jex.statics.JEXStatics;
import Database.DBObjects.JEXData;
import Database.DBObjects.JEXEntry;
import Database.DataReader.ImageReader;
import Database.DataWriter.MovieWriter;
import Database.Definition.Parameter;
import Database.Definition.ParameterSet;
import Database.Definition.TypeName;
import Database.SingleUserDatabase.JEXWriter;
import ch.randelshofer.media.quicktime.QuickTimeOutputStream;
import function.ExperimentalDataCrunch;


/**
 * This is a JEXperiment function template
 * To use it follow the following instructions
 * 
 * 1. Fill in all the required methods according to their specific instructions
 * 2. Place the file in the Functions/SingleDataPointFunctions folder
 * 3. Compile and run JEX!
 * 
 * JEX enables the use of several data object types
 * The specific API for these can be found in the main JEXperiment folder.
 * These API provide methods to retrieve data from these objects,
 * create new objects and handle the data they contain.
 * 
 * @author erwinberthier
 *
 */
public class JEX_MakeMovie extends ExperimentalDataCrunch {

	// ----------------------------------------------------
	// --------- INFORMATION ABOUT THE FUNCTION -----------
	// ----------------------------------------------------
	
	/**
	 * Returns the name of the function
	 * 
	 * @return Name string
	 */
	@Override
	public String getName() {
		String result = "Make movie";
		return result;
	}
	
	/**
	 * This method returns a string explaining what this method does
	 * This is purely informational and will display in JEX
	 * 
	 * @return Information string
	 */
	@Override
	public String getInfo() {
		String result = "Make a movie from a timelapse image";
		return result;
	}

	/**
	 * This method defines in which group of function this function 
	 * will be shown in... 
	 * Toolboxes (choose one, caps matter):
	 * Visualization, Image processing, Custom Cell Analysis, Cell tracking, Image tools
	 * Stack processing, Data Importing, Custom image analysis, Matlab/Octave
	 * 
	 */
	@Override
	public String getToolbox() {
		String toolbox = "Visualization";
		return toolbox;
	}

	/**
	 * This method defines if the function appears in the list in JEX
	 * It should be set to true expect if you have good reason for it
	 * 
	 * @return true if function shows in JEX
	 */
	@Override
	public boolean showInList() {
		return true;
	}
	
	/**
	 * Returns true if the user wants to allow multithreding
	 * @return
	 */
	@Override
	public boolean allowMultithreading()
	{
		return false;
	}
	
	
	
	// ----------------------------------------------------
	// --------- INPUT OUTPUT DEFINITIONS -----------------
	// ----------------------------------------------------
	
	/**
	 * Return the array of input names
	 * 
	 * @return array of input names
	 */
	@Override
	public TypeName[] getInputNames(){
		TypeName[] inputNames = new TypeName[1];
		inputNames[0] = new TypeName(IMAGE,"Timelapse stack");
		return inputNames;
	}
	
	/**
	 * Return the number of outputs returned by this function
	 * 
	 * @return number of outputs
	 */
	@Override
	public TypeName[] getOutputs() {
		defaultOutputNames = new TypeName[1];
		defaultOutputNames[0] = new TypeName(MOVIE,"Movie");

		if (outputNames == null) return defaultOutputNames;
		return outputNames;
	}
	
	/**
	 * Returns a list of parameters necessary for this function 
	 * to run...
	 * Every parameter is defined as a line in a form that provides 
	 * the ability to set how it will be displayed to the user and 
	 * what options are available to choose from
	 * The simplest FormLine can be written as:
	 * FormLine p = new FormLine(parameterName);
	 * This will provide a text field for the user to input the value
	 * of the parameter named parameterName
	 * More complex displaying options can be set by consulting the 
	 * FormLine API
	 * 
	 * @return list of FormLine to create a parameter panel
	 */
	@Override
	public ParameterSet requiredParameters() {
		Parameter p1 = new Parameter("Frame Rate","Frames per second","7");
		Parameter p2 = new Parameter("Compression Rate","Compression quality","0.9");
		Parameter p3 = new Parameter("Image Binning","Bin the images for smaller movie","1");
		
		// Make an array of the parameters and return it
		ParameterSet parameterArray = new ParameterSet();
		parameterArray.addParameter(p1);
		parameterArray.addParameter(p2);
		parameterArray.addParameter(p3);
		return parameterArray;
	}
	
	
	// ----------------------------------------------------
	// --------- ERROR CHECKING METHODS -------------------
	// ----------------------------------------------------
	
	/**
	 * Returns the status of the input validity checking
	 * It is HIGHLY recommended to implement input checking
	 * however this can be over-rided by returning false
	 * If over-ridden ANY batch function using this function 
	 * will not be able perform error checking... 
	 * 
	 * @return true if input checking is on
	 */
	@Override
	public boolean isInputValidityCheckingEnabled(){
		return true;
	}
	
		
	
	
	// ----------------------------------------------------
	// --------- THE ACTUAL MEAT OF THIS FUNCTION ---------
	// ----------------------------------------------------

	/**
	 * Perform the algorithm here
	 * 
	 */
	@Override
	public boolean run(JEXEntry entry, HashMap<String,JEXData> inputs){
		// Collect the inputs
		JEXStatics.logManager.log("Collecting inputs", 1, this);
		JEXData data = inputs.get("Timelapse stack");
		if (!data.getTypeName().getType().equals(JEXData.IMAGE)) return false;
		List<String> imset = ImageReader.readObjectToImagePathList(data);
		
		// Run the function
		JEXStatics.logManager.log("Running the function", 1, this);		
		String vhPath = JEXWriter.getDatabaseFolder() + File.separator + JEXWriter.getUniqueRelativeTempPath("avi");
		makeMovieAndSaveToPath(imset,vhPath);
		
		// Collect the outputs
		JEXStatics.logManager.log("Collecting outputs", 1, this);
		JEXData movie = MovieWriter.makeMovieObject(outputNames[0].getName(), vhPath);
		realOutputs.add(movie);
		
		// Return status
		return true;
	}
	
	public void makeMovieAndSaveToPath(List<String> imset, String path){
		// ------------------------------
		// save the movie of the tracking
		File outMovieFile = new File(path);
		QuickTimeOutputStream newStream = null;
		try{
			QuickTimeOutputStream.VideoFormat format = QuickTimeOutputStream.VideoFormat.values()[0];
			newStream = new QuickTimeOutputStream(outMovieFile, format);
		}
		catch (IOException e){
			System.out.println("   Tracker.TrackExtend ---> Not possible to create movie ... ");
		}
		
		int binning       = Integer.parseInt(parameters.getValueOfParameter("Image Binning"));
		float compression = Float.parseFloat(parameters.getValueOfParameter("Compression Rate"));
		
		// add each image one by one
		for (int k = 0, len = imset.size(); (k < len); k++) {
			ImagePlus imk = new ImagePlus(imset.get(k));
			ImageProcessor imp = imk.getProcessor();
			imp = imp.resize((imk.getWidth()/binning));
			BufferedImage bimage = imp.getBufferedImage();

			if (k == 0){
				newStream.setVideoCompressionQuality(compression);
				newStream.setTimeScale(8);
			}

			try{
				newStream.writeFrame(bimage,1);
				System.out.println("   Tracker.TrackExtend ---> Writing frame "+k);
			}
			catch (IOException e){
				System.out.println("   Tracker.TrackExtend ---> Not possible to write frame "+k);
			}

			// Status bar
			int percentage = (int) (100 * ((double) k/ (double)imset.size()));
			JEXStatics.statusBar.setProgressPercentage(percentage);
		}
		try {
			newStream.finish();
			newStream.close();
		}
		catch (IOException e){
			System.out.println("   Tracker.TrackExtend ---> Not possible to finalize movie ");
		}
		System.out.println("   Tracker.TrackExtend ---> Tracks movie saved in "+outMovieFile.getPath());
	}
	
}

