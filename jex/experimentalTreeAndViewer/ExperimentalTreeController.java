package jex.experimentalTreeAndViewer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.swing.JPanel;

import jex.JEXManager;
import jex.statics.DisplayStatics;
import jex.statics.JEXStatics;
import net.miginfocom.swing.MigLayout;
import signals.SSCenter;
import Database.Definition.Experiment;
import Database.Definition.HierarchyLevel;
import Database.Definition.Tray;

public class ExperimentalTreeController {
	public static String VIEWED_LEVEL_CHANGE_UP   = "VIEWED_LEVEL_CHANGE_UP";
	public static String VIEWED_LEVEL_CHANGE_DOWN = "VIEWED_LEVEL_CHANGE_DOWN";
	public static String OPEN_ARRAY               = "OPEN_ARRAY";
	public static String OPEN_EXPERIMENT          = "OPEN_EXPERIMENT";
	public static int    TREE_MODE_CREATION       = 1;
	public static int    TREE_MODE_VIEW           = 2;
	public static Color  defaultBackground        = DisplayStatics.lightBackground;
	public static Color  selectedBackground       = DisplayStatics.selectedLightBBackground;
	
	// Model
	public TreeMap<String,Experiment> experiments ;
	public List<ExperimentalTreeExperimentController> cellControllers ;
	private HierarchyLevel currentlyViewed = null;
	private int            treeMode = TREE_MODE_VIEW;

	// GUI
	private ExperimentalTree panel;

	public ExperimentalTreeController()
	{
		SSCenter.defaultCenter().connect(JEXStatics.jexManager, JEXManager.EXPERIMENTTREE_CHANGE, this, "rebuildModel", (Class[])null);
		SSCenter.defaultCenter().connect(JEXStatics.jexManager, JEXManager.NAVIGATION, this, "navigationChanged", (Class[])null);
		
		rebuildModel();
	}
	
	public void rebuildModel()
	{
		JEXStatics.logManager.log("Rebuilding Experimental tree model", 1, this);
		
		// Get the new model
		experiments = JEXStatics.jexManager.getExperimentTree();
		experiments = (experiments == null)? new TreeMap<String,Experiment>() : experiments;
		
		// Rebuild the controllers
		cellControllers = new ArrayList<ExperimentalTreeExperimentController>();
		for (Experiment exp: experiments.values())
		{
			// Create a new experiment controller
			ExperimentalTreeExperimentController cellController = new ExperimentalTreeExperimentController(this);
			cellController.setExperiment(exp);
			
			// Link it to a signaling center
			SSCenter.defaultCenter().connect(cellController, ExperimentalTreeController.VIEWED_LEVEL_CHANGE_UP, this, "viewedLevelChangeUp", new Class[]{HierarchyLevel.class});
			SSCenter.defaultCenter().connect(this, ExperimentalTreeController.VIEWED_LEVEL_CHANGE_DOWN, cellController, "viewedLevelChangeDown", new Class[]{HierarchyLevel.class});
			
			// Add it to the list of controllers
			cellControllers.add(cellController);
		}
		
		// Set the expansion of experiments
		navigationChanged();
	}
	
	public HierarchyLevel getCurrentlyViewed()
	{
		return this.currentlyViewed;
	}
	
	public int treeMode()
	{
		return this.treeMode;
	}
	
	public void setTreeMode(int treeMode)
	{
		this.treeMode = treeMode;
		rebuildModel();
	}
	
	///
	/// ACTION
	///
	
	public void openArray(Tray tray)
	{
		JEXStatics.logManager.log("Emitting signal for opening an array", 1, this);
		SSCenter.defaultCenter().emit(this, OPEN_ARRAY, new Object[]{tray});
		
		String expViewed = JEXStatics.jexManager.getExperimentViewed();
		JEXStatics.jexManager.setExperimentAndArrayViewed(expViewed, tray.getName());
	}
	
	public void openExperiment(Experiment exp)
	{
		String expName = (exp == null)? "NULL" : exp.getName();
		JEXStatics.logManager.log("Emitting signal for opening an experiment - "+expName, 1, this);
		if (exp == null)
		{
			JEXStatics.jexManager.setExperimentAndArrayViewed(null, null);
		}
		else
		{
		JEXStatics.jexManager.setExperimentAndArrayViewed(exp.getName(), null);
		}
//		SSCenter.defaultCenter().emit(this, OPEN_EXPERIMENT, new Object[]{exp});
	}
	
	///
	/// SIGNALS 
	///
	
	public void navigationChanged()
	{
		JEXStatics.logManager.log("Recieved navigation changed signal", 1, this);
		
		// Get the viewed experiment
		String expViewed = JEXStatics.jexManager.getExperimentViewed();
		String arrayViewed = JEXStatics.jexManager.getArrayViewed();
		
		// If an array is viewed don't care about this
		if (expViewed != null && arrayViewed != null) return;
		
		// Loop through the experiment controllers and set them to expanded or colapsed
		for (ExperimentalTreeExperimentController expController :cellControllers)
		{
			// Get the exp Name
			String expName = expController.exp.getName();
			
			// If it is the one viewed expand it, else collapse it
			//JEXStatics.logManager.log("Setting expansion mode on exp "+expName, 1, this);
			if (expName.equals(expViewed))
			{
				expController.setExpanded(true);
			}
			else
			{
				expController.setExpanded(false);
			}
		}
		panel().rebuild();
	}

	public void viewedLevelChangeDown(HierarchyLevel currentlyViewed)
	{
		SSCenter.defaultCenter().emit(this, VIEWED_LEVEL_CHANGE_DOWN, new Object[]{currentlyViewed});
	}
	
	public void viewedLevelChangeUp(HierarchyLevel currentlyViewed)
	{
		// Collect the signal of a viewed level change
		this.currentlyViewed = currentlyViewed;
		
		// Emit a change signal to possible listeners
		SSCenter.defaultCenter().emit(this, VIEWED_LEVEL_CHANGE_UP, new Object[]{currentlyViewed});
		
		// Transmit the signal back down
		viewedLevelChangeDown(currentlyViewed);
	}
	
	///
	/// GETTERS AND SETTERS 
	///

	public ExperimentalTree panel()
	{
		if (panel == null)
		{
			panel = new ExperimentalTree();
		}
		return panel;
	}


	class ExperimentalTree extends JPanel
	{
		private static final long serialVersionUID = 1L;

		ExperimentalTree()
		{
			rebuild();
		}
		
		public void rebuild()
		{
			JEXStatics.logManager.log("Rebuilding Experimental tree panel", 1, this);
			
			// remove all
			this.removeAll();
			
			// Set the mig layout
			this.setLayout(new MigLayout("flowy, ins 0","10[fill,grow]10",""));
			this.setBackground(DisplayStatics.background);
			
			// Add all the components
			for (ExperimentalTreeExperimentController cellController: cellControllers)
			{
				this.add(cellController.panel(),"gapy 5");
			}
			
			// refresh display
			this.revalidate();
			this.repaint();
		}

	}

}
