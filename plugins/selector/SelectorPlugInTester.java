package plugins.selector;

import java.awt.BorderLayout;
import java.util.TreeMap;

import javax.swing.WindowConstants;

import jex.statics.DisplayStatics;
import jex.statics.JEXStatics;
import plugins.plugin.PlugIn;
import plugins.plugin.PlugInController;
import Database.Definition.Experiment;

public class SelectorPlugInTester implements PlugInController {
	
	public PlugIn dialog;
	public QuickSelector selector;
	
	public SelectorPlugInTester(TreeMap<String,Experiment> experiments)
	{
		this.initizalizeDialog();
		this.selector = new QuickSelector();
		this.dialog.getContentPane().add(this.selector.panel(), BorderLayout.CENTER);
		this.dialog.setVisible(true);
	}
	
	private void initizalizeDialog()
	{
		this.dialog = new PlugIn(this);
		this.dialog.setBounds(100, 100, 800, 600);
		this.dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.dialog.getContentPane().setBackground(DisplayStatics.background);
		this.dialog.getContentPane().setLayout(new BorderLayout());
		
	}
	
	public void selectionChanged()
	{
		JEXStatics.logManager.log("SelectorPanel selection changed signal received.", 0, this);
	}
	
	/**
	 * Called upon closing of the window
	 */
	public void finalizePlugIn()
	{
		this.dialog = null;
	}
	
	public PlugIn plugIn()
	{
		return this.dialog;
	}

}
