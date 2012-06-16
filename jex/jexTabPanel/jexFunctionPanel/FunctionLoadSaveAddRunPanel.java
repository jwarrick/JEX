package jex.jexTabPanel.jexFunctionPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import jex.statics.DisplayStatics;
import net.miginfocom.swing.MigLayout;

public class FunctionLoadSaveAddRunPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	// GUI variables
	private JPanel panel;
	private FunctionListPanel parent;
	
	// Buttons
	private JButton loadButton = new JButton();
	private JButton saveButton = new JButton();
	private JButton addButton  = new JButton();
	private JButton runButton  = new JButton();
	private JCheckBox autoSave = new JCheckBox();
	
	public FunctionLoadSaveAddRunPanel(FunctionListPanel parent)
	{
		this.parent = parent;
		initialize();
	}
	
	public JPanel panel()
	{
		return this.panel;
	}
	
	private void initialize()
	{
		this.panel = new JPanel();
		this.panel.setBackground(DisplayStatics.lightBackground);
		//this.panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		this.panel.setLayout(new MigLayout("center, flowx, ins 0","[fill,grow,center]0[fill,grow,center]0[fill,grow,center]0[fill,grow,center]0[]"));
		//this.setMaximumSize(new Dimension(250,800));
		//this.setPreferredSize(new Dimension(250,200));

		// Create the add button
		loadButton.setText("LOAD");
		loadButton.setToolTipText("Click to add a function to the list");
		//loadButton.setPreferredSize(new Dimension(60,30));
		//loadButton.setMaximumSize(new Dimension(60,500));
		loadButton.addActionListener(this);

		saveButton.setText("SAVE");
		saveButton.setToolTipText("Click to add a function to the list");
		//saveButton.setPreferredSize(new Dimension(60,30));
		//saveButton.setMaximumSize(new Dimension(60,500));
		saveButton.addActionListener(this);

		addButton.setText("ADD");
		addButton.setToolTipText("Click to add a function to the list");
		//addButton.setPreferredSize(new Dimension(60,30));
		//addButton.setMaximumSize(new Dimension(60,500));
		addButton.addActionListener(this);

		// Create the run all button
		runButton.setText("RUN");
		runButton.setToolTipText("Click to add a function to the list");
		//runButton.setPreferredSize(new Dimension(60,30));
		//runButton.setMaximumSize(new Dimension(60,500));
		runButton.addActionListener(this);
		
		// Create autoSave checkBox
		autoSave.setText("Auto-Save");

		// Create the button panel
		this.panel.setBackground(DisplayStatics.lightBackground);
		this.panel.add(loadButton,"growx, width 10:10:");
		this.panel.add(saveButton,"growx, width 10:10:");
		this.panel.add(addButton,"growx, width 10:10:");
		this.panel.add(runButton,"growx, width 10:10:");
		this.panel.add(autoSave);
	}	
	
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == addButton)
		{
			parent.addFunction();
		}
		else if (e.getSource() == this.runButton)
		{
			parent.runAllFunctions(this.autoSave.isSelected());
		}
		else if (e.getSource() == this.loadButton)
		{
			parent.loadFunctionList();
		}
		else if (e.getSource() == this.saveButton)
		{
			parent.saveFunctionList();
		}
	}

}
