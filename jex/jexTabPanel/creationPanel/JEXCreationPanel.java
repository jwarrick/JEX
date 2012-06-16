package jex.jexTabPanel.creationPanel;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import jex.experimentalTreeAndViewer.ExperimentalTreeController;
import jex.statics.DisplayStatics;
import jex.statics.JEXStatics;
import signals.SSCenter;
import Database.Definition.HierarchyLevel;

public class JEXCreationPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	private ExperimentalTreeController creationArrayController;

	JEXCreationPanel()
	{
		initialize();
	}
	
	/**
	 * Detach the signals
	 */
	public void deInitialize()
	{
		SSCenter.defaultCenter().disconnect(creationArrayController);
		SSCenter.defaultCenter().disconnect(this);
	}
	
	private void initialize()
	{
		// Initialize the gui components
		creationArrayController = new ExperimentalTreeController();
		creationArrayController.setTreeMode(ExperimentalTreeController.TREE_MODE_CREATION);
		JScrollPane scroll = new JScrollPane(creationArrayController.panel());
		scroll.setBorder(BorderFactory.createEmptyBorder());
		
		// Place the components in the split pane
		this.setLayout(new BorderLayout());
		this.setBackground(DisplayStatics.background);
		
		// Place the components in this panel
		this.add(scroll,BorderLayout.CENTER);
		
		// Connect the signals
		SSCenter.defaultCenter().connect(creationArrayController, ExperimentalTreeController.VIEWED_LEVEL_CHANGE_UP, this, "viewedHierarchyLevelChange", new Class[]{HierarchyLevel.class});
		
	}
	
	public void viewedHierarchyLevelChange(HierarchyLevel hlevel)
	{
		JEXStatics.logManager.log("Signal change for viewing a different hierarchy level received", 1, this);
		SSCenter.defaultCenter().emit(this, ExperimentalTreeController.VIEWED_LEVEL_CHANGE_UP, new Object[]{hlevel});
	}
}