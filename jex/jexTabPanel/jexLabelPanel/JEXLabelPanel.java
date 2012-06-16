package jex.jexTabPanel.jexLabelPanel;

import javax.swing.JPanel;

import jex.statics.DisplayStatics;

public class JEXLabelPanel {
	
	private static final long serialVersionUID = 1L;
	
	private JPanel panel;

	JEXLabelPanel()
	{
		initialize();
	}

	private void initialize()
	{
		this.panel = new JPanel();
		this.panel.setBackground(DisplayStatics.background);
	}
	
	public JPanel panel()
	{
		return this.panel;
	}


}
