package plugins.workflow;

import java.awt.BorderLayout;

import javax.swing.WindowConstants;

import Database.DBObjects.JEXData;

import jex.statics.DisplayStatics;
import plugins.plugin.PlugIn;
import plugins.plugin.PlugInController;

public class TestImagePreview implements PlugInController
{
	private static final long serialVersionUID = 1L;
	
	private PlugIn dialog;
	private ImagePreview preview;
	
	public TestImagePreview(JEXData imageData, JEXData roiData)
	{
		this.preview = new ImagePreview(imageData, roiData);
		this.initizalizeDialog();
		this.dialog.setVisible(true);
	}
	
	private void initizalizeDialog()
	{
		this.dialog = new PlugIn(this);
		this.dialog.setBounds(100, 100, 800, 600);
		this.dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.dialog.getContentPane().setBackground(DisplayStatics.background);
		this.dialog.getContentPane().setLayout(new BorderLayout());
		this.dialog.getContentPane().add(this.preview.panel(), BorderLayout.CENTER);
		this.dialog.validate();
		this.dialog.repaint();
	}

	public void finalizePlugIn()
	{
		// Nothing to do
	}

	public PlugIn plugIn()
	{
		// TODO Auto-generated method stub
		return null;
	}
	

}
