package function.experimentalDataProcessing;

import image.roi.IdPoint;
import image.roi.PointList;
import image.roi.ROIPlus;
import image.roi.HashedPointList;

import java.awt.Point;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import jex.statics.JEXStatics;
import utilities.Pair;
import Database.DBObjects.JEXData;
import Database.DBObjects.JEXEntry;
import Database.DBObjects.dimension.Dim;
import Database.DBObjects.dimension.DimTable;
import Database.DBObjects.dimension.DimensionMap;
import Database.DataReader.RoiReader;
import Database.DataWriter.RoiWriter;
import Database.Definition.Parameter;
import Database.Definition.ParameterSet;
import Database.Definition.TypeName;
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
public class JEX_TrackPoints extends ExperimentalDataCrunch {
	
	public JEX_TrackPoints(){}

	// ----------------------------------------------------
	// --------- INFORMATION ABOUT THE FUNCTION -----------
	// ----------------------------------------------------
	
	/**
	 * Returns the name of the function
	 * 
	 * @return Name string
	 */
	public String getName() {
		String result = "Track Points";
		return result;
	}
	
	/**
	 * This method returns a string explaining what this method does
	 * This is purely informational and will display in JEX
	 * 
	 * @return Information string
	 */
	public String getInfo() {
		String result = "Use a nearest neighbor approach for creating tracks from point rois in each frame of a stack (i.e. along one dimension)";
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
	public String getToolbox() {
		String toolbox = "Image tools";
		return toolbox;
	}

	/**
	 * This method defines if the function appears in the list in JEX
	 * It should be set to true expect if you have good reason for it
	 * 
	 * @return true if function shows in JEX
	 */
	public boolean showInList() {
		return true;
	}
	
	/**
	 * Returns true if the user wants to allow multithreding
	 * @return
	 */
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
	public TypeName[] getInputNames(){
		TypeName[] inputNames = new TypeName[1];
		inputNames[0] = new TypeName(ROI,"Points");
		return inputNames;
	}
	
	/**
	 * Return the array of output names defined for this function
	 * @return
	 */
	public TypeName[] getOutputs(){
		defaultOutputNames = new TypeName[1];
		defaultOutputNames[0] = new TypeName(ROI,"Tracks Roi");
		
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
	public ParameterSet requiredParameters() {
//		Parameter p0 = new Parameter("Dummy Parameter","Lets user know that the function has been selected.",FormLine.DROPDOWN,new String[] {"true"},0);
		Parameter p3 = new Parameter("Radius of Search","The number of pixels left, rigth, up, and down to search for a nearest neighbor match.","25");
		Parameter p4 = new Parameter("Time Dim", "Name of the dimension along which points will be linked (typicall time)", "T");
		Parameter p5 = new Parameter("Track Dim Name", "Tracks will be grouped into new rois and indexed with a new dim of this name.","Track");
		
		// Make an array of the parameters and return it
		ParameterSet parameterArray = new ParameterSet();
		parameterArray.addParameter(p3);
		parameterArray.addParameter(p4);
		parameterArray.addParameter(p5);
		return parameterArray;
	}
	
	
	// ----------------------------------------------------
	// --------- ERROR CHECKING METHODS -------------------
	// ----------------------------------------------------
	
	/**
	 * Returns the status of the input validity checking
	 * It is HIGHLY recommended to implement input checking
	 * however this can be over-ridden by returning false
	 * If over-ridden ANY batch function using this function 
	 * will not be able perform error checking... 
	 * 
	 * @return true if input checking is on
	 */
	public boolean isInputValidityCheckingEnabled(){
		return false;
	}
	
	
	// ----------------------------------------------------
	// --------- THE ACTUAL MEAT OF THIS FUNCTION ---------
	// ----------------------------------------------------

	/**
	 * Perform the algorithm here
	 * 
	 */
	public boolean run(JEXEntry entry, HashMap<String,JEXData> inputs)
	{
		
		JEXStatics.statusBar.setProgressPercentage(0);
		
		// Collect the inputs
		JEXData roiData = inputs.get("Points");
		if (roiData == null || !roiData.getTypeName().getType().equals(JEXData.ROI)) return false;
		
		// Gather parameters
		int radius = Integer.parseInt(parameters.getValueOfParameter("Radius of Search"));
		String timeDimName = parameters.getValueOfParameter("Time Dim");
		String trackDimName = parameters.getValueOfParameter("Track Dim Name");
		
		// Run the function
		DimTable roiTable = roiData.getDimTable();
		Dim timeDim = roiTable.getDimWithName(timeDimName);
		DimTable timeTable = new DimTable();
		timeTable.add(timeDim);
		roiTable.remove(timeDim);
		
		List<DimensionMap> loopDims = roiTable.getDimensionMaps();
		List<DimensionMap> timeDims = timeTable.getDimensionMaps();
		
		TreeMap<DimensionMap,ROIPlus> roiMap = RoiReader.readObjectToRoiMap(roiData);
		int count = 0, percentage = 0;
		
		// Rois currently contains all the points for a given time, reorganize into tracks by finding nearest neighbors 
		// Loop through time and for each time, compare to next time and choose nearest neighbors and store in temp list of neighbors
		HashMap<DimensionMap,ROIPlus> trackRois = new HashMap<DimensionMap,ROIPlus>();
		PointList lastPoints = null, thisPoints = null;
		TrackHash tracks = new TrackHash();
		DimensionMap thisDim = null, lastDim = null;
		for(DimensionMap lDim : loopDims)
		{
			for(int nt = 1; nt < timeDims.size(); nt++)
			{
				// Create the dimension maps for getting data for tracking
				thisDim = new DimensionMap();
				thisDim.putAll(lDim);
				thisDim.putAll(timeDims.get(nt));
				thisPoints = roiMap.get(thisDim).getPointList();
				lastDim = new DimensionMap();
				lastDim.putAll(lDim);
				lastDim.putAll(timeDims.get(nt-1));
				lastPoints = roiMap.get(lastDim).getPointList();
				
				// Pair the points from the last timepoint to this timepoint
				List<Pair<IdPoint,IdPoint>> pairs = HashedPointList.getNearestNeighbors(lastPoints, thisPoints, radius, false);
				pairs = HashedPointList.filterConflicts(pairs);
				
				// Add the appropriate points to the accruing tracks
				for(Pair<IdPoint,IdPoint> pair : pairs)
				{
					IdPoint lastPoint = pair.p1;
					IdPoint thisPoint = pair.p2;
					lastPoint.id = nt-1;
					
					if(thisPoint != null)
					{
						thisPoint.id = nt;
						tracks.putPoint(lDim, lastPoint, thisPoint);
					}
					//JEXStatics.logManager.log(pair.toString(), 0, this);
				}
				//JEXStatics.logManager.log("\n", 0, this);
				
				// Update the hash table in tracks to be keyed by the newest points added to the pointLists
				tracks.updateTracks(lDim);
			}
			
			// Convert the PointLists to PointRois and save them in trackRois
			int trackNum = 1;
			for(PointList pl : tracks.getTracksList(lDim)) // getTrackList sorts by position of first point;
			{
				DimensionMap newDim = new DimensionMap();
				newDim.putAll(lDim);
				//newDim.put(timeDimName, timeDims.get(0).get(timeDimName));
				newDim.put(trackDimName, ""+trackNum);
				PointList temp = new PointList();
				temp.add(pl.get(0));
				ROIPlus roi = new ROIPlus(temp, ROIPlus.ROI_POINT);
				roi.setPattern(pl);
				trackRois.put(newDim, roi);
				trackNum = trackNum + 1;
			}
			
			// Update status indicator
			count = count + 1;
			percentage = (int) (100 * ((double) (count)/ ((double)loopDims.size())));
			JEXStatics.statusBar.setProgressPercentage(percentage);
		}
		
		JEXData output1 = RoiWriter.makeRoiObject(outputNames[0].getName(), trackRois);
		
		// Set the outputs
		realOutputs.add(output1);
		
		// Return status
		return true;
	}
	
}

class TrackHash implements Comparator<PointList> {
	
	HashMap<DimensionMap,HashMap<IdPoint,PointList>> trackHash = new HashMap<DimensionMap,HashMap<IdPoint,PointList>>();
	
	public void updateTracks(DimensionMap lDim)
	{
		HashMap<IdPoint,PointList> newTracks = new HashMap<IdPoint,PointList>();
		HashMap<IdPoint,PointList> tracks = this.getTracks(lDim);
		if(tracks == null)
		{
			return; // nothing to update
		}
		for(Point p : tracks.keySet())
		{
			PointList track = tracks.get(p);
			newTracks.put(track.lastElement(), track);
		}
		this.trackHash.put(lDim, newTracks);
	}
	
	public void putPoint(DimensionMap lDim, IdPoint lastPoint, IdPoint thisPoint)
	{
		HashMap<IdPoint,PointList> tracks = this.getTracks(lDim);
		if(tracks == null)
		{
			tracks = new HashMap<IdPoint,PointList>();
		}
		PointList track = tracks.get(lastPoint);
		if(track == null)
		{
			track = new PointList();
			track.add(lastPoint);
		}
		track.add(thisPoint);
		tracks.put(lastPoint, track);
		this.trackHash.put(lDim, tracks);
	}
	
	public PointList getTrack(DimensionMap lDim, IdPoint p)
	{
		HashMap<IdPoint,PointList> tracks = this.getTracks(lDim);
		if(tracks == null)
		{
			return null;
		}
		return tracks.get(p);
	}
	
	public HashMap<IdPoint,PointList> getTracks(DimensionMap lDim)
	{
		HashMap<IdPoint,PointList> tracks = this.trackHash.get(lDim);
		return tracks;
	}
	
	public List<PointList> getTracksList(DimensionMap lDim)
	{
		HashMap<IdPoint,PointList> tracks = this.getTracks(lDim);
		List<PointList> ret = new Vector<PointList>();
		if(tracks == null)
		{
			return ret;
		}
		for(IdPoint p : tracks.keySet())
		{
			ret.add(tracks.get(p));
		}
		Collections.sort(ret,new TrackHash());
		return ret;
		
	}
	
	public int compare(PointList pl1, PointList pl2)
	{
		Point origin = new Point(0,0);
		return (int) Math.signum(origin.distance(pl1.get(0)) - origin.distance(pl2.get(0)));
	}
	
	public void clear()
	{
		this.trackHash.clear();
	}
	
}




