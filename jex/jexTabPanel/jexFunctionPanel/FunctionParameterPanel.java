package jex.jexTabPanel.jexFunctionPanel;

import guiObject.JParameterPanel;

import javax.swing.JLabel;
import javax.swing.JPanel;

import cruncher.JEXFunction;

import jex.statics.DisplayStatics;
import net.miginfocom.swing.MigLayout;
import Database.Definition.ParameterSet;

public class FunctionParameterPanel {
	
	// Selected function and parameter pane
	private JEXFunction selectedFunction = null;
	private JPanel  panel	   = new JPanel();
	
	public FunctionParameterPanel()
	{
		initialize();
	}
	
	public JPanel panel()
	{
		return this.panel;
	}
	
	private void initialize()
	{
		// Make the parameter panel
		this.panel.setBackground(DisplayStatics.lightBackground);
		this.panel.setLayout(new MigLayout("center,flowy,ins 3","[center,fill,grow]","[center]5[center,fill,grow]"));
		this.panel.add(new JLabel("No function selected"));
	}
	
	public void selectFunction(JEXFunction function)
	{
		this.selectedFunction = function;
		this.panel.removeAll();
		
		if(this.selectedFunction == null)
		{
			JLabel functionName = new JLabel("No function selected");
			this.panel.add(functionName,"growx");
		}
		else
		{
			ParameterSet parameters = this.selectedFunction.getParameters();
			JParameterPanel paramPanel = new JParameterPanel(parameters);

			String funName = (function == null) ? "No function selected" : function.getFunctionName();
			JLabel functionName = new JLabel(funName);

			this.panel.add(functionName,"growx");
			this.panel.add(paramPanel,"grow");
		}

		this.panel.revalidate();
		this.panel.repaint();
	}

}
