package jex.jexTabPanel.jexViewPanel;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import jex.JEXManager;
import jex.arrayView.ArrayViewController;
import jex.experimentalTreeAndViewer.ExperimentalTreeController;
import jex.statics.DisplayStatics;
import jex.statics.JEXStatics;
import signals.SSCenter;
import Database.Definition.Experiment;
import Database.Definition.HierarchyLevel;
import Database.Definition.Tray;

public class JEXViewPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	private ExperimentalTreeController creationArrayController;
	private ArrayViewController arrayPaneController;

	JEXViewPanel()
	{
		initialize();
		
		SSCenter.defaultCenter().connect(JEXStatics.jexManager, JEXManager.NAVIGATION, this, "navigationChanged", (Class[])null);
	}
	
	/**
	 * Detach the signals
	 */
	public void deInitialize()
	{
		SSCenter.defaultCenter().disconnect(creationArrayController);
		SSCenter.defaultCenter().disconnect(arrayPaneController);
		SSCenter.defaultCenter().disconnect(this);
	}
	
	private void initialize()
	{
		// make a controller
		JEXStatics.logManager.log("Initializing new controller", 1, this);
		creationArrayController = new ExperimentalTreeController();
		creationArrayController.setTreeMode(ExperimentalTreeController.TREE_MODE_VIEW);
		SSCenter.defaultCenter().connect(creationArrayController, ExperimentalTreeController.OPEN_ARRAY, this, "openArray", new Class[]{HierarchyLevel.class});

		navigationChanged();
	}
	
	public void navigationChanged()
	{
		JEXStatics.logManager.log("Navigation changed, displaying update", 1, this);
		
		// Get the viewed experiment
		String expViewed = JEXStatics.jexManager.getExperimentViewed();
		String arrayViewed = JEXStatics.jexManager.getArrayViewed();
		
		// If an array is viewed open the array
		if (expViewed != null && arrayViewed != null)
		{
			Experiment exp = JEXStatics.jexManager.getExperimentTree().get(expViewed);
			if (exp == null) return;
			
			Tray tray = exp.get(arrayViewed);
			if (tray == null) return;
			
			openArray(tray);
		}
		else if (expViewed == null || arrayViewed == null)
		{
			arrayPaneController = null;
			displayTree();
		}
	}
	
	public void openArray(HierarchyLevel tray)
	{
		if (tray instanceof Tray)
		{
			displayArray((Tray)tray);
		}
	}
	
	public void arrayClosed()
	{
		arrayPaneController = null;
		displayTree();
		
		String expViewed = JEXStatics.jexManager.getExperimentViewed();
		JEXStatics.jexManager.setExperimentAndArrayViewed(expViewed, null);
	}
	
	private void displayTree()
	{
		JEXStatics.logManager.log("Displaying experimental tree", 1, this);
		
		// Remove all
		this.removeAll();
		
		// Set graphics
		this.setLayout(new BorderLayout());
		this.setBackground(DisplayStatics.background);
		
		// Make the array controller
		JScrollPane scroll = new JScrollPane(creationArrayController.panel());
		scroll.setBorder(BorderFactory.createEmptyBorder());
		this.add(scroll,BorderLayout.CENTER);
		
		// REvalidate
		this.revalidate();
		this.repaint();
	}
	
	private void displayArray(Tray tray)
	{
		JEXStatics.logManager.log("Displaying experimental array", 1, this);
		
		// Remove all
		this.removeAll();
		
		// Set graphics
		this.setLayout(new BorderLayout());
		this.setBackground(DisplayStatics.background);
		
		// Make the array controller
		arrayPaneController = new ArrayViewController();
		arrayPaneController.setArray(tray);
		SSCenter.defaultCenter().connect(arrayPaneController, ArrayViewController.CLOSE_ARRAY, this, "arrayClosed", (Class[])null);
		
		// Place the components in this panel
		this.add(arrayPaneController.panel(),BorderLayout.CENTER);
		
		// REvalidate
		this.revalidate();
		this.repaint();
	}
	
}