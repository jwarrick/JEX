package jex.jexTabPanel.creationPanel;

import javax.swing.JPanel;

import jex.JEXManager;
import jex.experimentalTreeAndViewer.ExperimentalTreeController;
import jex.jexTabPanel.JEXTabPanelController;
import jex.statics.JEXStatics;
import signals.SSCenter;
import Database.Definition.HierarchyLevel;

public class JEXCreationPanelController extends JEXTabPanelController{

	public static String CHANGE_EXP_NAME = "";
	public static String CHANGE_EXP_INFO = "";
	public static String CHANGE_EXP_DATE = "";
	public static String CHANGE_TRAY_NAME = "";
	public static String EDIT_LEVEL = "EDIT_LEVEL";
	
	// Controllers
	private JEXCreationPanel      centerPane ;
	private JEXCreationRightPanel rightPane;

	public JEXCreationPanelController()
	{
		centerPane = new JEXCreationPanel();
		SSCenter.defaultCenter().connect(centerPane, ExperimentalTreeController.VIEWED_LEVEL_CHANGE_UP, this, "viewedHierarchyLevelChange", new Class[]{HierarchyLevel.class});
		rightPane = new JEXCreationRightPanel();
		SSCenter.defaultCenter().connect(this, ExperimentalTreeController.VIEWED_LEVEL_CHANGE_UP, rightPane, "viewedHierarchyLevelChange", new Class[]{HierarchyLevel.class});
		SSCenter.defaultCenter().connect(JEXStatics.jexManager, JEXManager.NAVIGATION, this, "navigationChanged", (Class[])null);
	}

	//////
	////// EVENTS
	//////
	
	public void navigationChanged()
	{
		
	}
	
	public void viewedHierarchyLevelChange(HierarchyLevel hlevel)
	{
		JEXStatics.logManager.log("Signal change for viewing a different hierarchy level received", 1, this);
		SSCenter.defaultCenter().emit(this, ExperimentalTreeController.VIEWED_LEVEL_CHANGE_UP, new Object[]{hlevel});
	}
	
	//////
	////// JEXTabPanel interface
	//////
	
	public JPanel getMainPanel()
	{				
		return centerPane;
	}
	
	public JPanel getLeftPanel()
	{
		return null;
	}
	
	public JPanel getRightPanel()
	{
		return rightPane;
	}
	
	public void closeTab()
	{
		if (rightPane != null) rightPane.deInitialize();
		if (centerPane != null) centerPane.deInitialize();
		centerPane = null;
		rightPane = null;
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

