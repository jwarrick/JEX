package jex.infoPanels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jex.JEXManager;
import jex.statics.DisplayStatics;
import jex.statics.JEXStatics;
import net.miginfocom.swing.MigLayout;
import signals.SSCenter;
import Database.DBObjects.JEXData;
import Database.DBObjects.JEXEntry;
import Database.Definition.HierarchyLevel;
import Database.Definition.TypeName;
import Database.SingleUserDatabase.tnvi;

public class LabelInfoPanelController extends InfoPanelController {
	private static final long serialVersionUID = 1L;
	
	// Signals
	public static String SELECTED_LABEL_NAME  = "SELECTED_LABEL_NAME"  ;
//	public static String SELECTED_LABEL_VALUE = "SELECTED_LABEL_VALUE" ;
	
	// Model
	private List<String> labelNames ;
	private List<String> labelValues ;
	private String selectedName ;
//	private String selectedValue ;
	
	public LabelInfoPanelController(){
		// Signup for navigation change signals
		SSCenter.defaultCenter().connect(JEXStatics.jexManager, JEXManager.NAVIGATION, this, "availableLabelChange");
		SSCenter.defaultCenter().connectWithOneStringArgument(JEXStatics.jexManager, SELECTED_LABEL_NAME, this, "setSelectedLabelName");
//		SSCenter.defaultCenter().connectWithOneStringArgument(JEXStatics.jexManager, SELECTED_LABEL_VALUE, this, "setSelectedLabelValue");
		
		// Initialize the variables
		initialize();
	}
	
	private void initialize()
	{
		// Set variables
		selectedName  = null;
//		selectedValue = null;
		labelNames    = new ArrayList<String>(0);
		labelValues   = new ArrayList<String>(0);
		TypeName selectedLabel = JEXStatics.jexManager.getSelectedLabel();
		if(selectedLabel != null)
		{
			this.setSelectedLabelName(selectedLabel.getName());
		}
		
	}
	
	///// METHODS
	public void setSelectedLabelName(String labelName){
		// Control for invalid labelNames
		if (labelName.equals("")) labelName = null;
		
		// Set the current label name selection
		this.selectedName = labelName;
		
		// Set the label name as the selected label
		JEXStatics.jexManager.setSelectedLabel(new TypeName(JEXData.LABEL,selectedName));
		
		// MAKE A LIST OF VALUES FOR THE CURRENTLY SELECTED LABEL NAME FROM THE VIEWED ENTRIES
		makeValueList();
		
		// Refresh the gui
		refreshGUI();
	}
	
	public String getSelectedLabelName()
	{
		return this.selectedName;
	}
	
//	public void setSelectedLabelValue(String text)
//	{
//		// set the selected label value
//		this.selectedValue = text;
//		
//		// Refresh gui
//		refreshGUI();
//	}
//	
//	public String getSelectedLabelValue()
//	{
//		return this.selectedValue;
//	}
	
	public void availableLabelChange()
	{
		JEXStatics.logManager.log("Available Label Change", 0, this);
		// Reset the label name list
		labelNames    = new ArrayList<String>(0);
		
		// get the current tnvi
		tnvi TNVI = JEXStatics.jexManager.getFilteredTNVI();
		
		// grab the available label names
		TreeMap<String,TreeMap<String,Set<JEXEntry>>> nvi = TNVI.get(JEXData.LABEL);
		
		// Add each label name to the list
		labelNames.add("");
		if(nvi != null)
		{
			for (String labelName: nvi.keySet())
			{
				labelNames.add(labelName);
			}
		}

		// Make the value list
		makeValueList();
		
		// Refresh the gui
		refreshGUI();
	}
	
	public void refreshGUI()
	{
		// Set a new InfoPanel
		SSCenter.defaultCenter().emit(JEXStatics.jexManager, JEXManager.INFOPANELS, (Object[])null);
	}
	
	public InfoPanel panel()
	{
		LabelInfoPanel result = new LabelInfoPanel();
		return result;
	}
	
	///// PRIVATE METHODS
	private void makeValueList()
	{
		// IF the selectedname is null, value list is empty
		labelValues   = new ArrayList<String>(0);
		if (this.selectedName == null) return;
		
		// Get the viewed entries
		HierarchyLevel viewedHierarchy = JEXStatics.jexManager.getViewedHierarchyLevel();
		Set<JEXEntry> entries = viewedHierarchy.getEntries();
		
		// Loop through the entries and look for entries continaing a label named SELECTEDNAME
		for (JEXEntry entry: entries)
		{
			// get the tnv
			TreeMap<String,TreeMap<String,JEXData>> tnv = entry.getDataList();
			if (tnv == null) continue;
			
			// Get the NV
			TreeMap<String,JEXData> nv = tnv.get(JEXData.LABEL);
			if (nv == null) continue;
			
			// Get the V
			JEXData data = nv.get(this.selectedName);
			if (data == null) continue;
			
			// Add the value to the list of values
			labelValues.add(data.getDictionaryValue());
		}
	}
	
	
	
	
	class LabelInfoPanel extends InfoPanel implements ActionListener
	{
		private static final long serialVersionUID = 1L;
		
		// GUI
		private JButton   stopDisplaying = new JButton("X");
		private JComboBox labelSelector  = new JComboBox();
		private JPanel    centerPanel ;
		
		private LabelInfoPanel()
		{
			makePanel();
		}
		
		private void makePanel()
		{
			// Make the label chooser
			String[] labelStrings = labelNames.toArray(new String[0]);

			//Create the combo box
			labelSelector = new JComboBox(labelStrings);
			labelSelector.setSelectedItem(selectedName);
			labelSelector.addActionListener(this);
			
			// Create the label
			JLabel label = new JLabel("Display:");
			label.setForeground(DisplayStatics.intoPanelText);
			
			// Create the stop button
			stopDisplaying.addActionListener(this);
			
			// Make the headerpanel
			centerPanel    = new JPanel();
			centerPanel.setLayout(new MigLayout("ins 0","[60]2[fill,grow]2[25]",""));
			centerPanel.setBackground(transparent);
			centerPanel.add(label,"width 60,height 25!");
			centerPanel.add(labelSelector,"growx,height 25!");
			centerPanel.add(stopDisplaying,"width 25!,height 25!,wrap");
			
			// Create the label value panel
			makeLegend();
			
			// Make the rest of the GUI
			this.setTitle("Viewed label");
			this.setCenterPanel(centerPanel);
		}
		
		private void makeLegend()
		{
			// If no label is selected return null
			if (selectedName == null) 
			{
				return;
			}
			// Get the current label values
			TreeMap<String,TreeMap<String,Set<JEXEntry>>> labels = JEXStatics.jexManager.getLabels();
			
			// Get the current label selected values
			TreeMap<String,Set<JEXEntry>> VI = labels.get(selectedName);
			if (VI == null)
			{
				return;
			}
			
			// Add each value
			for (String value: VI.keySet())
			{
				// Make the label "panel"
				JPanel labelPane = new JPanel();
//				labelPane.setLayout(new BoxLayout(labelPane,BoxLayout.LINE_AXIS));
				labelPane.setLayout(new BorderLayout());
				
				// Make the color
				labelPane.setBackground(JEXStatics.labelColorCode.getColorForLabel(selectedName,value));
				
				// Make the label value
				JLabel newLabelValue = new JLabel(value);
				newLabelValue.setMaximumSize(new Dimension(100,15));
				newLabelValue.setForeground(Color.white);
				labelPane.add(newLabelValue);
				
				// Add it to the infopanel
				centerPanel.add(labelPane,"span,wrap,height 15,growx");
			}
		}
		
		///// EVENTS
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() == labelSelector)
			{
				String labelName = (String) labelSelector.getSelectedItem();
				SSCenter.defaultCenter().emit(JEXStatics.jexManager, SELECTED_LABEL_NAME, labelName);
//				setSelectedLabelName(labelName);
			}
			else if (e.getSource() == stopDisplaying)
			{
				SSCenter.defaultCenter().emit(JEXStatics.jexManager, SELECTED_LABEL_NAME, "");
//				setSelectedLabelName(null);
			}
		}
	}
}
