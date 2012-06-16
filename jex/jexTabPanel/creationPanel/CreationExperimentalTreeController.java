package jex.jexTabPanel.creationPanel;

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

public class CreationExperimentalTreeController {
	public static String VIEWED_LEVEL_CHANGE = "VIEWED_LEVEL_CHANGE";
	public static Color defaultBackground = DisplayStatics.lightBackground;
	public static Color selectedBackground = DisplayStatics.selectedLightBBackground;
	
	// Model
	public JEXCreationPanelController controller;
	public TreeMap<String,Experiment> experiments ;
	public List<CreationExperimentController> cellControllers ;
	
	private HierarchyLevel currentlyViewed = null;

	// GUI
	private CreationArrayPanel panel;

	public CreationExperimentalTreeController()
	{
		SSCenter.defaultCenter().connect(JEXStatics.jexManager, JEXManager.EXPERIMENTTREE_CHANGE, this, "rebuildModel", (Class[])null);
		
		rebuildModel();
	}
	
	public void rebuildModel()
	{
		// Get the new model
		experiments = JEXStatics.jexManager.getExperimentTree();
		experiments = (experiments == null)? new TreeMap<String,Experiment>() : experiments;
		
		// Rebuild the controllers
		cellControllers = new ArrayList<CreationExperimentController>();
		for (Experiment exp: experiments.values())
		{
			CreationExperimentController cellController = new CreationExperimentController(this);
			cellController.setExperiment(exp);
			cellControllers.add(cellController);
		} 
		
		panel().rebuild();
	}
	
	public void setViewedInInspector(HierarchyLevel currentlyViewed)
	{
		this.currentlyViewed = currentlyViewed;
		SSCenter.defaultCenter().emit(this, VIEWED_LEVEL_CHANGE, new Object[]{currentlyViewed});
	}
	
	public boolean isViewedInInspector(HierarchyLevel viewed)
	{
		if (this.currentlyViewed == viewed) return true;
		return false;
	}
	
	public HierarchyLevel getCurrentlyViewed()
	{
		return this.currentlyViewed;
	}
	
	///
	/// GETTERS AND SETTERS 
	///

	public CreationArrayPanel panel()
	{
		if (panel == null)
		{
			panel = new CreationArrayPanel();
		}
		return panel;
	}


	class CreationArrayPanel extends JPanel
	{
		private static final long serialVersionUID = 1L;

		CreationArrayPanel()
		{
			rebuild();
		}
		
		public void rebuild()
		{
			// remove all
			this.removeAll();
			
			// Set the mig layout
			this.setLayout(new MigLayout("flowy, ins 0","10[fill,grow]10",""));
			this.setBackground(DisplayStatics.background);
			
			// Add all the components
			for (CreationExperimentController cellController: cellControllers)
			{
				this.add(cellController.panel(),"gapy 5");
			}
			
			// refresh display
			this.invalidate();
			this.validate();
			this.repaint();
		}

	}

}
