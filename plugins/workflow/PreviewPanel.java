package plugins.workflow;

import javax.swing.JPanel;

import jex.statics.DisplayStatics;

import Database.DBObjects.JEXData;

public abstract class PreviewPanel {
	
	/**
	 * Return a panel showing a preview of the JEXData. Otherwise return a blank panel.
	 * @param data
	 * @param supportingData - like an image for a roi
	 * @return
	 */
	public JPanel panel(JEXData data, JEXData supportingData)
	{
		return blankPanel();
	}
	
	protected JPanel blankPanel()
	{
		JPanel ret = new JPanel();
		ret.setBackground(DisplayStatics.background);
		return ret;
	}

}
