package jex.jexTabPanel.jexDistributionPanel;

import guiObject.DialogGlassPane;

import java.awt.Point;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JPanel;

import jex.JEXManager;
import jex.jexTabPanel.JEXTabPanelController;
import jex.statics.JEXStatics;
import signals.SSCenter;
import utilities.ArrayUtility;
import utilities.Pair;
import Database.DBObjects.JEXEntry;
import Database.DBObjects.dimension.DimensionMap;
import Database.Definition.Experiment;
import Database.Definition.Tray;
import cruncher.ImportThread;

public class JEXDistributionPanelController extends JEXTabPanelController{
	
	// Other controllers
	public DistributorArray          importController ;
	public JEXDistributionRightPanel fileController ;
	public FileListPanel             flistpane;
	
	// Navigation
	public Tray curTray = null;
	public HashMap<Point,Vector<Pair<DimensionMap,String>>> files;
	
	// Variables
	public HashMap<Integer,DimensionSelector> dimensions ;
	public int dimension = 0;	
	
	public JEXDistributionPanelController()
	{
		importController = new DistributorArray(this);
		fileController   = new JEXDistributionRightPanel(this);
		files            = new HashMap<Point,Vector<Pair<DimensionMap,String>>>();
		SSCenter.defaultCenter().connect(JEXStatics.jexManager, JEXManager.NAVIGATION, this, "navigationChanged", (Class[])null);
		
	}
		
	////////////////////
	// SIGNAL METHODS //
	////////////////////
	
	public void navigationChanged()
	{
		// Clear changes
		this.files.clear();
		
		// If tray viewed, change array view
		this.curTray = null;
		String viewedExp = JEXStatics.jexManager.getExperimentViewed();
		String viewedArray = JEXStatics.jexManager.getArrayViewed();
		
		if(viewedExp != null)
		{
			TreeMap<String,Experiment> expTree = JEXStatics.jexManager.getExperimentTree();
			Experiment exp = expTree.get(viewedExp);
			if(viewedArray != null)
			{
				this.curTray = exp.get(viewedArray);
			}
		}
		
		if (importController!= null) importController.navigationChanged();
	}
	
	public void fileListChanged()
	{
		List<File> files = flistpane.files2Distribute;
		fileController.setFileList(files);
	}
	
	public void addFiles2Distribute(List<File> files2Distribute)
	{
		if (flistpane == null) flistpane = new FileListPanel(this);
		if (flistpane.files2Distribute == null) flistpane.files2Distribute = new java.util.ArrayList<File>();
		
		for (File f: files2Distribute)
		{
			flistpane.files2Distribute.add(f);
		}
		fileController.setFileList(flistpane.files2Distribute);
	}
	
	/////////////////////////
	// PREPARATION METHODS //
	/////////////////////////
	
	/**
	 * Open the file selector panel
	 */
	public void selecFiles()
	{
		DialogGlassPane diagPanel = new DialogGlassPane("Choose files");
		diagPanel.setSize(350, 500);
		
		if (flistpane == null) flistpane = new FileListPanel(this);
		diagPanel.setCentralPanel(flistpane);
		
		JEXStatics.main.displayGlassPane(diagPanel,true);
	}
	
	/**
	 * Deal the files
	 */
	public void dealFiles()
	{
		//clear();
		
		// Set up the vectors for the ticking
		String[] dimVector = new String[dimensions.size()];
		int[] maxIncr = new int[dimensions.size()];
		int[] initial = new int[dimensions.size()];
		int[] increments = new int[dimensions.size()];
		int[] compteur = new int[dimensions.size()];
		int rowLoc = -1;
		int colLoc = -1;
		for (int i=0, len=dimensions.size(); i<len; i++){
			DimensionSelector selector = dimensions.get(new Integer(dimensions.size()-1-i));
			dimVector[i] = selector.getDimensionName();
			maxIncr[i] = selector.getDimensionSize() - 1;
			if (dimVector[i].equals("Array Column")) {
				maxIncr[i] = importController.getNumberRows()-1; 
				if (colLoc != -1) colLoc = -2;
				else colLoc = i;
			}
			if (dimVector[i].equals("Array Row")) {
				maxIncr[i] = importController.getNumberColumns()-1; 
				if (rowLoc != -1) rowLoc = -2;
				else rowLoc = i;
			}
			increments[i] = 1;
			initial[i] = 0;
			compteur[i] = 0;
		}
		
		// Use this to keep track of how many we have distributed to a location.
		HashMap<String, Integer> numDistributed = new HashMap<String, Integer>();
		
		// Test if the counter can perform
		if (rowLoc < 0 || colLoc < 0) {
			JEXStatics.logManager.log("File distribution impossible... Must select array and row dimensions once and only once", 1, this);
			JEXStatics.statusBar.setStatusText("Error: Must select array and row dimensions once and only once");
			return;
		}
		
		// Initialize numDistributed
		for(int y = 0; y <= maxIncr[rowLoc]; y++)
		{
			for(int x = 0; x <= maxIncr[colLoc]; x++)
			{
				numDistributed.put(""+x+","+y, 0);
			}
		}
		// Calculate max to distribute per entry
		// (i.e. use the dimension limits to calculate how many files can be held in each well
		// if every dimension value were to be used)
		// Use this instead of checking what arrays have been visited
		// like we did before. We can't use that method because we have to index through each
		// entry multiple times to deal out all the files instead of just going into each well
		// and putting everything in there all at once.
		int maxDist = 1;
		for(int i : maxIncr)
		{
			maxDist = maxDist*(i+1);
		}
		maxDist = maxDist/(maxIncr[rowLoc]+1);
		maxDist = maxDist/(maxIncr[colLoc]+1);
		
		// Do the dealing
		// Check to see if there are any wells selected. If not we risk
		// an infinite loop below during dealing where we skip non-selected wells.
		Set<JEXEntry> selected = JEXStatics.jexManager.getSelectedEntries();
		if (selected == null || selected.size() == 0) {
			return;
		}
		
		files = new HashMap<Point,Vector<Pair<DimensionMap,String>>>();
		for (File f: flistpane.files2Distribute){
			int cellX = compteur[colLoc];
			int cellY = compteur[rowLoc];
			
			// if the cell at location cellX and cellY is not valid skip it and go to the next valid cell
			// We know from the test about 10 lines up that there is at least one cell selected
			// so we don't risk an infinite loop and don't need to test what locations we have visited previously.
			while (!isValidCell(cellX,cellY))
			{
				// Go to the next cell in the array
				JEXStatics.logManager.log("Skipped cell at location "+cellX+"-"+cellY, 1, this);
				
				compteur = ArrayUtility.getNextCompteur(compteur, initial, maxIncr, increments);
				cellX = compteur[colLoc];
				cellY = compteur[rowLoc];
			}
			
			// Check to see how many files we've distributed to this location. If it
			// is greater than maxDist, then stop because there are no more slots for
			// any more files in any entry (i.e. avoid overdistributing)
			int curCount = numDistributed.get(""+cellX+","+cellY);
			if(curCount >= maxDist)
			{
				break;
			}
			
			// Fill the treemap of files
			Vector<Pair<DimensionMap,String>> richFileMap = files.get(new Point(cellX,cellY));
			if (richFileMap == null)
			{
				richFileMap = new Vector<Pair<DimensionMap,String>>();
				files.put(new Point(cellX,cellY), richFileMap);
			}
			DimensionMap map = makeMap(compteur,dimVector);
			richFileMap.add(new Pair<DimensionMap,String>(map, f.getAbsolutePath()));
			
			// Update the number of files that have distributed to this location.
			numDistributed.put(""+cellX+","+cellY, curCount + 1);			
			compteur = ArrayUtility.getNextCompteur(compteur, initial, maxIncr, increments);
		}
		
		// Refresh the displayed array
		importController.setFileArray(files);
		JEXStatics.logManager.log("Finished dropping files.", 0, this);
	}
	
	/**
	 * Reset the file list
	 */
	public void clear()
	{
		files = new HashMap<Point,Vector<Pair<DimensionMap,String>>>();
		importController.setFileArray(files);
	}
	
	/**
	 * Return if cell at location x and y accepts drops
	 * @param x
	 * @param y
	 * @return true if cell accepts drops
	 */
	private boolean isValidCell(int x, int y){
		HashMap<Point,Boolean> selectionArray = importController.getSelectionArray();
		Boolean selected = selectionArray.get(new Point(x,y));
		if (selected == null) return false;
		else return true;
	}
		

	//////////////////////
	// CREATION METHODS //
	//////////////////////
	
	
	/**
	 * Create the labels that have been droped
	 */
	public void createObjects()
	{
		if(curTray == null)
		{
			JEXStatics.logManager.log("Couldn't create objects because no tray is selected.", 0, this);
			JEXStatics.statusBar.setStatusText("Couldn't create objects because no tray is selected.");
			return;
		}
		
		// Do the real distribution
		String objectName = fileController.getObjectName();
		String objectInfo = fileController.getObjectInfo();
		String objectType = fileController.getObjectType();
		
		TreeMap<JEXEntry,Vector<Pair<DimensionMap,String>>> importObject = new TreeMap<JEXEntry,Vector<Pair<DimensionMap,String>>>();
		for(int x = 0; x < importController.getNumberColumns(); x++)
		{
			for(int y = 0; y < importController.getNumberRows(); y++)
			{
				TreeMap<Integer,JEXEntry> columnEntries = curTray.get(y);
				if (columnEntries == null) continue;
				JEXEntry e = columnEntries.get(x);
				if (e == null)
				{
					continue;
				}
				
				Vector<Pair<DimensionMap,String>> files2Drop = files.get(new Point(y,x));
				if(files2Drop != null)
				{
					importObject.put(e, files2Drop);
				}
			}
		}
		ImportThread importThread = new ImportThread(objectName, objectType, objectInfo, importObject);
		JEXStatics.cruncher.runGuiTask(importThread);
	}
		
	/**
	 * Grab a file from a list of args
	 * @param args
	 * @return rich file with dimension info
	 */
	private DimensionMap makeMap(int[] compteur, String[] dimVector){
		DimensionMap map = new DimensionMap();
		
		int index = 1;
		for (int i=0, len=compteur.length; i<len; i++){
			String dimName = dimVector[i];
			if (dimName.equals("Array Row") || dimName.equals("Array Column")) continue;
			
			String  dn = dimName;
			Integer dv = compteur[i];
			map.put(dn, ""+dv);
			
			index ++;
		}
		
		return map;
	}
	
	//////
	
	
	//////
	////// JEXTabPanel interface
	//////
	
	public JPanel getMainPanel()
	{
		return importController.panel();
	}
	
	public JPanel getLeftPanel()
	{
		return null;
	}
	
	public JPanel getRightPanel()
	{
		return fileController;
	}
	
	public void closeTab()
	{
		if (fileController != null) fileController.deInitialize();
		if (importController != null) importController.deInitialize();
		importController = null;
		fileController = null;
	}
	
	public int getFixedPanelWidth()
	{
		return this.fixedPanelWidth;
	}

	public void setFixedPanelWidth(int width)
	{
		this.fixedPanelWidth = width;
	}

	public double getResizeWeight()
	{
		return this.resizeWeight;
	}
}

